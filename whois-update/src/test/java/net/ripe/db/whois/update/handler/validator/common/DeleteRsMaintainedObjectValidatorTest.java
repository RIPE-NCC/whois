package net.ripe.db.whois.update.handler.validator.common;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteRsMaintainedObjectValidatorTest {
    @Mock Subject authSubject;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Maintainers maintainers;
    @InjectMocks DeleteRsMaintainedObjectValidator subject;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(maintainers.isRsMaintainer(ciSet("DEV-MNT"))).thenReturn(false);
        lenient().when(maintainers.isRsMaintainer(ciSet("RS-MNT", "DEV-MNT"))).thenReturn(true);
        lenient().when(updateContext.getSubject(update)).thenReturn(authSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.DELETE));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.values()));
    }

    @Test
    public void validate_no_rs_auth_no_rs_maintainer() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), eq(UpdateMessages.authorisationRequiredForDeleteRsMaintainedObject()));
        verify(maintainers).isRsMaintainer(ciSet("DEV-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT\n" +
                "mnt-by: RS-MNT\n"));

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForDeleteRsMaintainedObject());
        verify(maintainers).isRsMaintainer(ciSet("DEV-MNT", "RS-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_no_rs_auth_rs_maintainer_override() {
        when(authSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT\n" +
                "mnt-by: RS-MNT\n"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(eq(update), eq(new Message(Messages.Type.WARNING, UpdateMessages.authorisationRequiredForDeleteRsMaintainedObject().getText())));
    }

    @Test
    public void validate_rs_auth_rs_maintainer() {
        lenient().when(authSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
        verifyNoMoreInteractions(maintainers);
    }
}
