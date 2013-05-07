package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
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

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationTypeValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;

    @InjectMocks OrganisationTypeValidator subject;

    @Before
    public void setup() {
        when(maintainers.getPowerMaintainers()).thenReturn(ciSet("POWER-MNT"));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(2));
        assertThat(subject.getActions().contains(Action.MODIFY), is(true));
        assertThat(subject.getActions().contains(Action.CREATE), is(true));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(1));
        assertThat(subject.getTypes().get(0), is(ObjectType.ORGANISATION));
    }

    @Test
    public void has_not_only_powermaintainers() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: LIR\nmnt-by:TEST-MNT\nmnt-by: POWER-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void has_not_only_powermaintainers_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: LIR\nmnt-by:TEST-MNT\nmnt-by: POWER-MNT"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void status_other_and_only_powermaintainers_gives_no_error() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: OTHER\nmnt-by: POWER-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void not_status_other_and_only_powermaintainers_but_no_authentication_fails() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: LIR\nmnt-by: POWER-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void success() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: LIR\nmnt-by: POWER-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void success_different_casing() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TEST\norg-type: LIR\nmnt-by: power-mnt"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }
}
