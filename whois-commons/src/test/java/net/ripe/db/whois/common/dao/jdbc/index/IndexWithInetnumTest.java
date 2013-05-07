package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithInetnumTest extends IndexTestBase {
    private RpslObjectInfo rpslObjectInfo;

    private IndexWithInetnum subject;

    @Before
    public void setup() {
        subject = new IndexWithInetnum(AttributeType.INETNUM);
        rpslObjectInfo = new RpslObjectInfo(1, ObjectType.INETNUM, "80.16.151.184 - 80.16.151.191");
    }

    @Test
    public void delete_inetnum() {
        final Ipv4Resource resource = Ipv4Resource.parse(rpslObjectInfo.getKey());
        whoisTemplate.update("INSERT INTO inetnum (object_id, begin_in, end_in) VALUES(?, ?, ?)", rpslObjectInfo.getObjectId(), resource.begin(), resource.end());

        checkRows(1);

        subject.removeFromIndex(whoisTemplate, rpslObjectInfo);

        checkRows(0);
    }

    @Test
    public void find_no_inetnum() {
        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found.size(), is(0));
    }

    @Test
    public void find_inetnum() {
        databaseHelper.addObject("inetnum: " + rpslObjectInfo.getKey() + "\nnetname: NN");

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found.size(), is(1));
        final RpslObjectInfo objectInfo = found.get(0);
        assertThat(objectInfo.getObjectId(), is(rpslObjectInfo.getObjectId()));
        assertThat(objectInfo.getObjectType(), is(ObjectType.INETNUM));
        assertThat(objectInfo.getKey(), is(rpslObjectInfo.getKey()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_invalid_inet() {
        RpslObjectInfo rpslObjectInfo1 = new RpslObjectInfo(1, ObjectType.INETNUM, "10.0.0.129 - 10.0.0.0");

        subject.addToIndex(whoisTemplate, rpslObjectInfo1, RpslObject.parse("inetnum:10.0.0.129 - 10.0.0.0\nnetname:netname"), "ignoredValue");
    }

    private void checkRows(int expectedCount) {
        assertThat(whoisTemplate.queryForInt("SELECT COUNT(*) FROM inetnum"), is(expectedCount));
    }
}
