package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message
import spock.lang.Ignore

class Inet6numSpec extends BaseSpec {

    @Override
    Map<String, String> getTransients() {
        [
                "RIR-ALLOC-20": """\
                inet6num:     2001::/20
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "RIR-ALLOC-25": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "USER-RIR-ALLOC-25": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "RIR-ALLOC-25-NO-LOW": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "RIR-ALLOC-25-LOW-R-D": """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    lir-MNT
                mnt-lower:    owner2-MNT
                mnt-ROUTES:   lir2-MNT
                mnt-DOMAINS:  lir3-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-ALLOC-30": """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-AGGR-32-48": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-AGGR-32-50": """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 50
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-AGGR-48-56": """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:56
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-AGGR-48-64": """\
                inet6num:     2001:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "LIR-AGGR-56-64": """\
                inet6num:     2001:600::/56
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size:64
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "ASS-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
                "ASSPI-64": """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                """,
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
                """,
                "IRT": """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$d9fKeTr2\$Si7YudNf4rUGmR71n/cqk/  #test
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """
        ]
    }

    // Create 0::/0 without override
    @Ignore
    def "create 0::/0 without override"() {
        expect:
        queryObjectNotFound("-r -T inet6num ::/0", "inet6num", "::/0")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     0::/0
                netname:      IANA-BLK
                descr:        The whole IPv6 address space
                country:      EU # Country is really world wide
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                mnt-routes:   RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] ::/0" }
        ack.errorMessagesFor("Create", "[inet6num] ::/0") == [
                "There is no parent object"]
        ack.infoMessagesFor("Create", "[inet6num] ::/0") == [
                "Value 0::/0 converted to ::/0"]

        queryObjectNotFound("-r -T inet6num ::/0", "inet6num", "::/0")
    }

    // Create 0::/0 with override
    @Ignore
    def "create 0::/0 with override"() {
        expect:
        queryObjectNotFound("-r -T inet6num 0::/0", "inetnum", "0::/0")

        when:
        def message = syncUpdate("""\
                inet6num:      0::/0
                netname:      IANA-BLK
                descr:        The whole IPv6 address space
                country:      EU # Country is really world wide
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                mnt-routes:   owner-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 0.0.0.0 - 255.255.255.255" }

        queryObject("-r -T inetnum 0/0", "inetnum", "0/0")
    }

    def "modify with invalid prefix 1::/0"() {
        expect:
        queryObject("-r -T inet6num ::/0", "inet6num", "::/0")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     1::/0
                netname:      IANA-BLK
                descr:        The whole IPv6 address space
                country:      EU # Country is really world wide
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                mnt-routes:   RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] ::/0" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] ::/0") == [
                "Value 1::/0 converted to ::/0"]

        queryObject("-r -T inet6num ::/0", "inet6num", "::/0")
    }

    def "create allocation with leading zero"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:0600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Value 2001:0600::/25 converted to 2001:600::/25"]

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create /64 assignment with all zeroes"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:0:0:0:0:0:0/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Value 2001:600:0:0:0:0:0:0/64 converted to 2001:600::/64"]

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with all bits, 1 at end"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600:1:1::/64", "inet6num", "2001:600:1:1::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:1:1:1:1:1:1/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1:1::/64" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600:1:1::/64") ==
                ["Value 2001:600:1:1:1:1:1:1/64 converted to 2001:600:1:1::/64"]

        queryObject("-rGBT inet6num 2001:600:1:1::/64", "inet6num", "2001:600:1:1::/64")
    }

    def "create /64 assignment with all bits, 1 at end, then delete with same format"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600:1:1::/64", "inet6num", "2001:600:1:1::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:1:1:1:1:1:1/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                inet6num:     2001:600:1:1:1:1:1:1/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:  check address format

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600:1:1::/64" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600:1:1::/64") ==
                ["Value 2001:600:1:1:1:1:1:1/64 converted to 2001:600:1:1::/64"]
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600:1:1::/64" }
        ack.infoSuccessMessagesFor("Delete", "[inet6num] 2001:600:1:1::/64") ==
                ["Value 2001:600:1:1:1:1:1:1/64 converted to 2001:600:1:1::/64"]

        queryObjectNotFound("-rGBT inet6num 2001:600:1:1::/64", "inet6num", "2001:600:1:1::/64")
    }

    def "create /64 assignment with most zeros, gap in middle"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:0:0::0:0/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Value 2001:600:0:0::0:0/64 converted to 2001:600::/64"]

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /128 assignment with all zeroes and modify it"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/128")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:0:0:0:0:0:0/128
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                inet6num:     2001:600:0:0:0:0:0:0/128
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                remarks:      just added
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/128" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/128") ==
                ["Value 2001:600:0:0:0:0:0:0/128 converted to 2001:600::/128"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/128" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001:600::/128") ==
                ["Value 2001:600:0:0:0:0:0:0/128 converted to 2001:600::/128"]

        queryObject("-rGBT inet6num 2001:600::/128", "inet6num", "2001:600::/128")
    }

    def "create /128 assignment with all bits, 1 at end"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::1/128", "inet6num", "2001:600::1/128")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600:0:0:0:0:0:1/128
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::1/128" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::1/128") ==
                ["Value 2001:600:0:0:0:0:0:1/128 converted to 2001:600::1/128"]

        queryObject("-rGBT inet6num 2001:600::1/128", "inet6num", "2001:600::1/128")
    }

    def "create allocation with invalid legacy status"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       SUBTLA
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Syntax error in SUBTLA"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create allocation with invalid unknown status"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       frED
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Syntax error in frED"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create allocation with invalid IPv4 status"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       ALLOCATED PA
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Syntax error in ALLOCATED PA"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create allocation, parent has invalid legacy status"() {
        expect:
        queryObjectNotFound("-r -T inet6num 1981:600::/48", "inet6num", "1981:600::/48")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     1981:600::/48
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                status:       ALLOCATED-BY-LIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 1981:600::/48" }
        ack.errorMessagesFor("Create", "[inet6num] 1981:600::/48") ==
                ["Parent 1981:600::/25 has invalid status: SUBTLA"]

        queryObjectNotFound("-rGBT inet6num 1981:600::/48", "inet6num", "1981:600::/48")
    }

    def "create RIR allocation not mnt-by RS"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/25") ==
                ["Status ALLOCATED-BY-RIR can only be created by the database administrator"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create RIR allocation, mnt-by RS"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create RIR allocation, joint mnt-by RS & LIR, using RS password"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create RIR allocation, joint mnt-by RS & LIR, using LIR password"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/30") == [
                "Adding or removing a RIPE NCC maintainer requires administrative authorisation",
                "Status ALLOCATED-BY-RIR can only be created by the database administrator"
        ]

        queryObjectNotFound("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "modify RIR allocation, joint mnt-by RS & LIR, using LIR password"() {
        given:
        syncUpdate(getTransient("USER-RIR-ALLOC-25") + "override: override1")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                remarks:  just
                 added
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/25" }

        query_object_matches("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25", "just")
    }

    def "create RIR allocation, joint mnt-by RS & LIR, using override"() {
        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-by:       liR-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST
                override:  override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/25" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/25") == [
                "Authorisation override used"]

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "delete with invalid legacy status"() {
        expect:
        queryObject("-r -T inet6num 1981:600::/25", "inet6num", "1981:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     1981:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                status:       SUBTLA
                changed:      dbtest@ripe.net
                source:       TEST
                delete:       old status

                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 1981:600::/25" }

        queryObjectNotFound("-rGBT inet6num 1981:600::/25", "inet6num", "1981:600::/25")
    }

    def "delete assignment"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("ASS-64") + "password: lir\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       ass

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }

        queryObjectNotFound("-rGBT inet6num 1981:600::/64", "inet6num", "2001:600::/64")
    }

    def "delete PI assignment, lir pw"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       ass

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Delete", "[inet6num] 2001:600::/64") ==
                ["Deleting this object requires administrative authorisation"]

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "delete PI assignment, RS pw"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       ass

                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/64" }

        queryObjectNotFound("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "delete allocation, lir pw"() {
        given:
        syncUpdate(getTransient("USER-RIR-ALLOC-25") + "password: hm\npassword: owner3\noverride: override1")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       ass

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }
        ack.errorMessagesFor("Delete", "[inet6num] 2001:600::/25") ==
                ["Deleting this object requires administrative authorisation"]

        queryObject("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "delete allocation, RS pw"() {
        given:
        syncUpdate(getTransient("USER-RIR-ALLOC-25") + "password: hm\npassword: owner3\noverride: override1")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       ass

                password: hm
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "delete allocation, override"() {
        given:
        syncUpdate(getTransient("USER-RIR-ALLOC-25") + "override: override1\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/25
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:       alloc
                override:  override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/25" }
        ack.infoSuccessMessagesFor("Delete", "[inet6num] 2001:600::/25") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/25", "inet6num", "2001:600::/25")
    }

    def "create RIR allocation, joint mnt-by RS & LIR, using RS password, parent no mnt-lower"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25-NO-LOW") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }

        queryObject("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "create RIR allocation, joint mnt-by RS & LIR, using RS password, parent has mnt-lower, no mnt-lower pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR2-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/30") ==
                ["Authorisation for parent [inet6num] 2001:600::/25 failed using \"mnt-lower:\" not authenticated by: LIR-MNT"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "create RIR allocation, parent has mnt-lower, MNT-ROUTES, MNT-DOMAINS, mnt-ROUTES,MNT-DOMAINS pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR2-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: lir2
                password: lir3
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/30") ==
                ["Authorisation for parent [inet6num] 2001:600::/25 failed using \"mnt-lower:\" not authenticated by: LIR-MNT, OWNER2-MNT"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "create RIR allocation, parent has mnt-lower, MNT-ROUTES, MNT-DOMAINS, mnt-lower pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    liR2-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }

        queryObject("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "create RIR allocation, obj has 2 RS mnt-by, parent has 2 mnt-lower, MNT-ROUTES, MNT-DOMAINS, 2nd mnt-by and 2nd mnt-lower pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-by:       ripe-ncc-hm2-MNT
                mnt-lower:    liR2-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: hm2
                password: owner2
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }

        queryObject("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "modify RIR allocation, obj has 2 RS mnt-by, parent has 2 mnt-lower, MNT-ROUTES, MNT-DOMAINS, 2nd lir mnt-by and no parent pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25-LOW-R-D") + "password: hm\npassword: owner3\n")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-by:       ripe-ncc-hm2-MNT
                mnt-by:       liR2-MNT
                mnt-by:       liR3-MNT
                mnt-lower:    liR2-MNT
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-by:       ripe-ncc-hm2-MNT
                mnt-by:       liR2-MNT
                mnt-by:       liR3-MNT
                mnt-lower:    liR2-MNT
                remarks:      just added
                +now
                status:       ALLOCATED-BY-RIR
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/30" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/30") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/30" }

        query_object_matches("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30", "now")
    }

    def "modify change status"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: lir\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 0, 2, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/32" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/32") ==
                ["status value cannot be changed, you must delete and re-create the object"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/30" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001:600::/30") ==
                ["status value cannot be changed, you must delete and re-create the object"]

        query_object_matches("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32", "AGGREGATED-BY-LIR")
        query_object_matches("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30", "ALLOCATED-BY-LIR")
    }

    def "modify change status with override"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: lir\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                override:  override1

                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/32" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001:600::/32") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/30" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001:600::/30") == [
                "Authorisation override used"]

        query_object_matches("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32", "ASSIGNED")
        query_object_matches("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30", "ASSIGNED")
    }

    def "delete and re-create to change status"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("LIR-AGGR-32-48") + "password: lir\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/32", "inet6num", "2001:600::/32")

        when:
        def message = syncUpdate("""\
                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       AGGREGATED-BY-LIR
                assignment-size: 48
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:   change status

                inet6num:     2001:600::/30
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-lower:    LiR-MNT
                status:       ALLOCATED-BY-LIR
                changed:      dbtest@ripe.net 20130101
                source:       TEST
                delete:  in the way

                inet6num:     2001:600::/32
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       ripe-ncc-hm-MNT
                mnt-lower:    LiR-MNT
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 1, 0, 2, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/32" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inet6num] 2001:600::/30" }
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/32" }

        query_object_matches("-rGBT inet6num 2001:600::/32", "inet6num", "2001:600::/32", "ASSIGNED PI")
        queryObjectNotFound("-rGBT inet6num 2001:600::/30", "inet6num", "2001:600::/30")
    }

    def "create /64 assignment with mnt-routes no op data"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-routes:   routes-mnt
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with mnt-routes op data ANY"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-routes:   routes-mnt ANY
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with mnt-routes op data exact match"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-routes:   routes-mnt { 2001:600::/64 }
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with mnt-routes op data exact match split"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-routes:   routes-mnt { 2001:600::/65, 2001:600:0:0:8000::/65 }
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with IRT ref, irt pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("IRT") + "password: owner")
        queryObject("-r -T irt irt-test", "irt", "irt-test")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-irt:      irt-test
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                password: test
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with IRT ref, no irt pw supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("IRT") + "password: owner")
        queryObject("-r -T irt irt-test", "irt", "irt-test")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                mnt-irt:      irt-test
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Authorisation for [inet6num] 2001:600::/64 failed using \"mnt-irt:\" not authenticated by: irt-test"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment, syntactically incorrect netname"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      123EU-ZZ-2001-0600-_
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Syntax error in 123EU-ZZ-2001-0600-_"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment, in range notation"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff") ==
                ["Syntax error in 2001:0600:0000:0000:0000:0000:0000:0000 - 2001:0600:0000:0000:ffff:ffff:ffff:ffff"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment, invalid address"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:GHJ::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       lir-MNT
                status:       ASSIGNED
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inet6num] 2001:GHJ::/64" }
        ack.errorMessagesFor("Create", "[inet6num] 2001:GHJ::/64") ==
                ["Syntax error in 2001:GHJ::/64"]

        queryObjectNotFound("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "create /64 assignment with all optional attrs supplied"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-25") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/25", "inet6num", "2001:600::/25")
        syncUpdate(getTransient("LIR-ALLOC-30") + "password: lir\npassword: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/30", "inet6num", "2001:600::/30")
        syncUpdate(getTransient("IRT") + "password: owner")
        queryObject("-r -T irt irt-test", "irt", "irt-test")

        expect:
        queryObjectNotFound("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64# primary key comment
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                descr:        second line
                language:      nl
                mnt-domains:    owner2-mnt, owner-mnt
                country:      NL
                mnt-routes:    owner2-mnt
                admin-c:      TP1-TEST
                admin-c:      TP1-TEST
                admin-c:      TP1-TEST
                remarks:      early comment
                mnt-lower:    owner-mnt
                geoloc:      10.568 158.552
                tech-c:       TP1-TEST
                org:          ORG-OTO1-TEST
                mnt-by:       lir-MNT
                mnt-irt:      irt-test
                status:       AGGREGATED-BY-LIR  #this is # aggregated space
                assignment-size: 65
                mnt-lower:    owner2-mnt
                mnt-domains:    owner-mnt
                notify:       test-dbtest@ripe.net
                mnt-by:       END-USER-MNT, owner-mnt
                language:      EN
                mnt-routes:    owner-mnt ANY
                mnt-by:       owner2-mnt
                notify:       test2-dbtest@ripe.net
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                changed:      dbtest@ripe.net 20130101
                changed:      dbtest@ripe.net 20130101
                changed:      dbtest@ripe.net
                source:       TEST
                tech-c:       TP3-TEST
                admin-c:      TP2-TEST
                remarks:      late comment

                password: hm
                password: end
                password: test
                password: owner3
                password: lir
                password: test
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inet6num] 2001:600::/64" }
        ack.infoSuccessMessagesFor("Create", "[inet6num] 2001:600::/64") ==
                ["Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64")
    }

    def "modify PI assignment, LIR pw"() {
        given:
        syncUpdate(getTransient("RIR-ALLOC-20") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001::/20", "inet6num", "2001::/20")
        syncUpdate(getTransient("ASSPI-64") + "password: hm\npassword: owner3")
        queryObject("-r -T inet6num 2001:600::/64", "inet6num", "2001:600::/64")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet6num:     2001:600::/64
                netname:      EU-ZZ-2001-0600
                descr:        European Regional Registry
                country:      EU
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                status:       ASSIGNED PI
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: lir
                """.stripIndent()
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001:600::/64" }

        query_object_matches("-rGBT inet6num 2001:600::/64", "inet6num", "2001:600::/64", "just added")
    }

}
