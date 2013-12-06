package net.ripe.db.whois.update.handler;

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

import java.util.ListIterator;

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

        addOrReplaceAttribute(update, updateContext, builder, AttributeType.METHOD, keyWrapper.getMethod());
        addOrReplaceAttribute(update, updateContext, builder, AttributeType.OWNER, keyWrapper.getOwner());
        addOrReplaceAttribute(update, updateContext, builder, AttributeType.FINGERPR, keyWrapper.getFingerprint());

        return builder.get();
    }

    private static void addOrReplaceAttribute(final Update update, final UpdateContext updateContext, final RpslObjectBuilder builder, final AttributeType attributeType, final String attributeValue) {
        boolean found = false;

        final ListIterator<RpslAttribute> iterator = builder.getAttributes().listIterator();
        while (iterator.hasNext()) {
            final RpslAttribute attribute = iterator.next();
            if (attribute.getType() == attributeType) {
                if (!found) {
                    if (!attribute.getValue().equals(attributeValue)) {
                        updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attribute.getType()));
                        iterator.set(new RpslAttribute(attributeType, attributeValue));
                    }
                    found = true;
                } else {
                    iterator.remove();
                }
            }
        }

        if (!found) {
            builder.addAttribute(new RpslAttribute(attributeType, attributeValue)).sort();
        }
    }
}
