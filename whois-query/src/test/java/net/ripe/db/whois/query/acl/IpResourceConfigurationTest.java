package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.IpResourceEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpResourceConfigurationTest {

    @Mock private IpResourceConfiguration.Loader loader;
    @InjectMocks private IpResourceConfiguration subject;

    private InetAddress inetAddress;

    @Before
    public void setup() throws UnknownHostException {
        when(loader.loadIpLimit()).thenReturn(Collections.<IpResourceEntry<Integer>>emptyList());
        when(loader.loadIpProxy()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());
        when(loader.loadIpDenied()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());

        inetAddress = InetAddress.getByName("128.0.0.1");

        subject.reload();
    }

    @Test
    public void test_limit_default() throws Exception {
        assertThat(subject.getLimit(inetAddress), is(5000));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_limit_specified() throws Exception {
        final IpResourceEntry<Integer> entry = new IpResourceEntry<>(IpInterval.asIpInterval(inetAddress), 1000);
        final List<IpResourceEntry<Integer>> entries = Arrays.asList(entry);
        when(loader.loadIpLimit()).thenReturn(entries);

        subject.reload();

        assertThat(subject.getLimit(inetAddress), is(1000));
    }

    @Test
    public void test_proxy_default() throws Exception {
        assertThat(subject.isProxy(inetAddress), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_proxy_specified() throws Exception {
        final IpResourceEntry<Boolean> entry = new IpResourceEntry<>(IpInterval.asIpInterval(inetAddress), true);
        when(loader.loadIpProxy()).thenReturn(Arrays.asList(entry));

        subject.reload();

        assertThat(subject.isProxy(inetAddress), is(true));
    }

    @Test
    public void test_denied_default() throws Exception {
        assertThat(subject.isDenied(inetAddress), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_denied_specified() throws Exception {
        final IpResourceEntry<Boolean> entry = new IpResourceEntry<>(IpInterval.asIpInterval(inetAddress), true);
        when(loader.loadIpDenied()).thenReturn(Arrays.asList(entry));

        subject.reload();

        assertThat(subject.isDenied(inetAddress), is(true));
    }

    @Test
    public void test_unlimitedConnections_default() throws Exception {
        assertThat(subject.isUnlimitedConnections(inetAddress), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_unlimitedConnections_specified() throws Exception {
        final IpResourceEntry<Boolean> entry = new IpResourceEntry<>(IpInterval.asIpInterval(inetAddress), true);
        when(loader.loadUnlimitedConnections()).thenReturn(Arrays.asList(entry));

        subject.reload();

        assertThat(subject.isUnlimitedConnections(inetAddress), is(true));
    }
}
