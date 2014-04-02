package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

public class AbstractAttributeGenerator {

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final String validAttributeValue) {
        cleanupAttributeType(update, updateContext, builder, attributeType, Collections.singleton(validAttributeValue));
    }

    protected void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final Collection<String> validAttributeValues) {
        final Set<String> found = Sets.newHashSet();

        final ListIterator<RpslAttribute> iterator = builder.getAttributes().listIterator();
        while (iterator.hasNext()) {
            final RpslAttribute attribute = iterator.next();
            if (attribute.getType() == attributeType) {
                final String attributeValue = attribute.getValue().trim();

                if (!found.contains(attributeValue)) {
                    if (validAttributeValues.contains(attributeValue)) {
                        // matched valid attribute
                        found.add(attributeValue);
                    } else {
                        // remove invalid attribute
                        updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attributeType));
                        iterator.remove();
                    }
                } else {
                    // remove duplicate attribute
                    iterator.remove();
                }
            }
        }

        for (String attributeValue : validAttributeValues) {
            if (!found.contains(attributeValue)) {
                // add missing attribute
                builder.append(new RpslAttribute(attributeType, attributeValue)).sort();
            }
        }
    }
}
