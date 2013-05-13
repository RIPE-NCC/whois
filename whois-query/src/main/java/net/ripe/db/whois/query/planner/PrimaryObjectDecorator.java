package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;

import java.util.Collection;

interface PrimaryObjectDecorator {
    boolean appliesToQuery(Query query);

    Collection<RpslObjectInfo> decorate(RpslObject rpslObject);
}
