package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

import java.time.LocalDateTime

@org.junit.jupiter.api.Tag("IntegrationTest")
class AuthSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "PN-SYN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "PN-ORG": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "ORG": """\
                organisation:    auto-1TO
                org-type:        other
                org-name:        Test org
                address:         RIPE NCC
                                 Singel 258
                                 1016 AB Amsterdam
                                 Netherlands
                e-mail:          dbtest@ripe.net
                mnt-ref:         owner3-mnt
                mnt-by:          owner2-mnt
                source:  TEST
                """,
            "P-LOW": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                """
    ]}

    def setupSpec() {
        resetTime()
    }

    def "create person 2 mnt-by 1 correct password"() {
      expect:
        queryObjectNotFound("-r -T person FP1-RIPE", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-ripE
                mnt-by:  owner-mnt
                mnt-by:  owner2-mnt
                source:  TEST

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-ripE   First Person" }

        queryObject("-rBT person FP1-ripE", "person", "First Person")
    }

    def "create person 4 mnt-by 2 correct password 2 invalid password"() {
      expect:
        queryObjectNotFound("-r -T person FP1-RIPE", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-RIPE
                mnt-by:  owner-mnt
                mnt-by:  owner2-mnt
                mnt-by:  owner3-mnt
                mnt-by:  owner4-mnt
                source:  TEST

                password: fred
                password: owner2
                password: bill
                password: owner4
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-RIPE   First Person" }

        queryObject("-rBT person FP1-RIPE", "person", "First Person")
    }

    def "create person with password hash"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "modify person change mnt-by using old object password"() {
      given:
        syncUpdate(getTransient("PN") + "password: owner")

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "mnt-by:\\s*owner-mnt")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner2-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person" }

        query_object_matches("-rBGT person FP1-TEST", "person", "First Person", "mnt-by:\\s*owner2-mnt")
    }

    def "modify person change mnt-by using new object password"() {
      given:
        syncUpdate(getTransient("PN") + "password: owner")

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "mnt-by:\\s*owner-mnt")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner2-mnt
                source:  TEST

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Modify", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        query_object_matches("-rBGT person FP1-TEST", "person", "First Person", "mnt-by:\\s*owner-mnt")
        query_object_not_matches("-rBGT person FP1-TEST", "person", "First Person", "mnt-by:\\s*owner2-mnt")
    }

    def "create person 2 mnt-by list 1 correct password for second MNTNER"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt, owner2-mnt
                source:  TEST

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person 2 mnt-by list no correct password"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt, owner2-mnt
                source:  TEST

                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT, OWNER2-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person 2 mnt-by list 1 correct password 1 non existent MNTNER"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt, non-exist-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Unknown object referenced non-exist-mnt",
                "The maintainer 'non-exist-mnt' was not found in the database"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person 2 mnt-by list no correct password 1 non existent MNTNER"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                mnt-by:  non-exist-mnt
                source:  TEST

                password: fred
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(3, 1, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Unknown object referenced non-exist-mnt",
                "Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT",
                "The maintainer 'non-exist-mnt' was not found in the database"];

        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "delete person 1 mnt-by correct password syntax error"() {
      given:
        dbfixture(getTransient("PN-SYN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                delete:  testing
                address: St James Street
                address: Burnley
                address: UK
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION all passwords supplied"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION all same passwords supplied"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner3-mnt
                source:  TEST

                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION no auth for org"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "organisation", "ORG-TO1-TEST", "mnt-ref", "OWNER3-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION no auth for org or mnt-by"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "organisation", "ORG-TO1-TEST", "mnt-ref", "OWNER3-MNT")
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION no auth for mnt-by"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST
                password: owner3

                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with reference to ORGANISATION using org mnt-by as auth for org"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")

      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.authFailCheck("Create", "FAILED", "person", "FP1-TEST   First Person", "", "organisation", "ORG-TO1-TEST", "mnt-ref", "OWNER3-MNT")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "modify person adding reference to ORGANISATION all passwords supplied"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")
        syncUpdate(getTransient("PN") + "password: owner")

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person" }

        query_object_matches("-rBT person FP1-TEST", "person", "First Person", "org:\\s*ORG-TO1-TEST")
    }

    def "modify person with existing reference to ORGANISATION obj password supplied"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")
        syncUpdate(getTransient("PN-ORG") + "password: owner\npassword: owner3")

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-TO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                remarks: updated
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person" }

        query_object_matches("-rBT person FP1-TEST", "person", "First Person", "org:\\s*ORG-TO1-TEST")
    }

    def "delete person with existing reference to ORGANISATION obj password supplied"() {
      given:
        syncUpdate(getTransient("ORG") + "password: owner2")
        syncUpdate(getTransient("PN-ORG") + "password: owner\npassword: owner3")

      expect:
        query_object_matches("-r -T person FP1-TEST", "person", "First Person", "org:\\s*ORG-TO1-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                org:     ORG-TO1-TEST
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                delete:  not needed

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person using pgp signed message"() {
      given:
        setTime(LocalDateTime.parse("2015-05-21T15:48:04")) // current time must be within 1 hour of signing time
        syncUpdate(
                "key-cert:       PGPKEY-AAAAAAAA\n" +       // primary key doesn't match public key id
                "method:         PGP\n" +
                "owner:          noreply@ripe.net <noreply@ripe.net>\n" +
                "fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:         \n" +
                "certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                "certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                "certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                "certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                "certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                "certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                "certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                "certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                "certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                "certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                "certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                "certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                "certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                "certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                "certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                "certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                "certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                "certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                "certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                "certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                "certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                "certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                "certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                "certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                "certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                "certif:         =HQmg\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n" +
                "password:       owner")

        syncUpdate(
                oneBasicFixture("OWNER-MNT").
                        replaceAll("source:\\s*TEST", "auth: PGPKEY-AAAAAAAA\nsource: TEST") + "password: owner")

      expect:
        query_object_matches("-r -T mntner OWNER-MNT", "mntner", "OWNER-MNT", "auth:\\s*PGPKEY-AAAAAAAA")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJVXeIUAAoJELvMuy1XY5UNU9IH/0ppILbAHBJPJ4XakUDWCEqp
                2ZfQidOTkmcbQFSHwxevZpTRSAQ8S8JciyOSj5h6CfNkBelf9qUpLZU/B6YPKBti
                b9EGHadc2PuS/UhmpgaU7d6r0QQyAKZtmLa2Q/TXgrxzW0m3N7ZfTesU6dZFWSBG
                j3OEDI13fQ7qWZqt0WHrOCybW740izn8fJUpbumrEpikkA+MaDxONLLxnGhVS0Ax
                vT6lb9BY578sarzspGLNtgjhRF3b1cS+TfeRu1aPaQt6/06V1CYLjEMH4nDqSKcU
                Qe5NwGUoB9i9yLJaHnDA79QVdkCLi5b6G6Y6DzzfNRlwdfdz5yAc42BQZ2StaW4=
                =vEn+
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "create person using pgp signed message and maintainer has multiple pgpkeys"() {
      given:
        setTime(LocalDateTime.parse("2015-05-21T15:48:04")) // current time must be within 1 hour of signing time
        syncUpdate(
                "key-cert:       PGPKEY-AAAAAAAA\n" +       // primary key doesn't match public key id
                "method:         PGP\n" +
                "owner:          noreply@ripe.net <noreply@ripe.net>\n" +
                "fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                "certif:         \n" +
                "certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                "certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                "certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                "certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                "certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                "certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                "certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                "certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                "certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                "certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                "certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                "certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                "certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                "certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                "certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                "certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                "certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                "certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                "certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                "certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                "certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                "certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                "certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                "certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                "certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                "certif:         =HQmg\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n" +
                "password:       owner")
        syncUpdate(
                "key-cert:       PGPKEY-6AC7922A\n" +
                "method:         PGP\n" +
                "owner:          J\\366rg Vierke <vierke@m-net.\n" +
                "fingerpr:       BAFB 42F3 1D51 50D9 DA7A  5E84 0A73 C116 6AC7 922A\n" +
                "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:         Version: PGP 6.0.2\n" +
                "certif:\n" +
                "certif:         mQGiBDY14PMRBADaqXHEBO+JrCB9MqYxJAhUgY7aHe3YmhOKYZeNzTGTVhpxLY/c\n" +
                "certif:         WiEQMspBkI/yVmMh3CHGPYSjXk0PC6R23Y2I285ZXLLSXulKh9xXvihK4abxMRJb\n" +
                "certif:         Z/YZVZmNDAmq7U/PkoVcpIrpJjAieKgci1nJ3nPYbxzdU8NFzVoePB0lnQCg/9hl\n" +
                "certif:         TN6J31igq9rGb1vbRR3LF3EEAJwvWx6xHP6w5YtHW33Lx38gJYh+6pD9ZCcYmMxg\n" +
                "certif:         2jUesYiS7nDHMWnnOFP83vu2NZX+kizfwXC3mvKThtVogBwsEHanx3ZCzE86Whtq\n" +
                "certif:         7zCTXsaxwYo7xOXsOu3QxIQwgxTJxP06DiKlseaH5RTl/jhrxLgNmVgt5u92jNIC\n" +
                "certif:         vugFA/9afQRvrTicAu/6/Amro+eZPJhIGn+UtAF1IWnxjo4oDyg7u4dAfBFWOoKB\n" +
                "certif:         JfU17uqfYO9+h7VE6JgpRIVoSaYuEdK7i6z2kQPUTZKu1q5yoCFeddGmz2L+Opfe\n" +
                "certif:         vhNF5zhDydRv8MuRVrhv4dGSzD2nt5U6VIIOoom312zleroinYkAYQQfEQIAIQUC\n" +
                "certif:         NpxGugIHABcMgBH8wxvgaVRCordBpFR3h3sgvTrgMAAKCRAKc8EWaseSKjD2AJ9Y\n" +
                "certif:         Of15mX43cAFxanHxXCMAl6rooQCfdor/TwOLDt+7a58Bt5ODMeuLwMC0HUr2cmcg\n" +
                "certif:         VmllcmtlIDx2aWVya2VAbS1uZXQuZGU+iQBLBBARAgALBQI2NeDzBAsBAwIACgkQ\n" +
                "certif:         CnPBFmrHkirjjQCg+2WduG4haRwL8Es5gjdvm9zhd04An30z5+XUuJXGB4B1PU1E\n" +
                "certif:         2hDOhTDauQMNBDY14PMQDADMHXdXJDhK4sTw6I4TZ5dOkhNh9tvrJQ4X/faY98h8\n" +
                "certif:         ebByHTh1+/bBc8SDESYrQ2DD4+jWCv2hKCYLrqmus2UPogBTAaB81qujEh76DyrO\n" +
                "certif:         H3SET8rzF/OkQOnX0ne2Qi0CNsEmy2henXyYCQqNfi3t5F159dSST5sYjvwqp0t8\n" +
                "certif:         MvZCV7cIfwgXcqK61qlC8wXo+VMROU+28W65Szgg2gGnVqMU6Y9AVfPQB8bLQ6mU\n" +
                "certif:         rfdMZIZJ+AyDvWXpF9Sh01D49Vlf3HZSTz09jdvOmeFXklnN/biudE/F/Ha8g8VH\n" +
                "certif:         MGHOfMlm/xX5u/2RXscBqtNbno2gpXI61Brwv0YAWCvl9Ij9WE5J280gtJ3kkQc2\n" +
                "certif:         azNsOA1FHQ98iLMcfFstjvbzySPAQ/ClWxiNjrtVjLhdONM0/XwXV0OjHRhs3jMh\n" +
                "certif:         LLUq/zzhsSlAGBGNfISnCnLWhsQDGcgHKXrKlQzZlp+r0ApQmwJG0wg9ZqRdQZ+c\n" +
                "certif:         fL2JSyIZJrqrol7DVelMMm8AAgIL/3+xN+QonsGWu7Df5Ect8IYqjiCORDNfNP7p\n" +
                "certif:         JjVbyl73ECB0eHCbcJpBsyxxwHLNzK1Z2bSfon7q4nl+LTXphMSeUa3gl7ljmZ1H\n" +
                "certif:         WId/zHk+xOoTZfylbzvNjQ2zqC5RPCBAETN/FKK8WAxDZ867rp88BrfUS4r6WdEL\n" +
                "certif:         /tyEGen78EjJ86h2wydt7WlRzekrlJ68R234ElFx1rotOBq5z7rZgvAHcy/GILkX\n" +
                "certif:         nR2TsknuWmp7eX4FdlReubXd4+BbUOfxllY8ucPYP2rCRxUpjBE8kZund6fX3Hv0\n" +
                "certif:         hHRamhOIKOkW8H6t6Zi3UQHYT8lbI7xLAvTuu3pIgPHm/CBpBKqhw1jJ+ElNybIA\n" +
                "certif:         H9Bk0IYDI0S+ZZGy0BYcBtVsf1WxeJqHTnOMl160VQcrmCJBUQ1g9Eo5rYhYE/Cm\n" +
                "certif:         a04g27tCb+hMbP+L4/mzxE5gP+J2lg5NVgbUutXxoRE7KIcy1h5vYfcwB2eheLd1\n" +
                "certif:         SU4o+BCbIUsi609XXZ2tVD2dxkDze4kARgQYEQIABgUCNjXg8wAKCRAKc8EWaseS\n" +
                "certif:         KuxYAJ9PkqfRvQUVvp4uiegYTa8PsFp12ACcDpUx3Ss98f9GX1wXzv9kh5ZlnwA=\n" +
                "certif:         =P5wH\n" +
                "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n" +
                "password:       owner")

        syncUpdate(
                oneBasicFixture("OWNER-MNT").
                        replaceAll("source:\\s*TEST", "auth: PGPKEY-6AC7922A\nauth: PGPKEY-AAAAAAAA\nsource: TEST") + "password: owner")

      expect:
        query_object_matches("-r -T mntner OWNER-MNT", "mntner", "OWNER-MNT", "auth:\\s*PGPKEY-6AC7922A")
        query_object_matches("-r -T mntner OWNER-MNT", "mntner", "OWNER-MNT", "auth:\\s*PGPKEY-AAAAAAAA")

      when:
        def message = send new Message(
                subject: "",
                body: """\
				-----BEGIN PGP SIGNED MESSAGE-----
				Hash: SHA1

				person:  First Person
				address: St James Street
				address: Burnley
				address: UK
				phone:   +44 282 420469
				nic-hdl: FP1-TEST
				mnt-by:  OWNER-MNT
				source:  TEST
				-----BEGIN PGP SIGNATURE-----
				Version: GnuPG v1
				Comment: GPGTools - http://gpgtools.org

				iQEcBAEBAgAGBQJVXeP/AAoJELvMuy1XY5UNRsIH+wQ2gVqjcHonoSrKsqGa/Dpo
				RI/GSASGG/29OV4qTVblsKwCUAnE7e3IAxkAVA8QDJCvy4HFrx4wTNPQCc5IZgy2
				BOmRdlz+EXthTiIYZXVJF7wES1yiFzaY+iLBH10k88mFYDk7nZm3wwR6jAsqlwlX
				3cds2DDRepM+H9UTz//bNxRnfnq5x+Fy9hFN4nl/Lpqex/vX/X3Z82fxyN3ixhgD
				E3bDCEgHylSvcNoyBjwL7QodwQFniCNKq/ILdXfmQxOQZcLzrbM7PqUmA0906CZ/
				I5yVaOhDqhXKfCMe4amyDUzsOaefXdDHXA5tgnUdZerYWzieIdKqcx4ZmBIo4h0=
				=f046
				-----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "modify person, PGP RSA signed message, no blank line after obj"() {
      given:
        setTime(LocalDateTime.parse("2015-05-21T15:48:04")) // current time must be within 1 hour of signing time

      expect:
        queryObject("-r -T person TP1-TEST", "person", "Test Person")

      when:
        syncUpdate new SyncUpdate(data:
                oneBasicFixture("TEST-PN").stripIndent(true).
                        replaceAll("mnt-by:\\s*OWNER-MNT", "mnt-by: PGP-MNT\nremarks: changed maintainer")
                        + "password: owner")
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  PGP-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJVXe6aAAoJELvMuy1XY5UN/wcH/1l63AiE5lMUZGWvota631+K
                XdaqxoaFS1eXBtT86POXVJnijCV9IMIrpLhN01RmboH91z9JiqIJXSYTraA1DmfC
                y1euwGJ0xtHwA77Fl+/mCxa3aIaje0AIaktBiL9XKo7rRgyJPv9eQfXteSyWst/t
                +XicLYng7k2wQ6G6LrjsisXaYfwuLmUY0J51gHzy8kvOg+W+jiExB7f1fRbWCXgQ
                w32Gic5Sd6MIMEkp1uqi2g5Zop4gFB+OEQoX+DoNKOGirqmsu+aoM8I99E2HU2LA
                7/IXF0M/7XXNUFCloA748S+3zJcYm5dloZ1tJsrhTMaW78PvJ656PuwghkCL1E8=
                =3MDy
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] TP1-TEST   Test Person" }

        queryObject("-rBT person TP1-TEST", "person", "Test Person")
    }

    def "modify person, PGP RSA signed message, with blank line after obj"() {
      given:
        setTime(LocalDateTime.parse("2015-05-21T16:48:05")) // current time must be within 1 hour of signing time

      expect:
        queryObject("-r -T person TP1-TEST", "person", "Test Person")

      when:
        syncUpdate new SyncUpdate(data:
                oneBasicFixture("TEST-PN").stripIndent(true).
                        replaceAll("mnt-by:\\s*OWNER-MNT", "mnt-by: PGP-MNT\nremarks: changed maintainer")
                        + "password: owner")
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  PGP-MNT
                source:  TEST

                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJVXfAlAAoJELvMuy1XY5UNGosH/26gg1OoMAatF2va4CgZSEk0
                PPYcIDmwuT86D8GLDE63KGTUhRBraD8ZgHDYiUBvf7aeFOG6AZLB3H/Ir3/kJst9
                9AGfPEpC6RHKRv2zq79zLIHuUAuDtEsljM0BKhuvj52/VVWm/+7si4MyIMOL4AZZ
                IAFYXVc+ZOtxp/PL+lERI+Xbe7eJx3HR59hpGGUi6mfjBnF6Tt2mh4pikPpO1drB
                6Cofe3Jh8S7UQQAXTQ+LcXTTG6chYVYtMX07eETt8pVofs6wHUR1ecViDB9oRiGW
                /E0ll7ksFSbXTjoBlr5FRlbmb3k8igCUyYtsPwX0qgy7YEYW1soxL64I1kVEOaA=
                =Ox/e
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] TP1-TEST   Test Person" }

        queryObject("-rBT person TP1-TEST", "person", "Test Person")
    }

    def "modify person, PGP RSA signed message, with 3 blank lines after obj"() {
      given:
        setTime(LocalDateTime.parse("2015-05-21T16:50:18")) // current time must be within 1 hour of signing time

      expect:
        queryObject("-r -T person TP1-TEST", "person", "Test Person")

      when:
        syncUpdate new SyncUpdate(data:
                oneBasicFixture("TEST-PN").stripIndent(true).
                        replaceAll("mnt-by:\\s*OWNER-MNT", "mnt-by: PGP-MNT\nremarks: changed maintainer")
                        + "password: owner")
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  PGP-MNT
                source:  TEST



                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJVXfCqAAoJELvMuy1XY5UN9FUIAJjxCiWAt8FkUk6YPZnyHqon
                7lylztCHzwmLuYFzBAJuTwKU1t8t3VSSmVKlWfINUy7veZudBRdb0w7Lob+q6p7D
                ty0CFNbfekqQwiy9K9y8kDde8a5dECSOmEn9Ev1nLs35IMKyDv/AvB/qUgrItNj3
                cwDlFgsGhGkAYCJHVtXcdtWBwuRTiCn4Np1SXBg4yvYXRPBYn77zXoedrNMDBk1M
                r4jkpTLyt3affZvVIo2bAvAM2miJ1O/Q0+WF8CEuAgBWA4ywGUJzYR5GjV/PTfuV
                zRaC40WFkdyveVbgytzBX69y6/vQaLJIrGPnzHyZoUQ5aZx6qsc8MC7CXMO6CCs=
                =0598
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] TP1-TEST   Test Person" }

        queryObject("-rBT person TP1-TEST", "person", "Test Person")
    }

    def "create assignment, parent with mnt-lower, diff pw to mnt-by, no pw supplied, override used"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                override: denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation override used"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, diff pw to mnt-by, no pw supplied, override separate from object"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST


                override: denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        ack.contents =~ /\*\*\*Warning: An override password was found not attached to an object and was\n\s+ignored/

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "Warn if update failed and auth lines removed"() {
      given:
        databaseHelper.updateObject(
            "mntner:         OWNER-MNT\n" +
            "descr:          used to maintain other MNTNERs\n" +
            "admin-c:        TP1-TEST\n" +
            "upd-to:         updto_owner@ripe.net\n" +
            "mnt-nfy:        mntnfy_owner@ripe.net\n" +
            "notify:         notify_owner@ripe.net\n" +
            "remarks:        MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  Another Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner
                """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)

        ack.errorMessagesFor("Create", "[person] AP1-TEST   Another Person") == [
                "Authorisation for [person] AP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
        ack.warningMessagesFor("Create", "[person] AP1-TEST   Another Person") == [
                "MD5 passwords older than November 2011 were removed for one or more maintainers of this object, see: https://www.ripe.net/removed2011pw"]
    }

    def "Warn if update failed and auth lines removed and no password supplied"() {
      given:
        databaseHelper.updateObject(
            "mntner:         OWNER-MNT\n" +
            "descr:          used to maintain other MNTNERs\n" +
            "admin-c:        TP1-TEST\n" +
            "upd-to:         updto_owner@ripe.net\n" +
            "mnt-nfy:        mntnfy_owner@ripe.net\n" +
            "notify:         notify_owner@ripe.net\n" +
            "remarks:        MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  Another Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)

        ack.errorMessagesFor("Create", "[person] AP1-TEST   Another Person") == [
                "Authorisation for [person] AP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
        ack.warningMessagesFor("Create", "[person] AP1-TEST   Another Person") == [
                "MD5 passwords older than November 2011 were removed for one or more maintainers of this object, see: https://www.ripe.net/removed2011pw"]
    }

    def "No warning if auth lines removed but update succeeds with password"() {
      given:
        databaseHelper.updateObject(
                "mntner:      OWNER-MNT\n" +
                "descr:       used to maintain other MNTNERs\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      updto_owner@ripe.net\n" +
                "mnt-nfy:     mntnfy_owner@ripe.net\n" +
                "notify:      notify_owner@ripe.net\n" +
                "auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner\n" +
                "remarks:     MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  Another Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner
                """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
    }

    def "No warning if auth lines removed but update succeeds with PGP signed update"() {
      given:
        setTime(LocalDateTime.parse("2015-07-15T16:51:41"))
        databaseHelper.updateObject(
                "mntner:      PGP-MNT\n" +
                "descr:       used for testing PGP signed messages\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      updto_lir@ripe.net\n" +
                "auth:        PGPKEY-5763950D\n" +
                "remarks:     MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw\n" +
                "mnt-by:      PGP-MNT\n" +
                "source:      TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                -----BEGIN PGP SIGNED MESSAGE-----
                Hash: SHA1

                person:  Another Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AP1-TEST
                mnt-by:  PGP-MNT
                source:  TEST
                -----BEGIN PGP SIGNATURE-----
                Version: GnuPG v1
                Comment: GPGTools - http://gpgtools.org

                iQEcBAEBAgAGBQJVpnN9AAoJELvMuy1XY5UN9k0H/2YTtJRU7QGabOmZqXDJjeT+
                Q+GNUW8YLbkmwvB2hdLFnvCJiUJrEu81+TORbXqxQ95XtQx1gXS/U19WxTZLoZtY
                es/GR9rdajb2lWfjwiC+M7dHw/z88H3uvX4bsdDbUG8WxVuHyVN48D+Nc0KqKx2k
                TPUPXrIIdOzPu6N5DL7AP26stceCwTB957i4kkSsb0FVjOWb/73bJjlYibCWkt0p
                yrRq5I+HTL3clEyMExP7C0gkpBFSjII3nwuqCYpD7ltPEEnXIElVHzrBG+OGQUPy
                FrIQEgDdEFAkY5qf4W2JCWPHjzD8tv2Uzp7NfEZsqHiHyFXltuhQbyn3t8LRoBc=
                =X49Y
                -----END PGP SIGNATURE-----
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
    }

    def "No warning if auth lines removed but multiple maintainers and auth succeeds"() {
      given:
        databaseHelper.updateObject(
                "mntner:      PGP-MNT\n" +
                "descr:       used for testing PGP signed messages\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      updto_lir@ripe.net\n" +
                "auth:        PGPKEY-5763950D\n" +
                "remarks:     MD5 passwords older than November 2011 were removed from this maintainer, see: https://www.ripe.net/removed2011pw\n" +
                "mnt-by:      PGP-MNT\n" +
                "source:      TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  Another Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AP1-TEST
                mnt-by:  PGP-MNT
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner
                """.stripIndent(true))

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
    }

}
