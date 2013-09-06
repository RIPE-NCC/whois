package net.ripe.db.whois.logsearch;

import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.logsearch.bootstrap.LogSearchJettyBootstrap;
import net.ripe.db.whois.logsearch.bootstrap.LogSearchJettyConfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-logsearch-test.xml"})
public class LogSearchLegacyFormatTestIntegration extends AbstractJUnit4SpringContextTests {
    @Autowired
    private LogSearchJettyBootstrap logSearchJettyBootstrap;
    @Autowired
    private LogSearchJettyConfig logSearchJettyConfig;
    @Autowired
    private LegacyLogFormatProcessor legacyLogFormatProcessor;
    @Autowired
    private LogFileIndex logFileIndex;

    private Client client;

    private static File indexDirectory = Files.createTempDir();
    private static File logDirectory = Files.createTempDir();

    @Value("${api.key}")
    private String apiKey;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("dir.logsearch.index", indexDirectory.getAbsolutePath());
        System.setProperty("dir.update.audit.log", logDirectory.getAbsolutePath());
    }

    @AfterClass
    public static void cleanupClass() {
        System.clearProperty("dir.logsearch.index");
        System.clearProperty("dir.update.audit.log");
    }

    @Before
    public void setup() {
        LogFileHelper.createLogDirectory(logDirectory);
        logSearchJettyBootstrap.start();
        client = Client.create(new DefaultClientConfig());
    }

    @After
    public void cleanup() {
        LogFileHelper.deleteLogs(logDirectory);
        logFileIndex.removeAll();
        logSearchJettyBootstrap.stop(true);
    }

    @Test
    public void legacy_one_logfile() throws Exception {
        addToIndex(LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox"));

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_one_logfile_no_duplicates() throws Exception {
        final File logfile = LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logfile);
        addToIndex(logfile);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_directory_one_logfile() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_directory_multiple_logfiles() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        LogFileHelper.createBzippedLogFile(logDirectory, "20100102", "the quick brown fox");
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 2 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void legacy_log_directory_no_duplicates() throws Exception {
        LogFileHelper.createBzippedLogFile(logDirectory, "20100101", "the quick brown fox");
        addToIndex(logDirectory);
        addToIndex(logDirectory);

        final String response = getUpdates("quick");

        assertThat(response, containsString("Found 1 update log(s)"));
        assertThat(response, containsString("the quick brown fox"));
    }

    @Test
    public void override_is_filtered() throws Exception {
        addToIndex(LogFileHelper.createBzippedLogFile(logDirectory, "20100101",
                "REQUEST FROM:127.0.0.1\n" +
                "PARAMS:\n" +
                "DATA=\n" +
                "\n" +
                "inet6num:      2001::/64\n" +
                "source:        RIPE\n" +
                "override: username,password,remark\n"));

        assertThat(getUpdates("2001::/64"), containsString("override: FILTERED\n"));
    }

    // API calls

    private String getUpdates(final String searchTerm) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&fromdate=&todate=&apiKey=%s", logSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), apiKey))
                .get(String.class);
    }

    private String getUpdates(final String searchTerm, final String date) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&fromdate=%s&apiKey=%s", logSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), date, apiKey))
                .get(String.class);
    }

    private String getUpdates(final String searchTerm, final String fromDate, final String toDate) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&fromdate=%s&todate=%s&apiKey=%s", logSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), fromDate, toDate, apiKey))
                .get(String.class);
    }

    // helper methods

    private void addToIndex(final File file) throws IOException {
        if (file.isDirectory()) {
            legacyLogFormatProcessor.addDirectoryToIndex(file.getAbsolutePath());
        } else {
            legacyLogFormatProcessor.addFileToIndex(file.getAbsolutePath());
        }
    }
}
