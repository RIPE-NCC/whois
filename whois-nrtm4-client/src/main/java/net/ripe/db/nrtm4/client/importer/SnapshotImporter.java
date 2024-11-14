package net.ripe.db.nrtm4.client.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;


    public SnapshotImporter(final NrtmRestClient nrtmRestClient,
                                        final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void initializeNRTMClientForSource(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        // TODO: Truncate tables and start snapshot from scratch
        LOGGER.info("Should initialise snapshot");
        /*nrtm4ClientMirrorDao.truncateTables();
        nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());
        importSnapshot(source, updateNotificationFile);*/
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

        timer.cancel();
        stopwatch.stop();
        LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch.elapsed().toMillis(),
                source, processedCount);
    }

    private void processObject(final String record) throws JsonProcessingException {
        final MirrorRpslObject mirrorRpslObject = new ObjectMapper().readValue(record, MirrorRpslObject.class);
        nrtm4ClientMirrorDao.persistRpslObject(mirrorRpslObject.getObject());
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

    private void persistBatches(String[] remainingRecords, AtomicInteger processedCount) {
        Arrays.stream(remainingRecords).parallel().forEach(record -> {
            try {
                processObject(record);
            } catch (JsonProcessingException e) {
                LOGGER.error("Unable to parse record {}", record, e);
                throw new IllegalStateException(e);
            }
        });
        processedCount.addAndGet(remainingRecords.length);
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
        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, version, sessionId);
    }
}
