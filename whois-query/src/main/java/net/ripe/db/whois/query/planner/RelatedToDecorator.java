package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.QueryFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Component
class RelatedToDecorator implements PrimaryObjectDecorator {
    private final static Set<ObjectType> NO_PERSONAL_EXCLUDES = Sets.newEnumSet(Lists.newArrayList(ObjectType.PERSON, ObjectType.ROLE), ObjectType.class);

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
    public Collection<RpslObjectInfo> decorate(final Query query, final RpslObject rpslObject) {
        final Set<ObjectType> excludeObjectTypes = query.hasOption(QueryFlag.NO_PERSONAL)
                ? NO_PERSONAL_EXCLUDES
                : Collections.<ObjectType>emptySet();

        return rpslObjectDao.relatedTo(rpslObject, excludeObjectTypes);
    }
}
