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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    public static final String RECORD_SEPARATOR = "\u001E";

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private static final int BATCH_SIZE = 1000;

    private static final int BUFFER_SIZE = 4096;

    public SnapshotImporter(final NrtmRestClient nrtmRestClient,
                                        final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao) {
        this.nrtmRestClient = nrtmRestClient;
        this.nrtm4ClientMirrorDao = nrtm4ClientMirrorDao;
    }

    public void initializeNRTMClientForSource(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        nrtm4ClientMirrorDao.truncateTables();
        nrtm4ClientMirrorDao.saveUpdateNotificationFileVersion(source, updateNotificationFile.getVersion(), updateNotificationFile.getSessionID());
        importSnapshot(source, updateNotificationFile);
    }

    public void importSnapshot(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final UpdateNotificationFileResponse.NrtmFileLink snapshot = updateNotificationFile.getSnapshot();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            return;
        }

        //TODO: [MH] new class that works in the middle of the clientRestService and the SnapshotImporter that
        // parallelise the process
        //String[] snapshotRecords;
        final byte[] payload = nrtmRestClient.getSnapshotFile(snapshot.getUrl());

        if (!snapshot.getHash().equals(calculateSha256(payload))){
            LOGGER.error("Snapshot hash doesn't match, skipping import");
            return;
        }

        LOGGER.info("Step 1");

        final var sessionId = new String[1];
        final AtomicInteger version = new AtomicInteger();
        final AtomicInteger processedCount = new AtomicInteger(0);
        final Timer timer = new Timer();
        printProgress(timer, processedCount);


        decompressAndProcessRecords(
                payload,
                firstRecord -> {
                    final JSONObject jsonObject = new JSONObject(firstRecord);
                    version.set(jsonObject.getInt("version"));
                    sessionId[0] = jsonObject.getString("session_id");
                    if (!sessionId[0].equals(updateNotificationFile.getSessionID())) {
                        // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
                        //  call initialize X number of times and return error to avoid this situation?
                        LOGGER.error("The session is not the same in the UNF and snapshot");
                        //initializeNRTMClientForSource(source, updateNotificationFile);
                        throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
                    }
                    LOGGER.info("Processed first record");
                },
                remainingRecords -> {
                    Arrays.stream(remainingRecords).parallel().forEach( record -> {
                        try {
                            processObject(record);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    processedCount.addAndGet(remainingRecords.length);
                }
        );



        timer.cancel();
        stopwatch.stop();
        LOGGER.info("Loading snapshot file took {} for source {} and added", stopwatch.elapsed().toMillis(), source);

        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, version.get(), sessionId[0]);
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


    public static void decompressAndProcessRecords(final byte[] compressed, Consumer<String> firstRecordProcessor,
                                                Consumer<String[]> remainingRecordProcessor){
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream, BUFFER_SIZE);
             InputStreamReader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            String[] batch = new String[BATCH_SIZE];
            int batchIndex = 0;
            boolean isFirstRecord = true;

            while ((line = bufferedReader.readLine()) != null) {
                String record = line.trim(); //each line is one record

                if (record.isEmpty()) {
                    continue;
                }
                if (isFirstRecord) {
                    firstRecordProcessor.accept(record);  // Process the first record
                    isFirstRecord = false;
                } else {
                    batch[batchIndex++] = record;
                    if (batchIndex == BATCH_SIZE) {
                        remainingRecordProcessor.accept(batch);
                        batch = new String[BATCH_SIZE];
                        batchIndex = 0;
                    }
                }
            }
            if (batchIndex > 0) {
                remainingRecordProcessor.accept(Arrays.copyOf(batch, batchIndex));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return encodeHexString(encodedSha256hex);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String byteArrayToHexString(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
