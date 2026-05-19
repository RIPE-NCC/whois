package net.ripe.db.whois.api;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.oauth.OidcConfigurationProvider;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static net.ripe.db.whois.api.AbstractIntegrationTest.getRequestBody;

@Profile({WhoisProfile.TEST})
@Component
public class OAuthTokenIntrospectDummy implements Stub {

    private Server server;
    private int port = 0;
    private final OidcConfigurationProvider oidcConfigurationProvider;

    public OAuthTokenIntrospectDummy(final OidcConfigurationProvider oidcConfigurationProvider) {
        this.oidcConfigurationProvider = oidcConfigurationProvider;
    }

    private static class ApiPublicKeyLoaderTestHandler extends Handler.Abstract {

            @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {

           response.getHeaders().put(HttpHeader.CONTENT_TYPE, "text/xml;charset=utf-8");

            if (request.getHttpURI().getPath().contains("ripe-ncc/protocol/openid-connect/token/introspect")) {
                try {

                    final String body = getRequestBody(request);
                    final SignedJWT signedJWT = SignedJWT.parse(StringUtils.substringAfter(body, "token="));

                    final String email = signedJWT.getJWTClaimsSet().getStringClaim("email");
                    if (email.equals("invalid@ripenet")) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return false;
                    }

                    final boolean isActive = !email.equals("inactive@ripe.net");

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                    response.write(
                            true,
                            ByteBuffer.wrap(JSONObjectUtils.parse(signedJWT.getPayload().toString()).appendField("active", isActive).toString().getBytes()),
                            callback
                    );

                    callback.succeeded();

                    return true;

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

            if (request.getHttpURI().getPath().contains("realms/ripe-ncc/protocol/openid-connect/certs")) {

                try (InputStream is = getClass().getResourceAsStream("/JWT_public.key")) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.write(
                            true,
                            ByteBuffer.wrap(is.readAllBytes()),
                            callback
                    );
                }

                response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                callback.succeeded();

                return true;
            }

            if (request.getHttpURI().getPath().contains("ripe-ncc/.well-known/openid-configuration")) {
                final int port = request.getHttpURI().getPort();
                String body = """
                {
                  "issuer": "http://localhost:%d/realms/ripe-ncc",
                  "jwks_uri": "http://localhost:%d/realms/ripe-ncc/protocol/openid-connect/certs",
                  "introspection_endpoint": "http://localhost:%d/realms/ripe-ncc/protocol/openid-connect/token/introspect",
                  "subject_types_supported": [
                    "public"
                  ],
                  "id_token_signing_alg_values_supported": [
                    "RS256"
                  ]
                }
                """.formatted(port, port, port);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getHeaders().put("Content-Type", "application/json");
                response.write(
                        true,
                        ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)),
                        callback
                );

                response.getHeaders().put(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                return true;
            }

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
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

        final URI openIdMetadataUrl = new URIBuilder()
                .setScheme("http")
                .setHost("localhost")
                .setPort(port)
                .setPath("realms/ripe-ncc")
                .build();

        ReflectionTestUtils.setField(this.oidcConfigurationProvider, "openIdMetadataUrl", openIdMetadataUrl.toString());
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
