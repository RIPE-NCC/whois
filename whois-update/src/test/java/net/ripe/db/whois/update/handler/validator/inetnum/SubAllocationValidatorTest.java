package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.domain.attrs.InetnumStatus;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubAllocationValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @InjectMocks SubAllocationValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.CREATE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.INETNUM));
    }

    @Test
    public void validate_ASSIGNED_PI() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum:        193.0.0.0 - 193.0.7.255\n" +
                "netname:        RIPE-NCC\n" +
                "status:         ASSIGNED PI\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_UNKNOWN() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum:        193.0.0.0 - 193.0.7.255\n" +
                "netname:        RIPE-NCC\n" +
                "status:         SOME_UNKNOWN_STATUS\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_SUB_ALLOCATED_PA_prefix_21() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum:        193.0.0.0 - 193.0.7.255\n" +
                "netname:        RIPE-NCC\n" +
                "status:         SUB-ALLOCATED PA\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_SUB_ALLOCATED_PA_prefix_24() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum:        193.0.0.0 - 193.0.0.255\n" +
                "netname:        RIPE-NCC\n" +
                "status:         SUB-ALLOCATED PA\n"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void validate_SUB_ALLOCATED_PA_prefix_25() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "inetnum:        193.0.0.0 - 193.0.0.127\n" +
                "netname:        RIPE-NCC\n" +
                "status:         SUB-ALLOCATED PA\n"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.rangeTooSmallForStatus(InetnumStatus.SUB_ALLOCATED_PA, 24));
    }
}
