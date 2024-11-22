package net.ripe.db.nrtm4.client.processor;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.OctetKeyPair;
import io.netty.util.internal.StringUtil;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import net.ripe.db.nrtm4.client.dao.NrtmClientVersionInfo;
import net.ripe.db.nrtm4.client.importer.SnapshotImporter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
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

    private final String publicKey;

    public UpdateNotificationFileProcessor(final NrtmRestClient nrtmRestClient,
                                           final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao,
                                           final SnapshotImporter snapshotImporter,
                                           @Value("${nrtm.key}") final String publicKey) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
        this.snapshotImporter = snapshotImporter;
        this.publicKey = publicKey;
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

        notificationFilePerSource.forEach((source, updateNotificationFile) -> {
            final NrtmClientVersionInfo nrtmClientLastVersionInfo = nrtmLastVersionInfoPerSource
                    .stream()
                    .filter(nrtmVersionInfo -> nrtmVersionInfo.source() != null && nrtmVersionInfo.source().equals(source))
                    .findFirst()
                    .orElse(null);

            if (isNotCorrectedSigned(source)){
                LOGGER.info("Update Notification File not corrected signed for {} source", source);
                return;
            }

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

    private boolean isNotCorrectedSigned(final String source) {
        final String signature = nrtmRestClient.getNotificationFileSignature(source);
        if (StringUtil.isNullOrEmpty(signature)){
            return true;
        }
        return !verifySignature(signature, publicKey);
    }

    private static boolean verifySignature(final String signature, final String publicKey) {
        try {
            final JWSObject jwsObjectParsed = JWSObject.parse(signature);
            final OctetKeyPair parsedPublicKey =  OctetKeyPair.parse(publicKey);

            final JWSVerifier verifier = new Ed25519Verifier(parsedPublicKey);
            return jwsObjectParsed.verify(verifier);
        } catch (JOSEException | ParseException ex) {
            LOGGER.error("failed to verify signature {}", ex.getMessage());
            return false;
        }
    }

}
