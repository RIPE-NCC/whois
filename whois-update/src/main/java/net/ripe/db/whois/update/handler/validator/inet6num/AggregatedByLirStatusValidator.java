package net.ripe.db.whois.update.handler.validator.inet6num;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper;
import org.apache.commons.lang.Validate;

import java.util.List;

import static net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus.AGGREGATED_BY_LIR;
import static net.ripe.db.whois.update.domain.Action.CREATE;

public abstract class AggregatedByLirStatusValidator<K extends IpInterval<K>, V extends IpEntry<K>> implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE, Action.MODIFY);

    private static final int MAX_ALLOWED_AGGREGATED_BY_LIR = 2;

    private final IpTree<K, V> ipTree;
    private final RpslObjectDao rpslObjectDao;

    public AggregatedByLirStatusValidator(final IpTree<K, V> ipTree, final RpslObjectDao rpslObjectDao) {
        this.ipTree = ipTree;
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
        final K resource = createResource(object.getKey());
        final List<Message> messages = Lists.newArrayList();

        final InetStatus status = InetStatusHelper.getStatus(object);
        if (AGGREGATED_BY_LIR.toString().equals(status.toString())) {
            validateRequiredAssignmentSize(object, resource, messages);
            validTotalNrAggregatedByLirInHierarchy(resource, messages);
        } else {
            addMessagesForAttributeAssignmentSizeNotAllowed(object, messages);
        }

        validatePrefixLengthForParent(resource, messages);

        return messages;
    }

    private void validateRequiredAssignmentSize(final RpslObject object, final K ipResource, final List<Message> messages) {

        if (object.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            final int assignmentSize = object.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            if (assignmentSize > getMaxAssignmentSize()) {
                messages.add(UpdateMessages.assignmentSizeTooLarge(getMaxAssignmentSize()));
            } else if (assignmentSize <= ipResource.getPrefixLength()) {
                messages.add(UpdateMessages.assignmentSizeTooSmall(ipResource.getPrefixLength()));
            } else {
                for (final V child : ipTree.findFirstMoreSpecific(ipResource)) {
                    final K childIpResource = child.getKey();
                    if (childIpResource.getPrefixLength() != assignmentSize) {
                        messages.add(UpdateMessages.invalidChildPrefixLength());
                    }
                }
            }
        } else if (isAssignmentSizeAttributeMandatory()){
            messages.add(ValidationMessages.missingConditionalRequiredAttribute(AttributeType.ASSIGNMENT_SIZE));
        }
    }

    private void validatePrefixLengthForParent(final K ipResource, final List<Message> messages) {
        final List<V> parents = ipTree.findFirstLessSpecific(ipResource);
        Validate.notEmpty(parents, "Parent must always exist");
        final RpslObject parent = rpslObjectDao.getById(parents.get(0).getObjectId());

        final InetStatus parentStatus = InetStatusHelper.getStatus(parent);

        if (AGGREGATED_BY_LIR.toString().equals(parentStatus.toString()) && parent.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            final int parentAssignmentSize = parent.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            final int prefixLength = ipResource.getPrefixLength();
            if (prefixLength != parentAssignmentSize) {
                messages.add(UpdateMessages.invalidPrefixLength(ipResource, parentAssignmentSize));
            }
        }
    }

    private void validTotalNrAggregatedByLirInHierarchy(final K ipResource, final List<Message> messages) {
        int remaining = MAX_ALLOWED_AGGREGATED_BY_LIR - 1;

        for (final V parentEntry : Lists.reverse(ipTree.findAllLessSpecific(ipResource))) {
            if (isAggregatedByLir(parentEntry) && remaining-- == 0) {
                messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
                return;
            }
        }

        if (!validChildNrAggregatedByLir(ipResource, remaining)) {
            messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
        }
    }

    private boolean validChildNrAggregatedByLir(final K ipv6Resource, final int remaining) {
        for (final V childEntry : ipTree.findFirstMoreSpecific(ipv6Resource)) {
            if (isAggregatedByLir(childEntry) && (remaining == 0 || !validChildNrAggregatedByLir(childEntry.getKey(), remaining - 1))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAggregatedByLir(final V entry) {
        final RpslObject object = rpslObjectDao.getById(entry.getObjectId());
        final InetStatus status = InetStatusHelper.getStatus(object);
        return AGGREGATED_BY_LIR.toString().equals(status.toString());
    }

    private  List<Message> validateModify(final PreparedUpdate update) {
        final List<Message> customValidationMessages = Lists.newArrayList();

        final InetStatus status = InetStatusHelper.getStatus(update.getUpdatedObject());
        if (assignmentSizeHasChanged(update)) {
            if(AGGREGATED_BY_LIR.toString().equals(status.toString())) {
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


    final K createResource(CIString key) {
        return createResource(key.toString());
    }

    public abstract K createResource(String key);

    public abstract int getMaxAssignmentSize();
    public abstract boolean isAssignmentSizeAttributeMandatory();

}
