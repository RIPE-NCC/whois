package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapper;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;

@Component
public class AttributeGenerator {
    private final KeyWrapperFactory keyWrapperFactory;

    @Autowired
    public AttributeGenerator(final KeyWrapperFactory keyWrapperFactory) {
        this.keyWrapperFactory = keyWrapperFactory;
    }

    public RpslObject generateAttributes(final RpslObject object, final Update update, final UpdateContext updateContext) {
        switch (object.getType()) {
            case KEY_CERT:
                return generateKeycertAttributes(object, update, updateContext);
            default:
                return object;
        }
    }

    private RpslObject generateKeycertAttributes(final RpslObject object, final Update update, final UpdateContext updateContext) {
        final KeyWrapper keyWrapper = keyWrapperFactory.createKeyWrapper(object, update, updateContext);
        if (keyWrapper == null) {
            return object;
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(object);

        cleanupAttributeType(update, updateContext, builder, AttributeType.METHOD, keyWrapper.getMethod());
        cleanupAttributeType(update, updateContext, builder, AttributeType.OWNER, keyWrapper.getOwners());
        cleanupAttributeType(update, updateContext, builder, AttributeType.FINGERPR, keyWrapper.getFingerprint());

        return builder.get();
    }

    private static void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final String validAttributeValue) {
        cleanupAttributeType(update, updateContext, builder, attributeType, Collections.singleton(validAttributeValue));
    }

    private static void cleanupAttributeType(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final Collection<String> validAttributeValues) {
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
                builder.addAttribute(new RpslAttribute(attributeType, attributeValue)).sort();
            }
        }
    }
}
