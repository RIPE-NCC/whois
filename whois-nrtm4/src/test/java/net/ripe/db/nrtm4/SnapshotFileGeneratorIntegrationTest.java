package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    private NrtmSourceHolder nrtmSourceHolder;

    @Autowired
    private NrtmFileService nrtmFileService;

    @Autowired
    private NrtmFileStore nrtmFileStore;

    @Test
    public void initial_snapshot_file_is_generated_and_written_to_disk() throws IOException {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        final String sessionID;
        {
            final List<PublishableSnapshotFile> psfList = snapshotFileGenerator.createSnapshots();
            assertThat(psfList.size(), is(2));
            final PublishableSnapshotFile snapshotFile = psfList.stream().filter(psf -> psf.getSource().name().equals("TEST")).findFirst().orElseThrow();
            assertThat(snapshotFile.getVersion(), is(1L));
            sessionID = snapshotFile.getSessionID();
            assertThat(sessionID, is(notNullValue()));
            assertThat(snapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(snapshotFile.getNrtmVersion(), is(4));
            assertThat(snapshotFile.getType(), is(SNAPSHOT));
            final var bos = new ByteArrayOutputStream();
            nrtmFileStore.streamFromFile(snapshotFile.getSessionID(), snapshotFile.getFileName(), bos);
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
            assertThat(snapshotFile.getFileName(), startsWith("nrtm-snapshot.1."));
        }
        {
            // don't generate snapshot version if nothing changed
            final List<PublishableSnapshotFile> snapshotFileOptional = snapshotFileGenerator.createSnapshots();
            assertThat(snapshotFileOptional.size(), is(0));
        }
    }

}