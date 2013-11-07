package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithInet6numTest extends IndexTestBase {
    private RpslObjectInfo rpslObjectInfo;

    private IndexWithInet6num subject;

    @Before
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
    public void mysqlBotchesOn64bitSignedInteger() {
        databaseHelper.addObject("inet6num: 2001:db8:60::/48\nnetname: testnet");
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, "2001:db8:60::/48");

        assertThat(found.size(), is(1));
    }

    @Test
    public void find_no_inet6num() {
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found.size(), is(0));
    }

    @Test
    public void find_inet6num() {
        databaseHelper.addObject(RpslObject.parse("inet6num:" + rpslObjectInfo.getKey() + "\nnetname:NN"));

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found.size(), is(1));
        final RpslObjectInfo objectInfo = found.get(0);
        assertThat(objectInfo.getKey(), is(rpslObjectInfo.getKey()));
        assertThat(objectInfo.getObjectId(), is(rpslObjectInfo.getObjectId()));
        assertThat(objectInfo.getObjectType(), is(ObjectType.INET6NUM));
    }

    private void checkRows(int expectedCount) {
        assertThat(whoisTemplate.queryForInt("SELECT COUNT(*) FROM inet6num"), is(expectedCount));
    }
}
