package net.ripe.db.nrtm4;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;
import net.ripe.db.nrtm4.persist.NrtmVersionInfoRepository;
import net.ripe.db.nrtm4.persist.SnapshotObjectRepository;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SnapshotInitializer {

    private final NrtmVersionInfoRepository nrtmVersionInfoRepository;
    private final SerialDao serialDao;
    private final SnapshotObjectRepository snapshotObjectRepository;
    private final JsonSerializer jsonSerializer;

    SnapshotInitializer(
        final NrtmVersionInfoRepository nrtmVersionInfoRepository,
        final SerialDao serialDao,
        final SnapshotObjectRepository snapshotObjectRepository,
        final JsonSerializer jsonSerializer
    ) {
        this.nrtmVersionInfoRepository = nrtmVersionInfoRepository;
        this.serialDao = serialDao;
        this.snapshotObjectRepository = snapshotObjectRepository;
        this.jsonSerializer = jsonSerializer;
    }

    NrtmVersionInfo init(final NrtmSource source) {
        final List<SerialEntry> objects = serialDao.getSerialEntriesFromLast();
        if (objects == null || objects.isEmpty()) {
            throw new IllegalStateException("generateSnapshot() failed because there are no objects in whois");
        }
        final int lastSerial = objects.get(objects.size() - 1).getSerialId();
        final NrtmVersionInfo version = nrtmVersionInfoRepository.createInitialSnapshot(source, lastSerial);
        final ObjectMapper mapper = new ObjectMapper();
        for (final SerialEntry obj : objects) {
            try {
                snapshotObjectRepository.insert(
                    version.getId(),
                    obj.getSerialId(),
                    obj.getRpslObject().getType(),
                    obj.getPrimaryKey(),
                    mapper.writeValueAsString(obj.getRpslObject())
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return version;
    }

}
