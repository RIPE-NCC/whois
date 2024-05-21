package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.AGGREGATED_BY_LIR;
import static net.ripe.db.whois.update.domain.Action.CREATE;
import static net.ripe.db.whois.update.domain.Action.MODIFY;

@Component
public class InetnumAggregatedByLirStatusValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.INETNUM);

    private static final int MAX_ALLOWED_AGGREGATED_BY_LIR = 2;
    private static final int MAX_ASSIGNMENT_SIZE = 32;

    private final Ipv4Tree ipv4Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public InetnumAggregatedByLirStatusValidator(final Ipv4Tree ipv4Tree, final RpslObjectDao rpslObjectDao) {
        this.ipv4Tree = ipv4Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject object = update.getUpdatedObject();
        final InetnumStatus status = InetnumStatus.getStatusFor(object.getValueForAttribute(AttributeType.STATUS));

        if (AGGREGATED_BY_LIR != status) {
            return addMessagesForAttributeAssignmentSizeNotAllowed(update.getUpdatedObject());
        }

        if(update.getAction()== MODIFY) {
            return assignmentSizeHasChanged(update) ? Lists.newArrayList(UpdateMessages.cantChangeAssignmentSize())
                                                     : Collections.emptyList();
        }

        return validateCreate(object);
    }

    private List<Message> validateCreate(final RpslObject object) {
        final List<Message> messages = Lists.newArrayList();

        final Ipv4Resource ipv4Resource = Ipv4Resource.parse(object.getKey());

        validateAssignmentSizeIfAvailable(object, ipv4Resource, messages);
        validTotalNrAggregatedByLirInHierarchy(ipv4Resource, messages);

        validatePrefixLengthForParent(ipv4Resource, messages);

        return messages;
    }

    private void validateAssignmentSizeIfAvailable(final RpslObject object, final Ipv4Resource ipv4Resource, final List<Message> messages) {
        if (!object.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            return;
        }

        final int assignmentSize = object.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
        if (assignmentSize > MAX_ASSIGNMENT_SIZE) {
            messages.add(UpdateMessages.assignmentSizeTooLarge(MAX_ASSIGNMENT_SIZE));
            return;
        }

        if (assignmentSize <= ipv4Resource.getPrefixLength()) {
            messages.add(UpdateMessages.assignmentSizeTooSmall(ipv4Resource.getPrefixLength()));
            return;
        }

        for (final Ipv4Entry child : ipv4Tree.findFirstMoreSpecific(ipv4Resource)) {
            final Ipv4Resource childIpv4Resource = child.getKey();
            if (childIpv4Resource.getPrefixLength() != assignmentSize) {
                        messages.add(UpdateMessages.invalidChildPrefixLength());
            }
        }
    }

    private void validatePrefixLengthForParent(final Ipv4Resource ipv4Resource, final List<Message> messages) {
        final List<Ipv4Entry> parents = ipv4Tree.findFirstLessSpecific(ipv4Resource);
        Validate.notEmpty(parents, "Parent must always exist");
        final RpslObject parent = rpslObjectDao.getById(parents.get(0).getObjectId());

        final InetStatus parentStatus = InetStatusHelper.getStatus(parent);

        if (AGGREGATED_BY_LIR == parentStatus && parent.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            final int parentAssignmentSize = parent.getValueForAttribute(AttributeType.ASSIGNMENT_SIZE).toInt();
            final int prefixLength = ipv4Resource.getPrefixLength();
            if (prefixLength != parentAssignmentSize) {
                messages.add(UpdateMessages.invalidPrefixLength(ipv4Resource, parentAssignmentSize));
            }
        }
    }

    private void validTotalNrAggregatedByLirInHierarchy(final Ipv4Resource ipv4Resource, final List<Message> messages) {
        int remaining = MAX_ALLOWED_AGGREGATED_BY_LIR - 1;

        for (final Ipv4Entry parentEntry : Lists.reverse(ipv4Tree.findAllLessSpecific(ipv4Resource))) {
            if (isAggregatedByLir(parentEntry) && remaining-- == 0) {
                messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
                return;
            }
        }

        if (!validChildNrAggregatedByLir(ipv4Resource, remaining)) {
            messages.add(UpdateMessages.tooManyAggregatedByLirInHierarchy());
        }
    }

    private boolean validChildNrAggregatedByLir(final Ipv4Resource ipv4Resource, final int remaining) {
        for (final Ipv4Entry childEntry : ipv4Tree.findFirstMoreSpecific(ipv4Resource)) {
            if (isAggregatedByLir(childEntry) && (remaining == 0 || !validChildNrAggregatedByLir(childEntry.getKey(), remaining - 1))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAggregatedByLir(final Ipv4Entry entry) {
        final RpslObject object = rpslObjectDao.getById(entry.getObjectId());
        final InetnumStatus status = InetnumStatus.getStatusFor(object.getValueForAttribute(AttributeType.STATUS));
        return AGGREGATED_BY_LIR == status;
    }

    private List<Message> addMessagesForAttributeAssignmentSizeNotAllowed(final RpslObject object) {
        if(!object.containsAttribute(AttributeType.ASSIGNMENT_SIZE)) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(UpdateMessages.attributeAssignmentSizeNotAllowed(object.findAttribute(AttributeType.ASSIGNMENT_SIZE)));
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
