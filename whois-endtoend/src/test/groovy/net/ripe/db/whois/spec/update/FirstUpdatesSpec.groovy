package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class FirstUpdatesSpec extends BaseQueryUpdateSpec {

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
            "PN-OPT": """\
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
            """,
    ]}

    def "empty update"() {

      when:
        def message = send new Message(
                subject: "",
                body: ""
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
    }

    def "garbage update"() {

      when:
        def message = send new Message(
                subject: "the rain in spain falls mainly on the plain",
                body: "the quick brown fox jumped over lazy dogs"
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("\nthe quick brown fox jumped over lazy dogs")

    }

    def "create person no source"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "nEw",
                body: """\
                person:  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT

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
        ack.errorMessagesFor("Create", "[person] FPE1-TEST   First Person Error") == [
                "Mandatory attribute \"source\" is missing"
        ]

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person syntax error"() {
      expect:
        queryNothing("-rGBT person SYN-ERR")

      when:
        def message = send new Message(
                subject: "create SYN-ERR",
                body: """\
                person:  First Person Error
                addres: St James Street  # invalid attribute name
                address: Burnley
                address: UK
                phone:   44 282 420469   # missing '+'
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
                remarks:                 # none
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

        ack.countErrorWarnInfo(2, 3, 0)
        ack.errorMessagesFor("Create", "[person] FPE1-TEST   First Person Error") == [
                "\"addres\" is not a known RPSL attribute",
                "Syntax error in 44 282 420469"]

        queryNothing("-rGBT person SYN-ERR")
    }

    def "create person invalid object type"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "create person",
                body: """\
                person :  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("\nperson :  First Person Error")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person indented"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "create indented",
                body: "password: owner\n\n" +
                """\
                person:  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("\nperson:  First Person Error")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person with blank lines"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "create INV-TYPE",
                body: """\
                person:  First Person Error\n
                address: St James Street\n
                address: Burnley\n
                address: UK\n
                phone:   +44 282 420469\n
                nic-hdl: FPE1-TEST\n
                mnt-by:  OWNER-MNT\n
                source:  TEST\n
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("person:  First Person Error")
        ack.garbageContains("phone:   \\+44 282 420469")
        ack.garbageContains("source:  TEST")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person syntax error wrong source and garbage"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "NEw",
                body: """\
                person:  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
                source: owner

                qwerty qwerty
                qwerty
                qwerty qwerty qwerty

                asdfg asdfg
                asdfg
                asdfg asdfg asdfg

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 1)
        ack.errorMessagesFor("Create", "[person] FPE1-TEST   First Person Error") == [
                "Unrecognized source: OWNER"
        ]
        ack.infoMessagesFor("Create", "[person] FPE1-TEST   First Person Error") == [
                "Value owner converted to OWNER"
        ]
        ack.garbageContains("\nqwerty qwerty")
        ack.garbageContains("\nasdfg asdfg")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "delete person non existant"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "delete person",
                body: """\
                person:  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
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
        ack.errorMessagesFor("Delete", "[person] FPE1-TEST   First Person Error") == [
                "Object [person] FPE1-TEST First Person Error does not exist in the database"]

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person space before colon"() {
      expect:
        queryNothing("-rGBT person SYN-ERR")

      when:
        def message = send new Message(
                subject: "create SYN-ERR",
                body: """\
                person:  First Person Error
                address : St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
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
        ack.errorMessagesFor("Create", "[person] FPE1-TEST   First Person Error") == [
                "\"address \" is not a known RPSL attribute"]

        queryNothing("-rGBT person SYN-ERR")
    }

    def "create person odd char in name"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "create person",
                body: """\
                person:  First Perüson Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPE1-TEST
                mnt-by:  OWNER-MNT
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
        def messages = ack.errorMessagesFor("Create", "[person] FPE1-TEST")
        messages.length == 1
        messages[0].startsWith("Syntax error in First ")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person odd char in nic-hdl"() {
      expect:
        queryNothing("-rGBT person FPE1-TEST")

      when:
        def message = send new Message(
                subject: "create person",
                body: """\
                person:  First Person Error
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FPEü1-TEST
                mnt-by:  OWNER-MNT
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
        ack.objErrorContains("Create", "FAILED", "person", "FPEü1-TEST   First Person Error","Syntax error in FPE")

        queryNothing("-rGBT person FPE1-TEST")
    }

    def "create person odd char in address"() {
      expect:
        queryNothing("-rGBT person FP1-TEST")

      when:
        def message = send new Message(
                subject: "create person",
                body: """\
                person:  First Person
                address: St James Street
                address: Bürnley
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
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)

        queryObject("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "create person odd char in address via syncupdate"() {
      expect:
        queryNothing("-rGBT person FP1-TEST")

      when:
        syncUpdate(
                """\
                person:  First Person
                address: St James Street
                address: Bürnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                password: owner
                """.stripIndent(true)
        )

      then:
        queryObject("-rGBT person FP1-TEST", "person", "First Person")
    }

    def "delete person corrupt class attr"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete person FP1-TEST",
                body: """\
                erson:  First Person
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

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("erson:  First Person")

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "delete person null pkey"() {
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
                address: UK
                phone:   +44 282 420469
                nic-hdl:
                mnt-by:  OWNER-MNT
                source:  TEST
                delete:  testing

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed

        ack.summary.nrFound == 0
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.garbageContains("person:  First Person")

        queryObject("-r -T person FP1-TEST", "person", "First Person")
    }

    def "modify person with end of line comment on source"() {
      given:
      syncUpdate(getTransient("PN-OPT") + "override: denis,override1")

      expect:
        queryObject("-rBG -T person FOP1-TEST", "person", "First Optional Person")

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
                org:     ORG-OTO1-TEST
                nic-hdl: FOP1-TEST
                remarks: test person
                notify:  dbtest-nfy@ripe.net
                mnt-by:  OWNER-MNT
                source:  TEST # some comment not allowed

                password: owner
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errorMessagesFor("Modify", "[person] FOP1-TEST   First Optional Person") ==
                ["End of line comments not allowed on \"source:\" attribute"]

        query_object_matches("-rBG -T person FOP1-TEST", "person", "First Optional Person", "e-mail:")
    }

}
