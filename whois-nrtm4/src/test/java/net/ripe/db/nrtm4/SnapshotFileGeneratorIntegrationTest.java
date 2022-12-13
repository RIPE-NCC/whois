package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.persist.NrtmSourceHolder;
import net.ripe.db.nrtm4.publish.PublishableSnapshotFile;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static net.ripe.db.nrtm4.persist.NrtmDocumentType.SNAPSHOT;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.truncateTables;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;


@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-nrtm4-test.xml"})
public class SnapshotFileGeneratorIntegrationTest extends AbstractDatabaseHelperIntegrationTest {

    @Autowired
    private SnapshotFileGenerator snapshotFileGenerator;

    @Autowired
    private NrtmSourceHolder nrtmSourceHolder;

    @Autowired
    private NrtmFileService nrtmFileService;

    @Autowired
    private NrtmFileRepo nrtmFileRepo;

    @BeforeEach
    public void setUp() {
        truncateTables(databaseHelper.getNrtmTemplate());
    }

    @Test
    public void snapshot_file_is_generated() throws IOException {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        final String sessionID;
        {
            final Optional<PublishableSnapshotFile> optFile = snapshotFileGenerator.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(optFile.isPresent(), is(true));
            final PublishableSnapshotFile snapshotFile = optFile.get();
            assertThat(snapshotFile.getVersion(), is(1L));
            sessionID = snapshotFile.getSessionID();
            assertThat(sessionID, is(notNullValue()));
            assertThat(snapshotFile.getSource(), is(nrtmSourceHolder.getSource()));
            assertThat(snapshotFile.getNrtmVersion(), is(4));
            assertThat(snapshotFile.getType(), is(SNAPSHOT));
            final var bos = new ByteArrayOutputStream();
            nrtmFileService.writeFileToStream(snapshotFile.getFileName(), bos);
            assertThat(bos.toString(StandardCharsets.UTF_8), is("" +
                "{\"nrtm_version\":4," +
                "\"type\":\"snapshot\"," +
                "\"source\":\"TEST\"," +
                "\"version\":1," +
                "\"objects\":[" +
                "\"inetnum:        195.77.187.144 - 195.77.187.151\\nnetname:        Netname\\ndescr:          Description\\ncountry:        es\\nadmin-c:        TEST-RIPE\\ntech-c:         TEST-RIPE\\nstatus:         ASSIGNED PA\\nmnt-by:         MAINT-AS3352\\nsource:         RIPE\\n\"," +
                "\"person:         Test Person\\naddress:        NL\\ne-mail:         test@test.net\\nphone:          +1 234 567 8900\\nnotify:         test@test.net\\nmnt-by:         TEST-MNT\\nnic-hdl:        TEST-RIPE\\nsource:         RIPE\\n\"" +
                "]}"));
        }
        {
            // don't generate snapshot version if nothing changed
            final Optional<PublishableSnapshotFile> snapshotFileOptional = snapshotFileGenerator.generateSnapshot(nrtmSourceHolder.getSource());
            assertThat(snapshotFileOptional.isPresent(), is(false));
        }
    }

}