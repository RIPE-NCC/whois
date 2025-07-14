package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@Tag("IntegrationTest")
public class IndexWithInet6numIntegrationTest extends IndexIntegrationTestBase {
    private RpslObjectInfo rpslObjectInfo;

    private IndexWithInet6num subject;

    @BeforeEach
    public void setup() {
        subject = new IndexWithInet6num(AttributeType.INET6NUM);
        rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET6NUM, "2001::/64");
    }

    @Test
    public void delete_inet6num() {
        final Ipv6Resource resource = Ipv6Resource.parse(rpslObjectInfo.getKey());

        whoisTemplate.update("INSERT INTO inet6num (object_id, i6_msb, i6_lsb, prefix_length) VALUES(?, ?, ?, ?)",
                rpslObjectInfo.getObjectId(),
                Ipv6Resource.msb(resource.begin()),
                Ipv6Resource.lsb(resource.begin()),
                resource.getPrefixLength());
        checkRows(1);


        subject.removeFromIndex(whoisTemplate, rpslObjectInfo);

        checkRows(0);
    }

    @Test
    public void find_msb_index_inet6num_matches() {
        databaseHelper.addObject("inet6num: 2001:db8:60::/48\nnetname: testnet");
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, "2001:db8:60::/48");

        assertThat(found, hasSize(1));
    }

    @Test
    public void find_msb_index_inet6num_must_not_match() {
        databaseHelper.addObject("inet6num: 2001:600:0:100::/56\nnetname: testnet");

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, "2001:600::/56");

        assertThat(found, hasSize(0));

    }

    @Test
    public void find_no_inet6num() {
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found, hasSize(0));
    }

    @Test
    public void find_inet6num() {
        databaseHelper.addObject(RpslObject.parse("inet6num:" + rpslObjectInfo.getKey() + "\nnetname:NN"));

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found, hasSize(1));
        final RpslObjectInfo objectInfo = found.get(0);
        assertThat(objectInfo.getKey(), is(rpslObjectInfo.getKey()));
        assertThat(objectInfo.getObjectId(), is(rpslObjectInfo.getObjectId()));
        assertThat(objectInfo.getObjectType(), is(ObjectType.INET6NUM));
    }

    private void checkRows(int expectedCount) {
        assertThat(whoisTemplate.queryForObject("SELECT COUNT(*) FROM inet6num", Integer.class), is(expectedCount));
    }

    private Ipv6Resource parseIpv6Resource(final String s) {
        try {
            return Ipv6Resource.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

}
