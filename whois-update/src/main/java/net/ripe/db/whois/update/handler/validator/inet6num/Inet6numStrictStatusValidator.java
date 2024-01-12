package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.update.domain.Action.CREATE;

/**
 * Apply stricter status validation when creating an inet6num object.
 */
@Component
public class Inet6numStrictStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private final Maintainers maintainers;

    @Autowired
    public Inet6numStrictStatusValidator(final Maintainers maintainers) {
        this.maintainers = maintainers;
    }

    @Override
    public  List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        return validateStatusAgainstResourcesInTree(update, updateContext);
    }

    @SuppressWarnings("unchecked")
    private List<Message> validateStatusAgainstResourcesInTree(final PreparedUpdate update, final UpdateContext updateContext) {
        final List<Message> validationMessages = Lists.newArrayList();
        checkAuthorisationForStatus(update, updateContext, validationMessages);

        return validationMessages;
    }

    private void checkAuthorisationForStatus(final PreparedUpdate update, final UpdateContext updateContext, final List<Message> validationMessages) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final Set<CIString> mntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);

        final Inet6numStatus currentStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        if (currentStatus.requiresAllocMaintainer()) {
            if (!updateContext.getSubject(update).hasPrincipal(Principal.ALLOC_MAINTAINER)) {
                validationMessages.add(UpdateMessages.statusRequiresAuthorization(currentStatus.toString()));
                return;
            }
        }

        if (currentStatus.requiresRsMaintainer()) {
            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (missingRsMaintainer) {
                validationMessages.add(UpdateMessages.statusRequiresAuthorization(updatedObject.getValueForAttribute(STATUS).toString()));
                return;
            }
            if (!updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
                validationMessages.add(UpdateMessages.authorisationRequiredForSetStatus(currentStatus.toString()));
            }
        }
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
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
