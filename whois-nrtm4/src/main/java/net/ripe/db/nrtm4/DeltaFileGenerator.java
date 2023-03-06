package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.WhoisObjectRepository;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableNrtmFile;
import net.ripe.db.whois.common.domain.serials.SerialEntry;

import java.util.List;
import java.util.Set;


public class DeltaFileGenerator {

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final WhoisObjectRepository whoisObjectRepository;

    DeltaFileGenerator(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public Set<PublishableNrtmFile> createDeltas() {
        final List<NrtmVersionInfo> sourceVersions = nrtmVersionInfoRepository.findLastVersionPerSource();
        if (sourceVersions.isEmpty()) {
            throw new RuntimeException("Cannot create deltas on an empty database");
        }
        sourceVersions.sort((v1, v2) -> v2.getLastSerialId() - v1.getLastSerialId());
        final List<SerialEntry> whoisChanges = whoisObjectRepository.getSerialEntriesSince(sourceVersions.get(0).getLastSerialId());

        return Set.of();
    }

}
