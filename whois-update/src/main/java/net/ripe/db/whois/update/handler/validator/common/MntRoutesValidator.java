package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MntRoutesValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES;
    static {
        List<ObjectType> types = Lists.newArrayList();
        for (final ObjectType objectType : ObjectType.values()) {
            if (ObjectTemplate.getTemplate(objectType).hasAttribute(AttributeType.MNT_ROUTES)) {
                types.add(objectType);
            }
        }
        TYPES = ImmutableList.copyOf(types);
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Map<MntRoutes, RpslAttribute> mntRoutesMap = Maps.newHashMap();

        final List<Message> messages = Lists.newArrayList();
        for (final RpslAttribute attribute : update.getUpdatedObject().findAttributes(AttributeType.MNT_ROUTES)) {
            for (final CIString mntRoutesValue : attribute.getCleanValues()) {
                try {
                    final MntRoutes mntRoutes = MntRoutes.parse(mntRoutesValue);
                    for (final Map.Entry<MntRoutes, RpslAttribute> mntRoutesEntry : mntRoutesMap.entrySet()) {
                        final MntRoutes otherMntRoutes = mntRoutesEntry.getKey();
                        if (mntRoutes.getMaintainer().equals(otherMntRoutes.getMaintainer()) && mntRoutes.isAnyRange() != otherMntRoutes.isAnyRange()) {
                            final RpslAttribute otherAttribute = mntRoutesEntry.getValue();
                            messages.add(syntaxError(otherAttribute));
                            messages.add(syntaxError(attribute));
                        }
                    }

                    mntRoutesMap.put(mntRoutes, attribute);
                } catch (AttributeParseException e) {
                    messages.add(syntaxError(attribute));
                }
            }
        }

        return messages;
    }

    private Message syntaxError(final RpslAttribute attribute) {
        return ValidationMessages.syntaxError(attribute, "ANY can only occur as a single value");
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
