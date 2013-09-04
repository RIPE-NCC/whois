package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message
import spec.domain.SyncUpdate
import spock.lang.Ignore

/**
 * Created with IntelliJ IDEA.
 * User: denis
 * Date: 04/09/2013
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
class DryRunSpec extends BaseSpec {

//    @Override
//    Map<String, String> getFixtures() {
//        [
//                "NULL": """\
//                """,
//        ]
//    }

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

}
