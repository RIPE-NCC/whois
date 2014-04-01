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
                descr:          RIPE NCC ASN block
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
                "ASSANY-64": """\
                inet6num:     2001:601::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED ANYCAST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
                "ASSPISPON": """\
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
                """,
                "ASSANYSPON": """\
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
                """,
                "ASSPI-64SPON": """\
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
                """,
                "AS222SPON": """
                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
    ]}

    def "create inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, with RS pw"() {
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

                inet6num:     2001:601::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED ANYCAST
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
                status:         ASSIGNED
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

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 5, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Create" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Create" && it.key == "[inet6num] 2001:601::/64"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:601::/64", "inet6num", "2001:601::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "create inetnum with status ASSIGNED PI, with 2x type LIR sponsoring org, with RS pw"() {
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

    def "create inetnum with status ASSIGNED PI, with 2x same type LIR sponsoring org, with RS pw"() {
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
                sponsoring-org: ORG-LIRA-TEST
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

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum, inet6num, aut-num, with type OTHER sponsoring org, with RS pw"() {
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
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-OFA10-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-OFA10-TEST")
    }

    def "create inetnum, inet6num, aut-num, with type RIR sponsoring org, with RS pw"() {
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
                sponsoring-org: ORG-RIR-TEST
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
                sponsoring-org: ORG-RIR-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-RIR-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObjectNotFound("-r -BG -T aut-num AS222", "aut-num", "AS222")
    }

    @Ignore
    // auth issues, password for parent RS MNTNER but not this mnt-by RS MNTNER
    def "create inetnum, inet6num, aut-num, with type LIR sponsoring org, with LIR pw"() {
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObjectNotFound("-r -BG -T aut-num AS222", "aut-num", "AS222")
    }

    def "create inetnum, inet6num, aut-num, with type LIR sponsoring org, with override"() {
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
                override:     denis,override1

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
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:     denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 3)
        ack.successes.any {it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Create" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "create inetnum, inet6num, aut-num, with type OTHER sponsoring org, with override"() {
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
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:     denis,override1

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
                sponsoring-org: ORG-OFA10-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-OFA10-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:     denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 0, 3)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-OFA10-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, change to another type LIR sponsoring org, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA2-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA2-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, change to another type LIR sponsoring org, with LIR pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA2-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced sponsoring-org can only be changed by the RIPE NCC"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, remove sponsoring org, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
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

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS222"}

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, remove sponsoring org, with override"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
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
                override:     denis,override1

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
                override:     denis,override1

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
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:     denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 4)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS222"}

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add type LIR sponsoring org, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA2-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA2-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add type LIR sponsoring org, with override"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

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
                sponsoring-org: ORG-LIRA2-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:   denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-LIRA2-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:   denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 4)
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255"}
        ack.successes.any {it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS222"}

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA2-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type LIR, with LIR pw"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
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
                sponsoring-org: ORG-LIRA2-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                sponsoring-org: ORG-LIRA2-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["The sponsoring-org can only be added by the RIPE NCC"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, remove sponsoring org, with LIR pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
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

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The sponsoring-org can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The sponsoring-org can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["The sponsoring-org can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["The sponsoring-org can only be removed by the RIPE NCC"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "delete organisation referenced as sponsoring org in inetnum with status ASSIGNED PI, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        queryObject("-r -BG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
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
                delete:   testing

                password: hm
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[organisation] ORG-LIRA-TEST" }
        ack.errorMessagesFor("Delete", "[organisation] ORG-LIRA-TEST") ==
                ["Object [organisation] ORG-LIRA-TEST is referenced from other objects"]

        queryObject("-r -BG -T organisation ORG-LIRA-TEST", "organisation", "ORG-LIRA-TEST")
    }

    def "delete inetnum with status ASSIGNED PI, with type LIR sponsoring org, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")

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
                delete:   testing

                password: hm
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255"}

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with LIR pw"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                sponsoring-org: ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(8, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR",
                        "The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR",
                        "The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR",
                        "The sponsoring-org can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR",
                        "The sponsoring-org can only be added by the RIPE NCC"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                sponsoring-org: ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with override"() {
        given:
        syncUpdate(getTransient("ASSPI") + "override: denis,override1")
        syncUpdate(getTransient("ASSANY") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64") + "override: denis,override1")
        syncUpdate(getTransient("AS222") + "override: denis,override1")

        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

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
                sponsoring-org: ORG-OFA10-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:   denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                sponsoring-org: ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:   denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 4)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, change to type OTHER sponsoring org, with LIR pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-OFA10-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(8, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR",
                        "Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR",
                        "Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR",
                        "Referenced sponsoring-org can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR",
                        "Referenced sponsoring-org can only be changed by the RIPE NCC"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, change to type OTHER sponsoring org, with RS pw"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                sponsoring-org: ORG-OFA10-TEST
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
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-OFA10-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password: nccend
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, change to type OTHER sponsoring org, with override"() {
        given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSANYSPON") + "override: denis,override1")
        syncUpdate(getTransient("ASSPI-64SPON") + "override: denis,override1")
        syncUpdate(getTransient("AS222SPON") + "override: denis,override1")

        expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:    denis,override1

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:    denis,override1

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
                sponsoring-org: ORG-OFA10-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:    denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                sponsoring-org: ORG-OFA10-TEST
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:    denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 0, 4)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced object must have org-type LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced object must have org-type LIR"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "create inetnum with status ASSIGNED PA, with type LIR sponsoring org, with RS pw"() {
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
                status:       ASSIGNED PA
                sponsoring-org: ORG-LIRA-TEST
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
                ["The \"sponsoring-org:\" attribute is not allowed with this status value"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inet6num with status ASSIGNED, with type LIR sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inet6num 12001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = syncUpdate("""\
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
                status:       ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org:\" attribute is not allowed with this status value"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    @Ignore
    // requires business rule to prevent creation without sponsoring-org
    def "create inet6num with status ASSIGNED PI, without sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = syncUpdate("""\
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

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["This object must have a \"sponsoring-org:\" attribute"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    @Ignore
    // requires business rule to prevent creation without sponsoring-org
    def "create inet6num with status ASSIGNED ANYCAST, without sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = syncUpdate("""\
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
                status:       ASSIGNED ANYCAST
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["This object must have a \"sponsoring-org:\" attribute"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

}
