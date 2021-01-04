package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
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
import java.util.Set;

import static net.ripe.db.whois.common.Messages.Type.ERROR;
import static net.ripe.db.whois.common.rpsl.AttributeType.STATUS;
import static net.ripe.db.whois.update.domain.Action.CREATE;

@Component
public class Inet6numStrictStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

    private final RpslObjectDao objectDao;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public Inet6numStrictStatusValidator(
            final RpslObjectDao objectDao,
            final Ipv6Tree ipv6Tree,
            final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv6Tree = ipv6Tree;
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
        final Ipv6Resource ipInterval = Ipv6Resource.parse(update.getUpdatedObject().getKey());

        final RpslObject updatedObject = update.getUpdatedObject();
        if (!allChildrenHaveCorrectStatus(update, updateContext, ipInterval)) {
            return;
        }

        final List<Ipv6Entry> parents = ipv6Tree.findFirstLessSpecific(ipInterval);
        if (parents.size() != 1) {
            updateContext.addMessage(update, UpdateMessages.invalidParentEntryForInterval(ipInterval));
            return;
        }

        if (!hasAuthOverride(updateContext.getSubject(update))) {
            checkAuthorisationForStatus(update, updateContext, updatedObject);
        }

        // TODO: [ES] use status index to lookup status of parent (don't load entire object)
        final RpslObject parentObject = objectDao.getById(parents.get(0).getObjectId());

        final Inet6numStatus parentStatus = Inet6numStatus.getStatusFor(parentObject.getValueForAttribute(STATUS));

        final Set<CIString> updateMntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);
        final boolean hasRsMaintainer = maintainers.isRsMaintainer(updateMntBy);

        final Inet6numStatus currentStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        if (!currentStatus.worksWithParentStatus(parentStatus, hasRsMaintainer)) {
            updateContext.addMessage(update, UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
        }
    }

    private boolean authByRsOrOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.RS_MAINTAINER) || hasAuthOverride(subject);
    }

    private boolean hasAuthOverride(final Subject subject) {
        return subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER);
    }

    private void checkAuthorizationForStatusInHierarchy(final PreparedUpdate update, final UpdateContext updateContext, final Ipv6Resource ipInterval, final Message errorMessage) {
        final RpslObject parentInHierarchyMaintainedByRs = findParentWithRsMaintainer(ipInterval);

        if (parentInHierarchyMaintainedByRs != null) {

            final List<RpslAttribute> parentStatuses = parentInHierarchyMaintainedByRs.findAttributes(STATUS);
            if (parentStatuses.isEmpty()) {
                return;
            }

            final CIString parentStatusValue = parentStatuses.get(0).getCleanValue();
            final Inet6numStatus parentStatus = Inet6numStatus.getStatusFor(parentStatusValue);

            final Set<CIString> mntLower = parentInHierarchyMaintainedByRs.getValuesForAttribute(AttributeType.MNT_LOWER);
            final boolean parentHasRsMntLower = maintainers.isRsMaintainer(mntLower);

            final RpslObject updatedObject = update.getUpdatedObject();
            final Inet6numStatus currentStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));

            if (!currentStatus.worksWithParentInHierarchy(parentStatus, parentHasRsMntLower)) {
                updateContext.addMessage(update, errorMessage);
            }
        }
    }

    @CheckForNull
    private RpslObject findParentWithRsMaintainer(final Ipv6Resource ipInterval) {
        @SuppressWarnings("unchecked")
        final List<Ipv6Entry> allLessSpecific = Lists.reverse(ipv6Tree.findAllLessSpecific(ipInterval));
        for (final Ipv6Entry parent : allLessSpecific) {
            final RpslObject parentObject = objectDao.getById(parent.getObjectId());
            final Set<CIString> mntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);

            final boolean missingRsMaintainer = !maintainers.isRsMaintainer(mntBy);
            if (!missingRsMaintainer) {
                return parentObject;
            }
        }

        return null;
    }

    private void checkAuthorisationForStatus(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject updatedObject) {
        final Set<CIString> mntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);

        final Inet6numStatus currentStatus = Inet6numStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
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
    private boolean allChildrenHaveCorrectStatus(final PreparedUpdate update, final UpdateContext updateContext, final Ipv6Resource ipInterval) {
        final List<Ipv6Entry> children = ipv6Tree.findFirstMoreSpecific(ipInterval);

        final Inet6numStatus updatedStatus = Inet6numStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(STATUS));

        // TODO: [ES] use status index to lookup status of children (don't load entire objects)
        for (final IpEntry child : children) {
            final RpslObject childObject = objectDao.getById(child.getObjectId());

            final Inet6numStatus childStatus = Inet6numStatus.getStatusFor(childObject.getValueForAttribute(STATUS));

            final Set<CIString> childMntBy = childObject.getValuesForAttribute(AttributeType.MNT_BY);
            final boolean hasRsMaintainer = maintainers.isRsMaintainer(childMntBy);

            if (!childStatus.worksWithParentStatus(updatedStatus, hasRsMaintainer)) {
                updateContext.addMessage(update, UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), childObject.getKey()));
                return false;
            }
        }
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
