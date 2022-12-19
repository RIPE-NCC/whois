package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.nrtm4.dao.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.dao.SnapshotObjectRepository;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


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
        final List<SerialEntry> objects = serialDao.getSerialEntriesFromLast()
            .filter((obj) -> dummifierNrtm.isAllowed(NrtmConstants.NRTM_VERSION, obj.getRpslObject()))
            .collect(Collectors.toList());
        if (objects.isEmpty()) {
            throw new IllegalStateException("init() failed because there are no objects in whois");
        }
        final int lastSerial = objects.get(objects.size() - 1).getSerialId();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialSnapshot(source, lastSerial);
        for (final SerialEntry obj : objects) {
            snapshotObjectRepository.insert(
                version.getId(),
                obj.getSerialId(),
                obj.getRpslObject().getType(),
                obj.getPrimaryKey(),
                dummifierNrtm.dummify(NrtmConstants.NRTM_VERSION, obj.getRpslObject()).toString()
            );
        }
        return version;
    }

}
