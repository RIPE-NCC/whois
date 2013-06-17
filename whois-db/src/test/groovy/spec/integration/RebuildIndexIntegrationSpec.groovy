package spec.integration
import net.ripe.db.whois.common.ClockDateTimeProvider
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.RpslObject
import org.springframework.beans.factory.annotation.Autowired

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoLastAndUpdateSerials

@org.junit.experimental.categories.Category(IntegrationTest.class)
class RebuildIndexIntegrationSpec extends BaseWhoisSourceSpec {
    @Autowired

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
}
