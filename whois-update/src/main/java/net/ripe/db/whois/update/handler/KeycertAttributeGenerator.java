package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapper;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeycertAttributeGenerator extends AttributeGenerator {
    private final KeyWrapperFactory keyWrapperFactory;

    @Autowired
    public KeycertAttributeGenerator(final KeyWrapperFactory keyWrapperFactory) {
        this.keyWrapperFactory = keyWrapperFactory;
    }

    public RpslObject generateAttributes(final RpslObject originalObject, final Update update, final UpdateContext updateContext) {
        return generateAttributes(originalObject, originalObject, update, updateContext);
    }

    @Override
    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        switch (updatedObject.getType()) {
            case KEY_CERT:
                return generateKeycertAttributesForKeycert(updatedObject, update, updateContext);
            default:
                return updatedObject;
        }
    }

    private RpslObject generateKeycertAttributesForKeycert(final RpslObject object, final Update update, final UpdateContext updateContext) {
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
}
