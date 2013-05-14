package net.ripe.db.whois.api.wsearch;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Category(IntegrationTest.class)
public class WSearchTestIntegration extends AbstractIntegrationTest {
    private static final File INDEX_DIR = Files.createTempDir();
    private static final File LOG_DIR = Files.createTempDir();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmmss");
    private static final String INPUT_FILE_NAME = "001.msg-in.txt.gz";

    @Autowired
    private LogFileIndex logFileIndex;

    @BeforeClass
    public static void setup() throws IOException {
        System.setProperty("api.key", "DB-RIPE-ZwBAFuR5JuBxQCnQ");
        System.setProperty("dir.wsearch.index", INDEX_DIR.getAbsolutePath() + "index");
        System.setProperty("dir.update.audit.log", LOG_DIR.getAbsolutePath());
    }

    @AfterClass
    public static void teardown() throws Exception {
        INDEX_DIR.delete();
        LOG_DIR.delete();
    }

    @Test
    public void single_term() throws Exception {
        createLogFile("the quick brown fox");

        assertThat(doWSearch("quick"), containsString("the quick brown fox"));
    }

    @Test
    public void multiple_inetnum_terms() throws Exception {
        createLogFile("inetnum: 192.0.0.0 - 193.0.0.0");

        final String wsearch = doWSearch("192.0.0.0 - 193.0.0.0");
        assertThat(wsearch, containsString("192.0.0.0 - 193.0.0.0"));
    }

    private String doWSearch(final String searchTerm) throws IOException {
        return doGetRequest(String.format("http://localhost:%s/api/logs?search=%s&date=&apiKey=DB-RIPE-ZwBAFuR5JuBxQCnQ", getPort(Audience.INTERNAL), URLEncoder.encode(searchTerm, "ISO-8859-1")), 200);
    }

    private void createLogFile(final String data) throws IOException {

        final StringBuilder builder = new StringBuilder();
        builder.append(LOG_DIR.getAbsolutePath())
                .append('/')
                .append(DATE_FORMAT.print(DateTime.now()))
                .append('/')
                .append(TIME_FORMAT.print(DateTime.now()))
                .append('.')
                .append(Math.random());


        final File fullDir = new File(builder.toString());
        fullDir.mkdirs();

        final File file = new File(fullDir, INPUT_FILE_NAME);
        final FileOutputStream fileOutputStream = new FileOutputStream(file);

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
