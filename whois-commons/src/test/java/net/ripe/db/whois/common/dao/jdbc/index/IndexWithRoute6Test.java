package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithRoute6Test extends IndexTestBase {
    private IndexWithRoute6 subject;

    private RpslObject route6;
    private RpslObjectInfo route6Info;

    @Before
    public void setup() {
        subject = new IndexWithRoute6(AttributeType.ROUTE);

        route6 = RpslObject.parse("" +
                "route6:          a001:1578:0200::/40\n" +
                "origin:          AS12817\n");

        route6Info = new RpslObjectInfo(1, route6.getType(), route6.getKey());
    }

    @Test
    public void addToIndex() {
        subject.addToIndex(whoisTemplate, route6Info, route6, route6.getTypeAttribute().getCleanValue());

        assertThat(whoisTemplate.queryForInt("SELECT COUNT(*) FROM route6"), is(1));
    }

    @Test
    public void findInIndex() {
        databaseHelper.addObject(route6);

        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, route6Info.getKey());
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0).getKey(), is(route6Info.getKey()));
    }

    @Test
    public void findInIndex_lc() {
        databaseHelper.addObject(route6);

        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, route6Info.getKey().toLowerCase());
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0).getKey(), is(route6Info.getKey()));
    }

    @Test
    public void removeFromIndex() {
        databaseHelper.addObject(route6);

        subject.removeFromIndex(whoisTemplate, route6Info);
        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, route6Info.getKey());
        assertThat(infos, hasSize(0));
    }
}
