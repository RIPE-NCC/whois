package net.ripe.db.whois.query.planner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
class DummifyFunction implements Function<ResponseObject, ResponseObject> {
    private static final int DUMMIFIER_VERSION = 3;

    private final SourceContext sourceContext;
    private final Dummifier dummifier;

    @Autowired
    public DummifyFunction(final SourceContext sourceContext, @Qualifier("dummifierLegacy") final Dummifier dummifier) {
        this.sourceContext = sourceContext;
        this.dummifier = dummifier;
    }

    @Nullable
    @Override
    public ResponseObject apply(@Nullable final ResponseObject input) {
        if (!(input instanceof RpslObject)) {
            return input;
        }

        RpslObject rpslObject = (RpslObject) input;
        if (dummifier.isAllowed(DUMMIFIER_VERSION, rpslObject)) {
            rpslObject = dummifier.dummify(DUMMIFIER_VERSION, rpslObject);


            // TODO [AK] Setting the source to the proper value should be part of the dummifier
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

                rpslObject = new RpslObject(rpslObject.getObjectId(), newAttributes);
            }

            return rpslObject;
        }

        return null;
    }
}
