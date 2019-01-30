package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

import java.util.ListIterator;

@Component
public class ShortFormatTransformer implements Transformer {

    @Override
    public RpslObject transform(final RpslObject rpslObject, final Update update, final UpdateContext updateContext, final Action action) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
        boolean updated = false;

        final ListIterator<RpslAttribute> iterator = builder.getAttributes().listIterator();
        while (iterator.hasNext()) {
            final RpslAttribute rpslAttribute = iterator.next();
            if (rpslAttribute.getType() != null && !rpslAttribute.getType().getName().equals(rpslAttribute.getKey())) {
                iterator.set(new RpslAttribute(rpslAttribute.getType(), rpslAttribute.getValue()));
                updated = true;
            }
        }

        if (updated) {
            updateContext.addMessage(update, UpdateMessages.shortFormatAttributeReplaced());
            return builder.get();
        }

        return rpslObject;
    }
}
