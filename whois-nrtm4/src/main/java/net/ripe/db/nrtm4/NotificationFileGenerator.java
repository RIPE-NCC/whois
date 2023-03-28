package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.NotificationFileDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.DeltaFileVersionInfo;
import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.VersionDateTime;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;


@Service
public class NotificationFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFileGenerator.class);

    private final String baseUrl;
    private final DateTimeProvider dateTimeProvider;
    private final DeltaFileDao deltaFileDao;
    private final NotificationFileDao notificationFileDao;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileRepository snapshotFileRepository;

    public NotificationFileGenerator(
        @Value("${nrtm.baseUrl}") final String baseUrl,
        final DateTimeProvider dateTimeProvider,
        final DeltaFileDao deltaFileDao,
        final NotificationFileDao notificationFileDao,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SnapshotFileRepository snapshotFileRepository
    ) {
        this.baseUrl = baseUrl;
        this.dateTimeProvider = dateTimeProvider;
        this.deltaFileDao = deltaFileDao;
        this.notificationFileDao = notificationFileDao;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileRepository = snapshotFileRepository;
    }

    void createInitialNotification(final NrtmVersionInfo version) {
        final SnapshotFile snapshotFile = snapshotFileRepository.getByVersionID(version.id()).orElseThrow();
        final String timestamp = new VersionDateTime(version.created()).toString();
        final PublishableNotificationFile publishableNotificationFile = new PublishableNotificationFile(version, timestamp, null, convert(version, snapshotFile), null);
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final String json = objectMapper.writeValueAsString(publishableNotificationFile);
            final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
            final NotificationFile notificationFile = NotificationFile.of(version.id(), createdTimestamp, json);
            notificationFileDao.save(notificationFile);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Saving notification file failed for {}", version.source().getName());
            throw new RuntimeException(e);
        }
    }

    void updateNotification() {
        for (final NrtmVersionInfo version : nrtmVersionInfoRepository.findLastVersionPerSource()) {

            final Optional<NotificationFile> lastNotificationFileOpt = notificationFileDao.findLastNotification(version.source());
            if (lastNotificationFileOpt.isEmpty()) {
                LOGGER.error("Expected a previous notification file for {}, but there wasn't one", version.source().getName());
                continue;
            }
            final NotificationFile lastNotificationFile = lastNotificationFileOpt.get();
            final LocalDateTime oneDayAgo = dateTimeProvider.getCurrentDateTime().minusDays(1);
            final SnapshotFile snapshotFile = snapshotFileRepository.getLastSnapshot(version.source()).orElseThrow();
            final NrtmVersionInfo lastSnapshotVersion = nrtmVersionInfoRepository.findById(snapshotFile.versionId());
            final List<DeltaFileVersionInfo> deltaFiles = deltaFileDao.getDeltasForNotificationSince(lastSnapshotVersion, oneDayAgo);
            final List<PublishableNotificationFile.NrtmFileLink> deltaLinks = convert(deltaFiles);
            try {
                final PublishableNotificationFile lastNotificationUpdate = new ObjectMapper().readValue(lastNotificationFile.payload().getBytes(StandardCharsets.UTF_8), PublishableNotificationFile.class);
                if (deltaFiles.isEmpty() || lastNotificationUpdate.deltaEquals(deltaLinks) && lastNotificationUpdate.getSnapshot().getVersion() == lastSnapshotVersion.version()) {
                    if (LocalDateTime.ofEpochSecond(lastNotificationFile.created(), 0, ZoneOffset.UTC).isBefore(oneDayAgo)) {
                        // Just republish the last notification without other changes
                        notificationFileDao.save(NotificationFile.of(
                            lastNotificationFile, dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC)));
                    }
                    // There's no need for a new notification file
                    continue;
                }
                // Here we have deltas and possibly a new snapshot.
                final DeltaFileVersionInfo lastDelta = deltaFiles.get(deltaFiles.size() - 1);
                final NrtmVersionInfo newNotificationVersion = nrtmVersionInfoRepository.saveNewNotificationVersion(lastDelta.versionInfo());
                final String timestamp = new VersionDateTime(newNotificationVersion.created()).toString();
                final PublishableNotificationFile publishableNotificationFile = new PublishableNotificationFile(newNotificationVersion, timestamp, null, convert(lastSnapshotVersion, snapshotFile), deltaLinks);
                final String json = new ObjectMapper().writeValueAsString(publishableNotificationFile);
                final NotificationFile notificationFile = NotificationFile.of(newNotificationVersion.id(), newNotificationVersion.created(), json);
                notificationFileDao.save(notificationFile);
            } catch (final IOException e) {
                LOGGER.error("Failed to update notification file for {}", version.source().getName(), e);
            }
        }
    }

    private List<PublishableNotificationFile.NrtmFileLink> convert(final List<? extends DeltaFileVersionInfo> files) {
        final List<PublishableNotificationFile.NrtmFileLink> links = files.stream()
            .map(file -> new PublishableNotificationFile.NrtmFileLink(
                file.versionInfo().version(),
                urlString(file.versionInfo().source().getName().toString(), file.deltaFile().name()),
                file.deltaFile().hash()))
            .toList();
        if (links.isEmpty()) {
            return null;
        }
        return links;
    }

    private PublishableNotificationFile.NrtmFileLink convert(final NrtmVersionInfo version, final SnapshotFile file) {
        return new PublishableNotificationFile.NrtmFileLink(
            version.version(),
            urlString(version.source().getName().toString(), file.name()),
            file.hash());
    }

    private String urlString(final String source, final String fileName) {
        return String.format("%s/%s/%s", baseUrl, source, fileName);
    }

}
