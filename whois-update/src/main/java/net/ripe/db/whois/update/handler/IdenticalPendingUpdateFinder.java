package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.PendingUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.List;

@Component
public class IdenticalPendingUpdateFinder {
    private final PendingUpdateDao pendingUpdateDao;

    @Autowired
    public IdenticalPendingUpdateFinder(final PendingUpdateDao pendingUpdateDao) {
        this.pendingUpdateDao = pendingUpdateDao;
    }

    @CheckForNull
    public PendingUpdate find(final RpslObject object) {
        final List<PendingUpdate> result = pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString());
        for (final PendingUpdate update : result) {
            final RpslObjectBase objectBase = update.getObject();

            if (objectBase.getAttributes().size() == object.getAttributes().size() &&
                    objectBase.getAttributes().containsAll(object.getAttributes())) {
                return update;
            }
        }
        return null;
    }
}
