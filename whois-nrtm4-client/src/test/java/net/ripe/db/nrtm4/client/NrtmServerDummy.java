package net.ripe.db.nrtm4.client;

import com.google.common.io.Resources;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Profile({WhoisProfile.TEST})
@Component
public class NrtmServerDummy implements Stub {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServerDummy.class);

    private Server server;
    private int port = 0;

    private final NrtmRestClient nrtmRestClient;

    private final List<Mock> mocks;

    private static final String FIRST_RIPE_SNAPSHOT_HASH = "b293e92997d3be7a5156fdca832af378c3989b2cefa9e3e37caaeeba0ca971e9";

    private static final String FIRST_NON_AUTH_SNAPSHOT_HASH = "148c3c411b8f044f5fc0ab201f6dd03e80c862e27ad1a63488aee337dc7eb4a2";

    private static final String FIRST_RIPE_DELTA_HASH = "56a17bc2a45167ff0288f84ffd01f74d64dfb7eed40c021bda03087e03708648";

    private static final String FIRST_NON_AUTH_DELTA_HASH = "bc5c8af6276593cfe930a150a839ac44ad0c8b773f42758e15d6e975fe16d91f";

    private static final String SECOND_RIPE_DELTA_HASH = "cfa467aa74261a38d185d12c5548fd407faff3ddd6bdaacaf5ade029275b3aaf";

    private static final String SECOND_NON_AUTH_DELTA_HASH = "b9c43e8381b6342d5fba0fa11a25182d56cfb27df1495b3b3270c1557dc074b1";

    final String unfRipeTemplate = """
                {
                  "nrtm_version": 4,
                  "timestamp": "2024-10-25T00:07:00Z",
                  "type": "notification",
                  "source": "RIPE",
                  "session_id": "4521174b-548f-4e51-98fc-dfd720011a0c",
                  "version": %s,
                  "snapshot": {
                    "version": 1,
                    "url": "http://localhost:%s/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": %s,
                      "url": "http://localhost:%s/nrtmv4/RIPE/nrtm-delta.%s.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json",
                      "hash": "%s"
                    }
                  ]
                }
                """;

    final String unfRipeNonAuthTemplate = """
                {
                  "nrtm_version": 4,
                  "timestamp": "2024-10-24T13:20:00Z",
                  "type": "notification",
                  "source": "RIPE-NONAUTH",
                  "session_id": "6328095e-7d46-415b-9333-8f2ae274b7c8",
                  "version": %s,
                  "snapshot": {
                    "version": 1,
                    "url": "http://localhost:%s/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": %s,
                      "url": "http://localhost:%s/nrtmv4/RIPE-NONAUTH/nrtm-delta.%s.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json",
                      "hash": "%s"
                    }
                  ]
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
                    if (mock instanceof NrtmResponseMock) {
                        response.setContentType(((NrtmResponseMock)mock).mediaType);
                        response.getWriter().println(mock.response());
                    } else {
                        response.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        response.getOutputStream().write(((NrtmCompressedResponseMock)mock).response.toByteArray());
                    }
                }
            }
        }
    }

    public void resetDefaultMocks(){
        mocks.clear();
        initialiseMocks();
    }

    public void setFakeHashMocks(){
        mocks.clear();
        initialMocks();
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json",
                getUpdateNotificationFileNonAuthResponse("fake_hash", FIRST_NON_AUTH_DELTA_HASH, "1"), "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json",
                getUpdateNotificationFileRipeResponse("fake_hash", FIRST_RIPE_DELTA_HASH, "1"), "application/json"));
    }

    public void setSecondUNFMocks() {
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json",
                getUpdateNotificationFileNonAuthResponse(FIRST_NON_AUTH_SNAPSHOT_HASH, SECOND_NON_AUTH_DELTA_HASH,"2"), "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json",
                getUpdateNotificationFileRipeResponse(FIRST_RIPE_SNAPSHOT_HASH, SECOND_RIPE_DELTA_HASH,"2"), "application/json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/nrtm-delta.2.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.2.RIPE.json", "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.2.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.2.RIPE-NONAUTH.json", "application/json"));
    }

    private void initialiseMocks() {
        initialMocks();
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json",
                getUpdateNotificationFileNonAuthResponse(FIRST_NON_AUTH_SNAPSHOT_HASH, FIRST_NON_AUTH_DELTA_HASH, "1"), "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json",
                getUpdateNotificationFileRipeResponse(FIRST_RIPE_SNAPSHOT_HASH, FIRST_RIPE_DELTA_HASH, "1"), "application/json"));
    }

    private void initialMocks(){
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-delta.1.RIPE-NONAUTH.4f3ff2a7-1877-4cab-82f4-1dd6425c4e7d.94b5a6cc54f258062c25d9bee224b5c.json", "nrtm-delta.1.RIPE-NONAUTH.json", "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/nrtm-delta.1.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json", "nrtm-delta.1.RIPE.json", "application/json"));
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

        default ByteArrayOutputStream getCompressedResource(final String searchKey) {
            try {
                // resource is in file
                String jsonData = Resources.toString(Resources.getResource(PATH + searchKey), Charset.defaultCharset());
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

    private String getUpdateNotificationFileRipeResponse(final String snapshotHash, final String deltaHash, final String version){
        return String.format(unfRipeTemplate, version, port, snapshotHash, version, port, version, deltaHash);
    }

    private String getUpdateNotificationFileNonAuthResponse(final String snapshotHash, final String deltaHash, final String version){
        return String.format(unfRipeNonAuthTemplate, version, port, snapshotHash, version, port, version, deltaHash);
    }
}
