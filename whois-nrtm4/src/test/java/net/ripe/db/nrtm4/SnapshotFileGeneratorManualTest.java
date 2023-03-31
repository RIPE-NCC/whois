/*
package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static net.ripe.db.nrtm4.domain.NrtmDocumentType.SNAPSHOT;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;


@Tag("ManualTest")
public class SnapshotFileGeneratorManualTest extends AbstractNrtm4IntegrationBase {

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

    @Test
    public void big_snapshot_file_is_generated_and_written_to_disk() {
        loadScripts(whoisTemplate, "serials.no-schema.md.sql");
        loadScripts(whoisTemplate, "last.no-schema.md.sql");
        System.setProperty("nrtm.file.path", "/tmp");
        final String sessionID;
        final var state = whoisObjectRepository.getSnapshotState();
        final Collection<NrtmVersionInfo> psfList = snapshotFileGenerator.createInitialSnapshots(state);
        assertThat(psfList.size(), is(2));
        final NrtmVersionInfo snapshotJsonFile = psfList.stream().filter(psf -> psf.source().getName().toString().equals("TEST")).findFirst().orElseThrow();
        assertThat(snapshotJsonFile.version(), is(1L));
        sessionID = snapshotJsonFile.sessionID();
        assertThat(sessionID, is(notNullValue()));
        assertThat(snapshotJsonFile.source().getId(), is(sourceRepository.getWhoisSource().orElseThrow().getId()));
        assertThat(snapshotJsonFile.source().getName(), is(sourceRepository.getWhoisSource().orElseThrow().getName()));
        assertThat(snapshotJsonFile.type(), is(SNAPSHOT));
        final var lastSnapshotFile = snapshotFileRepository.getLastSnapshot(snapshotJsonFile.source()).orElseThrow();
        assertThat(lastSnapshotFile.name(), startsWith("nrtm-snapshot.1."));
    }

}
*/
