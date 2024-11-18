package net.ripe.db.nrtm4.client.processor;

import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.importer.SnapshotImporter;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.Hosts;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class UpdateNotificationFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileProcessor.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao;

    private final SnapshotImporter snapshotImporter;

    public UpdateNotificationFileProcessor(final NrtmRestClient nrtmRestClient,
                                           final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao,
                                           final SnapshotImporter snapshotImporter) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
        this.snapshotImporter = snapshotImporter;
    }

    @Transactional(rollbackFor = Exception.class)
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

        final String hostname = Hosts.getInstanceName();

        final Map<RpslObject, RpslObjectUpdateInfo> persistedRpslObjects = new HashMap<>();

        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            NrtmClientVersionInfo nrtmClientLastVersionInfo = nrtmLastVersionInfoPerSource
                    .stream()
                    .filter(nrtmVersionInfo -> nrtmVersionInfo.source() != null && nrtmVersionInfo.source().equals(source))
                    .findFirst()
                    .orElse(null);

            if (nrtmClientLastVersionInfo != null && !nrtmClientLastVersionInfo.hostname().equals(hostname)){
                LOGGER.info("Different host");
                snapshotImporter.truncateTables();
                nrtmClientLastVersionInfo = null;
            }

            if (nrtmClientLastVersionInfo != null && !nrtmClientLastVersionInfo.sessionID().equals(updateNotificationFile.getSessionID())){
                LOGGER.info("Different session");
                snapshotImporter.truncateTables();
                nrtmClientLastVersionInfo = null;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version() > updateNotificationFile.getVersion()){
                LOGGER.info("The local version cannot be higher than the update notification version {}", source);
                snapshotImporter.truncateTables();
                nrtmClientLastVersionInfo = null;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version().equals(updateNotificationFile.getVersion())){
                LOGGER.info("There is no new version associated with the source {}", source);
                return;
            }

            nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(),
                    updateNotificationFile.getSessionID(), hostname);

            if (nrtmClientLastVersionInfo == null){
                LOGGER.info("There is no existing Snapshot for the source {}", source);
                persistedRpslObjects.putAll(snapshotImporter.importSnapshot(source, updateNotificationFile));
            }
        });

        createDummyPerson();
        /*if (!persistedRpslObjects.isEmpty()){
            createIndexesAndDummyPerson(persistedRpslObjects);
        }*/

    }

    private void createIndexesAndDummyPerson(final Map<RpslObject, RpslObjectUpdateInfo> persistedRpslObjects) {
        final Map.Entry<RpslObject, RpslObjectUpdateInfo> persistDummyObject = snapshotImporter.persistDummyObjectIfNotExist();
        persistedRpslObjects.put(persistDummyObject.getKey(), persistDummyObject.getValue());
        snapshotImporter.createIndexes(persistedRpslObjects);
    }

    private void createDummyPerson() {
        final Map.Entry<RpslObject, RpslObjectUpdateInfo> persistDummyObject = snapshotImporter.persistDummyObjectIfNotExist();
        if (persistDummyObject != null){
            snapshotImporter.createIndexes(persistDummyObject);
        }
    }

}
