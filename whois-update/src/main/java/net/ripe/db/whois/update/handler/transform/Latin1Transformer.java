package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

@Component
public class Latin1Transformer {

    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext) {

        final RpslObjectBuilder updatedRpslObject = new RpslObjectBuilder();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            if (Latin1Conversion.isLatin1(attribute.getValue())) {
                updatedRpslObject.append(attribute);
            } else {
                updatedRpslObject.append(
                        new RpslAttribute(attribute.getType(),
                                Latin1Conversion.convertToLatin1(attribute.getValue())));
                updateContext.addMessage(update, attribute, UpdateMessages.valueChangedDueToLatin1Conversion());
            }
        }
        return updatedRpslObject.get();
    }
}
