package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotFileRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final String whoisSource;
    private final NrtmFileService nrtmFileService;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SourceRepository sourceRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    public SnapshotFileGenerator(
        @Value("${whois.source}")
        final String whoisSource,
        final NrtmFileService nrtmFileService,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final SnapshotFileRepository snapshotFileRepository,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SourceRepository sourceRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisSource = whoisSource;
        this.nrtmFileService = nrtmFileService;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectEnqueuer = rpslObjectEnqueuer;
        this.snapshotFileRepository = snapshotFileRepository;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public Set<PublishableNrtmFile> createSnapshots() {
        final Stopwatch stopwatchRoot = Stopwatch.createStarted();
        // Get last version from database.
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        final SnapshotState state = whoisObjectRepository.getSnapshotState();
        LOGGER.info("NRTM found {} objects in {}", state.objectData().size(), stopwatchRoot);
        if (sourceVersions.isEmpty()) {
            LOGGER.info("Initializing NRTM");
            for (final NrtmSourceModel source : sourceRepository.getAllSources()) {
                final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialVersion(source, state.serialId());
                nrtmFileService.createNrtmSessionDirectory(version.getSessionID());
                sourceVersions.add(version);
            }
        }
        // TODO: else see if there are any deltas for each source. if so then add a sourceVersion to this list,
        //   otherwise skip the snapshot
        final List<Thread> queueReaders = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        final Set<PublishableNrtmFile> publishedFiles = new HashSet<>();
        for (final NrtmVersionInfo sourceVersion : sourceVersions) {
            LOGGER.info("NRTM creating snapshot for {}", sourceVersion.getSource().getName());
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            final PublishableNrtmFile snapshotFile = new PublishableNrtmFile(sourceVersion);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            queueMap.put(sourceVersion.getSource().getName(), queue);
            publishedFiles.add(snapshotFile);
            final Thread queueReader = new Thread(() -> {
                LOGGER.info("NRTM {} writing queue to snapshot", sourceVersion.getSource().getName());
                try (final GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
                    snapshotFileSerializer.writeObjectQueueAsSnapshot(snapshotFile, queue, gzOut);
                    gzOut.close();
                    final String fileName = NrtmFileUtil.newGzFileName(snapshotFile);
                    snapshotFile.setFileName(fileName);
                    snapshotFile.setHash(nrtmFileService.calculateSha256(bos));
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    nrtmFileService.writeToDisk(snapshotFile, bos);
                    LOGGER.info("Wrote {} {}/{} to disk in {}", snapshotFile.getSourceModel().getName(), snapshotFile.getSessionID(), snapshotFile.getFileName(), stopwatch);
                    snapshotFileRepository.insert(snapshotFile, bos.toByteArray());
                    LOGGER.info("Wrote {} to DB {}", snapshotFile.getFileName(), stopwatch);
                } catch (final Exception e) {
                    LOGGER.info("NRTM {} Exception writing snapshot", sourceVersion.getSource().getName(), e);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
            queueReader.start();
            queueReaders.add(queueReader);
        }
        final Thread queueWriter = new Thread(() -> {
            LOGGER.info("NRTM START enqueuing {} objects", state.objectData().size());
            try {
                rpslObjectEnqueuer.enrichAndEnqueueRpslObjects(state, queueMap);
            } catch (final Exception e) {
                LOGGER.info("NRTM Exception enqueuing state", e);
                Thread.currentThread().interrupt();
            }
            LOGGER.info("NRTM END enqueuing {} objects", state.objectData().size());
        });
        queueWriter.start();
        final int total = state.objectData().size();
        final Timer timer = new Timer(true);
        final LinkedBlockingQueue<RpslObjectData> whoisQueue = queueMap.get(CIString.ciString(whoisSource));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = rpslObjectEnqueuer.getDoneCount();
                LOGGER.info("NRTM RpslQueue {} of {} ({}%). Queue size {}", done, total, Math.floor((float) (done * 100) / (float) total), whoisQueue.size());
            }
        }, 0, 2000);
        for (final Thread queueReader : queueReaders) {
            try {
                queueReader.join();
            } catch (final InterruptedException e) {
                LOGGER.info("NRTM writer thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        timer.cancel();
        LOGGER.info("NRTM generation complete {}", stopwatchRoot);
        return publishedFiles;
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSourceModel source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}
