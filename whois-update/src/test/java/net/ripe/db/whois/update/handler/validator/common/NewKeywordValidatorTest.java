package net.ripe.db.whois.update.handler.validator.common;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NewKeywordValidatorTest {
    @Mock UpdateContext updateContext;
    @Mock PreparedUpdate preparedUpdate;
    @InjectMocks NewKeywordValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE));
    }

    @Test
    public void validate_create_with_existing() {
        when(preparedUpdate.getAction()).thenReturn(Action.CREATE);
        when(preparedUpdate.hasOriginalObject()).thenReturn(true);

        subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.newKeywordAndObjectExists());
    }

    @Test
    public void validate_create_without() {
        when(preparedUpdate.getAction()).thenReturn(Action.CREATE);
        when(preparedUpdate.hasOriginalObject()).thenReturn(false);

        subject.validate(preparedUpdate, updateContext);

        verifyZeroInteractions(updateContext);
    }
}
