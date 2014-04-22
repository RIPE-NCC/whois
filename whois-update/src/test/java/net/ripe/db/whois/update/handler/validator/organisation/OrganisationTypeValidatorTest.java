package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
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
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
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
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void status_other() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: other"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgtype_has_not_changed() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void not_auth_by_powermntner() {
        final RpslObject rpslObject = RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR");
        when(update.getUpdatedObject()).thenReturn(rpslObject);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.ORG_TYPE), UpdateMessages.invalidMaintainerForOrganisationType());
    }

    @Test
    public void orgtype_has_changed_auth_by_powermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(authenticationSubject.hasPrincipal(Principal.POWER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }
}
