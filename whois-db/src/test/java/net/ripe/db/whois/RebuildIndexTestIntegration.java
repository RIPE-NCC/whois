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
    public void asblock() {
        final RpslObject object = RpslObject.parse(
                "as-block:       AS1 - AS99\n" +
                "descr:          Test ASN blocks\n" +
                "remarks:        Testing\n" +
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
    public void asset() {
        final RpslObject object = RpslObject.parse(
                "as-set:       AS-TEST\n" +
                "descr:        test as-set\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      test@ripe.net 20120101\n" +
                "source:       TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void autnum() {
        final RpslObject object = RpslObject.parse(
                "aut-num:        AS101\n" +
                "as-name:        some-name\n" +
                "descr:          description\n" +
                "org:            ORG-TOL1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "changed:        test@ripe.net 20120101\n" +
                "source:         TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void domain() {
        final RpslObject object = RpslObject.parse(
                "domain:     0.0.10.in-addr.arpa\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120505\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void domain_sanitized() {
        final RpslObject object = RpslObject.parse(
                "domain:     0.0.10.in-addr.arpa.\n" +
                "descr:      Test domain\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "zone-c:     TP1-TEST\n" +
                "nserver:    ns.foo.net\n" +
                "nserver:    ns.bar.net\n" +
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
        assertThat(new String((byte[])lastRow.get("object")), is(
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
    public void filterset() {
        final RpslObject object = RpslObject.parse(
                "filter-set:   fltr-test\n" +
                "descr:        test filter\n" +
                "filter:       AS101\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      dbtest@ripe.net 20120101\n" +
                "source:  TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inetnum() {
        final RpslObject object = RpslObject.parse(
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inetnum_sanitized() {
        final RpslObject object = RpslObject.parse(
                "inetnum:        010.0.00.000 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
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
        assertThat(new String((byte[])lastRow.get("object")), is(
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
    public void inet6num() {
        final RpslObject object = RpslObject.parse(
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20120505\n" +
                "source:         TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inet6num_sanitized() {
        final RpslObject object = RpslObject.parse(
                "inet6num:       2001:0100:0000::/24\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
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
        assertThat(new String((byte[])lastRow.get("object")), is(
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
    public void inetrtr() throws Exception {
        final RpslObject object = RpslObject.parse(
                "inet-rtr:   test.ripe.net\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    test@ripe.net 20120622\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void inetrtr_sanitized() {
        final RpslObject object = RpslObject.parse(
                "inet-rtr:   test.ripe.net.\n" +
                "descr:      description\n" +
                "local-as:   AS123\n" +
                "ifaddr:     10.0.0.1 masklen 22\n" +
                "admin-c:    TP1-TEST\n" +
                "tech-c:     TP1-TEST\n" +
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
        assertThat(new String((byte[])lastRow.get("object")), is(
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
    public void irt() {
        final RpslObject object = RpslObject.parse(
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    test@ripe.net\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7.\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void keycert() {
        final RpslObject object = RpslObject.parse(
                "key-cert:     PGPKEY-28F6CD6C\n" +
                "method:       PGP\n" +
                "owner:        DFN-CERT (2003), ENCRYPTION Key\n" +
                "fingerpr:     1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:       -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:       -----END PGP PUBLIC KEY BLOCK-----\n" +
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
    public void mntner() {
        final RpslObject object = RpslObject.parse(
                "mntner:        UPD-MNT\n" +
                "descr:         description\n" +
                "admin-c:       TP1-TEST\n" +
                "mnt-by:        UPD-MNT\n" +
                "referral-by:   UPD-MNT\n" +
                "upd-to:        dbtest@ripe.net\n" +
                "auth:          MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:       dbtest@ripe.net 20120707\n" +
                "source:        TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void organisation() {
        final RpslObject object = RpslObject.parse(
                "organisation:  ORG-AOL1-TEST\n" +
                "org-name:      Another Organisation Ltd\n" +
                "org-type:      other\n" +
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
    public void peeringset() {
        final RpslObject object = RpslObject.parse(
                "peering-set:   prng-partners\n" +
                "descr:         This peering contains partners\n" +
                "peering:       AS2320834 at 193.109.219.24\n" +
                "mp-peering:    AS2320834 at 193.109.219.24\n" +
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
    public void person() {
        final RpslObject object = RpslObject.parse(
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
        rebuild(RpslObject.parse(
                "person:        First Last\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "nic-hdl:       FL1-TEST\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org\n" +
                "source:        TEST"));

        whoisTemplate.update("DELETE FROM last WHERE pkey = 'FL1-TEST'");

        final DatabaseDiff diff = rebuild();

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(4));
        assertThat(diff.getRemoved().getTable("names"), hasSize(2));
        assertThat(diff.getRemoved().getTable("person_role"), hasSize(1));
        assertThat(diff.getRemoved().getTable("mnt_by"), hasSize(1));
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
        databaseHelper.addObject(RpslObject.parse(
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n"));
        final RpslObject object = RpslObject.parse(
                "poem:            POEM-HAIKU-OBJECT\n" +
                "form:            FORM-HAIKU\n" +
                "text:            The haiku object\n" +
                "text:            Never came to life as such\n" +
                "text:            It's now generic\n" +
                "author:          TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         test@ripe.net\n" +
                "source:          TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void poeticform() {
        final RpslObject object = RpslObject.parse(
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TP1-TEST\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         ripe-dbm@ripe.net 20060913\n" +
                "source:          TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void role() throws Exception {
        final RpslObject object = RpslObject.parse(
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
    public void route() {
        final RpslObject object = RpslObject.parse(
                "route:      10.1.2.0/24\n" +
                "descr:      Test route\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void route_sanitized() {
        final RpslObject object = RpslObject.parse(
                "route:      10.01.2.0/24\n" +
                        "descr:      Test route\n" +
                        "origin:     AS123\n" +
                        "mnt-by:     TST-MNT\n" +
                        "changed:    ripe@test.net 20091015\n" +
                        "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("10.1.2.0/24AS123"));
        assertThat(new String((byte[])lastRow.get("object")), is(
                "route:          10.1.2.0/24\n" +
                "descr:          Test route\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void route6() {
        final RpslObject object = RpslObject.parse(
                "route6:     2001:100::/24\n" +
                "descr:      TEST\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
    }

    @Test
    public void route6_sanitized() {
        final RpslObject object = RpslObject.parse(
                "route6:     2001:0100::/24\n" +
                "descr:      TEST\n" +
                "origin:     AS123\n" +
                "mnt-by:     TST-MNT\n" +
                "changed:    ripe@test.net 20091015\n" +
                "source:     TEST");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(1));

        final Table lastTable = diff.getModified().getTable("last");
        assertThat(lastTable, hasSize(1));
        final Row lastRow = lastTable.get(0);
        assertThat(lastRow.getString("pkey"), is("2001:100::/24AS123"));
        assertThat(new String((byte[])lastRow.get("object")), is(
                "route6:         2001:100::/24\n" +
                "descr:          TEST\n" +
                "origin:         AS123\n" +
                "mnt-by:         TST-MNT\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"));
    }

    @Test
    public void routeset() {
        final RpslObject object = RpslObject.parse(
                "route-set:     AS101\n" +
                "descr:         test route-set\n" +
                "members:       10.0.0.0/16\n" +
                "mp-members:    2001:100::/24\n" +
                "tech-c:        TP1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "notify:        dbtest@ripe.net\n" +
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
    public void rtrset() {
        final RpslObject object = RpslObject.parse(
                "rtr-set:      AS101\n" +
                "descr:        test rtr-set\n" +
                "members:      rtr1.isp.net\n" +
                "mp-members:   2001:100::/24\n" +
                "tech-c:       TP1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "notify:       dbtest@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      dbtest@ripe.net 20120101\n" +
                "source:       TEST\n");

        final DatabaseDiff diff = rebuild(object);

        assertThat(diff.getAdded().getAll(), hasSize(0));
        assertThat(diff.getRemoved().getAll(), hasSize(0));
        assertThat(diff.getModified().getAll(), hasSize(0));
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
