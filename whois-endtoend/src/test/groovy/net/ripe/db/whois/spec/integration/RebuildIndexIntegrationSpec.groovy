package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.ClockDateTimeProvider
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.RpslObject

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndUpdateSerials

@org.junit.experimental.categories.Category(IntegrationTest.class)
class RebuildIndexIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "TST-MNT": """\
            mntner:  TST-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT
            referral-by: TST-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "TST-MNT2": """\
            mntner:  TST-MNT2
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT2
            referral-by: TST-MNT2
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \\\$1\\\$fU9ZMQN9\\\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "PWR-MNT": """\
            mntner:  RIPE-NCC-HM-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  RIPE-NCC-HM-MNT
            referral-by: RIPE-NCC-HM-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "ADMIN-PN": """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: TEST-RIPE
            mnt-by:  TST-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """,
                "ORG1": """\
            organisation: ORG-TOL1-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TST-MNT
            mnt-by:       TST-MNT
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            """,
                "ORG2": """\
            organisation: ORG-TOL2-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TST-MNT
            mnt-ref:      TST-MNT2
            mnt-by:       TST-MNT
            mnt-by:       TST-MNT2
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            """,
                "ABUSE-ROLE": """\
            role:    Abuse Me
            address: St James Street
            address: Burnley
            address: UK
            e-mail:  dbtest@ripe.net
            admin-c: TEST-RIPE
            tech-c:  TEST-RIPE
            nic-hdl: AB-NIC
            abuse-mailbox: abuse@test.net
            mnt-by:  TST-MNT2
            changed: dbtest@ripe.net 20121016
            source:  TEST
            """,
                "NOT-ABUSE-ROLE": """\
            role:    Not Abused
            address: St James Street
            address: Burnley
            address: UK
            e-mail:  dbtest@ripe.net
            admin-c: TEST-RIPE
            tech-c:  TEST-RIPE
            nic-hdl: NAB-NIC
            mnt-by:  TST-MNT2
            changed: dbtest@ripe.net 20121016
            source:  TEST
            """
        ]
    }

    def "rebuild indexes and query existing"() {
        when:
      whoisFixture.rebuildIndexes()

        then:
      queryObject("ORG-TOL1-TEST", "organisation", "ORG-TOL1-TEST")
      queryObject("-i mnt-by TST-MNT", "organisation", "ORG-TOL1-TEST")
    }

    def "new object in last, rebuild, query"() {
        when:
      insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
            person:  New person
            address: Address
            phone:   +44 282 411141
            nic-hdl: NP-RIPE
            mnt-by:  TST-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """.stripIndent()))

      whoisFixture.rebuildIndexes()

        then:
      queryObject("NP-RIPE", "person", "New person")
      queryObject("-i mnt-by TST-MNT", "person", "New person")
    }

    def "santitize objects and pkeys"() {
      given:
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                inetnum:    010.0.00.000 - 10.255.255.255
                netname:    RIPE-NCC
                descr:      some descr
                country:    NL
                admin-c:    TEST-RIPE
                tech-c:     TEST-RIPE
                status:     OTHER
                mnt-by:     TST-MNT
                changed:    ripe@test.net 20120505
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                inet6num:   2001:0100:0000::/24
                netname:    RIPE-NCC
                descr:      some descr
                country:    NL
                admin-c:    TEST-RIPE
                tech-c:     TEST-RIPE
                status:     OTHER
                mnt-by:     TST-MNT
                changed:    ripe@test.net 20120505
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                aut-num:    AS123
                as-name:    TST-AS
                descr:      Testing
                org:        ORG-TOL1-TEST
                mnt-by:     TST-MNT
                changed:    ripe@test.net 20091015
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                route:      10.01.2.0/24
                descr:      Test route
                origin:     AS123
                mnt-by:     TST-MNT
                changed:    ripe@test.net 20091015
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                route6:     2001:0100::/24
                descr:      TEST
                origin:     AS123
                mnt-by:     TST-MNT
                changed:    ripe@test.net 20091015
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                domain:     0.0.10.in-addr.arpa.
                descr:      Test domain
                admin-c:    TEST-RIPE
                tech-c:     TEST-RIPE
                zone-c:     TEST-RIPE
                nserver:    ns.foo.net
                nserver:    ns.bar.net
                mnt-by:     TST-MNT
                changed:    test@ripe.net 20120505
                source:     TEST
                """.stripIndent()))
          insertIntoLastAndUpdateSerials(new ClockDateTimeProvider(), whoisFixture.databaseHelper.whoisTemplate, RpslObject.parse("""\
                inet-rtr:   test.ripe.net.
                descr:      description
                local-as:   AS123
                ifaddr:     10.0.0.1 masklen 22
                admin-c:    TEST-RIPE
                tech-c:     TEST-RIPE
                mnt-by:     TST-MNT
                changed:    test@ripe.net 20120622
                source:     TEST
                """.stripIndent()))

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T inetnum 10.0.0.0 - 10.255.255.255", "inetnum", "10.0.0.0 - 10.255.255.255").contains("10.0.0.0 - 10.255.255.255")
        queryObject("-rBG -T inet6num 2001:100::/24", "inet6num", "2001:100::/24").contains("2001:100::/24")
        queryObject("-rBG -T route 10.1.2.0/24", "route", "10.1.2.0/24").contains("10.1.2.0/24")
        queryObject("-rBG -T route6 2001:100::/24", "route6", "2001:100::/24").contains("2001:100::/24")
        queryObject("-rBG -T domain 0.0.10.in-addr.arpa", "domain", "0.0.10.in-addr.arpa").contains("0.0.10.in-addr.arpa")
        queryObject("-rBG -T inet-rtr test.ripe.net", "inet-rtr", "test.ripe.net").contains("test.ripe.net")
    }

    def "rebuild with as-block"() {
        databaseHelper.addObject("" +
                "as-block:       AS222 - AS333\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TOL2-TEST\n" +
                "notify:         notify@test.net\n" +
                "changed:        chg@test.net\n" +
                "mnt-lower:      TST-MNT2\n" +
                "mnt-by:         TST-MNT\n" +
                "source:         TEST");

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T as-block AS222", "as-block", "AS222 - AS333")
        queryObject("-i org ORG-TOL2-TEST", "as-block", "AS222 - AS333")
        queryObject("-i notify notify@test.net", "as-block", "AS222 - AS333")
        queryObject("-i mnt-by TST-MNT", "as-block", "AS222 - AS333")
        queryObject("-i mnt-lower TST-MNT2", "as-block", "AS222 - AS333")
    }

    def "rebuild with autnum"() {
        databaseHelper.addObject("" +
                "aut-num:        AS101\n" +
                "as-name:        Aut-Num-1\n" +
                "descr:          description\n" +
                "org:            ORG-TOL1-TEST\n" +
                "admin-c:        NAB-NIC\n" +
                "tech-c:         NAB-NIC\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-by:         TST-MNT2\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T aut-num AS101", "aut-num", "AS101")
        queryObject("-i org ORG-TOL1-TEST", "aut-num", "AS101")
        queryObject("-i notify noreply@ripe.net", "aut-num", "AS101")
        queryObject("-i mnt-by TST-MNT2", "aut-num", "AS101")
        queryObject("-i mnt-lower TST-MNT", "aut-num", "AS101")
        queryObject("-i mnt-routes TST-MNT", "aut-num", "AS101")
        queryObject("-i admin-c NAB-NIC", "aut-num", "AS101")
        queryObject("-i tech-c NAB-NIC", "aut-num", "AS101")
    }

    def "rebuild with domain"() {
        databaseHelper.addObject("" +
                "domain:          0.0.193.in-addr.arpa\n" +
                "descr:           domain\n" +
                "org:             ORG-TOL2-TEST\n" +
                "admin-c:         NAB-NIC\n" +
                "tech-c:          TEST-RIPE\n" +
                "zone-c:          AB-NIC\n" +
                "nserver:         ns.1.net\n" +
                "mnt-by:          TST-MNT2\n" +
                "notify:          notify@ripe.net\n" +
                "ds-rdata:        52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "changed:         test@ripe.net 20120505\n" +
                "source:          TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i org ORG-TOL2-TEST", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i admin-c NAB-NIC", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i tech-c TEST-RIPE", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i zone-c AB-NIC", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i notify notify@ripe.net", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i nserver ns.1.net", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i mnt-by TST-MNT2", "domain", "0.0.193.in-addr.arpa")
        queryObject("-i ds-rdata 52151  1  1  13ee60f7499a70e5aadaf05828e7fc59e8e70bc1", "domain", "0.0.193.in-addr.arpa")
    }

    def "rebuild with filter-set"() {
        databaseHelper.addObject("" +
                "filter-set:   fltr-customers\n" +
                "descr:        This filter contains tests\n" +
                "org:          ORG-TOL2-TEST\n" +
                "admin-c:      NAB-NIC\n" +
                "tech-c:       TEST-RIPE\n" +
                "notify:       test@test.net\n" +
                "mnt-by:       TST-MNT2\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T filter-set fltr-customers", "filter-set", "fltr-customers")
        queryObject("-i org ORG-TOL2-TEST", "filter-set", "fltr-customers")
        queryObject("-i admin-c NAB-NIC", "filter-set", "fltr-customers")
        queryObject("-i tech-c TEST-RIPE", "filter-set", "fltr-customers")
        queryObject("-i notify test@test.net", "filter-set", "fltr-customers")
        queryObject("-i mnt-by TST-MNT2", "filter-set", "fltr-customers")
        queryObject("-i mnt-lower TST-MNT", "filter-set", "fltr-customers")
    }

    def "rebuild with inet-rtr"() {
        databaseHelper.addObject("" +
                "aut-num:      AS33\n" +
                "descr:        test as-set\n" +
                "admin-c:      TEST-RIPE\n" +
                "tech-c:       AB-NIC\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "rtr-set:      RTRS-TESTNET\n" +
                "descr:        Company\n" +
                "descr:        Router Set\n" +
                "tech-c:       TEST-RIPE\n" +
                "admin-c:      TEST-RIPE\n" +
                "mbrs-by-ref:  TST-MNT\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "inet-rtr:    test.net\n" +
                "descr:       test router\n" +
                "local-as:    AS33\n" +
                "ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);\n" +
                "member-of:   RTRS-TESTNET\n" +
                "org:         ORG-TOL1-TEST\n" +
                "admin-c:     TEST-RIPE\n" +
                "tech-c:      AB-NIC\n" +
                "mnt-by:      TST-MNT\n" +
                "notify:      dbtest@ripe.net\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST")
      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T inet-rtr test.net", "inet-rtr", "test.net")
        queryObject("-i local-as AS33", "inet-rtr", "test.net")
        queryObject("-i ifaddr 146.188.49.14 masklen 30 action community.append(12456:20);", "inet-rtr", "test.net")
        queryObject("-i org ORG-TOL1-TEST", "inet-rtr", "test.net")
        queryObject("-i admin-c TEST-RIPE", "inet-rtr", "test.net")
        queryObject("-i tech-c AB-NIC", "inet-rtr", "test.net")
        queryObject("-i notify dbtest@ripe.net", "inet-rtr", "test.net")
        queryObject("-i mnt-by TST-MNT", "inet-rtr", "test.net")
        queryObject("-i member-of RTRS-TESTNET", "inet-rtr", "test.net")
    }

    def "rebuild with inetnum and irt"() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL2-TEST\n" +
                "admin-c:   TEST-RIPE\n" +
                "tech-c:    TEST-RIPE\n" +
                "auth:      MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    RIPE-NCC-HM-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST")

        databaseHelper.addObject("" +
            "inetnum:       193.0.0.0 - 193.0.0.255\n" +
            "netname:       RIPE-NCC\n" +
            "descr:         description\n" +
            "country:       DK\n" +
            "admin-c:       TEST-RIPE\n" +
            "tech-c:        TEST-RIPE\n" +
            "status:        SUB-ALLOCATED PA\n" +
            "notify:        notify@test.net\n" +
            "mnt-by:        TST-MNT2\n" +
            "mnt-lower:     TST-MNT\n" +
            "mnt-domains:   TST-MNT2\n" +
            "mnt-routes:    TST-MNT\n" +
            "mnt-irt:       irt-IRT1\n" +
            "org:           ORG-TOL2-TEST\n" +
            "changed:       ripe@test.net 20120505\n" +
            "source:        TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T inetnum 193.0.0.0 - 193.0.0.255", "inetnum", "193.0.0.0 - 193.0.0.255");
        queryObject("-rBG RIPE-NCC", "inetnum", "193.0.0.0 - 193.0.0.255");
        queryObject("-i admin-c TEST-RIPE", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i tech-c TEST-RIPE", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i notify notify@test.net", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i mnt-by TST-MNT2", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i mnt-lower TST-MNT", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i mnt-domains TST-MNT2", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i mnt-routes TST-MNT", "inetnum", "193.0.0.0 - 193.0.0.255")
        queryObject("-i mnt-irt irt-IRT1", "inetnum", "193.0.0.0 - 193.0.0.255")

        queryObject("-rBG irt@ripe.net", "irt", "irt-IRT1")
        queryObject("-i abuse-mailbox abuse@ripe.net", "irt", "irt-IRT1")
        queryObject("-i admin-c TEST-RIPE", "irt", "irt-IRT1")
        queryObject("-i tech-c TEST-RIPE", "irt", "irt-IRT1")
        queryObject("-i irt-nfy irtnfy@test.net", "irt", "irt-IRT1")
        queryObject("-i notify nfy@test.net", "irt", "irt-IRT1")
        queryObject("-i mnt-by RIPE-NCC-HM-MNT", "irt", "irt-IRT1")

    }

    def "rebuild with inet6num"() {
        databaseHelper.addObject("" +
                "irt:       irt-IRT1\n" +
                "address:   Street 1\n" +
                "e-mail:    irt@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "org:       ORG-TOL2-TEST\n" +
                "admin-c:   TEST-RIPE\n" +
                "tech-c:    TEST-RIPE\n" +
                "auth:      MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "irt-nfy:   irtnfy@test.net\n" +
                "notify:    nfy@test.net\n" +
                "mnt-by:    RIPE-NCC-HM-MNT\n" +
                "changed:   test@ripe.net 20120505\n" +
                "source:    TEST")

        databaseHelper.addObject("" +
                "inet6num:      2323::/48\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         description\n" +
                "country:       DK\n" +
                "admin-c:       TEST-RIPE\n" +
                "tech-c:        TEST-RIPE\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "notify:        notify@test.net\n" +
                "mnt-by:        TST-MNT2\n" +
                "mnt-lower:     TST-MNT\n" +
                "mnt-domains:   TST-MNT2\n" +
                "mnt-routes:    TST-MNT\n" +
                "mnt-irt:       irt-IRT1\n" +
                "org:           ORG-TOL2-TEST\n" +
                "changed:       ripe@test.net 20120505\n" +
                "source:        TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T inet6num 2323::/48", "inet6num", "2323::/48");
        queryObject("-rBG RIPE-NCC", "inet6num", "2323::/48");
        queryObject("-i admin-c TEST-RIPE", "inet6num", "2323::/48")
        queryObject("-i tech-c TEST-RIPE", "inet6num", "2323::/48")
        queryObject("-i notify notify@test.net", "inet6num", "2323::/48")
        queryObject("-i mnt-by TST-MNT2", "inet6num", "2323::/48")
        queryObject("-i mnt-lower TST-MNT", "inet6num", "2323::/48")
        queryObject("-i mnt-domains TST-MNT2", "inet6num", "2323::/48")
        queryObject("-i mnt-routes TST-MNT", "inet6num", "2323::/48")
        queryObject("-i mnt-irt irt-IRT1", "inet6num", "2323::/48")
    }

    def "rebuild with keycert"() {
        databaseHelper.addObject("" +
                "key-cert:       PGPKEY-81CCF97D\n" +
                "method:         PGP\n" +
                "owner:          Unknown <unread@ripe.net>\n" +
                "fingerpr:       EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:         mQENBFC0yfkBCAC/zYZw2vDpNF2Q7bfoTeTmhEPERzUX3y1y0jJhGEdbp3re4v0i\n" +
                "certif:         XWDth4lp9Rr8RimoqQFN2JNFuUWvohiDAT91J+vAG/A67xuTWXACyAPhRtRIFhxS\n" +
                "certif:         tBu8h/qEv8yhudhjYfVHu8rUbm59BXzO80KQA4UP5fQeDVwGFbvB+73nF1Pwbg3n\n" +
                "certif:         RzgLvKZlxdgV2RdU+DvxabkHgiN0ybcJx3nntL3Do2uZEdkkDDKkN6hkUJY0cFbQ\n" +
                "certif:         Oge3AK84huZKnIFq8+NA/vsE3dg3XhbCYUlS4yMe0cvnZrH23lnu4Ubp1KBILHVW\n" +
                "certif:         K4vWnMEXcx/T2k4/vpXogZNUH6E3OjtlyjX5ABEBAAG0GVVua25vd24gPHVucmVh\n" +
                "certif:         ZEByaXBlLm5ldD6JATgEEwECACIFAlC0yfkCGwMGCwkIBwMCBhUIAgkKCwQWAgMB\n" +
                "certif:         Ah4BAheAAAoJEHbKke+BzPl9UasH/1Tc2YZiJHw3yaKvZ8jSXDmZKmO69C7YvgsX\n" +
                "certif:         B72w4K6d92vy8dLLreqEpzXKtWB1+K6bLZv6MEdNbvQReG3rw1i2Io7kdsKFn9QC\n" +
                "certif:         OeY4OwpzBMZIJGWWXxOLz9Auo9a43xU+wL92/oCqFJrLuuppgOIVkL0pBWRDQYqp\n" +
                "certif:         3MqyHdsUOEdd7pwUlGJlfLqa7wmO+r04EG1OBRLBg5p4gVARqDrVMA3ym9KF750T\n" +
                "certif:         78Il1eWrceLglI5F0h4RYEmQ3amF/ukbPyzf26+J6MnWeDSO3Q8P/aDO3L7ccNoC\n" +
                "certif:         VwyHxUumWgfQVEnt6IaKLSjxVPhhAFO0wLd2tgaUH1y/ug1RgJe5AQ0EULTJ+QEI\n" +
                "certif:         APgAjb0YCTRvIdlYFfKQfLCcIbifwFkBjaH9fN8A9ZbeXSWtO7RXEvWF70/ZX69s\n" +
                "certif:         1SfQyL4cnIUN7hEd7/Qgx63IXUfNijolbXOUkh+S41tht+4IgJ7iZsELuugvbDEb\n" +
                "certif:         VynMXFEtqCXm1zLfd0g2AsWPFRczkj7hWE0gNs7iKvEiGrjFy0eSd/q07oWLxJfq\n" +
                "certif:         n4GBBPMGkfKxWhy5AXAkPZp1mc7mlYuNP9xrn76bl69T0E69kDPS3JetSaVWj0Uh\n" +
                "certif:         NSJSjP1Zc8g+rvkeum3HKLoW0svRo2XsldjNMlSuWb/oxeaTdGZV6SxTJ+T1oHAi\n" +
                "certif:         tovyQHusvGu3D9dfvTcW3QsAEQEAAYkBHwQYAQIACQUCULTJ+QIbDAAKCRB2ypHv\n" +
                "certif:         gcz5fe7cB/9PrDR7ybLLmNAuoafsVQRevKG8DfVzDrgThgJz0jJhb1t74qy5xXn+\n" +
                "certif:         zW8d/f/JZ8jr7roWA64HKvdvo8ZXuGEf6H20p1+HbjYpT52zteNU/8ljaqIzJBes\n" +
                "certif:         tl8ecFB7qg3qUSDQseNaA1uHkZdxGybzgI69QlOyh8fRfOCh/ln9vAiL0tW+Kzjg\n" +
                "certif:         8VMY0N3HzBcAPSB7U8wDf1qMzS5Lb1yNunD0Ut5qxCq3fxcdLBk/ZagHmtXoelhH\n" +
                "certif:         Bng8TRND/cDUWWH7Rhv64NxUiaKsrM/EmrHFOpJlXuMRRx4FtRPZeXTOln7zTmIL\n" +
                "certif:         qqHWqaQHNMKDq0pf24NFrIMLc2iXCSh+\n" +
                "certif:         =FPEl\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "org:            ORG-TOL1-TEST\n" +
                "notify:         nfy@ripe.net\n" +
                "admin-c:        AB-NIC\n" +
                "tech-c:         NAB-NIC\n" +
                "mnt-by:         TST-MNT\n" +
                "notify:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120213\n" +
                "source:         TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T key-cert PGPKEY-81CCF97D", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i org ORG-TOL1-TEST", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i notify nfy@ripe.net", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i fingerpr EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i admin-c AB-NIC", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i tech-c NAB-NIC", "key-cert", "PGPKEY-81CCF97D")
        queryObject("-i mnt-by TST-MNT", "key-cert", "PGPKEY-81CCF97D")
    }

    def "rebuild with mntner"() {
        databaseHelper.addObject("" +
                "mntner:    MNT-MNT\n" +
                "descr:     description\n" +
                "org:       ORG-TOL1-TEST\n" +
                "admin-c:   TEST-RIPE\n" +
                "tech-c:    AB-NIC\n" +
                "upd-to:    upd@test.net\n" +
                "mnt-nfy:   nfy@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "notify:    notify@ripe.net\n" +
                "abuse-mailbox: abuse@ripe.net\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:    dbtest@ripe.net\n" +
                "auth:      MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:   dbtest@ripe.net 20120707\n" +
                "source:    TEST")

      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T mntner MNT-MNT", "mntner", "MNT-MNT")
        queryObject("-i org ORG-TOL1-TEST", "mntner", "MNT-MNT")
        queryObject("-i admin-c TEST-RIPE", "mntner", "MNT-MNT")
        queryObject("-i tech-c AB-NIC", "mntner", "MNT-MNT")
        queryObject("-i upd-to upd@test.net", "mntner", "MNT-MNT")
        queryObject("-i mnt-nfy nfy@test.net", "mntner", "MNT-MNT")
        queryObject("-i notify notify@ripe.net", "mntner", "MNT-MNT")
        queryObject("-i abuse-mailbox abuse@ripe.net", "mntner", "MNT-MNT")
        queryObject("-i auth MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7.", "mntner", "MNT-MNT")
    }

    def "rebuild with peering-set"() {
        databaseHelper.addObject("" +
                "peering-set:  prng-partners\n" +
                "descr:        description\n" +
                "peering:      AS4294967299 at 193.109.219.24\n" +
                "peering:      AS123 at 193.109.219.24\n" +
                "org:          ORG-TOL2-TEST\n" +
                "tech-c:       NAB-NIC\n" +
                "admin-c:      TEST-RIPE\n" +
                "notify:       notify@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT2\n" +
                "changed:      dbtest@ripe.net\n" +
                "source:       TEST")
      when:
        whoisFixture.rebuildIndexes()

      then:
        queryObject("-rBG -T peering-set prng-partners", "peering-set", "prng-partners")
        queryObject("-i org ORG-TOL2-TEST", "peering-set", "prng-partners")
        queryObject("-i admin-c TEST-RIPE", "peering-set", "prng-partners")
        queryObject("-i tech-c NAB-NIC", "peering-set", "prng-partners")
        queryObject("-i notify notify@ripe.net", "peering-set", "prng-partners")
        queryObject("-i mnt-by TST-MNT", "peering-set", "prng-partners")
        queryObject("-i mnt-lower TST-MNT2", "peering-set", "prng-partners")
    }

    def "rebuild with person"() {
      when:
        databaseHelper.addObject(RpslObject.parse("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "org:     ORG-TOL1-TEST\n" +
                "notify:  notify@test.net\n" +
                "nic-hdl: REB-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "changed: tester@test.net 20120101\n" +
                "source:  TEST\n"));

        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBG -T person Admin Person", "person", "Admin Person");
        queryObject("-rBG -T person REB-RIPE", "person", "Admin Person");
        queryObject("-i org ORG-TOL1-TEST", "person", "Admin Person")
        queryObject("-i admin-c TEST-RIPE", "person", "Admin Person")
        queryObject("-i tech-c TEST-RIPE", "person", "Admin Person")
        queryObject("-i mnt-by TST-MNT", "person", "Admin Person")
        queryObject("-i notify notify@test.net", "person", "Admin Person")
        queryObject("-i abuse-mailbox abuse@test.net", "person", "Admin Person")
    }

    def "rebuild with organisation"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation:  ORG-TOL3-TEST\n" +
                "org-name:      Test Organisation Ltd\n" +
                "org-type:      OTHER\n" +
                "org:           ORG-TOL2-TEST\n" +
                "descr:         test org\n" +
                "address:       street 5\n" +
                "e-mail:        org1@test.com\n" +
                "admin-c:       TEST-RIPE\n" +
                "tech-c:        TEST-RIPE\n" +
                "abuse-c:       AB-NIC\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "ref-nfy:       rebuild@test.net\n" +
                "notify:        rebuild@test.net\n" +
                "mnt-ref:       TST-MNT\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       dbtest@test.net 20120505\n" +
                "source:        TEST"));

      when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBGT organisation ORG-TOL3-TEST", "organisation", "ORG-TOL3-TEST")
        queryObject("-i mnt-by TST-MNT", "organisation", "ORG-TOL3-TEST")
        queryObject("-i mnt-ref TST-MNT", "organisation", "ORG-TOL3-TEST")
        queryObject("-i org ORG-TOL2-TEST", "organisation", "ORG-TOL3-TEST")
        queryObject("-i ref-nfy rebuild@test.net", "organisation", "ORG-TOL3-TEST")
        queryObject("-i abuse-c AB-NIC", "organisation", "ORG-TOL3-TEST")
        queryObject("-i abuse-mailbox abuse@test.net", "organisation", "ORG-TOL3-TEST")
        queryObject("-i notify rebuild@test.net", "organisation", "ORG-TOL3-TEST")
        queryObject("-i tech-c TEST-RIPE", "organisation", "ORG-TOL3-TEST")
        queryObject("-i admin-c TEST-RIPE", "organisation", "ORG-TOL3-TEST")
    }

    def "rebuild with poem and poetic-form"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "poetic-form:     FORM-HAIKU\n" +
                "descr:           haiku\n" +
                "admin-c:         TEST-RIPE\n" +
                "notify:          pform@test.net\n" +
                "mnt-by:          TST-MNT\n" +
                "changed:         ripe-dbm@test.net 20060913\n" +
                "source:          TEST"));

        databaseHelper.addObject(RpslObject.parse("" +
                "poem:      poem-plath\n" +
                "descr:     Nonpoetic description\n" +
                "form:      form-haiku\n" +
                "text:      poem text\n" +
                "author:    TEST-RIPE\n" +
                "remarks:   blabla\n" +
                "notify:    rebuild@test.net\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   dbtest@test.net 20120505\n" +
                "source:    TEST"))

      when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBGT poem poem-plath ", "poem", "poem-plath");
        queryObject("-i form form-haiku", "poem", "poem-plath")
        queryObject("-i author TEST-RIPE", "poem", "poem-plath")
        queryObject("-i notify rebuild@test.net", "poem", "poem-plath")
        queryObject("-i mnt-by TST-MNT", "poem", "poem-plath")

        queryObject("-i notify pform@test.net", "poetic-form", "FORM-HAIKU")
        queryObject("-i mnt-by TST-MNT", "poetic-form", "FORM-HAIKU")
        queryObject("-i admin-c TEST-RIPE", "poetic-form", "FORM-HAIKU")
    }

    def "rebuild with role"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "role:          Admin Role\n" +
                "address:       Admin Road\n" +
                "phone:         +44 282 411141\n" +
                "e-mail:        role@test.net\n" +
                "admin-c:       TEST-RIPE\n" +
                "tech-c:        TEST-RIPE\n" +
                "org:           ORG-TOL1-TEST\n" +
                "nic-hdl:       ROLE-RIPE\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "notify:        notify@test.net\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       tester@test.net 20120101\n" +
                "source:        TEST\n"))

      when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBG -T role ROLE-RIPE", "role", "Admin Role");
        queryObject("-i org ORG-TOL1-TEST", "role", "Admin Role")
        queryObject("-i admin-c TEST-RIPE", "role", "Admin Role")
        queryObject("-i tech-c TEST-RIPE", "role", "Admin Role")
        queryObject("-i mnt-by TST-MNT", "role", "Admin Role")
        queryObject("-i notify notify@test.net", "role", "Admin Role")
        queryObject("-i abuse-mailbox abuse@test.net", "role", "Admin Role")
    }

    def "rebuild with route-set"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-CUSTOMERS\n" +
                "descr:        test route-set\n" +
                "mbrs-by-ref:  ANY\n" +
                "org:          ORG-TOL1-TEST\n" +
                "tech-c:       TEST-RIPE\n" +
                "admin-c:      TEST-RIPE\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "notify:       notify@test.net\n" +
                "changed:      changed@test.net\n" +
                "source:       TEST"))
      when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBG -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS");
        queryObject("-i org ORG-TOL1-TEST", "route-set", "RS-CUSTOMERS")
        queryObject("-i admin-c TEST-RIPE", "route-set", "RS-CUSTOMERS")
        queryObject("-i tech-c TEST-RIPE", "route-set", "RS-CUSTOMERS")
        queryObject("-i mnt-by TST-MNT", "route-set", "RS-CUSTOMERS")
        queryObject("-i mnt-lower TST-MNT", "route-set", "RS-CUSTOMERS")
        queryObject("-i mbrs-by-ref ANY", "route-set", "RS-CUSTOMERS")
        queryObject("-i notify notify@test.net", "route-set", "RS-CUSTOMERS")
    }

    def "rebuilding with route and as-set"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set: RS-BLA123\n" +
                "descr: route set description\n" +
                "tech-c: TEST-RIPE\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: TST-MNT\n" +
                "mbrs-by-ref: ANY\n" +
                "changed: ripe@test.net 20120202\n" +
                "source: TEST"))

        databaseHelper.addObject(RpslObject.parse("" +
                "as-set:       AS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT2\n" +
                "admin-c:      TEST-RIPE\n" +
                "tech-c:       TEST-RIPE\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       asnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"))

        databaseHelper.addObject(RpslObject.parse("" +
                "route:        198.0/32\n" +
                "descr:        test route\n" +
                "origin:       AS1234\n" +
                "ping-hdl:     TEST-RIPE\n" +
                "org:          ORG-TOL1-TEST\n" +
                "member-of:    RS-BLA123\n" +
                "notify:       notify@test.net\n" +
                "mnt-lower:    TST-MNT\n" +
                "mnt-routes:   TST-MNT2\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      changed@test.net\n" +
                "source:       TEST\n"))

        when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBG -T route 198.0/32", "route", "198.0.0.0/32");
        queryObject("-i org ORG-TOL1-TEST", "route", "198.0.0.0/32")
        queryObject("-i member-of RS-BLA123", "route", "198.0.0.0/32")
        queryObject("-i ping-hdl TEST-RIPE", "route", "198.0.0.0/32")
        queryObject("-i mnt-by TST-MNT", "route", "198.0.0.0/32")
        queryObject("-i mnt-routes TST-MNT2", "route", "198.0.0.0/32")
        queryObject("-i mnt-lower TST-MNT", "route", "198.0.0.0/32")
        queryObject("-i notify notify@test.net", "route", "198.0.0.0/32")

        queryObject("-rBG -T as-set AS-BLA123", "as-set", "AS-BLA123");
        queryObject("-i org ORG-TOL1-TEST", "as-set", "AS-BLA123")
        queryObject("-i mnt-by TST-MNT", "as-set", "AS-BLA123")
        queryObject("-i mnt-lower TST-MNT2", "as-set", "AS-BLA123")
        queryObject("-i notify asnotify@test.net", "as-set", "AS-BLA123")
        queryObject("-i mbrs-by-ref ANY", "as-set", "AS-BLA123")
    }

    def "rebuilding with route6 and route-set"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "route-set:    RS-BLA123\n" +
                "org:          ORG-TOL1-TEST\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-lower:    TST-MNT2\n" +
                "admin-c:      TEST-RIPE\n" +
                "tech-c:       TEST-RIPE\n" +
                "mbrs-by-ref:  ANY\n" +
                "notify:       rsnotify@test.net\n" +
                "changed:      changed@test.com\n" +
                "source:       TEST"))

        databaseHelper.addObject(RpslObject.parse("" +
                "route6:       2222:3333::/48\n" +
                "descr:        test route6\n" +
                "origin:       AS1234\n" +
                "ping-hdl:     TEST-RIPE\n" +
                "org:          ORG-TOL1-TEST\n" +
                "member-of:    RS-BLA123\n" +
                "notify:       notify@test.net\n" +
                "mnt-lower:    TST-MNT\n" +
                "mnt-routes:   TST-MNT2\n" +
                "mnt-by:       TST-MNT\n" +
                "changed:      changed@test.net\n" +
                "source:       TEST\n"))

      when:
          whoisFixture.rebuildIndexes();

      then:
          queryObject("-rBG -T route6 2222:3333::/48", "route6", "2222:3333::/48");
          queryObject("-i org ORG-TOL1-TEST", "route6", "2222:3333::/48")
          queryObject("-i member-of RS-BLA123", "route6", "2222:3333::/48")
          queryObject("-i ping-hdl TEST-RIPE", "route6", "2222:3333::/48")
          queryObject("-i mnt-by TST-MNT", "route6", "2222:3333::/48")
          queryObject("-i mnt-routes TST-MNT2", "route6", "2222:3333::/48")
          queryObject("-i mnt-lower TST-MNT", "route6", "2222:3333::/48")
          queryObject("-i notify notify@test.net", "route6", "2222:3333::/48")

          queryObject("-rBG -T route-set RS-BLA123", "route-set", "RS-BLA123");
          queryObject("-i org ORG-TOL1-TEST", "route-set", "RS-BLA123")
          queryObject("-i mnt-by TST-MNT", "route-set", "RS-BLA123")
          queryObject("-i mnt-lower TST-MNT2", "route-set", "RS-BLA123")
          queryObject("-i notify rsnotify@test.net", "route-set", "RS-BLA123")
          queryObject("-i mbrs-by-ref ANY", "route-set", "RS-BLA123")
    }

    def "rebuild with rtr-set"() {
        databaseHelper.addObject(RpslObject.parse("" +
                "rtr-set:       RTRS-FOO\n" +
                "descr:         description\n" +
                "mbrs-by-ref:   ANY\n" +
                "remarks:       remarks\n" +
                "org:           ORG-TOL1-TEST\n" +
                "tech-c:        TEST-RIPE\n" +
                "admin-c:       NAB-NIC\n" +
                "notify:        notf@test.net\n" +
                "mnt-by:        TST-MNT2\n" +
                "mnt-lower:     TST-MNT\n" +
                "changed:       chg@test.net\n" +
                "source:        TEST"))

      when:
        whoisFixture.rebuildIndexes();

      then:
        queryObject("-rBG -T rtr-set rtrs-foo", "rtr-set", "RTRS-FOO");
        queryObject("-i org ORG-TOL1-TEST", "rtr-set", "RTRS-FOO")
        queryObject("-i mnt-by TST-MNT2", "rtr-set", "RTRS-FOO")
        queryObject("-i mnt-lower TST-MNT", "rtr-set", "RTRS-FOO")
        queryObject("-i notify notf@test.net", "rtr-set", "RTRS-FOO")
        queryObject("-i mbrs-by-ref ANY", "rtr-set", "RTRS-FOO")
        queryObject("-i tech-c TEST-RIPE", "rtr-set", "RTRS-FOO")
        queryObject("-i admin-c NAB-NIC", "rtr-set", "RTRS-FOO")
    }
}
