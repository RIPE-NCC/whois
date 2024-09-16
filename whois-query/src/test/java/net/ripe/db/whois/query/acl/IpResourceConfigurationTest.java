package net.ripe.db.whois.query.acl;

import net.ripe.db.whois.common.domain.IpResourceEntry;
import net.ripe.db.whois.common.ip.IpInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpResourceConfigurationTest {

    @Mock private IpResourceConfiguration.Loader loader;
    private IpResourceConfiguration subject;

    private InetAddress inetAddress;

    @BeforeEach
    public void setup() throws UnknownHostException {
        when(loader.loadIpLimits()).thenReturn(Collections.<IpResourceEntry<Integer>>emptyList());
        when(loader.loadIpProxy()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());
        when(loader.loadIpDenied()).thenReturn(Collections.<IpResourceEntry<Boolean>>emptyList());

        inetAddress = InetAddress.getByName("128.0.0.1");

        subject = new IpResourceConfiguration(loader, 5000);
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
        when(loader.loadIpLimits()).thenReturn(entries);

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
