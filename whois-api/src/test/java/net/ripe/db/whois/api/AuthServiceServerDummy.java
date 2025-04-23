package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.UserSession;
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
import java.util.Map;

@Profile({WhoisProfile.TEST})
@Component
public class AuthServiceServerDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceServerDummy.class);

    private Server server;
    private int port = 0;

    private final AuthServiceClient authServiceClient;

    @Autowired
    public AuthServiceServerDummy(AuthServiceClient crowdClient) {
        this.authServiceClient = crowdClient;
    }

    private class SSOTestHandler extends AbstractHandler {
        final Map<String, SSOUser> usermap;

        {
            usermap = Maps.newHashMap();
            usermap.put("db-test@ripe.net", new SSOUser("db-test@ripe.net","Db","User","ed7cd420-6402-11e3-949a-0800200c9a66", true));
            usermap.put("random@ripe.net", new SSOUser("random@ripe.net", "Random","User", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", true));
            usermap.put("test@ripe.net", new SSOUser("test@ripe.net", "Ripe","User", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", true));
            usermap.put("person@net.net", new SSOUser("person@net.net", "Test","User", "906635c2-0405-429a-800b-0602bd716124", true));

            usermap.put("ed7cd420-6402-11e3-949a-0800200c9a66", new SSOUser("db-test@ripe.net","Db","User","ed7cd420-6402-11e3-949a-0800200c9a66", true));
            usermap.put("017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", new SSOUser("random@ripe.net", "Random","User", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", true));
            usermap.put("8ffe29be-89ef-41c8-ba7f-0e1553a623e5", new SSOUser("test@ripe.net", "Ripe","User", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", true));
            usermap.put("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia", new SSOUser("test@ripe.net", "Ripe","User", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5", true));
            usermap.put("906635c2-0405-429a-800b-0602bd716124", new SSOUser("person@net.net", "Test","User", "906635c2-0405-429a-800b-0602bd716124", true));

            // for e2e integration test
            usermap.put("aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", new SSOUser("db_e2e_1@ripe.net", "DB","E2E_1", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", true));
            usermap.put("db_e2e_1@ripe.net", new SSOUser("db_e2e_1@ripe.net", "DB","E2E_1", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", true));
            usermap.put("e74ccc29-75f9-4ce1-aee0-690345a56c96", new SSOUser("db_e2e_2@ripe.net", "DB","E2E_2", "e74ccc29-75f9-4ce1-aee0-690345a56c96", true));
            usermap.put("db_e2e_2@ripe.net", new SSOUser("db_e2e_2@ripe.net", "DB","E2E_2", "e74ccc29-75f9-4ce1-aee0-690345a56c96", true));

            // for e2e integration test
            usermap.put("valid-token", new SSOUser("person@net.net", "Test","User", "906635c2-0405-429a-800b-0602bd716124", true));
            usermap.put("invalid-token", null);
            usermap.put("db_e2e_1", new SSOUser("db_e2e_1@ripe.net", "DB","E2E_1", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", true));
            usermap.put("db_e2e_2", new SSOUser("db_e2e_2@ripe.net", "DB","E2E_2", "e74ccc29-75f9-4ce1-aee0-690345a56c96", true));

        }

        final Map<String, UserSession> userSessionMap;
        {
            userSessionMap = Maps.newHashMap();
            userSessionMap.put("valid-token", new UserSession("aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef","person@net.net", "Test User", true, "2033-01-30T16:38:27.369+11:00"));
            userSessionMap.put("invalid-token", null);
            // for e2e integration test
            userSessionMap.put("db_e2e_1", new UserSession("e74ccc29-75f9-4ce1-aee0-690345a56c96","db_e2e_1@ripe.net", "DB E2E_1", true, "2033-01-30T16:38:27.369+11:00"));
            userSessionMap.put("db_e2e_2", new UserSession("e74ccc29-75f9-4ce1-aee0-690345a56c96","db_e2e_2@ripe.net", "DB E2E_2", true, "2033-01-30T16:38:27.369+11:00"));
        }

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);

            if(!request.getRequestURI().contains("/authorisation-service/v2/authresource/")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            final String userKey = request.getRequestURI().contains("validate") ? StringUtils.substringAfter(request.getHeader("Authorization"), "Bearer").trim() :
                                                        StringUtils.substringAfterLast(request.getRequestURI(), "/");

            final SSOUser user = usermap.get(userKey);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().println(request.getRequestURI().contains("history") ? serializeHistoricalDetails(user)
                    : serializeUuid(user));
        }

        private String serializeUuid(final SSOUser user) {
            return String.format("{\n" +
                    "  \"response\": {\n" +
                    "    \"status\": 200,\n" +
                    "    \"message\": \"OK\",\n" +
                    "    \"totalSize\": 1,\n" +
                    "    \"links\": [\n" +
                    "\n" +
                    "    ],\n" +
                    "    \"content\": {\n" +
                    "      \"firstName\": \"%s\",\n" +
                    "      \"lastName\": \"%s\",\n" +
                    "      \"email\": \"%s\",\n" +
                    "      \"id\": \"%s\",\n" +
                    "      \"active\": %s,\n" +
                    "      \"accessRoles\": [\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }\n" +
                    "}", user.getFirstName(), user.getLastName(), user.getEmail(), user.getUuid(), user.isActive());
        }

        private String serializeHistoricalDetails(final SSOUser user){
            return String.format("{\n" +
                    "  \"response\": {\n" +
                    "    \"results\": [\n " +
                    "    {\n" +
                    "      \"eventDateTime\": \"2015-05-08T12:32:01.275379Z\",\n" +
                    "      \"action\": \"EMAIL_CHANGE\",\n" +
                    "      \"uuid\": \"%s\",\n" +
                    "      \"actor\": \"%s\",\n" +
                    "      \"actingService\": \"crowd_email_migration\",\n" +
                    "      \"staff\": false,\n" +
                    "      \"attributeChanges\": [\n" +
                    "      {\n" +
                    "         \"name\": \"email\",\n" +
                    "         \"oldValue\": \"%s\",\n" +
                    "         \"newValue\": \"%s\"\n" +
                    "      }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}",  user.getUuid(), user.getEmail(), user.getEmail(), user.getEmail());
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new SSOTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String restUrl = String.format("http://localhost:%s/authorisation-service/v2/authresource", getPort());
        LOGGER.info("SSO dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(authServiceClient, "restUrl", restUrl);
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

    static class SSOUser {
        private String email;
        private String firstName;
        private String lastName;
        private String uuid;
        private boolean isActive;


        public SSOUser() {
            // required no-arg constructor
        }

        public SSOUser(final String email, final String firstName, final String lastName, final String uuid, final boolean isActive) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.uuid = uuid;
            this.isActive = isActive;
        }

        public boolean isActive() {
            return isActive;
        }

        public String getUuid() {
            return uuid;
        }

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

}
