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
public class GrsImporterApnicTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;
    @Autowired IpTreeUpdater ipTreeUpdater;

    @Autowired AuthoritativeResourceImportTask authoritativeResourceImportTask;
    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DailySchedulerDao dailySchedulerDao;
    @Autowired DateTimeProvider dateTimeProvider;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeClass
    public static void setup_database() throws IOException {
        DatabaseHelper.addGrsDatabases("APNIC-GRS");

        final File resourceFile = FileHelper.addToTextFileWithMd5Checksum(tempDirectory, "APNIC-GRS-RES.tmp",
                "apnic|*|asn|*|22831|summary\n" +
                "apnic|*|ipv4|*|53557|summary\n" +
                "apnic|*|ipv6|*|29780|summary\n" +
                "apnic|JP|ipv4|24.232.0.0|65536|20140414|allocated|35073\n" +
                "apnic|JP|ipv4|88.202.208.0|4096|20140414|allocated|35073\n"  + // franken-range part 1
                "apnic|JP|ipv4|88.202.224.0|4096|20140414|allocated|35073" );   // franken-range part 2

        final File dumpFile = FileHelper.addToGZipFile(
                tempDirectory,
                "APNIC-GRS-DMP.tmp",
                "\n" +
                        "mntner:         SOME-MNT\n" +
                        "descr:          description\n" +
                        "mnt-by:         SOME-MNT\n" +
                        "referral-by:    SOME-MNT\n" +
                        "upd-to:         dbtest@ripe.net\n" +
                        "auth:           MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "changed:        dbtest@ripe.net 20120707\n" +
                        "source:         APNIC\n" +
                        "\n" +
                        "\n" +
                        "inetnum:    24.232.0.0 - 24.232.255.255\n" +
                        "status:     allocated\n" +
                        "owner:      CABLEVISION S.A.\n" +
                        "city:       Munro\n" +
                        "country:    AR\n" +
                        "owner-c:    NEA\n" +
                        "tech-c:     NEA\n" +
                        "abuse-c:    NEA\n" +
                        "inetrev:    24.232/16\n" +
                        "nserver:    DNS1.CVTCI.COM.AR\n" +
                        "nserver:    DNS2.CVTCI.COM.AR\n" +
                        "created:    1997-06-02\n" +
                        "changed:    2003-05-19\n" +
                        "source:     APNIC\n" +
                        "\n" +
                        "\n" +
                        "inetnum:    88.202.208.0 - 88.202.239.255\n" +
                        "status:     allocated\n" +
                        "owner:      CABLEVISION S.A.\n" +
                        "city:       Munro\n" +
                        "country:    AR\n" +
                        "owner-c:    NEA\n" +
                        "tech-c:     NEA\n" +
                        "abuse-c:    NEA\n" +
                        "nserver:    DNS2.CVTCI.COM.AR\n" +
                        "created:    1997-06-02\n" +
                        "changed:    2003-05-19\n" +
                        "source:     APNIC\n" +
                        "\n" +
                        "\n"
        );

        System.setProperty("grs.import.apnic.source", "APNIC-GRS");
        System.setProperty("grs.import.apnic.resourceDataUrl", getUrl(resourceFile));
        System.setProperty("grs.import.apnic.download", getUrl(dumpFile));
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
    public void import_apnic_grs() throws Exception {
        awaitAll(grsImporter.grsImport("APNIC-GRS", false));
        ipTreeUpdater.rebuild();

        assertThat(query("-s APNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s APNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s APNIC-GRS 24.232.1.1"), containsString("status:         ALLOCATED"));
        assertThat(query("-s APNIC-GRS 88.202.208.0 - 88.202.239.255"), containsString("status:         ALLOCATED"));
        assertThat(query("-s APNIC-GRS 88.202.240.0"), containsString("No entries found"));
        assertThat(query("-s ARIN-GRS 88.202.224.0 - 88.202.239.255"),  containsString("unknown source"));
    }

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
