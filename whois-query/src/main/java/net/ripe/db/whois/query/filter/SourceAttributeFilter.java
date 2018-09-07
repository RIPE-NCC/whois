package net.ripe.db.whois.query.filter;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Deque;

@Component
public class SourceAttributeFilter implements AttributeFilter {

    @Override
    public Iterable<? extends ResponseObject> filter(final Iterable<? extends ResponseObject> responseObjects,
                                                     final Collection<String> filterValues,
                                                     final SourceContext sourceContext) {
        if (!sourceContext.isMain() || filterValues.isEmpty()) {
            return responseObjects;
        }

        return new IterableTransformer<ResponseObject>(responseObjects) {

            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (input instanceof RpslObject) {
                    final RpslObject rpslObject = (RpslObject)input;
                    final CIString source = rpslObject.getValueOrNullForAttribute(AttributeType.SOURCE);
                    if (source != null) {
                        if (!filterValues.contains(source.toString())) {
                            return; // filter this object
                        }
                    }
                }
                result.add(input);
            }
        };
    }

}
