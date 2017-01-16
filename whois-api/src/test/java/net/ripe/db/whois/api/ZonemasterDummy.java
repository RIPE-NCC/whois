package net.ripe.db.whois.api;

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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Profile({WhoisProfile.TEST})
@Component
public class ZonemasterDummy implements Stub {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZonemasterDummy.class);

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
            throw new UnsupportedOperationException("not implemented yet");
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
