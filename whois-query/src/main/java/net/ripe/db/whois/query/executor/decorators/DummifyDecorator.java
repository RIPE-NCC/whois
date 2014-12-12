package net.ripe.db.whois.query.executor.decorators;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Deque;

@Component
public class DummifyDecorator implements ResponseDecorator {
    private static final int DUMMIFIER_VERSION = 3;

    private final SourceContext sourceContext;
    private final Dummifier dummifier;

    @Autowired
    public DummifyDecorator(SourceContext sourceContext, @Qualifier("dummifierLegacy") final Dummifier dummifier) {
        this.sourceContext = sourceContext;
        this.dummifier = dummifier;
    }

    @Override
    public Iterable<? extends ResponseObject> decorate(final Query query, final Iterable<? extends ResponseObject> input) {
        if (!sourceContext.isDummificationRequired()) {
            return input;
        }

        return new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(final ResponseObject input, final Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                    return;
                }

                RpslObject rpslObject = (RpslObject) input;
                if (!dummifier.isAllowed(DUMMIFIER_VERSION, rpslObject)) {
                    return;
                }

                rpslObject = dummifier.dummify(DUMMIFIER_VERSION, rpslObject);

                final CIString source = sourceContext.getCurrentSource().getName();
                final RpslAttribute sourceAttribute = rpslObject.findAttribute(AttributeType.SOURCE);
                rpslObject = new RpslObjectBuilder(rpslObject)
                        .replaceAttribute(sourceAttribute, new RpslAttribute(AttributeType.SOURCE, source))
                        .get();
                result.add(rpslObject);
            }
        };
    }
}