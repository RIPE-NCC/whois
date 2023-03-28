package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.NotificationFileDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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

    @Autowired
    TestDateTimeProvider dateTimeProvider;

    @Test
    void file_write_job_works_on_empty_whois() {
        nrtmFileProcessor.updateNrtmFilesAndPublishNotification();
        final var source = nrtmVersionInfoRepository.findLastVersion();
        assertThat(source.isPresent(), is(true));
    }
    
    @Test
    public void snapshot_file_and_delta_is_generated() throws JsonProcessingException {
        final var mntObject = RpslObject.parse("""
            mntner: DEV-MNT
            source: TEST
            """);
        final var mntObjectMod = RpslObject.parse("""
            mntner: DEV-MNT
            description: How now brown cow
            source: TEST
            """);
        final var mntObject1 = RpslObject.parse("""
            mntner: DEV1-MNT
            source: TEST
            """);
        final var mntObject1mod = RpslObject.parse("""
            mntner: DEV1-MNT
            description: How now brown cow
            source: TEST
            """);
        final var mntObject2 = RpslObject.parse("""
            mntner: DEV2-MNT
            source: TEST
            """);
        final var mntObject3 = RpslObject.parse("""
            mntner: DEV3-MNT
            source: TEST
            """);

        final var twoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 0);
        final var oneMinutePastTwoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 1);
        final var twoMinutesPastTwoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 2);
        final var threeMinutesPastTwoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 3);
        final var fourMinutesPastTwoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 4);
        final var fivePastTwoPmInMay = LocalDateTime.of(2023, 5, 22, 14, 5);
        final var twoAmTheNextDayInMay = LocalDateTime.of(2023, 5, 23, 2, 0);


        MockitoAnnotations.openMocks(this);
        {
            System.setProperty("nrtm.snapshot.window", "02:00 - 04:00");
            dateTimeProvider.setTime(twoPmInMay);
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(false));
        }
        var sessionID = "";
        {
            dateTimeProvider.setTime(oneMinutePastTwoPmInMay);
            databaseHelper.addObject(mntObject);

            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get()).orElseThrow();
            final var notificationFile = new ObjectMapper().readValue(lastNotification.payload(), PublishableNotificationFile.class);
            sessionID = notificationFile.getSessionID();
            final var sessionUUID = UUID.fromString(sessionID); // throws exception if not UUID format
            assertThat(sessionUUID, is(notNullValue()));
            assertThat(notificationFile.getVersion(), is(1L));

            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(1L));
            final var deltaFile = deltaFileDao.getDeltasForNotificationSince(snapshotVersion, LocalDateTime.MIN);
            assertThat(deltaFile.size(), is(0));
            assertThat(lastNotification.versionId(), is(snapshotVersion.id()));
        }
        // Run again to ensure no new snapshot or delta is created
        {
            dateTimeProvider.setTime(twoMinutesPastTwoPmInMay);
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(1L));
            final var deltaFile = deltaFileDao.getDeltasForNotificationSince(snapshotVersion, LocalDateTime.MIN);
            assertThat(deltaFile.size(), is(0));
        }
        {
            dateTimeProvider.setTime(threeMinutesPastTwoPmInMay);
            databaseHelper.updateObject(mntObjectMod);
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get());
            assertThat(lastNotification.isPresent(), is(true));
            final var notificationFile = new ObjectMapper().readValue(lastNotification.get().payload(), PublishableNotificationFile.class);
            assertThat(sessionID, is(notificationFile.getSessionID()));
            assertThat(notificationFile.getVersion(), is(2L));
            assertThat(notificationFile.getDeltas().size(), is(1));
        }
        {
            dateTimeProvider.setTime(fourMinutesPastTwoPmInMay);
            // Make a change in whois and expect a delta
            databaseHelper.addObject(mntObject1);
            databaseHelper.addObject(mntObject2);
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get());
            assertThat(lastNotification.isPresent(), is(true));
            final var notificationFile = new ObjectMapper().readValue(lastNotification.get().payload(), PublishableNotificationFile.class);
            assertThat(sessionID, is(notificationFile.getSessionID()));
            assertThat(notificationFile.getVersion(), is(3L));
            assertThat(notificationFile.getDeltas().size(), is(2));
        }
        {
            dateTimeProvider.setTime(fivePastTwoPmInMay);

            // Make changes in whois and expect a delta
            databaseHelper.deleteObject(mntObject2);
            databaseHelper.updateObject(mntObject1mod);
            databaseHelper.addObject(mntObject3);
            databaseHelper.deleteObject(mntObject3);

            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var sources = sourceRepository.getSources();
            assertThat(sources.size(), greaterThanOrEqualTo(2));
            final var notificationFiles = sources.stream()
                .map(src -> notificationFileDao.findLastNotification(src))
                .map(nf -> {
                    try {
                        return new ObjectMapper().readValue(nf.orElseThrow().payload(), PublishableNotificationFile.class);
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
            assertThat(notificationFiles.get(0).getSessionID(), not(is(notificationFiles.get(1).getSessionID())));
            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get()).orElseThrow();
            final var notificationFile = new ObjectMapper().readValue(lastNotification.payload(), PublishableNotificationFile.class);
            assertThat(sessionID, is(notificationFile.getSessionID()));
            assertThat(notificationFile.getVersion(), is(4L));
            assertThat(notificationFile.getSnapshot().getVersion(), is(1L));

            final var snapshotFile = getSnapshotFromUrl(notificationFile.getSnapshot());
            assertThat(snapshotFile.versionId(), is(1L));
            assertThat(snapshotFile.name(), startsWith("nrtm-snapshot.1.TEST."));
            assertThat(snapshotFile.name(), endsWith(".json.gz"));
            final var publishableSnapshot = convertGzPayloadToFile(snapshotFileRepository.getPayload(snapshotFile.id()).orElseThrow());
            assertThat(publishableSnapshot, notNullValue());
            assertThat(publishableSnapshot.getType().lowerCaseName(), is("snapshot"));
            assertThat(publishableSnapshot.getNrtmVersion(), is(4));
            assertThat(publishableSnapshot.getVersion(), is(1L));
            assertThat(publishableSnapshot.getSessionID(), is(sessionID));
            assertThat(publishableSnapshot.getSource().getName(), is("TEST"));
            assertThat(publishableSnapshot.getObjects().size(), is(1));
            assertThat(publishableSnapshot.getObjects().get(0), is("""
                mntner:         DEV-MNT
                source:         TEST
                remarks:        ****************************
                remarks:        * THIS OBJECT IS MODIFIED
                remarks:        * Please note that all data that is generally regarded as personal
                remarks:        * data has been removed from this object.
                remarks:        * To view the original object, please query the RIPE Database at:
                remarks:        * http://www.ripe.net/whois
                remarks:        ****************************
                """));

            assertThat(notificationFile.getDeltas().size(), is(3));
            {
                final var deltaFile = getDeltaFromUrl(notificationFile.getDeltas().get(0));
                assertThat(deltaFile.name(), startsWith("nrtm-delta.2.TEST"));
                assertThat(deltaFile.name(), endsWith(".json"));
                final var publishableDeltaFile = new ObjectMapper().readValue(deltaFile.payload(), PublishableDeltaFile.class);
                assertThat(publishableDeltaFile.getNrtmVersion(), is(4));
                assertThat(publishableDeltaFile.getVersion(), is(2L));
                assertThat(publishableDeltaFile.getType().lowerCaseName(), is("delta"));
                assertThat(publishableDeltaFile.getChanges().size(), is(1));
                {
                    final var change = publishableDeltaFile.getChanges().get(0);
                    assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
                    assertThat(change.getObject().toString(), containsString(mntObject.getKey().toString()));
                    assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
                }
            }
            {
                final var deltaFile = getDeltaFromUrl(notificationFile.getDeltas().get(1));
                assertThat(deltaFile.name(), startsWith("nrtm-delta.3.TEST"));
                final var publishableDeltaFile = new ObjectMapper().readValue(deltaFile.payload(), PublishableDeltaFile.class);
                assertThat(publishableDeltaFile.getVersion(), is(3L));
                assertThat(publishableDeltaFile.getChanges().size(), is(2));
                {
                    final var change = publishableDeltaFile.getChanges().get(0);
                    assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
                    assertThat(change.getObject().toString(), containsString(mntObject1.getKey().toString()));
                    assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
                }
                {
                    final var change = publishableDeltaFile.getChanges().get(1);
                    assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
                    assertThat(change.getObject().toString(), containsString(mntObject2.getKey().toString()));
                    assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
                }
            }
            {
                final var deltaFile = getDeltaFromUrl(notificationFile.getDeltas().get(2));
                assertThat(deltaFile.name(), startsWith("nrtm-delta.4.TEST"));
                final var publishableDeltaFile = new ObjectMapper().readValue(deltaFile.payload(), PublishableDeltaFile.class);
                assertThat(publishableDeltaFile.getVersion(), is(4L));
                assertThat(publishableDeltaFile.getChanges().size(), is(4));
                {
                    final var change = publishableDeltaFile.getChanges().get(0);
                    assertThat(change.getAction(), is(DeltaChange.Action.DELETE));
                    assertThat(change.getObjectType(), is(ObjectType.MNTNER));
                    assertThat(change.getPrimaryKey(), is(mntObject2.getKey().toString()));
                }
                {
                    final var change = publishableDeltaFile.getChanges().get(1);
                    assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
                    assertThat(change.getObjectType(), is(nullValue()));
                    assertThat(change.getPrimaryKey(), is(nullValue()));
                    assertThat(change.getObject().toString(), containsString(mntObject1mod.getKey().toString()));
                    assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
                }
                {
                    final var change = publishableDeltaFile.getChanges().get(2);
                    assertThat(change.getAction(), is(DeltaChange.Action.ADD_MODIFY));
                    assertThat(change.getObjectType(), is(nullValue()));
                    assertThat(change.getPrimaryKey(), is(nullValue()));
                    assertThat(change.getObject().toString(), containsString(mntObject3.getKey().toString()));
                    assertThat(change.getObject().toString(), containsString("* THIS OBJECT IS MODIFIED"));
                }
                {
                    final var change = publishableDeltaFile.getChanges().get(3);
                    assertThat(change.getAction(), is(DeltaChange.Action.DELETE));
                    assertThat(change.getObjectType(), is(ObjectType.MNTNER));
                    assertThat(change.getPrimaryKey(), is(mntObject3.getKey().toString()));
                }
            }
        }
        {
            dateTimeProvider.setTime(twoAmTheNextDayInMay);
            nrtmFileProcessor.updateNrtmFilesAndPublishNotification();

            final var whoisSource = sourceRepository.getWhoisSource();
            assertThat(whoisSource.isPresent(), is(true));
            final var lastNotification = notificationFileDao.findLastNotification(whoisSource.get()).orElseThrow();
            final var notificationFile = new ObjectMapper().readValue(lastNotification.payload(), PublishableNotificationFile.class);
            sessionID = notificationFile.getSessionID();
            final var sessionUUID = UUID.fromString(sessionID); // throws exception if not UUID format
            assertThat(sessionUUID, is(notNullValue()));
            assertThat(notificationFile.getVersion(), is(4L));
            assertThat(notificationFile.getSnapshot().getVersion(), is(4L));

            final var snapshotFile = snapshotFileRepository.getLastSnapshot(whoisSource.get());
            assertThat(snapshotFile.isPresent(), is(true));
            final var snapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.get().versionId());
            assertThat(snapshotVersion.version(), is(4L));
            final var deltaFile = deltaFileDao.getDeltasForNotificationSince(snapshotVersion, LocalDateTime.MIN);
            assertThat(deltaFile.size(), is(3));
        }
    }

    private DeltaFile getDeltaFromUrl(final PublishableNotificationFile.NrtmFileLink fileLink) {
        final var splits = fileLink.getUrl().split("/");
        return deltaFileDao.getByName(splits[4]).orElseThrow();
    }

    private SnapshotFile getSnapshotFromUrl(final PublishableNotificationFile.NrtmFileLink fileLink) {
        final var splits = fileLink.getUrl().split("/");
        return snapshotFileRepository.getByName(splits[4]).orElseThrow();
    }

    private PublishableSnapshotFile convertGzPayloadToFile(final byte[] bytes) {
        try {
            final var gzIn = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return new ObjectMapper().readValue(gzIn, PublishableSnapshotFile.class);
        } catch (final IOException e) {
            return null;
        }
    }

}
