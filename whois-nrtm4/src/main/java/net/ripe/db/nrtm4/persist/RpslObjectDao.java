package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao;
import net.ripe.db.whois.common.dao.jdbc.SerialRpslObjectTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao.RpslObjectsTableName.HISTORY;
import static net.ripe.db.whois.common.dao.jdbc.JdbcVersionDao.RpslObjectsTableName.LAST;


@Service
public class RpslObjectDao {

    private final JdbcVersionDao versionDao;

    public RpslObjectDao(
        final JdbcVersionDao versionDao
    ) {
        this.versionDao = versionDao;
    }

    public List<SerialRpslObjectTuple> findSerialsAndObjectsSinceSerial(final int serialId) {
        final List<SerialRpslObjectTuple> lastObjects = versionDao.findSerialsAndRpslObjectsFromTableSinceSerialId(LAST, serialId);
        final List<SerialRpslObjectTuple> historyObjects = versionDao.findSerialsAndRpslObjectsFromTableSinceSerialId(HISTORY, serialId);
        final List<SerialRpslObjectTuple> allObjects = new ArrayList<>(lastObjects.size() + historyObjects.size());
        allObjects.addAll(lastObjects);
        allObjects.addAll(historyObjects);
        allObjects.sort(Comparator.comparingInt(entity -> entity.getSerial().getSerialId()));
        return allObjects;
    }

}
