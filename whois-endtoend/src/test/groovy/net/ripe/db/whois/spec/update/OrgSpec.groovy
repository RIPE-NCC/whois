package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.experimental.categories.Category(IntegrationTest.class)
class OrgSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "RL"             : """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
                "RL-ORG"         : """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                org:     ORG-LIR2-TEST
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
                "ORG"            : """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                """,
                "ORG-NAME"       : """\
                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                """,
                "ALLOC-PA"       : """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSIGN-PA"      : """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "LEGACY"         : """\
                inetnum:      10.168.0.0 - 10.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "LEGACY-NO-ORG"  : """\
                inetnum:      10.168.0.0 - 10.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "LEGACY-OTHER"   : """\
                inetnum:      10.168.0.0 - 10.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSIGN-PI"      : """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSIGN-PI-OTHER": """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSIGN-PA-OTHER": """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "AS500"          : """\
                aut-num:     AS500
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                org:         ORG-OR1-TEST
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      LIR-MNT
                mnt-by:      RIPE-NCC-END-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
        ]
    }

    def "delete non-existent org"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                delete:  testing

                password: owner
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Delete", "[organisation] ORG-FO1-TEST") == [
                "Object [organisation] ORG-FO1-TEST does not exist in the database"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation with auto-1"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO1-TEST" }

        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation with auto-1 with existing object in DB with same name"() {
        given:
        syncUpdate(getTransient("ORG") + "password: owner2")

        expect:
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO2-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-FO2-TEST", "organisation", "ORG-FO2-TEST")
    }

    def "create organisation with auto-1 and weird (valid) chars in name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        *EXAMPLE @ "*TTTTTT & , ][
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AA1-TEST")
    }

    def "create organisation with auto-1 and weird (valid) chars plus one valid word in name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-XA1-TEST", "organisation", "ORG-XA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        XAMPLE @ "*TTTTTT & , ][
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-XA1-TEST", "organisation", "ORG-XA1-TEST")
    }

    // FIXME: [AH] the character 'Ő' was converted to a ? in the acknowledgement message
    def "create organisation with auto-1 and weird (invalid) chars in name, missing auth"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Hö öns mö åäö ŐÚ
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)

        ack.errorMessagesFor("Create", "[organisation] auto-1") == ["Syntax error in Hö öns mö åäö ?Ú"]

        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AA1-TEST")
    }

    def "create organisation disallowed org-type no power mntner"() {
        given:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        def message = send new Message(
                subject: "",
                body: sprintf("""\
                organisation:    auto-1
                org-type:        %s
                org-name:        First Org
                address:         Amsterdam
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent(), orgtype)
        )

        expect:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "Value '" + orgtype + "' can only be set by the RIPE NCC for this organisation."]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        where:
        orgtype << [
                "LIR",
                "IANA",
                "RIR",
                "WHITEPAGES",
                "DIRECT_ASSIGNMENT"
        ]
    }

    def "create organisation org-type LIR with power mntner"() {
        expect:
            queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
            def ack = syncUpdateWithResponse("""
                organisation:    auto-1
                org-type:        LIR
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-NCC-hM-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: hm
                """.stripIndent()
            )

        then:
            ack.success

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 0)

            queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type IANA with power mntner"() {
        expect:
            queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
            def ack = syncUpdateWithResponse("""
                organisation:    auto-1
                org-type:        IANA
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-NCC-hM-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: hm
                """.stripIndent()
            )

        then:
            ack.success

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 0)

            queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type RIR with power mntner"() {
        expect:
            queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
            def ack = syncUpdateWithResponse("""
                organisation:    auto-1
                org-type:        RIR
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-NCC-hM-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: hm
                """.stripIndent()
            )

        then:
            ack.success

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 0)

            queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type WHITEPAGES with power mntner"() {
        expect:
            queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
           def ack = syncUpdateWithResponse("""
                organisation:    auto-1
                org-type:        WHITEPAGES
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-NCC-hM-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: hm
                """.stripIndent()
           )

        then:
            ack.success

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 0)

            queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type DIRECT_ASSIGNMENT with power mntner"() {
        expect:
            queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
            def ack = syncUpdateWithResponse("""
                organisation:    auto-1
                org-type:        DIRECT_ASSIGNMENT
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-NCC-hM-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: hm
                """.stripIndent()
            )

        then:
            ack.success

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)

            ack.countErrorWarnInfo(0, 0, 0)

            queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation with 1 word name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")
    }

    def "create organisation with 1 long word name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First678901234567890123456789012345678901234567890123456789012345
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-1" }
        ack.errorMessagesFor("Create", "[organisation] auto-1") ==
                ["Syntax error in First678901234567890123456789012345678901234567890123456789012345"]

        queryObjectNotFound("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")
    }

    def "create organisation with syntax and auth error"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First678901234567890123456789012345678901234567890123456789012345
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-1" }
        ack.errorMessagesFor("Create", "[organisation] auto-1") ==
                ["Syntax error in First678901234567890123456789012345678901234567890123456789012345"]

        queryObjectNotFound("-r -T organisation ORG-FA1-TEST", "organisation", "ORG-FA1-TEST")
    }

    def "create organisation with 6 word name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FSTF1-TEST", "organisation", "ORG-FSTF1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First second Third Forth fifth Sixth
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-FSTF1-TEST", "organisation", "ORG-FSTF1-TEST")
    }

    def "create organisation with >30 word name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-ABCD1-TEST", "organisation", "ORG-ABCD1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        1ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 2ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 3ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 4ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 5ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 6ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 7ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 8ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 9ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 0ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 aordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 bordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 cordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 dordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 eordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 fordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 gordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 hordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 iordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 jordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 kordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 lordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 mordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 nordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 oordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 pordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 qordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 rordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 sordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 tordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 uordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-1" }
        ack.errorMessagesFor("Create", "[organisation] auto-1") =~
                ["Syntax error in 1ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword"]

        queryObjectNotFound("-r -T organisation ORG-ABCD1-TEST", "organisation", "ORG-ABCD1-TEST")
    }

    def "create organisation with same name as a person name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-TP1-TEST", "organisation", "ORG-TP1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Test Person
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-rBT person TP1-TEST", "person", "Test Person")
        queryObject("-r -T organisation ORG-TP1-TEST", "organisation", "ORG-TP1-TEST")
        def qry = query("-B -r Test Person")
        qry =~ /(?is)organisation:\s*ORG-TP1-TEST.*?person:\s*Test Person/
        def qry2 = query("-B -r -T organisation Test Person")
        qry2 =~ /(?is)organisation:\s*ORG-TP1-TEST/
        !(qry2 =~ /(?is)organisation:\s*ORG-TP1-TEST.*?person:\s*Test Person/)
    }

    def "create organisation with multiple spaces in name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First       Org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation with name having 30 words each of 64 chars"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        1ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 2ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 3ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 4ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 4ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 5ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 6ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 7ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 8ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 9ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 0ordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 aordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 bordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 cordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 dordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 eordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 fordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 gordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 hordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 iordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 jordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 kordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 lordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 mordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 nordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 oordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 pordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 rordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 sordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                                 tordwordwordwordwordwordwordwordwordwordwordwordwordwordwordword
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -T organisation ORG-ABCD1-TEST", "organisation", "ORG-ABCD1-TEST")
    }

    def "create organisation with all possible valid chars in name"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AA1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+/-
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        def qry = query("-r -T organisation ORG-AA1-TEST")
        qry.contains(/org-name:       ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+\/-/)
        def qry5 = query("-Torganisation ABZ")
        qry5.contains(/org-name:       ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+\/-/)
        def qry2 = query("-Torganisation (qwerty)")
        qry2.contains(/org-name:       ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+\/-/)
// TODO commented out until bug fixed so these queries work
//        def qry3 = query("-Torganisation @")
//        qry3.contains(/org-name:       ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+\/-/)
//        def qry4 = query("-Torganisation 0123456789")
//        qry4.contains(/org-name:       ABZ 0123456789 .  _ " * (qwerty) @, & :!'`+\/-/)
    }

    def "create organisation with valid language and geoloc"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AMH1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Aardvark Multi Hire
                address:         Burnley
                e-mail:          dbtest@ripe.net
                language:        En
                geoloc:          0 0
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         dbtest@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -GBT organisation ORG-AMH1-TEST", "organisation", "ORG-AMH1-TEST")
    }

    def "create organisation with valid language and fine grain geoloc"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AMH1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Aardvark Multi Hire
                address:         Burnley
                e-mail:          dbtest@ripe.net
                language:        En
                geoloc:          78.28 1.5755
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         dbtest@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -GBT organisation ORG-AMH1-TEST", "organisation", "ORG-AMH1-TEST")
    }

    def "create organisation with valid language and extreme low geoloc"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AMH1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Aardvark Multi Hire
                address:         Burnley
                e-mail:          dbtest@ripe.net
                language:        En
                geoloc:          -90 -180
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         dbtest@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -GBT organisation ORG-AMH1-TEST", "organisation", "ORG-AMH1-TEST")
    }

    def "create organisation with valid language and extreme high geoloc"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AMH1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        Aardvark Multi Hire
                address:         Burnley
                e-mail:          dbtest@ripe.net
                language:        En
                geoloc:          90 180
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnT
                changed:         dbtest@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -GBT organisation ORG-AMH1-TEST", "organisation", "ORG-AMH1-TEST")
    }

    def "modify organisation with valid language"() {
        expect:
        queryObject("-r -T organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                language:        eN
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -GBT organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST", "eN")
    }

    def "modify organisation with invalid language"() {
        expect:
        queryObject("-r -T organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                language:        qq
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Modify", "[organisation] ORG-OTO1-TEST") == [
                "Language not recognised: qq"]

        query_object_not_matches("-r -GBT organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST", "qq")
    }

    def "modify organisation with valid geoloc"() {
        expect:
        queryObject("-r -T organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                geoloc:          -8.632 99.5
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -GBT organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST", "eN")
    }

    def "modify organisation with invalid geoloc"() {
        expect:
        queryObject("-r -T organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-OTO1-TEST
                org-type:        other
                org-name:        Other Test org
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                geoloc:          -95 183
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)

        query_object_not_matches("-r -GBT organisation ORG-OTO1-TEST", "organisation", "ORG-OTO1-TEST", "qq")
    }

    def "create organisation with all optional attributes"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "ORG-AMH1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                admin-c:TP1-TEST
                tech-c: TP2-TEST
                ref-nfy:         ref-nfy-dbtest@ripe.net
                e-mail:          dbtest2@ripe.net
                language:EN
                org-type:        other
                org-name:        Aardvark Multi Hire
                address:         Burnley
                descr:        my old tool hire company
                descr:                 long since defunct
                fax-no:          +44282411141 ext. 2
                mnt-by:          owneR-mnT
                remarks:   twas good while it lasted
                address:         Lancs
                org:          auto-1
                admin-c:TP2-TEST
                geoloc:      10.568 158.552
                tech-c: TP2-TEST
                phone:
                ++44282411141 ext. 0
                e-mail:          dbtest@ripe.net
                notify:          nfy-dbtest@ripe.net
                abuse-mailbox:   abuse-dbtest@ripe.net
                mnt-ref:         owner3-mnt
                phone:+44282411141
                fax-no:                                     +44282411141
                mnt-by:          owner2-mnT
                remarks:extra comment
                changed:         denis@ripe.net 20121016
                changed:denis-dbtest@ripe.net
                source:          TEST
                language:          NL
                org:          ORG-OTO1-TEST
                ref-nfy:         ref-nfy2-dbtest@ripe.net
                mnt-ref:         owner3-mnt
                notify:          nfy-dbtest@ripe.net
                abuse-mailbox:   abuse2-dbtest@ripe.net

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-r -GBT organisation ORG-AMH1-TEST", "organisation", "ORG-AMH1-TEST")
    }

    def "delete self referencing org"() {
        given:
        dbfixture(getTransient("ORG-NAME"))

        expect:
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                delete:  testing

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[organisation] ORG-FO1-TEST" }

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "delete referenced org"() {
        given:
        dbfixture(getTransient("ORG-NAME"))

        expect:
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                org:     ORG-FO1-TEST
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST

                organisation:    ORG-FO1-TEST
                org-type:        other
                org-name:        First Org
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                delete:  testing

                password: owner
                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[role] FR1-TEST   First Role" }
        ack.errors.any { it.operation == "Delete" && it.key == "[organisation] ORG-FO1-TEST" }
        ack.errorMessagesFor("Delete", "[organisation] ORG-FO1-TEST") == [
                "Object [organisation] ORG-FO1-TEST is referenced from other objects"]

        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation with auto-1AbC"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObjectNotFound("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1AbC
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-ABC1-TEST" }

        queryObject("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create self referencing organisation"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO1-TEST" }

        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create 2 self referencing organisations, auto-1 auto-2"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObjectNotFound("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                organisation:    auto-2
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-2
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO1-TEST" }
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-FO2-TEST" }

        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObject("-r -T organisation ORG-FO2-TEST", "organisation", "ORG-FO2-TEST")
    }

    // TODO: [AH] this test follows what legacy whois did; we should allow for this situation in the new code (as in, this test should create 2 organisation objects successfully)
    def "create 2 mutually referencing organisations, auto-1 auto-2"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObjectNotFound("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-2
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                organisation:    auto-2
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 2, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-1" }
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-2" }
        ack.errorMessagesFor("Create", "[organisation] auto-1") ==
                ["Reference \"auto-2\" not found"]
        ack.errorMessagesFor("Create", "[organisation] auto-2") ==
                ["Reference \"auto-1\" not found"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
        queryObjectNotFound("-r -T organisation ORG-FO2-TEST", "organisation", "ORG-FO2-TEST")
    }

    def "create 2 self referencing organisations, auto-1AbC auto-1deF"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-DEF1-TEST", "organisation", "ORG-DEF1-TEST")
        queryObjectNotFound("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1AbC
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1AbC
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                organisation:    auto-1deF
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1deF
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-ABC1-TEST" }
        ack.errors.any { it.operation == "Create" && it.key == "[organisation] auto-1deF" }
        ack.errorMessagesFor("Create", "[organisation] auto-1deF") ==
                ["Key auto-1 already used (AUTO-nnn must be unique per update message)"]

        queryObject("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")
        queryObjectNotFound("-r -T organisation ORG-DEF1-TEST", "organisation", "ORG-DEF1-TEST")
    }

    def "create 2 self referencing organisations, auto-1AbC auto-2deF"() {
        expect:
        queryObjectNotFound("-r -T organisation ORG-DEF1-TEST", "organisation", "ORG-DEF1-TEST")
        queryObjectNotFound("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1AbC
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-1
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                organisation:    auto-2deF
                org-type:        other
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                org:             auto-2
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-ABC1-TEST" }
        ack.successes.any { it.operation == "Create" && it.key == "[organisation] ORG-DEF1-TEST" }

        queryObject("-r -T organisation ORG-ABC1-TEST", "organisation", "ORG-ABC1-TEST")
        queryObject("-r -T organisation ORG-DEF1-TEST", "organisation", "ORG-DEF1-TEST")
    }

    def "modify organisation, change org-type LIR to OTHER"() {
        expect:
        queryObject("-r -T organisation ORG-LIR1-TEST", "organisation", "ORG-LIR1-TEST")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    ORG-LIR1-TEST
                org-type:        OTHER
                org-name:        Local Internet Registry
                address:         RIPE NCC
                e-mail:          dbtest@ripe.net
                ref-nfy:         dbtest-org@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -GBT organisation ORG-LIR1-TEST", "organisation", "ORG-LIR1-TEST", "OTHER")
    }

    def "modify organisation, change org-type OTHER to LIR"() {
        given:
        syncUpdate(getTransient("ORG") + "password: owner2\npassword: hm")

        expect:
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = syncUpdate("""\
                organisation:    ORG-FO1-TEST
                org-type:        LIR
                org-name:        First Org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          ripe-ncc-hm-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST
                override:        denis,override1
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        query_object_matches("-r -GBT organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST", "LIR")
    }

    def "modify organisation, org-type:LIR, change org-name"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "Local Internet Registry")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     My Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 0, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-LIR2-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-LIR2-TEST") ==
                ["Organisation name can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change the name.",
                 "Authorisation for [organisation] ORG-LIR2-TEST failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT",]

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "Local Internet Registry")
    }

    def "modify organisation, remove org-name attribute"() {
        given:
        dbfixture(getTransient("ORG-NAME"))

        expect:
        queryObject("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

        when:
        def message = syncUpdate("""
                organisation:    ORG-FO1-TEST
                org-type:        other
                org:             ORG-FO1-TEST
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                changed:         denis@ripe.net 20121016
                source:          TEST

                password: owner2
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-FO1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-FO1-TEST") == ["Mandatory attribute \"org-name\" is missing"]
    }

    def "modify organisation, org-type:LIR, change org-name with override"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "Local Internet Registry")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     My Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST
                override:   denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "My Registry")
    }

    def "modify organisation, org-type:LIR, change org-name with RS password"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "Local Internet Registry")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                organisation: ORG-LIR2-TEST
                org-type:     LIR
                org-name:     My Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      SR1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       ripe-ncc-hm-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password:     hm
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "My Registry")
    }

    def "modify organisation, org-type:OTHER, change org-name with user password"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "org-type:\\s*OTHER")

        when:
        def message = syncUpdate("""
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     Changed Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password:     lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "Changed Other Registry")
    }

    def "modify organisation, org-type:OTHER, ref from PI, change org-name"() {
        given:
        databaseHelper.addObject(getTransient("ASSIGN-PI-OTHER"))

        expect:
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "ASSIGNED PI")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "Other Registry")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "org-type:\\s*OTHER")

        when:
        def message = syncUpdate("""
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     New Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OR1-TEST") ==
                ["Organisation name can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change the name."]

        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "New Other Registry")
    }

    def "modify organisation, org-type:OTHER, ref from ASSIGNED PA, change org-name"() {
        given:
        syncUpdate(getTransient("ASSIGN-PA-OTHER") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "ASSIGNED PA")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "Other Registry")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "org-type:\\s*OTHER")

        when:
        def message = syncUpdate("""
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     New Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }

        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "New Other Registry")
    }

    def "modify organisation, org-type:OTHER, ref from legacy, change org-name"() {
        given:
        syncUpdate(getTransient("LEGACY-OTHER") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 10.168.0.0 - 10.169.255.255", "inetnum", "10.168.0.0 - 10.169.255.255", "LEGACY")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "Other Registry")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "org-type:\\s*OTHER")

        when:
        def message = syncUpdate("""
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     New Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }

        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "New Other Registry")
    }

    def "modify organisation, org-type:OTHER, ref from ASN, change org-name"() {
        given:
        databaseHelper.addObject(getTransient("AS500"))

        expect:
        queryObject("-r -T aut-num AS500", "aut-num", "AS500")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "Other Registry")
        query_object_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "org-type:\\s*OTHER")

        when:
        def message = syncUpdate("""
                organisation: ORG-OR1-TEST
                org-type:     OTHER
                org-name:     New Other Registry
                address:      RIPE NCC
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                ref-nfy:      dbtest-org@ripe.net
                mnt-ref:      owner3-mnt
                mnt-by:       lir-mnt
                changed:      denis@ripe.net 20121016
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[organisation] ORG-OR1-TEST" }
        ack.errorMessagesFor("Modify", "[organisation] ORG-OR1-TEST") ==
                ["Organisation name can only be changed by the RIPE NCC for this organisation. Please contact \"ncc@ripe.net\" to change the name."]

        query_object_not_matches("-r -T organisation ORG-OR1-TEST", "organisation", "ORG-OR1-TEST", "New Other Registry")
    }

    def "modify allocation, change org:"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation can only be changed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to change this reference.",
                 "Authorisation for [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"]

        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
    }

    def "modify allocation, change org: with override"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify allocation, change org: with RS password"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     hm
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify legacy, referenced org is type LIR and also referenced in allocation, change org:"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("LEGACY") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T inetnum 10.168.0.0 - 10.169.255.255", "inetnum", "10.168.0.0 - 10.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      10.168.0.0 - 10.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T inetnum 10.168.0.0 - 10.169.255.255", "inetnum", "10.168.0.0 - 10.169.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify legacy, add org: attribute"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("LEGACY-NO-ORG") + "override: denis,override1")

        when:
        def message = syncUpdate("""
                inetnum:      10.168.0.0 - 10.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
    }

    def "modify assignment, referenced org is type LIR and also referenced in allocation, change org:"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("ASSIGN-PA") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "create assignment, referenced org is type LIR and also referenced in allocation"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        queryObjectNotFound("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify person, referenced org is type LIR and also referenced in allocation, change org:"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "override: denis,override1")
        syncUpdate(getTransient("RL-ORG") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T role FR1-TEST", "role", "First Role", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                org:     ORG-LIRA-TEST
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST

                password:     owner
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)
        ack.successes.any { it.operation == "Modify" && it.key == "[role] FR1-TEST   First Role" }

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        query_object_matches("-r -T role FR1-TEST", "role", "First Role", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify pi, change org:"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     lir
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.255.0 - 192.168.255.255") ==
                ["Referenced organisation can only be changed by the RIPE NCC for this resource. Please contact \"ncc@ripe.net\" to change this reference."]

        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIR2-TEST")
    }

    def "modify pi, change org: with override"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:   denis,override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)

        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIRA-TEST")
    }

    def "modify pi, change org: with RS pw"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "override: denis,override1")

        expect:
        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIR2-TEST")
        query_object_matches("-r -T organisation ORG-LIR2-TEST", "organisation", "ORG-LIR2-TEST", "org-type:\\s*LIR")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIRA-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password:     nccend
                password:     owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }

        query_object_matches("-r -T inetnum 192.168.255.0 - 192.168.255.255", "inetnum", "192.168.255.0 - 192.168.255.255", "org:\\s*ORG-LIRA-TEST")
    }

}
