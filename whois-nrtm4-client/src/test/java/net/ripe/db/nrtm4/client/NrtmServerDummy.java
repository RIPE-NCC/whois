package net.ripe.db.nrtm4.client;

import com.google.common.io.Resources;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.compress.utils.Lists;
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
                    "url": "RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz",
                    "hash": "%s"
                  },
                  "deltas": [
                    {
                      "version": 1,
                      "url": "RIPE/nrtm-delta.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.e3be41ff312010046b67d099faa58f44.json",
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
                    "url": "RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz",
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
            response.getHeaders().put("Content-Type", "text/xml;charset=utf-8");
            for (Mock mock : mocks) {
                if (mock.matches(request)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    if (mock instanceof NrtmResponseMock) {
                        response.getHeaders().put("Content-Type", ((NrtmResponseMock)mock).mediaType);
                        response.write(true, mock.response(), null);
                    } else {
                        response.getHeaders().put("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
                        response.write(true, mock.response(), null);
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

    public void setFakeHashMocks(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.jose", "fake-nrtm-RIPE-signature.jose", "application/jose+json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", "fake-nrtm-RIPE-NONAUTH-signature.jose", "application/jose+json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
    }

    public void setWrongSignedUNF(){
        mocks.clear();
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.jose", "nrtm-RIPE-wrong-signature.jose", "application/jose+json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", "nrtm-RIPE-NONAUTH-wrong-signature.jose", "application/jose+json"));
    }

    private void initialiseMocks() {
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.jose", "nrtm-RIPE-signature.jose", "application/jose+json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.jose", "nrtm-RIPE-NONAUTH-signature.jose", "application/jose+json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE-NONAUTH/nrtm-snapshot.1.RIPE-NONAUTH.6328095e-7d46-415b-9333-8f2ae274b7c8.f1195bb8a666fe7b97fa74009a70cefa.json.gz", "nrtm-snapshot.1.RIPE-NONAUTH.json"));
        mocks.add(new NrtmCompressedResponseMock("/nrtmv4/RIPE/nrtm-snapshot.4.RIPE.4521174b-548f-4e51-98fc-dfd720011a0c.82542bd048e111fe57db404d08b6433e.json.gz", "nrtm-snapshot.1.RIPE.json"));
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
        public boolean matches(Request request) {
            return request.getHttpURI().toString().endsWith(fileType);
        }

        @Override
        public ByteBuffer response() {
            return response;
        }
    }
}
