package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public abstract class MntLowerValidator implements BusinessRuleValidator {

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM, ObjectType.INET6NUM);

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        if (ObjectType.INETNUM.equals(update.getType()) && isInvalidInetnumStatus(update)) {
            return Collections.emptyList();
        }

        if (ObjectType.INET6NUM.equals(update.getType()) && isInvalidInet6numStatus(update)) {
            return Collections.emptyList();
        }

        return addErrorMessage(update);
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }
    protected abstract List<Message> addErrorMessage(final PreparedUpdate update);

    protected abstract boolean isInvalidInetnumStatus(final PreparedUpdate update);

    protected abstract boolean isInvalidInet6numStatus(final PreparedUpdate update);
}
