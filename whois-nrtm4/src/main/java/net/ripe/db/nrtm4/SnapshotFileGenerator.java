package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.domain.CIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final NrtmFileService nrtmFileService;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SourceRepository sourceRepository;

    public SnapshotFileGenerator(
        final NrtmFileService nrtmFileService,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SourceRepository sourceRepository
    ) {
        this.nrtmFileService = nrtmFileService;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectEnqueuer = rpslObjectEnqueuer;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.sourceRepository = sourceRepository;
    }

    public Set<PublishableNrtmFile> createSnapshots(final SnapshotState state) {
        final Stopwatch stopwatchRoot = Stopwatch.createStarted();
        // Get last version from database.
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        LOGGER.info("Found {} objects in {}", state.objectData().size(), stopwatchRoot);
        if (sourceVersions.isEmpty()) {
            LOGGER.info("Initializing NRTM");
            for (final NrtmSourceModel source : sourceRepository.getAllSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                nrtmFileService.createNrtmSessionDirectory(version.sessionID());
                sourceVersions.add(version);
            }
        }
        // TODO: else see if there are any deltas for each source. if so then add a sourceVersion to this list,
        //   otherwise skip the snapshot
        final List<Thread> queueReaders = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        final Set<PublishableNrtmFile> publishedFiles = new HashSet<>();
        for (final NrtmVersionInfo sourceVersion : sourceVersions) {
            LOGGER.info("Creating snapshot for {}", sourceVersion.source().getName());
            final PublishableNrtmFile snapshotFile = new PublishableNrtmFile(sourceVersion);
            final String fileName = NrtmFileUtil.newGzFileName(snapshotFile);
            snapshotFile.setFileName(fileName);
            publishedFiles.add(snapshotFile);
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            queueMap.put(snapshotFile.getSource().getName(), queue);
            final RunnableFileGenerator runner = new RunnableFileGenerator(nrtmFileService, snapshotFileRepository, snapshotFileSerializer, snapshotFile, queue);
            final Thread queueReader = new Thread(runner);
            queueReader.start();
            queueReaders.add(queueReader);
        }
        new Thread(rpslObjectEnqueuer.getRunner(state, queueMap)).start();
        for (final Thread queueReader : queueReaders) {
            try {
                queueReader.join();
            } catch (final InterruptedException e) {
                LOGGER.info("Queue reader (JSON file writer) interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Generation complete {}", stopwatchRoot);
        return publishedFiles;
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSourceModel source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

    private record RunnableFileGenerator(NrtmFileService nrtmFileService, SnapshotFileRepository snapshotFileRepository,
                                         SnapshotFileSerializer snapshotFileSerializer, PublishableNrtmFile snapshotFile,
                                         LinkedBlockingQueue<RpslObjectData> queue) implements Runnable {

        @Override
        public void run() {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
                snapshotFileSerializer.writeObjectQueueAsSnapshot(snapshotFile, queue, gzOut);
                gzOut.close();
                LOGGER.info("Source {} snapshot file {}/{}", snapshotFile.getSource().getName(), snapshotFile.getSessionID(), snapshotFile.getFileName());
                Stopwatch stopwatch = Stopwatch.createStarted();
                snapshotFile.setHash(NrtmFileUtil.calculateSha256(bos.toByteArray()));
                LOGGER.info("Calculated hash for {} in {}", snapshotFile.getSource().getName(), stopwatch);
                stopwatch = Stopwatch.createStarted();
                nrtmFileService.writeToDisk(snapshotFile, bos);
                LOGGER.info("Wrote {} to disk in {}", snapshotFile.getSource().getName(), stopwatch);
                snapshotFileRepository.insert(snapshotFile, bos.toByteArray());
                LOGGER.info("Wrote {} to DB {}", snapshotFile.getSource().getName(), stopwatch);
            } catch (final Exception e) {
                LOGGER.warn("Exception writing snapshot {}", snapshotFile.getSource().getName(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

    }

}
