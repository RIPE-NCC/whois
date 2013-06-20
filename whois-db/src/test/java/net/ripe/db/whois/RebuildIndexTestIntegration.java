package net.ripe.db.whois;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.IndexDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.database.diff.Database;
import net.ripe.db.whois.common.support.database.diff.DatabaseDiff;
import net.ripe.db.whois.common.support.database.diff.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class RebuildIndexTestIntegration extends AbstractIntegrationTest {

    @Autowired IndexDao indexDao;

    @Before
    public void setup() {
        databaseHelper.addObject(RpslObject.parse(
                "person:    Test Person\n" +
                "nic-hdl:   TP1-TEST\n" +
                "source:    TEST"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:    TST-MNT\n" +
                "descr:     description\n" +
                "admin-c:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "upd-to:    dbtest@ripe.net\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:   dbtest@ripe.net 20120707\n" +
                "source:    TEST\n"));
        databaseHelper.updateObject(RpslObject.parse(
                "person:    Test Person\n" +
                "address:   NL\n" +
                "phone:     +31 20 123456\n" +
                "nic-hdl:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   dbtest@ripe.net 20120101\n" +
                "source:    TEST\n"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Ltd\n" +
                "org-type:      other\n" +
                "address:       NL\n" +
                "e-mail:        testing@ripe.net\n" +
                "mnt-ref:       TST-MNT\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       testing@ripe.net 20130617\n" +
                "source: TEST"));
        databaseHelper.addObject(RpslObject.parse(
                "aut-num:    AS123\n" +
                "as-name:    TST-AS\n" +
                "descr:      Testing\n" +
                "org:        ORG-TOL1-TEST\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));
    }

    @Test
    public void rebuild_base_database() throws Exception {
        DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));

        // TODO: [ES] 3 rows removed
        // assertThat(diff.getRemoved().getAll(), hasSize(0));

        // mntner:
        //       {thread_id=0, object_id=0, mntner=ANY, dummy=1}
        // names:
        //       {thread_id=0, object_id=1, name=Person, object_type=10}
        //       {thread_id=0, object_id=1, name=Test, object_type=10}
    }

    @Test
    public void inetnum_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inetnum:        010.0.00.000 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("10.0.0.0 - 10.255.255.255"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void inetnum_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    @Test
    public void inet6num_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inet6num:       2001:0100:0000::/24\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("2001:100::/24"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "inet6num:       2001:100::/24\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void inet6num_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    @Test
    public void route_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "route:      10.01.2.0/24\n" +
                "descr:      Test route\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("10.1.2.0/24AS123"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "route:          10.1.2.0/24\n" +
                "descr:          Test route\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void route_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "route:      10.1.2.0/24\n" +
                "descr:      Test route\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    @Test
    public void route6_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "route6:     2001:0100::/24\n" +
                "descr:      TEST\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("2001:100::/24AS123"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "route6:         2001:100::/24\n" +
                "descr:          TEST\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void route6_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "route6:     2001:100::/24\n" +
                "descr:      TEST\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    @Test
    public void inetrtr_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inet-rtr:   test.ripe.net.\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("test.ripe.net"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "inet-rtr:       test.ripe.net\n" +
                "descr:          description\n" +
                "local-as:       AS123\n" +
                "ifaddr:         10.0.0.1 masklen 22\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        test@ripe.net 20120622\n" +
                "source:         TEST\n"));
    }

    @Test
    public void inetrtr_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "inet-rtr:   test.ripe.net\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    @Test
    public void domain_last_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "domain:     0.0.10.in-addr.arpa.\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));

        final Table modifiedInLast = diff.getModified().getTable("last");
        assertThat(modifiedInLast, hasSize(1));
        assertThat(modifiedInLast.get(0).getString("pkey"), is("0.0.10.in-addr.arpa"));
        assertThat(new String((byte[])modifiedInLast.get(0).get("object")), is(
                "domain:         0.0.10.in-addr.arpa\n" +
                "descr:          Test domain\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "zone-c:         TP1-TEST\n" +
                "nserver:        ns.foo.net\n" +
                "nserver:        ns.bar.net\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        test@ripe.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void domain_last_not_updated() {
        databaseHelper.addObject(RpslObject.parse(
                "domain:     0.0.10.in-addr.arpa\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST"));

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getTable("last"), is(nullValue()));
        assertThat(diff.getRemoved().getTable("last"), is(nullValue()));
        assertThat(diff.getModified().getTable("last"), is(nullValue()));
    }

    private DatabaseDiff rebuild() {
        final Database before = new Database(databaseHelper.getWhoisTemplate());
        indexDao.rebuild();
        return Database.diff(before, new Database(databaseHelper.getWhoisTemplate()));
    }
}
