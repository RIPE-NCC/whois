package net.ripe.db.whois.common.domain;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class IpResourceTreeTest {
    private IpResourceTree<Integer> subject;

    private IpInterval<?> ipv4Resource, ipv4ResourceMoreSpecific, ipv4ResourceUnknown;
    private IpInterval<?> ipv6Resource, ipv6ResourceMoreSpecific, ipv6ResourceUnknown;

    @Before
    public void setUp() throws Exception {
        subject = new IpResourceTree<Integer>();

        ipv4Resource = IpInterval.parse("128/16");
        ipv4ResourceMoreSpecific = IpInterval.parse("128.0.0.1");
        ipv4ResourceUnknown = IpInterval.parse("129.0.0.2");

        ipv6Resource = IpInterval.parse("::0/32");
        ipv6ResourceMoreSpecific = IpInterval.parse("::0/64");
        ipv6ResourceUnknown = IpInterval.parse("1::1");

        subject.add(ipv4Resource, 41);
        subject.add(ipv6Resource, 61);
    }

    @Test
    public void test_getValue_ipv4_exact() {
        assertThat(subject.getValue(ipv4Resource), is(41));
    }

    @Test
    public void test_getValue_ipv4_lessSpecific() {
        assertThat(subject.getValue(ipv4ResourceMoreSpecific), is(41));
    }

    @Test
    public void test_getValue_ipv4_unknown() {
        assertThat(subject.getValue(ipv4ResourceUnknown), is(nullValue()));
    }

    @Test
    public void test_getValue_ipv6_exact() {
        assertThat(subject.getValue(ipv6Resource), is(61));
    }

    @Test
    public void test_getValue_ipv6_lessSpecific() {
        assertThat(subject.getValue(ipv6ResourceMoreSpecific), is(61));
    }

    @Test
    public void test_getValue_ipv6_unknown() {
        assertThat(subject.getValue(ipv6ResourceUnknown), is(nullValue()));
    }
}
