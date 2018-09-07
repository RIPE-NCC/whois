package net.ripe.db.whois.query.filter;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.source.SourceContext;

import java.util.Collection;

public interface AttributeFilter {

    Iterable<? extends ResponseObject> filter(Iterable<? extends ResponseObject> responseObjects,
                                              Collection<String> filterValues,
                                              SourceContext sourceContext);
}
