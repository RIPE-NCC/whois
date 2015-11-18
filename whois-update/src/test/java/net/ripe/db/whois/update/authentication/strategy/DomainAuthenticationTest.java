package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DomainAuthenticationTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Ipv4Tree ipv4Tree;
    @Mock Ipv6Tree ipv6Tree;
    @Mock RpslObjectDao objectDao;
    @Mock AuthenticationModule authenticationModule;
    @InjectMocks DomainAuthentication subject;

    RpslObject mntner;
    ArrayList<RpslObject> candidates;

    @Before
    public void setUp() throws Exception {
        mntner = RpslObject.parse("" +
                "mntner:  DEV-MNT\n");

        candidates = Lists.newArrayList(mntner);

        when(objectDao.getByKeys(ObjectType.MNTNER, ciSet("DEV-MNT"))).thenReturn(candidates);
    }

    @Test
    public void supports_create_domain() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void supports_modify_domain() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.DOMAIN);

        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void supports_create_inetnum() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getType()).thenReturn(ObjectType.INETNUM);

        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void authenticate_enum_domain() {
        final RpslObject rpslObject = RpslObject.parse("domain: 2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated, hasSize(0));

        verifyZeroInteractions(ipv4Tree, ipv6Tree, objectDao);
    }

    @Test
    public void authenticate_ipv4_domain_no_parent() {
        final RpslObject rpslObject = RpslObject.parse("domain: 255.255.193.in-addr.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);
        when(ipv4Tree.findExactOrFirstLessSpecific(Ipv4Resource.parse("193.255.255.0/24"))).thenReturn(Collections.<Ipv4Entry>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException ignored) {
            assertThat(ignored.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(rpslObject, AttributeType.DOMAIN, Collections.<RpslObject>emptySet())));
        }

        verify(ipv4Tree).findExactOrFirstLessSpecific(any(Ipv4Resource.class));
        verifyZeroInteractions(ipv6Tree, objectDao);
    }

    @Test
    public void authenticate_ipv6_domain_no_parent() {
        final RpslObject rpslObject = RpslObject.parse("domain: 0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);
        when(ipv6Tree.findExactOrFirstLessSpecific(Ipv6Resource.parse("2001:7f8::/48"))).thenReturn(Collections.<Ipv6Entry>emptyList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException ignored) {
            assertThat(ignored.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(rpslObject, AttributeType.DOMAIN, Collections.<RpslObject>emptySet())));
        }

        verify(ipv6Tree).findExactOrFirstLessSpecific(any(Ipv6Resource.class));
        verifyZeroInteractions(ipv4Tree, objectDao);
    }

    @Test
    public void authenticate_ipv6_domain_multiple_parents() {
        final RpslObject rpslObject = RpslObject.parse("domain: 0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        when(ipv6Tree.findExactOrFirstLessSpecific(Ipv6Resource.parse("2001:7f8::/48"))).thenReturn(Lists.newArrayList(
                new Ipv6Entry(Ipv6Resource.parse("2001:7f8::/32"), 1),
                new Ipv6Entry(Ipv6Resource.parse("2001:7f8::/32"), 2)));

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException ignored) {
            assertThat(ignored.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(rpslObject, AttributeType.DOMAIN, Collections.<RpslObject>emptySet())));
        }

        verify(ipv6Tree).findExactOrFirstLessSpecific(any(Ipv6Resource.class));
        verifyZeroInteractions(ipv4Tree, objectDao);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_by_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/32\n" +
                "mnt-by:    DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_by_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/48\n" +
                "mnt-by:    DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_lower_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/32\n" +
                "mnt-by:    DEV-MNT\n" +
                "mnt-lower: DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_lower_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/48\n" +
                "mnt-by:    DEV-MNT\n" +
                "mnt-lower: DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_domains_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:    2001:7f8::/32\n" +
                "mnt-by:      DEV-MNT\n" +
                "mnt-domains: DEV-MNT\n" +
                "mnt-lower:   DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_domains_success() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:    2001:7f8::/48\n" +
                "mnt-by:      DEV-MNT\n" +
                "mnt-domains: DEV-MNT\n" +
                "mnt-lower:   DEV-MNT\n");

        authenticateSuccess(ipObject);
    }

    private void authenticateSuccess(final RpslObject ipObject) {
        final RpslObject rpslObject = RpslObject.parse("domain: 0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        when(ipv6Tree.findExactOrFirstLessSpecific(Ipv6Resource.parse("2001:7f8::/48"))).thenReturn(
                Lists.newArrayList(new Ipv6Entry(Ipv6Resource.parse(ipObject.getKey()), 1)));

        when(objectDao.getById(1)).thenReturn(ipObject);
        when(authenticationModule.authenticate(update, updateContext, candidates)).thenReturn(candidates);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated.containsAll(candidates), is(true));

        verifyZeroInteractions(ipv4Tree);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_by_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/32\n" +
                "mnt-by:    DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_BY);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_by_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/48\n" +
                "mnt-by:    DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_BY);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_lower_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/32\n" +
                "mnt-by:    DEV-MNT\n" +
                "mnt-lower: DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_LOWER);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_lower_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:  2001:7f8::/48\n" +
                "mnt-by:    DEV-MNT\n" +
                "mnt-lower: DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_BY);
    }

    @Test
    public void authenticate_ipv6_domain_parent_has_mnt_domains_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:    2001:7f8::/32\n" +
                "mnt-by:      DEV-MNT\n" +
                "mnt-lower:   DEV-MNT\n" +
                "mnt-domains: DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_DOMAINS);
    }

    @Test
    public void authenticate_ipv6_domain_equal_has_mnt_domains_not_authenticated() {
        final RpslObject ipObject = RpslObject.parse("" +
                "inet6num:    2001:7f8::/48\n" +
                "mnt-by:      DEV-MNT\n" +
                "mnt-lower:   DEV-MNT\n" +
                "mnt-domains: DEV-MNT\n");

        authenticate_ipv6_domain_parent_failed(ipObject, AttributeType.MNT_DOMAINS);
    }

    void authenticate_ipv6_domain_parent_failed(final RpslObject ipObject, final AttributeType attributeType) {
        final RpslObject rpslObject = RpslObject.parse("domain: 0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa");
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        when(ipv6Tree.findExactOrFirstLessSpecific(Ipv6Resource.parse("2001:7f8::/48"))).thenReturn(
                Lists.newArrayList(new Ipv6Entry(Ipv6Resource.parse(ipObject.getKey().toString()), 1)));

        when(objectDao.getById(1)).thenReturn(ipObject);

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException ignored) {
            assertThat(ignored.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(ipObject, attributeType, candidates)));
        }

        verify(authenticationModule).authenticate(update, updateContext, candidates);
        verifyZeroInteractions(ipv4Tree);
    }
}
