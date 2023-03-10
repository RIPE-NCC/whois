package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final Dummifier dummifierNrtm;
    private final NrtmFileService nrtmFileService;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SourceRepository sourceRepository;

    public SnapshotFileGenerator(
        final Dummifier dummifierNrtm,
        final NrtmFileService nrtmFileService,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SourceRepository sourceRepository
    ) {
        this.dummifierNrtm = dummifierNrtm;
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
        LOGGER.info("Found {} objects in {}", state.whoisObjectData().size(), stopwatchRoot);
        if (sourceVersions.isEmpty()) {
            LOGGER.info("Initializing NRTM");
            for (final NrtmSourceModel source : sourceRepository.getAllSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                nrtmFileService.createNrtmSessionDirectory(version.sessionID());
                sourceVersions.add(version);
            }
        } else {
            sourceVersions.removeIf(versionToRemove -> versionToRemove.type() == NrtmDocumentType.SNAPSHOT);
            if (sourceVersions.isEmpty()) {
                LOGGER.info("No deltas created since last snapshot. Skipping snapshot creation");
                return Set.of();
            }
        }
        final List<Thread> queueReaders = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        final Set<PublishableNrtmFile> publishedFiles = new HashSet<>();
        for (final NrtmVersionInfo sourceVersion : sourceVersions) {
            LOGGER.info("Creating snapshot for {}", sourceVersion.source().getName());
            final String fileName = NrtmFileUtil.newGzFileName(sourceVersion);
            final PublishableNrtmFile snapshotFile = new PublishableNrtmFile(sourceVersion);
            publishedFiles.add(snapshotFile);
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            queueMap.put(snapshotFile.getSource().getName(), queue);
            final RunnableFileGenerator runner = new RunnableFileGenerator(dummifierNrtm, nrtmFileService, snapshotFileRepository, snapshotFileSerializer, sourceVersion, queue);
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

    private record RunnableFileGenerator(
        Dummifier dummifierNrtm,
        NrtmFileService nrtmFileService,
        SnapshotFileRepository snapshotFileRepository,
        SnapshotFileSerializer snapshotFileSerializer,
        NrtmVersionInfo version,
        LinkedBlockingQueue<RpslObjectData> queue
    ) implements Runnable {

        @Override
        public void run() {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
                final RpslObjectIterator rpslObjectIterator = new RpslObjectIterator(dummifierNrtm, queue);
                snapshotFileSerializer.writeObjectQueueAsSnapshot(version, rpslObjectIterator, gzOut);
            } catch (final Exception e) {
                LOGGER.error("Exception writing snapshot {}", version.source().getName(), e);
                Thread.currentThread().interrupt();
                return;
            }
            final String fileName = NrtmFileUtil.newFileName(version);
            LOGGER.info("Source {} snapshot file {}/{}", version.source().getName(), version.sessionID(), fileName);
            Stopwatch stopwatch = Stopwatch.createStarted();
            LOGGER.info("Calculated hash for {} in {}", version.source().getName(), stopwatch);
            stopwatch = Stopwatch.createStarted();
            final byte[] bytes = bos.toByteArray();
            final SnapshotFile snapshotFile = SnapshotFile.of(version().id(), fileName, calculateSha256(bytes));
            try {
                nrtmFileService.writeToDisk(version.sessionID(), fileName, bytes);
            } catch (final IOException e) {
                LOGGER.error("Error writing file to disk", e);
                return;
            }
            LOGGER.info("Wrote {} to disk in {}", version.source().getName(), stopwatch);
            snapshotFileRepository.insert(snapshotFile, bytes);
            LOGGER.info("Wrote {} to DB {}", version.source().getName(), stopwatch);
        }

    }

}
