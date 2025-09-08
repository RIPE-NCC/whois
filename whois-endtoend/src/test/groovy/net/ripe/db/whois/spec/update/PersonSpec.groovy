package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PersonSpec extends BaseQueryUpdateSpec  {

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
            "PN2": """\
                person:  Second Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP2-TEST
                mnt-by:  TST
                source:  TEST
                """,
            "PN-OPT": """\
                person:  First Optional Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                fax:     +31 20535 4444
                e-mail:  dbtest@ripe.net
                org:     ORG-OTO1-TEST
                nic-hdl: FOP1-TEST
                remarks: test person
                notify:  dbtest-nfy@ripe.net
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
             "NO-MB-PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                source:  TEST
                """,
            "RL": """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: FP1-TEST
                tech-c:  TP1-TEST
                nic-hdl: FR1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """
   ]}

    def "delete non-existent person"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete non existent person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                delete:  testing

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Delete", "[person] FP1-TEST   First Person") == [
                "Object [person] FP1-TEST First Person does not exist in the database"]

        queryNothing("-r -T person FP1-TEST")

        noMoreMessages()
    }

    def "delete person"() {
      given:
        def toDelete = dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: toDelete + "delete: testing person delete\npassword: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete person not identical"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                delete:  testing

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 4, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Delete", "[person] FP1-TEST   First Person") ==
                ["Object [person] FP1-TEST First Person doesn't match version in database"]

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete person not case sensitive"() {
      given:
        def toDelete = dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: toDelete.replace("St James Street", "ST JAMES STREET") + "delete: testing person delete\npassword: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete person authentication failed"() {
      given:
        def toDelete = dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: toDelete + "delete: testing person delete\npassword: WRONG"
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Delete", "[person] FP1-TEST   First Person") ==
                ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]
        ack.authFailCheck("Delete", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete person with no mnt-by"() {
      given:
        def toDelete = dbfixture(getTransient("NO-MB-PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: toDelete + "delete: testing person delete\npassword: owner"
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "delete referenced person"() {
      given:
        def toDelete = getTransient("PN")
        syncUpdate(getTransient("PN") + "password: owner")
        syncUpdate(getTransient("RL") + "password: owner")

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")
        query_object_matches("-rBT role FR1-TEST", "role", "First Role", "admin-c:\\s*FP1-TEST")

      when:
        def message = send new Message(
                subject: "delete non existent person FP1-TEST",
                body: toDelete + "delete: testing person delete\npassword: owner"
        )

      then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 4, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Delete", "[person] FP1-TEST   First Person") == [
                "Object [person] FP1-TEST is referenced from other objects"]

        queryObject("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "modify person identical"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 5, 0)
        ack.successes.any {it.operation == "No operation" && it.key == "[person] FP1-TEST   First Person"}
        ack.warningSuccessMessagesFor("No operation", "[person] FP1-TEST   First Person") ==
                ["Submitted object identical to database object"]

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "modify person add missing mnt-by"() {
      given:
        dbfixture(getTransient("NO-MB-PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        query_object_matches("-rBT person FP1-TEST", "person", "First Person", "mnt-by:\\s*OWNER-MNT")
    }

    def "modify person add missing mnt-by with no password"() {
      given:
        dbfixture(getTransient("NO-MB-PN"))

      expect:
        query_object_not_matches("-r -T person FP1-TEST", "person", "First Person", "mnt-by:")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        ack.errorMessagesFor("Modify", "[person] FP1-TEST   First Person") ==
                ["Authorisation for [person] FP1-TEST failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        ack.authFailCheck("Modify", "FAILED", "person", "FP1-TEST   First Person", "", "person", "FP1-TEST", "mnt-by", "OWNER-MNT")

        query_object_not_matches("-rBT person FP1-TEST", "person", "First Person", "mnt-by:")
    }

    def "modify person remove mnt-by"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Modify", "[person] FP1-TEST   First Person") ==
                ["Mandatory attribute \"mnt-by\" is missing"]

        query_object_matches("-rBT person FP1-TEST", "person", "First Person", "mnt-by:\\s*OWNER-MNT")
    }

    def "modify person to change name"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  Second Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   Second Person"}

        queryObject("-rBT person Second Person", "person", "Second Person")
        queryObject("-rBT person FP1-TEST", "person", "Second Person")
    }

    def "modify role to change name"() {
        given:
        databaseHelper.addObject(getTransient("PN"))
        databaseHelper.addObject(getTransient("RL"))

        when:
        def message = send new Message(
                subject: "modify role FP1-TEST",
                body: """\
                role:  Changed Role
                e-mail: role@ripe.net
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FR1-TEST
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

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[role] FR1-TEST   Changed Role"}

        queryObject("-rBT role FR1-TEST", "role", "Changed Role")
        queryObject("-rBT role Changed Role", "role", "Changed Role")
    }

    def "create person no mnt-by"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Mandatory attribute \"mnt-by\" is missing"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person 1 char in nic-hdl"() {
      expect:
        queryObjectNotFound("-r -T person F", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person F",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: F
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] F   First Person"}
        ack.errorMessagesFor("Create", "[person] F   First Person") ==
                ["Syntax error in F"]

        queryObjectNotFound("-rBT person F", "person", "First Person")
    }

    def "create person 5 char in nic-hdl"() {
      expect:
        queryObjectNotFound("-r -T person Fuddd", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person Fuddd",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fuddd
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] Fuddd   First Person"}
        ack.errorMessagesFor("Create", "[person] Fuddd   First Person") ==
                ["Syntax error in Fuddd"]

        queryObjectNotFound("-rBT person Fuddd", "person", "First Person")
    }

    def "create person 2 char, 7 digits in nic-hdl"() {
      expect:
        queryObjectNotFound("-r -T person FP1234567", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP1234567",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1234567
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1234567   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1234567   First Person") ==
                ["Syntax error in FP1234567"]

        queryObjectNotFound("-rBT person FP1234567", "person", "First Person")
    }

    def "create person 2 char, 6 digits, leading 0 in nic-hdl"() {
      expect:
        queryObjectNotFound("-r -T person FP012345", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP012345",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP012345
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP012345   First Person"}
        ack.errorMessagesFor("Create", "[person] FP012345   First Person") ==
                ["Syntax error in FP012345"]

        queryObjectNotFound("-rBT person FP012345", "person", "First Person")
    }

    def "create person 0 chars, 4 digits in nic-hdl"() {
      expect:
        queryObjectNotFound("-r -T person 1234", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person 1234",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: 1234
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] 1234   First Person"}
        ack.errorMessagesFor("Create", "[person] 1234   First Person") ==
                ["Syntax error in 1234"]

        queryObjectNotFound("-rBT person 1234", "person", "First Person")
    }

    def "create person nic-hdl with cc suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP11-NL", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP11-NL",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP11-NL
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP11-NL   First Person"}

        queryObject("-rBT person FP11-NL", "person", "First Person")
    }

    def "create person nic-hdl with RIR suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP11-APNIC", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP11-apNIc",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP11-apNIc
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP11-apNIc   First Person"}

        queryObject("-rBT person FP11-apnic", "person", "First Person")
    }

    def "create person nic-hdl with RIPE RIR suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP11-RIPE", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP11-ripE
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP11-ripE   First Person"}

        queryObject("-rBT person FP11-ripE", "person", "First Person")
    }

    def "create person invalid nic-hdl suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP1-fred", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP1-fred",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-fred
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

        ack.countErrorWarnInfo(1, 4, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-fred   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1-fred   First Person") ==
                ["Syntax error in FP1-fred"]

        queryObjectNotFound("-rBT person FP1-fred", "person", "First Person")
    }

    def "create person no dash suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP1TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP1TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1TEST
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1TEST   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1TEST   First Person") ==
                ["Syntax error in FP1TEST"]

        queryObjectNotFound("-rBT person FP1TEST", "person", "First Person")
    }

    def "create person no suffix"() {
      expect:
        queryObjectNotFound("-r -T person FP1", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP1   First Person"}
//        ack.errorMessagesFor("Create", "[person] FP1   First Person") ==          // TODO
 //               ["Syntax error in FP1"]

        queryObject("-rBT person FP1", "person", "First Person")
    }

    def "create person using previously deleted nic-hdl"() {
      given:
        syncUpdate(getTransient("PN") + "password: owner")
        queryObject("-r -T person FP1-TEST", "person", "First Person")
        syncUpdate(getTransient("PN") + "delete: testing\npassword: owner")

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
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["The nic-hdl \"FP1-TEST\" is not available"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person ref non existing mntner"() {
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
                mnt-by:  non-exist-mnt
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

        ack.countErrorWarnInfo(3, 1, 1)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}

        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Unknown object referenced non-exist-mnt",
                 "Authorisation for [person] FP1-TEST failed using \"mnt-by:\" no valid maintainer found",
                 "The maintainer 'non-exist-mnt' was not found in the database"]

        ack.contents =~
                "Error:   Authorisation for \\[person\\] FP1-TEST failed\n\\s+using \"mnt-by:\"\n\\s+no valid maintainer found"

        ack.infoMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["To create the first person/mntner pair of objects for an organisation see https://apps.db.ripe.net/db-web-ui/webupdates/create/RIPE/person/self"]

        ack.objErrorContains("Create", "FAILED", "person", "FP1-TEST   First Person", "Unknown object referenced non-exist-mnt")

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person syntax error"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person FP1-fred",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   invalid
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

        ack.countErrorWarnInfo(1, 3, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Syntax error in invalid"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person with all optional attrs"() {
      expect:
        queryObjectNotFound("-r -T person FOP1-TEST", "person", "First Optional Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Optional Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                fax-no:  +31 20535 4444
                e-mail:  dbtest@ripe.net
                org:     ORG-OTO1-TEST
                nic-hdl: FOP1-TEST
                remarks: test person
                notify:  dbtest-nfy@ripe.net
                mnt-by:  OWNER-MNT
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
        ack.successes.any {it.operation == "Create" && it.key == "[person] FOP1-TEST   First Optional Person"}

        queryObject("-rBT person FOP1-TEST", "person", "First Optional Person")
    }

    def "create person with all optional and multiple and duplicate attrs"() {
      expect:
        queryObjectNotFound("-r -T person FOP1-TEST", "person", "First Optional Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Optional Person
                address: St James Street
                address: Burnley
                address: UK
                address: UK
                phone:   +44 282 420469
                phone:   +44 282 411141
                phone:   +44 282 420469
                fax-no:  +31 20535 4444
                fax-no:  +31 20535 4444
                fax-no:  +44 20535 4444
                e-mail:  dbtest@ripe.net
                org:     ORG-OTO1-TEST
                org:     ORG-OTO1-TEST
                org:     ORG-OTO1-TEST
                org:     ORG-OTO1-TEST
                e-mail:  dbtest2@ripe.net
                nic-hdl: FOP1-TEST
                e-mail:  dbtest@ripe.net
                remarks: test person
                remarks: another remark
                remarks: test person
                notify:  dbtest-nfy@ripe.net
                notify:  dbtest-nfy@ripe.net
                mnt-by:  OWNER-MNT
                mnt-by:  OWNER3-MNT, OWNER2-MNT
                mnt-by:  OWNER-MNT, OWNER-MNT
                source:  TEST
                notify:  dbtest2-nfy@ripe.net

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
        ack.successes.any {it.operation == "Create" && it.key == "[person] FOP1-TEST   First Optional Person"}

        def notif = notificationFor "dbtest-nfy@ripe.net"
        notif.subject =~ "Notification of RIPE Database changes"
        notif.created.any { it.type == "person" && it.key == "First Optional Person" }

        def notif2 = notificationFor "dbtest2-nfy@ripe.net"
        notif2.subject =~ "Notification of RIPE Database changes"
        notif2.created.any { it.type == "person" && it.key == "First Optional Person" }

        def notif3 = notificationFor "dbtest-org@ripe.net"
        notif3.subject =~ "Notification of RIPE Database changes"
        notif3.created.any { it.type == "person" && it.key == "First Optional Person" }

        def notif4 = notificationFor "mntnfy_owner@ripe.net"
        notif4.subject =~ "Notification of RIPE Database changes"
        notif4.created.any { it.type == "person" && it.key == "First Optional Person" }

        def notif5 = notificationFor "mntnfy_owner2@ripe.net"
        notif5.subject =~ "Notification of RIPE Database changes"
        notif5.created.any { it.type == "person" && it.key == "First Optional Person" }

        noMoreMessages()

        queryObject("-rBT person FOP1-TEST", "person", "First Optional Person")
    }

    def "create person one word name"() {
      expect:
        queryObjectNotFound("-r -T person F1-TEST", "person", "First")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: F1-TEST
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

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] F1-TEST   First"}
        ack.errorMessagesFor("Create", "[person] F1-TEST   First") ==
                ["Syntax error in First",
                 "Syntax error in F1-TEST"]

        queryObjectNotFound("-rBT person F1-TEST", "person", "First")
    }

    def "create person two word name first word non letter start"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "-First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  -First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   -First Person"}
        ack.errorMessagesFor("Create", "[person] FP1-TEST   -First Person") ==
                ["Syntax error in -First Person"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "-First Person")
    }

    def "create person two word name second word non letter start"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First _Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First _Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First _Person"}
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First _Person") ==
                ["Syntax error in First _Person"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First _Person")
    }

    def "create person three word name, third word only digits"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person 352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person 352
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person 352"}

        queryObject("-rBT person FP1-TEST", "person", "First Person 352")
    }

    def "create person two word name with multiple - and _"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First__ Person-Smith--")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First__ Person-Smith--
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First__ Person-Smith--" }

        queryObject("-rBT person FP1-TEST", "person", "First__ Person-Smith--")
    }

    def "create person two identical words name"() {
      expect:
        queryObjectNotFound("-r -T person AA1-TEST", "person", "Anthony Anthony")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  Anthony Anthony
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AA1-TEST
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] AA1-TEST   Anthony Anthony" }

        queryObject("-rBT person AA1-TEST", "person", "Anthony Anthony")
    }

    def "modify person mntner no -mnt suffix"() {
      given:
        syncUpdate(getTransient("PN2") + "password: test")

      expect:
        queryObject("-r -T person FP2-TEST", "person", "Second Person")

      when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  Second Person
                address: St James Street
                address: Burnley
                address: UK
                remarks: updated
                phone:   +44 282 420469
                nic-hdl: FP2-TEST
                mnt-by:  TST
                source:  TEST

                password: test
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP2-TEST   Second Person"}

        query_object_not_matches("-r -T person FP2-TEST", "person", "Second Person", "20121016")
    }

    def "modify person with empty remarks"() {
        given:
        dbfixture(getTransient("PN"))

        expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

        when:
        def message = send new Message(
                subject: "modify person FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                remarks:
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner

                """.stripIndent(true)
        )

        then:
        ackFor message
        queryLineMatches("-GBr -T person FP1-TEST", "remarks")
    }

    def "create person with abuse-mailbox"() {
        expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                remarks:
                abuse-mailbox: abuse@ripe.net
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner

                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Create", "[person] FP1-TEST") ==
                [ "\"abuse-mailbox\" is not valid for this object type"]
    }

    def "modify person, add abuse-mailbox"() {
        given:
        dbfixture("" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST")

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
                abuse-mailbox: dbtest-abuse2@ripe.net
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Modify", "[person] FP1-TEST") ==
                [ "\"abuse-mailbox\" is not valid for this object type"]
    }

    def "modify person, remove abuse-mailbox"() {
        given:
        dbfixture("" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "abuse-mailbox: dbtest-abuse@ripe.net\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST")

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
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
    }

}
