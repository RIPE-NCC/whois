package net.ripe.db.whois.update.handler.validator.poem;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class PoeticFormHasOnlyDbmMaintainerValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.POETIC_FORM);

    private static final CIString POETIC_FORM_MAINTAINER = ciString("RIPE-DBM-MNT");

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<RpslAttribute> mntByAttribute = update.getUpdatedObject().findAttributes(AttributeType.MNT_BY);
        if (mntByAttribute.size() !=1 || !mntByAttribute.get(0).getCleanValue().equals(POETIC_FORM_MAINTAINER)) {
            return Arrays.asList(UpdateMessages.poeticFormRequiresDbmMaintainer(mntByAttribute.get(0)));
        }

        return Collections.emptyList();
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
