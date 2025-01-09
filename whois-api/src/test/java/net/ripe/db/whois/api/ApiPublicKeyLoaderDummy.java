package net.ripe.db.whois.api;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.api.apiKey.ApiPublicKeyLoader;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
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
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;

@Profile({WhoisProfile.TEST})
@Component
public class ApiPublicKeyLoaderDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPublicKeyLoaderDummy.class);

    private Server server;
    private int port = 0;

    private final ApiPublicKeyLoader apiPublicKeyLoader;

    @Autowired
    public ApiPublicKeyLoaderDummy(final ApiPublicKeyLoader apiPublicKeyLoader) {
        this.apiPublicKeyLoader = apiPublicKeyLoader;
    }

    private class ApiPublicKeyLoaderTestHandler extends AbstractHandler {

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);

            if(!request.getRequestURI().contains("realms/ripe-ncc/protocol/openid-connect/certs")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().println(new String(Files.readAllBytes(ResourceUtils.getFile("classpath:JWT_public.key").toPath())));
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new ApiPublicKeyLoaderTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String restUrl = String.format("http://localhost:%s/realms/ripe-ncc/protocol/openid-connect/certs", getPort());
        LOGGER.info("Load API key  dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(apiPublicKeyLoader, "restUrl", restUrl);
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
}
