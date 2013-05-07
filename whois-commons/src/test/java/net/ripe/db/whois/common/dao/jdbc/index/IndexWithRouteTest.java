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

public class IndexWithRouteTest extends IndexTestBase {
    private IndexWithRoute subject;

    private RpslObject route;
    private RpslObjectInfo routeInfo;

    @Before
    public void setup() {
        subject = new IndexWithRoute(AttributeType.ROUTE);

        route = RpslObject.parse("" +
                "route:           193.254.30.0/24\n" +
                "origin:          AS12726\n");

        routeInfo = new RpslObjectInfo(1, route.getType(), route.getKey());
    }

    @Test
    public void addToIndex() {
        subject.addToIndex(whoisTemplate, routeInfo, route, route.getTypeAttribute().getCleanValue());

        assertThat(whoisTemplate.queryForInt("SELECT COUNT(*) FROM route"), is(1));
    }

    @Test
    public void findInIndex() {
        databaseHelper.addObject(route);

        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, routeInfo.getKey());
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0).getKey(), is(routeInfo.getKey()));
    }

    @Test
    public void findInIndex_lc() {
        databaseHelper.addObject(route);

        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, routeInfo.getKey().toLowerCase());
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0).getKey(), is(routeInfo.getKey()));
    }

    @Test
    public void removeFromIndex() {
        databaseHelper.addObject(route);

        subject.removeFromIndex(whoisTemplate, routeInfo);
        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, routeInfo.getKey());
        assertThat(infos, hasSize(0));
    }
}
