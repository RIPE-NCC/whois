package net.ripe.db.whois.api.wsearch;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WSearchTestIntegration extends AbstractIntegrationTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");
    private static final String INPUT_FILE_NAME = "001.msg-in.txt.gz";
    private static final Splitter PATH_SPLITTER = Splitter.on('/');

    private Client client;
    private File logFile;

    @Autowired
    private LogFileIndex logFileIndex;

    @Value("${dir.update.audit.log}")
    private String logDir;

    @Value("${api.key}")
    private String apiKey;

    @Before
    public void setup() {
        client = Client.create(new DefaultClientConfig());
    }

    @After
    public void removeLogfile() {
        if (logFile != null) {
            logFile.delete();
        }
    }

    @Test
    public void single_term() throws Exception {
        createLogFile("the quick brown fox");

        assertThat(getUpdates("quick"), containsString("the quick brown fox"));
    }

    @Test
    public void single_term_inetnum_with_prefix_length() throws Exception {
        createLogFile("inetnum: 10.0.0.0/24");

        assertThat(getUpdates("10.0.0.0/24"), containsString("inetnum: 10.0.0.0/24"));
    }

    @Test
    public void multiple_inetnum_terms() throws Exception {
        createLogFile("inetnum: 192.0.0.0 - 193.0.0.0");

        final String response = getUpdates("192.0.0.0 - 193.0.0.0");

        assertThat(response, containsString("192.0.0.0 - 193.0.0.0"));
    }

    @Test
    public void curly_brace_in_search_term() throws Exception {
        createLogFile("mnt-routes: ROUTES-MNT {2001::/48}");

        final String response = getUpdates("{2001::/48}");

        assertThat(response, containsString("ROUTES-MNT {2001::/48}"));
    }

    @Test
    public void search_multiple_terms_in_failed_update() throws Exception {
        createLogFile(
            "SUMMARY OF UPDATE:\n"+
            "\n"+
            "Number of objects found:                   1\n"+
            "Number of objects processed successfully:  0\n"+
            " Create:         0\n"+
            " Modify:         0\n"+
            " Delete:         0\n"+
            " No Operation:   0\n"+
            "Number of objects processed with errors:   1\n"+
            " Create:         1\n"+
            " Modify:         0\n"+
            " Delete:         0\n"+
            "\n"+
            "DETAILED EXPLANATION:\n"+
            "\n"+
            "\n"+
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"+
            "The following object(s) were found to have ERRORS:\n"+
            "\n"+
            "---\n"+
            "Create FAILED: [person] FP1-TEST   First Person\n"+
            "\n"+
            "person:         First Person\n"+
            "address:        St James Street\n"+
            "address:        Burnley\n"+
            "address:        UK\n"+
            "phone:          +44 282 420469\n"+
            "nic-hdl:        FP1-TEST\n"+
            "mnt-by:         OWNER-MNT\n"+
            "changed:        user@ripe.net\n"+
            "source:         TEST\n"+
            "\n"+
            "***Error:   Authorisation for [person] FP1-TEST failed\n"+
            "           using \"mnt-by:\"\n"+
            "           not authenticated by: OWNER-MNT\n"+
            "\n\n\n"+
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        final String response = getUpdates("FAILED: mnt-by: OWNER-MNT");

        assertThat(response, containsString("First Person"));
    }

    @Test
    public void get_update_logs_for_id() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getCurrentUpdateLogsForId(URLEncoder.encode(getLogDirFullPathName(), "ISO-8859-1"));

        assertThat(response, containsString("TEST-MNT"));
    }

    @Test
    public void get_update_logs_for_id_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getCurrentUpdateLogsForIdAndDate(getLogDirName(), getDate());

        assertThat(response, containsString("TEST-MNT"));
    }

    @Test
    public void get_update_ids_for_name_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getUpdateIds("TEST-MNT", getDate());

        assertThat(response, containsString("\"host\":"));
        assertThat(response, containsString("\"id\":"));
    }

    @Test
    public void get_remote_update_logs_for_host_and_id() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getRemoteUpdateLogs("UNDEFINED", URLEncoder.encode(getLogDirFullPathName(), "ISO-8859-1"));

        assertThat(response, isEmptyString());
    }

    @Test
    public void get_remote_update_logs_for_host_and_id_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getRemoteUpdateLogs("UNDEFINED", getLogDirName(), getDate());

        assertThat(response, isEmptyString());
    }

    @Test
    public void get_current_update_logs_for_name_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getCurrentUpdateLogs("TEST-MNT", getDate());

        assertThat(response, containsString("\"host\":"));
        assertThat(response, containsString("\"id\":"));
    }

    // API calls

    private String getUpdates(final String searchTerm) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&date=&apiKey=%s", getPort(Audience.INTERNAL), URLEncoder.encode(searchTerm, "ISO-8859-1"), apiKey))
                .get(String.class);
    }


    private String getUpdateIds(final String searchTerm, final String date) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs/ids?search=%s&date=%s&apiKey=%s", getPort(Audience.INTERNAL), URLEncoder.encode(searchTerm, "ISO-8859-1"), date, apiKey))
                .get(String.class);
    }

    private String getRemoteUpdateLogs(final String host, final String updateId) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs/%s/%s?apiKey=%s", getPort(Audience.INTERNAL), host, updateId, apiKey))
                .get(String.class);
    }

    private String getRemoteUpdateLogs(final String host, final String updateId, final String date) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs/%s/%s/%s?apiKey=%s", getPort(Audience.INTERNAL), host, date, updateId, apiKey))
                .get(String.class);
    }

    private String getCurrentUpdateLogs(final String searchTerm, final String date) {
        return client
                .resource(String.format("http://localhost:%s/api/logs/current?search=%s&date=%s&apiKey=%s", getPort(Audience.INTERNAL), searchTerm, date, apiKey))
                .get(String.class);
    }

    private String getCurrentUpdateLogsForId(final String updateId) {
        return client
                .resource(String.format("http://localhost:%s/api/logs/current/%s?apiKey=%s", getPort(Audience.INTERNAL), updateId, apiKey))
                .get(String.class);
    }

    private String getCurrentUpdateLogsForIdAndDate(final String updateId, final String date) {
        return client
                .resource(String.format("http://localhost:%s/api/logs/current/%s/%s?apiKey=%s", getPort(Audience.INTERNAL), date, updateId, apiKey))
                .get(String.class);
    }

    // helper methods

    private void createLogFile(final String data) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(logDir)
                .append('/')
                .append(getDate())
                .append('/')
                .append(getTime())
                .append('.')
                .append(Math.random());

        final File fullDir = new File(builder.toString());
        fullDir.mkdirs();

        logFile = new File(fullDir, INPUT_FILE_NAME);
        final FileOutputStream fileOutputStream = new FileOutputStream(logFile);

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream), Charsets.ISO_8859_1));
            writer.write(data);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        logFileIndex.update();
    }

    private String getLogDirName() throws IOException {
        final List<String> path = Lists.newArrayList(PATH_SPLITTER.split(logFile.getCanonicalPath()));
        return path.get(path.size() - 2);
    }

    private String getLogDirFullPathName() throws IOException {
        final List<String> path = Lists.newArrayList(PATH_SPLITTER.split(logFile.getCanonicalPath()));

        return String.format("/%s/%s",
                path.get(path.size() - 3),
                path.get(path.size() - 2));
    }

    private String getDate() {
        return DATE_FORMAT.print(DateTime.now());
    }

    private String getTime() {
        return TIME_FORMAT.print(DateTime.now());
    }
}
