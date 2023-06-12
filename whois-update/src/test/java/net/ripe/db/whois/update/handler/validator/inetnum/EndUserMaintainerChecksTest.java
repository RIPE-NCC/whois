package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EndUserMaintainerChecksTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject principalSubject;
    @Mock Maintainers maintainers;

    @InjectMocks EndUserMaintainerChecks subject;

    @Test
    public void modify_has_no_endusermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-by: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        lenient().when(principalSubject.hasPrincipal(Principal.ENDUSER_MAINTAINER)).thenReturn(true);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.adminMaintainerRemoved());
        verify(maintainers).isEnduserMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void modify_has_no_endusermntner_override() {
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(principalSubject.hasPrincipal(Principal.ENDUSER_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void modify_succeeds() {
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(principalSubject.hasPrincipal(Principal.ENDUSER_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
        verifyNoMoreInteractions(maintainers);
    }
}
