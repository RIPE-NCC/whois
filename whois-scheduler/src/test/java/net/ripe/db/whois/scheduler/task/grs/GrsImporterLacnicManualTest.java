package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.io.Files;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.ManualTest;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(ManualTest.class)
@DirtiesContext
public class GrsImporterLacnicManualTest extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;
    @Autowired GrsSourceImporter grsSourceImporter;
    @Autowired LacnicGrsSource lacnicGrsSource;

    @Autowired AuthoritativeResourceImportTask authoritativeResourceImportTask;
    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DailySchedulerDao dailySchedulerDao;
    @Autowired DateTimeProvider dateTimeProvider;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeClass
    public static void setup_database() throws IOException {
        DatabaseHelper.addGrsDatabases("LACNIC-GRS");

        System.setProperty("grs.import.lacnic.source", "LACNIC-GRS");
        System.setProperty("grs.import.lacnic.resourceDataUrl", "ftp://ftp.lacnic.net/pub/stats/lacnic/delegated-lacnic-extended-latest");
        System.setProperty("grs.import.lacnic.userId", "XXX");
        System.setProperty("grs.import.lacnic.password", "XXX");

        System.setProperty("dir.grs.import.download", tempDirectory.getAbsolutePath());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        FileHelper.delete(tempDirectory);
    }

    @Before
    public void setUp() throws Exception {
        dailySchedulerDao.acquireDailyTask(dateTimeProvider.getCurrentDate(), AuthoritativeResourceImportTask.class, "localhost");
        authoritativeResourceImportTask.run();
        dailySchedulerDao.markTaskDone(System.currentTimeMillis(), dateTimeProvider.getCurrentDate(), AuthoritativeResourceImportTask.class);
        authoritativeResourceData.refreshAuthoritativeResourceCache();
        grsImporter.setGrsImportEnabled(true);
        queryServer.start();
    }

    @Test
    public void import_lacnic_grs_download_dump() throws Exception {
        awaitAll(grsImporter.grsImport("LACNIC-GRS", false));

        assertThat(query("-s LACNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s LACNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
    }

    @Test
    public void import_lacnic_grs_use_downloaded_dump() throws Exception {
        grsSourceImporter.grsImport(lacnicGrsSource, false);

        assertThat(query("-s LACNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s LACNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
    }

    private void awaitAll(final List<Future> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private String query(final String query) throws Exception {
        return DummyWhoisClient.query(QueryServer.port, query);
    }
}
