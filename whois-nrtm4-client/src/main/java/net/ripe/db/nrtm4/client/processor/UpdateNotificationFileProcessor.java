package net.ripe.db.nrtm4.client.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import io.netty.util.internal.StringUtil;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.importer.DeltaMirrorImporter;
import net.ripe.db.nrtm4.client.importer.SnapshotMirrorImporter;
import net.ripe.db.whois.common.domain.Hosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class UpdateNotificationFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileProcessor.class);

    private final NrtmRestClient nrtmRestClient;

    private final DeltaMirrorImporter deltaImporter;

    private final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao;

    private final SnapshotMirrorImporter snapshotImporter;

    private final String publicKeyPath;


    public UpdateNotificationFileProcessor(final NrtmRestClient nrtmRestClient,
                                           final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao,
                                           final SnapshotMirrorImporter snapshotImporter,
                                           final DeltaMirrorImporter deltaImporter,
                                           @Value("${nrtm4.public.key:/export/service/whois/public.key}") final String publicKeyPath) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
        this.snapshotImporter = snapshotImporter;
        this.deltaImporter = deltaImporter;
        this.publicKeyPath = publicKeyPath;
    }

    public void processFile(){
        final Map<String, String> notificationFilePerSource =
                nrtmRestClient.getNrtmAvailableSources()
                .stream()
                .collect(Collectors.toMap(
                        string -> string,
                        nrtmRestClient::getNotificationFile
                ));
        LOGGER.info("Succeeded to read notification files from {}", notificationFilePerSource.keySet());
        final List<NrtmClientVersionInfo> nrtmLastVersionInfoPerSource = nrtm4ClientMirrorDao.getNrtmLastVersionInfoForUpdateNotificationFile();

        final String hostname = Hosts.getInstanceName();

        notificationFilePerSource.forEach((source, updateNotificationSignature) -> {
            final NrtmClientVersionInfo nrtmClientLastVersionInfo = nrtmLastVersionInfoPerSource
                    .stream()
                    .filter(nrtmVersionInfo -> nrtmVersionInfo.source() != null && nrtmVersionInfo.source().equals(source))
                    .findFirst()
                    .orElse(null);

            if (nrtmClientLastVersionInfo != null && !nrtmClientLastVersionInfo.hostname().equals(hostname)) {
                LOGGER.error("Different host");
                return;
            }

            final JWSObject jwsObjectParsed;
            try {
                jwsObjectParsed = JWSObject.parse(updateNotificationSignature);
            } catch (ParseException e) {
                return;
            }

            if (!isCorrectSignature(jwsObjectParsed)) {
                LOGGER.error("Update Notification File not corrected signed for {} source", source);
                return;
            }

            final UpdateNotificationFileResponse updateNotificationFile = getUpdateNotificationFileResponse(jwsObjectParsed);

            if (updateNotificationFile == null) {
                return;
            }

            if (nrtmClientLastVersionInfo != null && !nrtmClientLastVersionInfo.sessionID().equals(updateNotificationFile.getSessionID())) {
                LOGGER.info("Different session");
                snapshotImporter.truncateTables();
                return;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version() > updateNotificationFile.getVersion()) {
                LOGGER.info("The local version cannot be higher than the update notification version {}", source);
                snapshotImporter.truncateTables();
                return;
            }

            if (nrtmClientLastVersionInfo != null && nrtmClientLastVersionInfo.version().equals(updateNotificationFile.getVersion())) {
                LOGGER.info("There is no new version associated with the source {}", source);
                return;
            }

            try {
                if (nrtmClientLastVersionInfo == null) {
                    snapshotImporter.doImport(source, updateNotificationFile.getSessionID(), updateNotificationFile.getSnapshot());
                }

                final List<UpdateNotificationFileResponse.NrtmFileLink> newDeltas = getNewDeltasFromNotificationFile(source, updateNotificationFile);

                deltaImporter.doImport(source, updateNotificationFile.getSessionID(), newDeltas);
                persistUpdateFileVersion(source, updateNotificationFile, hostname);
            } catch (Exception ex){
                LOGGER.error("Failed to mirror database, cleaning up the tables", ex);
                cleanUpTablesSafely();
            }
        });
    }

    private void cleanUpTablesSafely(){
        try {
            snapshotImporter.truncateTables();
        } catch (Exception cleanupEx) {
            LOGGER.error("Failed to clean up the tables during database mirroring failure, check if database is UP");
        }
    }

    private void persistUpdateFileVersion(final String source, final UpdateNotificationFileResponse updateNotificationFile,
                                          final String hostname){
        nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(),
                updateNotificationFile.getSessionID(), hostname);
    }

    private List<UpdateNotificationFileResponse.NrtmFileLink> getNewDeltasFromNotificationFile(final String source,
                                                                                               final UpdateNotificationFileResponse updateNotificationFile) {

        if (!areContinuous(updateNotificationFile.getDeltas())){
            LOGGER.warn("No continuous deltas, skipping deltas");
            return Lists.newArrayList();
        }

        final NrtmClientVersionInfo nrtmClientVersionInfo = nrtm4ClientMirrorDao.getNrtmLastVersionInfoForDeltasPerSource(source);

        if (nrtmClientVersionInfo == null){
            return updateNotificationFile.getDeltas();
        }

        if (!(updateNotificationFile.getDeltas().getFirst().getVersion() == nrtmClientVersionInfo.version() + 1)){
            deltaImporter.truncateDeltas(); //Reinitialise from snapshot, all deltas will be applied again
            return updateNotificationFile.getDeltas();
        }

        return updateNotificationFile.getDeltas()
                .stream()
                .filter(delta -> delta.getVersion() > nrtmClientVersionInfo.version())
                .toList();
    }

    private boolean areContinuous(final List<UpdateNotificationFileResponse.NrtmFileLink> deltas){
        return IntStream.range(0, deltas.size() - 1)
                .allMatch(deltaCount -> deltas.get(deltaCount).getVersion() + 1 == deltas.get(deltaCount+1).getVersion());
    }

    @Nullable
    private UpdateNotificationFileResponse getUpdateNotificationFileResponse(final JWSObject jwsObjectParsed) {
        try {
            String payloadJson = jwsObjectParsed.getPayload().toString();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, UpdateNotificationFileResponse.class);
        } catch (JsonProcessingException ex){
            LOGGER.error("Unable to get the update notification file from the signature");
            return null;
        }
    }

    private boolean isCorrectSignature(final JWSObject jwsObjectParsed) {
        try {
            final ECKey parsedPublicKey = (ECKey) JWK.parseFromPEMEncodedObjects(readPublicKey());

            final JWSVerifier verifier = new ECDSAVerifier(parsedPublicKey);
            return jwsObjectParsed.verify(verifier);
        } catch (JOSEException ex) {
            LOGGER.error("failed to verify signature {}", ex.getMessage());
            return false;
        }
    }

    private String readPublicKey() {
        try {
            final String publicKey = Files.readString(Path.of(publicKeyPath));

            if (StringUtil.isNullOrEmpty(publicKey)) {
                throw new FileNotFoundException("Public key file not found in resources: " + publicKeyPath);
            }

            return publicKey;
        } catch (IOException ex){
            throw new IllegalStateException("Public key file not found in resources: " + publicKeyPath);
        }
    }

}
