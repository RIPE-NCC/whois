package net.ripe.db.nrtm4.generator;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.nrtm4.GzipOutStreamWriter;
import net.ripe.db.nrtm4.SnapshotRecordProcessor;
import net.ripe.db.nrtm4.SnapshotRecordCreator;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoDao;
import net.ripe.db.nrtm4.dao.NrtmSourceDao;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmVersionRecord;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;

@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private final WhoisObjectRepository whoisObjectRepository;

    private final DummifierNrtmV4 dummifierNrtmV4;
    private final NrtmVersionInfoDao nrtmVersionInfoDao;
    private final NrtmSourceDao nrtmSourceDao;
    private final NrtmFileRepository nrtmFileRepository;
    private final DateTimeProvider dateTimeProvider;


    public SnapshotFileGenerator(
        final DummifierNrtmV4 dummifierNrtmV4,
        final NrtmVersionInfoDao nrtmVersionInfoDao,
        final WhoisObjectRepository whoisObjectRepository,
        final NrtmFileRepository nrtmFileRepository,
        final DateTimeProvider dateTimeProvider,
        final NrtmSourceDao nrtmSourceDao
    ) {
        this.dummifierNrtmV4 = dummifierNrtmV4;
        this.nrtmVersionInfoDao = nrtmVersionInfoDao;
        this.nrtmSourceDao = nrtmSourceDao;
        this.whoisObjectRepository = whoisObjectRepository;
        this.nrtmFileRepository = nrtmFileRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    public void createSnapshot()  {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoDao.findLastVersionPerSource();

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(sourceVersions.isEmpty() ? null : sourceVersions.get(0).lastSerialId());
        LOGGER.info("Found {} objects in {}", snapshotState.whoisObjectData().size(), stopwatch);


        final List<NrtmVersionInfo> sourceToNewVersion = getSources().stream()
                .filter( source -> canProceed(sourceVersions, source))
                .map( source -> getNewVersion(source, sourceVersions, snapshotState.serialId()))
                .collect(Collectors.toList());

        final Map<CIString, byte[]> sourceResources = writeToGzipStream(snapshotState, sourceToNewVersion);

        saveToDatabase(sourceToNewVersion,  sourceResources);
        LOGGER.info("Snapshot generation complete {}", stopwatch);
        cleanUpOldFiles();
    }

    private List<NrtmSource> getSources() {
        List<NrtmSource> sources = nrtmSourceDao.getSources();
        if(sources.isEmpty()) {
            nrtmSourceDao.createSources();
            return nrtmSourceDao.getSources();
        }
        return sources;
    }

    private Map<CIString, byte[]> writeToGzipStream(final SnapshotState snapshotState, final List<NrtmVersionInfo> sourceToNewVersion) {
        final Map<CIString, GzipOutStreamWriter> sourceResources = initializeResources(sourceToNewVersion);

        final AtomicInteger numberOfEnqueuedObjects = new AtomicInteger(0);
        final List<List<WhoisObjectData>> batches = Lists.partition(snapshotState.whoisObjectData(), 100);

        final Timer timer = new Timer(true);
        printProgress( numberOfEnqueuedObjects, snapshotState.whoisObjectData().size(), timer);

        try {
            batches.parallelStream().map(objectBatch -> {
                final Map<Integer, String> rpslMap = whoisObjectRepository.findRpslMapForObjects(objectBatch);
                final List<RpslObject> rpslObjects = Lists.newArrayList();

                for (final WhoisObjectData object : objectBatch) {
                    numberOfEnqueuedObjects.incrementAndGet();

                    final String rpsl = rpslMap.get(object.objectId());
                    final RpslObject rpslObject;
                    try {
                        rpslObject = RpslObject.parse(rpsl);
                    } catch (final Exception e) {
                        LOGGER.warn("Parsing RPSL threw exception", e);
                        continue;
                    }
                    if (dummifierNrtmV4.isAllowed(rpslObject)) {
                        rpslObjects.add(dummifierNrtmV4.dummify(rpslObject));

                    }
                }
                return rpslObjects;
            }).flatMap(Collection::stream).forEach(rpslObject -> {
                if(sourceResources.containsKey(rpslObject.getValueForAttribute(AttributeType.SOURCE))) {
                    try {
                        sourceResources.get(rpslObject.getValueForAttribute(AttributeType.SOURCE)).write(new SnapshotFileRecord(rpslObject));
                    } catch (IOException e) {
                        LOGGER.warn("Error while writing snapshotfile", e);
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (final Exception e) {
            LOGGER.warn("Error while writing snapshotfile", e);
            throw new RuntimeException(e);
        } finally {
            timer.cancel();
            sourceResources.values().forEach(GzipOutStreamWriter::close);
        }

        LOGGER.info("completed writing to outputstream");
        return  sourceResources.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().getOutputstream().toByteArray()));
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

    private void cleanUpOldFiles() {
        LOGGER.info("Deleting old snapshot files");

        final Map<CIString, List<NrtmVersionInfo>> versionsBySource = nrtmVersionInfoDao.getAllVersionsByType(NrtmDocumentType.SNAPSHOT).stream()
                .collect(groupingBy( versionInfo -> versionInfo.source().getName()));

        versionsBySource.forEach( (nrtmSource, versions) -> {
            if(versions.size() > 2) {
                nrtmFileRepository.deleteSnapshotFiles(versions.subList(2, versions.size()).stream().map(NrtmVersionInfo::id).toList());
            }
        });
    }

    private void printProgress(final AtomicInteger numberOfEnqueuedObjects, final int total, final Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = numberOfEnqueuedObjects.get();
                LOGGER.info("Enqueued {} RPSL objects out of {} ({}%).", done, total, Math.round(done * 1000. / total) / 10.);
            }
        }, 0, 2000);
    }

    private Map<CIString, GzipOutStreamWriter> initializeResources(final List<NrtmVersionInfo> sourceToVersionInfo)  {

        final Map<CIString, GzipOutStreamWriter> resources = Maps.newHashMap();

        sourceToVersionInfo.forEach(nrtmVersionInfo -> {
            try {
                final GzipOutStreamWriter resource = new GzipOutStreamWriter();
                resource.write(new NrtmVersionRecord(nrtmVersionInfo, NrtmDocumentType.SNAPSHOT));

                resources.put(nrtmVersionInfo.source().getName(), resource);

            } catch (IOException e) {
                LOGGER.error("Exception while creating a outputstream for {}-  {}", nrtmVersionInfo.source().getName(), e);
            }
        });

        return resources;
    }

    private void saveToDatabase(final List<NrtmVersionInfo> sourceToNewVersion,  final Map<CIString, byte[]> payloadBySource) {
        payloadBySource.forEach( (source, payload ) -> {
            final Optional<NrtmVersionInfo> versionInfo = sourceToNewVersion.stream().filter( (version) -> version.source().getName().equals(source)).findAny();
            if(versionInfo.isPresent()) {
                try {
                    final String fileName = NrtmFileUtil.newGzFileName(versionInfo.get());
                    LOGGER.info("Source {} snapshot file {}", source, fileName);
                    LOGGER.info("Calculated hash for {}", source);
                    nrtmFileRepository.saveSnapshotVersion(versionInfo.get(), fileName, calculateSha256(payload), payload);
                    LOGGER.info("Wrote {} to DB {}", source);
                } catch (final Throwable t) {
                    LOGGER.error("Unexpected throwable caught when inserting snapshot file", t);
                }
            }
        });
    }
}
