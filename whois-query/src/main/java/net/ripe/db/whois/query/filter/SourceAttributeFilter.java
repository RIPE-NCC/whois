package net.ripe.db.whois.query.filter;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Deque;

@Component
public class SourceAttributeFilter implements AttributeFilter {

    @Override
    public Iterable<? extends ResponseObject> filter(Iterable<? extends ResponseObject> responseObjects, Collection<String> values) {
        if (values.isEmpty()) { // we're interested in all sources
            return responseObjects;
        }

        return new IterableTransformer<ResponseObject>(responseObjects) {

            @Override
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (input instanceof RpslObject) {
                    final RpslObject rpslObject = (RpslObject)input;
                    final CIString source = rpslObject.getValueOrNullForAttribute(AttributeType.SOURCE);
                    if (source != null) {
                        if (!values.contains(source.toString())) {
                            return; // filter this object
                        }
                    }
                }
                result.add(input);
            }
        };
    }

}
