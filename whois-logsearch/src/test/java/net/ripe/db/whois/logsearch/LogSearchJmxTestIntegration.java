package net.ripe.db.whois.logsearch;

import com.google.common.io.Files;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.logsearch.bootstrap.LogSearchJettyBootstrap;
import net.ripe.db.whois.logsearch.jmx.LogFileUpdateJmx;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-logsearch-test.xml"})
public class LogSearchJmxTestIntegration extends AbstractJUnit4SpringContextTests {

    @Autowired
    private LogSearchJettyBootstrap logSearchJettyBootstrap;
    @Autowired
    private NewLogFormatProcessor newLogFormatProcessor;
    @Autowired
    private LogFileIndex logFileIndex;
    @Autowired
    private LogFileUpdateJmx logFileUpdateJmx;

    private static File indexDirectory = Files.createTempDir();
    private static File logDirectory = Files.createTempDir();

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
    }

    @After
    public void cleanup() {
        LogFileHelper.deleteLogs(logDirectory);
        logFileIndex.removeAll();
        logSearchJettyBootstrap.stop(true);
    }

    @Test
    public void search_by_updateId() throws Exception {
        addToIndex(LogFileHelper.createTarredLogFile(logDirectory, "20130102", "100102", "random", "mntner: UPD-MNT"));

        final String results = logFileUpdateJmx.searchByUpdateId(".*");

        assertThat(results, containsString("Found 1 updates matching regex .*\n"));
        assertThat(results, containsString("001.msg-in.txt.gz"));
    }

    // helper methods

    private void addToIndex(final File file) throws IOException {
        if (file.isDirectory()) {
            newLogFormatProcessor.update();
        } else {
            if (file.getAbsolutePath().endsWith(".tar")) {
                newLogFormatProcessor.addFileToIndex(file.getAbsolutePath());
            } else {
                newLogFormatProcessor.update();
            }
        }
    }


}
