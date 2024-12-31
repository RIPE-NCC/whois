package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    private Server server;
    private int port = 0;

    private final ApiKeyAuthServiceClient apiKeyAuthServiceClient;

    @Autowired
    public ApiKeyAuthServerDummy(ApiKeyAuthServiceClient apiKeyAuthServiceClient) {
        this.apiKeyAuthServiceClient = apiKeyAuthServiceClient;
    }

    private class OAuthTestHandler extends AbstractHandler {
        final Map<String, OAuthSession> usermap;

        {
            usermap = Maps.newHashMap();
            usermap.put(BASIC_AUTH_TEST_NO_MNT, new OAuthSession(AUD, "hHZjAbXPtxGxUJCgdwv2ufhY", "test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", null));
            usermap.put(BASIC_AUTH_PERSON_NO_MNT, new OAuthSession(AUD, "l6lRZgvOFIphjiGwtCGuLwqw","person@net.net", "906635c2-0405-429a-800b-0602bd716124", null));
            usermap.put(BASIC_AUTH_PERSON_OWNER_MNT, new OAuthSession(AUD, "l6lRZgvOFIphjiGwtCGuLwqw","person@net.net", "906635c2-0405-429a-800b-0602bd716124", "whois.mntner:OWNER-MNT"));
            usermap.put(BASIC_AUTH_TEST_TEST_MNT, new OAuthSession(AUD, "hHZjAbXPtxGxUJCgdwv2ufhY","test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "whois.mntner:TEST-MNT"));
        }

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

            final OAuthSession oAuthSession = usermap.get(userKey);
            if (oAuthSession == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().println(convertToJwt(oAuthSession));
        }

        private String convertToJwt(OAuthSession oAuthSession) {
           return StringUtils.joinWith(".",
            "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJuMzdXWllsaGYzLTUwVkVTVzl0YlowWFFCVXMxVlZZdzc5aEpkaFI4Q1ZJIn0",
                   Base64.getEncoder().encodeToString(ApiKeyUtils.getOAuthSession(oAuthSession).getBytes(StandardCharsets.UTF_8)),
                   "kKAyngAbKSeHzjxcNM0BuS4CRttdZYAk2GKXiI5IcqOXAlx66E_933Es_XBofeSkUiWzQM4v0mcdJr171bKbuFfpArjFnEWPXlcO9GAUTAJoO0spaxxP2KeWx3-cPdkaccJOnkyvTyb9ZzsNQX0qreLkeqiINc-FWU1qoNtZW7TolyQ_qstMNVTFcd2GO5Z0qEXySfnlRSjssnb78VhnDcijHGf_atTBhRkzLAg23-Z8HoeEfNkpJEFDMMhuGzRupE8_MA45ak-Cr7LMiFe4a_MNRGiet1OsN4rmIyTJwByx9akADyzLbPUHcbWE3gBJmsDR5mCajvXrnZBJYcGBiQ");
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
