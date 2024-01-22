package net.ripe.db.nrtm4.generator;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.nrtm4.GzipOutStreamWriter;
import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
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

import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;

@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    public static final int BATCH_SIZE = 100;
    private final WhoisObjectRepository whoisObjectRepository;
    private final DummifierNrtmV4 dummifierNrtmV4;
    private final NrtmVersionInfoDao nrtmVersionInfoDao;
    private final NrtmSourceDao nrtmSourceDao;
    private final UpdateNrtmFileRepository updateNrtmFileRepository;
    private final DateTimeProvider dateTimeProvider;
    private final NrtmKeyConfigDao nrtmKeyConfigDao;


    public SnapshotFileGenerator(
        final DummifierNrtmV4 dummifierNrtmV4,
        final NrtmVersionInfoDao nrtmVersionInfoDao,
        final WhoisObjectRepository whoisObjectRepository,
        final UpdateNrtmFileRepository updateNrtmFileRepository,
        final DateTimeProvider dateTimeProvider,
        final NrtmKeyConfigDao nrtmKeyConfigDao,
        final NrtmSourceDao nrtmSourceDao
    ) {
        this.dummifierNrtmV4 = dummifierNrtmV4;
        this.nrtmVersionInfoDao = nrtmVersionInfoDao;
        this.nrtmSourceDao = nrtmSourceDao;
        this.whoisObjectRepository = whoisObjectRepository;
        this.updateNrtmFileRepository = updateNrtmFileRepository;
        this.dateTimeProvider = dateTimeProvider;
        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
    }

    public void createSnapshot()  {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        initializeNrtm();

        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoDao.findLastVersionPerSource();

        final SnapshotState snapshotState = whoisObjectRepository.getSnapshotState(sourceVersions.isEmpty() ? null : sourceVersions.get(0).lastSerialId());
        LOGGER.info("Found {} objects in {}", snapshotState.whoisObjectData().size(), stopwatch);

        final List<NrtmVersionInfo> sourceToNewVersion = nrtmSourceDao.getSources().stream()
                .filter( source -> canProceed(sourceVersions, source))
                .map( source -> getNewVersion(source, sourceVersions, snapshotState.serialId()))
                .collect(Collectors.toList());

        final Map<CIString, byte[]> sourceToOutputBytes = writeToGzipStream(snapshotState, sourceToNewVersion);

        saveToDatabase(sourceToNewVersion, sourceToOutputBytes);
        LOGGER.info("Snapshot generation complete {}", stopwatch);

        cleanUpOldFiles();
    }

    private Map<CIString, byte[]> writeToGzipStream(final SnapshotState snapshotState, final List<NrtmVersionInfo> sourceToNewVersion) {
        final Map<CIString, GzipOutStreamWriter> sourceResources = initializeResources(sourceToNewVersion);

        final AtomicInteger noOfBatchesProcessed = new AtomicInteger(0);
        final List<List<WhoisObjectData>> batches = Lists.partition(snapshotState.whoisObjectData(), BATCH_SIZE);

        final Timer timer = new Timer(true);
        printProgress(noOfBatchesProcessed, snapshotState.whoisObjectData().size(), timer);

        try {
            batches.parallelStream().map(objectBatch -> {
                final List<RpslObject> rpslObjects = Lists.newArrayList();

                whoisObjectRepository.findRpslMapForObjects(objectBatch).values().forEach( object -> {
                    try {
                        final RpslObject rpslObject = RpslObject.parse(object);
                        if (dummifierNrtmV4.isAllowed(rpslObject)) {
                            rpslObjects.add(dummifierNrtmV4.dummify(rpslObject));
                        }
                    } catch (final Exception e) {
                        LOGGER.warn("Parsing RPSL threw exception", e);
                    }
                });

                noOfBatchesProcessed.incrementAndGet();
                return rpslObjects;
            }).flatMap(Collection::stream).forEach(rpslObject -> {
                if(sourceResources.containsKey(rpslObject.getValueForAttribute(AttributeType.SOURCE))) {
                    sourceResources.get(rpslObject.getValueForAttribute(AttributeType.SOURCE)).write(new SnapshotFileRecord(rpslObject));
                }
            });

        } catch (final Exception e) {
            LOGGER.error("Error while writing snapshotfile", e);
            throw new RuntimeException(e);
        } finally {
            timer.cancel();
            sourceResources.values().forEach(GzipOutStreamWriter::close);
        }

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

    private void initializeNrtm() {
        final List<NrtmSource> sourceList = nrtmSourceDao.getSources();
        if (sourceList.isEmpty()) {
            LOGGER.info("Creating sources...");
            nrtmSourceDao.createSources();
        }

        if(!nrtmKeyConfigDao.isKeyPairExists()) {
            final AsymmetricCipherKeyPair asymmetricCipherKeyPair = Ed25519Util.generateEd25519KeyPair();
            final byte[] privateKey =((Ed25519PrivateKeyParameters) asymmetricCipherKeyPair.getPrivate()).getEncoded();
            final byte[] publicKey = ((Ed25519PublicKeyParameters) asymmetricCipherKeyPair.getPublic()).getEncoded();

            nrtmKeyConfigDao.saveKeyPair(privateKey, publicKey);
        }
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
                updateNrtmFileRepository.deleteSnapshotFiles(versions.subList(2, versions.size()).stream().map(NrtmVersionInfo::id).toList());
            }
        });
    }

    private Map<CIString, GzipOutStreamWriter> initializeResources(final List<NrtmVersionInfo> sourceToVersionInfo)  {

        final Map<CIString, GzipOutStreamWriter> resources = Maps.newHashMap();
        sourceToVersionInfo.forEach(nrtmVersionInfo -> {
            final GzipOutStreamWriter resource = new GzipOutStreamWriter();
            resource.write(new NrtmVersionRecord(nrtmVersionInfo, NrtmDocumentType.SNAPSHOT));
            resources.put(nrtmVersionInfo.source().getName(), resource);
        });

        return resources;
    }

    private void saveToDatabase(final List<NrtmVersionInfo> sourceToNewVersion,  final Map<CIString, byte[]> payloadBySource) {
        payloadBySource.forEach((source, payload) -> {
            final Optional<NrtmVersionInfo> versionInfo = sourceToNewVersion.stream().filter((version) -> version.source().getName().equals(source)).findAny();
            if (versionInfo.isPresent()) {
                try {
                    final String fileName = NrtmFileUtil.newGzFileName(versionInfo.get());
                    LOGGER.info("Source {} snapshot file {}", source, fileName);
                    LOGGER.info("Calculated hash for {}", source);
                    updateNrtmFileRepository.saveSnapshotVersion(versionInfo.get(), fileName, calculateSha256(payload), payload);
                    LOGGER.info("Wrote {} to DB {}", source);
                } catch (final Throwable t) {
                    LOGGER.error("Unexpected throwable caught when inserting snapshot file", t);
                }
            }
        });
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
