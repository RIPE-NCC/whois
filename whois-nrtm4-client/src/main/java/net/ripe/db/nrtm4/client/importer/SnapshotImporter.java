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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private final AtomicInteger processedCount = new AtomicInteger(0);

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    private static final int BATCH_SIZE = 100;

    public static final String RECORD_SEPARATOR = "\u001E";

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
        final Stopwatch stopwatch = Stopwatch.createUnstarted();
        final UpdateNotificationFileResponse.NrtmFileLink snapshot = updateNotificationFile.getSnapshot();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            return;
        }

        //TODO: [MH] new class that works in the middle of the clientRestService and the SnapshotImporter that
        // parallelise the process
        String[] snapshotRecords;
        byte[] payload;
        try {
            payload = nrtmRestClient.getSnapshotFile(snapshot.getUrl());
            snapshotRecords = getSnapshotRecords(payload);
        } catch (IOException e){
            LOGGER.error("No able to decompress snapshot", e);
            return;
        }

        if (snapshotRecords == null){
            LOGGER.error("This cannot happen. UNF has a non-existing snapshot");
            return;
        }

        final JSONObject jsonObject = new JSONObject(snapshotRecords[0]);
        final int snapshotVersion = jsonObject.getInt("version");
        final String snapshotSessionId = jsonObject.getString("session_id");

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

        printProgress(new Timer());
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

        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, snapshotVersion, snapshotSessionId);
        LOGGER.info("Loading snapshot file took {} for source {}", stopwatch.elapsed().toMillis(), source);
    }

    private void processObject(final String record) throws JsonProcessingException {
        final MirrorRpslObject mirrorRpslObject = new ObjectMapper().readValue(record, MirrorRpslObject.class);
        nrtm4ClientMirrorDao.persistRpslObject(mirrorRpslObject.getObject());
    }

    private void printProgress(final Timer timer) {
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
        final int BUFFER_SIZE = 4096;
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
        final StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
