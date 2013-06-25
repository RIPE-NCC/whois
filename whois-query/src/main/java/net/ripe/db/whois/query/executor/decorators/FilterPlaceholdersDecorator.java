package net.ripe.db.whois.query.executor.decorators;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class FilterPlaceholdersDecorator implements ResponseDecorator {
    private final SourceContext sourceContext;
    private final AuthoritativeResourceData authoritativeResourceData;

    @Autowired
    public FilterPlaceholdersDecorator(SourceContext sourceContext, AuthoritativeResourceData authoritativeResourceData) {
        this.sourceContext = sourceContext;
        this.authoritativeResourceData = authoritativeResourceData;
    }

    public Iterable<? extends ResponseObject> decorate(final Query query, final Iterable<? extends ResponseObject> input) {
        if (!sourceContext.isVirtual()) {
            return input;
        }

        final AuthoritativeResource resourceData = authoritativeResourceData.getAuthoritativeResource(sourceContext.getCurrentSource().getName());

        final IterableTransformer<ResponseObject> responseObjects = new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(final ResponseObject input, final Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                    return;
                }

                final RpslObject object = (RpslObject) input;
                if (resourceData.isMaintainedInRirSpace(object)) {
                    result.add(object);
                }
            }
        };

        return responseObjects;
    }
}
