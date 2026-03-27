package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.update.handler.validator.ValidatorTestHelper.validateUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MntLowerAddedValidatorTest {

    @Mock private Subject authenticationSubject;
    private MntLowerAddedValidator subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new MntLowerAddedValidator();
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inetnum() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void status_does_not_require_endMntnerAuthorisation_inet6num() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ALLOCATED-BY-LIR"),
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ALLOCATED-BY-LIR"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLower_added_inetnum() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(),
                hasItems(UpdateMessages.attributeNotAllowedWithStatus(AttributeType.MNT_LOWER, CIString.ciString("ASSIGNED ANYCAST"))));
    }

    @Test
    public void modify_mntLower_added_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\n"),
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), hasItems(
                new Message(
                Messages.Type.WARNING,
                "\"%s:\" attribute not allowed for resources with \"%s:\" status", "mnt-lower", CIString.ciString("ASSIGNED ANYCAST"))));
    }

    @Test
    public void modify_mntLower_added_inet6num() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\n"),
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), hasItems(
                UpdateMessages.attributeNotAllowedWithStatus(AttributeType.MNT_LOWER, CIString.ciString("ASSIGNED PI"))));
    }

    @Test
    public void modify_mntLower_added_inet6num_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI"),
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED PI\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), hasItems(
                new Message(
                Messages.Type.WARNING,
                "\"%s:\" attribute not allowed for resources with \"%s:\" status", "mnt-lower", CIString.ciString("ASSIGNED PI"))));
    }

    @Test
    public void modify_mntLower_removed_inetnum() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLower_removed_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLower_removed_inet6num() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLower_removed_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT\nsource: TEST"),
                                            RpslObject.parse("inet6num: ffff::/48\nstatus: ASSIGNED ANYCAST\nsource: TEST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLowers_same_inetnum() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLowers_same_inetnum_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLowers_same_inet6num() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_mntLowers_same_inet6num_override() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_authorisation_succeeds_inetnum() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_authorisation_succeeds_inet6num() {
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(true);

        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            authenticationSubject,
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"),
                                            RpslObject.parse("inet6num: ffff::/24\nstatus: ASSIGNED ANYCAST"));

        assertThat(messages.getMessages().getAllMessages(), is(empty()));
    }

    @Test
    public void modify_fails_assigned_anycast_inetnum() {
        final ObjectMessages messages = validateUpdate(
                                            subject,
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST"),
                                            RpslObject.parse("inetnum: 192.0/24\nstatus: ASSIGNED ANYCAST\nmnt-lower: TEST-MNT"));

        assertThat(messages.getMessages().getAllMessages(), hasItems(
                UpdateMessages.attributeNotAllowedWithStatus(AttributeType.MNT_LOWER, CIString.ciString("ASSIGNED ANYCAST"))));
    }
}
