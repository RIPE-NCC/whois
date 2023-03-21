package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final Dummifier dummifierNrtm;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotGenerationWindow snapshotGenerationWindow;
    private final SourceRepository sourceRepository;

    public SnapshotFileGenerator(
        final Dummifier dummifierNrtm,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SnapshotGenerationWindow snapshotGenerationWindow,
        final SourceRepository sourceRepository
    ) {
        this.dummifierNrtm = dummifierNrtm;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectEnqueuer = rpslObjectEnqueuer;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotGenerationWindow = snapshotGenerationWindow;
        this.sourceRepository = sourceRepository;
    }

    public List<NrtmVersionInfo> createSnapshots(final SnapshotState state) {
        // Get last version from database.
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        LOGGER.info("Found {} objects", state.whoisObjectData().size());
        if (sourceVersions.isEmpty()) {
            LOGGER.info("Initializing NRTM");
            for (final NrtmSourceModel source : sourceRepository.getSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                sourceVersions.add(version);
            }
        } else {
            if (!snapshotGenerationWindow.isInWindow()) {
                return List.of();
            }
            sourceVersions.removeIf(versionToRemove ->
                versionToRemove.type() == NrtmDocumentType.SNAPSHOT);
            if (sourceVersions.isEmpty()) {
                return List.of();
            }
            sourceVersions.removeIf(versionToRemove -> !snapshotGenerationWindow.hasVersionExpired(
                nrtmVersionInfoRepository.findLastSnapshotVersionForSource(
                    versionToRemove.source()
                ))
            );
            if (sourceVersions.isEmpty()) {
                return List.of();
            }
        }
        return createSnapshotsForVersions(state, sourceVersions.stream().map(nrtmVersionInfoRepository::saveNewSnapshotVersion).toList());
    }

    public List<NrtmVersionInfo> createInitialSnapshots(final SnapshotState state) {
        sourceRepository.createSources();
        final List<NrtmVersionInfo> sourceVersions = new ArrayList<>();
        for (final NrtmSourceModel source : sourceRepository.getSources()) {
            final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
            sourceVersions.add(version);
        }
        return createSnapshotsForVersions(state, sourceVersions);
    }

    List<NrtmVersionInfo> createSnapshotsForVersions(final SnapshotState state, final List<NrtmVersionInfo> snapshotVersions) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<Thread> queueReaders = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        for (final NrtmVersionInfo snapshotVersion : snapshotVersions) {
            LOGGER.info("Creating snapshot for {}", snapshotVersion.source().getName());
            final PublishableNrtmFile snapshotFile = new PublishableSnapshotFile(snapshotVersion);
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            queueMap.put(snapshotFile.getSource().getName(), queue);
            final RunnableFileGenerator runner = new RunnableFileGenerator(dummifierNrtm, snapshotFileRepository, snapshotFileSerializer, snapshotVersion, queue);
            final Thread queueReader = new Thread(runner);
            queueReader.setName(snapshotFile.getSource().getName().toString());
            queueReaders.add(queueReader);
            queueReader.start();
        }
        new Thread(rpslObjectEnqueuer.getRunner(state, queueMap)).start();
        for (final Thread queueReader : queueReaders) {
            try {
                queueReader.join();
            } catch (final InterruptedException e) {
                LOGGER.warn("Queue reader (JSON file writer) interrupted", e);
                Thread.currentThread().interrupt();
            } catch (final Throwable t) {
                LOGGER.info("Queue reader (JSON file writer) threw unexpected exception", t);
            }
        }
        LOGGER.info("Snapshot generation complete {}", stopwatch);
        return snapshotVersions;
    }

    private record RunnableFileGenerator(
        Dummifier dummifierNrtm,
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
                snapshotFileSerializer.writeObjectsAsJsonToOutputStream(version, rpslObjectIterator, gzOut);
            } catch (final Exception e) {
                LOGGER.error("Exception writing snapshot {}", version.source().getName(), e);
                Thread.currentThread().interrupt();
                return;
            }
            final String fileName = NrtmFileUtil.newGzFileName(version);
            LOGGER.info("Source {} snapshot file {}", version.source().getName(), fileName);
            Stopwatch stopwatch = Stopwatch.createStarted();
            LOGGER.info("Calculated hash for {} in {}", version.source().getName(), stopwatch);
            stopwatch = Stopwatch.createStarted();
            try {
                final byte[] bytes = bos.toByteArray();
                final SnapshotFile snapshotFile = SnapshotFile.of(version().id(), fileName, calculateSha256(bytes));
                snapshotFileRepository.insert(snapshotFile, bytes);
                LOGGER.info("Wrote {} to DB {}", version.source().getName(), stopwatch);
            } catch (final Throwable t) {
                LOGGER.error("Unexpected throwable caught when inserting snapshot file", t);
            }
        }

    }

}
