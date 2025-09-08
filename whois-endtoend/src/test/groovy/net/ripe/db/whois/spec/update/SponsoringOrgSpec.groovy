package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse

@org.junit.jupiter.api.Tag("IntegrationTest")
class SponsoringOrgSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getFixtures() {
        [
                "ALLOC-UNS"    : """\
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
                source:       TEST
                """,
                "RIR-ALLOC-20" : """\
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
                source:       TEST
                """,
                "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
                "ROLE-A"       : """\
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
                source:       TEST
                """,
                "ORGLIR-A"     : """\
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
                source:       TEST
                """,
                "ORG-OTH-A"    : """\
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
                source:       TEST
                """,
                "ORGRIR"       : """\
                organisation:    ORG-RIR-TEST
                org-type:        RIR
                org-name:        Regional Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         ripe-ncc-hm-mnt
                mnt-by:          ripe-ncc-hm-mnt
                source:          TEST
                """,
                "ORGLIR-A2"    : """\
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
                source:       TEST
                """,
                "ASSPI"        : """\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ASSANY"       : """\
                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ASSPI-64"     : """\
                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                source:       TEST
                """,
                "ASSANY-64"    : """\
                inet6num:     2001:101::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST
                """,
                "AS333"        : """\
                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
                "ASSPISPON"   : """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ASSANYSPON"  : """\
                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "ASSPI-64SPON": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                source:       TEST
                """,
                "AS222SPON"   : """
                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST
                """,
        ]
    }

    def "create inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, with type LIR sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS222" }

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "create inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 4, 0, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["This resource object must be created with a sponsoring-org attribute"]
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.201.0 - 192.168.201.255") ==
                ["This resource object must be created with a sponsoring-org attribute"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["This resource object must be created with a sponsoring-org attribute"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["This resource object must be created with a sponsoring-org attribute"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObjectNotFound("-r -BG -T aut-num AS222", "aut-num", "AS222")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)

        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Attribute \"sponsoring-org\" appears more than once"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Attribute \"sponsoring-org\" appears more than once"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum status legacy"() {
        expect:
        queryObjectNotFound(" -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        queryObject("192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR"]

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-RIR-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-RIR-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:          ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
        queryObjectNotFound("-r -BG -T aut-num AS222", "aut-num", "AS222")
    }

    def "create inetnum, inet6num, aut-num, with type LIR sponsoring org, with LIR pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:          ORG-OFA10-TEST
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
                source:         TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS222" }

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:     denis,override1

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                source:       TEST
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST
                override:     denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 7, 3)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS222" }

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:     denis,override1

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:          ORG-OFA10-TEST
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
                source:         TEST
                override:     denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 3, 0, 0)

        ack.countErrorWarnInfo(3, 7, 3)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Create", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-OFA10-TEST")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-OFA10-TEST")
    }


    def "create inetnum with disallowed status ALLOCATED PA, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 4, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org:\" attribute is not allowed with status value \"ALLOCATED PA\""]
        ack.infoMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum with disallowed status ALLOCATED UNSPECIFIED, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 4, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org:\" attribute is not allowed with status value \"ALLOCATED UNSPECIFIED\""]
        ack.infoMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum with disallowed status LIR-PARTITIONED PA, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED",
                    "The \"sponsoring-org:\" attribute is not allowed with status value \"LIR-PARTITIONED PA\""]
        ack.infoMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum with disallowed status SUB-ALLOCATED PA, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED",
                 "The \"sponsoring-org:\" attribute is not allowed with status value \"SUB-ALLOCATED PA\""]
        ack.infoMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inetnum with disallowed status ASSIGNED PA, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override:     denis,override1

                """.stripIndent(true));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED",
                 "The \"sponsoring-org:\" attribute is not allowed with status value \"ASSIGNED PA\""]
        ack.infoMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create inet6num with disallowed statuses, with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        def message = syncUpdate(sprintf("""\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       %s%s
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST
                override:     denis,override1

                """.stripIndent(true), status, extra));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org:\" attribute is not allowed with status value \"" + status + "\""]
        ack.infoMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        where:
        status | extra
        "ALLOCATED-BY-LIR"  | ""
        "AGGREGATED-BY-LIR" | "\nassignment-size: 96"
        "ASSIGNED"          | ""
    }

    def "create inet6num with disallowed statuses (ALLOCATED-BY-RIR), with sponsoring org, with override"() {
        given:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        def message = syncUpdate(sprintf("""\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST
                override:     denis,override1

                """.stripIndent(true)));

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 4, 1)

        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org:\" attribute is not allowed with status value \"ALLOCATED-BY-RIR\""]
        ack.infoMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Authorisation override used"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "remove sponsoring-org without override without RS maintainer"() {
        given:
        syncUpdate(getTransient("AS222SPON") + "password: nccend\npassword: hm\npassword: owner3")

        def removedSponsoringOrg = new RpslObjectBuilder(getTransient("AS222SPON").trim()).removeAttributeType(AttributeType.SPONSORING_ORG).get().toString()
        def message = syncUpdate(removedSponsoringOrg +"\npassword: lir\n");

        expect:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["The \"sponsoring-org\" attribute can only be removed by the RIPE NCC"]
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:     denis,override1

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:     denis,override1

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                source:       TEST
                override:     denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:     denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 4)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }

        query_object_not_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add type LIR sponsoring org, with RS pw"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                source:       TEST

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }

        query_object_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:\\s*ORG-LIRA2-TEST")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add type LIR sponsoring org, with override"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                source:       TEST
                override:   denis,override1

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 0, 4, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 4)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }

        query_object_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:\\s*ORG-LIRA2-TEST")
        query_object_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:\\s*ORG-LIRA2-TEST")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type LIR, with LIR pw"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-LIRA2-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA2-TEST
                source:       TEST

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:100::/64") ==
                ["The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }
        ack.errorMessagesFor("Modify", "[aut-num] AS333") ==
                ["The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]

        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org\" attribute can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["The \"sponsoring-org\" attribute can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org\" attribute can only be removed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["The \"sponsoring-org\" attribute can only be removed by the RIPE NCC"]

        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64", "sponsoring-org:\\s*ORG-LIRA-TEST")
        query_object_matches("-r -BG -T aut-num AS222", "aut-num", "AS222", "sponsoring-org:\\s*ORG-LIRA-TEST")
    }

    def "modify inetnum add sponsoring-org status legacy"() {
        given:
            syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override: denis, override1""".stripIndent(true))

        expect:
            queryObject("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        update /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                password: lir
                password: nccend
                password: owner3
                password: hm""".stripIndent(true))

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
    }

    def "modify inetnum modify sponsoring-org status legacy"() {
        given:
        syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override: denis, override1""".stripIndent(true))

        expect:
        queryObject("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        update /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                password: lir
                password: owner3
                password: nccend
                password: hm""".stripIndent(true))

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
    }

    def "modify inetnum remove sponsoring-org status legacy"() {
        given:
        syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override: denis, override1""".stripIndent(true))

        expect:
        queryObject("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        update /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                password: lir
                password: nccend
                password: owner3
                password: hm""".stripIndent(true))

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
    }

    def "delete inetnum with status legacy"() {
        given:
        syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                override: denis, override1""".stripIndent(true))

        expect:
        queryObject("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIRA2-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                delete: reason
                password: lir
                password: nccend
                password: owner3
                password: hm""".stripIndent(true))

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST
                delete:   testing

                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 1, 0)
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                delete:   testing

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with LIR pw"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(8, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:100::/64") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }
        ack.errorMessagesFor("Modify", "[aut-num] AS333") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be added by the RIPE NCC"]

        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with RS pw"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:100::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }
        ack.errorMessagesFor("Modify", "[aut-num] AS333") ==
                ["Referenced organisation must have org-type: LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")
    }


    def "modify inetnum with status ASSIGNED PI and ANYCAST, inet6num with status ASSIGNED PI, aut-num, without sponsoring org, add sponsoring org with type OTHER, with override"() {
        expect:
        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.101.0 - 192.168.101.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:   denis,override1

                inet6num:     2001:100::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST
                override:   denis,override1

                aut-num:        AS333
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 4, 4)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.100.0 - 192.168.100.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.101.0 - 192.168.101.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.100.0 - 192.168.100.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:100::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:100::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS333" }
        ack.errorMessagesFor("Modify", "[aut-num] AS333") ==
                ["Referenced organisation must have org-type: LIR"]

        query_object_not_matches("-r -BG -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inetnum 192.168.101.0 - 192.168.101.255", "inetnum", "192.168.101.0 - 192.168.101.255", "sponsoring-org:")
        query_object_not_matches("-r -BG -T inet6num 2001:100::/64", "inet6num", "2001:100::/64", "sponsoring-org:")
        query_object_not_matches("-r -BG -T aut-num AS333", "aut-num", "AS333", "sponsoring-org:")
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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(8, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR",
                 "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST

                password: nccend
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR"]

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
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:    denis,override1

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                sponsoring-org: ORG-OFA10-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override:    denis,override1

                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                status:       ASSIGNED PI
                sponsoring-org: ORG-OFA10-TEST
                source:       TEST
                override:    denis,override1

                aut-num:        AS222
                as-name:        ASTEST
                descr:          description
                org:            ORG-OFA10-TEST
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
                source:         TEST
                override:    denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(4, 0, 4, 0)

        ack.countErrorWarnInfo(4, 4, 4)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/64") ==
                ["Referenced organisation must have org-type: LIR"]
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS222" }
        ack.errorMessagesFor("Modify", "[aut-num] AS222") ==
                ["Referenced organisation must have org-type: LIR"]

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
                source:       TEST

                password: nccend
                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED",
                 "The \"sponsoring-org:\" attribute is not allowed with status value \"ASSIGNED PA\""]

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
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["The \"sponsoring-org:\" attribute is not allowed with status value \"ASSIGNED\""]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create inet6num with status ASSIGNED PI, without sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["This resource object must be created with a sponsoring-org attribute"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create inet6num with status ASSIGNED ANYCAST, without sponsoring org, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["This resource object must be created with a sponsoring-org attribute"]

        queryObjectNotFound("-r -BG -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create inet6num with sponsoring-org attribute and assigned anycast status with override"() {
        expect:
            queryObjectNotFound("-r -BG -T inet6num 2001:102::/48", "inet6num", "2001:102::/48")
        when:
            def message = syncUpdate("""\
                    inet6num:       2001:102::/48
                    netname:        EU-ZZ-2001-600
                    descr:          assigned anycast
                    country:        EU
                    org:            ORG-OFA10-TEST
                    sponsoring-org: ORG-LIRA-TEST
                    admin-c:        TP1-TEST
                    tech-c:         TP1-TEST
                    mnt-by:         RIPE-NCC-END-MNT
                    mnt-by:         LIR-MNT
                    mnt-lower:      RIPE-NCC-HM-MNT
                    status:         ASSIGNED ANYCAST
                    source:         TEST
                    override:     denis,override1
                    """.stripIndent(true))
        then:
            def ack = new AckResponse("", message)

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
    }

    def "modify inet6num with assigned anycast status add sponsoring-org attribute with override"() {
        when:
            databaseHelper.addObject("""\
                    inet6num:       2001:102::/48
                    netname:        EU-ZZ-2001-600
                    descr:          assigned anycast
                    country:        EU
                    org:            ORG-OFA10-TEST
                    admin-c:        TP1-TEST
                    tech-c:         TP1-TEST
                    mnt-by:         RIPE-NCC-END-MNT
                    mnt-by:         LIR-MNT
                    mnt-lower:      RIPE-NCC-HM-MNT
                    status:         ASSIGNED ANYCAST
                    source:         TEST
                    """.stripIndent(true))
        then:
            def message = syncUpdate("""\
                    inet6num:       2001:102::/48
                    netname:        EU-ZZ-2001-600
                    descr:          assigned anycast
                    country:        EU
                    org:            ORG-OFA10-TEST
                    sponsoring-org: ORG-LIRA-TEST
                    admin-c:        TP1-TEST
                    tech-c:         TP1-TEST
                    mnt-by:         RIPE-NCC-END-MNT
                    mnt-by:         LIR-MNT
                    mnt-lower:      RIPE-NCC-HM-MNT
                    status:         ASSIGNED ANYCAST
                    source:         TEST
                    override:     denis,override1
                    """.stripIndent(true))
        then:
            def ack = new AckResponse("", message)

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
    }

    def "create inetnum with status ASSIGNED PI, with type LIR, multiple sponsoring orgs, with RS pw"() {
        expect:
        queryObjectNotFound("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: nccend
                password: hm
                password: owner3
                """.stripIndent(true)
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
    }

    def "modify inetnum with status ASSIGNED PI, with type LIR, multiple sponsoring orgs, with RS pw"() {
      given:
        syncUpdate(getTransient("ASSPISPON") + "override: denis,override1")

      expect:
        query_object_matches("-r -BG -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "sponsoring-org:\\s*ORG-LIRA-TEST")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        / 24 assigned
                country:      NL
                org:          ORG-OFA10-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                sponsoring-org: ORG-LIRA-TEST
                sponsoring-org: ORG-LIRA-TEST
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: nccend
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Attribute \"sponsoring-org\" appears more than once"]
    }

}
