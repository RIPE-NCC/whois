package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class FirstDescriptionChangedTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private Subject principalSubject;

    @InjectMocks private FirstDescriptionChanged subject;

    @Before
    public void setup() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
    }

    @Test
    public void modify_authorised() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: some description"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: changed description\nstatus: ASSIGNED PI"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(any(Principal.class))).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(eq(update), any(Message.class));
    }

    @Test
    public void modify_status_irrelevant() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: some description"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: changed description\nstatus: ALLOCATED PA"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForFirstAttrChange(AttributeType.DESCR));
    }

    @Test
    public void modify_not_authorised() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: some description"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: changed description\nstatus: ASSIGNED ANYCAST"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(any(Principal.class))).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForFirstAttrChange(AttributeType.DESCR));
    }

    @Test
    public void modify_not_authorised_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: some description"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\ndescr: changed description\nstatus: ASSIGNED PI"));
        when(updateContext.getSubject(update)).thenReturn(principalSubject);
        when(principalSubject.hasPrincipal(any(Principal.class))).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForFirstAttrChange(AttributeType.DESCR));
    }
}
