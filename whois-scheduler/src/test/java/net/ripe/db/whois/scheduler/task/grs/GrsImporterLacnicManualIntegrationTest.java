package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.io.Files;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask;
import net.ripe.db.whois.common.support.FileHelper;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Tag("ManualTest")
@Disabled("Replace username and password before running manually")
@DirtiesContext
public class GrsImporterLacnicManualIntegrationTest extends AbstractSchedulerIntegrationTest {

    @Autowired GrsImporter grsImporter;
    @Autowired GrsSourceImporter grsSourceImporter;
    @Autowired LacnicGrsSource lacnicGrsSource;

    @Autowired AuthoritativeResourceImportTask authoritativeResourceImportTask;
    @Autowired AuthoritativeResourceData authoritativeResourceData;
    @Autowired DateTimeProvider dateTimeProvider;

    private static final File tempDirectory = Files.createTempDir();

    @BeforeAll
    public static void setup_database() {
        DatabaseHelper.addGrsDatabases("LACNIC-GRS");

        System.setProperty("grs.import.lacnic.source", "LACNIC-GRS");
        System.setProperty("grs.import.lacnic.resourceDataUrl", "https://ftp.lacnic.net/pub/stats/lacnic/delegated-lacnic-extended-latest");
        System.setProperty("grs.import.lacnic.userId", "XXX");
        System.setProperty("grs.import.lacnic.password", "XXX");

        System.setProperty("dir.grs.import.download", tempDirectory.getAbsolutePath());
    }

    @AfterAll
    public static void cleanup() {
        FileHelper.delete(tempDirectory);
    }

    @BeforeEach
    public void setUp() throws Exception {
        authoritativeResourceImportTask.run();
        authoritativeResourceData.refreshGrsSources();

        queryServer.start();
    }

    @Test
    public void  import_lacnic_grs_download_dump() throws Exception {
        awaitAll(grsImporter.grsImport("LACNIC-GRS", false));

        assertThat(query("-s LACNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s LACNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
    }

    @Test
    public void import_lacnic_grs_use_downloaded_dump() {
        grsSourceImporter.grsImport(lacnicGrsSource, false);

        assertThat(query("-s LACNIC-GRS SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s LACNIC-GRS -i mnt-by SOME-MNT"), containsString("mntner:         SOME-MNT"));
        assertThat(query("-s LACNIC-GRS -i mnt-by SOME-MNT"), not(containsString("changed:")));
    }

    private void awaitAll(final List<Future> futures) throws ExecutionException, InterruptedException {
        for (final Future<?> future : futures) {
            future.get();
        }
    }

    private String query(final String query) {
        return TelnetWhoisClient.queryLocalhost(queryServer.getPort(), query);
    }
}
