package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrgAttributeForLirOrganisationValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject authenticationSubject;
    @Mock Maintainers maintainers;

    @InjectMocks OrgAttributeForLirOrganisationValidator subject;

    @Before
    public void setup() {
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
    public void no_org_for_lir_organisation() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void org_unchanged_for_lir_organisation() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR\norg: ORG-TST"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR\norg: ORG-TST"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void org_changed_for_other_organisation() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: OTHER\norg: ORG-TST\norg:ORG-TST2"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: OTHER\norg: ORG-TST3"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void org_changed_for_lir_organisation() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR\norg: ORG-TST"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update,  UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG));
    }

    @Test
    public void org_changed_for_lir_organisation_with_override() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST\norg-type: LIR\norg: ORG-TST"));
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
    }
}