package net.ripe.db.nrtm4.client.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
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
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class UpdateNotificationFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileProcessor.class);

    private final NrtmRestClient nrtmRestClient;

    private final DeltaMirrorImporter deltaImporter;

    private final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao;

    private final SnapshotMirrorImporter snapshotImporter;

    private final static String PUBLIC_KEY_PATH = "public.key";

    private final String baseUrl;

    public UpdateNotificationFileProcessor(
            @Value("${nrtm.baseUrl}") final String baseUrl,
            final NrtmRestClient nrtmRestClient,
            final Nrtm4ClientInfoRepository nrtm4ClientMirrorDao,
            final SnapshotMirrorImporter snapshotImporter,
            final DeltaMirrorImporter deltaImporter) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
        this.snapshotImporter = snapshotImporter;
        this.deltaImporter = deltaImporter;
        this.baseUrl = baseUrl;
    }

    public void processFile(){
        final StringBuilder filesCommonPath = new StringBuilder(baseUrl); // unf, snap, delta common path of their URL (match the RFC)

        final Map<String, String> notificationFilePerSource =
                nrtmRestClient.getNrtmAvailableSources(baseUrl)
                .stream()
                .collect(Collectors.toMap(
                        source -> source,
                        source -> nrtmRestClient.getNotificationFileSignature(filesCommonPath.append(source).toString())
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
                    snapshotImporter.doImport(source, updateNotificationFile.getSessionID(), filesCommonPath.append(source).toString(), updateNotificationFile.getSnapshot());
                }
                final List<UpdateNotificationFileResponse.NrtmFileLink> newDeltas = getNewDeltasFromNotificationFile(source, updateNotificationFile);
                deltaImporter.doImport(source, updateNotificationFile.getSessionID(), filesCommonPath.append(source).toString(), newDeltas);
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
        final NrtmClientVersionInfo nrtmClientVersionInfo = nrtm4ClientMirrorDao.getNrtmLastVersionInfoForDeltasPerSource(source);

        if (nrtmClientVersionInfo == null){
            return updateNotificationFile.getDeltas();
        }

        return updateNotificationFile.getDeltas()
                .stream()
                .filter(delta -> delta.getVersion() > nrtmClientVersionInfo.version())
                .toList();
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

    private static String readPublicKey() {
        try {
            try (InputStream inputStream = UpdateNotificationFileProcessor.class.getClassLoader().getResourceAsStream(PUBLIC_KEY_PATH)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Public key file not found in resources: " + PUBLIC_KEY_PATH);
                }
                return new String(inputStream.readAllBytes());
            }
        } catch (IOException ex){
            throw new IllegalStateException("Public key file not found in resources: " + PUBLIC_KEY_PATH);
        }
    }

}
