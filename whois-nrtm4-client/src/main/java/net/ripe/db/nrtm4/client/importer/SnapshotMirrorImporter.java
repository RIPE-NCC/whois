package net.ripe.db.nrtm4.client.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.hazelcast.org.codehaus.commons.nullanalysis.NotNull;
import net.ripe.db.nrtm4.client.client.MirrorSnapshotInfo;
import net.ripe.db.nrtm4.client.client.NrtmRestClient;
import net.ripe.db.nrtm4.client.client.UpdateNotificationFileResponse;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientInfoRepository;
import net.ripe.db.nrtm4.client.dao.Nrtm4ClientRepository;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.ripe.db.nrtm4.client.config.NrtmClientTransactionConfiguration.NRTM_CLIENT_UPDATE_TRANSACTION;


@Service
@Conditional(Nrtm4ClientCondition.class)
public class SnapshotMirrorImporter extends AbstractMirrorImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotMirrorImporter.class);

    private final NrtmRestClient nrtmRestClient;

    private final PlatformTransactionManager nrtmClientUpdateTransaction;

    private final int numberOfThreads;


    public SnapshotMirrorImporter(final NrtmRestClient nrtmRestClient,
                                  final Nrtm4ClientInfoRepository nrtm4ClientInfoMirrorDao,
                                  final Nrtm4ClientRepository nrtm4ClientRepository,
                                  @Qualifier(NRTM_CLIENT_UPDATE_TRANSACTION) final PlatformTransactionManager transactionManagerNrtmClientUpdate) {
        super(nrtm4ClientInfoMirrorDao, nrtm4ClientRepository);
        this.nrtmRestClient = nrtmRestClient;
        this.nrtmClientUpdateTransaction =  transactionManagerNrtmClientUpdate;

        final int numThreads = Runtime.getRuntime().availableProcessors();
        this.numberOfThreads = Math.min(1, numThreads/4);
    }

    public void doImport(final String source,
                         final String sessionId,
                         final URI unfUri,
                         final UpdateNotificationFileResponse.NrtmFileLink snapshot){

        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (snapshot == null){
            LOGGER.error("Snapshot cannot be null in the notification file");
            throw new IllegalArgumentException("Snapshot link does not exist in the Update Notification File");
        }

        final byte[] payload = nrtmRestClient.getSnapshotFile(getUriFromRelativePath(unfUri, snapshot.getUrl()));

        final String payloadHash = calculateSha256(payload);
        if (!snapshot.getHash().equals(calculateSha256(payload))){
            LOGGER.error("Snapshot hash {} doesn't match the payload {}, skipping import", snapshot.getHash(), payloadHash);
            return;
        }

        final AtomicInteger snapshotVersion = new AtomicInteger(0);
        final AtomicInteger processedCount = new AtomicInteger(0);

        persisSnapshot(payload, sessionId, snapshotVersion, processedCount);
        persistSnapshotVersion(source, snapshotVersion.get(), sessionId);

        stopwatch.stop();
        LOGGER.info("Loading snapshot file took {} for source {} and added {} records", stopwatch, source, processedCount.get());
    }

    private void persisSnapshot(final byte[] payload, final String sessionId,
                               final AtomicInteger snapshotVersion, final AtomicInteger processedCount){
        final Timer timer = new Timer();
        printProgress(timer, processedCount);

        try {
            GzipDecompressor.decompressRecords(
                    payload,
                    firstRecord -> {
                        validateSession(sessionId, firstRecord);
                        snapshotVersion.set(extractVersion(firstRecord));
                    },
                    recordBatches -> persistBatches(recordBatches, processedCount)
            );
        } catch (Exception ex){
            LOGGER.error("Error persisting snapshot", ex);
            throw new IllegalStateException(ex);
        }

        timer.cancel();
    }

    final void persistSnapshotVersion(final String source, final int version, final String sessionId) throws IllegalArgumentException {
        nrtm4ClientInfoRepository.saveSnapshotFileVersion(source, version, sessionId);
    }

    private void printProgress(final Timer timer, final AtomicInteger processedCount) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Records processed so far: {}", processedCount.get());
            }
        }, 0, 10000);
    }

    private void persistBatches(final String[] remainingRecords, final AtomicInteger processedCount) {
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        final List<TransactionStatus> batchTransactions = processBatchAndTransactions(remainingRecords, processedCount, executor);
        batchTransactions.forEach(nrtmClientUpdateTransaction::commit);
    }

    @NotNull
    private List<TransactionStatus> processBatchAndTransactions(final String[] remainingRecords, final AtomicInteger processedCount, final ExecutorService executor){

        //Transaction annotation does not work with any threaded processing methods
        final List<TransactionStatus> transactionStatuses = Lists.newArrayList();
        final List<Future<?>> futures = Lists.newArrayList();
        try {
            Arrays.stream(remainingRecords)
                    .forEach(record -> {
                        final TransactionStatus transactionStatus = nrtmClientUpdateTransaction.getTransaction(new DefaultTransactionDefinition()); // create a transaction per record
                        futures.add(processRecord(executor, processedCount, record, transactionStatus)); // process record
                        transactionStatuses.add(transactionStatus); // keep the transaction in case there is an issue in another record in the batch
                    });

            handleFuturesErrors(futures);
        } catch (Exception e){
            transactionStatuses.forEach(nrtmClientUpdateTransaction::rollback);
            throw new IllegalStateException("Unable to persist snapshot", e);
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                executor.shutdownNow(); // Interrupted while waiting; force shutdown
                Thread.currentThread().interrupt();
            }
        }

        return transactionStatuses;
    }

    private static void handleFuturesErrors(final List<Future<?>> futures) {
        futures.forEach(future -> {
            try {
                future.get(); // Wait for the task to complete
            } catch (ExecutionException e) {
                LOGGER.error("Error during task execution", e);
                throw new IllegalStateException("Transaction failed in batch processing", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                LOGGER.error("Batch processing interrupted", e);
                throw new IllegalStateException("Batch processing was interrupted", e);
            }
        });
    }

    private Future<?> processRecord(final ExecutorService executor, final AtomicInteger processedCount, final String record, final TransactionStatus transactionStatus){

        return executor.submit(() -> {
                try {
                    final MirrorSnapshotInfo mirrorRpslObject = new ObjectMapper().readValue(record, MirrorSnapshotInfo.class);
                    final Map.Entry<RpslObject, RpslObjectUpdateInfo> persistedRecord = nrtm4ClientRepository.processSnapshotRecord(mirrorRpslObject);
                    nrtm4ClientRepository.createIndexes(persistedRecord.getKey(), persistedRecord.getValue());
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    nrtmClientUpdateTransaction.rollback(transactionStatus);
                    LOGGER.error("Unable to process record {}", record, e);
                    throw new IllegalStateException(e);
                }
            });
    }


    private void validateSession(final String updateNotificationSessionId, final String firstRecord) throws IllegalArgumentException{
        final JSONObject jsonObject = new JSONObject(firstRecord);
        final String sessionId = jsonObject.getString("session_id");
        if (!sessionId.equals(updateNotificationSessionId)) {
            LOGGER.error("The session is not the same in the UNF and snapshot");
            throw new IllegalArgumentException("The session is not the same in the UNF and snapshot");
        }
    }

    private int extractVersion(final String firstRecord){
        final JSONObject jsonObject = new JSONObject(firstRecord);
        return jsonObject.getInt("version");
    }

}
