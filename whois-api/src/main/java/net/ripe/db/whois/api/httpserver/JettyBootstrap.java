package net.ripe.db.whois.api.httpserver;

import io.netty.handler.ssl.util.TrustManagerFactoryWrapper;
import jakarta.servlet.DispatcherType;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.jmx.ObjectMBean;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.PushCacheFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.JmxException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.lang.management.ManagementFactory;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

// TODO: [ES] remove duplicate code from InternalJettyBootstrap in whois-internal
@Component
public class JettyBootstrap implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyBootstrap.class);

    private static final String EXTENDED_RIPE_LOG_FORMAT =
        "%{client}a " +         // log client address
        "%{host}i " +           // host
        "- " +                  // -
        "%u " +                 // URL path
        "%{dd/MMM/yyyy:HH:mm:ss Z|" + ZoneOffset.systemDefault().getId() + "}t " +  // timestamp
        "\"%r\" " +             // method
        "%s " +                 // status code
        "%O " +                 // bytes sent
        "%D " +                 // elapsed time (microseconds)
        "\"%{Referer}i\" " +    // Referer header
        "\"%{User-Agent}i\"";   // User-Agent header

    // copied from SslContextFactory
    private static final String[] DEFAULT_EXCLUDED_CIPHER_SUITES = {
            // Exclude weak / insecure ciphers
            "^.*_(MD5|SHA|SHA1)$",
            // Exclude ciphers that don't support forward secrecy
            "^TLS_RSA_.*$",
            // The following exclusions are present to cleanup known bad cipher
            // suites that may be accidentally included via include patterns.
            // The default enabled cipher list in Java will not include these
            // (but they are available in the supported list).
            "^SSL_.*$",
            "^.*_NULL_.*$",
            "^.*_anon_.*$"
    };
    private static final String[] DEFAULT_EXCLUDED_PROTOCOLS = {
            "SSL",
            "SSLv2",
            "SSLv2Hello",
            "SSLv3"
    };

    private final ObjectName dosFilterMBeanName;

    private final RemoteAddressFilter remoteAddressFilter;
    private final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter;
    private final List<ServletDeployer> servletDeployers;
    private final RewriteEngine rewriteEngine;
    private final WhoisKeystore whoisKeystore;
    private final String trustedIpRanges;
    private final boolean rewriteEngineEnabled;
    private final boolean dosFilterEnabled;
    private final boolean sniHostCheck;
    private Server server;
    private int securePort;
    private int port;
    private final int idleTimeout;

    private int clientAuthPort;

    private final boolean xForwardedForHTTPS;

    @Autowired
    public JettyBootstrap(final RemoteAddressFilter remoteAddressFilter,
                          final ExtensionOverridesAcceptHeaderFilter extensionOverridesAcceptHeaderFilter,
                          final List<ServletDeployer> servletDeployers,
                          final RewriteEngine rewriteEngine,
                          final WhoisKeystore whoisKeystore,
                          @Value("${ipranges.trusted}") final String trustedIpRanges,
                          @Value("${http.idle.timeout.sec:60}") final int idleTimeout,
                          @Value("${http.sni.host.check:true}") final boolean sniHostCheck,
                          @Value("${dos.filter.enabled:false}") final boolean dosFilterEnabled,
                          @Value("${rewrite.engine.enabled:false}") final boolean rewriteEngineEnabled,
                          @Value("${port.api:0}") final int port,
                          @Value("${port.api.secure:-1}") final int securePort,
                          @Value("${port.client.auth:-1}") final int clientAuthPort,
                          @Value("${https.x_forwarded_for:false}") final boolean xForwardedForHTTPS
                        ) throws MalformedObjectNameException {
        this.remoteAddressFilter = remoteAddressFilter;
        this.extensionOverridesAcceptHeaderFilter = extensionOverridesAcceptHeaderFilter;
        this.servletDeployers = servletDeployers;
        this.rewriteEngine = rewriteEngine;
        this.whoisKeystore = whoisKeystore;
        this.trustedIpRanges = trustedIpRanges;
        this.rewriteEngineEnabled = rewriteEngineEnabled;
        LOGGER.info("Rewrite engine is {}abled", rewriteEngineEnabled ? "en" : "dis");
        this.dosFilterMBeanName = ObjectName.getInstance("net.ripe.db.whois:name=DosFilter");
        this.dosFilterEnabled = dosFilterEnabled;
        this.sniHostCheck = sniHostCheck;
        this.idleTimeout = idleTimeout;
        this.securePort = securePort;
        this.port = port;
        this.server = null;
        this.clientAuthPort = clientAuthPort;
        this.xForwardedForHTTPS = xForwardedForHTTPS;
    }

    @Override
    public void start() {
        this.server = createAndStartServer();
        updatePorts();
        logJettyStarted();
        logHttpsConfig();
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public int getSecurePort() {
        return this.securePort;
    }

    public int getClientAuthPort(){
        return this.clientAuthPort;
    }

    public Server getServer() {
        return this.server;
    }

    private Server createAndStartServer() {
        try {
            return startServer(createServer());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start server", e);
        }
    }

    /* TODO: define thread pool: https://www.baeldung.com/jetty-embedded
     maxThreads default 200
     minThreads: default is min(8, maxThreads)
     idleTimeout: default 60000
     QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
    server = new Server(threadPool);
      */
    private Server createServer() {


        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.addFilter(new FilterHolder(remoteAddressFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(extensionOverridesAcceptHeaderFilter), "/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        try {
            context.addFilter(createDosFilter(), "/*", EnumSet.allOf(DispatcherType.class));
        } catch (JmxException | JMException e) {
            throw new IllegalStateException("Error creating DOS Filter", e);
        }

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context });
        final Server server = new Server();
        setConnectors(server);
        server.setHandler(handlers);

        server.setStopAtShutdown(false);
        server.setRequestLog(createRequestLog());

        if (rewriteEngineEnabled) {
            final RewriteHandler rewriteHandler = rewriteEngine.getRewriteHandler();
            rewriteHandler.setHandler(context);
            server.setHandler(rewriteHandler);
        }

        for (final ServletDeployer servletDeployer : servletDeployers) {
            servletDeployer.deploy(context);
        }

        return server;
    }

    /*private Server setConnectors() {
        final Server server = new Server();
        server.setConnectors(new Connector[]{createConnector(server)});

        if (!isHttpProxy()) {
            server.addConnector(createSecureConnector(server, this.securePort, false));
            if (isClientAuthEnabled()) {
                server.addConnector(createSecureConnector(server, this.clientAuthPort, true));
            }
        }
        return server;
    }*/

    private void setConnectors(final Server server) {
        final HttpConfiguration httpConfiguration = new HttpConfiguration();
        if (isHttpProxy()){
            // client address is set in X-Forwarded-For header by HTTP proxy
            httpConfiguration.addCustomizer(new RemoteAddressCustomizer(trustedIpRanges, true));
            // request protocol is set in X-Forwarded-Proto header by HTTP proxy
            httpConfiguration.addCustomizer(new ProtocolCustomizer());
            server.setConnectors(new Connector[]{createConnector(server, httpConfiguration)});
        } else {
            httpConfiguration.addCustomizer(new RemoteAddressCustomizer(trustedIpRanges, xForwardedForHTTPS));
            server.setConnectors(new Connector[]{createConnector(server, httpConfiguration)});

            server.addConnector(createSecureConnector(server, this.securePort, false));
            if (isClientAuthEnabled()) {
                server.addConnector(createSecureConnector(server, this.clientAuthPort, true));
            }
        }

    }

    private Connector createConnector(final Server server, final HttpConfiguration httpConfiguration) {
        httpConfiguration.setIdleTimeout(idleTimeout * 1000L);
        httpConfiguration.setUriCompliance(UriCompliance.LEGACY);
        final ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration), new HTTP2CServerConnectionFactory(httpConfiguration));
        connector.setPort(this.port);
        return connector;
    }



    /**
     * Use the DoSFilter from Jetty for rate limiting: https://www.eclipse.org/jetty/documentation/current/dos-filter.html.
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

    private Connector createSecureConnector(final Server server, final int port, final boolean isClientCertificate) {
        // allow (untrusted) self-signed certificates to connect
        final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server() {
            @Override
            protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) {
                return SslContextFactory.TRUST_ALL_CERTS;
            }

            @Override
            protected TrustManagerFactory getTrustManagerFactoryInstance() {
                return new TrustManagerFactoryWrapper(SslContextFactory.TRUST_ALL_CERTS[0]);
            }
        };

        final String keystore = whoisKeystore.getKeystore();
        if (keystore == null) {
            throw new IllegalStateException("NO keystore");
        }

        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(whoisKeystore.getPassword());
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

        if (isClientCertificate) {
            // accept self-signed client certificates for authentication
            sslContextFactory.setNeedClientAuth(true);
            sslContextFactory.setValidateCerts(false);
            sslContextFactory.setTrustAll(true);
        }
        
        // Exclude weak / insecure ciphers
        // TODO CBC became weak, we need to skip them in the future https://support.kemptechnologies.com/hc/en-us/articles/9338043775757-CBC-ciphers-marked-as-weak-by-SSL-labs
        // Check client compatability first
        sslContextFactory.setExcludeCipherSuites(DEFAULT_EXCLUDED_CIPHER_SUITES);
        sslContextFactory.setExcludeProtocols(DEFAULT_EXCLUDED_PROTOCOLS);

        final HttpConfiguration httpsConfiguration = new HttpConfiguration();

        final SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        if (!sniHostCheck) {
            LOGGER.warn("SNI host check is OFF");   // normally off for testing on localhost
            secureRequestCustomizer.setSniHostCheck(false);
        }

        httpsConfiguration.addCustomizer(new RemoteAddressCustomizer(trustedIpRanges, xForwardedForHTTPS));
        httpsConfiguration.addCustomizer(secureRequestCustomizer);

        httpsConfiguration.setIdleTimeout(idleTimeout * 1000L);
        httpsConfiguration.setUriCompliance(UriCompliance.LEGACY);

        final HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpsConfiguration);
        final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(HttpVersion.HTTP_1_1.asString());

        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        final ServerConnector sslConnector = new ServerConnector(server, sslConnectionFactory, alpn, h2, new HttpConnectionFactory(httpsConfiguration));
        sslConnector.setPort(port);
        return sslConnector;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1_000L)
    private void reloadSecureContextOnKeyChange() {
        final String keystore = whoisKeystore.getKeystore();
        if (keystore == null) {
            return;
        }

        if (!whoisKeystore.isKeystoreOutdated()) {
            return;
        }

        try {
            whoisKeystore.reloadKeystore();
        } catch (Exception e) {
            LOGGER.error("Failed to reload keystore due to {}: {}", e.getClass().getName(), e.getMessage());
            return;
        }

        for (Connector connector : this.server.getConnectors()) {
            for (ConnectionFactory connectionFactory : (connector.getConnectionFactories())) {
                if (connectionFactory instanceof SslConnectionFactory) {
                    try {
                        ((SslConnectionFactory)connectionFactory).getSslContextFactory().reload(sslContextFactory -> {});
                    } catch (Exception e) {
                        LOGGER.error("Failed to reload ssl context due to {}: {}", e.getClass().getName(), e.getMessage());
                    }
                }
            }
        }

        LOGGER.info("Reloaded SSL Context on key change");
    }

    @RetryFor(attempts = 5, value = Exception.class)
    private Server startServer(final Server server) throws Exception {
        server.start();
        return server;
    }

    @Override
    public void stop(final boolean force) {
        LOGGER.info("Shutdown Jetty");
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
        return new CustomRequestLog(new FilteredPasswordSlf4RequestLogWriter(), EXTENDED_RIPE_LOG_FORMAT);
    }

    private void logHttpsConfig() {
        for (Connector connector : this.server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
                if (connectionFactory instanceof SslConnectionFactory) {
                    final SslConnectionFactory sslConnectionFactory = (SslConnectionFactory)connectionFactory;
                    final SslContextFactory.Server sslContextFactory = sslConnectionFactory.getSslContextFactory();
                    for (String alias : sslContextFactory.getAliases()) {
                        LOGGER.info("Certificate:       {}", sslContextFactory.getX509(alias));
                    }
                    LOGGER.info("Selected Protocols {}", Arrays.asList(sslContextFactory.getSelectedProtocols()));
                    LOGGER.info("Selected Ciphers   {}", Arrays.asList(sslContextFactory.getIncludeCipherSuites()));

                }
            }
        }
    }

    // Update port numbers once server has started (if initially set to 0, a random unused port is used)
    private void updatePorts() {
        for (Connector connector : this.server.getConnectors()) {
            final int localPort = ((NetworkConnector) connector).getLocalPort();
            if(!connector.getProtocols().contains("ssl")) {
                this.port = localPort;
                continue;
            }

            if (isClientAuthEnabled()) {
                this.clientAuthPort = localPort;
                continue;
            }

            this.securePort = localPort;

        }
    }

    private void logJettyStarted() {
        if (this.securePort > 0) {
            LOGGER.info("Jetty started on HTTP port {} HTTPS port {}", this.port, this.securePort);
        } else {
            LOGGER.info("Jetty started on HTTP port {} (NO HTTPS)", this.port);
        }
    }

    private boolean isClientAuthEnabled(){
        return clientAuthPort >= 0;
    }
    private boolean isHttpProxy() {
        // if we are not handling HTTPS then assume a loadbalancer is proxying requests
        return securePort < 0;
    }
}
