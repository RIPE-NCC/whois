package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class InetnumStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY, Action.DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    private final StatusDao statusDao;
    private final Ipv4Tree ipv4Tree;
    private final Maintainers maintainers;

    @Autowired
    public InetnumStatusValidator(
            final StatusDao statusDao,
            final Ipv4Tree ipv4Tree,
            final Maintainers maintainers) {
        this.statusDao = statusDao;
        this.ipv4Tree = ipv4Tree;
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        switch (update.getAction()) {
            case MODIFY:
                validateModify(update, updateContext);
                break;
            case DELETE:
                validateDelete(update, updateContext);
                break;
            default:
                throw new IllegalStateException(update.getAction().toString());
        }
    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getReferenceObject() == null || update.getUpdatedObject() == null) {
            return;
        }

        final CIString originalStatus = update.getReferenceObject().getValueForAttribute(AttributeType.STATUS);
        final CIString updateStatus = update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS);

        if (!Objects.equals(originalStatus, updateStatus) && (!hasAuthOverride(updateContext.getSubject(update)))) {
            updateContext.addMessage(update, UpdateMessages.statusChange());
        }

        validateHierarchy(update.getUpdatedObject(), update, updateContext);
    }

    private void validateDelete(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getReferenceObject() == null || update.getUpdatedObject() == null) {
            return;
        }

        final InetnumStatus updateStatus;
        try {
            updateStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS));
        } catch (IllegalArgumentException e) {
            // ignore invalid status
            return;
        }

        if (updateStatus.requiresRsMaintainer()) {
            final Set<CIString> mntBy = update.getReferenceObject().getValuesForAttribute(AttributeType.MNT_BY);
            if (!maintainers.isRsMaintainer(mntBy)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.deleteWithStatusRequiresAuthorization(updateStatus.toString()));
                }
            }
        }

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(update.getReferenceObject().getKey());

        final List<Ipv4Entry> parents = ipv4Tree.findFirstLessSpecific(ipv4Resource);
        if (parents.size() != 1) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipv4Resource));
            }
            return;
        }

        final InetnumStatus referenceStatus = InetnumStatus.getStatusFor(update.getReferenceObject().getValueForAttribute(AttributeType.STATUS));
        final InetnumStatus parentStatus = InetnumStatus.getStatusFor(statusDao.getStatus(parents.get(0).getObjectId()));

        validateLegacyStatus(
            referenceStatus,
            parentStatus,
            update,
            updateContext);

        validateHierarchy(
            update.getReferenceObject(),
            update,
            updateContext);
    }

    private void validateLegacyStatus(
            final InetnumStatus updateStatus,
            final InetnumStatus parentStatus,
            final PreparedUpdate update,
            final UpdateContext updateContext) {
        if ((updateStatus == InetnumStatus.LEGACY) &&
                (parentStatus != InetnumStatus.LEGACY) &&
               (!authByRsOrOverride(updateContext.getSubject(update)))) {
            updateContext.addMessage(update, UpdateMessages.inetnumStatusLegacy());
        }
    }

    private void validateHierarchy(
            final RpslObject rpslObject,
            final PreparedUpdate update,
            final UpdateContext updateContext) {
        final CIString objectStatus = rpslObject.getValueForAttribute(AttributeType.STATUS);

        final Ipv4Entry parent = ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(rpslObject.getKey())).get(0);
        final CIString parentStatus = statusDao.getStatus(parent.getObjectId());

        final List<Ipv4Entry> children = ipv4Tree.findFirstMoreSpecific(Ipv4Resource.parse(rpslObject.getKey()));
        final List<Integer> childrenObjectIds = Lists.transform(children, IpEntry::getObjectId);
        final Map<Integer, CIString> childStatusMap = statusDao.getStatus(childrenObjectIds);

        switch (update.getAction()) {
            case MODIFY: {
                validateParentStatus(
                    parentStatus,
                    rpslObject.getValueForAttribute(AttributeType.STATUS),
                    updateContext,
                    update);

                children.forEach(child -> {
                    final CIString childStatus = childStatusMap.get(child.getObjectId());
                    final CIString childKey = CIString.ciString(child.getKey().toRangeString());
                    validateChildStatus(
                        objectStatus,
                        childStatus,
                        childKey,
                        updateContext,
                        update);
                });
            }
            break;
            case DELETE: {
                children.forEach(child -> {
                    final CIString childStatus = childStatusMap.get(child.getObjectId());
                    final CIString childKey = CIString.ciString(child.getKey().toRangeString());
                    validateChildStatus(
                        parentStatus,
                        childStatus,
                        childKey,
                        updateContext,
                        update);
                });
            }
            break;
            default:
            break;
        }
    }

    // TODO: [ES] has RS maintainer is *always* false ?

    private void validateParentStatus(
            final CIString parentStatus,
            final CIString childStatus,
            final UpdateContext updateContext,
            final PreparedUpdate update) {
        if (!InetnumStatus.getStatusFor(childStatus).worksWithParentStatus(InetnumStatus.getStatusFor(parentStatus), false)) {
            updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(Messages.Type.WARNING, ObjectType.INETNUM, parentStatus.toString()));
        }
    }

    private void validateChildStatus(
            final CIString parentStatus,
            final CIString childStatus,
            final CIString childKey,
            final UpdateContext updateContext,
            final PreparedUpdate update) {
        if (!InetnumStatus.getStatusFor(childStatus).worksWithParentStatus(InetnumStatus.getStatusFor(parentStatus), false)) {
            updateContext.addMessage(update, UpdateMessages.incorrectChildStatus(Messages.Type.WARNING, parentStatus.toString(), childStatus.toString(), childKey));
        }
    }

    private boolean authByRsOrOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER) || hasAuthOverride(subject);
    }

    private boolean hasAuthOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER);
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
