package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus.AGGREGATED_BY_LIR;
import static net.ripe.db.whois.update.domain.Action.CREATE;

@Component
public class AggregatedByLirStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INET6NUM);

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
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        if (update.getAction()== CREATE) {
            return validateCreate(update);
        }
        return validateModify(update);
    }

    private List<Message> validateCreate(final PreparedUpdate update) {
        final RpslObject object = update.getUpdatedObject();
        final Ipv6Resource ipv6Resource = Ipv6Resource.parse(object.getKey());
        final List<Message> messages = Lists.newArrayList();

        final Inet6numStatus status = Inet6numStatus.getStatusFor(object.getValueForAttribute(AttributeType.STATUS));
        if (AGGREGATED_BY_LIR == status) {
            validateRequiredAssignmentSize(object, ipv6Resource, messages);
            validTotalNrAggregatedByLirInHierarchy(ipv6Resource, messages);
        } else {
            addMessagesForAttributeAssignmentSizeNotAllowed(object, messages);
        }

        validatePrefixLengthForParent(ipv6Resource, messages);

        return messages;
    }

    private void validateRequiredAssignmentSize(final RpslObject object, final Ipv6Resource ipv6Resource, final List<Message> messages) {
        if (object.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            final int assignmentSize = object.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            if (assignmentSize > MAX_ASSIGNMENT_SIZE) {
                messages.add(UpdateMessages.assignmentSizeTooLarge(MAX_ASSIGNMENT_SIZE));
            } else if (assignmentSize <= ipv6Resource.getPrefixLength()) {
                messages.add(UpdateMessages.assignmentSizeTooSmall(ipv6Resource.getPrefixLength()));
            } else {
                for (final Ipv6Entry child : ipv6Tree.findFirstMoreSpecific(ipv6Resource)) {
                    final Ipv6Resource childIpv6Resource = child.getKey();
                    if (childIpv6Resource.getPrefixLength() != assignmentSize) {
                        messages.add(UpdateMessages.invalidChildPrefixLength());
                    }
                }
            }
        } else {
            messages.add(ValidationMessages.missingConditionalRequiredAttribute(AttributeType.ASSIGNMENT_SIZE));
        }
    }

    private void validatePrefixLengthForParent(final Ipv6Resource ipv6Resource, final List<Message> messages) {
        final List<Ipv6Entry> parents = ipv6Tree.findFirstLessSpecific(ipv6Resource);
        Validate.notEmpty(parents, "Parent must always exist");
        final RpslObject parent = rpslObjectDao.getById(parents.get(0).getObjectId());

        final InetStatus parentStatus = InetStatusHelper.getStatus(parent);

        if (AGGREGATED_BY_LIR == parentStatus) {
            final int parentAssignmentSize = parent.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            final int prefixLength = ipv6Resource.getPrefixLength();
            if (prefixLength != parentAssignmentSize) {
                messages.add(UpdateMessages.invalidPrefixLength(ipv6Resource, parentAssignmentSize));
            }
        }
    }

    private void validTotalNrAggregatedByLirInHierarchy(final Ipv6Resource ipv6Resource, final List<Message> messages) {
        int remaining = MAX_ALLOWED_AGGREGATED_BY_LIR - 1;

        for (final Ipv6Entry parentEntry : Lists.reverse(ipv6Tree.findAllLessSpecific(ipv6Resource))) {
            if (isAggregatedByLir(parentEntry) && remaining-- == 0) {
                messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
                return;
            }
        }

        if (!validChildNrAggregatedByLir(ipv6Resource, remaining)) {
            messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
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
        return AGGREGATED_BY_LIR == status;
    }

    private  List<Message> validateModify(final PreparedUpdate update) {
        final RpslAttribute updatedStatus = update.getUpdatedObject().findAttribute(AttributeType.STATUS);
        final List<Message> customValidationMessages = Lists.newArrayList();

        final Inet6numStatus inet6numStatus = Inet6numStatus.getStatusFor(updatedStatus.getCleanValue());
        if (assignmentSizeHasChanged(update)) {
            if(AGGREGATED_BY_LIR == inet6numStatus) {
                customValidationMessages.add(UpdateMessages.cantChangeAssignmentSize());
            } else {
                addMessagesForAttributeAssignmentSizeNotAllowed(update.getUpdatedObject(), customValidationMessages);
            }
        }

        return customValidationMessages;
    }

    private void addMessagesForAttributeAssignmentSizeNotAllowed(final RpslObject object, final List<Message> messages) {
        for (final RpslAttribute attribute : object.findAttributes(AttributeType.ASSIGNMENT_SIZE)) {
            messages.add(UpdateMessages.attributeAssignmentSizeNotAllowed(attribute));
        }
    }

    private boolean assignmentSizeHasChanged(final PreparedUpdate update) {
        final List<RpslAttribute> originalAssignmentSize = update.getReferenceObject().findAttributes(AttributeType.ASSIGNMENT_SIZE);
        final List<RpslAttribute> updatedAssignmentSize = update.getUpdatedObject().findAttributes(AttributeType.ASSIGNMENT_SIZE);

        return !(originalAssignmentSize.size() == updatedAssignmentSize.size() &&
                Sets.difference(Sets.newLinkedHashSet(originalAssignmentSize), Sets.newLinkedHashSet(updatedAssignmentSize)).size() == 0);
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
