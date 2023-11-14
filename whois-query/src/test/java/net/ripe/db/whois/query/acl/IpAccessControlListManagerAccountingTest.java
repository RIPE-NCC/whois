package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpAccessControlListManagerAccountingTest {
    private AccessControlListManager subject;

    @Mock DateTimeProvider dateTimeProvider;
    @Mock IpResourceConfiguration ipResourceConfiguration;
    @Mock
    IpAccessControlListDao ipAccessControlListDao;
    @Mock
    SSOAccessControlListDao ssoAccessControlListDao;
    @Mock
    SSOResourceConfiguration ssoResourceConfiguration;

    @Mock IpRanges ipRanges;
    private PersonalObjectAccounting personalObjectAccounting = new TestPersonalObjectAccounting();


    private InetAddress ipv4Address;
    private InetAddress ipv6Address;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new AccessControlListManager(dateTimeProvider, ipResourceConfiguration, ipAccessControlListDao, personalObjectAccounting, ssoAccessControlListDao, ssoResourceConfiguration, ipRanges);
        ipv4Address = Inet4Address.getLocalHost();
        ipv6Address = Inet6Address.getByName("::1");
    }

    @Test
    public void unlimited() throws Exception {
        setPersonalLimit(-1);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(true));

        subject.accountPersonalObjects(ipv4Address,null, 1000);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(true));
    }

    @Test
    public void limited() throws Exception {
        setPersonalLimit(1);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(true));

        subject.accountPersonalObjects(ipv4Address, null, 1);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(true));

        subject.accountPersonalObjects(ipv4Address, null, 1);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(false));
    }

    @Test
    public void limit_zero() throws Exception {
        setPersonalLimit(0);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(true));

        subject.accountPersonalObjects(ipv4Address, null, 1);
        assertThat(subject.canQueryPersonalObjects(ipv4Address, null), is(false));
    }

    private void setPersonalLimit(int count) {
        when(ipResourceConfiguration.getLimit(ipv4Address)).thenReturn(count);
        personalObjectAccounting.resetAccounting();
    }
}
