package net.ripe.db.nrtm4.client.importer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.client.client.MirrorSnapshotInfo;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotMirrorImporter extends AbstractMirrorImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotMirrorImporter.class);

    private final NrtmRestClient nrtmRestClient;


    public SnapshotMirrorImporter(final NrtmRestClient nrtmRestClient,
                                  final Nrtm4ClientInfoRepository nrtm4ClientInfoMirrorDao,
                                  final Nrtm4ClientRepository nrtm4ClientRepository) {
        super(nrtm4ClientInfoMirrorDao, nrtm4ClientRepository);
        this.nrtmRestClient = nrtmRestClient;

    }


    public void doImport(final String source,
                         final String sessionId,
                         final UpdateNotificationFileResponse.NrtmFileLink snapshot){

        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            return;
        }

        final byte[] payload = nrtmRestClient.getSnapshotFile(snapshot.getUrl());

        final String payloadHash = calculateSha256(payload);
        if (!snapshot.getHash().equals(calculateSha256(payload))){
            LOGGER.error("Snapshot hash {} doesn't match the payload {}, skipping import", snapshot.getHash(), payloadHash);
            return;
        }

        final AtomicInteger processedCount = new AtomicInteger(0);
        final Timer timer = new Timer();
        printProgress(timer, processedCount);

        try {
            GzipDecompressor.decompressRecords(
                    payload,
                    firstRecord -> processMetadata(source, sessionId, firstRecord),
                    recordBatches -> persistBatches(recordBatches, processedCount)
            );
        } catch (IllegalArgumentException ex){
            return;
        }

        timer.cancel();
        stopwatch.stop();

        LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch.elapsed().toMillis(),
                source, processedCount);
    }

    private void printProgress(final Timer timer, final AtomicInteger processedCount) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Records processed so far: {}", processedCount.get());
            }
        }, 0, 10000);
    }

    private void persistBatches(final String[] remainingRecords,
                                final AtomicInteger processedCount) {
        Arrays.stream(remainingRecords).parallel().forEach(record -> {
            try {
                final MirrorSnapshotInfo mirrorRpslObject = new ObjectMapper().readValue(record, MirrorSnapshotInfo.class);
                final Map.Entry<RpslObject, RpslObjectUpdateInfo> persistedRecord = nrtm4ClientRepository.processSnapshotRecord(mirrorRpslObject);
                nrtm4ClientRepository.createIndexes(persistedRecord.getKey(), persistedRecord.getValue());
                processedCount.incrementAndGet();
            } catch (JsonProcessingException e) {
                LOGGER.error("Unable to parse record {}", record, e);
                throw new IllegalStateException(e);
            }
        });

    }

    private void processMetadata(final String source, final String updateNotificationSessionId,
                                 final String firstRecord) throws IllegalArgumentException {
        final JSONObject jsonObject = new JSONObject(firstRecord);
        final int version = jsonObject.getInt("version");
        final String sessionId = jsonObject.getString("session_id");
        if (!sessionId.equals(updateNotificationSessionId)) {
            LOGGER.error("The session is not the same in the UNF and snapshot");
            truncateTables();
            throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
        }
        nrtm4ClientInfoRepository.saveSnapshotFileVersion(source, version, sessionId);
    }
}
