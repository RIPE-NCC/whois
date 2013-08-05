package net.ripe.db.whois.wsearch;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.common.IntegrationTest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-wsearch-test.xml"})
public class WSearchTestIntegration extends AbstractJUnit4SpringContextTests {
    @Autowired
    private WSearchJettyBootstrap wSearchJettyBootstrap;
    @Autowired
    private WSearchJettyConfig wSearchJettyConfig;

    @Autowired
    private LogFileIndex logFileIndex;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");
    private static final String INPUT_FILE_NAME = "001.msg-in.txt.gz";

    private Client client;
    private File logFile;


    @Value("${dir.update.audit.log}")
    private String logDir;

    @Value("${api.key}")
    private String apiKey;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("dir.wsearch.index", "var1");
    }

    @Before
    public void setup() {
        wSearchJettyBootstrap.start();
        client = Client.create(new DefaultClientConfig());
    }

    @After
    public void removeLogfile() {
        if (logFile != null) {
            logFile.delete();
        }
        wSearchJettyBootstrap.stop(true);
        try {
            logFileIndex.lockedRebuild();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void single_term() throws Exception {
        createLogFile("the quick brown fox");

        assertThat(getUpdates("quick"), containsString("the quick brown fox"));
    }

    @Ignore("TODO: [ES] fix")
    @Test
    public void single_term_inetnum_with_prefix_length() throws Exception {
        createLogFile("inetnum: 10.0.0.0/24");

        assertThat(getUpdates("10.0.0.0\\/24"), containsString("inetnum: 10.0.0.0/24"));
    }

    @Test
    public void multiple_inetnum_terms() throws Exception {
        createLogFile("inetnum: 192.0.0.0 - 193.0.0.0");

        final String response = getUpdates("192.0.0.0 - 193.0.0.0");

        assertThat(response, containsString("192.0.0.0 - 193.0.0.0"));
    }

    //@Ignore("TODO: [ES] fix tokenizer, query string shouldn't match")
    @Test
    public void single_inet6num_term() throws Exception {
        createLogFile("inet6num: 2001:a08:cafe::/48");

        assertThat(getUpdates("2001:cafe"), not(containsString("2001:a08:cafe::/48")));
    }

    @Ignore("TODO: [ES] fix")
    @Test
    public void curly_brace_in_search_term() throws Exception {
        createLogFile("mnt-routes: ROUTES-MNT {2001::/48}");

        final String response = getUpdates("{2001::/48}");

        assertThat(response, containsString("ROUTES-MNT {2001::/48}"));
    }

    @Test
    public void search_multiple_terms_in_failed_update() throws Exception {
        createLogFile(
                "SUMMARY OF UPDATE:\n" +
                        "\n" +
                        "Number of objects found:                   1\n" +
                        "Number of objects processed successfully:  0\n" +
                        " Create:         0\n" +
                        " Modify:         0\n" +
                        " Delete:         0\n" +
                        " No Operation:   0\n" +
                        "Number of objects processed with errors:   1\n" +
                        " Create:         1\n" +
                        " Modify:         0\n" +
                        " Delete:         0\n" +
                        "\n" +
                        "DETAILED EXPLANATION:\n" +
                        "\n" +
                        "\n" +
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                        "The following object(s) were found to have ERRORS:\n" +
                        "\n" +
                        "---\n" +
                        "Create FAILED: [person] FP1-TEST   First Person\n" +
                        "\n" +
                        "person:         First Person\n" +
                        "address:        St James Street\n" +
                        "address:        Burnley\n" +
                        "address:        UK\n" +
                        "phone:          +44 282 420469\n" +
                        "nic-hdl:        FP1-TEST\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "changed:        user@ripe.net\n" +
                        "source:         TEST\n" +
                        "\n" +
                        "***Error:   Authorisation for [person] FP1-TEST failed\n" +
                        "           using \"mnt-by:\"\n" +
                        "           not authenticated by: OWNER-MNT\n" +
                        "\n\n\n" +
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        final String response = getUpdates("FAILED: mnt-by: OWNER-MNT");

        assertThat(response, containsString("First Person"));
    }

    @Test
    public void search_date_range() throws IOException, InterruptedException {
        createLogFileAtDate("mntner: TEST-MNT", "20130505");
        createLogFileAtDate("mntner: UPD-MNT", "20130507");
        createLogFileAtDate("mntner: OTHER-MNT", "20130508");

        String response = client
                .resource(String.format(
                        "http://localhost:%s/api/logs?search=%s&fromdate=20130506&todate=20130509&apiKey=%s",
                        wSearchJettyConfig.getPort(),
                        URLEncoder.encode("mntner", "ISO-8859-1"),
                        apiKey))
                .get(String.class);

        assertThat(response, containsString("UPD-MNT"));
        assertThat(response, containsString("OTHER-MNT"));
        assertThat(response, not(containsString("TEST-MNT")));
    }

    @Test
    public void search_date_range_dates_turnedaround() throws IOException {
        createLogFileAtDate("mntner: TEST-MNT", "20130505");
        createLogFileAtDate("mntner: UPD-MNT", "20130507");
        createLogFileAtDate("mntner: OTHER-MNT", "20130508");

        String response = client
                .resource(String.format(
                        "http://localhost:%s/api/logs?search=%s&fromdate=20130509&todate=20130506&apiKey=%s",
                        wSearchJettyConfig.getPort(),
                        URLEncoder.encode("mntner", "ISO-8859-1"),
                        apiKey))
                .get(String.class);

        assertThat(response, isEmptyOrNullString());
    }

    @Test
    public void search_on_date_that_does_not_exist() throws IOException {
        createLogFileAtDate("mntner: UPD-MNT", "20130507");
        createLogFileAtDate("mntner: OTHER-MNT", "20130508");

        String response = client
                .resource(String.format(
                        "http://localhost:%s/api/logs?search=%s&fromdate=20130506&todate=&apiKey=%s",
                        wSearchJettyConfig.getPort(),
                        URLEncoder.encode("mntner", "ISO-8859-1"),
                        apiKey))
                .get(String.class);
        assertThat(response, isEmptyOrNullString());
    }

    @Test
    public void get_update_ids_for_name_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getUpdateIds("TEST-MNT", getDate());

        assertThat(response, containsString("\"host\":"));
        assertThat(response, containsString("\"id\":"));
    }

    @Test
    public void get_current_update_logs_for_name_and_date() throws Exception {
        createLogFile("mntner: TEST-MNT");

        final String response = getUpdates("TEST-MNT", getDate());

        assertThat(response, containsString("mntner: TEST-MNT"));
    }

    @Test
    public void search_from_inetnum() throws IOException {
        createLogFile("REQUEST FROM:193.0.1.204\nPARAMS:");

        final String response = getUpdates("193.0.1.204", getDate());

        assertThat(response, containsString("REQUEST FROM:193.0.1.204"));
    }

    @Ignore("TODO")
    @Test
    public void search_from_inet6num() throws IOException {
        createLogFile("REQUEST FROM:2000:3000:4000::/48\nPARAMS:");

        final String response = getUpdates("2000:3000:4000::/48", getDate());

        assertThat(response, containsString("\"host\":"));
        assertThat(response, containsString("\"id\":"));
    }

    @Test
    public void search_wrong_api_key() throws IOException {
        createLogFileAtDate("mntner: OTHER-MNT", "20130508");

        try {
            client.resource(String.format(
                    "http://localhost:%s/api/logs?search=%s&fromdate=20130508&todate=&apiKey=WRONG",
                    wSearchJettyConfig.getPort(),
                    URLEncoder.encode("mntner", "ISO-8859-1")))
                    .get(String.class);
        } catch (final UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(403));
        }
    }

    @Test
    //TODO [AS] seems there's a mapping for <updates> missing...
    public void search_ids_xml() throws IOException {
        createLogFileAtDate("mntner: OTHER-MNT", "20130707");

        final String updates = client
                .resource(String.format("http://localhost:%s/api/logs/ids?search=%s&fromdate=20130707&todate=&apiKey=%s",
                        wSearchJettyConfig.getPort(),
                        URLEncoder.encode("OTHER-MNT", "ISO-8859-1"),
                        apiKey))
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(updates, containsString("<updates><update><host>UNDEFINED</host><id>20130707/"));
    }

    @Test
    public void search_ids_json() throws Exception {
        createLogFileAtDate("MIGHTY FINE LOGFILE", "20130808");
        final String updates = client
                .resource(String.format("http://localhost:%s/api/logs/ids?search=%s&fromdate=20130808&todate=&apiKey=%s",
                        wSearchJettyConfig.getPort(),
                        URLEncoder.encode("LOGFILE", "ISO-8859-1"),
                        apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(updates, containsString("{\"update\":{\"host\":\"UNDEFINED\",\"id\":\"20130808"));
    }

    @Test
    public void index_does_not_add_entry_twice() throws Exception {
        final double random = Math.random();
        createLogFileAtDateTime("Once upon a midnight dreary", "20131010", "222222", random);

        createLogFileAtDateTime("Once upon a midnight dreary", "20131010", "222222", random);

        final String result = client.resource(String.format(
                "http://localhost:%s/api/logs?search=%s&fromdate=&todate=&apiKey=%s",
                wSearchJettyConfig.getPort(),
                URLEncoder.encode("midnight", "ISO-8859-1"),
                apiKey))
                .get(String.class);

        final String folder = "# folder   : 222222." + random;
        final int lastSeen = result.indexOf(folder);
        assertThat(result.indexOf(folder, lastSeen + folder.length()), is(-1));
    }

    // API calls

    private String getUpdates(final String searchTerm) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&fromdate=&todate=&apiKey=%s", wSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), apiKey))
                .get(String.class);
    }

    private String getUpdates(final String searchTerm, final String date) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&date=%s&apiKey=%s", wSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), date, apiKey))
                .get(String.class);
    }

    private String getUpdateIds(final String searchTerm, final String date) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs/ids?search=%s&fromdate=%s&todate=&apiKey=%s", wSearchJettyConfig.getPort(), URLEncoder.encode(searchTerm, "ISO-8859-1"), date, apiKey))
                .get(String.class);
    }

    // helper methods

    private void createLogFileAtDateTime(final String data, final String date, final String time, final double random) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(logDir)
                .append("/whois2/audit/")
                .append(date)
                .append('/')
                .append(time)
                .append('.')
                .append(random);

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

    private void createLogFileAtDate(final String data, final String date) throws IOException {
        createLogFileAtDateTime(data, date, getTime(), Math.random());
    }

    private void createLogFile(final String data) throws IOException {
        createLogFileAtDate(data, getDate());
    }

    private String getDate() {
        return DATE_FORMAT.print(DateTime.now());
    }

    private String getTime() {
        return TIME_FORMAT.print(DateTime.now());
    }
}
