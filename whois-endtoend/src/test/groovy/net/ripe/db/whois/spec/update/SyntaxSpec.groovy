package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

class SyntaxSpec extends BaseQueryUpdateSpec {

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
                changed: denis@ripe.net 20121016
                source:  TEST
                """
   ]}

    def "delete person delete: within object"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: """\
                person:  First Person
                address: St James Street
                password: owner
                address: Burnley
                address: UK
                delete:  testing
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-r -T person FP1-TEST", "person", "FP1-TEST")
    }

    def "delete person delete: before object"() {
      given:
        dbfixture(getTransient("PN"))

      expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "delete FP1-TEST",
                body: """\
                password: owner
                delete:  testing
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] FP1-TEST   First Person" }

        queryObjectNotFound("-r -T person FP1-TEST", "person", "FP1-TEST")
    }

    def "create person password: inside object"() {
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
                password: owner
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person password: before object"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                password: owner
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object name continue line with space"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First
                 Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.successes.get(0).infos.get(0) == "Continuation lines are not allowed here and have been removed";

        queryObject("-rBT person FP1-TEST", "person", "First")
    }

    def "create person object name continue line with plus"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First
                + Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.successes.get(0).infos.get(0) == "Continuation lines are not allowed here and have been removed";

        queryObject("-rBT person FP1-TEST", "person", "First")
    }

    def "create person object name on continue lines with plus and space and eol comments"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:
                +
                + First
                  # dammed if I know
                 Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Continuation lines are not allowed here and have been removed",
                "Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rBT person FP1-TEST", "person", "")
    }

    def "create person object address block with continuation lines with space and plus"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                 Burnley
                + UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with blank continuation line with plus"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                +
                +
                 Burnley
                + UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with eol comment on pkey"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person     # pkey comment
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with eol comment"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST     ### fred's # handle
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with eol comment on source"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST     # source comment

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["End of line comments not allowed on \"source:\" attribute"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with blank eol comment"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST     #
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] FP1-TEST   First Person") == [
                "Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with eol comment on blank continuation line"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                +#eol comment
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with 2 changed attrs no date"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net
                changed: denis@ripe.net
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["More than one \"changed:\" attribute without date"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with 3 changed attrs dates wrong order"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20130112
                changed: denis@ripe.net 20101009
                changed: denis@ripe.net 20121009
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }

        queryObject("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with changed attrs date syntax error month"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20129909
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Syntax error in denis@ripe.net 20129909"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with changed attrs date syntax error day in month"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20120231
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Syntax error in denis@ripe.net 20120231"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with changed attrs date syntax error day"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121199
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Syntax error in denis@ripe.net 20121199"]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with changed attrs date syntax error year"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 19010912
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Date is older than the database itself in changed: attribute \"19010912\""]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "create person object with changed attrs date in future"() {
      expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20201109
                source:  TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] FP1-TEST   First Person" }
        ack.errorMessagesFor("Create", "[person] FP1-TEST   First Person") ==
                ["Date is in the future in changed: attribute \"20201109\""]

        queryObjectNotFound("-rBT person FP1-TEST", "person", "First Person")
    }

    def "modify person, make dummification style changes"() {
        given:
        dbfixture(getTransient("PN"))

        expect:
        queryObject("-r -T person FP1-TEST", "person", "First Person")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                person:  First Person
                address: ***
                address: ***
                address: UK
                phone:   +44 282 4.. ...
                nic-hdl: FP1-TEST
                mnt-by:  OWNER-MNT
                changed: ***@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[person] FP1-TEST   First Person"}

        query_object_matches("-rBG -T person FP1-TEST", "person", "First Person", "\\*\\*\\*@")
        query_object_matches("-rBG -T person FP1-TEST", "person", "First Person", "4\\.\\.")
    }

    @Ignore //TODO [AS] ignore this testcase until @ripe.net business rule is implemented
    def "create person with @ripe.net in notify: attr"() {
        given:

        expect:
        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")

        when:
        def message = syncUpdate("""\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                notify:  dbtest@ripe.net
                mnt-by:  OWNER-MNT
                changed: denis@ripe.net 20121016
                source:  TEST

                password: owner
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[person] First Person" }
        ack.errorMessagesFor("Modify", "[person] First Person") ==
                ["Authorisation for [inetnum] 192.168.200.0 - 192.168.200.255 failed using \"mnt-by:\" not authenticated by: END-USER-MNT"]

        queryObjectNotFound("-r -T person FP1-TEST", "person", "First Person")
    }

}
