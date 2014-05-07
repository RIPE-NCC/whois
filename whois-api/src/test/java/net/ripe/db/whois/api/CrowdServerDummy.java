package net.ripe.db.whois.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.sso.CrowdClient;
import net.ripe.db.whois.common.sso.UserSession;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        final Map<String, String> usermap;
        {
            usermap = Maps.newHashMap();
            usermap.put("db-test@ripe.net", "ed7cd420-6402-11e3-949a-0800200c9a66");
            usermap.put("random@ripe.net", "017f750e-6eb8-4ab1-b5ec-8ad64ce9a503");
            usermap.put("test@ripe.net", "8ffe29be-89ef-41c8-ba7f-0e1553a623e5");
            usermap.put("person@net.net", "906635c2-0405-429a-800b-0602bd716124");

            usermap.put("ed7cd420-6402-11e3-949a-0800200c9a66", "db-test@ripe.net");
            usermap.put("017f750e-6eb8-4ab1-b5ec-8ad64ce9a503", "random@ripe.net");
            usermap.put("8ffe29be-89ef-41c8-ba7f-0e1553a623e5", "test@ripe.net");
            usermap.put("906635c2-0405-429a-800b-0602bd716124", "person@net.net");
        }

        final Map<String, UserSession> crowdSessionMap;
        {
            crowdSessionMap = Maps.newHashMap();
            crowdSessionMap.put("valid-token", new UserSession("person@net.net", true, "2033-01-30T16:38:27.369+11:00"));
            crowdSessionMap.put("invalid-token", null);
        }

        @Override
        public void handle(final String target, final Request baseRequest,
                           final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/xml;charset=utf-8");
            baseRequest.setHandled(true);
            final Map<String, String[]> parameterMap = request.getParameterMap();

            if (parameterMap.get("username") != null) {
                final String uuid = usermap.get(parameterMap.get("username")[0]);
                if (uuid == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(getUuid(uuid));
                }
            }
            else if (parameterMap.get("uuid") != null) {
                final String username = usermap.get(parameterMap.get("uuid")[0]);
                if (username == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(getUsername(username));
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
                    response.getWriter().print(getUserSessionResponse(userSession));
                }
            }
            else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private String getUserSessionResponse(final UserSession userSession){
            return String.format(
                    "<session expand=\"user\">" +
                        "<user name=\"%s\">" +
                            "<active>%s</active>" +
                        "</user>" +
                        "<expiry-date>2033-01-30T16:38:27.369+11:00</expiry-date>" +
                    "</session>", userSession.getUsername(), userSession.isActive());
        }

        private String getUuid(final String username) {
            return String.format("<attributes><attribute name=\"uuid\"><values><value>%s</value></values></attribute></attributes>", username);
        }

        private String getUsername(final String uuid) {
            return String.format("<user name=\"%s\"></user>", uuid);
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

        this.port = server.getConnectors()[0].getLocalPort();

        final String restUrl = String.format("http://localhost:%s/crowd", getPort());
        LOGGER.info("Crowd dummy server restUrl: "+restUrl);
        crowdClient.setRestUrl(restUrl);
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
