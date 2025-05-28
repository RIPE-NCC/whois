package net.ripe.db.whois.update.handler.validator.asblock;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsblockByRsMaintainersOnlyValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject subjectObject;

    private AsblockByRsMaintainersOnlyValidator subject;

    @BeforeEach
    public void setup() {
        subject = new AsblockByRsMaintainersOnlyValidator();
    }

    @Test
    public void testGetActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void testGetTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.AS_BLOCK));
    }

    @Test
    public void validate_override_succeeds() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(subjectObject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.asblockIsMaintainedByRipe());
    }

    @Test
    public void validate_authenticatedByDbmMaintainer_succeeds() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
       lenient().when(subjectObject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(true);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.asblockIsMaintainedByRipe());
    }

    @Test
    public void validate_not_authenticatedByDbmMaintainer_or_override_fails() {
        lenient().when(updateContext.getSubject(update)).thenReturn(subjectObject);
        lenient().when(subjectObject.hasPrincipal(Principal.DBM_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.asblockIsMaintainedByRipe());
    }
}
