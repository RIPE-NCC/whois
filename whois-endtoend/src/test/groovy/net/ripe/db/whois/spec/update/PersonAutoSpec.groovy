package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PersonAutoSpec extends BaseQueryUpdateSpec {

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
             "NO-MB-PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                source:  TEST
                """,
            "PN1": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: ABC1-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """,
            "PN3": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: ABC3-TEST
                mnt-by:  OWNER-MNT
                source:  TEST
                """
   ]}

    def "create person auto-1"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-1",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-1
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
        ack.successes.any {it.operation == "Create" && it.key == "[person] FP1-TEST   First Person"}
        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person auto-123ABC"() {
      expect:
        queryObjectNotFound("-r -T person ABC1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-123ABC",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: AuTo-123ABC
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
        ack.successes.any {it.operation == "Create" && it.key == "[person] ABC1-TEST   First Person"}
        queryObject("-rBT person ABC1-TEST", "person", "First Person")
    }

    def "create person auto-22ABC"() {
      given:
        syncUpdate(getTransient("PN1") + "password: owner")
        syncUpdate(getTransient("PN3") + "password: owner")

      expect:
        queryObject("-rBT person ABC1-TEST", "person", "First Person")
        queryObject("-rBT person ABC3-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person ABC2-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-22ABC",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-22ABC
                mnt-by:  OWNER-MNT
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        queryObjectNotFound("-r -T person auto-22ABC", "person", "First Person")

        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[person] ABC2-TEST   First Person"}
        queryObject("-rBT person ABC1-TEST", "person", "First Person")
        queryObject("-rBT person ABC2-TEST", "person", "First Person")
        queryObject("-rBT person ABC3-TEST", "person", "First Person")
    }

    def "create person auto-2A"() {
      expect:
        queryObjectNotFound("-r -T person A1-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2A", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-2A",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-2A
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
        ack.errors.any {it.operation == "Create" && it.key == "[person] auto-2A   First Person"}
        ack.errorMessagesFor("Create", "[person] auto-2A   First Person") ==
                ["Syntax error in auto-2A"]

        queryObjectNotFound("-rBT person A1-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2A", "person", "First Person")
    }

    def "create person auto-2ABCDE"() {
      expect:
        queryObjectNotFound("-r -T person ABCDE1-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2ABCDE", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-2ABCDE",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-2ABCDE
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
        ack.errors.any {it.operation == "Create" && it.key == "[person] auto-2ABCDE   First Person"}
        ack.errorMessagesFor("Create", "[person] auto-2ABCDE   First Person") ==
                ["Syntax error in auto-2ABCDE"]

        queryObjectNotFound("-rBT person ABCDE1-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2ABCDE", "person", "First Person")
    }

    def "create person auto-2ABC-NL"() {
      expect:
        queryObjectNotFound("-r -T person ABC-NL-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person ABC-NL", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2ABC-NL", "person", "First Person")

      when:
        def message = send new Message(
                subject: "create person auto-2ABC-NL",
                body: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-2ABC-NL
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
        ack.errors.any {it.operation == "Create" && it.key == "[person] auto-2ABC-NL   First Person"}
        ack.errorMessagesFor("Create", "[person] auto-2ABC-NL   First Person") ==
                ["Syntax error in auto-2ABC-NL"]

        queryObjectNotFound("-r -T person ABC-NL-TEST", "person", "First Person")
        queryObjectNotFound("-r -T person ABC-NL", "person", "First Person")
        queryObjectNotFound("-r -T person auto-2ABC-NL", "person", "First Person")
    }

    def "create role with auto-1 that references auto-1"() {
      expect:
        queryObjectNotFound("-r -T role FR1-TEST", "role", "First Role")
        queryObjectNotFound("-r -T role AUTO-1", "role", "First Role")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: auto-1
                tech-c:  AuTo-1
                nic-hdl: AUTO-1
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

        ack.countErrorWarnInfo(2, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[role] AUTO-1   First Role"}
        ack.errorMessagesFor("Create", "[role] AUTO-1   First Role") ==
                ["Self reference is not allowed for attribute type \"admin-c\"",
                 "Self reference is not allowed for attribute type \"tech-c\""]

        queryObjectNotFound("-r -T role FR1-TEST", "role", "First Role")
        queryObjectNotFound("-r -T role AUTO-1", "role", "First Role")
    }

    def "create role that references non exist auto-1"() {
      expect:
        queryObjectNotFound("-r -T role FR1-TEST", "role", "First Role")
        queryObjectNotFound("-r -T role AUTO-1", "role", "First Role")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                role:    First Role
                address: St James Street
                address: Burnley
                address: UK
                e-mail:  dbtest@ripe.net
                admin-c: auto-1
                tech-c:  AuTo-1
                nic-hdl: FR1-TEST
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

        ack.countErrorWarnInfo(2, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[role] FR1-TEST   First Role"}
        ack.errorMessagesFor("Create", "[role] FR1-TEST   First Role") ==
                ["Reference \"auto-1\" not found",
                 "Reference \"AuTo-1\" not found"]

        queryObjectNotFound("-r -T role FR1-TEST", "role", "First Role")
    }

    def "create person three word name one word start with non letter"() {
      expect:
        queryObjectNotFound("-r -T person FS1-TEST", "person", "First_ -Person Smith--")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First_ -Person Smith--
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-1
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

        queryObject("-rBT person FS1-TEST", "person", "First_ -Person Smith--")
    }

    def "create person three word name two word start with non letter"() {
      expect:
        queryObjectNotFound("-r -T person FA1-TEST", "person", "First_ -Person _Smith--")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First_ -Person _Smith--
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: auto-1
                mnt-by:  owner-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Create", "[person] auto-1   First_ -Person _Smith--") ==
                ["Syntax error in First_ -Person _Smith--"]

        queryObjectNotFound("-rBT person FA1-TEST", "person", "First_ -Person _Smith--")
    }

    def "create person obj auto-1, modify mntner ref auto-1 in auth"() {
      expect:
        queryObjectNotFound("-r -T key-cert X509-1", "key-cert", "X509-1")

      when:
        def response = syncUpdate("""\
                mntner:      TST-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                auth:        auto-2
                mnt-by:      OWNER-MNT
                source:      TEST

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: aUtO-2
                mnt-by:  OWNER-MNT
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", response)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errors.any { it.operation == "Modify" && it.key == "[mntner] TST-MNT" }
        ack.errorMessagesFor("Modify", "[mntner] TST-MNT") ==
                ["Invalid reference to [person] FP1-TEST"]

        queryObject("-rGBT person FP1-TEST", "person", "First Person")
        query_object_not_matches("-rGBT mntner TST-MNT", "mntner", "TST-MNT", "auth:\\s*FP1-TEST")
    }
}
