package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.JmxException;
import org.springframework.stereotype.Component;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.DispatcherType;
import java.lang.management.ManagementFactory;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;

@Component
public class JettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyBootstrap.class);

    private static final String EXTENDED_RIPE_LOG_FORMAT = "%{client}a %{host}i - %u %{dd/MMM/yyyy:HH:mm:ss Z|" + ZoneOffset.systemDefault().getId() + "}t \"%r\" %s %O %D \"%{Referer}i\" \"%{User-Agent}i\"";

    private final ObjectName dosFilterMBeanName;

    private final RemoteAddressFilter remoteAddressFilter;
    private final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter;
    private final List<ServletDeployer> servletDeployers;
    private Server server;

    private int port = 0;
    private final int idleTimeout;

    private final RewriteEngine rewriteEngine;
    private final String trustedIpRanges;
    private final boolean rewriteEngineEnabled;

    private final boolean dosFilterEnabled;

    private final DelayShutdownHook delayShutdownHook;

    @Autowired
    public JettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                          final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter,
                          final List<ServletDeployer> servletDeployers,
                          final RewriteEngine rewriteEngine,
                          final DelayShutdownHook delayShutdownHook,
                          @Value("${ipranges.trusted}") final String trustedIpRanges,
                          @Value("${http.idle.timeout.sec:60}") final int idleTimeout,
                          @Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                          @Value("${rewrite.engine.enabled:false}") final boolean rewriteEngineEnabled) throws MalformedObjectNameException {
        this.remoteAddressFilter = remoteAddressFilter;
        this.extensionOverridesAcceptHeaderFilter = extensionOverridesAcceptHeaderFilter;
        this.servletDeployers = servletDeployers;
        this.rewriteEngine = rewriteEngine;
        this.delayShutdownHook = delayShutdownHook;
        this.trustedIpRanges = trustedIpRanges;
        this.rewriteEngineEnabled = rewriteEngineEnabled;
        LOGGER.info("Rewrite engine is {}abled", rewriteEngineEnabled? "en" : "dis");
        this.dosFilterMBeanName = ObjectName.getInstance("net.ripe.db.whois:name=DosFilter");
        this.dosFilterEnabled = dosFilterEnabled;
        this.idleTimeout = idleTimeout;
    }

    @Override
    public void start() {
        server = createAndStartServer(port);
    }

    @Value("${port.api:0}")
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

        try {
            context.addFilter(createDosFilter(), "/*", EnumSet.allOf(DispatcherType.class));
        } catch (JmxException | JMException je) {
            throw new RuntimeException(je);
        }

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context });

        if (rewriteEngineEnabled) {
            final RewriteHandler rewriteHandler = rewriteEngine.getRewriteHandler();
            rewriteHandler.setHandler(context);
            handlers.setHandlers(new Handler[] { rewriteHandler });
        }

        for (final ServletDeployer servletDeployer : servletDeployers) {
            servletDeployer.deploy(context);
        }

        try {
            return createAndStartServer(port, handlers);
        } catch (Exception e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    /**
     * Use the DoSFilter from Jetty for rate limitting: https://www.eclipse.org/jetty/documentation/current/dos-filter.html.
     * See {@link WhoisDoSFilter} for the customisations added.
     * @return the rate limiting filter
     * @throws JmxException if anything goes wrong JMX wise
     * @throws JMException if anything goes wrong JMX wise
     */
    private FilterHolder createDosFilter() throws JmxException, JMException {
        WhoisDoSFilter dosFilter = new WhoisDoSFilter();
        FilterHolder holder = new FilterHolder(dosFilter);
        holder.setName("DoSFilter");

        if (!dosFilterEnabled) {
            LOGGER.info("DoSFilter is *not* enabled");
        }
        holder.setInitParameter("enabled", Boolean.toString(dosFilterEnabled));
        holder.setInitParameter("maxRequestsPerSec", "50");
        holder.setInitParameter("maxRequestMs", "" + 10 * 60 * 1_000); // high default, 10 minutes
        holder.setInitParameter("delayMs", "-1"); // reject requests over threshold
        holder.setInitParameter("remotePort", "false");
        holder.setInitParameter("trackSessions", "false");
        holder.setInitParameter("insertHeaders", "false");
        holder.setInitParameter("ipWhitelist", trustedIpRanges);

        if (!ManagementFactory.getPlatformMBeanServer().isRegistered(dosFilterMBeanName)) {
            ManagementFactory.getPlatformMBeanServer().registerMBean(new ObjectMBean(dosFilter), dosFilterMBeanName);
        }

        return holder;
    }

    @RetryFor(attempts = 5, value = Exception.class)
    private Server createAndStartServer(int port, HandlerList handlers) throws Exception {
        delayShutdownHook.register();
        final Server server = new Server(port);
        server.setHandler(handlers);
        server.setStopAtShutdown(true);
        server.setRequestLog(createRequestLog());

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setIdleTimeout(idleTimeout * 1000);
        httpConfig.addCustomizer( new RemoteAddressCustomizer() );

        final HttpConnectionFactory connectionFactory = new HttpConnectionFactory( httpConfig );
        final ServerConnector connector = new ServerConnector(server, connectionFactory);

        //the port in the Server constructor is overridden by the new connector
        connector.setPort(port);

        server.setConnectors( new ServerConnector[] { connector } );

        server.start();
        this.port = ((NetworkConnector)server.getConnectors()[0]).getLocalPort();
        LOGGER.info("Jetty started on port {}", this.port);
        return server;
    }

    @Override
    public void stop(final boolean force) {
        new Thread(() -> {
            try {
                if (ManagementFactory.getPlatformMBeanServer().isRegistered(dosFilterMBeanName)) {
                    ManagementFactory.getPlatformMBeanServer().unregisterMBean(dosFilterMBeanName);
                }
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
        return new CustomRequestLog(new FilteredSlf4jRequestLogWriter("password"), EXTENDED_RIPE_LOG_FORMAT);
    }
}
