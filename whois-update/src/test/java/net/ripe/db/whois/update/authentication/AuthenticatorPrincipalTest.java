package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationFailedException;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.common.Credentials.OverrideCredential;
import net.ripe.db.whois.common.Credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;
import java.util.HashSet;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setup() {
        when(authenticationStrategy2.compareTo(authenticationStrategy1)).thenReturn(1);

        when(maintainers.getEnduserMaintainers()).thenReturn(ciSet("RIPE-NCC-END-MNT"));
        when(maintainers.getAllocMaintainers()).thenReturn(ciSet("RIPE-NCC-HM-MNT", "AARDVARK-MNT"));
        when(maintainers.getLegacyMaintainers()).thenReturn(ciSet("RIPE-NCC-LEGACY-MNT"));
        lenient().when(update.getCredentials()).thenReturn(new Credentials());

        subjectCapture = ArgumentCaptor.forClass(Subject.class);
        subject = new Authenticator(ipRanges, userDao, maintainers, loggerContext, new AuthenticationStrategy[]{authenticationStrategy1, authenticationStrategy2});
    }

    @Test
    public void authenticate_basic() {
        authenticate_maintainer(RpslObject.parse("mntner: TEST-MNT"));
    }

    @Test
    public void authenticate_powerMaintainer() {
        authenticate_maintainer(RpslObject.parse("mntner: RIPE-NCC-HM-MNT"), Principal.ALLOC_MAINTAINER, Principal.ALLOC_MAINTAINER);
    }

    @Test
    public void authenticate_powerMaintainer_case_mismatch() {
        authenticate_maintainer(RpslObject.parse("mntner: riPe-nCC-hm-Mnt"), Principal.ALLOC_MAINTAINER, Principal.ALLOC_MAINTAINER);
    }

    @Test
    public void authenticate_legacyMaintainer() {
        authenticate_maintainer(RpslObject.parse("mntner: RIPE-NCC-LEGACY-MNT"), Principal.LEGACY_MAINTAINER);
    }

    private void authenticate_maintainer(final RpslObject mntner, final Principal... excpectedPrincipals) {
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(mntner));

        subject.authenticate(origin, update, updateContext);
        verifySubject(updateContext, new Subject(
                Sets.newHashSet(excpectedPrincipals),
                Collections.singleton(authenticationStrategy1.getName()),
                Collections.<String>emptySet()
        ));
    }

    @Test
    public void authenticate_by_powerMaintainer_inside_ripe() {
        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(origin.allowAdminOperations()).thenReturn(true);
        when(ipRanges.isTrusted(any(Interval.class))).thenReturn(true);
        when(authenticationStrategy1.supports(update)).thenReturn(true);
        when(authenticationStrategy1.authenticate(update, updateContext)).thenReturn(Lists.newArrayList(RpslObject.parse("mntner: RIPE-NCC-HM-MNT")));

        subject.authenticate(origin, update, updateContext);
        verify(updateContext, times(0)).addMessage(update, UpdateMessages.ripeMntnerUpdatesOnlyAllowedFromWithinNetwork());
    }

    @Test
    public void authentication_fails() {
        when(authenticationStrategy2.getName()).thenReturn("authenticationStrategy2");
        when(authenticationStrategy1.supports(update)).thenReturn(false);
        when(authenticationStrategy2.supports(update)).thenReturn(true);
        when(authenticationStrategy2.authenticate(update, updateContext)).thenThrow(new AuthenticationFailedException(UpdateMessages.unexpectedError(), Collections.<RpslObject>emptyList()));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(eq(update), any(Message.class));
        verifySubject(updateContext, new Subject(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.singleton(authenticationStrategy2.getName())
        ));
    }

    @Test
    public void authenticate_too_many_passwords() {
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

        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.multipleOverridePasswords());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_no_users() {
        when(origin.allowAdminOperations()).thenReturn(true);

        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,password"));

        when(origin.getFrom()).thenReturn("193.0.0.10");
        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));
        when(ipRanges.isTrusted(IpInterval.parse("193.0.0.10"))).thenReturn(true);

        when(userDao.getOverrideUser("user")).thenThrow(EmptyResultDataAccessException.class);

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
        when(ipRanges.isTrusted(IpInterval.parse("193.0.0.10"))).thenReturn(true);

        when(userDao.getOverrideUser("user")).thenThrow(EmptyResultDataAccessException.class);

        subject.authenticate(origin, update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.overrideAuthenticationFailed());
        verifySubject(updateContext, new Subject());
    }

    @Test
    public void authenticate_override_valid_password_no_objecttypes() {
        final HashSet<Credential> credentialSet = Sets.newHashSet();
        credentialSet.add(OverrideCredential.parse("user,password"));

        when(update.isOverride()).thenReturn(true);
        when(update.getCredentials()).thenReturn(new Credentials(credentialSet));

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
        verify(update).getUpdate();
        verifyNoMoreInteractions(userDao, update, updateContext);
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
        when(ipRanges.isTrusted(IpInterval.parse("193.0.0.10"))).thenReturn(true);

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
            verify(updateContext, atLeastOnce()).status(eq(update), eq(UpdateStatus.FAILED_AUTHENTICATION));
        }
    }
}


