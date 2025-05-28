package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.LEGACY;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

/**
 * Apply stricter status validation when creating an inetnum object.
 */
@Component
public class InetnumStrictStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    private final RpslObjectDao objectDao;
    private final StatusDao statusDao;
    private final Ipv4Tree ipv4Tree;
    private final Maintainers maintainers;

    @Autowired
    public InetnumStrictStatusValidator(
            final RpslObjectDao objectDao,
            final StatusDao statusDao,
            final Ipv4Tree ipv4Tree,
            final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.statusDao = statusDao;
        this.ipv4Tree = ipv4Tree;
        this.maintainers = maintainers;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        return validateCreate(update, updateContext);
    }

    private List<Message> validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {
        return validateStatusAgainstResourcesInTree(update, updateContext);
    }

    @SuppressWarnings("unchecked")
    protected List<Message> validateStatusAgainstResourcesInTree(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final Ipv4Resource ipInterval = Ipv4Resource.parse(updatedObject.getKey());
        final List<Message> validationMessages = Lists.newArrayList();

        final List<Ipv4Entry> parents = ipv4Tree.findFirstLessSpecific(ipInterval);

        final InetnumStatus currentStatus = InetnumStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        checkAuthorisationForStatus(update,updateContext, currentStatus, validationMessages);

        final InetnumStatus parentStatus = InetnumStatus.getStatusFor(statusDao.getStatus(parents.get(0).getObjectId()));
        validateLegacyStatus(currentStatus, parentStatus, update, updateContext, validationMessages);

        return validationMessages;
    }

    private boolean authByRs(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER);
    }

    private void checkAuthorisationForStatus(final PreparedUpdate update, final UpdateContext updateContext, final InetnumStatus currentStatus, final List<Message> validationMessages) {
        final Set<CIString> mntBy = update.getUpdatedObject().getValuesForAttribute(AttributeType.MNT_BY);

        if (currentStatus.requiresRsMaintainer()) {
            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (missingRsMaintainer) {
                validationMessages.add(UpdateMessages.statusRequiresAuthorization(update.getUpdatedObject().getValueForAttribute(STATUS).toString()));
                return;
            }
            if (update.getAction() == CREATE && !updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
                validationMessages.add(UpdateMessages.authorisationRequiredForSetStatus(currentStatus.toString()));
            }
        }
    }

    private void validateLegacyStatus(final InetnumStatus currentStatus, final InetnumStatus parentStatus, final PreparedUpdate update, final UpdateContext updateContext, final List<Message> validationMessages) {
        if ((LEGACY == currentStatus) &&
                (LEGACY != parentStatus) &&
                    (!authByRs(updateContext.getSubject(update)))) {
                validationMessages.add(UpdateMessages.inetnumStatusLegacy());
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
