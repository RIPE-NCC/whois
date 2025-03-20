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
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
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

    private final NrtmRestClient nrtmRestClient;

    private final List<Mock> mocks;

    private final static String PRIVATE_KEY = "{\"kty\":\"EC\",\"d\":\"s5oYmEj_z_PaY2CO5sSQjuj6YaPwkFlAQGg064LlJVQ\",\"crv\":\"P-256\",\"kid\":\"cbc61f4e-7b78-4b7d-b934-bb1f4174a75e\",\"x\":\"lNjob69Ki9GH0NJ6gbQnXO0n-aMiZrpq8aAC2U-IWAY\",\"y\":\"NAg9cxYj5qyyD7c7yoJpHpFGjHVWUVMGJQKYYKjukY8\"}";
    private final static String RIPE_NONAUTH_SNAP_HASH = "148c3c411b8f044f5fc0ab201f6dd03e80c862e27ad1a63488aee337dc7eb4a2";

    private final static String RIPE_SNAP_HASH = "7c9d1a1ebc73dc719e11c1046fae6598c35ae507a391d142beebe33865f077a0";

    private final static Map<String, String> RIPE_NONAUTH_DELTA_HASH = ImmutableMap.of(
            "1", "5a889b779c018c9e62d82958e176f133d1d5f31fc64eed10a54a486ba4c29e03",
            "2", "632bc8d9a7a6d9587a05265786a58173d76df2ad4b7ceaa39a08fe261e197508",
            "3", "d16742be57b6c8c12be9e56696be363a0e78e68e91f703f52512180fbcfa9d49",
            "4", "19ea1a2dc2d97ead8abdad12149b7bfb7dd394d2618f48d71ce9f41f7689b47e",
            "fake", "fake_hash"
    );

    private final static Map<String, String> RIPE_DELTA_HASH = ImmutableMap.of(
            "1", "cb51f37f31f6132b674ff32f3734c42e3658383f1ad345bbe2f7989b01283ef0",
            "2", "e9b8e2c6a4d6f10f390010ad0852a015fca01d27b60fc52ece359e99c19c4ff1",
            "3", "ece98b808f42d55c7aa625dcc26ab7ec5b2967f3bd9ee3436330b6c65462a2c2",
            "4", "248f9778f222d167d5aad8f16e7687583a2126147cb05fb28a18bf75425c5faa",
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
                       %s
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
                        %s
                  ]
                }
                """;

    private final static String DELTA_TEMPLATE = """
                      {
                          "version": %s,
                          "url": "nrtm-delta.%s.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json",
                          "hash": "%s"
                      }
           """;

    private final static String DELTA_NON_AUTH_TEMPLATE = """
                      {
                          "version": %s,
                          "url": "nrtm-delta.%s.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json",
                          "hash": "%s"
                      }
            """;

    @Autowired
    public NrtmServerDummy(final NrtmRestClient nrtmRestClient) {
        this.nrtmRestClient = nrtmRestClient;
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
        ReflectionTestUtils.setField(nrtmRestClient, "baseUrl", restUrl);
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
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, List.of("2")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, List.of("2")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
    }

    public void setTwoAndThreeVersionDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, List.of("2", "3")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, List.of("2", "3")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
    }

    public void setTwoAndFourVersionDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, List.of("2", "4")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, List.of("2", "4")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.4.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.4.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.4.RIPE-NONAUTH.json", "application/json"));
    }

    public void setAllDeltas(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, List.of("1", "2", "3", "4")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, List.of("1", "2", "3", "4")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.1.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.1.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.1.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.4.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.4.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.4.RIPE-NONAUTH.json", "application/json"));
    }

    public void setFakeHashMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("1", "fake_hash", List.of("fake")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("1", "fake_hash", List.of("fake")), "application/jose+json"));
    }

    public void setWrongSignedUNF(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.jose", "nrtm-RIPE-wrong-signature.jose", "application/jose+json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", "nrtm-RIPE-NONAUTH-wrong-signature.jose", "application/jose+json"));
    }

    private void initialiseMocks() {
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("1", RIPE_SNAP_HASH, List.of("1")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("1", RIPE_NONAUTH_SNAP_HASH, List.of("1")), "application/jose+json"));
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

    private static String getUpdateNotificationFileRIPE(final String unfVersion, final String snapHast, final List<String> deltaVersion){
        final String deltas = deltaVersion.stream()
                .map(version -> String.format(DELTA_TEMPLATE, version, version, RIPE_DELTA_HASH.get(version)))
                .collect(Collectors.joining(",\n"));
        return String.format(UNF_RIPE_TEMPLATE, unfVersion, snapHast, deltas);
    }

    private static String getUpdateNotificationFileRIPENonAuth(final String unfVersion, final String snapHast, final List<String> deltaVersion){
        final String deltas = deltaVersion.stream()
                .map(version -> String.format(DELTA_NON_AUTH_TEMPLATE, version, version, RIPE_NONAUTH_DELTA_HASH.get(version)))
                .collect(Collectors.joining(",\n"));
        return String.format(UNF_RIPE_NONAUTH_TEMPLATE, unfVersion, snapHast, deltas);
    }
}
