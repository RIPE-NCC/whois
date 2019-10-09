package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
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
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;

@Component
public class JettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyBootstrap.class);

    private static final String EXTENDED_RIPE_LOG_FORMAT = "%{client}a %{host}i - %u %{dd/MMM/yyyy:HH:mm:ss Z|" + ZoneOffset.systemDefault().getId() + "}t \"%r\" %s %O %D \"%{Referer}i\" \"%{User-Agent}i\"";

    private final RemoteAddressFilter remoteAddressFilter;
    private final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter;
    private final List<ServletDeployer> servletDeployers;
    private Server server;

    private int port = 0;

    @Autowired
    public JettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                          final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter,
                          final List<ServletDeployer> servletDeployers) {
        this.remoteAddressFilter = remoteAddressFilter;
        this.extensionOverridesAcceptHeaderFilter = extensionOverridesAcceptHeaderFilter;
        this.servletDeployers = servletDeployers;
    }

    @Override
    public void start() {
        server = createAndStartServer(port);
    }

    @Value("${port.api}")
    public void setPort(final int port) {
        if (port > 0) {
            this.port = port;
        }
    }

    public int getPort() {
        return port;
    }

    public Server getServer() {
        return server;
    }

    // handler to serve static resources directly from jetty
    private ResourceHandler getStaticResourceHandler(String resourceBase) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource(resourceBase));
        return resourceHandler;
    }

    private Server createAndStartServer(final int port) {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.addFilter(new FilterHolder(remoteAddressFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(extensionOverridesAcceptHeaderFilter), "/*", EnumSet.allOf(DispatcherType.class));

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{context});

        for (final ServletDeployer servletDeployer : servletDeployers) {
            servletDeployer.deploy(context);
        }

        try {
            return createAndStartServer(port, handlers);
        } catch (Exception e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    @RetryFor(attempts = 5, value = Exception.class)
    private Server createAndStartServer(int port, HandlerList handlers) throws Exception {
        final Server server = new Server(port);
        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        server.setRequestLog(createRequestLog());
        server.start();
        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();
        LOGGER.info("Jetty started on port {}", this.port);
        return server;
    }

    @Override
    public void stop(final boolean force) {
        new Thread(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                LOGGER.error("Stopping server", e);
            }
        }).start();

        try {
            server.join();
        } catch (InterruptedException e) {
            LOGGER.error("Stopping server", e);
        }
    }

    // Log requests to org.eclipse.jetty.server.RequestLog
    private RequestLog createRequestLog() {
        return new CustomRequestLog(new Slf4jRequestLogWriter(), EXTENDED_RIPE_LOG_FORMAT);
    }
}
