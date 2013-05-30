package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import net.ripe.db.whois.update.domain.Ack
import spec.domain.AckResponse
import spec.domain.Message

class OrgSpec extends BaseSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "RL": """\
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
                "ORG": """\
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
                "ORG-NAME": """\
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
                """
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

    def "create organisation org-type LIR no power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        LIR
                org-name:        First Org
                address:         Amsterdam
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
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "This org-type value can only be set by administrative mntners"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type IANA no power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        IANA
                org-name:        First Org
                address:         Amsterdam
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
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "This org-type value can only be set by administrative mntners"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type RIR no power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        RIR
                org-name:        First Org
                address:         Amsterdam
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
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "This org-type value can only be set by administrative mntners"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type WHITEPAGES no power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        WHITEPAGES
                org-name:        First Org
                address:         Amsterdam
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
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "This org-type value can only be set by administrative mntners"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type DIRECT_ASSIGNMENT no power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        DIRECT_ASSIGNMENT
                org-name:        First Org
                address:         Amsterdam
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
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errorMessagesFor("Create", "[organisation] auto-1") == [
                "This org-type value can only be set by administrative mntners"]

        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")
    }

    def "create organisation org-type LIR with power mntner"() {
      expect:
        queryObjectNotFound("-r -T organisation ORG-FO1-TEST", "organisation", "ORG-FO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
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
        def ack = ackFor message
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
        def message = send new Message(
                subject: "",
                body: """\
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
        def ack = ackFor message
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
        def message = send new Message(
                subject: "",
                body: """\
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
        def ack = ackFor message
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
        def message = send new Message(
                subject: "",
                body: """\
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
        def ack = ackFor message
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
        def message = send new Message(
                subject: "",
                body: """\
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
        def ack = ackFor message
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
        queryObjectNotFound("-r -T organisation ORG-AA1-TEST", "organisation", "A-Z 0-9 .  _ \" * ()@, & :!'`+/-")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                organisation:    auto-1
                org-type:        other
                org-name:        A-Z 0-9 .  _ " * ()@, & :!'`+/-
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
        qry.contains(/org-name:       A-Z 0-9 .  _ " * ()@, & :!'`+\/-/)
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
                override:        override1
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

}
