package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.loadScripts;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


@Tag("IntegrationTest")
public class NrtmFileProcessorIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private DeltaFileRepository deltaFileRepository;

    @Autowired
    private NrtmFileProcessor nrtmFileProcessor;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private SnapshotFileRepository snapshotFileRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    void file_write_job_is_enabled_by_jmx() throws IOException {
        loadScripts(whoisTemplate, "nrtm_sample_sm.sql");
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final var source = nrtmVersionInfoRepository.findLastVersion();
        assertThat(source.isPresent(), is(true));
    }

    @Test
    void file_write_job_works_on_empty_whois() throws IOException {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final var source = nrtmVersionInfoRepository.findLastVersion();
        assertThat(source.isPresent(), is(true));
    }

    @Test
    public void snapshot_file_and_delta_is_generated() throws JsonProcessingException {
        {
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(false));
        }
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        {
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(1L));
            final List<DeltaFile> deltaFile = deltaFileRepository.getDeltasForNotification(snapshotVersion, 0);
            assertThat(deltaFile.size(), is(0));
        }
        // Run again to ensure no new snapshot or delta is created
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        {
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(1L));
            final List<DeltaFile> deltaFile = deltaFileRepository.getDeltasForNotification(snapshotVersion, 0);
            assertThat(deltaFile.size(), is(0));
        }
        // Make a change in whois and expect a delta
        final var mntObject = RpslObject.parse("""
            mntner: DEV-MNT
            source: TEST
            """);
        databaseHelper.addObject(mntObject);
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        {
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(1L));
            final var deltaFiles = deltaFileRepository.getDeltasForNotification(snapshotVersion, 0);
            assertThat(deltaFiles.size(), is(1));
            final var deltaFile = deltaFiles.get(0);
            final var deltaVersion = nrtmVersionInfoRepository.findById(deltaFile.versionId());
            assertThat(deltaVersion.version(), is(2L));
            assertThat(deltaFile.name(), startsWith("nrtm-delta.2.TEST."));
            final var publishableFile = new ObjectMapper().readValue(deltaFile.payload(), PublishableDeltaFile.class);
            assertThat(publishableFile.getSource().getName(), is("TEST"));
            assertThat(publishableFile.getChanges().size(), is(1));
            final var change = publishableFile.getChanges().get(0);
            assertThat(change.getObject().toString(), startsWith(mntObject.toString()));
            assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
        }
    }

}
