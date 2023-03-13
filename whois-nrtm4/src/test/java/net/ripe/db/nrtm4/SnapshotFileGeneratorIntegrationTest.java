package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.nrtm4.domain.NrtmDocumentType.SNAPSHOT;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;


@Tag("IntegrationTest")
public class SnapshotFileGeneratorIntegrationTest extends AbstractNrtm4IntegrationBase {

    @BeforeAll
    static void enablePrettyPrint() {
        System.setProperty("nrtm.prettyprint.snapshots", "true");
    }

    @Autowired
    private SnapshotFileGenerator snapshotFileGenerator;

    @Autowired
    private SnapshotFileRepository snapshotFileRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private WhoisObjectRepository whoisObjectRepository;

    @Autowired
    private NrtmFileService nrtmFileService;

    @Test
    public void snapshot_file_is_generated_and_written_to_disk() throws IOException {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        System.setProperty("nrtm.file.path", "/tmp");
        sourceRepository.createSources();
        final String sessionID;
        {
            final var state = whoisObjectRepository.getSnapshotState();
            final Collection<PublishableNrtmFile> psfList = snapshotFileGenerator.createSnapshots(state);
            assertThat(psfList.size(), is(2));
            final PublishableNrtmFile snapshotJsonFile = psfList.stream().filter(psf -> psf.getSource().getName().toString().equals("TEST")).findFirst().orElseThrow();
            assertThat(snapshotJsonFile.getVersion(), is(1L));
            sessionID = snapshotJsonFile.getSessionID();
            assertThat(sessionID, is(notNullValue()));
            assertThat(snapshotJsonFile.getSource().getId(), is(sourceRepository.getWhoisSource().orElseThrow().getId()));
            assertThat(snapshotJsonFile.getSource().getName(), is(sourceRepository.getWhoisSource().orElseThrow().getName()));
            assertThat(snapshotJsonFile.getNrtmVersion(), is(4));
            assertThat(snapshotJsonFile.getType(), is(SNAPSHOT));
            final var lastSnapshotFile = snapshotFileRepository.getLastSnapshot(snapshotJsonFile.getSource()).orElseThrow();
            final var bos = new ByteArrayOutputStream();
            streamFromGZFile(snapshotJsonFile.getSessionID(), lastSnapshotFile.name(), bos);
            final var expected = """
                {
                  "nrtm_version" : 4,
                  "type" : "snapshot",
                  "source" : "TEST",
                  "session_id" : "",
                  "version" : 1,
                  "objects" : [
                    "inetnum:        195.77.187.144 - 195.77.187.151\\nnetname:        Netname\\ndescr:          Descrip-\\\\ntion\\ncountry:        es\\nadmin-c:        DUMY-RIPE\\ntech-c:         DUMY-RIPE\\nstatus:         ASSIGNED PA\\nmnt-by:         MAINT-AS3352\\nsource:         TEST\\nremarks:        ****************************\\nremarks:        * THIS OBJECT IS MODIFIED\\nremarks:        * Please note that all data that is generally regarded as personal\\nremarks:        * data has been removed from this object.\\nremarks:        * To view the original object, please query the RIPE Database at:\\nremarks:        * http://www.ripe.net/whois\\nremarks:        ****************************\\n"
                  ]
                }""";
            assertThat(bos.toString(StandardCharsets.UTF_8).replaceFirst("\"session_id\" : \"[^\"]+\"", "\"session_id\" : \"\""), is(expected));
            assertThat(lastSnapshotFile.name(), startsWith("nrtm-snapshot.1."));
        }
    }

    @Test
    public void big_snapshot_file_is_generated_and_written_to_disk() {
        loadScripts(whoisTemplate, "serials.no-schema.md.sql");
        loadScripts(whoisTemplate, "last.no-schema.md.sql");
        sourceRepository.createSources();
        final String sessionID;
        {
            final var state = whoisObjectRepository.getSnapshotState();
            final Collection<PublishableNrtmFile> psfList = snapshotFileGenerator.createSnapshots(state);
            assertThat(psfList.size(), is(2));
            final PublishableNrtmFile snapshotJsonFile = psfList.stream().filter(psf -> psf.getSource().getName().toString().equals("TEST")).findFirst().orElseThrow();
            assertThat(snapshotJsonFile.getVersion(), is(1L));
            sessionID = snapshotJsonFile.getSessionID();
            assertThat(sessionID, is(notNullValue()));
            assertThat(snapshotJsonFile.getSource().getId(), is(sourceRepository.getWhoisSource().orElseThrow().getId()));
            assertThat(snapshotJsonFile.getSource().getName(), is(sourceRepository.getWhoisSource().orElseThrow().getName()));
            assertThat(snapshotJsonFile.getNrtmVersion(), is(4));
            assertThat(snapshotJsonFile.getType(), is(SNAPSHOT));
            //final var bos = new ByteArrayOutputStream();
            //nrtmFileStore.streamFromGZFile(snapshotFile.getSessionID(), snapshotFile.getFileName(), bos);
            final var lastSnapshotFile = snapshotFileRepository.getLastSnapshot(snapshotJsonFile.getSource()).orElseThrow();
            assertThat(lastSnapshotFile.name(), startsWith("nrtm-snapshot.1."));
        }
    }

    void streamFromGZFile(final String sessionId, final String name, final OutputStream out) throws IOException {
        final var path = System.getProperty("nrtm.file.path");
        try (final FileInputStream fis = NrtmFileUtil.getFileInputStream(path, sessionId, name)) {
            final GZIPInputStream gzipInputStream = new GZIPInputStream(fis);
            gzipInputStream.transferTo(out);
        }
    }

}
