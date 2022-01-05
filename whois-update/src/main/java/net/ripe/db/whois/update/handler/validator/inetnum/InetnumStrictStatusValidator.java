package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.StatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

import javax.annotation.CheckForNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.Messages.Type.ERROR;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ASSIGNED_PI;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.LEGACY;
import static net.ripe.db.whois.update.domain.Action.CREATE;

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
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        validateCreate(update, updateContext);
    }

    private void validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {
        validateStatusAgainstResourcesInTree(update, updateContext);
    }

    @SuppressWarnings("unchecked")
    private void validateStatusAgainstResourcesInTree(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        final Ipv4Resource ipInterval = Ipv4Resource.parse(updatedObject.getKey());

        if (!allChildrenHaveCorrectStatus(update, updateContext, ipInterval)) {
            return;
        }

        final List<Ipv4Entry> parents = ipv4Tree.findFirstLessSpecific(ipInterval);
        if (parents.size() != 1) {
            updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipInterval));
            return;
        }

        final InetnumStatus currentStatus = InetnumStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        if (!hasAuthOverride(updateContext.getSubject(update))) {
            checkAuthorisationForStatus(update, updateContext, updatedObject, currentStatus);
        }

        final InetnumStatus parentStatus = InetnumStatus.getStatusFor(statusDao.getStatus(parents.get(0).getObjectId()));

        validateLegacyStatus(currentStatus, parentStatus, update, updateContext);

        final Set<CIString> updateMntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);
        final boolean hasRsMaintainer = maintainers.isRsMaintainer(updateMntBy);

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PA) && parentStatus.equals(InetnumStatus.ASSIGNED_PA)) {
            checkAuthorizationForStatusInHierarchy(
                    update,
                    updateContext,
                    ipInterval,
                    UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString())
            );
        } else {
            if (!currentStatus.worksWithParentStatus(parentStatus, hasRsMaintainer)) {
                updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        }

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PI) && parentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
            final RpslObject parentObject = objectDao.getById(parents.get(0).getObjectId());

            final Set<CIString> parentMntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);
            final boolean parentHasRsMaintainer = maintainers.isRsMaintainer(parentMntBy);

            if (parentHasRsMaintainer) {
                updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        }

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
            checkAuthorizationForStatusInHierarchy(
                    update,
                    updateContext,
                    ipInterval,
                    UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
        }

    }

    private boolean authByRsOrOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER) || hasAuthOverride(subject);
    }

    private boolean hasAuthOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER);
    }

    private void checkAuthorizationForStatusInHierarchy(final PreparedUpdate update, final UpdateContext updateContext, final Ipv4Resource ipInterval, final Message errorMessage) {
        final RpslObject parentInHierarchyMaintainedByRs = findParentWithRsMaintainer(ipInterval);

        if (parentInHierarchyMaintainedByRs != null) {
            final List<RpslAttribute> parentStatuses = parentInHierarchyMaintainedByRs.findAttributes(STATUS);
            if (parentStatuses.isEmpty()) {
                return;
            }

            final CIString parentStatusValue = parentStatuses.get(0).getCleanValue();
            final InetnumStatus parentStatus = InetnumStatus.getStatusFor(parentStatusValue);

            final Set<CIString> mntLower = parentInHierarchyMaintainedByRs.getValuesForAttribute(AttributeType.MNT_LOWER);
            final boolean parentHasRsMntLower = maintainers.isRsMaintainer(mntLower);

            final InetnumStatus currentStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(STATUS));
            if (!currentStatus.worksWithParentInHierarchy(parentStatus, parentHasRsMntLower)) {
                updateContext.addMessage(update, errorMessage);
            }
        }
    }

    @CheckForNull
    private RpslObject findParentWithRsMaintainer(final Ipv4Resource ipInterval) {
        @SuppressWarnings("unchecked")
        final List<Ipv4Entry> allLessSpecific = Lists.reverse(ipv4Tree.findAllLessSpecific(ipInterval));
        for (final Ipv4Entry parent : allLessSpecific) {
            final RpslObject parentObject = objectDao.getById(parent.getObjectId());
            final Set<CIString> mntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);

            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (!missingRsMaintainer) {
                return parentObject;
            }
        }

        return null;
    }

    private void checkAuthorisationForStatus(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject updatedObject, final InetnumStatus currentStatus) {
        final Set<CIString> mntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);

        if (currentStatus.requiresAllocMaintainer()) {
            final boolean hasOnlyAllocMaintainer = Sets.intersection(maintainers.getAllocMaintainers(), mntBy).containsAll(mntBy);
            if (!hasOnlyAllocMaintainer) {
                updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(currentStatus.toString()));
                return;
            }
            if (!updateContext.getSubject(update).hasPrincipal(Principal.ALLOC_MAINTAINER)) {
                updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(currentStatus.toString()));
                return;
            }
        }

        if (currentStatus.requiresRsMaintainer()) {
            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (missingRsMaintainer) {
                updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(updatedObject.getValueForAttribute(STATUS).toString()));
                return;
            }
            if (!updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
                updateContext.addMessage(update, UpdateMessages.authorisationRequiredForSetStatus(currentStatus.toString()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean allChildrenHaveCorrectStatus(final PreparedUpdate update, final UpdateContext updateContext, final Ipv4Resource ipInterval) {
        final InetnumStatus updatedStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(STATUS));

        final List<Ipv4Entry> children = ipv4Tree.findFirstMoreSpecific(ipInterval);
        final List<Integer> childrenObjectIds = Lists.transform(children, IpEntry::getObjectId);
        final Map<Integer, CIString> childStatusMap = statusDao.getStatus(childrenObjectIds);

        for (final Ipv4Entry child : children) {
            final InetnumStatus childStatus = InetnumStatus.getStatusFor(childStatusMap.get(child.getObjectId()));

            if (!childStatus.worksWithParentStatus(updatedStatus, childHasRsMaintainer(child, childStatus))) {
                updateContext.addMessage(update, UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), child.getKey().toRangeString()));
                return false;
            } else if (updatedStatus.equals(InetnumStatus.ASSIGNED_PA) && childStatus.equals(InetnumStatus.ASSIGNED_PA)) {
                checkAuthorizationForStatusInHierarchy(
                    update,
                    updateContext,
                    ipInterval,
                    UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), child.getKey().toRangeString())
                );
            }
        }
        return true;
    }

    private boolean childHasRsMaintainer(final Ipv4Entry child, final InetnumStatus childStatus) {
        if (!ASSIGNED_PI.equals(childStatus)) {
            // check only needed for ASSIGNED PI status
            return false;
        }

        final RpslObject childObject = objectDao.getById(child.getObjectId());

        final Set<CIString> childMntBy = childObject.getValuesForAttribute(AttributeType.MNT_BY);
        return maintainers.isRsMaintainer(childMntBy);
    }

    private void validateLegacyStatus(final InetnumStatus currentStatus, final InetnumStatus parentStatus, final PreparedUpdate update, final UpdateContext updateContext) {
        if ((LEGACY == currentStatus) &&
                (LEGACY != parentStatus) &&
                    (!authByRsOrOverride(updateContext.getSubject(update)))) {
                updateContext.addMessage(update, UpdateMessages.inetnumStatusLegacy());
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
