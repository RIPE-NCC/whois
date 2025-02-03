package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.apiKey.ApiKeyAuthServiceClient;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;

@Profile({WhoisProfile.TEST})
@Component
public class ApiKeyAuthServerDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthServerDummy.class);
    public static final String[] AUD = {"account", "whois"};
    public static final String BASIC_AUTH_TEST_NO_MNT = "eFR0cm9lZUpWYWlmSWNQR1BZUW5kSmhnOmp5akhYR2g4WDFXRWZyc2M5SVJZcUVYbw==";
    public static final String BASIC_AUTH_PERSON_NO_MNT = "bDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_PERSON_OWNER_MNT = "cDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_TEST_TEST_MNT = "dDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_INVALID_API_KEY = "aDZsUlpndk9GSXBoamlHd3RDR3VMd3F3OjJDVEdQeDVhbFVFVzRwa1Rrd2FRdGRPNg==";
    public static final String BASIC_AUTH_INVALID_SIGNATURE_API_KEY = "TXp1ZzRxRVlpSTVET1dqOXI1Qkp1Y2k4OnZBdzgyRTFCMkZ2dFVyYjB0MDF0Ykt2cg==";

    public static final Map<String, OAuthSession> APIKEY_TO_OAUTHSESSION =  Maps.newHashMap();

    {
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_TEST_NO_MNT, new OAuthSession(AUD, "hHZjAbXPtxGxUJCgdwv2ufhY", "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "profile email"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_NO_MNT, new OAuthSession(AUD, "l6lRZgvOFIphjiGwtCGuLwqw","person@net.net", "906635c2-0405-429a-800b-0602bd716124", null));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_PERSON_OWNER_MNT, new OAuthSession(AUD, "l6lRZgvOFIphjiGwtCGuLwqw","person@net.net", "906635c2-0405-429a-800b-0602bd716124", "profile email whois.mntner:OWNER-MNT"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_TEST_TEST_MNT, new OAuthSession(AUD, "hHZjAbXPtxGxUJCgdwv2ufhY","test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "whois.mntner:TEST-MNT profile email"));
        APIKEY_TO_OAUTHSESSION.put(BASIC_AUTH_INVALID_SIGNATURE_API_KEY, new OAuthSession(AUD, "hHZjAbXPtxGxUJCgdwv2ufhY","invalid@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "profile email whois.mntner:TEST-MNT"));
    }

    private Server server;
    private int port = 0;

    private final ApiKeyAuthServiceClient apiKeyAuthServiceClient;

    @Autowired
    public ApiKeyAuthServerDummy(ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        this.apiKeyAuthServiceClient = apiKeyAuthServiceClient;
    }

    private class OAuthTestHandler extends AbstractHandler {

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);

            if(!request.getRequestURI().contains("/api/v1/api-keys/authenticate")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            final String userKey = StringUtils.substringAfter(request.getHeader("Authorization"), "Basic").trim();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().println(convertToJwt(userKey));
        }

        private String convertToJwt(final String userKey) {

            final OAuthSession oAuthSession = APIKEY_TO_OAUTHSESSION.get(userKey);
            if (oAuthSession == null) {
                return null;
            }

            try {
                final RSAKey privateKey = userKey.equals(BASIC_AUTH_INVALID_SIGNATURE_API_KEY) ? new RSAKeyGenerator(2048)
                        .keyID("123")
                        .generate() :
                        RSAKey.parse(new String(Files.readAllBytes(ResourceUtils.getFile("classpath:JWT_private.key").toPath())));
                final JWSSigner signer = new RSASSASigner(privateKey);

                JWSObject jwsObject = new JWSObject(
                        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.getKeyID()).build(),
                        new Payload(ApiKeyUtils.getOAuthSession(oAuthSession)));

                jwsObject.sign(signer);

                return jwsObject.serialize();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new OAuthTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String restUrl = String.format("http://localhost:%s/api/v1/api-keys/authenticate", getPort());
        LOGGER.info("OAuth dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(apiKeyAuthServiceClient, "restUrl", restUrl);
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
