package net.ripe.db.whois.logsearch.bootstrap;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.Slf4JLogConfiguration;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LogSearchServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogSearchServer.class);

    private final LogSearchJettyBootstrap jettyBootstrap;

    @Autowired
    public LogSearchServer(final LogSearchJettyBootstrap jettyBootstrap) {
        this.jettyBootstrap = jettyBootstrap;
    }

    public static void main(final String[] args) {
        Slf4JLogConfiguration.init();

        final Stopwatch stopwatch = new Stopwatch().start();
        WhoisProfile.setDeployed();

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext-logsearch.xml");

        final LogSearchServer logSearchServer = applicationContext.getBean(LogSearchServer.class);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logSearchServer.stop();
            }
        });

        logSearchServer.start();

        LOGGER.info("Whois server started in {}", stopwatch.stop());
    }

    public void start() {
        LOGGER.info("Initializing: {}", jettyBootstrap);
        jettyBootstrap.start();
    }

    public void stop() {
        try {
            jettyBootstrap.stop(false);
        } catch (final RuntimeException e) {
            LOGGER.error("Stopping: {}", jettyBootstrap, e);
        }
    }
}
