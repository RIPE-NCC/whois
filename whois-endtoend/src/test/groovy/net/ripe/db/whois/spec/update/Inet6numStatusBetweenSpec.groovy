package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.jupiter.api.Tag("IntegrationTest")
class Inet6numStatusBetweenSpec extends BaseQueryUpdateSpec {
    @Override
    Map<String, String> getTransients() {
        [
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
                source:       TEST
                """,
            "RIR-ALLOC-20-2a": """\
                inet6num:     2a00::/20
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
            "RIR-ALLOC-25": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
            "USER-RIR-ALLOC-25": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
            "RIR-ALLOC-25-NO-LOW": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
            "RIR-ALLOC-25-LOW-R-D": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST
                """,
            "LIR-ALLOC-20": """\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                """,
            "LIR-ALLOC-30": """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST
                """,
            "LIR-AGGR-32-48": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                """,
            "LIR-AGGR-32-50": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 50
                source:       TEST
                """,
            "LIR-AGGR-48-56": """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:56
                source:       TEST
                """,
            "LIR-AGGR-48-64": """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                """,
            "LIR-AGGR-56-64": """\
                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                source:       TEST
                """,
            "ASS-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                """,
            "ASS-32": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                source:       TEST
                """,
            "ASS-ANY-32": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST
                """,
            "ASSPI-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                status:       ASSIGNED PI
                source:       TEST
                """,
            "ASSPI-32": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                status:       ASSIGNED PI
                source:       TEST
                """,
            "PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """
    ]}

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ALLOCATED-BY-LIR not allowed when more specific object '2001:600::/25' has status ALLOCATED-BY-RIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  25
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '2001:600::/25' has status ALLOCATED-BY-RIR"]


        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/25' has status ALLOCATED-BY-RIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/25' has status ALLOCATED-BY-RIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-RIR, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/25' has status ALLOCATED-BY-RIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    // Create object between objects with status values ALLOCATED-BY-RIR and ALLOCATED-BY-LIR tests

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  30
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '2001:600::/30' has status ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/30' has status ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/30' has status ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ALLOCATED-BY-LIR, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/30' has status ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    // Create object between objects with status values ALLOCATED-BY-RIR and AGGREGATED-BY-LIR tests

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  48
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/48' has status AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/48' has status AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and AGGREGATED-BY-LIR, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-AGGR-48-56") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/48' has status AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    // Create object between objects with status values ALLOCATED-BY-RIR and ASSIGNED tests

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  64
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/64' has status ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/64' has status ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-64") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/64' has status ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    // Create object between objects with status values ALLOCATED-BY-RIR and ASSIGNED PI tests

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ALLOCATED-BY-LIR not allowed when more specific object '2001:600::/64' has status ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  64
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '2001:600::/64' has status ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/64' has status ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/64' has status ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED PI, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/64' has status ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    // Create object between objects with status values ALLOCATED-BY-RIR and ASSIGNED ANYCAST tests

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }

        queryObject("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ALLOCATED-BY-LIR not allowed when more specific object '2001:600::/32' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:  32
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '2001:600::/32' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status ASSIGNED"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED not allowed when more specific object '2001:600::/32' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED PI not allowed when more specific object '2001:600::/32' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

    def "create between ALLOCATED-BY-RIR and ASSIGNED ANYCAST, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/24", "inet6num", "2001:600::/24")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/24
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/24" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/24") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '2001:600::/32' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/24", "inet6num", "2001:600::/24")
    }

}
