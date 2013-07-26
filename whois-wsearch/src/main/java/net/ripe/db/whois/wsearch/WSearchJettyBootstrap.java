package net.ripe.db.whois.wsearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.acl.ApiKeyFilter;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.RemoteAddressFilter;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.ServerHelper;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.Application;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class WSearchJettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSearchJettyBootstrap.class);

    private final List<Server> servers = Lists.newArrayList();
    private final RemoteAddressFilter remoteAddressFilter;
    private final ApiKeyFilter apiKeyFilter;
    private final WSearchServlet wsearchServlet;
    private final LogSearchService logSearchService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final WSearchJettyConfig wSearchJettyConfig;

    private final int internalPort;

    @Autowired
    public WSearchJettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                                 final WSearchServlet wsearchServlet,
                                 final ApiKeyFilter apiKeyFilter,
                                 final LogSearchService logSearchService,
                                 final DefaultExceptionMapper defaultExceptionMapper,
                                 final WSearchJettyConfig wSearchJettyConfig,
                                 @Value("${port.wsearch.internal:-1}") final int internalPort) {
        this.remoteAddressFilter = remoteAddressFilter;
        this.wsearchServlet = wsearchServlet;
        this.apiKeyFilter = apiKeyFilter;
        this.logSearchService = logSearchService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.wSearchJettyConfig = wSearchJettyConfig;
        this.internalPort = internalPort;
    }

    @Override
    public void start() {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.addFilter(new FilterHolder(remoteAddressFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(apiKeyFilter), "/*", EnumSet.of(DispatcherType.REQUEST));

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(new ServletHolder(wsearchServlet), "/wsearch/*");
        servletContextHandler.addServlet(new ServletHolder("WSEARCH API", new ServletContainer(new Application() {
            @Override
            public Set<Object> getSingletons() {
                return Sets.newHashSet(
                        logSearchService,
                        defaultExceptionMapper);
            }
        })), "/api/*");

        try {
            servers.add(createAndStartServer(internalPort, servletContextHandler, Audience.INTERNAL));
        } catch (Exception e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }


    @RetryFor(attempts = 5, value = Exception.class)
    private Server createAndStartServer(int port, ServletContextHandler servletContextHandler, final Audience audience) throws Exception {
        int tryPort = (port <= 0) ? ServerHelper.getAvailablePort() : port;
        wSearchJettyConfig.setPort(tryPort);
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
