package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlListManagerAccountingTest {
    private AccessControlListManager subject;

    @Mock DateTimeProvider dateTimeProvider;
    @Mock IpResourceConfiguration ipResourceConfiguration;
    @Mock AccessControlListDao accessControlListDao;
    private PersonalObjectAccounting personalObjectAccounting = new TestPersonalObjectAccounting();


    private InetAddress ipv4Address;
    private InetAddress ipv6Address;

    @Before
    public void setUp() throws Exception {
        subject = new AccessControlListManager(dateTimeProvider, ipResourceConfiguration, accessControlListDao, personalObjectAccounting);
        ipv4Address = Inet4Address.getLocalHost();
        ipv6Address = Inet6Address.getByName("::1");
    }

    @Test
    public void unlimited() throws Exception {
        setPersonalLimit(-1);
        assertTrue(subject.canQueryPersonalObjects(ipv4Address));

        subject.accountPersonalObjects(ipv4Address, 1000);
        assertTrue(subject.canQueryPersonalObjects(ipv4Address));
    }

    @Test
    public void limited() throws Exception {
        setPersonalLimit(1);
        assertTrue(subject.canQueryPersonalObjects(ipv4Address));

        subject.accountPersonalObjects(ipv4Address, 1);
        assertTrue(subject.canQueryPersonalObjects(ipv4Address));

        subject.accountPersonalObjects(ipv4Address, 1);
        assertFalse(subject.canQueryPersonalObjects(ipv4Address));
    }

    @Test
    public void limit_zero() throws Exception {
        setPersonalLimit(0);
        assertTrue(subject.canQueryPersonalObjects(ipv4Address));

        subject.accountPersonalObjects(ipv4Address, 1);
        assertFalse(subject.canQueryPersonalObjects(ipv4Address));
    }

    private void setPersonalLimit(int count) {
        when(ipResourceConfiguration.getLimit(ipv4Address)).thenReturn(count);
        when(ipResourceConfiguration.getLimit(ipv6Address)).thenReturn(count);
        personalObjectAccounting.resetAccounting();
    }
}
