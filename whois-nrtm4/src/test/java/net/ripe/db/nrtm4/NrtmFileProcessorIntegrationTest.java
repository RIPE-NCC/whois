package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.NotificationFileDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


@Tag("IntegrationTest")
public class NrtmFileProcessorIntegrationTest extends AbstractNrtm4IntegrationBase {

    @Autowired
    private DeltaFileDao deltaFileDao;

    @Autowired
    private NrtmFileProcessor nrtmFileProcessor;

    @Autowired
    private NotificationFileDao notificationFileDao;

    @Autowired
    private NrtmVersionInfoRepository nrtmVersionInfoRepository;

    @Autowired
    private SnapshotFileRepository snapshotFileRepository;

    @Autowired
    private SourceRepository sourceRepository;

    @Test
    void file_write_job_works_on_empty_whois() {
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
            final var deltaFile = deltaFileDao.getDeltasForNotification(snapshotVersion, 0);
            assertThat(deltaFile.size(), is(0));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get());
            assertThat(lastNotification.versionId(), is(snapshotVersion.id()));
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
            final var deltaFile = deltaFileDao.getDeltasForNotification(snapshotVersion, 0);
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
            final var deltaFiles = deltaFileDao.getDeltasForNotification(snapshotVersion, 0);
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
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get());
            assertThat(lastNotification.versionId(), is(deltaVersion.id()));
            final var notificationFile = new ObjectMapper().readValue(lastNotification.payload(), PublishableNotificationFile.class);
            assertThat(notificationFile.getDeltas().size(), is(1));
        }
    }

}
