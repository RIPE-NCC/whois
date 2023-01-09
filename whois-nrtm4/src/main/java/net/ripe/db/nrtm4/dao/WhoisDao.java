package net.ripe.db.nrtm4.dao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Transactional
@Service
public class WhoisDao {

    private final WhoisObjectRepository whoisObjectRepository;

    WhoisDao(
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisObjectRepository = whoisObjectRepository;
    }

    public InitialSnapshotState getInitialSnapshotState() {
        final int lastSerialId = whoisObjectRepository.findLastSerialId();
        final List<ObjectData> objects = whoisObjectRepository.findLastObjects();
        return new InitialSnapshotState(lastSerialId, objects);
    }

    public Map<Integer, String> findRpslMapForObjects(final List<ObjectData> objects) {
        final Map<Integer, String> results = whoisObjectRepository.findRpslMapForLastObjects(objects);
        for (final ObjectData object : objects) {
            if (!results.containsKey(object.objectId())) {
                results.put(object.objectId(), whoisObjectRepository.findRpslMapForHistoryObject(object.objectId(), object.sequenceId()));
            }
        }
        return results;
    }

}
