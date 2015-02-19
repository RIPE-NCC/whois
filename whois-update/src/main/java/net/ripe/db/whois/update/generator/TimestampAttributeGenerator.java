package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.stereotype.Component;

@Component
public class TimestampAttributeGenerator extends AttributeGenerator {

    public RpslObject generateAttributes(final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext) {
        RpslObject adjusted = null;
        if (updatedObject.containsAttribute(AttributeType.CREATED)) {
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
            adjusted = new RpslObjectBuilder(updatedObject).removeAttributeType(AttributeType.CREATED).
        }
        if (updatedObject.containsAttribute(AttributeType.LAST_MODIFIED)) {
            updateContext.addMessage(update, ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
        }

        if (updateContext.getAction(update) == Action.CREATE) {
            //new RpslObjectBuilder(updatedObject).removeAttribute(AttributeType.CREATED).addAttribute(AttributeType.CREATED).build();
        } else if (updateContext.getAction(update) == Action.MODIFY) {
            //new RpslObjectBuilder(updatedObject).removeAttribute(AttributeType.CREATED).addAttribute(CREATED).build();
        }
    }

}
