package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.UserSession;
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Profile({WhoisProfile.TEST})
@Component
public class CrowdServerDummy implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdServerDummy.class);

    private Server server;
    private int port = 0;

    private final CrowdClient crowdClient;

    @Autowired
    public CrowdServerDummy(CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }

    private class CrowdTestHandler extends AbstractHandler {
        final Map<String, CrowdUser> usermap;

        {
            usermap = Maps.newHashMap();
            usermap.put("db-test@ripe.net", new CrowdUser("db-test@ripe.net","Db User","ed7cd420-6402-11e3-949a-0800200c9a66"));
            usermap.put("random@ripe.net", new CrowdUser("random@ripe.net", "Random User", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503"));
            usermap.put("test@ripe.net", new CrowdUser("test@ripe.net", "Ripe  User", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5"));
            usermap.put("person@net.net", new CrowdUser("person@net.net", "Test User", "906635c2-0405-429a-800b-0602bd716124"));

            usermap.put("ed7cd420-6402-11e3-949a-0800200c9a66", new CrowdUser("db-test@ripe.net","Db User","ed7cd420-6402-11e3-949a-0800200c9a66"));
            usermap.put("017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", new CrowdUser("random@ripe.net", "Random User", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503"));
            usermap.put("8ffe29be-89ef-41c8-ba7f-0e1553a623e5", new CrowdUser("test@ripe.net", "Ripe  User", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5"));
            usermap.put("906635c2-0405-429a-800b-0602bd716124", new CrowdUser("person@net.net", "Test User", "906635c2-0405-429a-800b-0602bd716124"));

            // for e2e integration test
            usermap.put("aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef", new CrowdUser("db_e2e_1@ripe.net", "DB E2E_1", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef"));
            usermap.put("db_e2e_1@ripe.net", new CrowdUser("db_e2e_1@ripe.net", "DB E2E_1", "aff2b59f-7bd0-413b-a16f-5bc1c5c3c3ef"));
            usermap.put("e74ccc29-75f9-4ce1-aee0-690345a56c96", new CrowdUser("db_e2e_2@ripe.net", "DB E2E_2", "e74ccc29-75f9-4ce1-aee0-690345a56c96"));
            usermap.put("db_e2e_2@ripe.net", new CrowdUser("db_e2e_2@ripe.net", "DB E2E_2", "e74ccc29-75f9-4ce1-aee0-690345a56c96"));
        }

        final Map<String, UserSession> crowdSessionMap;
        {
            crowdSessionMap = Maps.newHashMap();
            crowdSessionMap.put("valid-token", new UserSession("person@net.net", "Test User", true, "2033-01-30T16:38:27.369+11:00"));
            crowdSessionMap.put("invalid-token", null);
            // for e2e integration test
            crowdSessionMap.put("db_e2e_1", new UserSession("db_e2e_1@ripe.net", "DB E2E_1", true, "2033-01-30T16:38:27.369+11:00"));
            crowdSessionMap.put("db_e2e_2", new UserSession("db_e2e_2@ripe.net", "DB E2E_2", true, "2033-01-30T16:38:27.369+11:00"));
        }

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);
            final Map<String, String[]> parameterMap = request.getParameterMap();

            if (parameterMap.get("username") != null) {
                final String uuid = usermap.get(parameterMap.get("username")[0]).getUuid();
                if (uuid == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(serializeUuid(uuid));
                }
            }
            else if (parameterMap.get("restriction") != null) {
                final String username = usermap.get(parameterMap.get("restriction")[0].split("=")[1]).getName();
                final String displayName = usermap.get(parameterMap.get("restriction")[0].split("=")[1]).getDisplayName();
                if (username == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(serializeUserName(username, displayName));
                }
            }
            else if (request.getRequestURI().contains("session")) {
                Splitter SPACE_SPLITTER = Splitter.on('/');
                String ssoToken= Iterables.getLast(SPACE_SPLITTER.split(request.getRequestURI()));
                final UserSession userSession = crowdSessionMap.get(ssoToken);
                if (userSession == null){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().print(serializeUserSessionResponse(userSession));
                }
            }
            else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private String serializeUserSessionResponse(final UserSession userSession){
            return String.format(
                    "<session expand=\"user\">" +
                            "<user name=\"%s\">" +
                            "<active>%s</active>" +
                            "</user>" +
                            "<expiry-date>2033-01-30T16:38:27.369+11:00</expiry-date>" +
                            "</session>", userSession.getUsername(), userSession.isActive());
        }

        private String serializeUuid(final String username) {
            return String.format("<attributes><attribute name=\"uuid\"><values><value>%s</value></values></attribute></attributes>", username);
        }

        private String serializeUserName(final String uuid, final String displayName) {
            return String.format("" +
                    "<users expand=\"user\">\n" +
                    "   <user name=\"%s\">\n" +
                    "   <display-name>" + displayName + "</display-name>" +
                    "   </user>\n" +
                    "</users>", uuid);
        }

    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new CrowdTestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String restUrl = String.format("http://localhost:%s/crowd", getPort());
        LOGGER.info("Crowd dummy server restUrl: {}", restUrl);
        ReflectionTestUtils.setField(crowdClient, "restUrl", restUrl);
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

    static class CrowdUser {
        private String name;
        private String displayName;
        private String uuid;

        public CrowdUser() {
            // required no-arg constructor
        }

        public CrowdUser(final String name, final String displayName, final String uuid) {
            this.name = name;
            this.displayName = displayName;
            this.uuid = uuid;
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

}
