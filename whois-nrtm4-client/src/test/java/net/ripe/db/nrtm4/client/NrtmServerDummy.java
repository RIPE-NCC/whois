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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    final String unfRipeTemplate = """
                {
                  "nrtm_version": 4,
                  "timestamp": "2024-10-25T00:07:00Z",
                  "type": "notification",
                  "source": "RIPE",
                  "session_id": "4521174b-548f-4e51-98fc-dfd720011a0c",
                  "version": 1,
                  "snapshot": {
                    "version": 1,
                    "url": "http://localhost:%s/nrtmv4/RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": 1,
                      "url": "http://localhost:%s/nrtmv4/RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json",
                      "hash": "c50dd7554cb35ef5f2f45d7bfa09fc51033cbe1152d29b36cb1178319e22be3e"
                    }
                  ]
                }
                """;

    final String unfRipeNonAuthTemplate = """
                {
                  "nrtm_version": 1,
                  "timestamp": "2024-10-24T13:20:00Z",
                  "type": "notification",
                  "source": "RIPE-NONAUTH",
                  "session_id": "6328095e-7d46-415b-9333-8f2ae274b7c8",
                  "version": 1,
                  "snapshot": {
                    "version": 1,
                    "url": "http://localhost:%s/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz",
                    "hash": "%s"
                  },
                  "deltas": []
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
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json", getFakeUpdateNotificationNonAuthResponse(), "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json", getFakeUpdateNotificationRipeResponse(), "application/json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
    }

    private void initialiseMocks() {
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json", getUpdateNotificationFileNonAuthResponse(), "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json", getUpdateNotificationFileRipeResponse(), "application/json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
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

    private String getFakeUpdateNotificationRipeResponse(){
        return String.format(unfRipeTemplate, port, "fake_hash", port);
    }

    private String getFakeUpdateNotificationNonAuthResponse(){
        return String.format(unfRipeNonAuthTemplate, port, "fake_hash");
    }

    private String getUpdateNotificationFileRipeResponse(){
        return String.format(unfRipeTemplate, port, "b8fe5f2ae046e37a34c0228c237e824ac4c53d973beb81495b0e9526b4607c19", port);
    }

    private String getUpdateNotificationFileNonAuthResponse(){
        return String.format(unfRipeNonAuthTemplate, port, "148c3c411b8f044f5fc0ab201f6dd03e80c862e27ad1a63488aee337dc7eb4a2");
    }
}
