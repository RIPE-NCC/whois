package net.ripe.db.whois.api.httpserver;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.ServerHelper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

@Component
public class JettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyBootstrap.class);

    private final RemoteAddressFilter remoteAddressFilter;
    private final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter;
    private final List<ServletDeployer> servletDeployers;
    private final JettyConfig jettyConfig;

    private final List<Server> servers = Lists.newArrayList();
    private int internalPort;
    private int publicPort;

    @Value("${port.api.internal:-1}")
    public void setInternalPort(final int internalPort) {
        this.internalPort = internalPort;
    }

    @Value("${port.api.public:-1}")
    public void setPublicPort(final int publicPort) {
        this.publicPort = publicPort;
    }

    @Autowired
    public JettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                          final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter,
                          final List<ServletDeployer> servletDeployers,
                          final JettyConfig jettyConfig) {
        this.remoteAddressFilter = remoteAddressFilter;
        this.extensionOverridesAcceptHeaderFilter = extensionOverridesAcceptHeaderFilter;
        this.servletDeployers = servletDeployers;
        this.jettyConfig = jettyConfig;
    }

    @Override
    public void start() {
        servers.add(createAndStartServer(Audience.INTERNAL, internalPort, "/int-doc/"));
        servers.add(createAndStartServer(Audience.PUBLIC, publicPort, "/ext-doc/"));
    }

    Server createAndStartServer(final Audience audience, final int port, final String resourceBase) {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.addFilter(new FilterHolder(remoteAddressFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(extensionOverridesAcceptHeaderFilter), "/*", EnumSet.allOf(DispatcherType.class));

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource(resourceBase));
        LOGGER.info("Serving {} from {}", audience, resourceHandler.getResourceBase());

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resourceHandler, context});

        for (final ServletDeployer servletDeployer : servletDeployers) {
            if (servletDeployer.getAudience().equals(audience)) {
                servletDeployer.deploy(context);
            }
        }

        try {
            return createAndStartServer(port, handlers, audience);
        } catch (Exception e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    private Server createAndStartServer(int port, HandlerList handlers, Audience audience) throws Exception {
        Server server = null;
        int tryPort = -1;
        int retry = 0;
        try {
            tryPort = (port <= 0) ? ServerHelper.getAvailablePort() : port;
            LOGGER.info("Trying port {}", tryPort);
            server = new Server(tryPort);
            server.setHandler(handlers);
            server.setStopAtShutdown(true);

            server.start();
            jettyConfig.setPort(audience, tryPort);
            LOGGER.info("Jetty started on port {} ({})", tryPort, audience);
        } catch (Exception ex) {
            ++retry;
            LOGGER.info("Tried port {} but failed to start server", tryPort);
            if (retry > 5) {
                throw ex;
            }
        }
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
