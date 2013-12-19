package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.ServerHelper;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.TestingProfile;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@TestingProfile
public class CrowdServerDummy {
    private Server server;

    private int port = -1;

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
            else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private String getUuid(final String username) {
            return String.format("<attributes><attribute name=\"uuid\"><values><value>%s</value></values></attribute></attributes>", username);
        }

        private String getUsername(final String uuid) {
            return String.format("<user name=\"%s\"></user>", uuid);
        }
    }

    public void start() {
        try {
            start(ServerHelper.getActualPort(port));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RetryFor(attempts = 5, value = Exception.class)
    void start(final int port) throws Exception {
        this.port = port;
        server = new Server(port);
        server.setHandler(new CrowdTestHandler());
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return port;
    }
}
