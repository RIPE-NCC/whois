package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.SyncUpdate

class DryRunSpec extends BaseSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "NULL": """\
                """,
        ]
    }

    def "create person with dry-run"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                dry-run:
                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run before object"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                dry-run:

                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run attached to, before object"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                dry-run:
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run attached to, after object"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST
                dry-run:

                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run within object"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                dry-run:
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run with value"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                dry-run:  qwerty
                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run, auth fail"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                dry-run:
                password:   fred
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]
        ack.errorMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Authorisation for [person] Fp11-RIpe failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "create person with dry-run, syntax fail"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   44 282 420469
                nic-hdl: Fp11-RIpe
                mnt-by:  owner-mnt
                changed: denis@ripe.net 20121016
                source:  TEST

                dry-run:
                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]
        ack.errorMessagesFor("Create", "[person] Fp11-RIpe") == [
                "Syntax error in 44 282 420469"]

        noMoreMessages()

        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "modify person with dry-run"() {
        given:

        expect:
        queryObjectNotFound("-rGBT person Fp11-RIpe", "person", "First Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                remarks: just added
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                dry-run:
                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[person] TP1-TEST   Test Person" }
        ack.infoSuccessMessagesFor("Modify", "[person] TP1-TEST") == [
                "Dry-run performed, no changes to the database have been made"]
        ack.contents =~ /phone:\s*\+44 282 420469\n\+remarks:\s*just added\n nic-hdl:\s*TP1-TEST/

        noMoreMessages()

        query_object_not_matches("-rGBT person Fp11-RIpe", "person", "First Person", "just added")
    }

}
