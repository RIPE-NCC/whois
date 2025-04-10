package net.ripe.db.nrtm4.client.importer;

import io.netty.util.internal.StringUtil;
import net.ripe.db.nrtm4.client.client.MirrorDeltaInfo;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.config.NrtmClientTransactionConfiguration;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class DeltaMirrorImporter extends AbstractMirrorImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaMirrorImporter.class);

    private final NrtmRestClient nrtmRestClient;

    public DeltaMirrorImporter(final NrtmRestClient nrtmRestClient,
                               final Nrtm4ClientRepository nrtm4ClientRepository,
                               final Nrtm4ClientInfoRepository nrtm4ClientInfoRepository) {
        super(nrtm4ClientInfoRepository, nrtm4ClientRepository);
        this.nrtmRestClient = nrtmRestClient;
    }

    public void doImport(final String source,
                         final String sessionId,
                         final List<UpdateNotificationFileResponse.NrtmFileLink> freshDeltas){
        if (freshDeltas.isEmpty()) {
            LOGGER.info("No new deltas to be processed");
            return;
        }

        freshDeltas.forEach(delta -> {
            final byte[] deltaFilePayload = nrtmRestClient.getDeltaFile(source, delta.getUrl());

            if (deltaFilePayload == null || deltaFilePayload.length == 0){
                LOGGER.error("This cannot happen. UNF has a non-existing delta");
                return;
            }

            final String payloadHash = calculateSha256(deltaFilePayload);
            if (!delta.getHash().equals(payloadHash)){
                LOGGER.error("Delta hash {} doesn't match the payload {}, skipping import", delta.getHash(), payloadHash);
                return;
            }

            processPayload(deltaFilePayload, sessionId, source);
        });
    }

    private void processPayload(final byte[] deltaFilePayload, final String sessionId, final String source) {
        final Metadata metadata = persistDeltas(deltaFilePayload, sessionId);
        persistDeltaVersion(source, metadata.version, metadata.sessionId);
    }

    private Metadata persistDeltas(final byte[] deltaFilePayload, String sessionId) {
        ByteBuffer buffer = ByteBuffer.wrap(deltaFilePayload);
        InputStream inputStream = new ByteArrayInputStream(buffer.array(), buffer.position(), buffer.remaining());
        Metadata metadata = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String record;
            boolean isFirstRecord = true;
            while ((record = reader.readLine()) != null) {
                if (isFirstRecord){
                    metadata = extractMetadata(record);
                    validateSession(metadata.sessionId, sessionId);
                    isFirstRecord = false;
                    continue;
                }
                final MirrorDeltaInfo mirrorDeltaInfo = mapDeltaRecordOrNull(record);
                if (mirrorDeltaInfo == null){
                    continue;
                }
                applyDeltaRecord(mirrorDeltaInfo);
            }

            if (metadata == null){
                LOGGER.error("Metadata is missing in some delta");
                throw new IllegalArgumentException("Metadata is missing in some delta");
            }
        } catch (IOException ex){
            LOGGER.error("Unable to process deltas");
            throw new IllegalStateException(ex);
        }
        return metadata;
    }

    private void persistDeltaVersion(final String source, final int version, final String sessionId) throws IllegalArgumentException {
        nrtm4ClientInfoRepository.saveDeltaFileVersion(source, version, sessionId);
    }

    private MirrorDeltaInfo mapDeltaRecordOrNull(final String record){
        try {
            final JSONObject jsonObject = new JSONObject(record);
            final String deltaAction = jsonObject.getString("action");
            final String deltaObjectType = jsonObject.optString("object_class", null);
            final String deltaPrimaryKey = jsonObject.optString("primary_key", null);
            final String deltaUpdatedObject = jsonObject.optString("object", null);
            final RpslObject rpslObject = !StringUtil.isNullOrEmpty(deltaUpdatedObject) ?
                    RpslObject.parse(deltaUpdatedObject) : null;

            return new MirrorDeltaInfo(rpslObject,
                    deltaAction,
                    deltaObjectType,
                    deltaPrimaryKey);
        } catch (Exception ex){
            LOGGER.error("Unable to parse delta record");
            return null;
        }
    }

    @Transactional(transactionManager = NrtmClientTransactionConfiguration.NRTM_CLIENT_UPDATE_TRANSACTION, isolation = Isolation.REPEATABLE_READ)
    private void applyDeltaRecord(final MirrorDeltaInfo deltaInfo){
        if (deltaInfo.getAction().equals(MirrorDeltaInfo.Action.ADD_MODIFY)){

            final RpslObjectUpdateInfo rpslObjectUpdateInfo = nrtm4ClientRepository
                    .getMirroredObjectId(deltaInfo.getRpslObject().getType(), deltaInfo.getRpslObject().getKey().toString());
            if (rpslObjectUpdateInfo == null) {
                applyDeltaCreation(deltaInfo);
                return;
            }
            applyDeltaUpdate(deltaInfo, rpslObjectUpdateInfo);
        } else {
            applyDeltaDeletion(deltaInfo);
        }
    }

    private void applyDeltaCreation(final MirrorDeltaInfo deltaInfo) {
        final RpslObjectUpdateInfo rpslObjectCretedInfo = nrtm4ClientRepository.persistRpslObject(deltaInfo.getRpslObject());
        nrtm4ClientRepository.createIndexes(deltaInfo.getRpslObject(), rpslObjectCretedInfo);
    }

    private void applyDeltaDeletion(final MirrorDeltaInfo deltaInfo) {
        final RpslObjectUpdateInfo rpslObjectUpdateInfo = nrtm4ClientRepository.getMirroredObjectId(deltaInfo.getObjectType(), deltaInfo.getPrimaryKey());

        if (!serialExist(rpslObjectUpdateInfo)){
            LOGGER.error("delta with pkey: {} not deleted because serial doesn't exist", deltaInfo.getPrimaryKey());
            return;
        }
        nrtm4ClientRepository.removeMirroredObjectAndUpdateSerials(rpslObjectUpdateInfo);
    }

    private void applyDeltaUpdate(final MirrorDeltaInfo deltaInfo, final RpslObjectUpdateInfo rpslObjectUpdateInfo) {
        if (!serialExist(rpslObjectUpdateInfo)) {
            LOGGER.error("delta with pkey: {} not updated because serial doesn't exist", deltaInfo.getPrimaryKey());
            return;
        }
        nrtm4ClientRepository.updateMirroredObject(deltaInfo.getRpslObject(), rpslObjectUpdateInfo);
    }

    private boolean serialExist(final RpslObjectUpdateInfo rpslObjectUpdateInfo) {
        if (rpslObjectUpdateInfo != null){
            return nrtm4ClientRepository
                    .getSerialByObjectId(rpslObjectUpdateInfo.getObjectId(), rpslObjectUpdateInfo.getSequenceId()) != null;
        }
        return false;
    }

    private Metadata extractMetadata(final String firstRecord){
        return getMetadata(firstRecord);
    }

    private void validateSession(final String metadataSessionId, final String sessionId) throws IllegalArgumentException{
        if (!metadataSessionId.equals(sessionId)){
            LOGGER.error("The session {} is not the same in the UNF and delta {}", metadataSessionId, sessionId);
            throw new IllegalArgumentException("The session is not the same in the UNF and delta");
        }
    }

    private static Metadata getMetadata(final String records) {
        final JSONObject jsonObject = new JSONObject(records);
        final int deltaVersion = jsonObject.getInt("version");
        final String deltaSessionId = jsonObject.getString("session_id");
        return new Metadata(deltaVersion, deltaSessionId);
    }

    private record Metadata(int version, String sessionId) {}
}
