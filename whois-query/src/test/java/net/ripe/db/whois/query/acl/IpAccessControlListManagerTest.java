package net.ripe.db.whois.query.acl;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;

import static net.ripe.db.whois.query.acl.AccessControlListManager.mask;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpAccessControlListManagerTest {

    @Mock DateTimeProvider dateTimeProvider;
    @Mock IpResourceConfiguration ipResourceConfiguration;
    @Mock SSOResourceConfiguration ssoResourceConfiguration;
    @Mock
    IpAccessControlListDao ipAccessControlListDao;
    @Mock PersonalObjectAccounting personalObjectAccounting;
    @Mock SSOAccessControlListDao ssoAccessControlListDao;
    @Mock SsoTokenTranslator ssoTokenTranslator;
    @Mock IpRanges ipRanges;
    AccessControlListManager subject;

    private InetAddress ipv4Restricted;
    private InetAddress ipv4Unrestricted;
    private InetAddress ipv4Unknown;

    private InetAddress ipv6Restricted;
    private InetAddress ipv6Unrestricted;
    private InetAddress ipv6Unknown;

    private static final int PERSONAL_DATA_LIMIT = 1000;
    private static final int PERSONAL_DATA_NO_LIMIT = -1;
    private static final int PERSONAL_DATA_LIMIT_UNKNOWN = 0;

    private final LocalDate now = LocalDate.now();
    private AccountingIdentifier accountingIdentifierIpv6;
    private AccountingIdentifier accountingIdentifierIpv4;


    @BeforeEach
    public void setup() throws UnknownHostException {
       lenient().when(dateTimeProvider.getCurrentDate()).thenReturn(now);

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

        subject = new AccessControlListManager(dateTimeProvider, ipResourceConfiguration, ipAccessControlListDao, personalObjectAccounting, ssoAccessControlListDao, ssoResourceConfiguration, false, ipRanges);

    }

    private void mockResourceConfiguration(InetAddress address, boolean denied, boolean proxy, int limit) throws UnknownHostException {
        lenient().when(ipResourceConfiguration.isDenied(address)).thenReturn(denied);
        lenient().when(ipResourceConfiguration.isProxy(address)).thenReturn(proxy);
        lenient().when(ipResourceConfiguration.getLimit(address)).thenReturn(limit);
    }


    @Test
    public void check_proxy_restricted() throws Exception {
        assertThat(subject.isAllowedToProxy(ipv4Restricted), is(false));
        assertThat(subject.isAllowedToProxy(ipv6Restricted), is(false));
    }

    @Test
    public void check_proxy_unrestricted() throws Exception {
        assertThat(subject.isAllowedToProxy(ipv4Unrestricted), is(true));
        assertThat(subject.isAllowedToProxy(ipv6Unrestricted), is(true));
    }

    @Test
    public void check_proxy_unknown() {
        assertThat(subject.isAllowedToProxy(ipv4Unknown), is(false));
        assertThat(subject.isAllowedToProxy(ipv6Unknown), is(false));
    }

    @Test
    public void check_getLimit_restricted() {
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv4Restricted, null)), is(PERSONAL_DATA_LIMIT));
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv6Restricted, null)), is(PERSONAL_DATA_LIMIT));
    }

    @Test
    public void check_getLimit_unrestricted() {
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv4Unrestricted, null)), is(Integer.MAX_VALUE));
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv6Unrestricted, null)), is(Integer.MAX_VALUE));
    }

    @Test
    public void check_getLimit_unknown() {
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv4Unknown, null)), is(PERSONAL_DATA_LIMIT_UNKNOWN));
        assertThat(subject.getPersonalObjects(new AccountingIdentifier(ipv6Unknown, null)), is(PERSONAL_DATA_LIMIT_UNKNOWN));
    }

    @Test
    public void testMask() throws UnknownHostException {
        final InetAddress subject = Inet6Address.getByName("3ffe:6a88:85a3:98d3:1319:8a2e:9370:7344");

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
