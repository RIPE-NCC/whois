package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.InitialSnapshotState;
import net.ripe.db.nrtm4.domain.RpslObjectData;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Repository
public class WhoisObjectRepository {

    private final WhoisObjectDao whoisObjectDao;

    WhoisObjectRepository(
        final WhoisObjectDao whoisObjectDao
    ) {
        this.whoisObjectDao = whoisObjectDao;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public InitialSnapshotState getInitialSnapshotState() {
        final int lastSerialId = whoisObjectDao.getLastSerialId();
        final List<RpslObjectData> objects = whoisObjectDao.getAllObjectsFromLast();
        return new InitialSnapshotState(lastSerialId, objects);
    }

    public Map<Integer, String> findRpslMapForObjects(final List<RpslObjectData> objects) {
        final Map<Integer, String> results = whoisObjectDao.findRpslMapForLastObjects(objects);
        if (objects.size() == results.size()) {
            return results;
        }
        for (final RpslObjectData object : objects) {
            if (!results.containsKey(object.objectId())) {
                results.put(object.objectId(), whoisObjectDao.findRpslForHistoryObject(object.objectId(), object.sequenceId()));
            }
        }
        return results;
    }

}
