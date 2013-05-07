package net.ripe.db.whois.common.domain;

import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HostsTest {

    @Test
    public void getHost() {
        for (final Hosts hosts : Hosts.values()) {
            assertThat(Hosts.getHost(hosts.getHostName()), is(hosts));
        }
    }

    @Test
    public void getHost_unknown() {
        assertThat(Hosts.getHost("somehost"), is(Hosts.UNDEFINED));
    }

    @Test
    public void getLocalHost() {
        assertThat(Hosts.getLocalHost(), instanceOf(Hosts.class));
    }
}
