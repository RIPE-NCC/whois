package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class MntLowerAddedRemovedValidatorTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private Subject authenticationSubject;

    @InjectMocks private MntLowerAddedRemovedValidator subject;

    @BeforeEach
    public void setUp() throws Exception {
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ALLOCATED-BY-LIR"));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_added_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_added_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_added_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_added_inet6num_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_removed_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_removed_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_removed_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_removed_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLowers_same_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLowers_same_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_mntLowers_same_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLowers_same_inet6num_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_authorisation_succeeds_inetnum() {
       lenient().when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
       lenient().when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_authorisation_succeeds_inet6num() {
        lenient().when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);
        lenient().when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_fails_assigned_anycast_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("OTHER-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: OTHER-MNT"));
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

       subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }
}
