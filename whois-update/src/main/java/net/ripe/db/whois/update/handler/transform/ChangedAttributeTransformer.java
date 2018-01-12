package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChangedAttributeTransformer implements Transformer {

    private final ChangedAttrFeatureToggle changedAttrFeatureToggle;

    @Autowired
    public ChangedAttributeTransformer(final ChangedAttrFeatureToggle changedAttrFeatureToggle) {
        this.changedAttrFeatureToggle = changedAttrFeatureToggle;
    }

    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext,
                                final Action action) {

        if (Action.DELETE == action) {
            // ignore changed attribute on delete
            return rpslObject;
        }

        if (!rpslObject.containsAttribute(AttributeType.CHANGED)) {
            return rpslObject;
        }

        if (changedAttrFeatureToggle.isChangedAttrAvailable()) {
            updateContext.addMessage(update, UpdateMessages.changedAttributeRemoved());
            return new RpslObjectBuilder(rpslObject).removeAttributeType(AttributeType.CHANGED).get();
         } else {
            for (RpslAttribute changed : rpslObject.findAttributes(AttributeType.CHANGED)) {
                updateContext.addMessage(update, ValidationMessages.unknownAttribute(changed.getKey()));
            }
            return rpslObject;
        }
    }

}
