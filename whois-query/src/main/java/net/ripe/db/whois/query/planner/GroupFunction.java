package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import net.ripe.db.whois.common.domain.ResponseObject;

interface GroupFunction extends Function<ResponseObject, Iterable<ResponseObject>> {
    Iterable<ResponseObject> getGroupedAfter();
}
