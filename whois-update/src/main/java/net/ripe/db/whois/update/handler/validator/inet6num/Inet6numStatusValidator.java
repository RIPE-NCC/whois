package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;

@Component
public class Inet6numStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY, Action.DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private final Maintainers maintainers;

    @Autowired
    public Inet6numStatusValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        switch (update.getAction()) {
            case MODIFY:
                return validateModify(update, updateContext);
            case DELETE:
                return validateDelete(update);
            default:
                throw new IllegalStateException(update.getAction().toString());
        }
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

    private List<Message> validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getReferenceObject() == null || update.getUpdatedObject() == null) {
            return Collections.emptyList();
        }

        final CIString originalStatus = update.getReferenceObject().getValueForAttribute(STATUS);
        final CIString updateStatus = update.getUpdatedObject().getValueForAttribute(STATUS);

        if (!Objects.equals(originalStatus, updateStatus)) {
           return Arrays.asList(UpdateMessages.statusChange());
        }
        return Collections.emptyList();
    }

    private List<Message> validateDelete(final PreparedUpdate update) {
        if (update.getReferenceObject() == null) {
            return Collections.emptyList();
        }

        final Inet6numStatus status;
        try {
            status = Inet6numStatus.getStatusFor(update.getReferenceObject().getValueForAttribute(STATUS));
        } catch (IllegalArgumentException e) {
            // ignore invalid status
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        if (status.requiresRsMaintainer()) {
            final Set<CIString> mntBy = update.getReferenceObject().getValuesForAttribute(AttributeType.MNT_BY);
            if (!maintainers.isRsMaintainer(mntBy)) {
                    messages.add(UpdateMessages.deleteWithStatusRequiresAuthorization(status.toString()));
            }
        }

        return messages;
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
