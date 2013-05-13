package net.ripe.db.whois.update.handler.validator.personrole;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MustKeepAbuseMailboxIfReferencedValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock JdbcRpslObjectUpdateDao updateDao;
    @Mock JdbcRpslObjectDao objectDao;

    @InjectMocks MustKeepAbuseMailboxIfReferencedValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.ROLE));
    }

    @Test
    public void add_referenced_abuse_mailbox() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@test.net"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void remove_unreferenced_abuse_mailbox() {
        final RpslObject originalObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@test.net");
        final RpslObject updatedObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC");

        when(update.getReferenceObject()).thenReturn(originalObject);
        when(update.getUpdatedObject()).thenReturn(updatedObject);
        when(updateDao.getReferences(updatedObject)).thenReturn(Sets.<RpslObjectInfo>newHashSet());
        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_referenced_abuse_mailbox() {
        final RpslObject originalObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@test.net");
        final RpslObject updatedObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse2@test.net");

        when(update.getReferenceObject()).thenReturn(originalObject);
        when(update.getUpdatedObject()).thenReturn(updatedObject);
        when(updateDao.getReferences(updatedObject)).thenReturn(Sets.newHashSet(new RpslObjectInfo(1, ObjectType.ORGANISATION, "ORG-TEST1")));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("organisation: ORG-TEST1\nabuse-c: TEST-NIC"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void remove_referenced_abuse_mailbox() {
        final RpslObject originalObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@test.net");
        final RpslObject updatedObject = RpslObject.parse("role: Abuse Role\nnic-hdl: TEST-NIC");

        when(update.getReferenceObject()).thenReturn(originalObject);
        when(update.getUpdatedObject()).thenReturn(updatedObject);
        when(updateDao.getReferences(updatedObject)).thenReturn(Sets.newHashSet(new RpslObjectInfo(1, ObjectType.ORGANISATION, "ORG-TEST1")));
        when(objectDao.getById(1)).thenReturn(RpslObject.parse("organisation: ORG-TEST1\nabuse-c: TEST-NIC"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseMailboxReferenced("Abuse Role"));
    }
}
