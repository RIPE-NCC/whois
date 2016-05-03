package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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

    private static final RpslObject NON_LIR_ORG = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    private static final RpslObject NON_LIR_ORG_CHANGED = RpslObject.parse("" +
            "organisation: ORG-NCC1-RIPE\n" +
            "org-name:     RIPE Network Coordination Centre\n" +
            "org-type:     RIR\n" +
            "address:      different street and number\n" +
            "address:      different city \n" +
            "address:      different country\n" +
            "phone:        +31 111 1111111\n" +
            "fax-no:       +31 111 1111112\n" +
            "e-mail:       different@test.com\n");

    private static final RpslObject LIR_ORG = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    private static final RpslObject LIR_ORG_ADDRESS = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      different street and number\n" +
            "address:      different city \n" +
            "address:      different country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");


    private static final RpslObject LIR_ORG_PHONE = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 111 1111111\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");


    private static final RpslObject LIR_ORG_FAX = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 111 1111111\n" +
            "e-mail:       org1@test.com\n");


    private static final RpslObject LIR_ORG_EMAIL = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       different@test.com\n");

    private static final RpslObject LIR_ORG_MNT_BY = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "mnt-by:       TEST-MNT\n" +
            "e-mail:       org1@test.com\n");

    private static final RpslObject LIR_ORG_ORG_NAME = RpslObject.parse("" +
            "organisation: LIR-ORG-TST\n" +
            "org-name:     Test Organisation Ltd modified\n" +
            "org-type:     LIR\n" +
            "address:      street and number\n" +
            "address:      city \n" +
            "address:      country\n" +
            "phone:        +31 000 0000000\n" +
            "fax-no:       +31 000 0000001\n" +
            "e-mail:       org1@test.com\n");

    @Before
    public void setup() {
        when(maintainers.getPowerMaintainers()).thenReturn(ciSet("POWER-MNT"));
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(1));
        assertThat(subject.getActions().contains(Action.MODIFY), is(true));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(1));
        assertThat(subject.getTypes().get(0), is(ObjectType.ORGANISATION));
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
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_NAME));
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
    public void update_of_mntby_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MNT_BY);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name_with_override() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

}
