package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

/**
 * Transform an RPSL object into only valid latin-1 characters.
 *
 * TODO: This class is not extending Transformer, as otherwise syntax checking can fail due to invalid character(s).
 */
@Component
public class Latin1Transformer {

    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext) {

        final RpslObject updatedObject = Latin1Conversion.convert(rpslObject);

        for (int offset = 0; offset < rpslObject.getAttributes().size(); offset++) {
            final RpslAttribute attribute = rpslObject.getAttributes().get(offset);
            final RpslAttribute updatedAttribute = updatedObject.getAttributes().get(offset);

            if (!attribute.equals(updatedAttribute)) {
                updateContext.addMessage(update, attribute, UpdateMessages.valueChangedDueToLatin1Conversion());
            }
        }

        return updatedObject;
    }
}
