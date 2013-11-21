package net.ripe.db.whois.logsearch;

import com.google.common.io.Files;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.RestClientUtils;
import net.ripe.db.whois.internal.AbstractInternalTest;
import net.ripe.db.whois.internal.logsearch.LogFileIndex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

public abstract class AbstractLogSearchTest extends AbstractInternalTest {
    @Autowired
    protected LogFileIndex logFileIndex;

    protected static File indexDirectory = Files.createTempDir();
    protected static File logDirectory = Files.createTempDir();

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
        databaseHelper.insertApiKey(apiKey, "/api/logs", "abuse-c automagic creation");
        LogFileHelper.createLogDirectory(logDirectory);
    }

    @After
    public void cleanup() {
        LogFileHelper.deleteLogs(logDirectory);
        logFileIndex.removeAll();
    }

    // API calls
    protected String getUpdates(final String searchTerm) throws IOException {
        return RestTest.target(getPort(), "api/logs", String.format("search=%s", RestClientUtils.encode(searchTerm)), apiKey)
                .request()
                .get(String.class);
    }

    protected String getUpdates(final String searchTerm, final String date) throws IOException {
        return RestTest.target(getPort(), "api/logs", String.format("search=%s&fromdate=%s", RestClientUtils.encode(searchTerm), date), apiKey)
                .request()
                .get(String.class);
    }

    protected String getUpdates(final String searchTerm, final String fromDate, final String toDate) throws IOException {
        return RestTest.target(getPort(), "api/logs", String.format("search=%s&fromdate=%s&todate=%s", RestClientUtils.encode(searchTerm), fromDate, toDate), apiKey)
                .request()
                .get(String.class);
    }
}
