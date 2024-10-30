package net.ripe.db.nrtm4.client.importer;

import net.ripe.db.nrtm4.client.client.MirrorDeltaInfo;
import net.ripe.db.nrtm4.client.client.NrtmClientFileResponse;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class DeltaImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    public DeltaImporter(final NrtmRestClient nrtmRestClient, final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void importDeltas(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        final List<UpdateNotificationFileResponse.NrtmFileLink> deltas = getNewDeltas(source, updateNotificationFile);

        if (deltas.isEmpty()) {
            LOGGER.info("No new deltas to be processed");
            return;
        }

        deltas.forEach(delta -> {
            final NrtmClientFileResponse deltaFileResponse = nrtmRestClient.getDeltaFiles(delta.getUrl());

            if (deltaFileResponse == null || deltaFileResponse.getObjectMirrorInfo() == null){
                LOGGER.error("This cannot happen. UNF has a non-existing delta");
                return;
            }

            if (!delta.getHash().equals(deltaFileResponse.getHash())){
                LOGGER.error("Snapshot hash doesn't match, skipping import");
                return;
            }

            if (!deltaFileResponse.getSessionID().equals(updateNotificationFile.getSessionID())){
                // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
                //  call initialize X number of times and return error to avoid this situation?
                LOGGER.error("The session is not the same in the UNF and snapshot");
                //initializeNRTMClientForSource(source, updateNotificationFile);
                return;
            }

            deltaFileResponse.getObjectMirrorInfo().forEach(deltaInfo -> {
                final MirrorDeltaInfo mirrorDeltaInfo = (MirrorDeltaInfo)deltaInfo;

                if (mirrorDeltaInfo.getAction().equals(MirrorDeltaInfo.Action.ADD_MODIFY)){
                    final Integer objectId = nrtm4ClientMirrorDao.getMirroredObjectId(mirrorDeltaInfo.getPrimaryKey());
                    if (objectId == null) {
                        nrtm4ClientMirrorDao.persistRpslObject(mirrorDeltaInfo.getObject());
                        return;
                    }
                    nrtm4ClientMirrorDao.updateMirroredObject(mirrorDeltaInfo.getObject(), objectId);
                } else {
                    nrtm4ClientMirrorDao.removeMirroredObject(mirrorDeltaInfo.getPrimaryKey());
                }
            });

            nrtm4ClientMirrorDao.saveDeltaFileVersion(source, deltaFileResponse.getVersion(), deltaFileResponse.getSessionID());
        });
    }

    private List<UpdateNotificationFileResponse.NrtmFileLink> getNewDeltas(String source, UpdateNotificationFileResponse updateNotificationFile) {
        final NrtmClientVersionInfo nrtmClientVersionInfo = nrtm4ClientMirrorDao.getNrtmLastVersionInfoForDeltasPerSource(source);

        if (nrtmClientVersionInfo == null){
            return updateNotificationFile.getDeltas();
        }

        return updateNotificationFile.getDeltas()
                .stream()
                .filter(delta -> delta.getVersion() > nrtmClientVersionInfo.version())
                .toList();
    }
}
