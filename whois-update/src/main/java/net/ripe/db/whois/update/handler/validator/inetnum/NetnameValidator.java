package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
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

import static net.ripe.db.whois.common.rpsl.AttributeType.NETNAME;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class NetnameValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(INETNUM, INET6NUM);
    private final Maintainers maintainers;

    @Autowired
    public NetnameValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {

        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        final RpslObject referenceObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString refNetname = referenceObject.getValueOrNullForAttribute(NETNAME);
        final CIString updNetname = updatedObject.getValueOrNullForAttribute(NETNAME);

        final boolean isAllocMaintainer = maintainers.isAllocMaintainer(referenceObject.getValuesForAttribute(AttributeType.MNT_BY));

        final Action action = update.getAction();

        if (isAllocMaintainer && hasChanged(refNetname, updNetname, action)) {
           return Arrays.asList(UpdateMessages.netnameCannotBeChanged());
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }

    private boolean hasChanged(final CIString referenceNetname, final CIString updatedNetname, final Action action) {
        return action == MODIFY && !Objects.equals(referenceNetname, updatedNetname);
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
