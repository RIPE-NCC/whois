package net.ripe.db.nrtm4.client.importer;

import io.netty.util.internal.StringUtil;
import net.ripe.db.nrtm4.client.client.MirrorDeltaInfo;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class DeltaImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientRepository nrtm4ClientRepository;

    private final Nrtm4ClientInfoRepository nrtm4ClientInfoRepository;

    public static final String RECORD_SEPARATOR = "\u001E";

    public DeltaImporter(final NrtmRestClient nrtmRestClient,
                         final Nrtm4ClientRepository nrtm4ClientRepository,
                         final Nrtm4ClientInfoRepository nrtm4ClientInfoRepository) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientRepository = nrtm4ClientRepository;
        this.nrtm4ClientInfoRepository = nrtm4ClientInfoRepository;
    }

    @Override
    public void doImport(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        final List<UpdateNotificationFileResponse.NrtmFileLink> deltas = getNewDeltas(source, updateNotificationFile);

        if (deltas.isEmpty()) {
            LOGGER.info("No new deltas to be processed");
            return;
        }

        deltas.forEach(delta -> {
            final byte[] deltaFilePayload = nrtmRestClient.getDeltaFiles(delta.getUrl());

            if (deltaFilePayload == null){
                LOGGER.error("This cannot happen. UNF has a non-existing delta");
                return;
            }

            final String[] deltaFileResponse = StringUtils.split(new String(deltaFilePayload, StandardCharsets.UTF_8), RECORD_SEPARATOR);

            final Metadata metadata = getMetadata(deltaFileResponse);

            final List<MirrorDeltaInfo> mirrorObjectInfos = getMirrorDeltaObjects(deltaFileResponse);

            if (!delta.getHash().equals(calculateSha256(deltaFilePayload))){
                LOGGER.error("Snapshot hash doesn't match, skipping import");
                return;
            }

            if (!metadata.sessionId().equals(updateNotificationFile.getSessionID())){
                // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
                //  call initialize X number of times and return error to avoid this situation?
                LOGGER.error("The session is not the same in the UNF and snapshot");
                //initializeNRTMClientForSource(source, updateNotificationFile);
                return;
            }

            mirrorObjectInfos.forEach(deltaInfo -> {
                if (deltaInfo.getAction().equals(MirrorDeltaInfo.Action.ADD_MODIFY)){
                    final Integer objectId = nrtm4ClientRepository.getMirroredObjectId(deltaInfo.getRpslObject().getKey().toString());
                    if (objectId == null) {
                        nrtm4ClientRepository.persistRpslObject(deltaInfo.getRpslObject());
                        return;
                    }
                    nrtm4ClientRepository.updateMirroredObject(deltaInfo.getRpslObject(), objectId);
                } else {
                    nrtm4ClientRepository.removeMirroredObject(deltaInfo.getPrimaryKey());
                }
            });

            nrtm4ClientInfoRepository.saveDeltaFileVersion(source, metadata.version, metadata.sessionId());
        });
    }

    private List<UpdateNotificationFileResponse.NrtmFileLink> getNewDeltas(String source, UpdateNotificationFileResponse updateNotificationFile) {
        final NrtmClientVersionInfo nrtmClientVersionInfo = nrtm4ClientInfoRepository.getNrtmLastVersionInfoForDeltasPerSource(source);

        if (nrtmClientVersionInfo == null){
            return updateNotificationFile.getDeltas();
        }

        return updateNotificationFile.getDeltas()
                .stream()
                .filter(delta -> delta.getVersion() > nrtmClientVersionInfo.version())
                .toList();
    }


    private static List<MirrorDeltaInfo> getMirrorDeltaObjects(final String[] records) {
        final List<MirrorDeltaInfo> mirrorDeltaInfos = Lists.newArrayList();
        for (int i = 1; i < records.length; i++) {
            final JSONObject jsonObject = new JSONObject(records[i]);
            final String deltaAction = jsonObject.getString("action");
            final String deltaObjectType = jsonObject.optString("object_class", null);
            final String deltaPrimaryKey = jsonObject.optString("primary_key", null);
            final String deltaUpdatedObject = jsonObject.optString("object", null);
            final RpslObject rpslObject = !StringUtil.isNullOrEmpty(deltaUpdatedObject) ?
                    RpslObject.parse(deltaUpdatedObject) : null;

            final MirrorDeltaInfo mirrorDeltaInfo =
                    new MirrorDeltaInfo(rpslObject,
                            deltaAction,
                            deltaObjectType,
                            deltaPrimaryKey);
            mirrorDeltaInfos.add(mirrorDeltaInfo);
        }
        return mirrorDeltaInfos;
    }

    private static Metadata getMetadata(String[] records) {
        final JSONObject jsonObject = new JSONObject(records[0]);
        final int deltatVersion = jsonObject.getInt("version");
        final String deltaSessionId = jsonObject.getString("session_id");
        return new Metadata(deltatVersion, deltaSessionId);
    }

    private record Metadata(int version, String sessionId) {}
}
