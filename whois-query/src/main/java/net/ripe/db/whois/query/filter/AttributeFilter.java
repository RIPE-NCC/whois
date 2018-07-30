package net.ripe.db.whois.query.filter;

import net.ripe.db.whois.common.domain.ResponseObject;

import java.util.Collection;

public interface AttributeFilter {

    Iterable<? extends ResponseObject> filter(Iterable<? extends ResponseObject> responseObjects,
                                              Collection<String> values);
}
