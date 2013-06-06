package net.ripe.db.whois.update.authentication;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.Maintainers;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticatorPendingTest {
    @Mock IpRanges ipRanges;
    @Mock UserDao userDao;
    @Mock Origin origin;
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock AuthenticationStrategy authStrategy;
    @Mock AuthenticationStrategy authStrategyPending1;
    @Mock AuthenticationStrategy authStrategyPending2;
    @Mock Maintainers maintainers;
    @Mock LoggerContext loggerContext;
    @Mock Subject authSubject;

    Authenticator subject;

    @Before
    public void setup() {
        when(update.getCredentials()).thenReturn(new Credentials());

        when(authStrategy.getName()).thenReturn("authStrategy");
        when(authStrategyPending1.getName()).thenReturn("authStrategyPending1");
        when(authStrategyPending2.getName()).thenReturn("authStrategyPending2");

        final HashSet<ObjectType> delayedAuthenticationTypes = Sets.newHashSet(ObjectType.ROUTE, ObjectType.ROUTE6);
        when(authStrategy.getTypesWithDeferredAuthenticationSupport()).thenReturn(Collections.<ObjectType>emptySet());
        when(authStrategyPending1.getTypesWithDeferredAuthenticationSupport()).thenReturn(delayedAuthenticationTypes);
        when(authStrategyPending2.getTypesWithDeferredAuthenticationSupport()).thenReturn(delayedAuthenticationTypes);
        when(updateContext.getSubject(update)).thenReturn(authSubject);

        subject = new Authenticator(ipRanges, userDao, maintainers, loggerContext, new AuthenticationStrategy[]{authStrategy, authStrategyPending1, authStrategyPending2});
    }

    @Test
    public void deferred_authentication_allowed() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.ROUTE);
        when(authSubject.getFailedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending1"));
        when(authSubject.getPassedAuthentications()).thenReturn(Sets.newHashSet("authStrategyPending2"));

        final boolean deferredAuthenticationAllowed = subject.isDeferredAuthenticationAllowed(update, updateContext);
        assertThat(deferredAuthenticationAllowed, is(true));
    }
}
