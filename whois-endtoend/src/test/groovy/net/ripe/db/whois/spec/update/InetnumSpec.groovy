package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class InetnumSpec extends BaseQueryUpdateSpec {

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
                source:       TEST
                """,
                "PLACEHOLDER": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                """,
                "ALLOC-PA-8": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
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
                source:       TEST
                """,
                "ALLOC-ASSIGN-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
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
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
                "AGGREGATED-LIR": """\
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
                source:       TEST
                """,
                "LEGACY-USER-ONLY": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
                source:       TEST
                """
        ]
    }

    def "create ALLOCATED UNSPECIFIED, with RS mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
          def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }

        queryObject("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "create ALLOCATED UNSPECIFIED, with nonexistent org"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
          def ack = syncUpdateWithResponse("""
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NULL1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.0.0.0 - 192.255.255.255") ==
                ["Unknown object referenced ORG-NULL1-TEST", "Reference \"ORG-NULL1-TEST\" not found"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "not create inetnum with abuse-c that references role without abuse-mailbox"() {
        given:
        dbfixture(  """\
                role:         Abuse Handler2
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST
            """.stripIndent(true)
        )

        expect:
        queryObject("-r -T role AH2-TEST", "role", "Abuse Handler2")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                abuse-c:      AH2-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.0.0.0 - 192.255.255.255") ==
                ["The \"abuse-c\" ROLE object 'AH2-TEST' has no \"abuse-mailbox:\" Add \"abuse-mailbox:\" to the ROLE object, then update the INETNUM object"]

        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "create inetnum with mntner that still has referral-by"() {
        given:
        dbfixture(  """\
            mntner:  REFERRALBY-MNT
            descr:   description
            admin-c: TP1-TEST
            mnt-by:  REFERRALBY-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            referral-by: REFERRALBY-MNT
            source:  TEST
            """.stripIndent(true)
        )
        dbfixture(getTransient("ALLOC-PA"))

        expect:
        queryObject("-r -T mntner REFERRALBY-MNT", "mntner", "REFERRALBY-MNT")
        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.128.2 - 192.168.128.2
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       REFERRALBY-MNT
                source:       TEST

                password: update
                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.2 - 192.168.128.2" }

        queryObject("-rGBT inetnum 192.168.128.2 - 192.168.128.2", "inetnum", "192.168.128.2 - 192.168.128.2")
        query_object_matches("-r -T mntner REFERRALBY-MNT", "mntner", "REFERRALBY-MNT", "mnt-by:\\s*REFERRALBY-MNT")
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Status ALLOCATED UNSPECIFIED can only be created by the database administrator"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation with single IP address, parent with mnt-lower, parent mnt-lower pw supplied"() {
        expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.128.0", "inetnum", "192.168.128.0 - 192.168.128.0")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.128.0 - 192.168.128.0
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.128.0" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.128.0", "inetnum", "192.168.128.0 - 192.168.128.0")
    }

    def "create allocation with 3 IP addresses, create 2 partitions with 1 and 2 IPs, create assignments from partition parent with mnt-lower, parent mnt-lower pw supplied"() {
        expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.128.2", "inetnum", "192.168.128.0 - 192.168.128.2")
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.128.0", "inetnum", "192.168.128.0 - 192.168.128.0")
        queryObjectNotFound("-r -T inetnum 192.168.128.1 - 192.168.128.2", "inetnum", "192.168.128.1 - 192.168.128.2")
        queryObjectNotFound("-r -T inetnum 192.168.128.1 - 192.168.128.1", "inetnum", "192.168.128.1 - 192.168.128.1")
        queryObjectNotFound("-r -T inetnum 192.168.128.2 - 192.168.128.2", "inetnum", "192.168.128.2 - 192.168.128.2")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.128.0 - 192.168.128.2
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.128.0 - 192.168.128.0
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.128.1 - 192.168.128.2
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.128.1 - 192.168.128.1
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.128.2 - 192.168.128.2
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent(true)
        )

        then:
        ack.success

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 5, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.128.2" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.128.0" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.1 - 192.168.128.2" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.1 - 192.168.128.1" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.2 - 192.168.128.2" }

        queryObject("-r -T inetnum 192.168.128.0 - 192.168.128.2", "inetnum", "192.168.128.0 - 192.168.128.2")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.128.0", "inetnum", "192.168.128.0 - 192.168.128.0")
        queryObject("-r -T inetnum 192.168.128.1 - 192.168.128.2", "inetnum", "192.168.128.1 - 192.168.128.2")
        queryObject("-r -T inetnum 192.168.128.1 - 192.168.128.1", "inetnum", "192.168.128.1 - 192.168.128.1")
        queryObject("-r -T inetnum 192.168.128.2 - 192.168.128.2", "inetnum", "192.168.128.2 - 192.168.128.2")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied, mnt-by using second alloc mntner"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword:hm")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: hm
                password: owner2
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation, parent with mnt-lower, parent mnt-lower pw supplied, obj mnt-by not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST

                password: lir
                password: owner3
                password: hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation UNSPECIFIED, parent mnt-lower alloc mntner, parent mnt-lower pw supplied, obj mnt-by not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
          def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        ack.errorMessagesFor("Create", "[inetnum] 192.168.128.0 - 192.168.255.255") ==
                ["Status ALLOCATED UNSPECIFIED can only be created by the database administrator"]

        queryObjectNotFound("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation, parent with mnt-lower pw supplied, obj 2 mnt-by 1 not alloc mntner"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
          def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create allocation    UNSPECIFIED with spaces, parent with mnt-lower, parent mnt-lower pw supplied"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      when:
          def ack = syncUpdateWithResponse("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
          def ack = syncUpdateWithResponse("""
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST

                password: hm
                password: end
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create assignment with single IP address, parent no mnt-lower, parent mnt-by pw supplied"() {
        given:
        syncUpdate(getTransient("P-NO-LOW") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.200.0 - 192.168.200.0
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST

                password: hm
                password: end
                """.stripIndent(true)
        )

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.0" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir2
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: hm
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: fred
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
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
                source:       TEST
                override: denis,override1

                """.stripIndent(true)
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
                source:       TEST
                override: fred

                """.stripIndent(true)
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                inetnum:      62.59.192.2 - 62.59.192.30
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                source:       TEST
                override: denis,override1

                password: hm
                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(4, 4, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 1)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 62.59.192.2 - 62.59.192.30" }
        ack.errorMessagesFor("Create", "[inetnum] 62.59.192.2 - 62.59.192.30") ==
                ["Status ASSIGNED PA not allowed when more specific object '62.59.192.8 - 62.59.192.15' has status ALLOCATED PA",
                 "This range overlaps with 62.59.192.0 - 62.59.192.7"]
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
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
                source:       TEST

                password: lir
                password: lir3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create LIR-PARTITIONED PA with single IP address, parent and child with mnt-lower, pw supplied for parent mnt-lower and child mnt-by"() {
        given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.0
                netname:      RIPE-NET1
                descr:        /24 partition
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR3-MNT
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.0" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")
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
                source:       TEST

                password: lir
                password: lir3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "LIR2-MNT")

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create SUB-ALLOCATED PA with single IP address, parent and child with mnt-lower, pw supplied for parent mnt-lower and child mnt-by"() {
        given:
        syncUpdate(getTransient("P-LOW") + "password: hm\npassword: owner3")

        expect:
        queryObject("-GBr -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.0
                netname:      RIPE-NET1
                descr:        /24 partition
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR3-MNT
                source:       TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.0" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.0", "inetnum", "192.168.200.0 - 192.168.200.0")
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
                source:       TEST

                password: lir2
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir2
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir2
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir2
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
                remarks:      updated
                source:       TEST

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                remarks:      updated
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.authFailCheck("Modify", "FAILED", "inetnum", "192.168.200.0 - 192.168.200.255", "", "inetnum", "192.168.200.0 - 192.168.200.255", "mnt-by", "END-USER-MNT")

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "updated")
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
                remarks:      updated
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Value 192.168.200/24 converted to 192.168.200.0 - 192.168.200.255"]

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "updated")
    }

    def "modify resource, add comment not allowed in managed attributes by end user"() {
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
                status:       ASSIGNED PI #test comment
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Comments are not allowed on RIPE NCC managed Attribute \"status:\""]

    }

    def "modify resource LEGACY, add comment allowed in only user maintained resource"() {
        given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST # test comment
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
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
                source:       TEST
                delete:  testing

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST
                delete:  testing

                password: sub
                password: lir
                password: lir2
                password: lir3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(3, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                mnt-routes:   routes-mnt any
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                mnt-routes:   routes-mnt ANY
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Syntax error in routes-mnt { 192.168.200.0/24 } (ANY can only occur as a single value)",
                "Syntax error in routes-mnt ANY (ANY can only occur as a single value)"
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)

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
                mnt-routes:   routes-mnt any
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Syntax error in routes-mnt { 192.168.2.3/16 }"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with IRT ref, irt pw supplied"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")
        dbfixture(getTransient("ALLOC-PA-8"))

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: hm
                password: end
                password: test
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with IRT ref, no irt pw"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")
        dbfixture(getTransient("ALLOC-PA-8"))

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
          def ack = syncUpdateWithResponse("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-irt:      irt-test
                source:       TEST

                password: hm
                password: end
                """.stripIndent(true)
        )

      then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.authFailCheck("Create", "FAILED", "inetnum", "192.168.0.0 - 192.168.255.255", "", "inetnum", "192.168.0.0 - 192.168.255.255", "mnt-irt", "irt-test")

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create assignment, with all optional attributes"() {
      given:
        syncUpdate(getTransient("IRT") + "password: owner")
        dbfixture(getTransient("ALLOC-PA-8"))

      expect:
        queryObject("-GBr -T irt irt-test", "irt", "irt-test")
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
          def ack = syncUpdateWithResponse("""
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
                geofeed:     https://example.com/geofeed.csv
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
                mnt-routes:    owner-mnt ANY
                mnt-by:       owner2-mnt
                notify:       test2-dbtest@ripe.net
                mnt-irt:      irt-test
                source:       TEST
                tech-c:       TP3-TEST
                admin-c:      TP2-TEST
                abuse-c:      AH200-TEST
                remarks:      late comment

                # optional "sponsoring-org:" attribute not allowed with "ASSIGNED PA" status
                # optional "assignment-size:" only allowed with AGGREGATED-BY-LIR status

                password: hm
                password: end
                password: test
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["Syntax error in FRED"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")
    }

    def "create inetnum with  status AGGREGATED-BY-LIR, assignment-size optional"() {
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

          ack.summary.nrFound == 1
          ack.summary.assertSuccess(1, 1, 0, 0, 0)
          ack.summary.assertErrors(0, 0, 0, 0)

          ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }

          queryObject("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

    }

    def "create inetnum with  status AGGREGATED-BY-LIR, with assignment-size"() {
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
                assignment-size: 32
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

    }

    def "create inetnum with  status AGGREGATED-BY-LIR, with incorrect assignment-size"() {
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
                assignment-size: 40
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message


        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["\"assignment-size:\" value must not be greater than the maximum prefix size 32"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

    }

    def "modify inetnum with  status AGGREGATED-BY-LIR, add assignment-size fails"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        syncUpdate(getTransient("AGGREGATED-LIR") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.255 - 192.168.255.255", "inetnum", "192.168.0.255 - 192.168.255.255")

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
                assignment-size: 32
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["\"assignment-size:\" value cannot be changed"]

    }

    def "modify inetnum with  status AGGREGATED-BY-LIR, can not change assignment-size"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")


        def child = syncUpdate(new SyncUpdate(data: """\
                                        inetnum:      192.168.0.255 - 192.168.255.255
                                        netname:      TEST-NET-NAME
                                        descr:        TEST network
                                        country:      NL
                                        org:          ORG-LIR1-TEST
                                        admin-c:      TP1-TEST
                                        tech-c:       TP1-TEST
                                        status:       AGGREGATED-BY-LIR
                                        assignment-size: 32
                                        mnt-by:       lir-MNT
                                        mnt-lower:    LIR-MNT
                                        source:       TEST
                        
                                        password: owner3
                                        password: lir
                                    """.stripIndent(true)))

        expect:
        child =~ /SUCCESS/

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
                assignment-size: 28
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.255 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.255 - 192.168.255.255") ==
                ["\"assignment-size:\" value cannot be changed"]
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                remarks:      updated
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.100 - 192.168.0.100" }
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.100 - 192.168.0.100" }

        query_object_matches("-rGBT inetnum 192.168.0.100 - 192.168.0.100", "inetnum", "192.168.0.100 - 192.168.0.100", "updated")
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 3, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
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
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.128 - 192.168.0.255" }
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.0.128" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.0.128") ==
                ["This range overlaps with 192.168.0.128 - 192.168.0.255"]

        queryObject("-rGBT inetnum 192.168.0.128 - 192.168.0.255", "inetnum", "192.168.0.128 - 192.168.0.255")
        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.0.128", "inetnum", "192.168.0.0 - 192.168.0.128")
    }

    def "modify 1 object, change status"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-8") + "country: NL\nmnt-by: LIR-MNT\noverride: denis,override1")
        when:
        def message = syncUpdate("""\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                status:       SUB-ALLOCATED PA
                country:      NL
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.0.0.0 - 192.255.255.255") == [
                "status value cannot be changed, you must delete and re-create the object"]
        ack.warningMessagesFor("Modify", "[inetnum] 192.0.0.0 - 192.255.255.255") == [
                "inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        query_object_matches("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255", "ALLOCATED PA")
    }

    def "modify 3 objects, change status"() {
      given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

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
                source:       TEST

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       END-USER-MNT
                source:       TEST

                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 0, 0, 0, 2)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 4, 2)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["status value cannot be changed, you must delete and re-create the object"]
        ack.warningMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
              ["Status ALLOCATED PA not allowed when more specific object '192.168.200.0 - 192.168.255.255' has status LEGACY"]
        ack.successes.any { it.operation == "No operation" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.warningSuccessMessagesFor("No operation", "[inetnum] 192.168.200.0 - 192.168.255.255") ==
                ["Submitted object identical to database object"]
        ack.infoSuccessMessagesFor("No operation", "[inetnum] 192.168.200.0 - 192.168.255.255") ==
                ["Value SUB-ALLOCATED PA converted to LEGACY"]
        ack.successes.any { it.operation == "No operation" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.warningSuccessMessagesFor("No operation", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Submitted object identical to database object"]
        ack.infoSuccessMessagesFor("No operation", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Value LIR-PARTITIONED PA converted to LEGACY"]

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LEGACY")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "LEGACY")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "LEGACY")
    }

    def "modify 3 objects, change status, with override"() {
      given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")
        syncUpdate(getTransient("ASS-END") + "password: lir\npassword: end")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 62.59.192.2 - 92.59.192.30", "inetnum", "62.59.192.2 - 92.59.192.30")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                override: denis,override1

                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override: denis,override1

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                override: denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 0, 3, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 6, 3)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "You cannot add or remove a RIPE NCC maintainer",
                "status value cannot be changed, you must delete and re-create the object",
                "Status ALLOCATED PA not allowed when more specific object '192.168.200.0 - 192.168.255.255' has status LEGACY"]
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.warningSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.255.255") == [
                "status value cannot be changed, you must delete and re-create the object",
                "Status LIR-PARTITIONED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status LEGACY"]
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.255.255") == [
                "Authorisation override used"]
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") == [
                "Authorisation override used"]

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "LIR-PARTITIONED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "SUB-ALLOCATED PA")
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
                source:       TEST

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                source:       TEST
                delete:       changing status

                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       END-USER-MNT
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
                mnt-lower:    LIR-MNT
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
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 6
        ack.summary.assertSuccess(6, 3, 0, 3, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.warningSuccessMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Status ALLOCATED UNSPECIFIED not allowed when more specific object '192.168.200.0 - 192.168.255.255' has status SUB-ALLOCATED PA"]
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255", "SUB-ALLOCATED PA")
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "SUB-ALLOCATED PA")
    }

    def "delete inetnum with cidr notation supported"() {
        given:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.207.255", "inetnum", "192.168.200.0 - 192.168.207.255")

        syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.207.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                org:          ORG-LIR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent(true)
        )
        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.207.255", "inetnum", "192.168.200.0 - 192.168.207.255")
        queryObject("-r -T inetnum 192.168.200/21", "inetnum", "192.168.200.0 - 192.168.207.255")

        when:
        syncUpdate("""\
                inetnum:      192.168.200/21
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                org:          ORG-LIR1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                DELETE:       changing status

                password: lir
                password: hm
                """.stripIndent(true)
        )
        then:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.207.255", "inetnum", "192.168.200.0 - 192.168.207.255")
        queryObjectNotFound("-r -T inetnum 192.168.200/21", "inetnum", "192.168.200.0 - 192.168.207.255")
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
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "delete allocation, override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "override:  denis,override1")
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
                source:       TEST
                delete:  test override
                override:  denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete ALLOCATED-ASSIGNED PA, override"() {
        given:
        syncUpdate(getTransient("ALLOC-ASSIGN-PA") + "override:  denis,override1")
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
                status:       ALLOCATED-ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:  test override
                override:  denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.infoSuccessMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") == [
                "Authorisation override used"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete ALLOCATED-ASSIGNED PA, rs"() {
        given:
        syncUpdate(getTransient("ALLOC-ASSIGN-PA") + "override:  denis,override1")
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
                status:       ALLOCATED-ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:  test rs
                password:  hm

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "delete ALLOCATED-ASSIGNED PA, user"() {
        given:
        syncUpdate(getTransient("ALLOC-ASSIGN-PA") + "override:  denis,override1")
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
                status:       ALLOCATED-ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:  test user
                password: lir

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Deleting this object requires administrative authorisation"]

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
                source:       TEST

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["You cannot add or remove a RIPE NCC maintainer"]

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-NCC-HM-MNT")
    }

    def "modify assignment, user mnt-by, add RS mntner with RS auth only allowed by override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
        inetnum:      192.168.200.0 - 192.168.200.255
        netname:      RIPE-NET1
        descr:        /24 assigned
        country:      NL
        admin-c:      TP1-TEST
        tech-c:       TP1-TEST
        status:       ASSIGNED PA
        mnt-by:       END-USER-MNT
        mnt-by:       RIPE-NCC-HM-MNT
        source:       TEST
        override:     denis,override1
        """.stripIndent(true))


      then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-NCC-HM-MNT")
    }

    def "modify assignment, user mnt-by, add RS mntner with RS auth should not be allowed"() {
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
                source:       TEST

                password: end
                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["You cannot add or remove a RIPE NCC maintainer"]
        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "END-USER-MNT")
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
                source:       TEST

                password: end
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["You cannot add or remove a RIPE NCC maintainer"]

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-DBM-MNT")
    }

    def "modify assignment, user mnt-by, add DB mntner should only be allowed by ripe"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASS") + "password: lir\npassword: end")
        queryObject("-GBr -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       END-USER-MNT
                mnt-by:       RIPE-DBM-MNT
                source:       TEST
                override:     denis,override1
                """.stripIndent(true)
        )

      then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "RIPE-DBM-MNT")
    }

    def "Remove mnt-routes by hostmaster, inverse lookup"() {
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
                mnt-routes:   LIR2-MNT ANY
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent(true))
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
                source:       TEST

                password: hm
                """.stripIndent(true))
      then:
        def updateAck = new AckResponse("", update)
        updateAck.summary.nrFound == 1
        updateAck.summary.assertSuccess(1, 0, 1, 0, 0)
        updateAck.summary.assertErrors(0, 0, 0, 0)

        queryObjectNotFound("-r -i mu LIR2-MNT", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

   def "Remove mnt-routes by user maintainer, inverse lookup"() {
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
                mnt-by:       LIR2-MNT
                mnt-routes:   LIR2-MNT ANY
                source:       TEST

                password: hm
                password: lir
                password: end
                password: owner3
                """.stripIndent(true))
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
                mnt-by:       LIR2-MNT
                source:       TEST

                password: lir2
                """.stripIndent(true))
      then:
        def updateAck = new AckResponse("", update)
        updateAck.summary.nrFound == 1
        updateAck.summary.assertSuccess(1, 0, 1, 0, 0)
        updateAck.summary.assertErrors(0, 0, 0, 0)

        queryObjectNotFound("-r -i mu LIR2-MNT", "inetnum", "192.168.128.0 - 192.168.255.255")
    }

    def "create top level LEGACY, mnt-by user only, no parent LEGACY, LIR pw"() {
        given:
        syncUpdate(getTransient("PLACEHOLDER") + "override: denis,override1")

        expect:
        queryObjectNotFound("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for parent [inetnum] 192.0.0.0 - 192.255.255.255 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Only RIPE NCC can create/delete a top level object with status 'LEGACY' Contact legacy@ripe.net for more info"]

        queryObjectNotFound("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create top level LEGACY, mnt-by user only, no parent LEGACY, override"() {
        given:
        syncUpdate(getTransient("PLACEHOLDER") + "override: denis,override1")

        expect:
        queryObjectNotFound("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "modify top level LEGACY, mnt-by user only, change mnt-lower, LIR pw"() {
        given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")

        expect:
        query_object_not_matches("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT      # was LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")
    }

    def "delete top level LEGACY, re-create 2 sub ranges, mnt-by user only, override"() {
        given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")

        expect:
        query_object_not_matches("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255", "LIR2-MNT")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete: splitting into 2
                override:   denis,override1

                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:   denis,override1

                inetnum:      192.168.128.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:       TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 2, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 3)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.128.0 - 192.168.255.255" }

        queryObjectNotFound("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        query_object_matches("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255", "LIR-MNT")
        query_object_matches("-rGBT inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255", "LIR2-MNT")
    }

    def "delete top level LEGACY, mnt-by user only, LIR pw"() {
        given:
        syncUpdate(getTransient("LEGACY-USER-ONLY") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete: splitting into 2

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Delete", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Only RIPE NCC can create/delete a top level object with status 'LEGACY' Contact legacy@ripe.net for more info"]

        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "dry-run modify PI assignment, pw supplied, add remarks:"() {
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
                dry-run: testing
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       lir-MNT
                remarks:      just added
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        query_object_not_matches("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255", "just added")
    }

    def "create and modify, more specific, without needing mnt-lower"() {
        given:
          databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
          whoisFixture.reloadTrees()
        when:
          def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password: lir
                    """.stripIndent(true)))
        then:
          created =~ /Create SUCCEEDED: \[inetnum\] 192.168.0.0 - 192.168.0.255/
        when:
          def modified = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description (updated)
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password: lir
                    """.stripIndent(true)))
        then:
          modified =~ /Modify SUCCEEDED: \[inetnum\] 192.168.0.0 - 192.168.0.255/
    }

    def "create with abuse-c"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        abuse-c:    AH1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create SUCCEEDED: \[inetnum\] 192.168.0.0 - 192.168.0.255/
    }

    def "create and modify, with abuse-c"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        abuse-c:    AH1-TEST
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create SUCCEEDED: \[inetnum\] 192.168.0.0 - 192.168.0.255/
        when:
        def modified = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        modified =~ /Modify SUCCEEDED: \[inetnum\] 192.168.0.0 - 192.168.0.255/
    }

    def "create and modify with geofeed"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        geofeed:    https://www.example.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create SUCCEEDED: \[inetnum] 192.168.0.0 - 192.168.0.255/
        when:
        def modified = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        modified =~ /Modify SUCCEEDED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with invalid geofeed"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        geofeed:    not an url
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with not secure url as geofeed"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        geofeed:    http://unsecure.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with geofeed and remarks geofeed"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        geofeed:    https://example.com
                        remarks:    geofeed: https://example.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create and modify with prefixlen"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        prefixlen:  https://www.example.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create SUCCEEDED: \[inetnum] 192.168.0.0 - 192.168.0.255/
        when:
        def modified = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    DK
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        modified =~ /Modify SUCCEEDED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with invalid prefixlen"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        prefixlen:  not an url
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with not secure url as prefixlen"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        prefixlen:  http://unsecure.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create with prefixlen and remarks prefixlen"() {
        given:
        databaseHelper.addObject("""\
                    inetnum:    192.168.0.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ALLOCATED UNSPECIFIED
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true))
        whoisFixture.reloadTrees()
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                        inetnum:    192.168.0.0 - 192.168.0.255
                        netname:    RIPE-NCC
                        status:     ASSIGNED PI
                        descr:      description
                        country:    NL
                        prefixlen:  https://example.com
                        remarks:    prefixlen: https://example.com
                        admin-c:    TP1-TEST
                        tech-c:     TP1-TEST
                        mnt-by:     LIR-MNT
                        source:     TEST
                        password:   lir
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.0.0 - 192.168.0.255/
    }

    def "create LIR-PARTITIONED PA child under ASSIGNED PA parent"() {
        given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.168.200.4 - 192.168.200.7
                    netname:    RIPE-NCC
                    status:     LIR-PARTITIONED PA
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.200.4 - 192.168.200.7/
        created =~ /inetnum parent has incorrect status: ASSIGNED PA/
    }

    def "create ASSIGNED PA parent above LIR-PARTITIONED PA child"() {
        given:
        syncUpdate(getTransient("EARLY") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.168.100.0 - 192.168.255.255
                    netname:    RIPE-NCC
                    status:     ASSIGNED PA
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.100.0 - 192.168.255.255/
        created =~ /Status ASSIGNED PA not allowed when more specific object
            '192.168.200.0 - 192.168.255.255' has status LIR-PARTITIONED PA/
    }

    def "create ASSIGNED PA child under ASSIGNED PA parent"() {
        given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.168.200.4 - 192.168.200.7
                    netname:    RIPE-NCC
                    status:     ASSIGNED PA
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.200.4 - 192.168.200.7/
        created =~ /inetnum parent has incorrect status: ASSIGNED PA/
    }

    def "create ASSIGNED PA parent above ASSIGNED PA child"() {
        given:
        syncUpdate(getTransient("P-LOW-R-D") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS") + "password: end\npassword: lir")

        when:
        def created = syncUpdate(new SyncUpdate(data: """\
                    inetnum:    192.168.100.0 - 192.168.200.255
                    netname:    RIPE-NCC
                    status:     ASSIGNED PA
                    descr:      description
                    country:    NL
                    admin-c:    TP1-TEST
                    tech-c:     TP1-TEST
                    mnt-by:     LIR-MNT
                    source:     TEST
                    """.stripIndent(true)))
        then:
        created =~ /Create FAILED: \[inetnum] 192.168.100.0 - 192.168.200.255/
        created =~ /Status ASSIGNED PA not allowed when more specific object
            '192.168.200.0 - 192.168.200.255' has status ASSIGNED PA/
    }

}
