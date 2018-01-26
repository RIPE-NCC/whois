package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

@Component
public class NonBreakSpacesTransformer implements Transformer {

    @Override
    public RpslObject transform(final RpslObject rpslObject, final Update update, final UpdateContext updateContext, final Action action) {
        final String value = rpslObject.toString();
        final String updatedValue = replaceNonBreakSpaces(rpslObject.toString());

        if (updatedValue.equals(value)) {
            return rpslObject;
        }

        updateContext.addMessage(update, UpdateMessages.nonBreakSpacesReplaced());
        return RpslObject.parse(updatedValue);
    }

    private String replaceNonBreakSpaces(final String value) {
        return value.replace('\u00a0', '\u0020');
    }

}
