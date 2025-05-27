package net.ripe.db.whois.db;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.ReadinessHealthCheck;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.Closeable;
import java.security.Security;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Configuration
@EnableSpringConfigured
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@Component
public class WhoisServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisServer.class);

    private final ApplicationContext applicationContext;
    private final List<ApplicationService> applicationServices;
    private final ApplicationVersion applicationVersion;

    @Value("${shutdown.pause.sec:10}")
    private int preShutdownPause;

    @Autowired
    public WhoisServer(
            final ApplicationContext applicationContext,
            final List<ApplicationService> applicationServices,
            final ApplicationVersion applicationVersion) {
        this.applicationContext = applicationContext;
        this.applicationServices = applicationServices;
        this.applicationVersion = applicationVersion;
    }

    public static void main(final String[] args) {

        if (!ZoneId.systemDefault().equals(ZoneId.of("UTC"))) {
            throw new IllegalStateException(String.format("Illegal timezone: %s. Application timezone should be UTC", ZoneId.systemDefault()));
        }

        Slf4JLogConfiguration.init();
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final ClassPathXmlApplicationContext applicationContext = WhoisProfile.initContextWithProfile("applicationContext-whois.xml", WhoisProfile.DEPLOYED);
        final WhoisServer whoisServer = applicationContext.getBean(WhoisServer.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                whoisServer.stop();
            }
        });

        whoisServer.start();

        final MutablePropertySources sources = applicationContext.getEnvironment().getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .sorted()
                .forEach(prop -> LOGGER.info("{}: {}", prop, (prop.contains("credentials") || prop.contains("password")) ? "*****" :  applicationContext.getEnvironment().getProperty(prop)));

        printJvmSecurityProperties();
        LOGGER.info("Whois server started in {}", stopwatch.stop());
    }

    public void start() {
        for (final ApplicationService applicationService : applicationServices) {
            LOGGER.info("Initializing: {}", applicationService);
            applicationService.start();
        }

        LOGGER.info("Running version: {} (commit: {})", applicationVersion.getVersion(), applicationVersion.getCommitId());
    }

    public void stop() {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        markServiceAsDown(applicationContext);

        for (final ApplicationService applicationService : applicationServices) {
            stopService(applicationService, false);
        }

        for (final ApplicationService applicationService : applicationServices) {
            stopService(applicationService, true);
        }

        if (applicationContext instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) applicationContext);
        }

        LOGGER.info("Whois server stopped in {}", stopwatch.stop());
    }

    private void markServiceAsDown(final ApplicationContext context) {
        context.getBean(ReadinessHealthCheck.class).down();

        LOGGER.info("waiting for {} seconds before starting to close spring context", preShutdownPause);
        // This sleep is needed to also prevent other applicationServices from shutting
        // within the grace period the jetty server indicates to be taken out of the loadbalancer pool
        Uninterruptibles.sleepUninterruptibly(this.preShutdownPause, TimeUnit.SECONDS);
        LOGGER.info("starting to destroy beans");
    }

    private void stopService(final ApplicationService applicationService, boolean forced) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            if (!forced) LOGGER.info("Preparing to shut down {}", applicationService);
            applicationService.stop(forced);
            if (forced) LOGGER.info("Stopped {} in {}", applicationService, stopwatch.stop());
        } catch (RuntimeException e) {
            LOGGER.error("Stopping: {}", applicationService, e);
        }
    }

    private static void printJvmSecurityProperties() {
        if(!Boolean.valueOf(Security.getProperty("security.overridePropertiesFile"))) {
            LOGGER.warn("security.overridePropertiesFile is false, cannot override security values");
        }

        final String networkAddrCacheTtl = Security.getProperty("networkaddress.cache.ttl");
        final String networkAddrNegativeCacheTtl = Security.getProperty("networkaddress.cache.negative.ttl");
        LOGGER.info("networkaddress.cache.ttl: {}", networkAddrCacheTtl);
        LOGGER.info("networkaddress.cache.negative.ttl: {}", networkAddrNegativeCacheTtl);

        if(networkAddrCacheTtl == null || networkAddrCacheTtl.equals("-1")) {
            LOGGER.warn("networkaddress.cache.ttl is not set properly");
        }

        if(networkAddrNegativeCacheTtl == null || networkAddrNegativeCacheTtl.equals("-1")) {
            LOGGER.warn("networkaddress.cache.negative.ttl is not set properly");
        }
    }
}
