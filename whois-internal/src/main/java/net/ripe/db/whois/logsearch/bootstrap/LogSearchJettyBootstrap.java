package net.ripe.db.whois.logsearch.bootstrap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.acl.ApiKeyFilter;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.RemoteAddressFilter;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.ServerHelper;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.logsearch.api.LogSearchService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

@Component
public class LogSearchJettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSearchJettyBootstrap.class);

    private final List<Server> servers = Lists.newArrayList();
    private final RemoteAddressFilter remoteAddressFilter;
    private final ApiKeyFilter apiKeyFilter;
    private final LogSearchServlet logSearchServlet;
    private final LogSearchService logSearchService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final LogSearchJettyConfig logSearchJettyConfig;

    private final int internalPort;

    @Autowired
    public LogSearchJettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                                   final LogSearchServlet logSearchServlet,
                                   final ApiKeyFilter apiKeyFilter,
                                   final LogSearchService logSearchService,
                                   final DefaultExceptionMapper defaultExceptionMapper,
                                   final LogSearchJettyConfig logSearchJettyConfig,
                                   @Value("${port.logsearch.internal:-1}") final int internalPort) {
        this.remoteAddressFilter = remoteAddressFilter;
        this.logSearchServlet = logSearchServlet;
        this.apiKeyFilter = apiKeyFilter;
        this.logSearchService = logSearchService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.logSearchJettyConfig = logSearchJettyConfig;
        this.internalPort = internalPort;
    }

    @Override
    public void start() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.setResourceBase("src/main/webapp");
        servletContextHandler.addFilter(new FilterHolder(remoteAddressFilter), "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(new FilterHolder(apiKeyFilter), "/api/*", EnumSet.of(DispatcherType.REQUEST));
        servletContextHandler.addServlet(new ServletHolder(logSearchServlet), "/logsearch/*");
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(logSearchService);
        resourceConfig.register(defaultExceptionMapper);
        servletContextHandler.addServlet(new ServletHolder("LOGSEARCH API", new ServletContainer(resourceConfig)), "/api/*");

        try {
            servers.add(createAndStartServer(internalPort, servletContextHandler, Audience.INTERNAL));
        } catch (Exception e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    @RetryFor(attempts = 5, value = Exception.class)
    private Server createAndStartServer(int port, ServletContextHandler servletContextHandler, final Audience audience) throws Exception {
        int tryPort = (port <= 0) ? ServerHelper.getAvailablePort() : port;
        logSearchJettyConfig.setPort(tryPort);
        LOGGER.debug("Trying port {}", tryPort);

        final Server server = new Server(tryPort);
        server.setStopAtShutdown(true);
        server.setHandler(servletContextHandler);

        server.start();
        LOGGER.info("Jetty started on port {} ({})", tryPort, audience);
        return server;
    }

    @Override
    public void stop(final boolean force) {
        for (final Server server : servers) {
            stopServer(server);
        }

        servers.clear();
    }

    private static void stopServer(final Server server) {
        new Thread() {
            @Override
            public void run() {
                try {
                    server.stop();
                } catch (Exception e) {
                    LOGGER.error("Stopping server", e);
                }
            }
        }.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            LOGGER.error("Stopping server", e);
        }
    }
}
