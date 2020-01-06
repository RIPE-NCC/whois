package net.ripe.db.whois.api.log;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jetty.server.RequestLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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

    @Value("var${jvmId:}/log/jetty")
    String requestLogDirectory;

    @Before
    public void setup() {
        databaseHelper.addObjects(OWNER_MNT, TEST_PERSON);
        addLog4jAppender();
    }

    @After
    public void tearDown() throws Exception {
        removeLog4jAppender();
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

    // helper methods

    private void cleanupRequestLogDirectory() throws IOException {
        for (File next : new File(requestLogDirectory).listFiles()) {
            if (next.isDirectory()) {
                FileUtils.deleteDirectory(next);
            } else {
                next.delete();
            }
        }
    }

    private void addLog4jAppender() {
        final FileAppender appender = new FileAppender();
        appender.setName("REQUESTLOG");
        appender.setFile(getRequestLogFilename());
        appender.setLayout(new PatternLayout());
        appender.activateOptions();
        Logger.getLogger(RequestLog.class).addAppender(appender);
    }

    private void removeLog4jAppender() {
        Logger.getLogger(RequestLog.class).removeAppender("REQUESTLOG");
    }

    private String getRequestLogFilename() {
        return String.format("%s/%s", requestLogDirectory, "request.log");
    }

    private String fileToString(final String filename) throws IOException {
        return new String(Files.readAllBytes(new File(filename).toPath()));
    }
}
