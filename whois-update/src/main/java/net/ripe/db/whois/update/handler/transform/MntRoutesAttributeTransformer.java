package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

@Component
public class MntRoutesAttributeTransformer implements Transformer {

    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext,
                                final Action action) {

        if ((rpslObject.getType() != ObjectType.AUT_NUM) ||
                !rpslObject.containsAttribute(AttributeType.MNT_ROUTES)) {
            return rpslObject;
        }

        updateContext.addMessage(update, UpdateMessages.mntRoutesAttributeRemoved());
        return new RpslObjectBuilder(rpslObject).removeAttributeType(AttributeType.MNT_ROUTES).get();
    }

}
