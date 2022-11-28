package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao;
import net.ripe.db.whois.common.dao.jdbc.SerialRpslObjectTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class WhoisSlaveDao {

    private final JdbcVersionDao versionDao;

    public WhoisSlaveDao(
        final JdbcVersionDao versionDao
    ) {
        this.versionDao = versionDao;
    }

    public List<SerialRpslObjectTuple> findSerialsAndObjectsSinceSerial(final int serialId) {
        final List<SerialRpslObjectTuple> lastObjects = versionDao.findSerialsInLastSince(serialId);
        final List<SerialRpslObjectTuple> historyObjects = versionDao.findSerialsInHistorySince(serialId);
        final List<SerialRpslObjectTuple> allObjects = new ArrayList<>(lastObjects.size() + historyObjects.size());
        allObjects.addAll(lastObjects);
        allObjects.addAll(historyObjects);
        allObjects.sort(Comparator.comparingInt(pair -> pair.getSerial().getSerialId()));
        return allObjects;
    }

}
