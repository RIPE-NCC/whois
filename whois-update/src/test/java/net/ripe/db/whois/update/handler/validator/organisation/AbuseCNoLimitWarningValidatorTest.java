package net.ripe.db.whois.update.handler.validator.organisation;

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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbuseCNoLimitWarningValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @InjectMocks AbuseCNoLimitWarningValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.ROLE));
    }

    @Test
    public void role_has_added_abuse_mailbox_create() {
        final RpslObject role = RpslObject.parse("role: Some Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@ripe.net");

        when(update.hasOriginalObject()).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(role);
        when(update.getUpdatedObject()).thenReturn(role);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseCNoLimitWarning());
    }

    @Test
    public void role_has_added_abuse_mailbox_modify() {
        when(update.hasOriginalObject()).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: TEST-NIC"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@ripe.net"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseCNoLimitWarning());
    }

    @Test
    public void role_has_not_added_abuse_mailbox() {
        when(update.hasOriginalObject()).thenReturn(true);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@ripe.net"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("role: Some Role\nnic-hdl: TEST-NIC\nabuse-mailbox: abuse@ripe.net"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.abuseCNoLimitWarning());
    }
}
