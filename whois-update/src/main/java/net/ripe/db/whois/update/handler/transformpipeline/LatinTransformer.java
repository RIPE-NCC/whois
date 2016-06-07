package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.CharacterSetConversion;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

@Component
public class LatinTransformer implements Transformer {

    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext,
                                final Action action) {
        final RpslObjectBuilder updatedRpslObject = new RpslObjectBuilder();
        for (RpslAttribute attribute : rpslObject.getAttributes()) {
            if (CharacterSetConversion.isConvertableIntoLatin1(attribute.getValue())) {
                updatedRpslObject.append(attribute);
            } else {
                updatedRpslObject.append(
                        new RpslAttribute(attribute.getType(),
                                CharacterSetConversion.convertToLatin1(attribute.getValue())));
                updateContext.addMessage(update, UpdateMessages.valueChangedDueToLatin1Conversion(attribute.getKey()));
            }
        }
        return updatedRpslObject.get();
    }
}
