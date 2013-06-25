package net.ripe.db.whois;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.IndexDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.database.diff.Database;
import net.ripe.db.whois.common.support.database.diff.DatabaseDiff;
import net.ripe.db.whois.common.support.database.diff.Row;
import net.ripe.db.whois.common.support.database.diff.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class RebuildIndexTestIntegration extends AbstractIntegrationTest {

    @Autowired IndexDao indexDao;

    @Before
    public void setup() {
        databaseHelper.addObject(RpslObject.parse("" +
                "person:    Test Person\n" +
                "nic-hdl:   TP1-TEST\n" +
                "source:    TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:    TST-MNT\n" +
                "descr:     description\n" +
                "admin-c:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "upd-to:    dbtest@ripe.net\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:   dbtest@ripe.net 20120707\n" +
                "source:    TEST\n"));
        databaseHelper.updateObject(RpslObject.parse("" +
                "person:    Test Person\n" +
                "address:   NL\n" +
                "phone:     +31 20 123456\n" +
                "nic-hdl:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   dbtest@ripe.net 20120101\n" +
                "source:    TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Ltd\n" +
                "org-type:      other\n" +
                "address:       NL\n" +
                "e-mail:        testing@ripe.net\n" +
                "mnt-ref:       TST-MNT\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       testing@ripe.net 20130617\n" +
                "source: TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "aut-num:    AS123\n" +
                "as-name:    TST-AS\n" +
                "descr:      Testing\n" +
                "org:        ORG-TOL1-TEST\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST"));
    }

    @Test
    public void asblock() {
        final RpslObject object = RpslObject.parse("" +
                "as-block:       AS1 - AS99\n" +
                "descr:          Test ASN blocks\n" +
                "remarks:        Testing\n" +
                "org:            ORG-TOL1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20130101\n" +
                "mnt-lower:      TST-MNT\n" +
                "source:         TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void asblock_missing_reference() {
        rebuild(RpslObject.parse("" +
                "as-block:       AS1 - AS99\n" +
                "descr:          Test ASN blocks\n" +
                "remarks:        Testing\n" +
                "org:            ORG-TOL1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20130101\n" +
                "mnt-lower:      TST-MNT\n" +
                "source:         TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'AS1 - AS99'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(4));
        assertThat(diff.getRemoved().getTable("as_block"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
    }

    @Test
    public void asset() {
        final RpslObject object = RpslObject.parse("" +
                "as-set:       AS-TEST\n" +
                "descr:        test as-set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       asnotify@test.net\n" +
                "changed:      test@ripe.net 20120101\n" +
                "source:       TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void asset_missing_reference() {
        rebuild(RpslObject.parse("" +
                "as-set:       AS-TEST\n" +
                "descr:        test as-set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       asnotify@test.net\n" +
                "changed:      test@ripe.net 20120101\n" +
                "source:       TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'AS-TEST'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("as_set"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mbrs_by_ref"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void autnum() {
        databaseHelper.addObject("" +
                "as-set:       AS-TESTSET\n" +
                "descr:        Test Set\n" +
                "members:      AS1\n" +
                "member-of:    AS-TESTSET\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "notify:       noreply@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "changed:      noreply@ripe.net 20120101\n" +
                "source:       TEST");

        final RpslObject object = RpslObject.parse("" +
                "aut-num:        AS101\n" +
                "as-name:        some-name\n" +
                "descr:          description\n" +
                "org:            ORG-TOL1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "notify:         noreply@ripe.net\n" +
                "changed:        test@ripe.net 20120101\n" +
                "source:         TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void autnum_missing_reference() {
        databaseHelper.addObject("" +
                "as-set:       AS-TESTSET\n" +
                "descr:        Test Set\n" +
                "members:      AS1\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "notify:       noreply@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "changed:      noreply@ripe.net 20120101\n" +
                "source:       TEST");

        rebuild(RpslObject.parse("" +
                "aut-num:        AS101\n" +
                "as-name:        some-name\n" +
                "descr:          description\n" +
                "member-of:      AS-TESTSET\n" +
                "org:            ORG-TOL1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "notify:         noreply@ripe.net\n" +
                "changed:        test@ripe.net 20120101\n" +
                "source:         TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'AS101'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(9));
        assertThat(diff.getRemoved().getTable("aut_num"), hasSize(1));
        assertThat(diff.getRemoved().getTable("member_of"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_routes"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void domain() {
        final RpslObject object = RpslObject.parse("" +
                "domain:     0.0.10.in-addr.arpa\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     notify@ripe.net\n" +
                "ds-rdata:   52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void domain_missing_reference() {

        rebuild(RpslObject.parse("" +
                "domain:     0.0.10.in-addr.arpa\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     notify@ripe.net\n" +
                "ds-rdata:   52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = '0.0.10.in-addr.arpa'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(10));
        assertThat(diff.getRemoved().getTable("domain"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("zone_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("nserver"), hasSize(2));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("ds_rdata"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
    }

    @Test
    public void domain_sanitized() {
        final RpslObject object = RpslObject.parse("" +
                "domain:     0.0.10.in-addr.arpa.\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     notify@ripe.net\n" +
                "ds-rdata:   52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(2));

        final Table domainTable = diff.getModified().getTable("domain");
        assertThat(domainTable, hasSize(1));
        final Row domainRow = domainTable.get(0);
        assertThat(domainRow.getString("domain"), is("0.0.10.in-addr.arpa"));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("0.0.10.in-addr.arpa"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "domain:         0.0.10.in-addr.arpa\n" +
                "descr:          Test domain\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "zone-c:         TP1-TEST\n" +
                "nserver:        ns.foo.net\n" +
                "nserver:        ns.bar.net\n" +
                "org:            ORG-TOL1-TEST\n" +
                "notify:         notify@ripe.net\n" +
                "ds-rdata:       52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        test@ripe.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void filterset() {
        final RpslObject object = RpslObject.parse("" +
                "filter-set:   fltr-test\n" +
                "descr:        test filter\n" +
                "filter:       AS101\n" +
                "org:          ORG-TOL1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "notify:       test@test.net\n" +
                "changed:      dbtest@ripe.net 20120101\n" +
                "source:  TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void filterset_missing_reference() {
        rebuild(RpslObject.parse("" +
                "filter-set:   fltr-test\n" +
                "descr:        test filter\n" +
                "filter:       AS101\n" +
                "org:          ORG-TOL1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "notify:       test@test.net\n" +
                "changed:      dbtest@ripe.net 20120101\n" +
                "source:  TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'fltr-test'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(7));
        assertThat(diff.getRemoved().getTable("filter_set"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void inetnum() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        final RpslObject object = RpslObject.parse("" +
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inetnum_missing_reference() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        rebuild(RpslObject.parse("" +
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = '10.0.0.0 - 10.255.255.255'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(10));
        assertThat(diff.getRemoved().getTable("inetnum"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_domains"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_routes"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_irt"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
    }

    @Test
    public void inetnum_sanitized() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        final RpslObject object = RpslObject.parse("" +
                "inetnum:        010.0.00.000 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("10.0.0.0 - 10.255.255.255"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void inet6num() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:       3333::4444/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inet6num_missing_reference() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        rebuild(RpslObject.parse("" +
                "inet6num:       3333:4444::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = '3333:4444::/48'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(10));
        assertThat(diff.getRemoved().getTable("inet6num"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_domains"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_routes"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_irt"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
    }

    @Test
    public void inet6num_sanitized() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST");

        final RpslObject object = RpslObject.parse("" +
                "inet6num:       2001:0100:0000::/24\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("2001:100::/24"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "inet6num:       2001:100::/24\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "status:         OTHER\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST\n"));
    }

    @Test
    public void inetrtr() throws Exception {
        databaseHelper.addObject("" +
                "rtr-set:      RTRS-TESTNET\n" +
                "descr:        Company\n" +
                "descr:        Router Set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST");

        final RpslObject object = RpslObject.parse("" +
                "inet-rtr:   test.ripe.net\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "member-of:  RTRS-TESTNET\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     dbtest@ripe.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inetrtr_missing_reference() {
        databaseHelper.addObject("" +
                "rtr-set:      RTRS-TESTNET\n" +
                "descr:        Company\n" +
                "descr:        Router Set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST");

        rebuild(RpslObject.parse("" +
                "inet-rtr:   test.ripe.net\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "member-of:  RTRS-TESTNET\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     dbtest@ripe.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'test.ripe.net'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("inet_rtr"), hasSize(1));
        assertThat(diff.getRemoved().getTable("ifaddr"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("member_of"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
    }

    @Test
    public void inetrtr_sanitized() {
        databaseHelper.addObject("" +
                "rtr-set:      RTRS-TESTNET\n" +
                "descr:        Company\n" +
                "descr:        Router Set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST");

        final RpslObject object = RpslObject.parse("" +
                "inet-rtr:   test.ripe.net.\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "member-of:  RTRS-TESTNET\n" +
                "org:        ORG-TOL1-TEST\n" +
                "notify:     dbtest@ripe.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(2));

        final Table inetrtrTable = diff.getModified().getTable("inet_rtr");
        assertThat(inetrtrTable, hasSize(1));
        final Row inetrtrRow = inetrtrTable.get(0);
        assertThat(inetrtrRow.getString("inet_rtr"), is("test.ripe.net"));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("test.ripe.net"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "inet-rtr:       test.ripe.net\n" +
                "descr:          description\n" +
                "local-as:       AS123\n" +
                "ifaddr:         10.0.0.1 masklen 22\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "member-of:      RTRS-TESTNET\n" +
                "org:            ORG-TOL1-TEST\n" +
                "notify:         dbtest@ripe.net\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        test@ripe.net 20120622\n" +
                "source:         TEST\n"));
    }

    @Test
    public void irt() {
        final RpslObject object = RpslObject.parse("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    test@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7.\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "notify:    nfy@test.net\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void irt_missing_reference() {
        rebuild(RpslObject.parse("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    test@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7.\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "notify:    nfy@test.net\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'irt-IRT1'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(9));
        assertThat(diff.getRemoved().getTable("irt"), hasSize(1));
        assertThat(diff.getRemoved().getTable("e_mail"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_mailbox"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("auth"), hasSize(1));
        assertThat(diff.getRemoved().getTable("irt_nfy"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void keycert() {
        final RpslObject object = RpslObject.parse("" +
                "key-cert:     PGPKEY-28F6CD6C\n" +
                "method:       PGP\n" +
                "owner:        DFN-CERT (2003), ENCRYPTION Key\n" +
                "fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "org:          ORG-TOL1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "notify:       test@ripe.net\n" +
                "changed:      test@ripe.net 20120213\n" +
                "source:       TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void keycert_missing_reference() {
        rebuild(RpslObject.parse("" +
                "key-cert:     PGPKEY-28F6CD6C\n" +
                "method:       PGP\n" +
                "owner:        DFN-CERT (2003), ENCRYPTION Key\n" +
                "fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
                "org:          ORG-TOL1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "notify:       test@ripe.net\n" +
                "changed:      test@ripe.net 20120213\n" +
                "source:       TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'PGPKEY-28F6CD6C'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(7));
        assertThat(diff.getRemoved().getTable("key_cert"), hasSize(1));
        assertThat(diff.getRemoved().getTable("fingerpr"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void mntner() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:        UPD-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "mnt-by:        UPD-MNT\n" +
                "referral-by:   UPD-MNT\n" +
                "upd-to:        dbtest@ripe.net\n" +
                "auth:          MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "org:           ORG-TOL1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-nfy:       nfy@test.net\n" +
                "notify:        notify@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "referral-by:   TST-MNT\n" +
                "changed:       dbtest@ripe.net 20120707\n" +
                "source:        TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void mntner_missing_reference() {
        rebuild(RpslObject.parse("" +
                "mntner:        UPD-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "mnt-by:        UPD-MNT\n" +
                "referral-by:   UPD-MNT\n" +
                "upd-to:        dbtest@ripe.net\n" +
                "auth:          MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "org:           ORG-TOL1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-nfy:       nfy@test.net\n" +
                "notify:        notify@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "changed:       dbtest@ripe.net 20120707\n" +
                "source:        TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'UPD-MNT'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(11));
        assertThat(diff.getRemoved().getTable("upd_to"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_mailbox"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_nfy"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mntner"), hasSize(1));
        assertThat(diff.getRemoved().getTable("auth"), hasSize(1));
        assertThat(diff.getRemoved().getTable("referral_by"), hasSize(1));
    }

    @Test
    public void organisation() {
        final RpslObject object = RpslObject.parse("" +
                "organisation:  ORG-AOL1-TEST\n" +
                "org-name:      Another Organisation Ltd\n" +
                "org-type:      other\n" +
                "descr:         test org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "abuse-c:       TP1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "ref-nfy:       rebuild@test.net\n" +
                "notify:        rebuild@test.net\n" +
                "address:       NL\n" +
                "e-mail:        testing@ripe.net\n" +
                "mnt-ref:       TST-MNT\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       testing@ripe.net 20130617\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void organisation_missing_reference() {
        rebuild(RpslObject.parse("" +
                "organisation:  ORG-AOL1-TEST\n" +
                "org-name:      Another Organisation Ltd\n" +
                "org-type:      other\n" +
                "descr:         test org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "abuse-c:       TP1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "ref-nfy:       rebuild@test.net\n" +
                "notify:        rebuild@test.net\n" +
                "address:       NL\n" +
                "e-mail:        testing@ripe.net\n" +
                "mnt-ref:       TST-MNT\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       testing@ripe.net 20130617\n" +
                "source:        TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'ORG-AOL1-TEST'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(14));
        assertThat(diff.getRemoved().getTable("mnt_ref"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_mailbox"), hasSize(1));
        assertThat(diff.getRemoved().getTable("organisation"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("ref_nfy"), hasSize(1));
        assertThat(diff.getRemoved().getTable("e_mail"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org_name"), hasSize(3));
    }

    @Test
    public void peeringset() {
        final RpslObject object = RpslObject.parse("" +
                "peering-set:   prng-partners\n" +
                "descr:         This peering contains partners\n" +
                "peering:       AS2320834 at 193.109.219.24\n" +
                "mp-peering:    AS2320834 at 193.109.219.24\n" +
                "org:           ORG-TOL1-TEST\n" +
                "notify:        notify@ripe.net\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "mnt-by:        TST-MNT\n" +
                "mnt-lower:     TST-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void peeringset_missing_references() {
        rebuild(RpslObject.parse("" +
                "peering-set:   prng-partners\n" +
                "descr:         This peering contains partners\n" +
                "peering:       AS2320834 at 193.109.219.24\n" +
                "mp-peering:    AS2320834 at 193.109.219.24\n" +
                "org:           ORG-TOL1-TEST\n" +
                "notify:        notify@ripe.net\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "mnt-by:        TST-MNT\n" +
                "mnt-lower:     TST-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'prng-partners'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(7));
        assertThat(diff.getRemoved().getTable("peering_set"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
    }

    @Test
    public void person() {
        final RpslObject object = RpslObject.parse("" +
                "person:        First Last\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "fax-no:        +31 20 123457\n" +
                "e-mail:        first@last.org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "nic-hdl:       FL1-TEST\n" +
                "remarks:       remark\n" +
                "notify:        first@last.org\n" +
                "abuse-mailbox: first@last.org\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void person_missing_reference() throws Exception {
        rebuild(RpslObject.parse("" +
                "person:        First Last\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "fax-no:        +31 20 123457\n" +
                "e-mail:        first@last.org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "nic-hdl:       FL1-TEST\n" +
                "remarks:       remark\n" +
                "notify:        first@last.org\n" +
                "abuse-mailbox: first@last.org\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org\n" +
                "source:        TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'FL1-TEST'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("names"), hasSize(2));
        assertThat(diff.getRemoved().getTable("person_role"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_mailbox"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("e_mail"), hasSize(1));
    }

    @Test
    public void person_invalid_email_attribute() throws Exception {
        final RpslObject person = databaseHelper.addObject(
                "person:       Another Person\n" +
                "address:      Amsterdam\n" +
                "phone:        +31 2 12 34 56\n" +
                "nic-hdl:      AP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      user@ripe.net 20130101\n" +
                "source:       TEST");
        whoisTemplate.update("UPDATE last SET object = ? WHERE object_id = ?",
                ("person:      Another Person\n" +
                "address:      Amsterdam\n" +
                "phone:        +31 2 12 34 56\n" +
                "nic-hdl:      AP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "e-mail:       12345678901234567890123456789012345678901234567890123456789012345678901234567890@host.org\n" +
                "changed:      user@ripe.net 20130101\n" +
                "source:       TEST").getBytes(),
                person.getObjectId());

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void poem() {
        databaseHelper.addObject(RpslObject.parse("" +
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n"));

        final RpslObject object = RpslObject.parse("" +
                "poem:            POEM-HAIKU-OBJECT\n" +
                "form:            FORM-HAIKU\n" +
                "text:            The haiku object\n" +
                "text:            Never came to life as such\n" +
                "text:            It's now generic\n" +
                "author:          TP1-TEST\n" +
                "notify:          nfy@ripe.net\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         test@ripe.net\n" +
                "source:          TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void poem_missing_references() {
        databaseHelper.addObject(RpslObject.parse("" +
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n"));

        rebuild(RpslObject.parse("" +
                "poem:            POEM-HAIKU-OBJECT\n" +
                "form:            FORM-HAIKU\n" +
                "text:            The haiku object\n" +
                "text:            Never came to life as such\n" +
                "text:            It's now generic\n" +
                "author:          TP1-TEST\n" +
                "notify:          nfy@ripe.net\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         test@ripe.net\n" +
                "source:          TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'POEM-HAIKU-OBJECT'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(5));
        assertThat(diff.getRemoved().getTable("poem"), hasSize(1));
        assertThat(diff.getRemoved().getTable("form"), hasSize(1));
        assertThat(diff.getRemoved().getTable("author"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
    }

    @Test
    public void poeticform() {
        final RpslObject object = RpslObject.parse("" +
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "notify:          nfy@ripe.net\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void poeticform_missing_references() {
        rebuild(RpslObject.parse("" +
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "notify:          nfy@ripe.net\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'FORM-HAIKU'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(4));
        assertThat(diff.getRemoved().getTable("poetic_form"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
    }

    @Test
    public void role() throws Exception {
        final RpslObject object = RpslObject.parse("" +
                "role:          Testing Role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "fax-no:        +31 20 123457\n" +
                "e-mail:        first@last.org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "nic-hdl:       TR1-TEST\n" +
                "remarks:       remark\n" +
                "notify:        first@last.org\n" +
                "abuse-mailbox: first@last.org\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void role_missing_reference() {
        rebuild(RpslObject.parse("" +
                "role:          Testing Role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "fax-no:        +31 20 123457\n" +
                "e-mail:        first@last.org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "nic-hdl:       TR1-TEST\n" +
                "remarks:       remark\n" +
                "notify:        first@last.org\n" +
                "abuse-mailbox: first@last.org\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org\n" +
                "source:        TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'TR1-TEST'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(10));
        assertThat(diff.getRemoved().getTable("names"), hasSize(2));
        assertThat(diff.getRemoved().getTable("e_mail"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("person_role"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("abuse_mailbox"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
    }

    @Test
    public void route() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        final RpslObject object = RpslObject.parse("" +
                "route:         10.1.2.0/24\n" +
                "descr:         Test route\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void route_missing_reference() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        rebuild(RpslObject.parse("" +
                "route:         10.1.2.0/24\n" +
                "descr:         Test route\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = '10.1.2.0/24AS123'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("route"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("ping_hdl"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("member_of"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_routes"), hasSize(1));
    }

    @Test
    public void route_sanitized() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        final RpslObject object = RpslObject.parse("" +
                "route:         10.01.2.0/24\n" +
                "descr:         Test route\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("10.1.2.0/24AS123"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "route:          10.1.2.0/24\n" +
                "descr:          Test route\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "ping-hdl:       TP1-TEST\n" +
                "org:            ORG-TOL1-TEST\n" +
                "member-of:      RS-BLA123\n" +
                "notify:         notify@test.net\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void route6() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        final RpslObject object = RpslObject.parse("" +
                "route6:        2001:100::/24\n" +
                "descr:         TEST\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void route6_missing_reference() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        rebuild(RpslObject.parse("" +
                "route6:        2001:100::/24\n" +
                "descr:         TEST\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = '2001:100::/24AS123'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("route6"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("ping_hdl"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("member_of"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_routes"), hasSize(1));
    }

    @Test
    public void route6_sanitized() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"));

        final RpslObject object = RpslObject.parse("" +
                "route6:        2001:0100::/24\n" +
                "descr:         TEST\n" +
                "origin:        AS123\n" +
                "mnt-by:        TST-MNT\n" +
                "ping-hdl:      TP1-TEST\n" +
                "org:           ORG-TOL1-TEST\n" +
                "member-of:     RS-BLA123\n" +
                "notify:        notify@test.net\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-routes:    TST-MNT\n" +
                "changed:       ripe@test.net 20091015\n" +
                "source:        TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("2001:100::/24AS123"));
        assertThat(new String((byte[]) lastRow.get("object")), is("" +
                "route6:         2001:100::/24\n" +
                "descr:          TEST\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "ping-hdl:       TP1-TEST\n" +
                "org:            ORG-TOL1-TEST\n" +
                "member-of:      RS-BLA123\n" +
                "notify:         notify@test.net\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void routeset() {
        final RpslObject object = RpslObject.parse("" +
                "route-set:     RS101\n" +
                "descr:         test route-set\n" +
                "members:       10.0.0.0/16\n" +
                "mp-members:    2001:100::/24\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "notify:        dbtest@ripe.net\n" +
                "mnt-by:        TST-MNT\n" +
                "mnt-lower:     TST-MNT\n" +
                "org:           ORG-TOL1-TEST\n" +
                "mbrs-by-ref:   ANY\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void routeset_missing_reference() {
        rebuild(RpslObject.parse("" +
                "route-set:     RS101\n" +
                "descr:         test route-set\n" +
                "members:       10.0.0.0/16\n" +
                "mp-members:    2001:100::/24\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "notify:        dbtest@ripe.net\n" +
                "mnt-by:        TST-MNT\n" +
                "mnt-lower:     TST-MNT\n" +
                "org:           ORG-TOL1-TEST\n" +
                "mbrs-by-ref:   ANY\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'RS101'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("route_set"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mbrs_by_ref"), hasSize(1));
    }

    @Test
    public void rtrset() {
        final RpslObject object = RpslObject.parse("" +
                "rtr-set:      AS101\n" +
                "descr:        test rtr-set\n" +
                "members:      rtr1.isp.net\n" +
                "mp-members:   2001:100::/24\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "notify:       dbtest@ripe.net\n" +
                "mbrs-by-ref:   ANY\n" +
                "org:           ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      dbtest@ripe.net 20120101\n" +
                "source:       TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void rtrset_missing_reference() {
        rebuild(RpslObject.parse("" +
                "rtr-set:       AS101\n" +
                "descr:         test rtr-set\n" +
                "members:       rtr1.isp.net\n" +
                "mp-members:    2001:100::/24\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "notify:        dbtest@ripe.net\n" +
                "mbrs-by-ref:   ANY\n" +
                "org:           ORG-TOL1-TEST\n" +
                "mnt-by:        TST-MNT\n" +
                "mnt-lower:     TST-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'AS101'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(8));
        assertThat(diff.getRemoved().getTable("rtr_set"), hasSize(1));
        assertThat(diff.getRemoved().getTable("tech_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("admin_c"), hasSize(1));
        assertThat(diff.getRemoved().getTable("notify"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mbrs_by_ref"), hasSize(1));
        assertThat(diff.getRemoved().getTable("org"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_lower"), hasSize(1));
    }

    private DatabaseDiff rebuild(final RpslObject object) {
        indexDao.rebuild();
        databaseHelper.addObject(object);
        return rebuild();
    }

    private DatabaseDiff rebuild() {
        final Database before = new Database(databaseHelper.getWhoisTemplate());
        indexDao.rebuild();
        return Database.diff(before, new Database(databaseHelper.getWhoisTemplate()));
    }
}
