package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialRange;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.stereotype.Service;


@Service
public class SnapshotInitializer {

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final Dummifier dummifierNrtm;

    SnapshotInitializer(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository,
        final Dummifier dummifierNrtm
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.dummifierNrtm = dummifierNrtm;
    }

    NrtmVersionInfo init(final NrtmSource source) {
        final SerialRange serialRange = serialDao.getSerials();
        final int lastSerial = serialRange.getEnd();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialSnapshot(source, lastSerial);
        serialDao.getSerialEntriesFromLast()
            .filter((obj) -> dummifierNrtm.isAllowed(NrtmConstants.NRTM_VERSION, obj.getRpslObject()))
            .forEach(
                (obj) ->
                    snapshotObjectRepository.insert(
                        version.getId(),
                        obj.getSerialId(),
                        obj.getRpslObject().getType(),
                        obj.getPrimaryKey(),
                        dummifierNrtm.dummify(NrtmConstants.NRTM_VERSION, obj.getRpslObject()).toString()
                    )
            );
        return version;
    }

}
