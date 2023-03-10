package net.ripe.db.nrtm4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import net.ripe.db.nrtm4.dao.DeltaFileRepository;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;

@Service
public class DeltaFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileGenerator.class);

    private final DeltaFileRepository deltaFileRepository;
    private final DummifierNrtm dummifier;
    private final NrtmFileService nrtmFileService;
    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    DeltaFileGenerator(
        final DeltaFileRepository deltaFileRepository,
        final DummifierNrtm dummifier,
        final NrtmFileService nrtmFileService,
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.deltaFileRepository = deltaFileRepository;
        this.dummifier = dummifier;
        this.nrtmFileService = nrtmFileService;
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public Set<PublishableDeltaFile> createDeltas(final int serialIDTo) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            throw new RuntimeException("Cannot create deltas on an empty database");
        }
        final Map<CIString, List<DeltaChange>> deltaMap = new HashMap<>();
        sourceVersions.forEach(sv -> deltaMap.put(sv.source().getName(), new ArrayList<>()));
        final List<SerialEntry> whoisChanges = whoisObjectRepository.getSerialEntriesBetween(sourceVersions.get(0).lastSerialId(), serialIDTo);
        // iterate changes and divide objects per source
        for (final SerialEntry serialEntry : whoisChanges) {
            if (!dummifier.isAllowed(NRTM_VERSION, serialEntry.getRpslObject())) {
                continue;
            }
            final CIString source = serialEntry.getRpslObject().getValueForAttribute(AttributeType.SOURCE);
            final DeltaChange deltaChange;
            if (serialEntry.getOperation() == Operation.DELETE) {
                deltaChange = DeltaChange.delete(serialEntry.getRpslObject().getType(), serialEntry.getPrimaryKey());
            } else {
                deltaChange = DeltaChange.addModify(dummifier.dummify(NRTM_VERSION, serialEntry.getRpslObject()));
            }
            deltaMap.get(source).add(deltaChange);
        }
        final Set<PublishableDeltaFile> deltaFiles = new HashSet<>();
        for (final NrtmVersionInfo version: sourceVersions) {
            final List<DeltaChange> deltas = deltaMap.get(version.source().getName());
            if (!deltas.isEmpty()) {
                final NrtmVersionInfo newVersion = nrtmVersionInfoRepository.saveNewDeltaVersion(version, serialIDTo);
                final PublishableDeltaFile publishableDeltaFile = new PublishableDeltaFile(newVersion, deltas);
                final ObjectMapper objectMapper = new ObjectMapper();
                try {
                    final String json = objectMapper.writeValueAsString(publishableDeltaFile);
                    final String hash = NrtmFileUtil.calculateSha256(json.getBytes(StandardCharsets.UTF_8));
                    final DeltaFile deltaFile = DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json);
                    deltaFileRepository.save(deltaFile);
                    nrtmFileService.writeToDisk(newVersion.sessionID(), deltaFile.name(), json.getBytes(StandardCharsets.UTF_8));
                    deltaFiles.add(publishableDeltaFile);
                } catch (final IOException e) {
                    LOGGER.warn("Exception processing delta file {}", publishableDeltaFile.getSource().getName(), e);
                }
            }
        }
        LOGGER.info("Created delta list in {}", stopwatch);
        return deltaFiles;
    }

}
