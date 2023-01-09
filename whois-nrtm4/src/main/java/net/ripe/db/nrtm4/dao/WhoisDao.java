package net.ripe.db.nrtm4.dao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Transactional
@Service
public class WhoisDao {

    private final WhoisSerialRepository whoisSerialRepository;

    WhoisDao(
        final WhoisSerialRepository whoisSerialRepository
    ) {
        this.whoisSerialRepository = whoisSerialRepository;
    }

    public InitialSnapshotState getInitialSnapshotState() {
        final int lastSerialId = whoisSerialRepository.findLastSerialId();
        final List<ObjectData> objects = whoisSerialRepository.findLastObjects();
        return new InitialSnapshotState(lastSerialId, objects);
    }

    public Map<Integer, String> findRpslMapForObjects(final List<ObjectData> objects) {
        final Map<Integer, String> results = whoisSerialRepository.findRpslMapForLastObjects(objects);
        for (final ObjectData object : objects) {
            if (!results.containsKey(object.objectId())) {
                results.put(object.objectId(), whoisSerialRepository.findRpslMapForHistoryObject(object.objectId(), object.sequenceId()));
            }
        }
        return results;
    }

}
