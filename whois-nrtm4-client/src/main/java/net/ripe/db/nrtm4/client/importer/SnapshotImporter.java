package net.ripe.db.nrtm4.client.importer;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.nrtm4.client.client.MirrorRpslObject;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.SnapshotFileResponse;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientMirrorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final Nrtm4ClientMirrorRepository nrtm4ClientMirrorDao;

    private static final int BATCH_SIZE = 100;

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
        final UpdateNotificationFileResponse.NrtmFileLink snapshot = updateNotificationFile.getSnapshot();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            return;
        }
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final SnapshotFileResponse snapshotFileResponse = nrtmRestClient.getSnapshotFile(snapshot.getUrl());
        stopwatch.stop();
        LOGGER.info("loading snapshot took {} mins", stopwatch.elapsed().toMillis());

        if (snapshotFileResponse == null){
            LOGGER.error("This cannot happen. UNF has a non-existing snapshot");
            return;
        }

        if (!snapshot.getHash().equals(snapshotFileResponse.getHash())){
            LOGGER.error("Snapshot hash doesn't match, skipping import");
            return;
        }

        if (!snapshotFileResponse.getSessionID().equals(updateNotificationFile.getSessionID())){
            // TODO: [MH] if the service is wrong for any reason...we have here a non-ending loop, we need to
            //  call initialize X number of times and return error to avoid this situation?
            LOGGER.error("The session is not the same in the UNF and snapshot");
            //initializeNRTMClientForSource(source, updateNotificationFile);
        }

        final AtomicInteger noOfBatchesProcessed = new AtomicInteger(0);
        final List<List<MirrorRpslObject>> batches = Lists.partition(snapshotFileResponse.getObjects(), BATCH_SIZE);
        final Timer timer = new Timer(true);
        printProgress(noOfBatchesProcessed, snapshotFileResponse.getObjects().size(), timer);

        try {
            batches.parallelStream().forEach(objectBatch -> {
                objectBatch.forEach(objectRecord -> {
                    nrtm4ClientMirrorDao.persistRpslObject(objectRecord.getObject());
                });
                noOfBatchesProcessed.incrementAndGet();
            });

        } catch (final Exception e) {
            LOGGER.error("Error while writing snapshotfile", e);
            throw new RuntimeException(e);
        } finally {
            timer.cancel();
        }
        nrtm4ClientMirrorDao.saveSnapshotFileVersion(source, snapshotFileResponse.getVersion(), snapshotFileResponse.getSessionID());
    }

    private void printProgress(final AtomicInteger noOfBatchesProcessed, final int total, final Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = noOfBatchesProcessed.get();
                LOGGER.info("Processed {} objects out of {} ({}%).", (done * BATCH_SIZE), total, ((done * BATCH_SIZE) * 100/ total));
            }
        }, 0, 10000);
    }
}
