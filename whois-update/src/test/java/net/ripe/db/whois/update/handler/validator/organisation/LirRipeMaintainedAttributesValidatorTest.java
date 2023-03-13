package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_MNT_BY;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_ORG_TYPE;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LirRipeMaintainedAttributesValidatorTest {

    @Mock
    PreparedUpdate update;
    @Mock
    UpdateContext updateContext;
    @Mock
    Subject authenticationSubject;

    @InjectMocks
    LirRipeMaintainedAttributesValidator subject;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), hasSize(1));
        assertThat(subject.getActions(), contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), hasSize(1));
        assertThat(subject.getTypes(), contains(ObjectType.ORGANISATION));
    }

    @Test
    public void update_of_non_lir() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_mntby() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MNT_BY);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.MNT_BY));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG);

       subject.validate(update, updateContext);

        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_type() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_TYPE);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_TYPE));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_with_override() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_TYPE);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, new Message(Messages.Type.WARNING, UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_TYPE).getText(), UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_TYPE).getArgs() ));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_abuse_mailbox_with_powermntner() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
        lenient().when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(true);

       subject.validate(update, updateContext);
        verifyNoMoreInteractions(updateContext);
    }
}
