package net.ripe.db.whois.update.handler.validator.poem;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class PoemHasOnlyPublicMaintainerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.POEM);

    private static final CIString POEM_MAINTAINER = ciString("LIM-MNT");

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslAttribute mntByAttribute = update.getUpdatedObject().findAttribute(AttributeType.MNT_BY);
        if (!mntByAttribute.getCleanValue().equals(POEM_MAINTAINER)) {
            updateContext.addMessage(update, mntByAttribute, UpdateMessages.poemRequiresPublicMaintainer());
        }
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
