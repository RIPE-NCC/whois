package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_MULTIPLE_USER_MNTNER;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_SINGLE_USER_MNTNER;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LirMntByAttributeCountValidatorTest {

    @Mock
    PreparedUpdate update;
    @Mock
    UpdateContext updateContext;
    @Mock
    Maintainers maintainers;
    @InjectMocks
    LirMntByAttributeCountValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), hasSize(2));
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), hasSize(1));
        assertThat(subject.getTypes(), contains(ObjectType.ORGANISATION));
    }

    @Test
    public void update_of_not_lir_with_single_mntner() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);

       subject.validate(update, updateContext);

        verify(update).getReferenceObject();
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(maintainers, updateContext);
    }

    @Test
    public void update_of_not_lir_with_multiple_mntner() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);

       subject.validate(update, updateContext);

        verify(update).getReferenceObject();
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(maintainers, updateContext);
    }

    @Test
    public void update_of_lir_with_single_mntner() {
        when(maintainers.isRsMaintainer(ciString("MNT1-LIR"))).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_SINGLE_USER_MNTNER);

       subject.validate(update, updateContext);

        verify(maintainers).isRsMaintainer(ciString("MNT1-LIR"));
        verifyNoMoreInteractions(maintainers);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_lir_with_multiple_mntner() {
        when(maintainers.isRsMaintainer(ciString("MNT1-LIR"))).thenReturn(false);
        when(maintainers.isRsMaintainer(ciString("MNT2-LIR"))).thenReturn(false);
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_MULTIPLE_USER_MNTNER);

       subject.validate(update, updateContext);

        verify(maintainers).isRsMaintainer(ciString("MNT1-LIR"));
        verify(maintainers).isRsMaintainer(ciString("MNT2-LIR"));
        verifyNoMoreInteractions(maintainers);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.multipleUserMntBy(ImmutableList.of(ciString("MNT1-LIR"), ciString("MNT2-LIR"))));
        verifyNoMoreInteractions(update);
        verifyNoMoreInteractions(updateContext);
    }
}
