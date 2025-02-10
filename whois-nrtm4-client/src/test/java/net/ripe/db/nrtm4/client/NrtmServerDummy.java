package net.ripe.db.nrtm4.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.nrtm4.client.processor.UpdateNotificationFileProcessor;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.compress.utils.Lists;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Profile({WhoisProfile.TEST})
@Component
public class NrtmServerDummy implements Stub {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServerDummy.class);

    private Server server;
    private int port = 0;

    public static final String RECORD_SEPARATOR = "\u001E";

    private final UpdateNotificationFileProcessor updateNotificationFileProcessor;

    private final List<Mock> mocks;

    private final static String PRIVATE_KEY = "{\"kty\":\"EC\",\"d\":\"s5oYmEj_z_PaY2CO5sSQjuj6YaPwkFlAQGg064LlJVQ\",\"crv\":\"P-256\",\"kid\":\"cbc61f4e-7b78-4b7d-b934-bb1f4174a75e\",\"x\":\"lNjob69Ki9GH0NJ6gbQnXO0n-aMiZrpq8aAC2U-IWAY\",\"y\":\"NAg9cxYj5qyyD7c7yoJpHpFGjHVWUVMGJQKYYKjukY8\"}";
    private final static String RIPE_NONAUTH_SNAP_HASH = "148c3c411b8f044f5fc0ab201f6dd03e80c862e27ad1a63488aee337dc7eb4a2";

    private final static String RIPE_SNAP_HASH = "7c9d1a1ebc73dc719e11c1046fae6598c35ae507a391d142beebe33865f077a0";

    private final static Map<String, String> RIPE_NONAUTH_DELTA_HASH = ImmutableMap.of(
            "1", "d671895977aa9dd8ca2b7ba74cd96cd28a786b0e19b3db709c9c5a02a52466ef",
            "2", "c0916fbf16da2972de2a9e78a79fe5390b9139d570ca24f5e17d280ca96078e4",
            "fake", "fake_hash"
    );

    private final static Map<String, String> RIPE_DELTA_HASH = ImmutableMap.of(
            "1", "e9c948811a949935aafbb55fbf180421bdf23f9527f2342e58d69013475d1709",
            "2", "60eaced7492f74e0e0b2009fd8354e0ddf66162276c22d56ea092f14fb5eddf6",
            "fake", "fake_hash"
    );


   private  final static String UNF_RIPE_TEMPLATE = """
                {
                  "nrtm_version": 4,
                  "timestamp": "2024-10-25T00:07:00Z",
                  "type": "notification",
                  "source": "RIPE",
                  "session_id": "4521174b-548f-4e51-98fc-dfd720011a0c",
                  "version": %s,
                  "snapshot": {
                    "version": 1,
                    "url": "nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": %s,
                      "url": "nrtm-delta.%s.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json",
                      "hash": "%s"
                    }
                  ]
                }
                """;

    private final static String UNF_RIPE_NONAUTH_TEMPLATE = """
                {
                  "nrtm_version": 4,
                  "timestamp": "2024-10-24T13:20:00Z",
                  "type": "notification",
                  "source": "RIPE-NONAUTH",
                  "session_id": "6328095e-7d46-415b-9333-8f2ae274b7c8",
                  "version": %s,
                  "snapshot": {
                    "version": 1,
                    "url": "nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": %s,
                      "url": "nrtm-delta.%s.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json",
                      "hash": "%s"
                    }
                  ]
                }
                """;

    @Autowired
    public NrtmServerDummy(final UpdateNotificationFileProcessor updateNotificationFileProcessor) {
        this.updateNotificationFileProcessor = updateNotificationFileProcessor;
        this.mocks = Lists.newArrayList();
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new NrtmTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();
        initialiseMocks();
        final String restUrl = String.format("http://localhost:%s/nrtmv4", getPort());
        LOGGER.info("NRTM Service dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(updateNotificationFileProcessor, "baseUrl", restUrl);
    }

    @PreDestroy
    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void reset() {
    }


    private class NrtmTestHandler extends AbstractHandler {
        @Override
        public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response)
                throws IOException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);

            for (Mock mock : mocks) {
                if (mock.matches(request)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    switch (mock) {
                        //TODO: [MH] Combine this first three
                        case NrtmResponseMock nrtmResponseMock -> {
                            response.setContentType(nrtmResponseMock.mediaType);
                            response.getWriter().println(mock.response());
                        }
                        case NrtmDeltaResponseMock nrtmDeltaResponseMock -> {
                            response.setContentType(nrtmDeltaResponseMock.mediaType);
                            response.getWriter().println(mock.response());
                        }
                        case NrtmSignedResponseMock nrtmSignedResponseMock -> {
                            response.setContentType(nrtmSignedResponseMock.mediaType);
                            response.getWriter().println(mock.response());
                        }
                        case NrtmCompressedResponseMock nrtmCompressedResponseMock -> {
                            response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                            response.getOutputStream().write(nrtmCompressedResponseMock.response.toByteArray());
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + mock);
                    }
                }
            }
        }
    }

    public void resetDefaultMocks(){
        mocks.clear();
        initialiseMocks();
    }

    public void setSecondDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, "2"), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, "2"), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
    }

    public void setFakeHashMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("1", "fake_hash", "fake"), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("1", "fake_hash", "fake"), "application/jose+json"));
    }

    public void setWrongSignedUNF(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.jose", "nrtm-RIPE-wrong-signature.jose", "application/jose+json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", "nrtm-RIPE-NONAUTH-wrong-signature.jose", "application/jose+json"));
    }

    private void initialiseMocks() {
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("1", RIPE_SNAP_HASH, "1"), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("1", RIPE_NONAUTH_SNAP_HASH, "1"), "application/jose+json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.1.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.1.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.1.RIPE-NONAUTH.json", "application/json"));
    }

    private interface Mock {

        String PATH = "mock/";
        boolean matches(final HttpServletRequest request);

        Object response();

        default String getResource(final String resource) {
            try {
                // resource is in file
                return Resources.toString(Resources.getResource(PATH + resource), Charset.defaultCharset());
            } catch (IllegalArgumentException e) {
                // resource doesn't exist (use resource as content)
                return resource;
            } catch (IOException e) {
                // error reading content from resource
                throw new IllegalStateException(e);
            }
        }

        default String getResourceAsRecords(final String resource){
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Resources.getResource(PATH + resource).openStream(), Charset.defaultCharset()))){

                return reader.lines()
                        .map(line -> RECORD_SEPARATOR + line)
                        .collect(Collectors.joining("\n"));
            } catch (IllegalArgumentException e) {
                // resource doesn't exist (use resource as content)
                throw new IllegalStateException(e);
            } catch (IOException e) {
                // error reading content from resource
                throw new IllegalStateException(e);
            }
        }

        default ByteArrayOutputStream getCompressedResource(final String searchKey) {
            try {
                // resource is in file
                final String jsonData = getResourceAsRecords(searchKey);
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)){
                    gzipOutputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));
                    gzipOutputStream.finish();
                    return byteArrayOutputStream;
                }
            } catch (IllegalArgumentException e) {
                // resource doesn't exist (use resource as content)
                throw new IllegalStateException(e);
            } catch (IOException e) {
                // error reading content from resource
                throw new IllegalStateException(e);
            }
        }
    }

    private record NrtmResponseMock(String fileType, String response, String mediaType) implements Mock {

        private NrtmResponseMock(final String fileType, final String response, final String mediaType) {
            this.fileType = fileType;
            this.response = getResource(response);
            this.mediaType = mediaType;
        }

        @Override
        public boolean matches(final HttpServletRequest request) {
            return request.getRequestURI().endsWith(fileType);
        }
    }

    private record NrtmSignedResponseMock(String fileType, String response, String mediaType) implements Mock {

        private NrtmSignedResponseMock(final String fileType, final String response, final String mediaType) {
            this.fileType = fileType;
            this.response = getResource(signWithJWS(response));
            this.mediaType = mediaType;
        }

        @Override
        public boolean matches(final HttpServletRequest request) {
            return request.getRequestURI().endsWith(fileType);
        }
    }

    private record NrtmDeltaResponseMock(String fileType, String response, String mediaType) implements Mock {

        private NrtmDeltaResponseMock(final String fileType, final String response, final String mediaType) {
            this.fileType = fileType;
            this.response = getResourceAsRecords(response);
            this.mediaType = mediaType;
        }

        @Override
        public boolean matches(final HttpServletRequest request) {
            return request.getRequestURI().endsWith(fileType);
        }
    }

    private static class NrtmCompressedResponseMock implements Mock {
        private final String fileType;
        private final String searchKey;
        private final ByteArrayOutputStream response;

        public NrtmCompressedResponseMock(final String fileType, final String searchKey) {
            this.fileType = fileType;
            this.searchKey = searchKey;
            this.response = getCompressedResource(searchKey);
        }

        @Override
        public boolean matches(HttpServletRequest request) {
            return request.getRequestURI().endsWith(fileType);
        }

        @Override
        public Object response() {
            return response;
        }
    }

    private static String signWithJWS(final String payload)  {

        try {
            final ECKey jwk = ECKey.parse(new String(PRIVATE_KEY.getBytes()));
            final JWSSigner signer = new ECDSASigner(jwk);

            final JWSObject jwsObject = new JWSObject(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(jwk.getKeyID()).build(),
                    new Payload(payload));

            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (ParseException | JOSEException ex) {
            LOGGER.error("failed to sign payload {}", ex.getMessage());
            throw new IllegalStateException("failed to sign contents of file");
        }
    }

    private static String getUpdateNotificationFileRIPE(final String unfVersion, final String snapHast, final String deltaVersion){
        return String.format(UNF_RIPE_TEMPLATE, unfVersion, snapHast, deltaVersion, deltaVersion, RIPE_DELTA_HASH.get(deltaVersion));
    }

    private static String getUpdateNotificationFileRIPENonAuth(final String unfVersion, final String snapHast, final String deltaVersion){
        return String.format(UNF_RIPE_NONAUTH_TEMPLATE, unfVersion, snapHast, deltaVersion, deltaVersion, RIPE_NONAUTH_DELTA_HASH.get(deltaVersion));
    }
}
