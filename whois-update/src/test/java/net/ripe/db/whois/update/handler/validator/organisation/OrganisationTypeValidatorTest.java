package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrganisationTypeValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject authenticationSubject;

    @InjectMocks OrganisationTypeValidator subject;

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), hasSize(2));
        assertThat(subject.getActions().contains(Action.MODIFY), is(true));
        assertThat(subject.getActions().contains(Action.CREATE), is(true));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), hasSize(1));
        assertThat(subject.getTypes().get(0), is(ObjectType.ORGANISATION));
    }

    @Test
    public void update_is_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: other"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), eq(UpdateMessages.invalidMaintainerForOrganisationType(new RpslAttribute(AttributeType.ORG,"other"))));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void status_other() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: other"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void orgtype_has_not_changed() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    public void not_auth_by_powermntner() {
        final RpslObject rpslObject = RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR");
        when(update.getUpdatedObject()).thenReturn(rpslObject);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        lenient().when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, rpslObject.findAttribute(AttributeType.ORG_TYPE), UpdateMessages.invalidMaintainerForOrganisationType(rpslObject.findAttribute(AttributeType.ORG_TYPE)));
    }

    @Test
    public void orgtype_has_changed_auth_by_powermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: RIR"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-TST-RIPE\norg-type: LIR"));
        when(update.getAction()).thenReturn(Action.MODIFY);
        lenient().when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(true);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }
}
