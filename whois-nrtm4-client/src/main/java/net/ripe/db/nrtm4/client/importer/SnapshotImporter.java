package net.ripe.db.nrtm4.client.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientInfoRepository nrtm4ClientInfoMirrorDao;

    private final Nrtm4ClientRepository nrtm4ClientRepository;


    public SnapshotImporter(final NrtmRestClient nrtmRestClient,
                            final Nrtm4ClientInfoRepository nrtm4ClientInfoMirrorDao,
                            final Nrtm4ClientRepository nrtm4ClientRepository) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientInfoMirrorDao = nrtm4ClientInfoMirrorDao;
        this.nrtm4ClientRepository  = nrtm4ClientRepository;
    }

    public void truncateTables(){
        nrtm4ClientInfoMirrorDao.truncateTables();
        nrtm4ClientRepository.truncateTables();
    }

    public void importSnapshot(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final UpdateNotificationFileResponse.NrtmFileLink snapshot = updateNotificationFile.getSnapshot();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            return;
        }

        final byte[] payload = nrtmRestClient.getSnapshotFile(snapshot.getUrl());

        if (!snapshot.getHash().equals(calculateSha256(payload))){
            LOGGER.error("Snapshot hash doesn't match, skipping import");
            return;
        }

        final AtomicInteger processedCount = new AtomicInteger(0);
        final Timer timer = new Timer();
        printProgress(timer, processedCount);

        GzipDecompressor.decompressRecords(
                payload,
                firstRecord -> processMetadata(source, updateNotificationFile, firstRecord),
                recordBatches -> persistBatches(recordBatches, processedCount)
        );

        persistDummyObjectIfNotExist();

        timer.cancel();
        stopwatch.stop();

        LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch.elapsed().toMillis(),
                source, processedCount);
    }


    public void persistDummyObjectIfNotExist(){
        final RpslObject dummyObject = getPlaceholderPersonObject();
        final Integer objectId = nrtm4ClientRepository.getMirroredObjectId(dummyObject.getKey().toString());
        if (objectId != null){
            return;
        }
        nrtm4ClientRepository.persistRpslObject(getPlaceholderPersonObject());
    }

    private void printProgress(final Timer timer, final AtomicInteger processedCount) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Records processed so far: {}", processedCount.get());
            }
        }, 0, 10000);
    }

    private static String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return encodeHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.error("Unable to calculate the hash", e);
            throw new IllegalStateException(e);
        }
    }

    private void persistBatches(final String[] remainingRecords,
                                final AtomicInteger processedCount) {
        Arrays.stream(remainingRecords)
                .parallel()
                .forEach(record -> {
                    try {
                        final Map.Entry<RpslObject, RpslObjectUpdateInfo> persistedRecord = nrtm4ClientRepository.processObject(record);
                        nrtm4ClientRepository.createIndexes(persistedRecord.getKey(), persistedRecord.getValue());
                        processedCount.incrementAndGet();
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Unable to parse record {}", record, e);
                        throw new IllegalStateException(e);
                    }
                });
    }

    public static RpslObject getPlaceholderPersonObject() {
        return RpslObject.parse("" +
                "person:         Placeholder Person Object\n" +
                "address:        RIPE Network Coordination Centre\n" +
                "address:        P.O. Box 10096\n" +
                "address:        1001 EB Amsterdam\n" +
                "address:        The Netherlands\n" +
                "phone:          +31 20 535 4444\n" +
                "nic-hdl:        DUMY-RIPE\n" +
                "mnt-by:         RIPE-DBM-MNT\n" +
                "remarks:        **********************************************************\n" +
                "remarks:        * This is a placeholder object to protect personal data.\n" +
                "remarks:        * To view the original object, please query the RIPE\n" +
                "remarks:        * Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        **********************************************************\n" +
                "created:        2009-07-24T17:00:00Z\n" +
                "last-modified:  2009-07-24T17:00:00Z\n" +
                "source:         RIPE"
        );
    }

    private void processMetadata(String source, UpdateNotificationFileResponse updateNotificationFile, String firstRecord) {
        final JSONObject jsonObject = new JSONObject(firstRecord);
        final int version = jsonObject.getInt("version");
        final String sessionId = jsonObject.getString("session_id");
        if (!sessionId.equals(updateNotificationFile.getSessionID())) {
            // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
            //  call initialize X number of times and return error to avoid this situation?
            LOGGER.error("The session is not the same in the UNF and snapshot");
            //initializeNRTMClientForSource(source, updateNotificationFile);
            throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
        }
        nrtm4ClientInfoMirrorDao.saveSnapshotFileVersion(source, version, sessionId);
    }
}
