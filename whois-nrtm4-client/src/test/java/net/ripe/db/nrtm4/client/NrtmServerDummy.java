package net.ripe.db.nrtm4.client;

import com.google.common.io.Resources;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Profile({WhoisProfile.TEST})
@Component
public class NrtmServerDummy implements Stub {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmServerDummy.class);

    private Server server;
    private int port = 0;

    private final NrtmRestClient nrtmRestClient;

    private final List<Mock> mocks;

    @Autowired
    public NrtmServerDummy(final NrtmRestClient nrtmRestClient) {
        this.nrtmRestClient = nrtmRestClient;
        this.mocks = Lists.newArrayList();
        initialiseMocks();
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
                    response.setContentType(((NrtmResponseMock)mock).mediaType);
                    response.getWriter().println(mock.response());
                }
            }
        }
    }

    private void initialiseMocks() {
        mocks.add(new NrtmResponseMock("/nrtmv4", "nrtm-sources.html", "application/html"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE-NONAUTH/update-notification-file.json", "nrtm-non-auth-unf.json", "application/json"));
        mocks.add(new NrtmResponseMock("/nrtmv4/RIPE/update-notification-file.json", "nrtm-ripe-unf.json", "application/json"));
    }


    private interface Mock {

        String PATH = "mock/";
        boolean matches(final HttpServletRequest request);

        String response();

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
}
