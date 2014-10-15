package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.io.Files;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
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
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext
public class GrsImporterAfrinicTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;
    @Autowired IpTreeUpdater ipTreeUpdater;

    @Autowired AuthoritativeResourceImportTask authoritativeResourceImportTask;
    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DailySchedulerDao dailySchedulerDao;
    @Autowired DateTimeProvider dateTimeProvider;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeClass
    public static void setup_database() throws IOException {
        DatabaseHelper.addGrsDatabases("AFRINIC-GRS");

        final File resourceFile = FileHelper.addToTextFileWithMd5Checksum(tempDirectory, "AFRINIC-GRS-RES.tmp",
                "afrinic|*|asn|*|2303|summary\n" +
                "afrinic|*|ipv4|*|2792|summary\n" +
                "afrinic|*|ipv6|*|1798|summary\n" +
                "afrinic|ZA|ipv4|66.18.0.0|65536|20140414|allocated|35073\n" +
                "afrinic|ZA|ipv4|88.202.208.0|4096|20140414|allocated|35073\n"  + // franken-range part 1
                "afrinic|ZA|ipv4|88.202.224.0|4096|20140414|allocated|35073\n" );   // franken-range part 2

        final File dumpFile = FileHelper.addToGZipFile(
                tempDirectory,
                "AFRINIC-GRS-DMP.tmp",
                "#\n" +
                "# The contents of this file are subject to\n" +
                "# AFRINIC Database Terms and Conditions\n" +
                "#\n" +
                "# http://www.afrinic.net/en/services\n" +
                "#\n" +
                "\n" +
                "mntner:         AFRINIC-HM-MNT\n" +
                "descr:          AfriNIC RS\n" +
                "admin-c:        AA1-AFRINIC\n" +
                "tech-c:         EB1-AFRINIC\n" +
                "upd-to:         ***@afrinic.net\n" +
                "auth:           MD5-PW # Filtered\n" +
                "auth:           PGPKEY-1124A1E5\n" +
                "mnt-by:         AFRINIC-HM-MNT\n" +
                "changed:        ***@afrinic.net 20050101\n" +
                "source:         AFRINIC\n" +
                "\n" +
                "inetnum:        66.18.64.0 - 66.18.95.255\n" +
                "netname:        SENTECH-ZA\n" +
                "descr:          Sentech Ltd\n" +
                "country:        ZA\n" +
                "admin-c:        NM9-AFRINIC\n" +
                "tech-c:         NM9-AFRINIC\n" +
                "status:         ALLOCATED PA\n" +
                "mnt-by:         AFRINIC-HM-MNT\n" +
                "changed:        ***@afrinic.net 20130704\n" +
                "source:         AFRINIC\n" +
                "\n" +
                "inetnum:        88.202.208.0 - 88.202.239.255\n" +
                "netname:        SENTECH-ZA\n" +
                "descr:          Sentech Ltd\n" +
                "country:        ZA\n" +
                "admin-c:        NM9-AFRINIC\n" +
                "tech-c:         NM9-AFRINIC\n" +
                "status:         ALLOCATED PA\n" +
                "mnt-by:         AFRINIC-HM-MNT\n" +
                "changed:        ***@afrinic.net 20130704\n" +
                "source:         AFRINIC\n" +
                "\n"
        );

        System.setProperty("grs.import.afrinic.source", "AFRINIC-GRS");
        System.setProperty("grs.import.afrinic.resourceDataUrl", getUrl(resourceFile));
        System.setProperty("grs.import.afrinic.download", getUrl(dumpFile));
        System.setProperty("dir.grs.import.download", getPath(tempDirectory));
    }

    @AfterClass
    public static void cleanup() throws Exception {
        FileHelper.delete(tempDirectory);
    }

    @Before
    public void setUp() throws Exception {
        // initialize authoritativeresource
        dailySchedulerDao.acquireDailyTask(dateTimeProvider.getCurrentDate(), AuthoritativeResourceImportTask.class, "localhost");
        authoritativeResourceImportTask.run();
        dailySchedulerDao.markTaskDone(System.currentTimeMillis(), dateTimeProvider.getCurrentDate(), AuthoritativeResourceImportTask.class);
        authoritativeResourceData.refreshAuthoritativeResourceCache();

        grsImporter.setGrsImportEnabled(true);
        queryServer.start();
    }

    @Test
    public void import_afrinic_grs() throws Exception {
        awaitAll(grsImporter.grsImport("AFRINIC-GRS", false));
        ipTreeUpdater.rebuild();

        assertThat(query("-s AFRINIC-GRS AFRINIC-HM-MNT"), containsString("mntner:         AFRINIC-HM-MNT"));
        assertThat(query("-s AFRINIC-GRS -i mnt-by AFRINIC-HM-MNT"), containsString("mntner:         AFRINIC-HM-MNT"));
        assertThat(query("-s AFRINIC-GRS 66.18.64.0"), containsString("status:         ALLOCATED PA"));
        assertThat(query("--resource 66.18.64.0"), containsString("status:         ALLOCATED PA"));
        assertThat(query("-s AFRINIC-GRS 88.202.240.0"), containsString("No entries found"));
        assertThat(query("-s ARIN-GRS 88.202.224.0 - 88.202.239.255"),  containsString("unknown source"));
    }

    // helper methods

    private void awaitAll(final List<Future> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private String query(final String query) throws Exception {
        return TelnetWhoisClient.queryLocalhost(QueryServer.port, query);
    }

    private static String getUrl(final File file) throws MalformedURLException {
        return file.toURI().toURL().toString();
    }

    private static String getPath(final File file) {
        return file.getAbsolutePath();
    }
}
