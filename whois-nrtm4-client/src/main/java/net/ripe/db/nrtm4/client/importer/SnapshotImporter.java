package net.ripe.db.nrtm4.client.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    public static final String RECORD_SEPARATOR = "\u001E";

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

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


        final CompletableFuture<List<String>> future = decompressAndProcessRecords(
            payload,
            firstRecord -> {
                final JSONObject jsonObject = new JSONObject(firstRecord);
                version.set(jsonObject.getInt("version"));
                sessionId[0] = jsonObject.getString("session_id");
                if (!sessionId[0].equals(updateNotificationFile.getSessionID())){
                    // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
                    //  call initialize X number of times and return error to avoid this situation?
                    LOGGER.error("The session is not the same in the UNF and snapshot");
                    //initializeNRTMClientForSource(source, updateNotificationFile);
                    throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
                }
                LOGGER.info("Processed first record");
            },
            remainingRecord -> {
                try {
                    processObject(remainingRecord);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                processedCount.incrementAndGet();
            }
        );

        try {
            final List<String> processedRecords = future.get();
            timer.cancel();
            stopwatch.stop();
            LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch.elapsed().toMillis(),
                    source, processedRecords.size());
        } catch(InterruptedException | ExecutionException ex){
            LOGGER.error("Error when processing the future");
        }
        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, version.get(), sessionId[0]);
        /*try {
            snapshotRecords = getSnapshotRecords(payload);
        } catch (IOException e){
            LOGGER.error("No able to decompress snapshot", e);
            return;
        }
        LOGGER.info("Step 2");

        if (snapshotRecords == null){
            LOGGER.error("This cannot happen. UNF has a non-existing snapshot");
            return;
        }

        final JSONObject jsonObject = new JSONObject(snapshotRecords[0]);
        final int snapshotVersion = jsonObject.getInt("version");
        final String snapshotSessionId = jsonObject.getString("session_id");

        LOGGER.info("Step 3");
        if (!snapshot.getHash().equals(calculateSha256(payload))){
            LOGGER.error("Snapshot hash doesn't match, skipping import");
            return;
        }

        if (!snapshotSessionId.equals(updateNotificationFile.getSessionID())){
            // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
            //  call initialize X number of times and return error to avoid this situation?
            LOGGER.error("The session is not the same in the UNF and snapshot");
            //initializeNRTMClientForSource(source, updateNotificationFile);
            return;
        }

        LOGGER.info("Step 4");
        final AtomicInteger processedCount = new AtomicInteger(0);
        final Timer timer = new Timer();
        printProgress(timer, processedCount);
        Arrays.stream(snapshotRecords).skip(1)
                .parallel()
                .forEach(record -> {
                    try {
                        processObject(record);
                        processedCount.incrementAndGet();
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Unable to process record", e);
                    }
                });
        timer.cancel();
        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, snapshotVersion, snapshotSessionId);
        stopwatch.stop();
        LOGGER.info("Loading snapshot file took {} for source {}", stopwatch.elapsed().toMillis(), source);*/
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

    private static String[] getSnapshotRecords(byte[] compressed) throws IOException {
        return StringUtils.split(decompress(compressed), RECORD_SEPARATOR);
    }

    private static String decompress(final byte[] compressed) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(compressed);
         GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
         ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    public static CompletableFuture<List<String>> decompressAndProcessRecords(final byte[] compressed, Consumer<String> firstRecordProcessor, Consumer<String> remainingRecordProcessor){
        return CompletableFuture.supplyAsync(() -> {
            List<String> processedRecords = new ArrayList<>();

            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
                 GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream, BUFFER_SIZE);
                 InputStreamReader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {

                StringBuilder recordBuffer = new StringBuilder();
                String line;
                boolean isFirstRecord = true;

                while ((line = bufferedReader.readLine()) != null) {
                    recordBuffer.append(line);

                    int index;
                    while ((index = recordBuffer.indexOf(RECORD_SEPARATOR)) != -1) {
                        String record = recordBuffer.substring(0, index).trim();

                        if (!record.isEmpty()) {  // Only process non-empty records
                            if (isFirstRecord) {
                                firstRecordProcessor.accept(record);  // Process the first record
                                isFirstRecord = false;
                            } else {
                                remainingRecordProcessor.accept(record);  // Process remaining records
                            }
                            processedRecords.add(record);
                        }

                        recordBuffer.delete(0, index + 1);  // Remove processed record from buffer
                    }
                }

                // Handle any leftover data after the last "\u001E"
                if (!recordBuffer.isEmpty()) {
                    if (isFirstRecord) {
                        firstRecordProcessor.accept(recordBuffer.toString());
                    } else {
                        remainingRecordProcessor.accept(recordBuffer.toString());
                    }
                    processedRecords.add(recordBuffer.toString().trim());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return processedRecords;
        });
    }

    private static String calculateSha256(final byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] encodedSha256hex = digest.digest(bytes);
            return byteArrayToHexString(encodedSha256hex);
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
