package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
class RelatedToDecorator implements PrimaryObjectDecorator {
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public RelatedToDecorator(final RpslObjectDao rpslObjectDao) {
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public boolean appliesToQuery(final Query query) {
        return query.isReturningReferencedObjects();
    }

    @Override
    public Collection<RpslObjectInfo> decorate(final RpslObject rpslObject) {
        return rpslObjectDao.relatedTo(rpslObject);
    }
}
