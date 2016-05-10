package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LirRipeMaintainedAttributesValidatorTest {

    @Mock
    PreparedUpdate update;
    @Mock
    UpdateContext updateContext;
    @Mock
    Subject authenticationSubject;
    @Mock
    Maintainers maintainers;

    @InjectMocks
    LirRipeMaintainedAttributesValidator subject;

    @Before
    public void setup() {
        when(maintainers.getPowerMaintainers()).thenReturn(ciSet("POWER-MNT"));
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(1));
        assertTrue(subject.getActions().contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(1));
        assertTrue(subject.getTypes().contains(ObjectType.ORGANISATION));
    }

    @Test
    public void update_of_non_lir() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(NON_LIR_ORG_CHANGED);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_address() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ADDRESS);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ADDRESS));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void update_of_phone() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_PHONE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.PHONE));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_fax() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_FAX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.FAX_NO));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_email() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_EMAIL);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.E_MAIL));
        verifyNoMoreInteractions(updateContext);
    }


    @Test
    public void update_of_mntby() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MNT_BY);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_NAME));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_type() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_TYPE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_TYPE));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_abuse_mailbox() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ABUSE_MAILBOX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ABUSE_MAILBOX));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_address_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ADDRESS);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_phone_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_PHONE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_fax_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_FAX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_email_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_EMAIL);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_mntby_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MNT_BY);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_type_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_TYPE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_abuse_mailbox_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ABUSE_MAILBOX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_address_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ADDRESS);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_phone_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_PHONE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_fax_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_FAX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_email_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_EMAIL);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_mntby_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MNT_BY);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_type_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_TYPE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_abuse_mailbox_with_powermntner() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ABUSE_MAILBOX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }
}
