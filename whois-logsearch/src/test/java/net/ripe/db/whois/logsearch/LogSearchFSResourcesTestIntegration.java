package net.ripe.db.whois.logsearch;

import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.logsearch.bootstrap.LogSearchJettyBootstrap;
import net.ripe.db.whois.logsearch.logformat.LegacyLogEntry;
import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;
import org.joda.time.LocalDate;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static net.ripe.db.whois.logsearch.LogFileHelper.getAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:applicationContext-logsearch-test.xml"})
public class LogSearchFSResourcesTestIntegration extends AbstractJUnit4SpringContextTests {
    @Autowired
    private LogSearchJettyBootstrap logSearchJettyBootstrap;
    @Autowired
    private NewLogFormatProcessor newLogFormatProcessor;
    @Autowired
    private LegacyLogFormatProcessor legacyLogFormatProcessor;
    @Autowired
    private LogFileIndex logFileIndex;

    private static File indexDirectory = Files.createTempDir();

    @Value("${api.key}")
    private String apiKey;

    @BeforeClass
    public static void setupClass() throws IOException {
        System.setProperty("dir.logsearch.index", indexDirectory.getAbsolutePath());
    }

    @AfterClass
    public static void cleanupClass() {
        System.clearProperty("dir.logsearch.index");
    }

    @Before
    public void setup() {
        logSearchJettyBootstrap.start();
    }

    @After
    public void cleanup() {
        logFileIndex.removeAll();
        logSearchJettyBootstrap.stop(true);
    }

    @Test
    public void attemptAddingDuplicatesToLegacyIndex() throws IOException {
        final String path = getAbsolutePath("/log/integration");

        legacyLogFormatProcessor.addDirectoryToIndex(path);
        legacyLogFormatProcessor.addDirectoryToIndex(path);

        final Set<LoggedUpdate> result = logFileIndex.searchByDateRangeAndContent("lorem", null, null);

        Assert.assertThat(result, hasSize(1));
        final LegacyLogEntry legacyLogEntry = (LegacyLogEntry) Iterables.get(result, 0);

        Assert.assertThat(legacyLogEntry.getDate(), is("20120404"));
        Assert.assertThat(legacyLogEntry.getType(), is(LoggedUpdate.Type.LEGACY));
        Assert.assertThat(legacyLogEntry.getUpdateId(), is(path + "/acklog.20120404.bz2/0"));
    }

    @Test
    public void addToLegacyIndexByPath_directory() throws IOException {
        final String path = getAbsolutePath("/log/legacy");

        legacyLogFormatProcessor.addDirectoryToIndex(path);

        final Set<LoggedUpdate> result = logFileIndex.searchByDateRangeAndContent("15.229.64.0", null, null);
        final LegacyLogEntry legacyLogEntry = (LegacyLogEntry) Iterables.get(result, 0);

        assertThat(legacyLogEntry.getDate(), is("20110506"));
        assertThat(legacyLogEntry.getType(), is(LoggedUpdate.Type.LEGACY));
        assertThat(legacyLogEntry.getUpdateId(), is(path + "/acklog.20110506.bz2/1"));
    }

    @Test
    public void addToIndexByPath_file() throws IOException {
        final String path = getAbsolutePath("/log/legacy/updlog.20030726.bz2");
        legacyLogFormatProcessor.addFileToIndex(path);

        final Set<LoggedUpdate> result = logFileIndex.searchByDateRangeAndContent("0.0.193.in-addr.arpa", null, null);
        final LegacyLogEntry legacyLogEntry = (LegacyLogEntry) Iterables.get(result, 0);

        assertThat(legacyLogEntry.getDate(), is("20030726"));
        assertThat(legacyLogEntry.getType(), is(LoggedUpdate.Type.LEGACY));
        assertThat(legacyLogEntry.getUpdateId(), is(path + "/2"));
    }

    @Test
    public void addToIndex_newlog() throws IOException {
        final String path = getAbsolutePath("/log/update/20130305.tar");

        newLogFormatProcessor.addFileToIndex(path);

        final Set<LoggedUpdate> loggedUpdates = logFileIndex.searchByDateRangeAndContent("OWNER-MNT", null, null);

        assertThat(loggedUpdates, hasSize(1));
        final LoggedUpdate loggedUpdate = Iterables.get(loggedUpdates, 0);
        assertThat(loggedUpdate.getUpdateId(), is(path + "/140319.syncupdate_127.0.0.1_1362488599134839000/001.msg-in.txt.gz"));
        assertThat(loggedUpdate.getDate(), is("20130305"));
    }

    @Test
    public void addDirectoryToIndex_newlog() throws IOException {
        final String path = getAbsolutePath("/log/update");

        newLogFormatProcessor.addDirectoryToIndex(path);

        final Set<LoggedUpdate> result = logFileIndex.searchByDateRangeAndContent("mnt-by:", null, null);

        assertThat(result, hasSize(11));
    }

    @Test
    public void directoryIndexing_newlog_per_day() throws IOException {
        final String path = getAbsolutePath("/log/update");

        newLogFormatProcessor.addDirectoryToIndex(path);

        final Set<LoggedUpdate> untarred = logFileIndex.searchByDateRangeAndContent("mnt-by:", new LocalDate(2013, 3, 6), null);
        assertThat(untarred, hasSize(2));

        final Set<LoggedUpdate> tarred1 = logFileIndex.searchByDateRangeAndContent("mnt-by:", new LocalDate(2012, 8, 16), null);
        assertThat(tarred1, hasSize(7));

        final Set<LoggedUpdate> tarred2 = logFileIndex.searchByDateRangeAndContent("mnt-by:", new LocalDate(2013, 3, 5), null);
        assertThat(tarred2, hasSize(2));
    }
}
