package net.ripe.db.nrtm4.generator;

import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.SnapshotRecordConsumer;
import net.ripe.db.nrtm4.SnapshotRecordProducer;
import net.ripe.db.nrtm4.dao.NrtmFileRepository;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoDao;
import net.ripe.db.nrtm4.dao.NrtmSourceDao;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.SnapshotFileRecord;
import net.ripe.db.nrtm4.domain.SnapshotState;
import net.ripe.db.nrtm4.util.Ed25519Util;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.DummifierNrtmV4;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static net.ripe.db.nrtm4.util.NrtmFileUtil.calculateSha256;


@Service
public class SnapshotFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileGenerator.class);
    private static final int QUEUE_CAPACITY = 1000;

    private final WhoisObjectRepository whoisObjectRepository;

    private final DummifierNrtmV4 dummifierNrtmV4;
    private final NrtmVersionInfoDao nrtmVersionInfoDao;
    private final NrtmSourceDao nrtmSourceDao;
    private final NrtmFileRepository nrtmFileRepository;
    private final DateTimeProvider dateTimeProvider;
    private final NrtmKeyConfigDao nrtmKeyConfigDao;


    public SnapshotFileGenerator(
        final DummifierNrtmV4 dummifierNrtmV4,
        final NrtmVersionInfoDao nrtmVersionInfoDao,
        final WhoisObjectRepository whoisObjectRepository,
        final NrtmFileRepository nrtmFileRepository,
        final DateTimeProvider dateTimeProvider,
        final NrtmKeyConfigDao nrtmKeyConfigDao,
        final NrtmSourceDao nrtmSourceDao
    ) {
        this.dummifierNrtmV4 = dummifierNrtmV4;
        this.nrtmVersionInfoDao = nrtmVersionInfoDao;
        this.nrtmSourceDao = nrtmSourceDao;
        this.whoisObjectRepository = whoisObjectRepository;
        this.nrtmFileRepository = nrtmFileRepository;
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
        if(sourceToNewVersion.isEmpty()) {
            LOGGER.info("Skipping generation fo snapshot for all sources");
            return;
        }

        final LinkedBlockingQueue<SnapshotFileRecord> sharedQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

        final CompletableFuture<Void> snapshotRecordProducer = CompletableFuture.supplyAsync(new SnapshotRecordProducer(sharedQueue, dummifierNrtmV4, snapshotState, whoisObjectRepository));
        final CompletableFuture<Map<CIString, byte[]>> snapshotRecordConsumer = CompletableFuture.supplyAsync(new SnapshotRecordConsumer(sharedQueue, sourceToNewVersion));

        try {
            CompletableFuture.allOf(snapshotRecordProducer, snapshotRecordConsumer);
            final Map<CIString, byte[]> payloadBySource = snapshotRecordConsumer.get();
            saveToDatabase(sourceToNewVersion, payloadBySource);
        } catch (Exception e) {
            LOGGER.error("Unexpected throwable caught when fetching results", e);
            throw new RuntimeException(e);
        }

        LOGGER.info("Snapshot generation complete {}", stopwatch);
        cleanUpOldFiles();
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
                nrtmFileRepository.deleteSnapshotFiles(versions.subList(2, versions.size()).stream().map(NrtmVersionInfo::id).toList());
            }
        });
    }
}
