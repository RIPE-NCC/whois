package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
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

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final CIString... validAttributeValues) {
        cleanupAttributeType(update, updateContext, builder, attributeType, Arrays.asList(validAttributeValues));
    }

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final Collection<CIString> validAttributeValues) {
        final Set<CIString> found = Sets.newHashSet();

        final Iterator<RpslAttribute> iterator = builder.getAttributes().iterator();
        while (iterator.hasNext()) {
            final RpslAttribute attribute = iterator.next();
            if (attribute.getType() == attributeType) {
                final CIString attributeValue = attribute.getCleanValue();

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

        for (CIString attributeValue : validAttributeValues) {
            if (!found.contains(attributeValue)) {
                builder.append(new RpslAttribute(attributeType, attributeValue)).sort();
            }
        }
    }
}
