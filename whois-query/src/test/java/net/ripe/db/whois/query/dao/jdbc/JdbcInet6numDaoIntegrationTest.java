package net.ripe.db.whois.query.dao.jdbc;


import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.query.dao.Inet6numDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.UnknownHostException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class JdbcInet6numDaoIntegrationTest extends AbstractQueryDaoIntegrationTest {

    @Autowired Inet6numDao subject;

    private Ipv6Resource ipv6Resource1;
    private Ipv6Resource ipv6Resource2;

    @BeforeEach
    public void setup() throws UnknownHostException {
        ipv6Resource1 = Ipv6Resource.parse("2A02:758:500::/48");
        ipv6Resource2 = Ipv6Resource.parse("2a01:198:200:397::/64");
    }

    @Test
    public void findByNetname() {
        databaseHelper.addObject("inet6num: " + ipv6Resource1.toString() + "\nnetname: " + "netname").getObjectId();
        databaseHelper.addObject("inet6num: " + ipv6Resource2.toString() + "\nnetname: " + "other").getObjectId();

        final List<Ipv6Entry> entries = subject.findByNetname("netname");
        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).getKey(), is(ipv6Resource1));
    }
}
