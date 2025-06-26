package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@Tag("IntegrationTest")
public class IndexWithRouteIntegrationTest extends IndexIntegrationTestBase {
    private IndexWithRoute subject;

    private RpslObject route;
    private RpslObjectInfo routeInfo;

    @BeforeEach
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

        assertThat(whoisTemplate.queryForObject("SELECT COUNT(*) FROM route", Integer.class), is(1));
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
