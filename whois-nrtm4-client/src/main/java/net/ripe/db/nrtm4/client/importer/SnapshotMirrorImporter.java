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

    @Override
    public void doImport(final String source, final UpdateNotificationFileResponse updateNotificationFile){
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final UpdateNotificationFileResponse.NrtmFileLink snapshot = updateNotificationFile.getSnapshot();

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
                    firstRecord -> processMetadata(source, updateNotificationFile, firstRecord),
                    recordBatches -> persistBatches(recordBatches, processedCount)
            );
        } catch (IllegalArgumentException ex){
            return;
        }

        persistDummyObjectIfNotExist();

        timer.cancel();
        stopwatch.stop();

        LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch.elapsed().toMillis(),
                source, processedCount);
    }


    public void persistDummyObjectIfNotExist(){
        final RpslObject dummyObject = getPlaceholderPersonObject();
        final RpslObjectUpdateInfo rpslObjectUpdateInfo = nrtm4ClientRepository.getMirroredObjectId(dummyObject.getType(), dummyObject.getKey().toString());
        if (rpslObjectUpdateInfo != null){
            return;
        }
        final RpslObjectUpdateInfo createdDummy = nrtm4ClientRepository.persistRpslObject(dummyObject);
        nrtm4ClientRepository.createIndexes(dummyObject, createdDummy);
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


    private void processMetadata(final String source, final UpdateNotificationFileResponse updateNotificationFile,
                                 final String firstRecord) throws IllegalArgumentException {
        final JSONObject jsonObject = new JSONObject(firstRecord);
        final int version = jsonObject.getInt("version");
        final String sessionId = jsonObject.getString("session_id");
        if (!sessionId.equals(updateNotificationFile.getSessionID())) {
            LOGGER.error("The session is not the same in the UNF and snapshot");
            truncateTables();
            throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
        }
        nrtm4ClientInfoRepository.saveSnapshotFileVersion(source, version, sessionId);
    }
}
