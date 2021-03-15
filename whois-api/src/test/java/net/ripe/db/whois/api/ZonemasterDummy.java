package net.ripe.db.whois.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.update.dns.zonemaster.ZonemasterRestClient;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Profile({WhoisProfile.TEST})
@Component
public class ZonemasterDummy implements Stub {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDummy.class);

    private static final Map<String, List<String>> RESPONSES = Maps.newHashMap();

    private Server server;
    private int port = 0;

    private final ZonemasterRestClient zonemasterClient;

    @Autowired
    public ZonemasterDummy(final ZonemasterRestClient zonemasterClient) {
        this.zonemasterClient = zonemasterClient;
    }

    private static class ZonemasterHandler extends AbstractHandler {

        @Override
        public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
            final String requestBody = getRequestBody(request);
            for (Map.Entry<String, List<String>> entry : RESPONSES.entrySet()) {
                if (requestBody.contains(entry.getKey())) {
                    putResponseBody(response, removeFirst(entry.getValue()));
                    return;
                }
            }

            throw new IllegalStateException("request not handled: " + requestBody);
        }

        private String getRequestBody(final HttpServletRequest request) throws IOException {
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }

        private void putResponseBody(final HttpServletResponse response, final String body) throws IOException {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            try (final PrintWriter writer = response.getWriter()) {
                writer.write(body);
            }
        }

        private String removeFirst(final List<String> list) {
            final ListIterator<String> iterator = list.listIterator();
            if (!iterator.hasNext()) {
                throw new IllegalStateException("No RESPONSES left");
            } else {
                final String response = iterator.next();
                iterator.remove();
                return response;
            }
        }
    }

    @PostConstruct
    @RetryFor(attempts = 5, value = Exception.class)
    public void start() {
        server = new Server(0);
        server.setHandler(new ZonemasterHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();

        final String baseUrl = String.format("http://localhost:%s/zonemaster", getPort());
        LOGGER.info("Zonemaster dummy server restUrl: {}", baseUrl);
        ReflectionTestUtils.setField(zonemasterClient, "baseUrl", baseUrl);
    }

    public void whenThen(final String when, final String then) {
        final List<String> values = RESPONSES.get(when);
        if (values != null) {
            values.add(then);
        } else {
            RESPONSES.put(when, Lists.newArrayList(then));
        }
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
