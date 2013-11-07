package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.ip.IpInterval;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class IpRangesTest {

    private IpRanges subject;

    @Before
    public void setup() {
        subject = new IpRanges();
    }

    @Test
    public void ipv4_addresses() throws Exception {
        subject.setTrusted("193.0.20/24");

        assertThat(subject.isTrusted(IpInterval.parse("193.0.20.1")), is(true));
        assertThat(subject.isTrusted(IpInterval.parse("193.0.21.1")), is(false));
    }

    @Test
    public void ipv6_addresses() throws Exception {
        subject.setTrusted("2001:14b8:100:9f::/64");

        assertThat(subject.isTrusted(IpInterval.parse("2001:14b8:100:9f:1111:2222:3333:4444")), is(true));
        assertThat(subject.isTrusted(IpInterval.parse("2001:14b8:100:af::")), is(false));
    }

    @Test
    public void both_address_types() throws Exception {
        subject.setTrusted("193.0.20/24", "2001:14b8:100:9f::/64");

        assertThat(subject.isTrusted(IpInterval.parse("2001:14b8:100:9f:1111:2222:3333:4444")), is(true));
        assertThat(subject.isTrusted(IpInterval.parse("193.0.20.1")), is(true));
    }

    @Test
    public void empty_file() throws Exception {
        subject.setTrusted();

        assertFalse(subject.isTrusted(IpInterval.parse("127.0.0.1")));
    }
}
