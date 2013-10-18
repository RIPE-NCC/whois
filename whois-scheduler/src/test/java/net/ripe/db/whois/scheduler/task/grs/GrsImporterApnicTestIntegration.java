package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.io.Files;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class GrsImporterApnicTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeClass
    public static void setup_database() throws IOException {
        DatabaseHelper.addGrsDatabases("APNIC-GRS");

        final File resourceFile = FileHelper.addToTextFile(tempDirectory, "APNIC-GRS-RES.tmp", "apnic|*|asn|*|22831|summary\n" +
                "apnic|*|ipv4|*|53557|summary\n" +
                "apnic|*|ipv6|*|29780|summary\n");

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
                "source:         APNIC\n\n\n");

        System.setProperty("grs.import.apnic.source", "APNIC-GRS");
        System.setProperty("grs.import.apnic.resourceDataUrl", getUrl(resourceFile));
        System.setProperty("grs.import.apnic.download", getUrl(dumpFile));
        System.setProperty("dir.grs.import.download", getPath(tempDirectory));
    }

    @AfterClass
    public static void cleanup() throws Exception {
        System.clearProperty("grs.import.apnic.source");
        System.clearProperty("grs.import.apnic.resourceDataUrl");
        System.clearProperty("grs.import.apnic.download");
        System.clearProperty("dir.grs.import.download");

        FileHelper.delete(tempDirectory);
    }

    @Before
    public void setUp() throws Exception {
        grsImporter.setGrsImportEnabled(true);
        queryServer.start();
    }

    @Test
    public void import_apnic_grs() throws Exception {
        awaitAll(grsImporter.grsImport("APNIC-GRS", false));

        assertThat(query("-s APNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s APNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
    }

    private void awaitAll(final List<Future<?>> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private String query(final String query) throws Exception {
        return DummyWhoisClient.query(QueryServer.port, query);
    }

    private static String getUrl(final File file) throws MalformedURLException {
        return file.toURI().toURL().toString();
    }

    private static String getPath(final File file) {
        return file.getAbsolutePath();
    }
}
