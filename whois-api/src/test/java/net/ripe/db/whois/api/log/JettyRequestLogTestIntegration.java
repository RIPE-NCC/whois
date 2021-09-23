package net.ripe.db.whois.api.log;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.eclipse.jetty.server.RequestLog;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Category(IntegrationTest.class)
public class JettyRequestLogTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:        OWNER-MNT\n" +
            "descr:         Owner Maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        noreply@ripe.net\n" +
            "auth:          MD5-PW $1$GUKzBg/F$PoCZBbhTNxCKM3K9VF8y60\n" + // #team-red4321
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:        Test Person\n" +
            "address:       Singel 258\n" +
            "phone:         +31 6 12345678\n" +
            "nic-hdl:       TP1-TEST\n" +
            "mnt-by:        OWNER-MNT\n" +
            "source:        TEST");

    private final static String requestLogDirectory = "target/log/jetty";

    @Before
    public void setup() {
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);
        addLog4jAppender();
    }

    @After
    public void tearDown() {
        removeLog4jAppender();
    }
    
    @AfterClass
    public static void cleanUp() throws Exception {
        cleanupRequestLogDirectory();
    }

    @Test
    public void log_request() throws Exception {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .get(WhoisResources.class);

        // request log (default format)
        // 127.0.0.1 - - [27/Sep/2019:13:18:39 +0200] "GET /whois/test/person/TP1-TEST HTTP/1.1" 200 1002 "-" "Jersey/2.12 (HttpUrlConnection 1.8.0_161)" 182

        assertThat(fileToString(getRequestLogFilename()), containsString("\"GET /whois/test/person/TP1-TEST HTTP/1.1\" 200"));
    }

    @Test
    public void log_request_x_forwarded_for() throws Exception {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(WhoisResources.class);

        final String requestLongContent = fileToString(getRequestLogFilename());
        assertThat(requestLongContent, containsString("10.20.30.40"));
    }


    // helper methods

    private static void cleanupRequestLogDirectory() throws IOException {
        for (File next : new File(requestLogDirectory).listFiles()) {
            if (next.isDirectory()) {
                FileUtils.deleteDirectory(next);
            } else {
                next.delete();
            }
        }
    }

    private void addLog4jAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                //.withPattern("%d{HH:mm:ss.SSS} %level %msg%n")
                .build();

        Appender appender = FileAppender.newBuilder()
                .setConfiguration(config)
                .setName("REQUESTLOG")
                .setLayout(layout)
                .withFileName(getRequestLogFilename())
                .build();

        appender.start();
        config.addAppender(appender);

        AppenderRef ref = AppenderRef.createAppenderRef("requestLogAppender", null, null);
        AppenderRef[] refs = new AppenderRef[] { ref };

        LoggerConfig loggerConfig = LoggerConfig
                .createLogger(false, Level.TRACE, RequestLog.class.getName(), "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(RequestLog.class.getName(), loggerConfig);
        ctx.updateLoggers();
    }

    private void removeLog4jAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(true);
        Configuration config = ctx.getConfiguration();
        config.getRootLogger().removeAppender("requestLogAppender");
        ctx.updateLoggers();
    }

    private String getRequestLogFilename() {
        return String.format("%s/%s", requestLogDirectory, "request.log");
    }

    private String fileToString(final String filename) throws IOException {
        return new String(Files.readAllBytes(new File(filename).toPath()));
    }
}
