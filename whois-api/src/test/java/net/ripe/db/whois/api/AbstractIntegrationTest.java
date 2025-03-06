package net.ripe.db.whois.api;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired protected JettyBootstrap jettyBootstrap;
    @Autowired protected List<ApplicationService> applicationServices;

    protected final StringWriter stringWriter = new StringWriter();

    @Value("${rdap.public.baseUrl:}")
    private String rdapBaseUrl;

    @BeforeEach
    public void startServer() {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }
    }

    protected void setTime(final LocalDateTime localDateTime){
        testDateTimeProvider.setTime(localDateTime);
    }

    @AfterEach
    public void stopServer() {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.stop(true);
        }
    }

    public int getPort() {
        return jettyBootstrap.getPort();
    }

    /**
     * This method can be called anywhere in a derived test class to
     * be able to debug the server on a local development machine.
     */
    protected synchronized void stopExecutionHereButKeepTheServerRunning() {
        Instant start = Instant.now();

        while (true) {
            Duration timeElapsed = Duration.between(start, Instant.now());
            System.out.println(String.format("Server listening for %d minutes on port %d", timeElapsed.toMinutes(), getPort()));
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
        }
    }

    // Ref. https://logging.apache.org/log4j/2.x/manual/customconfig.html
    protected void addLog4jAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        this.stringWriter.getBuffer().setLength(0);
        final Appender appender = WriterAppender.newBuilder()
                .setName("REQUESTLOG")
                .setTarget(this.stringWriter)
                .build();
        appender.start();
        config.addAppender(appender);
        final AppenderRef ref = AppenderRef.createAppenderRef("REQUESTLOG", null, null);
        final AppenderRef[] refs = new AppenderRef[] {ref};
        final LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.ALL, "org.eclipse.jetty.server.RequestLog", "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("org.eclipse.jetty.server.RequestLog", loggerConfig);
        ctx.updateLoggers();
    }

    protected void removeLog4jAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        config.removeLogger("org.eclipse.jetty.server.RequestLog");
        ctx.updateLoggers();
    }

    protected String getRequestLog() {
        return stringWriter.toString();
    }

    protected Map<String, String> getRelationCallsFromLinks(final List<Link> links){
        return links.stream()
                .filter(link ->  RelationType.isValidValue(link.getRel())
                        || link.getRel().contains("up-active")
                        || link.getRel().contains("top-active"))
                .collect(Collectors.toMap(
                        link -> link.getRel().toUpperCase(),
                        link -> link.getHref().replace(rdapBaseUrl + "/", "")
                ));
    }
}
