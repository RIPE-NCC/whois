package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.Inet6numStatus;
import net.ripe.db.whois.common.domain.attrs.InetStatus;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AggregatedByLirStatusValidator implements BusinessRuleValidator {
    private static final int MAX_ALLOWED_AGGREGATED_BY_LIR = 2;
    private static final int MAX_ASSIGNMENT_SIZE = 128;

    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public AggregatedByLirStatusValidator(final Ipv6Tree ipv6Tree, final RpslObjectDao rpslObjectDao) {
        this.ipv6Tree = ipv6Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.INET6NUM);
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getAction().equals(Action.CREATE)) {
            validateCreate(update, updateContext);
        } else {
            validateModify(update, updateContext);
        }
    }

    private void validateCreate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject object = update.getUpdatedObject();
        final Ipv6Resource ipv6Resource = Ipv6Resource.parse(object.getKey());

        final Inet6numStatus status = Inet6numStatus.getStatusFor(object.getValueForAttribute(AttributeType.STATUS));
        if (status.equals(Inet6numStatus.AGGREGATED_BY_LIR)) {
            validateRequiredAssignmentSize(update, updateContext, object, ipv6Resource);
            validTotalNrAggregatedByLirInHierarchy(update, updateContext, ipv6Resource);
        } else {
            for (final RpslAttribute attribute : object.findAttributes(AttributeType.ASSIGNMENT_SIZE)) {
                updateContext.addMessage(update, attribute, UpdateMessages.attributeAssignmentSizeNotAllowed());
            }
        }

        validatePrefixLengthForParent(update, updateContext, ipv6Resource);
    }

    private void validateRequiredAssignmentSize(final PreparedUpdate update, final UpdateContext updateContext, final RpslObject object, final Ipv6Resource ipv6Resource) {
        if (object.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            final int assignmentSize = object.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            if (assignmentSize > MAX_ASSIGNMENT_SIZE) {
                updateContext.addMessage(update, UpdateMessages.assignmentSizeTooLarge(MAX_ASSIGNMENT_SIZE));
            } else if (assignmentSize <= ipv6Resource.getPrefixLength()) {
                updateContext.addMessage(update, UpdateMessages.assignmentSizeTooSmall(ipv6Resource.getPrefixLength()));
            } else {
                for (final Ipv6Entry child : ipv6Tree.findFirstMoreSpecific(ipv6Resource)) {
                    final Ipv6Resource childIpv6Resource = child.getKey();
                    if (childIpv6Resource.getPrefixLength() != assignmentSize) {
                        updateContext.addMessage(update, UpdateMessages.invalidChildPrefixLength());
                    }
                }
            }
        } else {
            updateContext.addMessage(update, ValidationMessages.missingConditionalRequiredAttribute(AttributeType.ASSIGNMENT_SIZE));
        }
    }

    private void validatePrefixLengthForParent(final PreparedUpdate update, final UpdateContext updateContext, final Ipv6Resource ipv6Resource) {
        final List<Ipv6Entry> parents = ipv6Tree.findFirstLessSpecific(ipv6Resource);
        Validate.notEmpty(parents, "Parent must always exist");
        final RpslObject parent = rpslObjectDao.getById(parents.get(0).getObjectId());

        final InetStatus parentStatus = InetStatusHelper.getStatus(parent);
        if (parentStatus == null) {
            updateContext.addMessage(update, UpdateMessages.objectHasInvalidStatus("Parent", parent.getKey(), parent.getValueForAttribute(AttributeType.STATUS)));
            return;
        }

        if (parentStatus.equals(Inet6numStatus.AGGREGATED_BY_LIR)) {
            final int parentAssignmentSize = parent.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            final int prefixLength = ipv6Resource.getPrefixLength();
            if (prefixLength != parentAssignmentSize) {
                updateContext.addMessage(update, UpdateMessages.invalidPrefixLength(ipv6Resource, parentAssignmentSize));
            }
        }
    }

    private void validTotalNrAggregatedByLirInHierarchy(final PreparedUpdate update, final UpdateContext updateContext, final Ipv6Resource ipv6Resource) {
        int remaining = MAX_ALLOWED_AGGREGATED_BY_LIR - 1;

        for (final Ipv6Entry parentEntry : Lists.reverse(ipv6Tree.findAllLessSpecific(ipv6Resource))) {
            if (isAggregatedByLir(parentEntry) && remaining-- == 0) {
                updateContext.addMessage(update, UpdateMessages.tooManyAggregatedByLirInHierarchy());
                return;
            }
        }

        if (!validChildNrAggregatedByLir(ipv6Resource, remaining)) {
            updateContext.addMessage(update, UpdateMessages.tooManyAggregatedByLirInHierarchy());
        }
    }

    private boolean validChildNrAggregatedByLir(final Ipv6Resource ipv6Resource, final int remaining) {
        for (final Ipv6Entry childEntry : ipv6Tree.findFirstMoreSpecific(ipv6Resource)) {
            if (isAggregatedByLir(childEntry) && (remaining == 0 || !validChildNrAggregatedByLir(childEntry.getKey(), remaining - 1))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAggregatedByLir(final Ipv6Entry entry) {
        final RpslObject object = rpslObjectDao.getById(entry.getObjectId());
        final Inet6numStatus status = Inet6numStatus.getStatusFor(object.getValueForAttribute(AttributeType.STATUS));
        return Inet6numStatus.AGGREGATED_BY_LIR.equals(status);
    }

    private void validateModify(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslAttribute updatedStatus = update.getUpdatedObject().findAttribute(AttributeType.STATUS);

        final Inet6numStatus inet6numStatus = Inet6numStatus.getStatusFor(updatedStatus.getCleanValue());
        if (inet6numStatus.equals(Inet6numStatus.AGGREGATED_BY_LIR) && assignmentSizeHasChanged(update)) {
            updateContext.addMessage(update, UpdateMessages.cantChangeAssignmentSize());
        }
    }

    private boolean assignmentSizeHasChanged(final PreparedUpdate update) {
        final List<RpslAttribute> originalAssignmentSize = update.getReferenceObject().findAttributes(AttributeType.ASSIGNMENT_SIZE);
        final List<RpslAttribute> updatedAssignmentSize = update.getUpdatedObject().findAttributes(AttributeType.ASSIGNMENT_SIZE);

        return !(originalAssignmentSize.size() == updatedAssignmentSize.size() &&
                Sets.difference(Sets.newLinkedHashSet(originalAssignmentSize), Sets.newLinkedHashSet(updatedAssignmentSize)).size() == 0);
    }
}
