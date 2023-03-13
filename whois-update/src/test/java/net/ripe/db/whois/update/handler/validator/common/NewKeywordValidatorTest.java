package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        when(preparedUpdate.hasOriginalObject()).thenReturn(true);

       subject.validate(preparedUpdate, updateContext);

        verify(updateContext).addMessage(preparedUpdate, UpdateMessages.newKeywordAndObjectExists());
    }

    @Test
    public void validate_create_without() {
       subject.validate(preparedUpdate, updateContext);

        verifyNoMoreInteractions(updateContext);
    }
}
