package net.ripe.db.nrtm4.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Repository
public class WhoisDao {

    private final WhoisObjectRepository whoisObjectRepository;

    WhoisDao(
        final WhoisObjectRepository whoisObjectRepository
    ) {
        this.whoisObjectRepository = whoisObjectRepository;
    }

    @Transactional
    public InitialSnapshotState getInitialSnapshotState() {
        final int lastSerialId = whoisObjectRepository.getLastSerialId();
        final List<RpslObjectData> objects = whoisObjectRepository.getAllObjectsFromLast();
        return new InitialSnapshotState(lastSerialId, objects);
    }

    public Map<Integer, String> findRpslMapForObjects(final List<RpslObjectData> objects) {
        final Map<Integer, String> results = whoisObjectRepository.findRpslMapForLastObjects(objects);
        if (objects.size() == results.size()) {
            return results;
        }
        for (final RpslObjectData object : objects) {
            if (!results.containsKey(object.objectId())) {
                results.put(object.objectId(), whoisObjectRepository.findRpslForHistoryObject(object.objectId(), object.sequenceId()));
            }
        }
        return results;
    }

}
