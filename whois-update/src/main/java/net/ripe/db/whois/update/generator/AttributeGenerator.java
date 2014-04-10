package net.ripe.db.whois.update.generator;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class AttributeGenerator {

    public abstract RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext);

    protected RpslObject cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObject rpslObject, final AttributeType attributeType, final String... validAttributeValues) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
        cleanupAttributeType(update, updateContext, builder, attributeType, Arrays.asList(validAttributeValues));
        return builder.get();
    }

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final String... validAttributeValues) {
        cleanupAttributeType(update, updateContext, builder, attributeType, Arrays.asList(validAttributeValues));
    }

    protected RpslObject cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObject rpslObject, final AttributeType attributeType, final Collection<String> validAttributeValues) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
        cleanupAttributeType(update, updateContext, builder, attributeType, validAttributeValues);
        return builder.get();
    }

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final Collection<String> validAttributeValues) {
        final Set<String> found = Sets.newHashSet();

        final Iterator<RpslAttribute> iterator = builder.getAttributes().iterator();
        while (iterator.hasNext()) {
            final RpslAttribute attribute = iterator.next();
            if (attribute.getType() == attributeType) {
                final String attributeValue = attribute.getValue().trim();

                if (found.contains(attributeValue)) {
                    iterator.remove();
                    continue;
                }

                found.add(attributeValue);

                if (!validAttributeValues.contains(attributeValue)) {
                    updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attributeType));
                    iterator.remove();
                }
            }
        }

        for (String attributeValue : validAttributeValues) {
            if (!found.contains(attributeValue)) {
                builder.addAttribute(1, new RpslAttribute(attributeType, attributeValue));
            }
        }
    }
}
