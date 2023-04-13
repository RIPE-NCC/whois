package net.ripe.db.nrtm4;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.DeltaFileDao;
import net.ripe.db.nrtm4.dao.NotificationFileDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.DeltaFileVersionInfo;
import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNotificationFile;
import net.ripe.db.nrtm4.domain.SnapshotFileVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.VersionDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;


@Service
public class UpdateNotificationFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileGenerator.class);

    private final String baseUrl;
    private final DateTimeProvider dateTimeProvider;
    private final DeltaFileDao deltaFileDao;
    private final NotificationFileDao notificationFileDao;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SourceRepository sourceRepository;

    public UpdateNotificationFileGenerator(
        @Value("${nrtm.baseUrl}") final String baseUrl,
        final DateTimeProvider dateTimeProvider,
        final DeltaFileDao deltaFileDao,
        final NotificationFileDao notificationFileDao,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SourceRepository sourceRepository,
        final SnapshotFileRepository snapshotFileRepository
    ) {
        this.baseUrl = baseUrl;
        this.dateTimeProvider = dateTimeProvider;
        this.deltaFileDao = deltaFileDao;
        this.notificationFileDao = notificationFileDao;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.snapshotFileRepository = snapshotFileRepository;
        this.sourceRepository = sourceRepository;
    }

    public void generateFile() {
        LOGGER.info("Generating the update notification file");

       final List<NrtmSource> nrtmSources = sourceRepository.getSources();
       final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
       final LocalDateTime oneDayAgo = dateTimeProvider.getCurrentDateTime().minusDays(1);

       for(final NrtmSource nrtmSource : nrtmSources) {

          final Optional<NotificationFile> notificationFile = notificationFileDao.findLastNotification(nrtmSource);
          final Optional<SnapshotFileVersionInfo> snapshotFile = snapshotFileRepository.getLastSnapshotWithVersion(nrtmSource);

          if( !canProceed(notificationFile, nrtmSource, oneDayAgo, snapshotFile)) {
              LOGGER.info("Skipping generation of update notification file");
              continue;
          }

           LOGGER.info("Last generated snapshot file version is : {}" , snapshotFile.get().versionInfo().version());

          final List<DeltaFileVersionInfo> deltaFiles = deltaFileDao.getAllDeltasForSourceSince(nrtmSource, oneDayAgo);
          final NrtmVersionInfo fileVersion = getVersion(deltaFiles, snapshotFile.get());
          final String json = getPayload(snapshotFile.get(), deltaFiles, fileVersion, createdTimestamp);

          saveNotificationFile(createdTimestamp, notificationFile, fileVersion, json);
       }
    }

    private void saveNotificationFile(long createdTimestamp, Optional<NotificationFile> notificationFile, NrtmVersionInfo fileVersion, String json) {
        if(json == null) {
            LOGGER.error("Payload is empty for notification file");
            return;
        }

        if (notificationFile.isEmpty()) {
          notificationFileDao.save(NotificationFile.of(fileVersion.id(), createdTimestamp, json));
          return;
        }

        notificationFileDao.update(NotificationFile.of(notificationFile.get().id(), fileVersion.id(), createdTimestamp, json));
    }

    private boolean canProceed(final Optional<NotificationFile> notificationFile, final NrtmSource sourceModel, final LocalDateTime oneDayAgo, final Optional<SnapshotFileVersionInfo> snapshotFile) {
       if(snapshotFile.isEmpty()) {
           return false;
       }

        if(notificationFile.isEmpty() ||
                LocalDateTime.ofEpochSecond(notificationFile.get().created(), 0, ZoneOffset.UTC).isBefore(oneDayAgo)) {
            return true;
        }

        final NrtmVersionInfo lastVersion = nrtmVersionInfoRepository.findLastVersion(sourceModel)
                                                        .orElseThrow( () -> new IllegalStateException("No version exists with id " + notificationFile.get().versionId()));

        final NrtmVersionInfo notificationVersion = nrtmVersionInfoRepository.findById(notificationFile.get().versionId());
        LOGGER.info("Last notification file version  is : {}" , notificationVersion.version());

        if(notificationVersion.version() > lastVersion.version()) {
            throw new IllegalStateException("Something went wrong, found notification version higher then latest version");
        }

        //means there is new snapshot file created
        if(notificationVersion.version() > snapshotFile.get().versionInfo().version() ) {
            return true;
        }

        return notificationVersion.version() < lastVersion.version() ||
                notificationVersion.type() != lastVersion.type();
    }

    private NrtmVersionInfo getVersion(final List<DeltaFileVersionInfo> deltaFiles, final SnapshotFileVersionInfo snapshotFileWithVersion) {
        if(deltaFiles.isEmpty()) {
          return  snapshotFileWithVersion.versionInfo();
        }

        return snapshotFileWithVersion.versionInfo().version() >= deltaFiles.get(deltaFiles.size() - 1).versionInfo().version() ?
                     snapshotFileWithVersion.versionInfo() : deltaFiles.get(deltaFiles.size() - 1).versionInfo();
    }

    private List<PublishableNotificationFile.NrtmFileLink> getPublishableFile(final List<DeltaFileVersionInfo> files) {
        return files.stream()
                .map(file -> getPublishableFile(file.versionInfo(), file.deltaFile().name(),file.deltaFile().hash()))
                .toList();
    }

    private PublishableNotificationFile.NrtmFileLink getPublishableFile(final NrtmVersionInfo versionInfo, final String file, final String hash) {
        return new PublishableNotificationFile.NrtmFileLink(
                versionInfo.version(),
                urlString(versionInfo.source().getName().toString(), file),
                hash);
    }

    private String urlString(final String source, final String fileName) {
        return String.format("%s/%s/%s", baseUrl, source, fileName);
    }

    private String getPayload(final SnapshotFileVersionInfo snapshotFile, final List<DeltaFileVersionInfo> deltaFiles, final NrtmVersionInfo fileVersion, final long createdTimestamp) {
        try {
            final PublishableNotificationFile notification = new PublishableNotificationFile(
                    fileVersion,
                    new VersionDateTime(createdTimestamp).toString(),
                    null,
                    getPublishableFile(snapshotFile.versionInfo(), snapshotFile.snapshotFile().name(), snapshotFile.snapshotFile().hash()),
                    getPublishableFile(deltaFiles)
            );
            return new ObjectMapper().writeValueAsString(notification);
        } catch (Exception e) {
            LOGGER.error("NRTM file generation failed", e);
            return null;
        }
    }
}
