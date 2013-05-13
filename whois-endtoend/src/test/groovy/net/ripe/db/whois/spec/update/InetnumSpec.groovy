package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import spec.domain.AckResponse
import spec.domain.Message
import spock.lang.Ignore

class InetnumSpec extends BaseSpec {

    @Override
    Map<String, String> getFixtures() {
        [
                "TEST-PN": """\
                person:  Test Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: TP1-TEST
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                """,
        ]
    }

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
                abuse-mailbox: dbtest-abuse@ripe.net
                mnt-by:  OWNER-MNT
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
                "NO-MB-PN": """\
                person:  First Person
                address: St James Street
                address: Burnley
                address: UK
                phone:   +44 282 420469
                nic-hdl: FP1-TEST
                changed: denis@ripe.net 20121016
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
                changed: dbtest@ripe.net 20121016
                source:  TEST
                """,
                "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-mnt
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-NO-LOW": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-NO-LOW-R": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-routes:   LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-NO-LOW-D": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-domains:  LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-LOW-LIST": """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT, LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-LOW-R": """\
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
                mnt-routes:   LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-LOW-R-D": """\
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
                mnt-routes:   LIR2-MNT
                mnt-domains:  LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "P-192-8": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   LIR2-MNT
                mnt-domains:  LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "JOINT-ASS": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "PART-PA": """\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "SUB-LOW-R-D": """\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       SUB-MNT
                mnt-lower:    LIR-MNT
                mnt-routes:   LIR2-MNT
                mnt-domains:  LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASS-END": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "ASSPI": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "EARLY": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "EARLY-USER": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
                "EARLY-USER-ONLY": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
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

    // Create 0/0 without override
    @Ignore
    def "create 0/0 without override"() {
      expect:
        queryObjectNotFound("-r -T inetnum 0/0", "inetnum", "0/0")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      0.0.0.0 - 255.255.255.255
                netname:      IANA-BLK
                descr:        The whole IPv4 address space
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                mnt-routes:   owner-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.failed

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 0.0.0.0 - 255.255.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 0.0.0.0 - 255.255.255.255") == [
                "There is no parent object"]

        queryObjectNotFound("-r -T inetnum 0/0", "inetnum", "0/0")
    }

    // Create 0/0 with override
    @Ignore
    def "create 0/0 with override"() {
      expect:
        queryObjectNotFound("-r -T inetnum 0/0", "inetnum", "0/0")

      when:
        def message = syncUpdate("""\
                inetnum:      0.0.0.0 - 255.255.255.255
                netname:      IANA-BLK
                descr:        The whole IPv4 address space
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                remarks:      The country is really worldwide.
                remarks:      This address space is assigned at various other places in
                remarks:      the world and might therefore not be in the RIPE database.
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
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

    def "create ALLOCATED UNSPECIFIED, with RS mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }

        queryObject("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "create ALLOCATED UNSPECIFIED, with LIR mntner, lir password"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword:hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
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

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Status ALLOCATED UNSPECIFIED can only be created by the database administrator"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied, mnt-by using second alloc mntner"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword:hm")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM2-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm2
                password: owner2
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied, obj mnt-by not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                password: hm
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation PI, parent with mnt-lower, parent mnt-lower pw supplied, obj mnt-by not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                password: hm
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation UNSPECIFIED, parent mnt-lower alloc mntner, parent mnt-lower pw supplied, obj mnt-by not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        ack.errorMessagesFor("Create", "[inetnum] 192.168.128.0 - 192.168.255.255") ==
                ["Status ALLOCATED UNSPECIFIED can only be created by the database administrator"]

        queryObjectNotFound("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation, parent with mnt-lower pw supplied, obj 2 mnt-by 1 not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation PI, parent with mnt-lower, parent mnt-lower pw supplied"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation    UNSPECIFIED with spaces, parent with mnt-lower, parent mnt-lower pw supplied"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED    UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create assignment, parent no mnt-lower, parent mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("P-NO-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, diff pw to mnt-by, parent mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower list, diff pw to mnt-by, parent 2nd mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir2
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, diff pw to mnt-by, parent mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-lower", "LIR-MNT,\\s*LIR2-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with no mnt-lower, no parent pw supplied"() {
      given:
        syncUpdate(getTransient("P-NO-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-by", "RIPE-NCC-HM-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, parent pw supplied, no assignment pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "END-USER-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, no parent pw supplied, no assignment pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: fred
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-lower", "LIR-MNT")
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "END-USER-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
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

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation override used"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower, diff pw to mnt-by, no pw supplied, wrong override pw used"() {
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
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: fred

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Override authentication failed"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, overlapping with 4 parents"() {
      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      62.59.192.0 - 62.59.192.7
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.8 - 62.59.192.15
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.24 - 62.59.192.31
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.32 - 62.59.192.39
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.2 - 62.59.192.30
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 62.59.192.2 - 62.59.192.30" }
        ack.errorMessagesFor("Create", "[inetnum] 62.59.192.2 - 62.59.192.30") ==
                ["This range overlaps with 62.59.192.0 - 62.59.192.7"]

        queryObjectNotFound("-rGBT inetnum 62.59.192.2 - 62.59.192.30", "inetnum", "62.59.192.2 - 62.59.192.30")
    }

    def "create assignment, overlapping with 4 parents, with override"() {
      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 62.59.192.30", "inetnum", "62.59.192.2 - 62.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      62.59.192.0 - 62.59.192.7
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.8 - 62.59.192.15
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.24 - 62.59.192.31
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.32 - 62.59.192.39
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      62.59.192.2 - 62.59.192.30
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: override1

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 62.59.192.2 - 62.59.192.30" }
        ack.errorMessagesFor("Create", "[inetnum] 62.59.192.2 - 62.59.192.30") ==
                ["This range overlaps with 62.59.192.0 - 62.59.192.7"]
        ack.infoMessagesFor("Create", "[inetnum] 62.59.192.2 - 62.59.192.30") ==
                ["Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 62.59.192.2 - 62.59.192.30", "inetnum", "62.59.192.2 - 62.59.192.30")
    }

    def "create assignment, parent with mnt-lower, /24"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200/24
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Value 192.168.200/24 converted to 192.168.200.0 - 192.168.200.255"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create LIR-PARTITIONED PA, parent and child with mnt-lower, pw supplied for parent and child mnt-lower"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 partition
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: lir3
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "LIR2-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create LIR-PARTITIONED PA, parent and child with mnt-lower, pw supplied for parent mnt-lower and child mnt-by"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 partition
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create SUB-ALLOCATED PA, parent and child with mnt-lower, pw supplied for parent and child mnt-lower"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 partition
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR3-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: lir3
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "LIR2-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower mnt-routes, pw supplied parent mnt-routes object mnt-by"() {
      given:
        syncUpdate(getTransient("P-LOW-R") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir2
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-lower", "LIR-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-routes no mnt-lower, pw supplied parent mnt-routes object mnt-by"() {
      given:
        syncUpdate(getTransient("P-NO-LOW-R") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir2
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-by", "RIPE-NCC-HM-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-domains no mnt-lower, pw supplied parent mnt-domains object mnt-by"() {
      given:
        syncUpdate(getTransient("P-NO-LOW-D") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir2
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "parent", "inetnum", "192.168.128.0 - 192.168.255.255", "mnt-by", "RIPE-NCC-HM-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent with mnt-lower mnt-routes mnt-domains, diff pw to mnt-by, parent mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent 2 mnt-lower, obj 2 mnt-by, 1st of each pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       owner-mnt
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, parent 2 mnt-lower, obj 2 mnt-by, 2nd of each pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       owner-mnt
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir2
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "modify assignment, parent with mnt-lower mnt-routes mnt-domains, diff pw to mnt-by, no parent pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "20020101")
    }

    def "modify assignment, parent with mnt-lower, diff pw to mnt-by, parent mnt-lower pw supplied no obj mnt-by pw"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Modify", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "END-USER-MNT")

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "20020101")
    }

    def "modify assignment, parent with mnt-lower, /24"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200/24
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Value 192.168.200/24 converted to 192.168.200.0 - 192.168.200.255"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "20020101")
    }

    def "delete assignment, parent with mnt-lower mnt-routes mnt-domains, diff pw to mnt-by, no parent pw supplied, assignment pw supplied"() {
      given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:  testing

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "delete assignment, parent with mnt-lower mnt-routes mnt-domains, diff pw to mnt-by, all parent pw supplied, no obj mnt-by pw"() {
      given:
        syncUpdate(getTransient("P-NO-LOW") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("SUB-LOW-R-D") + "password: sub\npassword: owner3\npassword: hm")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:  testing

                password: sub
                password: lir
                password: lir2
                password: lir3
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Delete", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "END-USER-MNT")

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes no op data"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data ANY"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { ANY }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data ANY, bareword"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt ANY
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt ANY"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "routes-mnt \\{ 192.168.200.0/24 \\}")
    }

    def "create assignment, mnt-routes op data exact match split"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/25, 192.168.200.128/25 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match split 2 same mntners"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/25 }
                mnt-routes:   routes-mnt { 192.168.200.128/25 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match split 2 diff mntners"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/25 }
                mnt-routes:   owner-mnt { 192.168.200.128/25 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match split 2 diff mntners, list"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/25 }, owner-mnt { 192.168.200.128/25 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Syntax error in routes-mnt { 192.168.200.0/25 }, owner-mnt { 192.168.200.128/25 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match split, cont line attr"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt
                              {
                +              192.168.200.0/25,
                              192.168.200.128/25
                              }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data exact match split, space before no spaces in brackets"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt {192.168.200.0/25,192.168.200.128/25}
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data sub range"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/25 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data ANY + prefix"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { ANY,192.168.200.0/24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { ANY,192.168.200.0/24 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data prefix + ANY"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/24,ANY }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { 192.168.200.0/24,ANY }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data prefix + ANY split"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/24 }
                mnt-routes:   routes-mnt { ANY }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Syntax error in routes-mnt { 192.168.200.0/24 } (ANY can only occur as a single value)",
                "Syntax error in routes-mnt { ANY } (ANY can only occur as a single value)"
        ]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data ANY + ANY"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { ANY,ANY,ANY,ANY }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)

        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { ANY,ANY,ANY,ANY }"]
    }

    def "create assignment, mnt-routes op data any"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { any }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data greater range"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["192.168.0.0/16 is outside the range of this object"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data invalid prefix"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.201/24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { 192.168.201/24 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data invalid prefix boundary"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.2.3/16 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid prefix >32"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0/33 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { 192.168.200.0/33 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data IPv6"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 200:168::/48 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["200:168::/48 is not a valid IPv4 address"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data invalid IPv4 addr"() {
      given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.200.0.0/24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Syntax error in routes-mnt { 192.168.200.0.0/24 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment, mnt-routes op data invalid ^"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/24^ }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/24^ }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^n"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^n equal"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^16 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid ^n"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/24^16 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/24^16 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid ^n >32"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^48 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/16^48 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^n-m"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^20-24 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^n-m equal"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^20-20 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^n-m equal max"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/32^32-32 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid ^n-m reversed"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^24-20 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/16^24-20 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid ^n-m overlap"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^15-17 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/16^15-17 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data invalid ^n-m >32"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^24-38 }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.0.0/16^24-38 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^-"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^- }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, mnt-routes op data valid ^+"() {
      given:
        syncUpdate(getTransient("P-192-8") + "password: hm\npassword: owner3")

      expect:
        queryObject("-GBr -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-routes:   routes-mnt { 192.168.0.0/16^+ }
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with IRT ref, irt pw supplied"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: end
                password: test
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with IRT ref, no irt pw"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.0.0 - 192.168.255.255", "", "inetnum", "192.168.0.0 - 192.168.255.255", "mnt-irt", "irt-test")

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with all optional attributes"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255  # primary key comment
                netname:      RIPE-NET1
                descr:        /16 assigned
                descr:        second line
                language:      nl
                mnt-domains:    owner2-mnt, owner-mnt
                country:      NL
                mnt-routes:    owner2-mnt
                admin-c:      TP1-TEST
                remarks:      early comment
                mnt-lower:    RIPE-NCC-HM-MNT, owner-mnt
                geoloc:      10.568 158.552
                mnt-irt:      irt-test
                tech-c:       TP1-TEST
                org:          ORG-OTO1-TEST
                status:       ASSIGNED PA  #this is # PA space
                mnt-lower:    owner2-mnt
                mnt-domains:    owner-mnt
                notify:       test-dbtest@ripe.net
                mnt-by:       END-USER-MNT, owner-mnt
                language:      EN
                admin-c:      TP1-TEST
                country:      SG
                mnt-routes:    owner-mnt {ANY}
                mnt-by:       owner2-mnt
                notify:       test2-dbtest@ripe.net
                mnt-irt:      irt-test
                changed:      dbtest@ripe.net 20020101
                changed:      dbtest@ripe.net
                source:       TEST
                tech-c:       TP3-TEST
                admin-c:      TP2-TEST
                remarks:      late comment

                password: hm
                password: end
                password: test
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Please use the \"remarks:\" attribute instead of end of line comment on primary key"]

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create ASSIGNED PA, syntactically incorrect netname"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      123TEST-NET-NAME-_
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in 123TEST-NET-NAME-_"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create invalid status"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.255 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       FRED
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["Syntax error in FRED"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")
    }

    def "create with IPv6 status"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.255 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       AGGREGATED-BY-LIR
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["Syntax error in AGGREGATED-BY-LIR"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")
    }

    def "create ASSIGNED PA, invalid range, reversed"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.255 - 192.168.0.0", "inetnum", "192.168.0.255 - 192.168.0.0")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.255 - 192.168.0.0
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.0.0" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.0.0") ==
                ["Syntax error in 192.168.0.255 - 192.168.0.0"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.0.0", "inetnum", "192.168.0.255 - 192.168.0.0")
    }

    def "create ASSIGNED PA, invalid range, value 266"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.255 - 192.168.255.266", "inetnum", "192.168.0.255 - 192.168.255.266")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.255 - 192.168.255.266
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.266" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.255.266") ==
                ["Syntax error in 192.168.0.255 - 192.168.255.266"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.255.266", "inetnum", "192.168.0.255 - 192.168.255.266")
    }

    def "create ASSIGNED PA, range of 1 IP, then modify it"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.100 - 192.168.0.100", "inetnum", "192.168.0.100 - 192.168.0.100")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.100 - 192.168.0.100
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.100 - 192.168.0.100
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20130101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.100 - 192.168.0.100" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.100 - 192.168.0.100" }

        query_object_matches("-rGBT inetnum 192.168.0.100 - 192.168.0.100", "inetnum", "192.168.0.100 - 192.168.0.100", "20130101")
    }

    def "create ASSIGNED PA, with tabs and spaces"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.100 - 192.168.0.100", "inetnum", "192.168.0.100 - 192.168.0.100")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.100 \t  -   \t  192.168.0.100
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.100 - 192.168.0.100" }

        queryObject("-rGBT inetnum 192.168.0.100 - 192.168.0.100", "inetnum", "192.168.0.100 - 192.168.0.100")
    }

    def "create LIR-PARTITIONED PA containing 2 existing assingments"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.0.255", "inetnum", "192.168.0.0 - 192.168.0.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.0.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.128 - 192.168.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.0 - 192.168.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.0.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.0.255", "inetnum", "192.168.0.0 - 192.168.0.255")
    }

    def "create assignment within LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.190 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.191 - 192.169.0.194
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.191 - 192.169.0.194" }

        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.195", "inetnum", "192.168.0.190 - 192.169.0.195")
        queryObject("-rGBT inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")
    }

    def "create assignment within LIR-PARTITIONED PA, begin IPs match"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.190 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.190 - 192.169.0.194
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.190 - 192.169.0.194" }

        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.195", "inetnum", "192.168.0.190 - 192.169.0.195")
        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.194", "inetnum", "192.168.0.190 - 192.169.0.194")
    }

    def "create LIR-PARTITIONED PA encompassing assignment, begin IPs match"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.190 - 192.169.0.194
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.190 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.190 - 192.169.0.195" }

        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.195", "inetnum", "192.168.0.190 - 192.169.0.195")
        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.194", "inetnum", "192.168.0.190 - 192.169.0.194")
    }

    def "create assignment within LIR-PARTITIONED PA, end IPs match"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.190 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.191 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.191 - 192.169.0.195" }

        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.195", "inetnum", "192.168.0.190 - 192.169.0.195")
        queryObject("-rGBT inetnum 192.168.0.191 - 192.169.0.195", "inetnum", "192.168.0.191 - 192.169.0.195")
    }

    def "create LIR-PARTITIONED PA encompassing assignment, end IPs match"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.191 - 192.169.0.194", "inetnum", "192.168.0.191 - 192.169.0.194")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.191 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.190 - 192.169.0.195
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.190 - 192.169.0.195" }

        queryObject("-rGBT inetnum 192.168.0.190 - 192.169.0.195", "inetnum", "192.168.0.190 - 192.169.0.195")
        queryObject("-rGBT inetnum 192.168.0.191 - 192.169.0.195", "inetnum", "192.168.0.191 - 192.169.0.195")
    }

    def "create 2 assignments, end1 = begin2"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.0.128
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.128 - 192.168.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.0.128" }
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.128 - 192.168.0.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.128 - 192.168.0.255") ==
                ["This range overlaps with 192.168.0.0 - 192.168.0.128"]

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.0.128", "inetnum", "192.168.0.0 - 192.168.0.128")
        queryObjectNotFound("-rGBT inetnum 192.168.0.128 - 192.168.0.255", "inetnum", "192.168.0.128 - 192.168.0.255")
    }

    def "create 2 assignments, end2 = begin1"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.128 - 192.168.0.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.0 - 192.168.0.128
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.128 - 192.168.0.255" }
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.0.128" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.0.128") ==
                ["This range overlaps with 192.168.0.128 - 192.168.0.255"]

        queryObject("-rGBT inetnum 192.168.0.128 - 192.168.0.255", "inetnum", "192.168.0.128 - 192.168.0.255")
        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.0.128", "inetnum", "192.168.0.0 - 192.168.0.128")
    }

    def "modify 3 objects, change status"() {
      given:
        syncUpdate(getTransient("EARLY") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(3, 0, 3, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["status value cannot be changed, you must delete and re-create the object"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.255.255") ==
                ["status value cannot be changed, you must delete and re-create the object"]
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["status value cannot be changed, you must delete and re-create the object"]

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "EARLY-REGISTRATION")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "LIR-PARTITIONED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "ASSIGNED PA")
    }

    def "modify 3 objects, change status, with override"() {
      given:
        syncUpdate(getTransient("EARLY") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: override1

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: override1

                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override: override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 0, 3, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 3)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.255.255") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation override used"]

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "SUB-ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "LIR-PARTITIONED PA")
    }

    def "delete and re-create 3 objects, change status"() {
      given:
        syncUpdate(getTransient("EARLY") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:       changing status

                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:       changing status

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                DELETE:       changing status

                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 6
        ack.summary.assertSuccess(6, 3, 0, 3, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "SUB-ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "LIR-PARTITIONED PA")
    }

    def "modify PI assignment, pw supplied, add remarks:"() {
      given:
        syncUpdate(getTransient("ASSPI") + "password: hm")

      expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                changed:      dbtest@ripe.net 20020101
                remarks:      just added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "delete allocation, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override:  override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:  test override
                override:  override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "modify assignment, joint RS & user mnt-by, remove RS mntner, user auth"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("JOINT-ASS") + "password: lir\npassword: hm")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Adding or removing a RIPE NCC maintainer requires administrative authorisation"]

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-NCC-HM-MNT")
    }

    // Modify assignment, user mnt-by, add RS mntner with RS auth
    @Ignore
    def "modify assignment, user mnt-by, add RS mntner with RS auth"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                password: hm
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-NCC-HM-MNT")
    }

    def "modify assignment, user mnt-by, add DB mntner without DB auth"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       RIPE-DBM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Adding or removing a RIPE NCC maintainer requires administrative authorisation"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-DBM-MNT")
    }

    // Modify assignment, user mnt-by, add DB mntner with DB auth
    @Ignore
    def "modify assignment, user mnt-by, add DB mntner with DB auth"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       RIPE-DBM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: end
                password: dbm
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-DBM-MNT")
    }

    def "delete assignment, using prefix notation"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200/24
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                delete:  using prefix

                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Value 192.168.200/24 converted to 192.168.200.0 - 192.168.200.255"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "Remove mnt-routes, inverse lookup"() {
      when:
        def message = syncUpdate("""\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-routes:   LIR2-MNT {ANY}
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent())
      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        queryObject("-r -i mu LIR2-MNT", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def update = syncUpdate("""\
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent())
      then:
        def updateAck = new AckResponse("", update)
        updateAck.summary.nrFound == 1
        updateAck.summary.assertSuccess(1, 0, 1, 0, 0)
        updateAck.summary.assertErrors(0, 0, 0, 0)

        queryObjectNotFound("-r -i mu LIR2-MNT", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "modify EARLY-REGISTRATION, mnt-by RS and user, change mnt-lower"() {
      given:
        syncUpdate(getTransient("EARLY-USER") + "override: override1")

      expect:
        query_object_not_matches("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")


      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT      # was LIR-MNT
                changed:      dbtest@ripe.net 20020101
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")
    }

    def "modify EARLY-REGISTRATION, mnt-by user only, change mnt-lower"() {
      given:
        syncUpdate(getTransient("EARLY-USER-ONLY") + "override: override1")

      expect:
        query_object_not_matches("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT      # was LIR-MNT
                mnt-lower:    OWNER-MNT
                changed:      dbtest@ripe.net 20020101
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")
    }

}
