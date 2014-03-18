package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EndUserMaintainerChecksTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject principalSubject;
    @Mock Maintainers maintainers;

    @InjectMocks EndUserMaintainerChecks subject;

    @Before
    public void setup() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(maintainers.getEnduserMaintainers()).thenReturn(ciSet("END-MNT"));
    }

    @Test
    public void modify_has_no_endusermntner() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-by: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(Principal.ENDUSER_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.adminMaintainerRemoved());
    }

    @Test
    public void modify_has_no_endusermntner_override() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-by: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(any(Principal.class))).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void modify_succeeds() {
        when(principalSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-by: END-MNT"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }
}
