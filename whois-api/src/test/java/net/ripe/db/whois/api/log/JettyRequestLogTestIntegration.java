package net.ripe.db.whois.api.log;

import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Tag("IntegrationTest")
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

    private final StringWriter stringWriter = new StringWriter();

    @BeforeEach
    public void setup() {
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);
        addLog4jAppender();
    }

    @AfterEach
    public void tearDown() {
        removeLog4jAppender();
    }

    @Test
    public void log_request() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .get(WhoisResources.class);

        // request log (default format)
        // 127.0.0.1 - - [27/Sep/2019:13:18:39 +0200] "GET /whois/test/person/TP1-TEST HTTP/1.1" 200 1002 "-" "Jersey/2.12 (HttpUrlConnection 1.8.0_161)" 182

        assertThat(getRequestLog(), containsString("\"GET /whois/test/person/TP1-TEST HTTP/1.1\" 200"));
    }

    @Test
    public void log_request_x_forwarded_for() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST")
                .request()
                .header(HttpHeaders.X_FORWARDED_FOR, "10.20.30.40")
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("10.20.30.40"));
    }


    @Test
    public void password_filtered() throws Exception {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=some-api_key-123")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?password=FILTERED"));
        assertThat(getRequestLog(), not(containsString("some-api_key-123")));
    }

    @Test
    public void password_and_override_filtered() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=some-api_key-123&override=SOME-USER,some-users-password,reason")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?password=FILTERED&override=SOME-USER,FILTERED,reason"));
        assertThat(getRequestLog(), not(containsString("some-api_key-123")));
    }

    @Test
    public void password_and_encoded_override_filtered() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=some-api_key-123&override=SOME-USER%2Csome-users-password%2Creason")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?password=FILTERED&override=SOME-USER,FILTERED,reason"));
        assertThat(getRequestLog(), not(containsString("some-api_key-123")));
    }

    @Test
    public void multiple_password_filtered() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=pass1&password=pass2")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?password=FILTERED&password=FILTERED"));
        assertThat(getRequestLog(), not(containsString("pass1")));
        assertThat(getRequestLog(), not(containsString("pass2")));
    }

    @Test
    public void multiple_query_password_filtered() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=pass1&key=value")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?password=FILTERED&key=value"));
        assertThat(getRequestLog(), containsString("key=value"));
        assertThat(getRequestLog(), not(containsString("pass1")));
    }

    @Test
    public void multiple_query_password_last_filtered() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?key=value&password=pass1")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("GET /whois/test/person/TP1-TEST?key=value&password=FILTERED"));
        assertThat(getRequestLog(), containsString("key=value"));
        assertThat(getRequestLog(), not(containsString("pass1")));
    }

    @Test
    public void password_filtered_case_insensitive() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?PassWord=pass1")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("PassWord=FILTERED"));
        assertThat(getRequestLog(), not(containsString("pass1")));
    }

    @Test
    public void override_filtered_case_insensitive() {
        RestTest.target(getPort(), "whois/test/person/TP1-TEST?oVeRrIdE=overrideUser,overPASS1")
                .request()
                .get(WhoisResources.class);

        assertThat(getRequestLog(), containsString("oVeRrIdE=overrideUser,FILTERED"));
        assertThat(getRequestLog(), not(containsString("overPASS1")));
    }

    // helper methods

    // Ref. https://logging.apache.org/log4j/2.x/manual/customconfig.html
    private void addLog4jAppender() {
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

    private void removeLog4jAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        config.removeLogger("org.eclipse.jetty.server.RequestLog");
        ctx.updateLoggers();
    }

    private String getRequestLog() {
        return stringWriter.toString();
    }

}
