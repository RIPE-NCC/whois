package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.io.Files;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("IntegrationTest")
@DirtiesContext
public class GrsImporterJpirrTestIntegration extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;
    @Autowired IpTreeUpdater ipTreeUpdater;

    @Autowired AuthoritativeResourceImportTask authoritativeResourceImportTask;
    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DateTimeProvider dateTimeProvider;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeAll
    public static void setup_database() throws IOException {
        DatabaseHelper.addGrsDatabases("JPIRR-GRS");

        final File dumpFile = FileHelper.addToGZipFile(
                tempDirectory,
                "JPIRR-GRS-DMP.tmp",
                "\n" +
                "route:      192.168.0.0/16\n" +
                "descr:      Some Route\n" +
                "origin:     AS123\n" +
                "mnt-by:     SOME-MNT\n" +
                "changed:    noreply@ripe.net 20010203\n" +
                "source:     JPIRR\n" +
                "\n" +
                "mntner:         SOME-MNT\n" +
                "descr:          description\n" +
                "mnt-by:         SOME-MNT\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:        dbtest@ripe.net 20120707\n" +
                "source:         JPIRR\n" +
                "\n"
        );

        System.setProperty("grs.import.jpirr.source", "JPIRR-GRS");
        System.setProperty("grs.import.jpirr.download", getUrl(dumpFile));
        System.setProperty("dir.grs.import.download", getPath(tempDirectory));
    }

    @AfterAll
    public static void cleanup() throws Exception {
        FileHelper.delete(tempDirectory);
    }

    @BeforeEach
    public void setUp() throws Exception {
        // initialize authoritativeresource
        authoritativeResourceImportTask.run();
        authoritativeResourceData.refreshGrsSources();

        grsImporter.setGrsImportEnabled(true);
        queryServer.start();
    }

    @Test
    public void import_jpirr_grs() throws Exception {
        awaitAll(grsImporter.grsImport("JPIRR-GRS", false));
        ipTreeUpdater.rebuild();

        assertThat(query("-s JPIRR-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s JPIRR-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s JPIRR-GRS 192.168.0.0/16"), containsString("route:          192.168.0.0/16"));
        assertThat(query("-s JPIRR-GRS 192.168.0.0/16"), not(containsString("changed:")));
    }

    private void awaitAll(final List<Future> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private String query(final String query) throws Exception {
        return TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);
    }

    private static String getUrl(final File file) throws MalformedURLException {
        return file.toURI().toURL().toString();
    }

    private static String getPath(final File file) {
        return file.getAbsolutePath();
    }
}

