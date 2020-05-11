package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
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
import java.util.Objects;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.DELETE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;
import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

// TODO [SB] This class is over complex and needs to be split into seperate validators for easier overview/maintenance
// TODO probably something like specific validators for inetnum/inet6num, create/modify/delete combinations and seperate validator(s) for the authorisation checks
@Component
public class StatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(MODIFY, DELETE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(INETNUM, ObjectType.INET6NUM);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public StatusValidator(
            final RpslObjectDao objectDao,
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getAction() == DELETE) {
            if (update.getType().equals(INETNUM)) {
                validateDelete(update, updateContext, ipv4Tree);
            } else {
                validateDelete(update, updateContext, ipv6Tree);
            }
        } else {
            validateModify(update, updateContext);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateStatusAgainstResourcesInTree(final PreparedUpdate update, final UpdateContext updateContext, final IpTree ipTree, final IpInterval ipInterval) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!allChildrenHaveCorrectStatus(update, updateContext, ipTree, ipInterval)) {
            return;
        }

        final InetStatus currentStatus = InetStatusHelper.getStatus(update);
        final List<IpEntry> parents = ipTree.findFirstLessSpecific(ipInterval);
        if (parents.size() != 1) {
            updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipInterval));
            return;
        }
        checkAuthorisationForStatus(update, updateContext, updatedObject, currentStatus);

        final RpslObject parentObject = objectDao.getById(parents.get(0).getObjectId());
        final List<RpslAttribute> parentStatuses = parentObject.findAttributes(AttributeType.STATUS);
        if (parentStatuses.isEmpty()) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.objectLacksStatus("Parent", parentObject.getKey()));
            }
            return;
        }

        final InetStatus parentStatus = InetStatusHelper.getStatus(parentObject);
        if (parentStatus == null) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.objectHasInvalidStatus("Parent", parentObject.getKey(), parentObject.getValueForAttribute(AttributeType.STATUS)));
            }
            return;
        }

        if (updatedObject.getType() == INETNUM) {
            validateStatusLegacy(updatedObject, parentObject, update, updateContext);
        }

        final Set<CIString> updateMntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);
        final boolean hasRsMaintainer = maintainers.isRsMaintainer(updateMntBy);

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PA) && parentStatus.equals(InetnumStatus.ASSIGNED_PA)) {
            checkAuthorizationForStatusInHierarchy(update, updateContext, ipTree, ipInterval, UpdateMessages.incorrectParentStatus(updatedObject.getType(), parentStatus.toString()));
        } else if (!currentStatus.worksWithParentStatus(parentStatus, hasRsMaintainer, update.getAction() == CREATE)) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(updatedObject.getType(), parentStatus.toString()));
            }
        }

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
            if (parentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
                final Set<CIString> parentMntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);
                final boolean parentHasRsMaintainer = maintainers.isRsMaintainer(parentMntBy);
                if (parentHasRsMaintainer) {
                    if (!hasAuthOverride(updateContext.getSubject(update))) {
                        updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(updatedObject.getType(), parentStatus.toString()));
                    }
                }
            }

            checkAuthorizationForStatusInHierarchy(update, updateContext, ipTree, ipInterval, UpdateMessages.incorrectParentStatus(updatedObject.getType(), parentStatus.toString()));
        }

    }

    private boolean authByRsOrOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER) || hasAuthOverride(subject);
    }

    private boolean hasAuthOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER);
    }

    private void checkAuthorizationForStatusInHierarchy(final PreparedUpdate update, final UpdateContext updateContext, final IpTree ipTree, final IpInterval ipInterval, final Message errorMessage) {
        final RpslObject parentInHierarchyMaintainedByRs = findParentWithRsMaintainer(ipTree, ipInterval);

        if (parentInHierarchyMaintainedByRs != null) {

            final List<RpslAttribute> parentStatuses = parentInHierarchyMaintainedByRs.findAttributes(AttributeType.STATUS);
            if (parentStatuses.isEmpty()) {
                return;
            }

            final CIString parentStatusValue = parentStatuses.get(0).getCleanValue();
            final InetStatus parentStatus = getStatus(parentStatusValue, update);

            if (parentStatus == null) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.objectHasInvalidStatus("Parent", parentInHierarchyMaintainedByRs.getKey(), parentStatusValue));
                }
            } else {
                final Set<CIString> mntLower = parentInHierarchyMaintainedByRs.getValuesForAttribute(AttributeType.MNT_LOWER);
                final boolean parentHasRsMntLower = maintainers.isRsMaintainer(mntLower);
                final InetStatus currentStatus = InetStatusHelper.getStatus(update);

                if (!currentStatus.worksWithParentInHierarchy(parentStatus, parentHasRsMntLower)) {
                    if (!hasAuthOverride(updateContext.getSubject(update))) {
                        updateContext.addMessage(update, errorMessage);
                    }
                }
            }
        }
    }

    @CheckForNull
    private RpslObject findParentWithRsMaintainer(final IpTree ipTree, final IpInterval ipInterval) {
        @SuppressWarnings("unchecked")
        final List<IpEntry> allLessSpecific = Lists.reverse(ipTree.findAllLessSpecific(ipInterval));
        for (final IpEntry parent : allLessSpecific) {
            final RpslObject parentObject = objectDao.getById(parent.getObjectId());
            final Set<CIString> mntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);

            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (!missingRsMaintainer) {
                return parentObject;
            }
        }

        return null;
    }

    private void checkAuthorisationForStatus(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject updatedObject, final InetStatus currentStatus) {
        final Set<CIString> mntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);

        if (currentStatus.requiresAllocMaintainer()) {
            final boolean hasOnlyAllocMaintainer = Sets.intersection(maintainers.getAllocMaintainers(), mntBy).containsAll(mntBy);
            if (!hasOnlyAllocMaintainer) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(currentStatus.toString()));
                }
                return;
            }
            if (!updateContext.getSubject(update).hasPrincipal(Principal.ALLOC_MAINTAINER)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(currentStatus.toString()));
                }
                return;
            }
        }

        if (currentStatus.requiresRsMaintainer()) {
            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (missingRsMaintainer) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.statusRequiresAuthorization(updatedObject.getValueForAttribute(AttributeType.STATUS).toString()));
                }
                return;
            }
            if (!updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.authorisationRequiredForSetStatus(currentStatus.toString()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean allChildrenHaveCorrectStatus(final PreparedUpdate update, final UpdateContext updateContext, final IpTree ipTree, final IpInterval ipInterval) {
        final List<IpEntry> children = ipTree.findFirstMoreSpecific(ipInterval);
        final RpslAttribute updateStatusAttribute = update.getUpdatedObject().findAttribute(AttributeType.STATUS);
        final InetStatus updatedStatus = InetStatusHelper.getStatus(update);

        for (final IpEntry child : children) {
            final RpslObject childObject = objectDao.getById(child.getObjectId());
            final List<RpslAttribute> childStatuses = childObject.findAttributes(AttributeType.STATUS);
            if (childStatuses.isEmpty()) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.objectLacksStatus("Child", childObject.getKey()));
                }
                continue;
            }

            final CIString childStatusValue = childStatuses.get(0).getCleanValue();
            final InetStatus childStatus = getStatus(childStatusValue, update);
            if (childStatus == null) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.objectHasInvalidStatus("Child", childObject.getKey(), childStatusValue));
                }
                return false;
            }
            final Set<CIString> childMntBy = childObject.getValuesForAttribute(AttributeType.MNT_BY);
            final boolean hasRsMaintainer = maintainers.isRsMaintainer(childMntBy);

            if (!childStatus.worksWithParentStatus(updatedStatus, hasRsMaintainer, update.getAction() == CREATE)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.incorrectChildStatus(updateStatusAttribute.getCleanValue(), childStatusValue, childObject.getKey()));
                }
                return false;
            } else if (updatedStatus.equals(InetnumStatus.ASSIGNED_PA) && childStatus.equals(InetnumStatus.ASSIGNED_PA)) {
                checkAuthorizationForStatusInHierarchy(update, updateContext, ipTree, ipInterval, UpdateMessages.incorrectChildStatus(updateStatusAttribute.getCleanValue(), childStatusValue, childObject.getKey()));
            }
        }
        return true;
    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        final CIString originalStatus = update.getReferenceObject() != null ? update.getReferenceObject().getValueForAttribute(AttributeType.STATUS) : null;
        final CIString updateStatus = update.getUpdatedObject() != null ? update.getUpdatedObject().getValueForAttribute(AttributeType.STATUS) : null;

        if (!Objects.equals(originalStatus, updateStatus)) {
            if (!hasAuthOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.statusChange());
            }
        }
    }

    private void validateDelete(final PreparedUpdate update, final UpdateContext updateContext, final IpTree ipTree) {
        InetStatus status;

        if (update.getReferenceObject() == null) {
            return;
        }

        try {
            status = getStatus(update.getReferenceObject());
            if (status == null) {
                // invalid status attribute value
                return;
            }
        } catch (IllegalArgumentException e) {
            // status attribute not found
            return;
        }

        if (status.requiresRsMaintainer()) {
            final Set<CIString> mntBy = update.getReferenceObject().getValuesForAttribute(AttributeType.MNT_BY);
            if (!maintainers.isRsMaintainer(mntBy)) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.deleteWithStatusRequiresAuthorization(status.toString()));
                }
            }
        }

        if (update.getReferenceObject().getType().equals(INETNUM)) {
            final IpInterval ipInterval = IpInterval.parse(update.getReferenceObject().getKey());
            final List<IpEntry> parents = ipTree.findFirstLessSpecific(ipInterval);
            if (parents.size() != 1) {
                if (!hasAuthOverride(updateContext.getSubject(update))) {
                    updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipInterval));
                }
                return;
            }
            validateStatusLegacy(update.getReferenceObject(), objectDao.getById(parents.get(0).getObjectId()), update, updateContext);
        }
    }

    private void validateStatusLegacy(final RpslObject updatedObject, final RpslObject parentObject, final PreparedUpdate update, final UpdateContext updateContext) {
        if (updatedObject.getValueForAttribute(AttributeType.STATUS).equals(InetnumStatus.LEGACY.toString()) &&
                !parentObject.getValueForAttribute(AttributeType.STATUS).equals(InetnumStatus.LEGACY.toString())) {
            if (!authByRsOrOverride(updateContext.getSubject(update))) {
                updateContext.addMessage(update, UpdateMessages.inetnumStatusLegacy());
            }
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
