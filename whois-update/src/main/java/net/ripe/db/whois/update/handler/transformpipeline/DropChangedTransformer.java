package net.ripe.db.whois.update.handler.transformpipeline;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DropChangedTransformer implements PipelineTransformer {

    private final ChangedAttrFeatureToggle changedAttrFeatureToggle;

    @Autowired
    public DropChangedTransformer(ChangedAttrFeatureToggle changedAttrFeatureToggle) {
        this.changedAttrFeatureToggle = changedAttrFeatureToggle;
    }

    @Override
    public RpslObject transform(final RpslObject rpslObject,
                                final Update update,
                                final UpdateContext updateContext,
                                final Action action) {
        if (!Action.DELETE.equals(action) &&
                rpslObject.containsAttribute(AttributeType.CHANGED)) {
            if (changedAttrFeatureToggle.isChangedAttrAvailable()) {
                updateContext.addMessage(update, ValidationMessages.changedAttributeRemoved());
            }
            return new RpslObject(rpslObject.getAttributes()
                    .stream()
                    .filter(attribute ->
                            !attribute.getType().equals(AttributeType.CHANGED))
                    .collect(Collectors.toList()));
        }
        return rpslObject;
    }
}
