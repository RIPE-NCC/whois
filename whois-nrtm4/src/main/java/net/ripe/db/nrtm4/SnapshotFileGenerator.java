package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.domain.CIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import static net.ripe.db.nrtm4.util.ByteArrayUtil.byteArrayToHexString;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final NrtmFileStore nrtmFileStore;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectBatchEnqueuer rpslObjectBatchEnqueuer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    public SnapshotFileGenerator(
        final NrtmFileStore nrtmFileStore,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectBatchEnqueuer rpslObjectBatchEnqueuer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.nrtmFileStore = nrtmFileStore;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectBatchEnqueuer = rpslObjectBatchEnqueuer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public Set<PublishableSnapshotFile> createSnapshots() {
        // Get last version from database.
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        final SnapshotState state = whoisObjectRepository.getSnapshotState();
        if (sourceVersions.isEmpty()) {
            LOGGER.info("Initializing NRTM");
            for (final NrtmSourceModel source : sourceRepository.getAllSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                nrtmFileStore.createNrtmSessionDirectory(version.getSessionID());
                sourceVersions.add(version);
            }
        }
        // TODO: else see if there are any deltas for each source. if so then add a sourceVersion to this list,
        //   otherwise skip the snapshot
        final List<Thread> writerThreads = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        final Map<PublishableSnapshotFile, ByteArrayOutputStream> outputStreamMap = new HashMap<>();
        for (final NrtmVersionInfo sourceVersion : sourceVersions) {
            LOGGER.info("NRTM creating snapshot for {}", sourceVersion.getSource().getName());
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            final PublishableSnapshotFile snapshotFile = new PublishableSnapshotFile(sourceVersion);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            queueMap.put(sourceVersion.getSource().getName(), queue);
            outputStreamMap.put(snapshotFile, bos);
            final Thread writerThread = new Thread(() -> {
                final Stopwatch stopwatch = Stopwatch.createStarted();
                LOGGER.info("NRTM {} writing queue to snapshot", sourceVersion.getSource().getName());
                try (final GZIPOutputStream out = new GZIPOutputStream(bos)) {
                    snapshotFileSerializer.writeObjectQueueAsSnapshot(snapshotFile, queue, out);
                } catch (final IOException e) {
                    LOGGER.info("NRTM {} exception writing snapshot {}", sourceVersion.getSource().getName(), e);
                    throw new RuntimeException(e);
                } catch (final InterruptedException e) {
                    LOGGER.info("NRTM {} interrupted writing snapshot", sourceVersion.getSource().getName(), e);
                    Thread.currentThread().interrupt();
                } catch (final Exception e) {
                    LOGGER.info("NRTM {} Exception writing snapshot", sourceVersion.getSource().getName(), e);
                    Thread.currentThread().interrupt();
                }
                LOGGER.info("NRTM {} snapshot queue written in {}", sourceVersion.getSource().getName(), stopwatch);
            });
            writerThreads.add(writerThread);
        }
        new Thread(() -> {
            LOGGER.info("NRTM START enqueuing {} objects", state.objectData().size());
            try {
                rpslObjectBatchEnqueuer.enrichAndEnqueueRpslObjects(state, queueMap);
            } catch (final Exception e) {
                LOGGER.info("NRTM Exception enqueuing state", e);
                Thread.currentThread().interrupt();
            }
            LOGGER.info("NRTM END enqueuing {} objects", state.objectData().size());
        }).start();
        for (final Thread writerThread : writerThreads) {
            try {
                writerThread.start();
            } catch (final Exception e) {
                LOGGER.info("NRTM Exception start/join thread", e);
                Thread.currentThread().interrupt();
            }
        }
        for (final Thread writerThread : writerThreads) {
            try {
                writerThread.join();
            } catch (final InterruptedException e) {
                LOGGER.info("NRTM writer thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        for (final PublishableSnapshotFile snapshotFile : outputStreamMap.keySet()) {
            final String fileName = NrtmFileUtil.newFileName(snapshotFile);
            LOGGER.info("{} at version: {}", snapshotFile.getSourceModel().getName(), snapshotFile.getVersion());
            snapshotFile.setFileName(fileName);
            try (final ByteArrayOutputStream out = outputStreamMap.get(snapshotFile)) {
                Stopwatch stopwatch = Stopwatch.createStarted();
                final OutputStream fileOut = nrtmFileStore.getFileOutputStream(snapshotFile.getSessionID(), fileName);
                fileOut.write(out.toByteArray());
                fileOut.flush();
                LOGGER.info("Wrote JSON for {} in {}", snapshotFile.getSourceModel().getName(), stopwatch);
                stopwatch = Stopwatch.createStarted();
                final MessageDigest digest = MessageDigest.getInstance("SHA-256");
                final byte[] encodedSha256hex = digest.digest(out.toByteArray());
                snapshotFile.setHash(byteArrayToHexString(encodedSha256hex));
                LOGGER.info("Calculated hash for {} in {}", snapshotFile.getSourceModel().getName(), stopwatch);
                stopwatch = Stopwatch.createStarted();
                snapshotFileRepository.insert(snapshotFile, out.toByteArray());
                LOGGER.info("Inserted payload for {} in {}", snapshotFile.getSourceModel().getName(), stopwatch);
            } catch (final IOException | NoSuchAlgorithmException e) {
                LOGGER.error("Exception thrown when calculating hash of snapshot file " + snapshotFile.getSourceModel().getName(), e);
            }
        }
        return outputStreamMap.keySet();
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSourceModel source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}
