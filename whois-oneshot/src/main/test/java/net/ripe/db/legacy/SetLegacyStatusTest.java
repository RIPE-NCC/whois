package net.ripe.db.legacy;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
    (1) input line: 137.191.192.0/20,/21,/22,/23,/24
        should map to: 137.191.192.0 - 137.191.222.255 [7936]
            137.191.192/20      137.191.192.0 - 137.191.207.255 [4096]
            137.191.208/21      137.191.208.0 - 137.191.215.255 [2048]
            137.191.216/22      137.191.216.0 - 137.191.219.255 [1024]
            137.191.220/23      137.191.220.0 - 137.191.221.255 [512]
            137.191.222/24      137.191.222.0 - 137.191.222.255 [256]
    (2) input line: 128.141.0.0/16,/16
        should map to: 128.141.0.0 - 128.142.255.255
 */
public class SetLegacyStatusTest {

    private SetLegacyStatus subject;

    @Before
    public void setup() throws Exception {
        this.subject = new SetLegacyStatus(null, "dbint", "dbint", null, false);
    }

    @Test
    public void createIpv4Resource() {
        assertThat(subject.createIpv4Resource("10.0.0.0/8"), is(Ipv4Resource.parse("10.0.0.0/8")));
        assertThat(subject.createIpv4Resource("128.130.0.0/15"), is(Ipv4Resource.parseIPv4Resource("128.130.0.0 - 128.131.255.255")));
    }

    @Test
    public void createIpv4ResourceFromCommaSeparatedList() {
        assertThat(subject.createIpv4ResourceFromCommaSeparatedList("137.191.192.0/20,/21,/22,/23,/24"), is(Ipv4Resource.parse("137.191.192.0 - 137.191.222.255")));
        assertThat(subject.createIpv4ResourceFromCommaSeparatedList("128.141.0.0/16,/16"), is(Ipv4Resource.parse("128.141.0.0 - 128.142.255.255")));
        assertThat(subject.createIpv4ResourceFromCommaSeparatedList("128.141.0.0/16,/17"), is(Ipv4Resource.parse("128.141.0.0 - 128.142.127.255")));
    }

    @Test
    public void createIpv4ResourceFromAddressAndLength() {
        final Ipv4Resource slash8 = Ipv4Resource.parse("10.0.0.0/8");
        assertThat(subject.createIpv4Resource(slash8.begin(), 8), is(slash8));

        final Ipv4Resource slash15 = Ipv4Resource.parse("192.168.0.0/15");
        assertThat(subject.createIpv4Resource(slash15.begin(), 15), is(slash15));

        final Ipv4Resource slash16 = Ipv4Resource.parse("192.168.0.0 - 192.168.255.255");
        assertThat(subject.createIpv4Resource(slash16.begin(), 16), is(slash16));
    }
}
