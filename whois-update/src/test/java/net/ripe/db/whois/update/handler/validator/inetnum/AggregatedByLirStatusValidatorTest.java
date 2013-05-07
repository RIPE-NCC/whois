package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AggregatedByLirStatusValidatorTest {
    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate update;

    @Mock Ipv6Tree ipv6Tree;
    @Mock RpslObjectDao rpslObjectDao;
    @InjectMocks AggregatedByLirStatusValidator subject;

    @Before
    public void setUp() throws Exception {
        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(new Ipv6Entry(Ipv6Resource.parse("::0/0"), 0)));
        when(rpslObjectDao.getById(0)).thenReturn(RpslObject.parse("" + "" +
                "inet6num:       0::/0\n" +
                "netname:        IANA-BLK\n" +
                "status:         ALLOCATED-BY-RIR\n" +
                "source:         TEST"));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.INET6NUM));
    }

    @Test
    public void validate_no_aggregated_by_lir() {
        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          ASSIGNED");

        when(update.getUpdatedObject()).thenReturn(object);
        when(update.getAction()).thenReturn(Action.CREATE);
        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_no_aggregated_by_lir_with_assignment_size() {
        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          ASSIGNED\n" +
                "assignment-size: 64");

        when(update.getUpdatedObject()).thenReturn(object);
        when(update.getAction()).thenReturn(Action.CREATE);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, object.findAttribute(AttributeType.ASSIGNMENT_SIZE), UpdateMessages.attributeAssignmentSizeNotAllowed());
    }

    @Test
    public void validate_no_assignment_size() {
        validate_assignment(RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR"),
                ValidationMessages.missingConditionalRequiredAttribute(AttributeType.ASSIGNMENT_SIZE));
    }

    @Test
    public void validate_assignment_size_smaller_than_prefix_length() {
        validate_assignment(RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 24"),
                UpdateMessages.assignmentSizeTooSmall(48));
    }


    @Test
    public void validate_assignment_size_equals_to_prefix_length() {
        validate_assignment(RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 48"),
                UpdateMessages.assignmentSizeTooSmall(48));
    }

    @Test
    public void validate_assignment_size_greater_than_maximum_prefix_length() {
        validate_assignment(RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 129"),
                UpdateMessages.assignmentSizeTooLarge(128));
    }

    private void validate_assignment(final RpslObject object, final Message expectedMessage) {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);

        when(ipv6Tree.findFirstLessSpecific(any(Ipv6Resource.class))).thenReturn(Lists.newArrayList(new Ipv6Entry(Ipv6Resource.parse("::0/0"), 1)));
        when(rpslObjectDao.getById(1)).thenReturn(RpslObject.parse("" +
                "inet6num: 0::/0\n" +
                "status:   ALLOCATED-BY-RIR"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, expectedMessage);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_parent_assignment_size() {
        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 64");

        final Ipv6Resource key = Ipv6Resource.parse("2001:0658:021A::/48");

        final RpslObject parent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/32\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 40");

        final Ipv6Entry ipv6Entry = new Ipv6Entry(Ipv6Resource.parse(parent.getKey()), 1);
        when(update.getAction()).thenReturn(Action.CREATE);
        when(ipv6Tree.findFirstLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6Entry));
        when(rpslObjectDao.getById(1)).thenReturn(parent);

        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidPrefixLength(Ipv6Resource.parse(object.getKey()), 40));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_aggregatedByLir_in_parent_and_grandparent() {
        final Ipv6Resource key = Ipv6Resource.parse("2001:0658:021A::/48");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 64");

        final RpslObject parent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/32\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 48");

        final RpslObject grandParent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/16\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 32");

        final Ipv6Entry ipv6EntryParent = new Ipv6Entry(Ipv6Resource.parse(parent.getKey()), 1);
        final Ipv6Entry ipv6EntryGrandParent = new Ipv6Entry(Ipv6Resource.parse(grandParent.getKey()), 2);

        when(update.getAction()).thenReturn(Action.CREATE);
        when(ipv6Tree.findFirstLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findAllLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent, ipv6EntryGrandParent));
        when(rpslObjectDao.getById(1)).thenReturn(parent);
        when(rpslObjectDao.getById(2)).thenReturn(grandParent);

        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.tooManyAggregatedByLirInHierarchy());
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_aggregatedByLir_in_parent_and_child() {
        final Ipv6Resource key = Ipv6Resource.parse("2001:0658:021A::/48");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 64");

        final RpslObject parent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/32\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 48");

        final RpslObject child = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/64\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 128");

        final Ipv6Entry ipv6EntryParent = new Ipv6Entry(Ipv6Resource.parse(parent.getKey()), 1);
        final Ipv6Entry ipv6EntryChild = new Ipv6Entry(Ipv6Resource.parse(child.getKey()), 2);

        when(update.getAction()).thenReturn(Action.CREATE);
        when(ipv6Tree.findFirstLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findAllLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findFirstMoreSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryChild));
        when(rpslObjectDao.getById(1)).thenReturn(parent);
        when(rpslObjectDao.getById(2)).thenReturn(child);

        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.tooManyAggregatedByLirInHierarchy());
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_aggregatedByLir_in_child_and_grandchild() {
        final Ipv6Resource key = Ipv6Resource.parse("2001:0658:021A::/48");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 64");

        final RpslObject parent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/32\n" +
                "status:          ALLOCATED-BY-RIR");

        final RpslObject child = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/64\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 68");

        final RpslObject grandChild = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/68\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 128");

        final Ipv6Entry ipv6EntryParent = new Ipv6Entry(Ipv6Resource.parse(parent.getKey()), 1);
        final Ipv6Entry ipv6EntryChild = new Ipv6Entry(Ipv6Resource.parse(child.getKey()), 2);
        final Ipv6Entry ipv6EntryGrandChild = new Ipv6Entry(Ipv6Resource.parse(grandChild.getKey()), 3);

        when(ipv6Tree.findFirstLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findAllLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findFirstMoreSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryChild));
        when(ipv6Tree.findFirstMoreSpecific(ipv6EntryChild.getKey())).thenReturn(Lists.newArrayList(ipv6EntryGrandChild));
        when(rpslObjectDao.getById(1)).thenReturn(parent);
        when(rpslObjectDao.getById(2)).thenReturn(child);
        when(rpslObjectDao.getById(3)).thenReturn(grandChild);

        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.tooManyAggregatedByLirInHierarchy());
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void validate_child_prefix_length() {
        final Ipv6Resource key = Ipv6Resource.parse("2001:0658:021A::/48");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/48\n" +
                "status:          AGGREGATED-BY-LIR\n" +
                "assignment-size: 64");

        final RpslObject parent = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/32\n" +
                "status:          ALLOCATED-BY-RIR");

        final RpslObject child = RpslObject.parse("" +
                "inet6num:        2001:0658:021A::/128\n" +
                "status:          ASSIGNED");

        final Ipv6Entry ipv6EntryParent = new Ipv6Entry(Ipv6Resource.parse(parent.getKey()), 1);
        final Ipv6Entry ipv6EntryChild = new Ipv6Entry(Ipv6Resource.parse(child.getKey()), 2);

        when(rpslObjectDao.getById(1)).thenReturn(parent);
        when(rpslObjectDao.getById(2)).thenReturn(child);

        when(ipv6Tree.findFirstLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findAllLessSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryParent));
        when(ipv6Tree.findFirstMoreSpecific(key)).thenReturn(Lists.newArrayList(ipv6EntryChild));

        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(object);
        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidChildPrefixLength());
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_assignmentSize_has_not_changed() {
        when(update.getAction()).thenReturn(Action.MODIFY);

        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nassignment-size: 48\nstatus:AGGREGATED-BY-LIR"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nassignment-size: 48\nstatus:AGGREGATED-BY-LIR"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_assignmentSize_has_changed() {
        when(update.getAction()).thenReturn(Action.MODIFY);

        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nstatus:AGGREGATED-BY-LIR"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nassignment-size: 48\nstatus:AGGREGATED-BY-LIR"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.cantChangeAssignmentSize());
    }

    @Test
    public void modify_permissive_status() {
        when(update.getAction()).thenReturn(Action.MODIFY);

        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nstatus:ALLOCATED-BY-LIR"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nassignment-size: 48\nstatus:ALLOCATED-BY-LIR"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void create_has_assSize_when_it_should_not() {
        when(update.getAction()).thenReturn(Action.CREATE);
        final RpslObject rpslObject = RpslObject.parse("inet6num: ffee::/48\nassignment-size: 48\nstatus:ALLOCATED-BY-LIR");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.ASSIGNMENT_SIZE), UpdateMessages.attributeAssignmentSizeNotAllowed());
    }

    @Test
    public void create_has_no_assSize_when_it_should_not() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nstatus:ALLOCATED-BY-LIR"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void create_succeeds() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffee::/48\nstatus:ASSIGNED"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
