package net.ripe.db.whois.update.handler.validator.inetnum;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MntLowerAddedRemovedTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private Subject authenticationSubject;

    @InjectMocks private MntLowerAddedRemoved subject;

    @Before
    public void setUp() throws Exception {
        when(update.getAction()).thenReturn(Action.MODIFY);
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ALLOCATED-BY-LIR"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ALLOCATED-BY-LIR"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_added_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_added_inetnum_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED PI"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_added_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_added_inet6num_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_removed_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_removed_inetnum_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLower_removed_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST"));
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLower_removed_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLowers_same_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLowers_same_inetnum_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_mntLowers_same_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_mntLowers_same_inet6num_override() {
        when(update.isOverride()).thenReturn(true);
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void modify_authorisation_succeeds_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: OTHER-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(true);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_authorisation_succeeds_inet6num() {
        when(update.getType()).thenReturn(ObjectType.INET6NUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inet6num: ffff/32\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inet6num: ffff::/32\nstatus: ASSIGNED ANYCAST\nmnt-lower: OTHER-MNT"));
        when(authenticationSubject.hasPrincipal(any(Principal.class))).thenReturn(true);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_succeeds_early_registration_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: EARLY-REGISTRATION\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: EARLY-REGISTRATION\nmnt-lower: OTHER-MNT"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }

    @Test
    public void modify_fails_assigned_anycast_inetnum() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getDifferences(AttributeType.MNT_LOWER)).thenReturn(ciSet("OTHER-MNT"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: OTHER-MNT"));
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(updateContext.getSubject(update)).thenReturn(authenticationSubject);

        subject.validate(update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.authorisationRequiredForAttrChange(AttributeType.MNT_LOWER));
    }
}
