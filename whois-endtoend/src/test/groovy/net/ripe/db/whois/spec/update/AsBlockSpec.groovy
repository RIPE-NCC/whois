package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class AsBlockSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST
                """,
                "RIPE-DBM-STARTUP-MNT": """\
                mntner:      RIPE-DBM-STARTUP-MNT
                descr:       Mntner for creating as-objects.
                upd-to:      updto_hm@ripe.net
                mnt-nfy:     mntnfy_hm@ripe.net
                notify:      notify_hm@ripe.net
                admin-c:     TP2-TEST
                auth:        MD5-PW \$1\$vuJ5SxxZ\$B4rxOF9eXgwvJPm.zDZxF1 # startup
                mnt-by:      RIPE-DBM-STARTUP-MNT
                source:      TEST
                """
        ]
    }

    def "create as-block with RIPE mnt-by, mnt-by pw supplied"() {
        expect:
            queryObjectNotFound("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                password: owner3
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")

    }

    def "create as-block with RIPE mnt-by, mnt-by pw supplied, no as-block end value"() {

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS655
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS655" }
        ack.errorMessagesFor("Create", "[as-block] AS655") ==
                ["Syntax error in AS655"]
        queryObjectNotFound("-rGBT as-block AS655", "as-block", "AS655")

    }

    def "create as-block with RIPE mnt-by, with override"() {
        expect:
        queryObjectNotFound("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
        def message = syncUpdate("""\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST
                override:       denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)

        ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
        ack.infoSuccessMessagesFor("Create", "[as-block] AS222 - AS333") == [
                "Authorisation override used"]

        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
    }

    def "create as-block with RIPE mnt-by, no mnt-by pw supplied"() {
        expect:
        queryObjectNotFound("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(3, 0, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
        ack.errorMessagesFor("Create", "[as-block] AS222 - AS333") == [
                "Authorisation for [as-block] AS222 - AS333 failed using \"mnt-by:\" not authenticated by: RIPE-DBM-MNT",
                "As-block object are maintained by RIPE NCC",
                "You cannot add or remove a RIPE NCC maintainer"
        ]
        queryObjectNotFound("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")

    }

    def "create as-block with RIPE mnt-by, mnt-by pw supplied, syntax error negative second value"() {

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS655 - AS-2
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS655 - AS-2" }
        ack.errorMessagesFor("Create", "[as-block] AS655 - AS-2") ==
                ["Syntax error in AS655 - AS-2"]
        queryObjectNotFound("-rGBT as-block AS655 - AS-2", "as-block", "AS655 - AS-2")

    }

    def "create as-block with RIPE mnt-by, mnt-by pw supplied, syntax error negative first value"() {

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS-1 - AS655
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS-1 - AS655" }
        ack.errorMessagesFor("Create", "[as-block] AS-1 - AS655") ==
                ["Syntax error in AS-1 - AS655"]
        queryObjectNotFound("-rGBT as-block AS-1 - AS655", "as-block", "AS-1 - AS655")

    }

    def "create as-block with RIPE mnt-by, with range of single value"() {
        expect:
            queryObjectNotFound("-r -T as-block AS222 - AS222", "as-block", "AS222 - AS222")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS222
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS222" }
            queryObject("-rGBT as-block AS222 - AS222", "as-block", "AS222 - AS222")

    }

    def "create as-block with non-RIPE mnt-by, mnt-by pw supplied"() {
        expect:
            queryObjectNotFound("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         TST-MNT2
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: test2
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)
            ack.countErrorWarnInfo(2, 1, 0)

            ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
            ack.errorMessagesFor("Create", "[as-block] AS222 - AS333") == [
                    "As-block object are maintained by RIPE NCC",
                    "You cannot add or remove a RIPE NCC maintainer"
            ]
            queryObjectNotFound("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
    }

    def "create as-block, with RIPE mnt-by, mnt-by pw supplied, reversed values"() {

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS333 - AS222
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS333 - AS222" }
        ack.errorMessagesFor("Create", "[as-block] AS333 - AS222") ==
                ["Syntax error in AS333 - AS222"]
        queryObjectNotFound("-rGBT as-block AS333 - AS222", "as-block", "AS333 - AS222")

    }

    def "create as-block with RIPE mnt-by, mnt-by pw supplied, diff mnt-lower"() {

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      LIR-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }

            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")

    }

    def "modify as-block, change mnt-by, old mnt-by pw supplied, new mnt-by pw not supplied"() {

        given:
            syncUpdate(getTransient("RIPE-DBM-STARTUP-MNT") + "password:startup\noverride:       denis,override1")
            syncUpdate(getTransient("AS222 - AS333") + "password: dbm\npassword: owner3")

        expect:
            queryObject("-rBGT mntner RIPE-DBM-STARTUP-MNT", "mntner", "RIPE-DBM-STARTUP-MNT")
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-STARTUP-MNT
                mnt-lower:      RIPE-DBM-STARTUP-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Modify" && it.key == "[as-block] AS222 - AS333" }
            query_object_matches("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333", "mnt-by:\\s*RIPE-DBM-STARTUP-MNT")
    }

    def "modify as-block, add org, change remarks"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password: dbm\npassword: owner3")

        expect:
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                org:            ORG-LIR1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                password: owner3
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Modify" && it.key == "[as-block] AS222 - AS333" }
            query_object_matches("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333", "org:\\s*ORG-LIR1-TEST")
    }

    def "modify as-block, add multiple mnt-by"() {

        given:
        syncUpdate(getTransient("RIPE-DBM-STARTUP-MNT") + "password:startup\noverride:       denis,override1")
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\npassword: owner3")
        expect:
        queryObject("-rBGT mntner RIPE-DBM-STARTUP-MNT", "mntner", "RIPE-DBM-STARTUP-MNT")
        queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
        def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-by:         RIPE-DBM-STARTUP-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[as-block] AS222 - AS333" }
        query_object_matches("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333", "mnt-by:\\s*RIPE-DBM-STARTUP-MNT")
    }

    def "modify as-block, add admin-c and add tech-c"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\npassword: owner3")
        expect:
        queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[as-block] AS222 - AS333" }
        ack.errorMessagesFor("Modify", "[as-block] AS222 - AS333") == [
                "\"admin-c\" is not valid for this object type",
                "\"tech-c\" is not valid for this object type"
        ]
        query_object_not_matches("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333", "admin-c:\\s*TP1-TEST")
        query_object_not_matches("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333", "tech-c:\\s*TP1-TEST")

    }

    def "delete as-block"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password: dbm\npassword: owner3")

        expect:
            queryObject("-rBG -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST
                delete:         reason

                password:   dbm
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 0, 1, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Delete" && it.key == "[as-block] AS222 - AS333" }
            queryObjectNotFound("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
    }

    def "create as-block, with full 16 bit range"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS0 - AS65535
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS0 - AS65535" }
            queryObject("-rGBT as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
    }

    def "create as-block, crossing 16/32 bit ranges"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS655 - AS7775535", "as-block", "AS655 - AS7775535")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS655 - AS7775535
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS655 - AS7775535" }
            queryObject("-rGBT as-block AS655 - AS7775535", "as-block", "AS655 - AS7775535")
    }

    def "create as-block with hierarchy parent exists"() {
        expect:
        queryObjectNotFound("-rBG -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T as-block AS250 - AS300", "as-block", "AS250 - AS300")

        when:
        def message = syncUpdate("""\
                as-block:       AS222 - AS333
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                as-block:       AS250 - AS300
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS250 - AS300" }
        ack.errorMessagesFor("Create", "[as-block] AS250 - AS300") ==
                ["Parent As-block already exists"]

        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rGBT as-block AS250 - AS300", "as-block", "AS250 - AS300")
    }

    def "create as-block with hierarchy child exists"() {
        expect:
        queryObjectNotFound("-rBG -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T as-block AS250 - AS300", "as-block", "AS250 - AS300")

        when:
        def message = syncUpdate("""\
                as-block:       AS250 - AS300
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                as-block:       AS222 - AS333
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS250 - AS300" }
        ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
        ack.errorMessagesFor("Create", "[as-block] AS222 - AS333") ==
                ["Child As-block already exists"]

        queryObject("-rGBT as-block AS250 - AS300", "as-block", "AS250 - AS300")
        queryObjectNotFound("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
    }

    def "create overlapping as-block scenario1"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T as-block AS250 - AS350", "as-block", "AS250 - AS350")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS250 - AS350
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                as-block:       AS222 - AS333
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 2
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)
            ack.countErrorWarnInfo(1, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS250 - AS350" }
            ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
            ack.errorMessagesFor("Create", "[as-block] AS222 - AS333") ==
                    ["Overlapping As-block already exists"]

            queryObject("-rGBT as-block AS250 - AS350", "as-block", "AS250 - AS350")
            queryObjectNotFound("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
    }

    def "create overlapping as-block scenario2 "() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T as-block AS250 - AS350", "as-block", "AS250 - AS350")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS222 - AS333
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                as-block:       AS250 - AS350
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 2
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)
            ack.countErrorWarnInfo(1, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS222 - AS333" }
            ack.errors.any { it.operation == "Create" && it.key == "[as-block] AS250 - AS350" }
            ack.errorMessagesFor("Create", "[as-block] AS250 - AS350") ==
                    ["Overlapping As-block already exists"]

            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rGBT as-block AS250 - AS350", "as-block", "AS250 - AS350")
    }

    def "create as-block ranging from 16 bit to 32 bit number"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS777 - AS7775535", "as-block", "AS777 - AS7775535")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS777 - AS7775535
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS777 - AS7775535" }
            queryObject("-rGBT as-block AS777 - AS7775535", "as-block", "AS777 - AS7775535")
    }

    def "create as-block ranging from AS0-AS0"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS0-AS0", "as-block", "AS0-AS0")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS0-AS0
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS0-AS0" }
            queryObject("-rGBT as-block AS0-AS0", "as-block", "AS0-AS0")
    }

    def "create as-block ranging from AS4294967295 - AS4294967295"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS4294967295 - AS4294967295", "as-block", "AS4294967295 - AS4294967295")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS4294967295 - AS4294967295
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS4294967295 - AS4294967295" }
            queryObject("-rGBT as-block AS4294967295 - AS4294967295", "as-block", "AS4294967295 - AS4294967295")
    }

    def "create as-block covering full range"() {
        expect:
            queryObjectNotFound("-rBG -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")

        when:
            def ack = syncUpdateWithResponse("""
                as-block:       AS0 - AS4294967295
                descr:          ASN block
                remarks:        some comment
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST

                password: dbm

                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)

            ack.successes.any { it.operation == "Create" && it.key == "[as-block] AS0 - AS4294967295" }
            queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
    }
}
