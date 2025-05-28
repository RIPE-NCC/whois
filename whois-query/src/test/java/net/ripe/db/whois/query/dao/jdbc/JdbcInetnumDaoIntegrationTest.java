package net.ripe.db.whois.query.dao.jdbc;


import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.query.dao.InetnumDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class JdbcInetnumDaoIntegrationTest extends AbstractQueryDaoIntegrationTest {

    @Autowired InetnumDao subject;
    private Ipv4Resource ipv4Resource1;
    private Ipv4Resource ipv4Resource2;

    @BeforeEach
    public void setup() {
        ipv4Resource1 = Ipv4Resource.parse("81.80.117.237/32");
        ipv4Resource2 = Ipv4Resource.parse("194.206.65.37/32");
    }

    @Test
    public void findByNetname() {
        databaseHelper.addObject("inetnum: " + ipv4Resource1.toRangeString() + "\nnetname: " + "netname").getObjectId();
        databaseHelper.addObject("inetnum: " + ipv4Resource2.toRangeString() + "\nnetname: " + "other").getObjectId();

        final List<Ipv4Entry> entries = subject.findByNetname("netname");
        assertThat(entries, hasSize(1));
        assertThat(entries.get(0).getKey(), is(ipv4Resource1));
    }
}
