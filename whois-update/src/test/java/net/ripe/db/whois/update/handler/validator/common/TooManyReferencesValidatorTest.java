package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TooManyReferencesValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject subject;

    TooManyReferencesValidator tooManyReferencesValidator;

    @BeforeEach
    public void setUp() {
        this.tooManyReferencesValidator = new TooManyReferencesValidator(1);
        when(updateContext.getSubject(update)).thenReturn(subject);
    }

    @Test
    public void noReferences() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nsource: TEST"));

       tooManyReferencesValidator.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void oneReference() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nmnt-by: TEST-MNT\nsource: TEST"));

       tooManyReferencesValidator.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void tooManyReferences() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nmnt-by: TEST-MNT\nmnt-by: TEST-MNT\nsource: TEST"));

       tooManyReferencesValidator.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.tooManyReferences());
    }

    @Test
    public void tooManyReferencesWithOverride() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("mntner: TEST-MNT\nmnt-by: TEST-MNT\nmnt-by: TEST-MNT\nsource: TEST"));
        when(subject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        tooManyReferencesValidator.validate(update, updateContext);

        verify(updateContext).addMessage(eq(update), eq(new Message(Messages.Type.WARNING, UpdateMessages.tooManyReferences().getText())));
    }

}
