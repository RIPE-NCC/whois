package net.ripe.db.whois.query.acl;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static net.ripe.db.whois.query.acl.AccessControlListManager.mask;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlListManagerTest {

    private final RpslObject role = RpslObject.parse("role: Test Role\nnic-hdl: TR1-TEST");
    private final RpslObject roleWithAbuseMailbox = RpslObject.parse("role: Test Role\nnic-hdl: TR1-TEST\nabuse-mailbox: abuse@mailbox.com");
    private final RpslObject person = RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST");
    private final RpslObject domain = RpslObject.parse("domain: 0.0.0.0");
    private final RpslObject autNum = RpslObject.parse("aut-num: AS1234");
    private final RpslObject inetnum = RpslObject.parse("inetnum: 10.0.0.0");

    @Mock DateTimeProvider dateTimeProvider;
    @Mock IpResourceConfiguration ipResourceConfiguration;
    @Mock AccessControlListDao accessControlListDao;
    @Mock PersonalObjectAccounting personalObjectAccounting;
    @Mock IpRanges ipRanges;
    @InjectMocks AccessControlListManager subject;

    private InetAddress ipv4Restricted;
    private InetAddress ipv4Unrestricted;
    private InetAddress ipv4Unknown;

    private InetAddress ipv6Restricted;
    private InetAddress ipv6Unrestricted;
    private InetAddress ipv6Unknown;

    private static final int PERSONAL_DATA_LIMIT = 1000;
    private static final int PERSONAL_DATA_NO_LIMIT = -1;
    private static final int PERSONAL_DATA_LIMIT_UNKNOWN = 0;

    private final LocalDate now = new LocalDate();

    @Before
    public void setup() throws UnknownHostException {
        when(dateTimeProvider.getCurrentDate()).thenReturn(now);

        ipv4Restricted = InetAddress.getByName("127.1.0.0");
        ipv4Unrestricted = InetAddress.getByName("127.0.0.0");
        ipv4Unknown = InetAddress.getByName("1.0.0.0");

        ipv6Restricted = InetAddress.getByName("2001::1");
        ipv6Unrestricted = InetAddress.getByName("2001:1::1");
        ipv6Unknown = InetAddress.getByName("::1");

        mockResourceConfiguration(ipv4Restricted, true, false, PERSONAL_DATA_LIMIT);
        mockResourceConfiguration(ipv4Unrestricted, false, true, PERSONAL_DATA_NO_LIMIT);
        mockResourceConfiguration(ipv6Restricted, true, false, PERSONAL_DATA_LIMIT);
        mockResourceConfiguration(ipv6Unrestricted, false, true, PERSONAL_DATA_NO_LIMIT);
    }

    private void mockResourceConfiguration(InetAddress address, boolean denied, boolean proxy, int limit) throws UnknownHostException {
        when(ipResourceConfiguration.isDenied(address)).thenReturn(denied);
        when(ipResourceConfiguration.isProxy(address)).thenReturn(proxy);
        when(ipResourceConfiguration.getLimit(address)).thenReturn(limit);
    }

    @Test
    public void check_denied_restricted() throws Exception {
        assertTrue(subject.isDenied(ipv4Restricted));
        assertTrue(subject.isDenied(ipv6Restricted));
    }

    @Test
    public void check_denied_unrestricted() throws Exception {
        assertFalse(subject.isDenied(ipv4Unrestricted));
        assertFalse(subject.isDenied(ipv6Unrestricted));
    }

    @Test
    public void check_denied_unknown() throws Exception {
        assertFalse(subject.isDenied(ipv4Unknown));
        assertFalse(subject.isDenied(ipv6Unknown));
    }

    @Test
    public void check_proxy_restricted() throws Exception {
        assertFalse(subject.isAllowedToProxy(ipv4Restricted));
        assertFalse(subject.isAllowedToProxy(ipv6Restricted));
    }

    @Test
    public void check_proxy_unrestricted() throws Exception {
        assertTrue(subject.isAllowedToProxy(ipv4Unrestricted));
        assertTrue(subject.isAllowedToProxy(ipv6Unrestricted));
    }

    @Test
    public void check_proxy_unknown() throws Exception {
        assertFalse(subject.isAllowedToProxy(ipv4Unknown));
        assertFalse(subject.isAllowedToProxy(ipv6Unknown));
    }

    @Test
    public void check_getLimit_restricted() throws Exception {
        assertThat(subject.getPersonalDataLimit(ipv4Restricted), is(PERSONAL_DATA_LIMIT));
        assertThat(subject.getPersonalDataLimit(ipv6Restricted), is(PERSONAL_DATA_LIMIT));
    }

    @Test
    public void check_getLimit_unrestricted() throws Exception {
        assertThat(subject.getPersonalDataLimit(ipv4Unrestricted), is(PERSONAL_DATA_NO_LIMIT));
        assertThat(subject.getPersonalDataLimit(ipv6Unrestricted), is(PERSONAL_DATA_NO_LIMIT));
    }

    @Test
    public void check_getLimit_unknown() throws Exception {
        assertThat(subject.getPersonalDataLimit(ipv4Unknown), is(PERSONAL_DATA_LIMIT_UNKNOWN));
        assertThat(subject.getPersonalDataLimit(ipv6Unknown), is(PERSONAL_DATA_LIMIT_UNKNOWN));
    }

    @Captor
    ArgumentCaptor<Ipv6Resource> ipv6ResourceCaptor;

    @Test
    public void test_if_block_temporary_is_logged() {
        subject.blockTemporary(ipv6Restricted, PERSONAL_DATA_LIMIT);
        verify(accessControlListDao).saveAclEvent(ipv6ResourceCaptor.capture(), eq(now), eq(PERSONAL_DATA_LIMIT), eq(BlockEvent.Type.BLOCK_TEMPORARY));

        Ipv6Resource ipv6Resource = ipv6ResourceCaptor.getValue();
        assertThat(ipv6Resource.toString(), is("2001::/64"));
    }

    @Test
    public void requiresAcl_withRipeSource() {
        assertTrue(subject.requiresAcl(person, Source.slave("RIPE")));

        assertTrue(subject.requiresAcl(role, Source.slave("RIPE")));
        assertFalse(subject.requiresAcl(roleWithAbuseMailbox, Source.slave("RIPE")));
        assertFalse(subject.requiresAcl(autNum, Source.slave("RIPE")));
        assertFalse(subject.requiresAcl(inetnum, Source.slave("RIPE")));
        assertFalse(subject.requiresAcl(domain, Source.slave("RIPE")));
    }

    @Test
    public void requiresAcl_withNonRipeSource() {
        assertFalse(subject.requiresAcl(autNum, Source.slave("APNIC-GRS")));
        assertFalse(subject.requiresAcl(person, Source.slave("APNIC-GRS")));
        assertFalse(subject.requiresAcl(role, Source.slave("AFRINIC-GRS")));
        assertFalse(subject.requiresAcl(person, Source.slave("AFRINIC-GRS")));
        assertFalse(subject.requiresAcl(role, Source.slave("JPIRR-GRS")));
        assertFalse(subject.requiresAcl(inetnum, Source.slave("JPIRR-GRS")));
        assertFalse(subject.requiresAcl(autNum, Source.slave("RADB-GRS")));
    }

    @Test
    public void requiresAcl_withTest() {
        assertTrue(subject.requiresAcl(person, Source.slave("TEST")));

        assertTrue(subject.requiresAcl(role, Source.slave("TEST")));
        assertFalse(subject.requiresAcl(roleWithAbuseMailbox, Source.slave("TEST")));
        assertFalse(subject.requiresAcl(autNum, Source.slave("TEST")));
        assertFalse(subject.requiresAcl(inetnum, Source.slave("TEST")));
        assertFalse(subject.requiresAcl(domain, Source.slave("TEST")));
    }

    @Test
    public void testMask() throws UnknownHostException {
        InetAddress subject = Inet6Address.getByName("3ffe:6a88:85a3:98d3:1319:8a2e:9370:7344");

        assertThat(mask(subject, 125).getHostAddress(), is("3ffe:6a88:85a3:98d3:1319:8a2e:9370:7340"));
        assertThat(mask(subject, 112).getHostAddress(), is("3ffe:6a88:85a3:98d3:1319:8a2e:9370:0"));
        assertThat(mask(subject, 96).getHostAddress(), is("3ffe:6a88:85a3:98d3:1319:8a2e:0:0"));
        assertThat(mask(subject, 80).getHostAddress(), is("3ffe:6a88:85a3:98d3:1319:0:0:0"));
        assertThat(mask(subject, 64).getHostAddress(), is("3ffe:6a88:85a3:98d3:0:0:0:0"));
        assertThat(mask(subject, 48).getHostAddress(), is("3ffe:6a88:85a3:0:0:0:0:0"));
        assertThat(mask(subject, 32).getHostAddress(), is("3ffe:6a88:0:0:0:0:0:0"));
        assertThat(mask(subject, 16).getHostAddress(), is("3ffe:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 8).getHostAddress(), is("3f00:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 7).getHostAddress(), is("3e00:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 6).getHostAddress(), is("3c00:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 5).getHostAddress(), is("3800:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 4).getHostAddress(), is("3000:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 3).getHostAddress(), is("2000:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 2).getHostAddress(), is("0:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 1).getHostAddress(), is("0:0:0:0:0:0:0:0"));
        assertThat(mask(subject, 0).getHostAddress(), is("0:0:0:0:0:0:0:0"));

        assertThat(mask(Inet6Address.getByName("::1"), AccessControlListManager.IPV6_NETMASK), is(Inet6Address.getByName("0:0:0:0:0:0:0:0")));
    }

    @Test
    public void override_from_trusted_range() {
        when(ipRanges.isTrusted(any(IpInterval.class))).thenReturn(true);
        assertThat(subject.isTrusted(InetAddresses.forString("10.0.0.1")), is(true));
    }

    @Test
    public void override_from_untrusted_range() {
        when(ipRanges.isTrusted(any(IpInterval.class))).thenReturn(false);
        assertThat(subject.isTrusted(InetAddresses.forString("10.0.0.1")), is(false));
    }
}
