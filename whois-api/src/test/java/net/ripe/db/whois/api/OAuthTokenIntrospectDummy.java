package net.ripe.db.whois.api;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.api.oauth.BearerTokenExtractor;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.http.client.utils.URIBuilder;
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
import java.net.URI;
import java.net.URISyntaxException;

@Profile({WhoisProfile.TEST})
@Component
public class OAuthTokenIntrospectDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenIntrospectDummy.class);

    private Server server;
    private int port = 0;

    private final BearerTokenExtractor bearerTokenExtractor;

    @Autowired
    public OAuthTokenIntrospectDummy(final BearerTokenExtractor bearerTokenExtractor) {
        this.bearerTokenExtractor = bearerTokenExtractor;
    }

    private static class ApiPublicKeyLoaderTestHandler extends AbstractHandler {

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);

            if(!request.getRequestURI().contains("ripe-ncc/protocol/openid-connect/token/introspect")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            try {
                 final SignedJWT signedJWT = SignedJWT.parse(request.getParameter("token"));

                 final String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
                 if(email.equals("invalid@ripenet")) {
                     response.sendError(HttpServletResponse.SC_NOT_FOUND);
                     return;
                 }

                 final boolean isActive = !email.equals("inactive@ripe.net");

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(MediaType.APPLICATION_JSON);
                response.getWriter().println(JSONObjectUtils.parse(signedJWT.getPayload().toString()).appendField("active", isActive));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() throws URISyntaxException {
        server = new Server(0);
        server.setHandler(new ApiPublicKeyLoaderTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final URI restUrl = new URIBuilder()
                            .setScheme("http")
                            .setHost("localhost")
                            .setPort(port)
                            .setPath("realms/ripe-ncc/protocol/openid-connect/token/introspect")
                            .build();

        LOGGER.info("Validate Token using  dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(bearerTokenExtractor, "tokenIntrospectEndpoint", restUrl);
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
