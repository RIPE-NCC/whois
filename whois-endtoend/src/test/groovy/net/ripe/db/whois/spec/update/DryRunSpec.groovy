package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.SyncUpdate

class DryRunSpec extends BaseSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "FIRST": """\
                   person:  First Person
                   address: St James Street
                   address: Burnley
                   address: UK
                   phone:   +44 282 420469
                   nic-hdl: Fp11-RIpe
                   mnt-by:  owner-mnt
                   changed: denis@ripe.net 20121016
                   source:  TEST
                """,
                "ALLOC-PA-LOW-DOM-R": """\
                inetnum:      193.0.0.0 - 193.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-domains:  LIR2-MNT
                mnt-routes:   LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
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

    def "modify person with dry-run, add data"() {
        given:

        expect:
        queryObject("-rGBT person TP1-TEST", "person", "Test Person")

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

        query_object_not_matches("-rGBT person TP1-TEST", "person", "Test Person", "just added")
    }

    def "modify person with dry-run, remove data"() {
        given:

        expect:
        queryObject("-rGBT person TP1-TEST", "person", "Test Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  Test Person
                address: St James Street
                address: UK
                phone:   +44 282 420469
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
        ack.contents =~ /address:\s*St James Street\n-address:\s*Burnley\n address:\s*UK/

        noMoreMessages()

        query_object_matches("-rGBT person TP1-TEST", "person", "Test Person", "Burnley")
    }

    def "modify person with dry-run, add & remove data"() {
        given:

        expect:
        queryObject("-rGBT person TP1-TEST", "person", "Test Person")

        when:
        def message = syncUpdate(new SyncUpdate(data: """\
                person:  Test Person
                address: St James Street
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
        ack.contents =~ /address:\s*St James Street\n-address:\s*Burnley\n address:\s*UK/
        ack.contents =~ /phone:\s*\+44 282 420469\n\+remarks:\s*just added\n nic-hdl:\s*TP1-TEST/

        noMoreMessages()

        query_object_not_matches("-rGBT person TP1-TEST", "person", "Test Person", "just added")
        query_object_matches("-rGBT person TP1-TEST", "person", "Test Person", "Burnley")
    }

    def "delete person with dry-run"() {
        given:
        dbfixture(getTransient("FIRST"))

        expect:
        queryObject("-rGBT person Fp11-RIpe", "person", "First Person")

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
                delete:  dry-run, should not delete

                dry-run:
                password:   owner
                """.stripIndent(), redirect: false)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[person] Fp11-RIpe   First Person" }
        ack.infoSuccessMessagesFor("Delete", "[person] Fp11-RIpe") == [
                "Dry-run performed, no changes to the database have been made"]

        noMoreMessages()

        queryObject("-rGBT person Fp11-RIpe", "person", "First Person")
    }

    def "modify 2 person with dry-run"() {
        given:
        dbfixture(getTransient("FIRST"))

        expect:
        queryObject("-rGBT person TP1-TEST", "person", "Test Person")
        queryObject("-rGBT person Fp11-RIpe", "person", "First Person")

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

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 0, 0, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.contents =~ /(?s)Dry-run is only supported when a single update is specified.*?Dry-run is only supported when a single update is specified/
        ! (ack.contents =~ /\@\@/)

        noMoreMessages()

        query_object_not_matches("-rGBT person TP1-TEST", "person", "Test Person", "just added")
    }

    def "create reverse domain, with dry run"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LOW-DOM-R") + "password: hm\npassword: owner3")

        expect:
        queryObject("-r -T inetnum 193.0.0.0 - 193.255.255.255", "inetnum", "193.0.0.0 - 193.255.255.255")
        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")

        when:
        def message = syncUpdate("""\
                domain:         0.0.193.in-addr.arpa
                descr:          reverse domain
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                zone-c:         TP1-TEST
                nserver:        pri.authdns.ripe.net
                nserver:        ns3.nic.fr
                mnt-by:         owner-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                dry-run:
                password:   lir2
                password:   owner
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[domain] 0.0.193.in-addr.arpa" }
        ack.infoSuccessMessagesFor("Create", "[domain] 0.0.193.in-addr.arpa") == [
                "Dry-run performed, no changes to the database have been made"]

        queryObjectNotFound("-rGBT domain 0.0.193.in-addr.arpa", "domain", "0.0.193.in-addr.arpa")
    }

}
