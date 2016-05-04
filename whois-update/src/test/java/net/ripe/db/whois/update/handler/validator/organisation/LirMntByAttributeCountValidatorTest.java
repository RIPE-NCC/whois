package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
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

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_MULTIPLE_USER_MNTNER;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_SINGLE_USER_MNTNER;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG_MULTIPLE_USER_MNTNER;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG_SINGLE_USER_MNTNER;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LirMntByAttributeCountValidatorTest {

    @Mock
    PreparedUpdate update;
    @Mock
    UpdateContext updateContext;
    @Mock
    Subject authenticationSubject;
    @Mock
    Maintainers maintainers;

    @InjectMocks
    LirMntByAttributeCountValidator subject;

    @Before
    public void setup() {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(2));
        assertTrue(subject.getActions().contains(Action.CREATE));
        assertTrue(subject.getActions().contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(1));
        assertTrue(subject.getTypes().contains(ObjectType.ORGANISATION));
    }

    @Test
    public void update_of_not_lir_with_single_mntner() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(NON_LIR_ORG_SINGLE_USER_MNTNER);

        subject.validate(update, updateContext);

        verify(update).getReferenceObject();
        verifyNoMoreInteractions(update);
        verifyZeroInteractions(maintainers, updateContext);
    }

    @Test
    public void update_of_not_lir_with_multiple_mntner() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(NON_LIR_ORG_MULTIPLE_USER_MNTNER);

        subject.validate(update, updateContext);

        verify(update).getReferenceObject();
        verifyNoMoreInteractions(update);
        verifyZeroInteractions(maintainers, updateContext);
    }

    @Test
    public void update_of_lir_with_single_mntner() {
        when(maintainers.isRsMaintainer(ciString("MNT1-LIR"))).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_SINGLE_USER_MNTNER);

        subject.validate(update, updateContext);

        verify(maintainers).isRsMaintainer(ciString("MNT1-LIR"));
        verifyNoMoreInteractions(maintainers);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verifyNoMoreInteractions(update);
        verifyZeroInteractions(updateContext);
    }

    @Test
    public void update_of_lir_with_multiple_mntner() {
        when(maintainers.isRsMaintainer(ciString("MNT1-LIR"))).thenReturn(false);
        when(maintainers.isRsMaintainer(ciString("MNT2-LIR"))).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MULTIPLE_USER_MNTNER);

        subject.validate(update, updateContext);

        verify(maintainers).isRsMaintainer(ciString("MNT1-LIR"));
        verify(maintainers).isRsMaintainer(ciString("MNT2-LIR"));
        verifyNoMoreInteractions(maintainers);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.multipleUserMntBy(new CIString[]{ciString("MNT1-LIR"), ciString("MNT2-LIR")}));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }
}
