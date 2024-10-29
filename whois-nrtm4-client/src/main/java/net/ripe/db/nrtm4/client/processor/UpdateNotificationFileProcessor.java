package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.importer.SnapshotImporter;
import net.ripe.db.nrtm4.client.scheduler.Nrtm4ClientCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class UpdateNotificationFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileProcessor.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    private final SnapshotImporter snapshotImporter;

    public UpdateNotificationFileProcessor(final NrtmRestClient nrtmRestClient,
                                           final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao,
                                           final SnapshotImporter snapshotImporter) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
        this.snapshotImporter = snapshotImporter;
    }

    // TODO: Transaction needed, rollback if there is an issue somewhere to restore the database
    public void processFile(){
        final Map<String, UpdateNotificationFileResponse> notificationFilePerSource =
                nrtmRestClient.getNrtmAvailableSources()
                .stream()
                .collect(Collectors.toMap(
                        string -> string,
                        nrtmRestClient::getNotificationFile
                ));
        LOGGER.info("Succeeded to read notification files from {}", notificationFilePerSource.keySet());
        final List<NrtmClientVersionInfo> nrtmLastVersionInfoPerSource = nrtm4ClientMirrorDao.getNrtmLastVersionInfoForUpdateNotificationFile();

        //TODO: [MH] Review integrity of the data checking the signature using the public key

        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            final NrtmClientVersionInfo nrtmClientLastVersionInfo = nrtmLastVersionInfoPerSource
                    .stream()
                    .filter(nrtmVersionInfo -> nrtmVersionInfo.source() != null && nrtmVersionInfo.source().equals(source))
                    .findFirst()
                    .orElse(null);

            if (nrtmClientLastVersionInfo != null && !nrtmClientLastVersionInfo.sessionID().equals(updateNotificationFile.getSessionID())){
                LOGGER.info("Different session");
                snapshotImporter.initializeNRTMClientForSource(source, updateNotificationFile);
                return;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version() > updateNotificationFile.getVersion()){
                LOGGER.info("The local version cannot be higher than the update notification version {}", source);
                snapshotImporter.initializeNRTMClientForSource(source, updateNotificationFile);
                return;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version().equals(updateNotificationFile.getVersion())){
                LOGGER.info("There is no new version associated with the source {}", source);
                return;
            }

            nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());

            if (nrtmClientLastVersionInfo == null){
                LOGGER.info("There is no existing Snapshot for the source {}", source);
                snapshotImporter.importSnapshot(source, updateNotificationFile);
            }
        });
    }

}