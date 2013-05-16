package net.ripe.db.whois.api.wsearch;

import com.google.common.base.Charsets;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Category(IntegrationTest.class)
public class WSearchTestIntegration extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSearchTestIntegration.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");
    private static final String INPUT_FILE_NAME = "001.msg-in.txt.gz";

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
        logFile.delete();
    }

    @Test
    public void single_term() throws Exception {
        LOGGER.info("log directory = {}", logDir);

        createLogFile("the quick brown fox");

        assertThat(wsearch("quick"), containsString("the quick brown fox"));
    }

    @Test
    public void multiple_inetnum_terms() throws Exception {
        createLogFile("inetnum: 192.0.0.0 - 193.0.0.0");

        final String response = wsearch("192.0.0.0 - 193.0.0.0");

        assertThat(response, containsString("192.0.0.0 - 193.0.0.0"));
    }

    @Test
    public void curly_brace_in_search_term() throws Exception {
        createLogFile("mnt-routes: ROUTES-MNT {2001::/48}");

        final String response = wsearch("{2001::/48}");

        assertThat(response, containsString("ROUTES-MNT {2001::/48}"));
    }

    private String wsearch(final String searchTerm) throws IOException {
        return client
                .resource(String.format("http://localhost:%s/api/logs?search=%s&date=&apiKey=%s", getPort(Audience.INTERNAL), URLEncoder.encode(searchTerm, "ISO-8859-1"), apiKey))
                .get(String.class);
    }

    private void createLogFile(final String data) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(logDir)
                .append('/')
                .append(DATE_FORMAT.print(DateTime.now()))
                .append('/')
                .append(TIME_FORMAT.print(DateTime.now()))
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
}
