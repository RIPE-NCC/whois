package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest

@org.junit.experimental.categories.Category(IntegrationTest.class)
class SimpleQuerySpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        [
                "TEST-PN": """\
                person:         Test Person
                address:        St James Street
                address:        Burnley
                address:        UK
                phone:          +44 282 420469
                nic-hdl:        TP1-TEST
                mnt-by:         TST-MNT
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """,
                "TST-MNT": """\
                mntner:         TST-MNT
                descr:          MNTNER for test
                admin-c:        TP1-TEST
                upd-to:         dbtest@ripe.net
                auth:           MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                referral-by:    TST-MNT
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """,
                "ORG1": """\
                organisation:   ORG-OTO1-TEST
                org-type:       other
                org-name:       Other Test org
                address:        RIPE NCC
                e-mail:         dbtest@ripe.net
                ref-nfy:        dbtest-org@ripe.net
                mnt-ref:        TST-MNT
                mnt-by:         TST-MNT
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """,
                "DOMAIN": """\
                domain:         0.8.7.0.1.0.0.2.ip6.arpa
                descr:          Reverse delegation
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        dns.ripe.net
                notify:         notify@ripe.net
                mnt-by:         TST-MNT
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """,
                "ROUTE6": """\
                route6:         2001:780::/32
                descr:          Test
                origin:         AS123
                mnt-by:         TST-MNT
                mnt-lower:      TST-MNT
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """,
                "INET6NUM": """\
                inet6num:       2001:780::/29
                netname:        RIPE-NN
                descr:          Test
                country:        NL
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         notify@ripe.net
                mnt-by:         TST-MNT
                mnt-lower:      TST-MNT
                mnt-routes:     TST-MNT
                status:         ALLOCATED-BY-RIR
                changed:        dbtest@ripe.net 20120101
                source:         TEST
                """
        ]
    }

    def "query returns multiple types"() {
      when:
        def response = query "-rBGd 2001:780::/32"

      then:
        def expectedResult = """\
            inet6num:       2001:780::/29
            netname:        RIPE-NN
            descr:          Test
            country:        NL
            org:            ORG-OTO1-TEST
            admin-c:        TP1-TEST
            tech-c:         TP1-TEST
            notify:         notify@ripe.net
            mnt-by:         TST-MNT
            mnt-lower:      TST-MNT
            mnt-routes:     TST-MNT
            status:         ALLOCATED-BY-RIR
            changed:        dbtest@ripe.net 20120101
            source:         TEST

            route6:         2001:780::/32
            descr:          Test
            origin:         AS123
            mnt-by:         TST-MNT
            mnt-lower:      TST-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST

            domain:         0.8.7.0.1.0.0.2.ip6.arpa
            descr:          Reverse delegation
            admin-c:        TP1-TEST
            tech-c:         TP1-TEST
            zone-c:         TP1-TEST
            nserver:        dns.ripe.net
            notify:         notify@ripe.net
            mnt-by:         TST-MNT
            changed:        dbtest@ripe.net 20120101
            source:         TEST
            """.stripIndent()

        response.contains(expectedResult)
    }
}
