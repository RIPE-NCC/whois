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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.compress.utils.Lists;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
            "2", "4fc80d09ec7e7448d94257c11bd44069aa00b6065ef88f7ad6b02ee78305cfa7",
            "3", "91e230c1c7db8078b61644150d1f68949342ef08d7975497c89ed75d58e940bc",
            "4", "ef533ff7eb558c64f08e2a4c2d13506429516890d550e7d111ac73a58b6e6577",
            "5", "0cebaa79b27d612945b28663ee00e427601a8e3c9210b195c290fcb63d14a4b4",
            "fake", "fake_hash"
    );

    private final static Map<String, String> RIPE_DELTA_HASH = ImmutableMap.of(
            "2", "e47c45a91c543d145fc5418777fe75b8bb12f6a63d41625f56a502af3968b237",
            "3", "57474b8eff0f07ad1cf08383b0b1674596a67a5e52a54e384d4ef21412a8823d",
            "4", "ef9770204fff4d469bf55c31eb427b58e639ad47bbea45864397f8bc93cd90ae",
            "5", "dbe744159a6433141efc9f139feecf56f665c128df89e4339bbe41289a046a16",
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
        server.insertHandler(new NrtmTestHandler());
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


    private class NrtmTestHandler extends Handler.Wrapper {

        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/xml;charset=utf-8");
            for (Mock mock : mocks) {
                if (mock.matches(request)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    switch (mock) {
                        //TODO: [MH] Combine this first three
                        case NrtmResponseMock nrtmResponseMock -> {
                            response.getHeaders().put(HttpHeader.CONTENT_TYPE, ((NrtmResponseMock)mock).mediaType);
                            response.write(true, mock.response(), null);
                        }
                        case NrtmDeltaResponseMock nrtmDeltaResponseMock -> {
                            response.getHeaders().put(HttpHeader.CONTENT_TYPE, nrtmDeltaResponseMock.mediaType);
                            response.write(true, mock.response(), null);
                        }
                        case NrtmSignedResponseMock nrtmSignedResponseMock -> {
                            response.getHeaders().put(HttpHeader.CONTENT_TYPE, nrtmSignedResponseMock.mediaType);
                            response.write(true, mock.response(), null);
                        }
                        case NrtmCompressedResponseMock nrtmCompressedResponseMock -> {
                            response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                            response.write(true, mock.response(), null);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + mock);
                    }
                }
            }
            return true;
        }
    }

    public void resetDefaultMocks(){
        mocks.clear();
        initialiseMocks();
    }

    public void setSecondDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("3", RIPE_SNAP_HASH, List.of("2", "3")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("3", RIPE_NONAUTH_SNAP_HASH, List.of("2", "3")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
    }

    public void setThreeAndFourVersionDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("4", RIPE_SNAP_HASH, List.of("2", "3", "4")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("4", RIPE_NONAUTH_SNAP_HASH, List.of("2", "3", "4")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.4.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.4.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.4.RIPE-NONAUTH.json", "application/json"));
    }

    public void setThreeAndFiveVersionDeltasMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("5", RIPE_SNAP_HASH, List.of("2", "3", "5")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("5", RIPE_NONAUTH_SNAP_HASH, List.of("2", "3", "5")), "application/jose+json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.5.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.5.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.5.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.5.RIPE-NONAUTH.json", "application/json"));
    }

    public void setAll(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("5", RIPE_SNAP_HASH, List.of("2", "3", "4", "5")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("5", RIPE_NONAUTH_SNAP_HASH, List.of("2", "3", "4", "5")), "application/jose+json"));

        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));

        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.3.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.3.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.3.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.3.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.4.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.4.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.4.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.5.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.5.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.5.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.5.RIPE-NONAUTH.json", "application/json"));
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
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE/update-notification-file.jose", getUpdateNotificationFileRIPE("2", RIPE_SNAP_HASH, List.of("2")), "application/jose+json"));
        mocks.add(new NrtmSignedResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", getUpdateNotificationFileRIPENonAuth("2", RIPE_NONAUTH_SNAP_HASH, List.of("2")), "application/jose+json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmDeltaResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
    }

    private interface Mock {

        String PATH = "mock/";

        boolean matches(final Request request);

        ByteBuffer response();

        static ByteBuffer getResource(final String resource) {
            try {
                // resource is in file
                return ByteBuffer.wrap(Resources.toByteArray(Resources.getResource(PATH + resource)));
            } catch (IllegalArgumentException e) {
                // resource doesn't exist (use resource as content)
                return  ByteBuffer.wrap(resource.getBytes());
            } catch (IOException e) {
                // error reading content from resource
                throw new IllegalStateException(e);
            }
        }

        static ByteBuffer getResourceAsRecords(final String resource) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Resources.getResource(PATH + resource).openStream(), Charset.defaultCharset()))) {
                return ByteBuffer.wrap(reader.lines()
                        .map(line -> RECORD_SEPARATOR + line)
                        .collect(Collectors.joining("\n"))
                        .getBytes());
            } catch (IllegalArgumentException e) {
                // resource doesn't exist (use resource as content)
                throw new IllegalStateException(e);
            } catch (IOException e) {
                // error reading content from resource
                throw new IllegalStateException(e);
            }
        }

        static ByteBuffer getCompressedResource(final String searchKey) {
            try {
                // resource is in file
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                    gzipOutputStream.write(getResourceAsRecords(searchKey).array());
                    gzipOutputStream.finish();
                    return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
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

    private record NrtmResponseMock(String fileType, ByteBuffer response, String mediaType) implements Mock {

        private NrtmResponseMock(final String fileType, final String response, final String mediaType) {
            this(fileType, Mock.getResource(response), mediaType);
        }

        @Override
        public boolean matches(final Request request) {
            return request.getHttpURI().toString().endsWith(fileType);
        }
    }

    private record NrtmSignedResponseMock(String fileType, ByteBuffer response, String mediaType) implements Mock {

        private NrtmSignedResponseMock(final String fileType, final String payload, final String mediaType) {
            this(fileType, Mock.getResourceAsRecords(payload), mediaType);
        }

        @Override
        public boolean matches(final Request request) {
            return request.getHttpURI().toString().endsWith(fileType);
        }
    }

    private record NrtmDeltaResponseMock(String fileType, ByteBuffer response, String mediaType) implements Mock {

        private NrtmDeltaResponseMock(final String fileType, final String payload, final String mediaType) {
            this(fileType, Mock.getResourceAsRecords(payload), mediaType);
        }

        @Override
        public boolean matches(final Request request) {
            return request.getHttpURI().toString().endsWith(fileType);
        }
    }

    private static class NrtmCompressedResponseMock implements Mock {
        private final String fileType;
        private final String searchKey;
        private final ByteBuffer response;

        public NrtmCompressedResponseMock(final String fileType, final String searchKey) {
            this.fileType = fileType;
            this.searchKey = searchKey;
            this.response = Mock.getCompressedResource(searchKey);
        }

        @Override
        public boolean matches(final Request request) {
            return request.getHttpURI().toString().endsWith(fileType);
        }

        @Override
        public ByteBuffer response() {
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

    private static ByteBuffer getUpdateNotificationFileRIPE(final String unfVersion, final String snapHast, final List<String> deltaVersion){
        final String deltas = deltaVersion.stream()
                .map(version -> String.format(DELTA_TEMPLATE, version, version, RIPE_DELTA_HASH.get(version)))
                .collect(Collectors.joining(",\n"));
        return ByteBuffer.wrap(String.format(UNF_RIPE_TEMPLATE, unfVersion, snapHast, deltas).getBytes());
    }

    private static ByteBuffer getUpdateNotificationFileRIPENonAuth(final String unfVersion, final String snapHast, final List<String> deltaVersion){
        final String deltas = deltaVersion.stream()
                .map(version -> String.format(DELTA_NON_AUTH_TEMPLATE, version, version, RIPE_NONAUTH_DELTA_HASH.get(version)))
                .collect(Collectors.joining(",\n"));
        return ByteBuffer.wrap(String.format(UNF_RIPE_NONAUTH_TEMPLATE, unfVersion, snapHast, deltas).getBytes());

    }
}
