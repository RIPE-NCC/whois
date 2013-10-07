package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.strategy.AuthenticationStrategy;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatorPendingTest {
    @Mock IpRanges ipRanges;
    @Mock UserDao userDao;
    @Mock Origin origin;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock AuthenticationStrategy authStrategyPending1;
    @Mock AuthenticationStrategy authStrategyPending2;
    @Mock Maintainers maintainers;
    @Mock LoggerContext loggerContext;
    @Mock Subject authSubject;

    Authenticator subject;

    @Before
    public void setup() {
        when(update.getCredentials()).thenReturn(new Credentials());

        when(authStrategyPending1.getName()).thenReturn("authStrategyPending1");
        when(authStrategyPending2.getName()).thenReturn("authStrategyPending2");

        final HashSet<ObjectType> delayedAuthenticationTypes = Sets.newHashSet(ObjectType.ROUTE, ObjectType.ROUTE6);
        when(authStrategyPending1.getTypesWithPendingAuthenticationSupport()).thenReturn(delayedAuthenticationTypes);
        when(authStrategyPending2.getTypesWithPendingAuthenticationSupport()).thenReturn(delayedAuthenticationTypes);
        when(updateContext.getSubject(update)).thenReturn(authSubject);

        subject = new Authenticator(ipRanges, userDao, maintainers, loggerContext, new AuthenticationStrategy[]{authStrategyPending1, authStrategyPending2});
    }

    @Test
    public void pending_authentication_allowed() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getPendingAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));

        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(true));
    }

    @Test
    public void pending_authentication_no_create() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getPendingAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));

        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(false));
    }

    @Test
    public void pending_authentication_unsupported_type() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(authSubject.getPendingAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));

        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(false));
    }

    @Test
    public void pending_authentication_failed_other() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getFailedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1, authStrategy"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));

        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(false));
    }

    @Test
    public void pending_authentication_passed_none() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getPendingAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1", "authStrategyPending2"));
        when(authSubject.getPassedAuthentications()).thenReturn(Collections.<String>emptySet());

        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(false));
    }

    @Test
    public void pending_authentication_existing_errors() {
        when(updateContext.hasErrors(update)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getPendingAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));
        final boolean pendingAuthenticationAllowed = subject.isPending(update, updateContext, authSubject);
        assertThat(pendingAuthenticationAllowed, is(false));
    }

    @Test
    public void isCompleteAuthentication_incomplete() {
        assertThat(subject.isAuthenticationForTypeComplete(ObjectType.ROUTE, new PendingUpdate(null, Sets.newHashSet("authStrategyPending1"), null, null)), is(false));
    }

    @Test
    public void isCompleteAuthentication_complete() {
        assertThat(subject.isAuthenticationForTypeComplete(ObjectType.ROUTE, new PendingUpdate(null, Sets.newHashSet("mnt-by", "authStrategyPending1", "authStrategyPending2"), null, null)), is(true));
    }

}
