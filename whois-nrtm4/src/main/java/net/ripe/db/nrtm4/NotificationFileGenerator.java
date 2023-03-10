package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.dao.NotificationFileDao;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmNotifiable;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNrtmNotificationFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.whois.common.dao.VersionDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class NotificationFileGenerator {

    private final String baseUrl;
    private final NotificationFileDao notificationFileDao;
    private final SnapshotFileRepository snapshotFileRepository;

    public NotificationFileGenerator(
        @Value("${nrtm.baseUrl}") final String baseUrl,
        final NotificationFileDao notificationFileDao,
        final SnapshotFileRepository snapshotFileRepository
    ) {
        this.baseUrl = baseUrl;
        this.notificationFileDao = notificationFileDao;
        this.snapshotFileRepository = snapshotFileRepository;
    }

    void createInitialNotification(final NrtmVersionInfo version) {
        final SnapshotFile snapshotFile = snapshotFileRepository.getByVersionID(version.id()).orElseThrow();
        final String timestamp = new VersionDateTime(version.created()).toString();
        final PublishableNrtmNotificationFile publishableNrtmNotificationFile = new PublishableNrtmNotificationFile(
            version, timestamp, null, convert(version, snapshotFile), null);
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final String json = objectMapper.writeValueAsString(publishableNrtmNotificationFile);
            final NotificationFile notificationFile = NotificationFile.of(version.id(), json);
            notificationFileDao.save(notificationFile);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PublishableNrtmNotificationFile.NrtmFileLink> convert(final NrtmVersionInfo version, final List<NrtmNotifiable> files) {
        return files.stream().map(file -> convert(version, file)).toList();
    }

    private PublishableNrtmNotificationFile.NrtmFileLink convert(final NrtmVersionInfo version, final NrtmNotifiable file) {
        final String url = String.format("%s/%s/%s", baseUrl, version.sessionID(), file.name());
        return new PublishableNrtmNotificationFile.NrtmFileLink(
            version.version(), url, file.hash()
        );
    }

}
