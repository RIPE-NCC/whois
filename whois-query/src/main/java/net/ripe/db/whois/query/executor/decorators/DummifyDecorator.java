package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;

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
            public void apply(ResponseObject input, Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                    return;
                }

                RpslObject rpslObject = (RpslObject) input;
                if (!dummifier.isAllowed(DUMMIFIER_VERSION, rpslObject)) {
                    return;
                }

                rpslObject = dummifier.dummify(DUMMIFIER_VERSION, rpslObject);

                // TODO Setting the source should not be this complex
                final String source = sourceContext.getCurrentSource().getName().toString();
                final RpslAttribute sourceAttribute = rpslObject.findAttribute(AttributeType.SOURCE);
                if (!source.equals(sourceAttribute.getCleanValue().toString())) {
                    final List<RpslAttribute> attributes = rpslObject.getAttributes();
                    final List<RpslAttribute> newAttributes = Lists.newArrayListWithCapacity(attributes.size());
                    for (final RpslAttribute attribute : attributes) {
                        if (attribute.equals(sourceAttribute)) {
                            newAttributes.add(new RpslAttribute(AttributeType.SOURCE, source));
                        } else {
                            newAttributes.add(attribute);
                        }
                    }

                    rpslObject = new RpslObject(rpslObject, newAttributes);
                }

                result.add(rpslObject);
            }
        };
    }
}