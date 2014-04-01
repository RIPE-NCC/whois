package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import spock.lang.Ignore

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 27/03/2014
 * Time: 14:37
 * To change this template use File | Settings | File Templates.
 */
@org.junit.experimental.categories.Category(EndToEndTest.class)
class SponsorSpec extends BaseQueryUpdateSpec  {

    @Override
    Map<String, String> getFixtures() {
        [
                "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "RIR-ALLOC-20": """\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                changed:        dbtest@ripe.net
                source:         TEST
                """,
                "ROLE-A": """\
                role:         Abuse Handler
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                abuse-mailbox:abuse@lir.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH1-TEST
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20121016
                source:       TEST
                """,
                "ORGLIR-A": """\
                organisation: ORG-LIRA-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST
                """,
                "ORG-OTH-A": """\
                organisation: ORG-OFA10-TEST
                org-type:     OTHER
                org-name:     Organisation for Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST
                """,
                "ORGRIR": """\
                organisation:    ORG-RIR-TEST
                org-type:        RIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         ripe-ncc-hm-mnt
                mnt-by:          ripe-ncc-hm-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                """,
                "ORGLIR-A2": """\
                organisation: ORG-LIRA2-TEST
                org-type:     LIR
                org-name:     Local Internet Registry Abuse
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
                "ASSPI": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSANY": """\
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSPI-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "AS222": """
                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
    ]}

    def "create inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, with RS auth"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Create" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "create inetnum with status ASSIGNED PI, with 2x type LIR sponsoring org, with RS auth"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: nccend
                password: hm
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Attribute \"sponsoring-org\" appears more than once"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    @Ignore //TODO this test case seems to be in progress
    def "delete allocation, override"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override:  denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:  test override
                override:  denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

}
