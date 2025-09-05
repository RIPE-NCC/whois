package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.jupiter.api.Tag("IntegrationTest")
class Inet6numStatusChildSpec extends BaseQueryUpdateSpec {
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

    def "create an allocation with 4 assignments, start & end match"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20-2a") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2a00::/20", "inet6num", "2a00::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2a00:c90::/32", "inet6num", "2a00:c90::/32")
        queryObjectNotFound("-r -T inet6num 2a00:c90::/34", "inet6num", "2a00:c90::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:4000::/34", "inet6num", "2a00:c90:4000::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:8000::/34", "inet6num", "2a00:c90:8000::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:c000::/34", "inet6num", "2a00:c90:c000::/34")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2a00:c90::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                MNT-LOWER:    lir-mnt
                status:       ALLOCATED-BY-RIR
                source:       TEST

                inet6num:     2a00:c90::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:4000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:8000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:c000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 5, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2a00:c90::/32" }

        queryObject("-r -T inet6num 2a00:c90::/32", "inet6num", "2a00:c90::/32")
        queryObject("-r -T inet6num 2a00:c90::/34", "inet6num", "2a00:c90::/34")
        queryObject("-r -T inet6num 2a00:c90:4000::/34", "inet6num", "2a00:c90:4000::/34")
        queryObject("-r -T inet6num 2a00:c90:8000::/34", "inet6num", "2a00:c90:8000::/34")
        queryObject("-r -T inet6num 2a00:c90:c000::/34", "inet6num", "2a00:c90:c000::/34")
    }

    def "create 4 assignments then an allocation, start & end match"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20-2a") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2a00::/20", "inet6num", "2a00::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2a00:c90::/32", "inet6num", "2a00:c90::/32")
        queryObjectNotFound("-r -T inet6num 2a00:c90::/34", "inet6num", "2a00:c90::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:4000::/34", "inet6num", "2a00:c90:4000::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:8000::/34", "inet6num", "2a00:c90:8000::/34")
        queryObjectNotFound("-r -T inet6num 2a00:c90:c000::/34", "inet6num", "2a00:c90:c000::/34")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2a00:c90::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:4000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:8000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90:c000::/34
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-mnt
                status:       ASSIGNED
                source:       TEST

                inet6num:     2a00:c90::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                MNT-LOWER:    lir-mnt
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 5, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2a00:c90::/32" }

        queryObject("-r -T inet6num 2a00:c90::/32", "inet6num", "2a00:c90::/32")
        queryObject("-r -T inet6num 2a00:c90::/34", "inet6num", "2a00:c90::/34")
        queryObject("-r -T inet6num 2a00:c90:4000::/34", "inet6num", "2a00:c90:4000::/34")
        queryObject("-r -T inet6num 2a00:c90:8000::/34", "inet6num", "2a00:c90:8000::/34")
        queryObject("-r -T inet6num 2a00:c90:c000::/34", "inet6num", "2a00:c90:c000::/34")
    }

    // Create child object with status ALLOCATED-BY-RIR tests

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["inet6num parent has incorrect status: ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child ALLOCATED-BY-RIR, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-RIR, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-RIR, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-RIR, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR, no required org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
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
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-RIR, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    // Create child object with status ALLOCATED-BY-LIR tests

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child ALLOCATED-BY-LIR, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-LIR, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-LIR, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-LIR, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR, no referenced org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-LIR
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ALLOCATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    // Create child object with status AGGREGATED-BY-LIR tests

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child AGGREGATED-BY-LIR, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }

        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR, no referenced org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
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
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child AGGREGATED-BY-LIR, no assignment-size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Missing required \"assignment-size\" attribute"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, with assignment-size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" attribute only allowed with status AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child AGGREGATED-BY-LIR, assignment-size < prefix length"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/56", "inet6num", "2001:600::/56")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/56") ==
                ["\"assignment-size:\" value must be greater than prefix size 56"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/56")
    }

    def "create child AGGREGATED-BY-LIR, assignment-size = prefix length"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/48")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["\"assignment-size:\" value must be greater than prefix size 48"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/48")
    }

    @Ignore("Ref. Assignment-size can't be smaller than 48")
    def "create child AGGREGATED-BY-LIR, assignment-size = 0"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 0
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" value cannot be smaller than 48",
                 "\"assignment-size:\" value must be greater than prefix size 32"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    @Ignore("Ref. Assignment-size can't be smaller than 48")
    def "create child AGGREGATED-BY-LIR, assignment-size = 40"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

        when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 40
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" value cannot be smaller than 48"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    @Ignore("Ref. Assignment-size can't be smaller than 48")
    def "create child AGGREGATED-BY-LIR, assignment-size = 128"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 128
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" value cannot be greater than 64"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    @Ignore("Ref. Assignment-size can't be smaller than 48")
    def "create child AGGREGATED-BY-LIR, assignment-size > 128"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 130
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" value cannot be greater than 64"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child AGGREGATED-BY-LIR, assignment-size = large"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: large
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["Syntax error in large"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child AGGREGATED-BY-LIR, valid assignment-size, modify and change size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 50
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/32") ==
                ["\"assignment-size:\" value cannot be changed"]

        query_object_matches("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32", "48")
    }

    def "create child AGGREGATED-BY-LIR, then create grand-child AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create grand-child AGGREGATED-BY-LIR, then create child AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create grand-child ASSIGNED, then create child AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create grand-child ASSIGNED, then create child AGGREGATED-BY-LIR with different size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["More specific objects exist that do not match assignment-size"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create 2 grand-child ASSIGNED of different sizes, then create child AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600:1::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/64" }
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["More specific objects exist that do not match assignment-size"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObject("-rGBT inet6num 2001:600:1::/64", "inet6num", "2001:600:1::/64")
    }

    def "create child AGGREGATED-BY-LIR then create grand-child ASSIGNED with different size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["Prefix length for 2001:600::/48 must be 56"]

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR then create grand-child ASSIGNED with correct size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/56", "inet6num", "2001:600::/56")
    }

    def "create child AGGREGATED-BY-LIR then create 2 grand-child ASSIGNED with correct size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600:0:100::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:0:100::/56" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:0:100::/56", "inet6num", "2001:600:0:100::/56")
        queryObject("-rGBT inet6num 2001:600::/56", "inet6num", "2001:600::/56")
    }

    def "create child AGGREGATED-BY-LIR then create grand-child ASSIGNED & grand-child AGGREGATED-BY-LIR with correct size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600:1::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/48", "inet6num", "2001:600:1::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR then create grand-child AGGREGATED-BY-LIR & grand-child ASSIGNED with correct size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600:1::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/48", "inet6num", "2001:600:1::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR then create grand-child AGGREGATED-BY-LIR with wrong size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600:1::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/56" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600:1::/56") ==
                ["Prefix length for 2001:600:1::/56 must be 48"]

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObjectNotFound("-rGBT inet6num 2001:600:1::/56", "inet6num", "2001:600:1::/56")
    }

    def "create grand-child AGGREGATED-BY-LIR then create child AGGREGATED-BY-LIR with wrong size"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:1::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/56" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["More specific objects exist that do not match assignment-size"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/56", "inet6num", "2001:600:1::/56")
    }

    def "create child AGGREGATED-BY-LIR, then create grand-child AGGREGATED-BY-LIR, then delete child"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST
                delete:

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 2, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/32" }

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, then create grand-child AGGREGATED-BY-LIR, then create great grand-child ASSIGNED, then delete grand-child"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST
                delete:

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 3, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/48" }

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/56", "inet6num", "2001:600::/56")
    }

    def "create child AGGREGATED-BY-LIR then create 2 grand-child AGGREGATED-BY-LIR with correct prefix lengths, diff sizes"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600:1::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/48", "inet6num", "2001:600:1::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create 2 grand-child AGGREGATED-BY-LIR with same prefix lengths, diff sizes then create child AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:1::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/48" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/48", "inet6num", "2001:600:1::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child AGGREGATED-BY-LIR, create 2 grand-child AGGREGATED-BY-LIR with correct prefix lengths, diff sizes + ASSIGNED, create ASSIGNED under each grand-child"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600:1::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                inet6num:     2001:600:1:100::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600:5::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                inet6num:     2001:600:0:f::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                status:       ASSIGNED
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 6
        ack.summary.assertSuccess(6, 6, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1:100::/56" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:5::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:0:f::/64" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600:1::/48", "inet6num", "2001:600:1::/48")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObject("-rGBT inet6num 2001:600:1:100::/56", "inet6num", "2001:600:1:100::/56")
        queryObject("-rGBT inet6num 2001:600:5::/48", "inet6num", "2001:600:5::/48")
        queryObject("-rGBT inet6num 2001:600:0:f::/64", "inet6num", "2001:600:0:f::/64")
    }

    def "create 3 levels AGGREGATED-BY-LIR, adding at end"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/56") ==
                ["Only two levels of hierarchy allowed with status AGGREGATED-BY-LIR"]

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObjectNotFound("-rGBT inet6num 2001:600::/56", "inet6num", "2001:600::/56")
    }

    def "create 3 levels AGGREGATED-BY-LIR, adding at start"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 56
                source:       TEST

                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 64
                source:       TEST

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    liR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/56" }
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["Only two levels of hierarchy allowed with status AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
        queryObject("-rGBT inet6num 2001:600::/56", "inet6num", "2001:600::/56")
    }


    // Create child object with status ASSIGNED tests

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }

        queryObject("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child ASSIGNED, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }

        queryObject("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR, no referenced org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    // Create child object with status ASSIGNED PI tests

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["inet6num parent has incorrect status: ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child ASSIGNED PI, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR, no referenced org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED PI
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")

    }

    def "create child ASSIGNED PI, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    // Create child object with status ASSIGNED ANYCAST tests

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/32
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/32") ==
                ["inet6num parent has incorrect status: ALLOCATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32")
    }

    def "create child ASSIGNED ANYCAST, parent status AGGREGATED-BY-LIR"() {
      given:
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED"() {
      given:
        syncUpdate(getTransient("ASS-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ASS-ANY-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ASSPI-32") + "password: owner3\npassword: lir\npassword: hm")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/48", "inet6num", "2001:600::/48")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/48
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

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/48") ==
                ["inet6num parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/48", "inet6num", "2001:600::/48")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR, no referenced org"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ASSIGNED ANYCAST
                source:       TEST

                password: hm
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-OTO1-TEST
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR, referenced org type LIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED-BY-RIR, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: owner3\npassword: hm")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")

      expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

      when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-IANA1-TEST
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
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

}
