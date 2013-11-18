package net.ripe.db.whois.update.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapper;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newLinkedHashMap();
        final List<RpslAttribute> additions = Lists.newArrayList();
        addOrReplaceAttribute(object, AttributeType.METHOD, keyWrapper.getMethod(), replacements, additions);
        addOrReplaceAttribute(object, AttributeType.OWNER, keyWrapper.getOwner(), replacements, additions);
        addOrReplaceAttribute(object, AttributeType.FINGERPR, keyWrapper.getFingerprint(), replacements, additions);

        for (final RpslAttribute attribute : replacements.values()) {
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(attribute.getType()));
        }

        RpslObjectBuilder builder = new RpslObjectBuilder(object).replaceAttributes(replacements);
        if (!additions.isEmpty()) {     // don't sort unless needed
            builder.addAttributes(additions).sort();
        }
        return builder.get();
    }

    private void addOrReplaceAttribute(final RpslObject rpslObject, final AttributeType attributeType, final String attributeValue, final Map<RpslAttribute, RpslAttribute> replacements, final List<RpslAttribute> additions) {
        if (!rpslObject.containsAttribute(attributeType)) {
            additions.add(new RpslAttribute(attributeType, attributeValue));
        } else {
            replacements.put(rpslObject.findAttribute(attributeType), new RpslAttribute(attributeType, attributeValue));
        }
    }
}
