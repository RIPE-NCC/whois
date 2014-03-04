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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    public void update_is_override() {
        when(update.isOverride()).thenReturn(true);

        subject.validate(update, updateContext);

        verify(update).isOverride();
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void status_other() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: other"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void orgtype_has_not_changed() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));

        subject.validate(update, updateContext);
        verify(updateContext, never()).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void not_auth_by_powermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void orgtype_has_changed_auth_by_powermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.invalidMaintainerForOrganisationType());
    }
}
