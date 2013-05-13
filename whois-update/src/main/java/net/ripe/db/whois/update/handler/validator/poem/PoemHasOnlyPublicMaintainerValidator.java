package net.ripe.db.whois.update.handler.validator.poem;

import com.google.common.collect.Lists;
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

import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class PoemHasOnlyPublicMaintainerValidator implements BusinessRuleValidator {
    private static final CIString POEM_MAINTAINER = ciString("LIM-MNT");

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.POEM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslAttribute mntByAttribute = update.getUpdatedObject().findAttribute(AttributeType.MNT_BY);
        if (!mntByAttribute.getCleanValue().equals(POEM_MAINTAINER)) {
            updateContext.addMessage(update, mntByAttribute, UpdateMessages.poemRequiresPublicMaintainer());
        }
    }
}
