package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.ip.Ipv4Resource;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class IndexWithInetnumIntegrationTest extends IndexIntegrationTestBase {
    private RpslObjectInfo rpslObjectInfo;

    private IndexWithInetnum subject;

    @BeforeEach
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

        assertThat(found, hasSize(0));
    }

    @Test
    public void find_inetnum() {
        databaseHelper.addObject("inetnum: " + rpslObjectInfo.getKey() + "\nnetname: NN");

        final List<RpslObjectInfo> found = subject.findInIndex(whoisTemplate, rpslObjectInfo.getKey());

        assertThat(found, hasSize(1));
        final RpslObjectInfo objectInfo = found.get(0);
        assertThat(objectInfo.getObjectId(), is(rpslObjectInfo.getObjectId()));
        assertThat(objectInfo.getObjectType(), is(ObjectType.INETNUM));
        assertThat(objectInfo.getKey(), is(rpslObjectInfo.getKey()));
    }

    @Test
    public void add_invalid_inet() {
        assertThrows(IllegalArgumentException.class, () -> {
            RpslObjectInfo rpslObjectInfo1 = new RpslObjectInfo(1, ObjectType.INETNUM, "10.0.0.129 - 10.0.0.0");

            subject.addToIndex(whoisTemplate, rpslObjectInfo1, RpslObject.parse("inetnum:10.0.0.129 - 10.0.0.0\nnetname:netname"), "ignoredValue");

        });
    }

    private void checkRows(int expectedCount) {
        assertThat(whoisTemplate.queryForObject("SELECT COUNT(*) FROM inetnum", Integer.class), is(expectedCount));
    }
}
