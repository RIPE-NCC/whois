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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final String whoisSource;
    private final NrtmFileService nrtmFileService;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileRunner snapshotFileRunner;
    private final SnapshotFileRepository snapshotFileRepository;
    private final SourceRepository sourceRepository;

    public SnapshotFileGenerator(
        @Value("${whois.source}") final String whoisSource,
        final NrtmFileService nrtmFileService,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final SnapshotFileRunner snapshotFileRunner,
        final SnapshotFileRepository snapshotFileRepository,
        final SourceRepository sourceRepository
    ) {
        this.whoisSource = whoisSource;
        this.nrtmFileService = nrtmFileService;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectEnqueuer = rpslObjectEnqueuer;
        this.snapshotFileRunner = snapshotFileRunner;
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
            LOGGER.info("Creating snapshot for {}", sourceVersion.getSource().getName());
            final PublishableNrtmFile snapshotFile = new PublishableNrtmFile(sourceVersion);
            final String fileName = NrtmFileUtil.newGzFileName(snapshotFile);
            snapshotFile.setFileName(fileName);
            publishedFiles.add(snapshotFile);
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            queueMap.put(snapshotFile.getSource().getName(), queue);
            final Thread queueReader = new Thread(snapshotFileRunner.getRunner(snapshotFile, queue));
            queueReader.start();
            queueReaders.add(queueReader);
        }
        final Thread queueWriter = new Thread(() -> {
            try {
                final int total = state.objectData().size();
                final Timer timer = new Timer(true);
                final LinkedBlockingQueue<RpslObjectData> whoisQueue = queueMap.get(CIString.ciString(whoisSource));
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        final int done = rpslObjectEnqueuer.getDoneCount();
                        LOGGER.info("Enqueued {} RPSL objects out of {} ({}%). Queue size {}", done, total, Math.round(done * 1000. / total) / 10., whoisQueue.size());
                    }
                }, 0, 2000);
                rpslObjectEnqueuer.enrichAndEnqueueRpslObjects(state, queueMap);
                timer.cancel();
            } catch (final Exception e) {
                LOGGER.info("Exception enqueuing state", e);
                Thread.currentThread().interrupt();
            }
        });
        queueWriter.start();
        for (final Thread queueReader : queueReaders) {
            try {
                queueReader.join();
            } catch (final InterruptedException e) {
                LOGGER.info("Writer thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Generation complete {}", stopwatchRoot);
        return publishedFiles;
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSourceModel source) {
        return snapshotFileRepository.getLastSnapshot(source);
    }

}
