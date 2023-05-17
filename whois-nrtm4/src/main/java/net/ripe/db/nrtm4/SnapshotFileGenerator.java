package net.ripe.db.nrtm4;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SourceRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import static java.util.stream.Collectors.groupingBy;
import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final WhoisObjectRepository whoisObjectRepository;

    private final DummifierNrtmV4 dummifierNrtmV4;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final RpslObjectEnqueuer rpslObjectEnqueuer;
    private final SnapshotFileSerializer snapshotFileSerializer;
    private final SourceRepository sourceRepository;
    private final NrtmFileRepository nrtmFileRepository;
    private final DateTimeProvider dateTimeProvider;


    public SnapshotFileGenerator(
        final DummifierNrtmV4 dummifierNrtmV4,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final RpslObjectEnqueuer rpslObjectEnqueuer,
        final WhoisObjectRepository whoisObjectRepository,
        final NrtmFileRepository nrtmFileRepository,
        final DateTimeProvider dateTimeProvider,
        final SnapshotFileSerializer snapshotFileSerializer,
        final SourceRepository sourceRepository
    ) {
        this.dummifierNrtmV4 = dummifierNrtmV4;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.rpslObjectEnqueuer = rpslObjectEnqueuer;
        this.snapshotFileSerializer = snapshotFileSerializer;
        this.sourceRepository = sourceRepository;
        this.whoisObjectRepository = whoisObjectRepository;
        this.nrtmFileRepository = nrtmFileRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void createSnapshot() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<NrtmSource> sources = getSources();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(sourceVersions.isEmpty() ? null : sourceVersions.get(0).lastSerialId());
        LOGGER.info("Found {} objects in {}", snapshotState.whoisObjectData().size(), stopwatch);

        final List<Thread> queueReaders = new ArrayList<>();
        final Map<CIString, LinkedBlockingQueue<RpslObjectData>> queueMap = new HashMap<>();
        for (final NrtmSource source : sources) {

            if(!canProceed(sourceVersions, source)) {
                LOGGER.info("skipping generation of snapshot file for source {}", source.getName());
                continue;
            }

            final NrtmVersionInfo newSnapshotVersion = getNewVersion(source, sourceVersions, snapshotState.serialId());
            LOGGER.info("Creating snapshot for {} with version {}", source.getName(), newSnapshotVersion);
            final LinkedBlockingQueue<RpslObjectData> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
            queueMap.put(source.getName(), queue);
            final RunnableFileGenerator runner = new RunnableFileGenerator(dummifierNrtmV4, nrtmFileRepository, snapshotFileSerializer, newSnapshotVersion, queue);
            final Thread queueReader = new Thread(runner);
            queueReader.setName(source.getName().toString());
            queueReaders.add(queueReader);
            queueReader.start();
        }
        new Thread(rpslObjectEnqueuer.getRunner(snapshotState, queueMap)).start();
        for (final Thread queueReader : queueReaders) {
            try {
                queueReader.join();
            } catch (final InterruptedException e) {
                LOGGER.warn("Queue reader (JSON file writer) interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Snapshot generation complete {}", stopwatch);
        cleanUpOldFiles();
    }

    private boolean canProceed(final List<NrtmVersionInfo> sourceVersions, final NrtmSource source) {
        if(!sourceVersions.isEmpty()) {
            final Optional<NrtmVersionInfo> versionInfo = sourceVersions.stream().filter((sourceVersion) -> source.getName().equals(sourceVersion.source().getName())).findFirst();
            if(versionInfo.isPresent() && versionInfo.get().type() == NrtmDocumentType.SNAPSHOT) {
                LOGGER.info("skipping generation of snapshot file for source {}, as no changes since last snapshot file", source.getName());
                return false;
            }
        }
        return true;
    }

    private List<NrtmSource> getSources() {
        final List<NrtmSource> sourceList = sourceRepository.getSources();
        if (sourceList.isEmpty()) {
            sourceRepository.createSources();
            LOGGER.info("Creating sources...");
        }
        return sourceRepository.getSources();
    }

    private  NrtmVersionInfo getNewVersion(final NrtmSource source, final List<NrtmVersionInfo> sourceVersions, final int currentSerialId) {
        final long createdTimestamp = dateTimeProvider.getCurrentDateTime().toEpochSecond(ZoneOffset.UTC);

        if (sourceVersions.isEmpty()) {
           return NrtmVersionInfo.of(source, 1L, UUID.randomUUID().toString(), NrtmDocumentType.SNAPSHOT, currentSerialId, createdTimestamp);
        }

        final Optional<NrtmVersionInfo> versionInfo = sourceVersions.stream().filter((sourceVersion) -> source.getName().equals(sourceVersion.source().getName())).findFirst();

        return versionInfo.isEmpty() ?
                NrtmVersionInfo.of(source, 1L, UUID.randomUUID().toString(), NrtmDocumentType.SNAPSHOT, currentSerialId, createdTimestamp)
                : NrtmVersionInfo.of(source, versionInfo.get().version(), versionInfo.get().sessionID(), NrtmDocumentType.SNAPSHOT, versionInfo.get().lastSerialId(), createdTimestamp) ;
    }

    private record RunnableFileGenerator(
        DummifierNrtmV4 dummifierNrtmV4,
        NrtmFileRepository nrtmFileRepository,
        SnapshotFileSerializer snapshotFileSerializer,
        NrtmVersionInfo version,
        LinkedBlockingQueue<RpslObjectData> queue
    ) implements Runnable {

        @Override
        public void run() {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
                final RpslObjectIterator rpslObjectIterator = new RpslObjectIterator(dummifierNrtmV4, queue);
                snapshotFileSerializer.writeObjectsAsJsonToOutputStream(version, rpslObjectIterator, gzOut);
            } catch (final Exception e) {
                LOGGER.error("Exception writing snapshot {}", version.source().getName(), e);
                Thread.currentThread().interrupt();
                return;
            }
            try {
                final String fileName = NrtmFileUtil.newGzFileName(version);
                LOGGER.info("Source {} snapshot file {}", version.source().getName(), fileName);
                Stopwatch stopwatch = Stopwatch.createStarted();
                final byte[] bytes = bos.toByteArray();
                LOGGER.info("Calculated hash for {} in {}", version.source().getName(), stopwatch);
                stopwatch = Stopwatch.createStarted();
                nrtmFileRepository.saveSnapshotVersion(version, fileName, calculateSha256(bytes), bytes);
                LOGGER.info("Wrote {} to DB {}", version.source().getName(), stopwatch);
            } catch (final Throwable t) {
                LOGGER.error("Unexpected throwable caught when inserting snapshot file", t);
            }
        }
    }

    private void cleanUpOldFiles() {
        LOGGER.info("Deleting old snapshot files");

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoRepository.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        versionsBySource.forEach( (nrtmSource, versions) -> {
            if(versions.size() > 2) {
                nrtmFileRepository.deleteSnapshotFiles(versions.subList(2, versions.size()).stream().map(NrtmVersionInfo::id).toList());
            }
        });
    }
}
