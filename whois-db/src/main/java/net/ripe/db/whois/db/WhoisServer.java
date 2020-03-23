package net.ripe.db.whois.db;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.List;

@Component
public class WhoisServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisServer.class);

    private final ApplicationContext applicationContext;
    private final List<ApplicationService> applicationServices;
    private final ApplicationVersion applicationVersion;

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
}
