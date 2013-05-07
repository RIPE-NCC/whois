package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithMntRoutesTest extends IndexTestBase {
    private IndexStrategy subject;

    private RpslObject maintainer;

    @Before
    public void setUp() throws Exception {
        maintainer = RpslObject.parse("mntner: DEV-MNT");
        databaseHelper.addObject(maintainer);

        subject = IndexStrategies.get(AttributeType.MNT_ROUTES);
    }

    @Test
    public void add_for_inetnum() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:10.0.0.129 - 10.0.0.0\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {ANY}\n");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MNT_ROUTES));

        assertThat(getNrMntRoutes(), is(1));
    }

    @Test
    public void add_for_inet6num() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:2a00:c00::/32\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/48}\n");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MNT_ROUTES));

        assertThat(getNrMntRoutes(), is(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_for_inetnum_unknown_maintainer() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:10.0.0.0 - 10.0.0.255\n" +
                "netname:netname\n" +
                "mnt-routes: UNKNOWN-MNT {ANY}\n");

        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(2, rpslObject.getType(), rpslObject.getKey());
        subject.addToIndex(whoisTemplate, rpslObjectInfo, rpslObject, rpslObject.getValueForAttribute(AttributeType.MNT_ROUTES));
    }

    @Test
    public void remove_for_inetnum() {
        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:10.0.0.0 - 10.0.0.255\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {10.0.0.0/28}\n");

        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num:2a00:c00::/32\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/48}\n");

        databaseHelper.addObject(inetnum);
        assertThat(getNrMntRoutes(), is(1));
        databaseHelper.addObject(inet6num);
        assertThat(getNrMntRoutes(), is(2));

        subject.removeFromIndex(whoisTemplate, new RpslObjectInfo(2, inetnum.getType(), inetnum.getKey()));
        assertThat(getNrMntRoutes(), is(1));
    }

    @Test
    public void remove_for_inet6num() {
        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:10.0.0.0 - 10.0.0.255\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {10.0.0.0/28}\n");

        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num:2a00:c00::/32\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/48}\n");

        databaseHelper.addObject(inetnum);
        assertThat(getNrMntRoutes(), is(1));
        databaseHelper.addObject(inet6num);
        assertThat(getNrMntRoutes(), is(2));

        subject.removeFromIndex(whoisTemplate, new RpslObjectInfo(3, inet6num.getType(), inet6num.getKey()));
        assertThat(getNrMntRoutes(), is(1));
    }

    @Test
    public void findInIndex_not_found() throws Exception {
        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "DEV-MNT");

        assertThat(results.size(), is(0));
    }

    @Test
    public void findInIndex_single() throws Exception {
        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:10.0.0.0 - 10.0.0.255\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {10.0.0.0/28}\n");

        databaseHelper.addObject(inetnum);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "DEV-MNT");
        assertThat(results.size(), is(1));
    }

    @Test
    public void findInIndex_both() throws Exception {
        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:10.0.0.0 - 10.0.0.255\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {10.0.0.0/28}\n");

        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num:2a00:c00::/32\n" +
                "netname:netname\n" +
                "mnt-routes: DEV-MNT {2a00:c00::/48}\n");

        databaseHelper.addObject(inetnum);
        databaseHelper.addObject(inet6num);

        final List<RpslObjectInfo> results = subject.findInIndex(whoisTemplate, "DEV-MNT");
        assertThat(results.size(), is(2));
    }

    private int getNrMntRoutes() {
        return whoisTemplate.queryForInt("select count(*) from mnt_routes");
    }
}