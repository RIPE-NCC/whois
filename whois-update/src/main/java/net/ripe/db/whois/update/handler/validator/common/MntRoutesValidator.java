package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.attrs.AttributeParseException;
import net.ripe.db.whois.common.domain.attrs.MntRoutes;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MntRoutesValidator implements BusinessRuleValidator {
    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        final List<ObjectType> types = Lists.newArrayList();

        for (final ObjectType objectType : ObjectType.values()) {
            if (ObjectTemplate.getTemplate(objectType).hasAttribute(AttributeType.MNT_ROUTES)) {
                types.add(objectType);
            }
        }

        return types;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Map<MntRoutes, RpslAttribute> mntRoutesMap = Maps.newHashMap();

        for (final RpslAttribute attribute : update.getUpdatedObject().findAttributes(AttributeType.MNT_ROUTES)) {
            for (final CIString mntRoutesValue : attribute.getCleanValues()) {
                try {
                    final MntRoutes mntRoutes = MntRoutes.parse(mntRoutesValue);
                    for (final Map.Entry<MntRoutes, RpslAttribute> mntRoutesEntry : mntRoutesMap.entrySet()) {
                        final MntRoutes otherMntRoutes = mntRoutesEntry.getKey();
                        if (mntRoutes.getMaintainer().equals(otherMntRoutes.getMaintainer()) && mntRoutes.isAnyRange() != otherMntRoutes.isAnyRange()) {
                            final RpslAttribute otherAttribute = mntRoutesEntry.getValue();
                            syntaxError(update, updateContext, otherAttribute);
                            syntaxError(update, updateContext, attribute);
                        }
                    }

                    mntRoutesMap.put(mntRoutes, attribute);
                } catch (AttributeParseException e) {
                    syntaxError(update, updateContext, attribute);
                }
            }
        }
    }

    private void syntaxError(final PreparedUpdate update, final UpdateContext updateContext, final RpslAttribute attribute) {
        updateContext.addMessage(update, attribute, ValidationMessages.syntaxError(attribute.getCleanValue(), "ANY can only occur as a single value"));
    }
}
