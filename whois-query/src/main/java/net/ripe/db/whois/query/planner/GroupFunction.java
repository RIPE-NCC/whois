package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;

import java.util.function.Function;

interface GroupFunction extends Function<ResponseObject, Iterable<? extends ResponseObject>> {
    Iterable<? extends ResponseObject> getGroupedAfter();
}
