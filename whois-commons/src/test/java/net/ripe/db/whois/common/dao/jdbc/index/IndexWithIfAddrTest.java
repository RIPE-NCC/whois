package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithIfAddrTest extends IndexTestBase {
    private IndexWithIfAddr subject;

    @Before
    public void setup() {
        subject = new IndexWithIfAddr(AttributeType.IFADDR);
    }

    @Test
    public void add_to_index() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: 10.2.3.4 masklen 32");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET_RTR, rpslObject.getKey());
        checkRows(0);

        final int rows = subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, getIfAddrAttributeAsString(rpslObject));

        assertThat(rows, is(1));
        checkRows(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_to_index_invalid_ifaddr_string() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: invalid");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET_RTR, rpslObject.getKey());

        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, getIfAddrAttributeAsString(rpslObject));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_to_index_invalid_ifaddr_ipv6_address() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: ::1");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET_RTR, rpslObject.getKey());

        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, getIfAddrAttributeAsString(rpslObject));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_to_index_invalid_ifaddr_is_range() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: 10.1.2.0/24");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET_RTR, rpslObject.getKey());

        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, getIfAddrAttributeAsString(rpslObject));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_to_index_empty_ifaddr() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr:\n");
        RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INET_RTR, rpslObject.getKey());

        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, getIfAddrAttributeAsString(rpslObject));
    }

    @Test
    public void find_in_index() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: 10.2.3.4 masklen 32");
        addObject(rpslObject);

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, "10.2.3.4");

        assertThat(found.size(), is(1));
        final RpslObjectInfo objectInfo = found.get(0);
        assertThat(objectInfo.getObjectId(), is(1));
        assertThat(objectInfo.getObjectType(), is(ObjectType.INET_RTR));
        assertThat(objectInfo.getKey(), is("test"));
    }

    @Test
    public void find_in_index_not_found() {
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, "10.2.3.4");

        assertThat(found.size(), is(0));
    }

    @Test
    public void remove_from_index() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: 10.2.3.4 masklen 32");
        checkRows(0);
        addObject(rpslObject);
        checkRows(1);

        subject.removeFromIndex(whoisTemplate, new RpslObjectInfo(getObjectId(rpslObject), ObjectType.INET_RTR, rpslObject.getKey()));

        checkRows(0);
    }

    @Test
    public void remove_from_index_different_inetrtr() {
        RpslObject rpslObject = RpslObject.parse("inet-rtr: test\nifaddr: 10.2.3.4 masklen 32");
        addObject(rpslObject);
        checkRows(1);

        subject.removeFromIndex(whoisTemplate, new RpslObjectInfo(2, ObjectType.INET_RTR, "another inet-rtr"));

        checkRows(1);
    }

    //

    private int addObject(final RpslObject rpslObject) {
        databaseHelper.addObject("inet-rtr: " + rpslObject.findAttribute(AttributeType.INET_RTR).getValue());
        final int objectId = getObjectId(rpslObject);
        final long ifAddr = Ipv4Resource.parse(getIfAddrAttributeAsString(rpslObject)).begin();
        whoisTemplate.update("INSERT INTO ifaddr (object_id, ifaddr) VALUES (?, ?)",
                objectId, ifAddr);
        return objectId;
    }

    private void checkRows(int expectedCount) {
        assertThat(whoisTemplate.queryForInt("SELECT COUNT(*) FROM ifaddr"), is(expectedCount));
    }

    private String getIfAddrAttributeAsString(final RpslObject rpslObject) {
        return rpslObject.findAttribute(AttributeType.IFADDR).getCleanValue().toString().split(" ")[0];
    }
}
