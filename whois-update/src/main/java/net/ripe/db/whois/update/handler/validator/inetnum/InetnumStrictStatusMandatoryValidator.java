package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import static net.ripe.db.whois.update.domain.Action.CREATE;

/**
 * Apply stricter status validation when creating an inetnum object.
 */
@Component
public class InetnumStrictStatusMandatoryValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    private final RpslObjectDao objectDao;
    private final StatusDao statusDao;
    private final Ipv4Tree ipv4Tree;
    private final Maintainers maintainers;

    @Autowired
    public InetnumStrictStatusMandatoryValidator(
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
        
        if (!allChildrenHaveCorrectStatus(update, ipInterval, validationMessages)) {
            return validationMessages;
        }

        final List<Ipv4Entry> parents = ipv4Tree.findFirstLessSpecific(ipInterval);
        if (parents.size() != 1) {
            validationMessages.add(UpdateMessages.invalidParentEntryForInterval(ipInterval));
            return validationMessages;
        }

        final InetnumStatus currentStatus = InetnumStatus.getStatusFor(updatedObject.getValueForAttribute(STATUS));
        final InetnumStatus parentStatus = InetnumStatus.getStatusFor(statusDao.getStatus(parents.get(0).getObjectId()));

        final Set<CIString> updateMntBy = updatedObject.getValuesForAttribute(AttributeType.MNT_BY);
        final boolean hasRsMaintainer = maintainers.isRsMaintainer(updateMntBy);

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PA) && parentStatus.equals(InetnumStatus.ASSIGNED_PA)) {
            if (checkAuthorizationForStatusInHierarchy(update,ipInterval)) {
               validationMessages.add(UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        } else {
            if (!currentStatus.worksWithParentStatus(parentStatus, hasRsMaintainer)) {
                validationMessages.add(UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        }

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PI) && parentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
            final RpslObject parentObject = objectDao.getById(parents.get(0).getObjectId());

            final Set<CIString> parentMntBy = parentObject.getValuesForAttribute(AttributeType.MNT_BY);
            final boolean parentHasRsMaintainer = maintainers.isRsMaintainer(parentMntBy);

            if (parentHasRsMaintainer) {
                validationMessages.add(UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        }

        if (currentStatus.equals(InetnumStatus.ASSIGNED_PI)) {
            if(checkAuthorizationForStatusInHierarchy(update,ipInterval)) {
                validationMessages.add(UpdateMessages.incorrectParentStatus(ERROR, updatedObject.getType(), parentStatus.toString()));
            }
        }

        return validationMessages;
    }

    private boolean checkAuthorizationForStatusInHierarchy(final PreparedUpdate update, final Ipv4Resource ipInterval) {
        final RpslObject parentInHierarchyMaintainedByRs = findParentWithRsMaintainer(ipInterval);

        if (parentInHierarchyMaintainedByRs != null) {
            final List<RpslAttribute> parentStatuses = parentInHierarchyMaintainedByRs.findAttributes(STATUS);
            if (parentStatuses.isEmpty()) {
                return false;
            }

            final CIString parentStatusValue = parentStatuses.get(0).getCleanValue();
            final InetnumStatus parentStatus = InetnumStatus.getStatusFor(parentStatusValue);

            final Set<CIString> mntLower = parentInHierarchyMaintainedByRs.getValuesForAttribute(AttributeType.MNT_LOWER);
            final boolean parentHasRsMntLower = maintainers.isRsMaintainer(mntLower);

            final InetnumStatus currentStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(STATUS));
            if (!currentStatus.worksWithParentInHierarchy(parentStatus, parentHasRsMntLower)) {
               return true;
            }
        }

        return false;
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

    @SuppressWarnings("unchecked")
    private boolean allChildrenHaveCorrectStatus(final PreparedUpdate update, final Ipv4Resource ipInterval, final List<Message> validationMessages) {
        final InetnumStatus updatedStatus = InetnumStatus.getStatusFor(update.getUpdatedObject().getValueForAttribute(STATUS));

        final List<Ipv4Entry> children = ipv4Tree.findFirstMoreSpecific(ipInterval);
        final List<Integer> childrenObjectIds = Lists.transform(children, IpEntry::getObjectId);
        final Map<Integer, CIString> childStatusMap = statusDao.getStatus(childrenObjectIds);

        for (final Ipv4Entry child : children) {
            final InetnumStatus childStatus = InetnumStatus.getStatusFor(childStatusMap.get(child.getObjectId()));

            if (!childStatus.worksWithParentStatus(updatedStatus, childHasRsMaintainer(child, childStatus))) {
                validationMessages.add(UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), child.getKey().toRangeString()));
                return false;
            } else if (updatedStatus.equals(InetnumStatus.ASSIGNED_PA) && childStatus.equals(InetnumStatus.ASSIGNED_PA)) {
                if(checkAuthorizationForStatusInHierarchy(update, ipInterval )) {
                   validationMessages.add(UpdateMessages.incorrectChildStatus(ERROR, updatedStatus.toString(), childStatus.toString(), child.getKey().toRangeString()));
                }
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

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
