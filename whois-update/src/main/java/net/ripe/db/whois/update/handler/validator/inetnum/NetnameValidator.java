package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
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

import static net.ripe.db.whois.common.rpsl.AttributeType.NETNAME;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class NetnameValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(INETNUM, INET6NUM, AUT_NUM);
    private final RpslObjectDao objectDao;
    private final Maintainers maintainers;

    @Autowired
    public NetnameValidator(final RpslObjectDao objectDao, final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {

        final Subject subject = updateContext.getSubject(update);

        if (subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER) || subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return;
        }

        final RpslObject referenceObject = update.getReferenceObject();
        final RpslObject updatedObject = update.getUpdatedObject();

        final CIString refNetname = referenceObject.getValueOrNullForAttribute(NETNAME);
        final CIString updNetname = updatedObject.getValueOrNullForAttribute(NETNAME);

        final boolean rsMaintained = !Sets.intersection(this.maintainers.getRsMaintainers(), referenceObject.getValuesForAttribute(AttributeType.MNT_BY)).isEmpty();
        final Action action = update.getAction();

        if (rsMaintained && hasChanged(refNetname, updNetname, action)) {
            updateContext.addMessage(update, UpdateMessages.netnameCannotBeChanged());
        }
    }

    private boolean hasChanged(final CIString referenceNetname, final CIString updatedNetname, final Action action) {
        return action == MODIFY && !Objects.equal(referenceNetname, updatedNetname);
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
