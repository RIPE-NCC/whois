package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.etree.Interval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationFailedException;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.HashSet;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AuthenticatorPrincipalTest {
    @Mock IpRanges ipRanges;
    @Mock UserDao userDao;
    @Mock Origin origin;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock AuthenticationStrategy authenticationStrategy1;
    @Mock AuthenticationStrategy authenticationStrategy2;
    @Mock Maintainers maintainers;
    @Mock LoggerContext loggerContext;

    Authenticator subject;
    ArgumentCaptor<Subject> subjectCapture;

    @Before
    public void setup() {
        when(authenticationStrategy1.getName()).thenReturn("authenticationStrategy1");
        when(authenticationStrategy2.getName()).thenReturn("authenticationStrategy2");

        when(maintainers.getPowerMaintainers()).thenReturn(ciSet("RIPE-NCC-HM-MNT"));
        when(maintainers.getEnduserMaintainers()).thenReturn(ciSet("RIPE-NCC-END-MNT"));
        when(maintainers.getAllocMaintainers()).thenReturn(ciSet("RIPE-NCC-HM-MNT", "AARDVARK-MNT"));
        when(update.getCredentials()).thenReturn(new Credentials());

        subjectCapture = ArgumentCaptor.forClass(Subject.class);
        subject = new Authenticator(ipRanges, userDao, maintainers, loggerContext, new AuthenticationStrategy[]{authenticationStrategy1, authenticationStrategy2});
    }

    @Test
    public void authenticate_basic() {
        authenticate_maintainer(RpslObject.parse("mntner: TEST-MNT"));
    }

    @Test
    public void authenticate_powerMaintainer() {
        when(origin.getFrom()).thenReturn("127.0.0.1");
        when(ipRanges.isInRipeRange(any(Interval.class))).thenReturn(true);
        authenticate_maintainer(RpslObject.parse("mntner: RIPE-NCC-HM-MNT"), Principal.POWER_MAINTAINER, Principal.ALLOC_MAINTAINER);
    }

    @Test
    public void authenticate_powerMaintainer_case_mismatch() {
        when(origin.getFrom()).thenReturn("127.0.0.1");
        when(ipRanges.isInRipeRange(any(Interval.class))).thenReturn(true);
        authenticate_maintainer(RpslObject.parse("mntner: riPe-nCC-hm-Mnt"), Principal.POWER_MAINTAINER, Principal.ALLOC_MAINTAINER);
    }

    private void authenticate_maintainer(final RpslObject mntner, final Principal... excpectedPrincipals) {
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(mntner));

        subject.authenticate(origin, update, updateContext);
        verifySubject(updateContext, new Subject(
                Sets.newHashSet(excpectedPrincipals),
                Collections.singleton(authenticationStrategy1.getName()),
                Collections.<String>emptySet()));
    }

    @Test
    @Ignore // [AK] For now we allow updating by power maintainers outside the RIPE range, so this test fails
    public void authenticate_by_powerMaintainer_outside_ripe() {
        when(origin.getFrom()).thenReturn("212.0.0.0");
        when(ipRanges.isInRipeRange(any(Interval.class))).thenReturn(false);
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

    @Test
    @Ignore
    public void authenticate_by_powerMaintainer_by_email() {
        when(origin.allowAdminOperations()).thenReturn(false);
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext, times(1)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

    @Test
    public void authenticate_by_powerMaintainer_inside_ripe() {
        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(origin.allowAdminOperations()).thenReturn(true);
        when(ipRanges.isInRipeRange(any(Interval.class))).thenReturn(true);
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);
        verify(updateContext, times(0)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

    @Test
    public void authentication_fails() {
        when(origin.allowAdminOperations()).thenReturn(true);
        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(authenticationStrategy1.supports(update)).thenReturn(false);
        when(authenticationStrategy2.supports(update)).thenReturn(true);
        when(authenticationStrategy2.authenticate(update, updateContext)).thenThrow(new AuthenticationFailedException(UpdateMessages.unexpectedError()));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(eq(update), any(Message.class));
        verifySubject(updateContext, new Subject(
                Collections.<Principal>emptySet(),
                Collections.<String>emptySet(),
                Collections.singleton(authenticationStrategy2.getName())));
    }

    @Test
    public void authenticate_too_many_passwords() {
        when(origin.allowAdminOperations()).thenReturn(true);
        when(origin.getFrom()).thenReturn("193.0.0.10");

        final HashSet<Credential> credentialSet = Sets.newHashSet();
        for (int i = 0; i < 30; i++) {
            credentialSet.add(new PasswordCredential("password" + i));
        }

        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));
        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.tooManyPasswordsSpecified());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_no_syncupdate() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,pwd"));

        when(origin.getFrom()).thenReturn("someone@mail.com");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideNotAllowedForOrigin(origin));
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_outside_RIPE_range() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,pwd"));

        when(origin.allowAdminOperations()).thenReturn(true);
        when(origin.getFrom()).thenReturn("10.0.0.0");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideOnlyAllowedByDbAdmins());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_multiple_overrides() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,pwd1"));
        credentialSet.add(OverrideCredential.parse("user,pwd2"));

        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.multipleOverridePasswords());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_no_users() {
        when(origin.getName()).thenReturn("sync update");
        when(origin.allowAdminOperations()).thenReturn(true);

        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,password"));

        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));
        when(ipRanges.isInRipeRange(IpInterval.parse("193.0.0.10"))).thenReturn(true);

        when(userDao.getOverrideUser("user")).thenThrow(EmptyResultDataAccessException.class);
        when(userDao.getOverrideUser("dbase1")).thenThrow(EmptyResultDataAccessException.class);
        when(userDao.getOverrideUser("dbase2")).thenThrow(EmptyResultDataAccessException.class);

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideAuthenticationFailed());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_invalid_password() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,invalid"));

        when(origin.allowAdminOperations()).thenReturn(true);
        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));
        when(ipRanges.isInRipeRange(IpInterval.parse("193.0.0.10"))).thenReturn(true);

        when(userDao.getOverrideUser("user")).thenThrow(EmptyResultDataAccessException.class);
        when(userDao.getOverrideUser("dbase1")).thenReturn(User.createWithPlainTextPassword("dbase", "password"));
        when(userDao.getOverrideUser("dbase2")).thenReturn(User.createWithPlainTextPassword("dbase", "password"));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideAuthenticationFailed());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_valid_password_no_objecttypes() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,password"));

        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

        when(userDao.getOverrideUser("user")).thenReturn(User.createWithPlainTextPassword("user", "password"));
        when(userDao.getOverrideUser("dbase1")).thenReturn(User.createWithPlainTextPassword("dbase", "password"));
        when(userDao.getOverrideUser("dbase2")).thenReturn(User.createWithPlainTextPassword("dbase", "password"));

        subject.authenticate(origin, update, updateContext);

        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_maintenance_job() {
        Origin origin = new Origin() {
            @Override
            public boolean isDefaultOverride() {
                return true;
            }

            @Override
            public boolean allowAdminOperations() {
                return true;
            }

            @Override
            public String getId() {
                return "";
            }

            @Override
            public String getFrom() {
                return "";
            }

            @Override
            public String getResponseHeader() {
                return "";
            }

            @Override
            public String getNotificationHeader() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }
        };

        subject.authenticate(origin, update, updateContext);
        verifySubject(updateContext, new Subject(Principal.OVERRIDE_MAINTAINER));
        verify(authenticationStrategy1).getTypesWithDeferredAuthenticationSupport();
        verify(authenticationStrategy2).getTypesWithDeferredAuthenticationSupport();
        verifyNoMoreInteractions(authenticationStrategy1, authenticationStrategy2, userDao, update, updateContext);
    }

    @Test
    public void authenticate_override_valid_password_and_objecttypes() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,password"));

        when(origin.allowAdminOperations()).thenReturn(true);
        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));
        when(ipRanges.isInRipeRange(IpInterval.parse("193.0.0.10"))).thenReturn(true);

        when(userDao.getOverrideUser("user")).thenReturn(User.createWithPlainTextPassword("user", "password", ObjectType.INETNUM));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideAuthenticationUsed());
        verifySubject(updateContext, new Subject(Principal.OVERRIDE_MAINTAINER));
    }

    private void verifySubject(final UpdateContext updateContext, final Subject expectedSubject) {
        verify(updateContext).subject(any(UpdateContainer.class), subjectCapture.capture());

        final Subject capturedSubject = subjectCapture.getValue();
        assertThat(capturedSubject.getPrincipals(), containsInAnyOrder(expectedSubject.getPrincipals().toArray()));
        assertThat(capturedSubject.getPassedAuthentications(), containsInAnyOrder(expectedSubject.getPassedAuthentications().toArray()));
        assertThat(capturedSubject.getFailedAuthentications(), containsInAnyOrder(expectedSubject.getFailedAuthentications().toArray()));

        if (!capturedSubject.getFailedAuthentications().isEmpty()) {
            verify(updateContext, atLeastOnce()).status(any(Update.class), eq(UpdateStatus.FAILED_AUTHENTICATION));
        }
    }
}


