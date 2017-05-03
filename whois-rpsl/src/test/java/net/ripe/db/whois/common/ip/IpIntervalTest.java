package net.ripe.db.whois.common.ip;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IpIntervalTest {
    @Test
    public void ipv4() throws Exception {
        final IpInterval<?> subject = IpInterval.parse("128/8");

        assertThat(subject.getAttributeType(), is(AttributeType.INETNUM));
        assertThat(subject.toString(), is("128.0.0.0/8"));
    }

    @Test
    public void ipv6() throws Exception {
        final IpInterval<?> subject = IpInterval.parse("::0/8");

        assertThat(subject.getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(subject.toString(), is("::/8"));
    }

    @Test
    public void ipInterval_ipv4() throws Exception {
        final IpInterval<?> subject = IpInterval.asIpInterval(InetAddress.getByName("128.0.0.0"));

        assertThat(subject.getAttributeType(), is(AttributeType.INETNUM));
        assertThat(subject.toString(), is("128.0.0.0/32"));
    }

    @Test
    public void parseReverseDomain() {
        assertThat(IpInterval.parseReverseDomain("0.0.193.in-ADDR.arpA").toString(), is("193.0.0.0/24"));
        assertThat(IpInterval.parseReverseDomain("a.b.0.0.1.iP6.arpA").toString(), is("100b:a000::/20"));
        assertThat(IpInterval.parseReverseDomain("0.0.193.in-ADDR.arpA.").toString(), is("193.0.0.0/24"));
        assertThat(IpInterval.parseReverseDomain("a.b.0.0.1.iP6.arpA.").toString(), is("100b:a000::/20"));
    }

    @Test
    public void parse() {
        assertThat(IpInterval.parse("193.0.0/24").toString(), is("193.0.0.0/24"));
        assertThat(IpInterval.parse("00ab:cd::").toString(), is("ab:cd::/128"));
    }


    @Test
    public void ipInterval_ipv6() throws Exception {
        final IpInterval<?> subject = IpInterval.asIpInterval(InetAddress.getByName("3ffe:6a88:85a3:08d3:1319:8a2e:0370:7344"));

        assertThat(subject.getAttributeType(), is(AttributeType.INET6NUM));
        assertThat(subject.toString(), is("3ffe:6a88:85a3:8d3:1319:8a2e:370:7344/128"));
    }
}
