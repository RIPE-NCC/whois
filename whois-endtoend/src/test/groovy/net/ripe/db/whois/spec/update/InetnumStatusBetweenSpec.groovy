package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.EndToEndTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.experimental.categories.Category(EndToEndTest.class)
class InetnumStatusBetweenSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "ALLOC-UNS": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-UNS2": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
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
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ERX-ALLOC-PA": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-PA2": """\
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
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-PI": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ALLOC-PI2": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ERX-ALLOC-PI": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "PART-PA": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "PART-PI": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "SUB-ALLOC-PA": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-SUB1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    SUB-MNT
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
            "EARLY-ASSPI": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
                mnt-lower:    RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "ASSANY": """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      RIPE-NET1
                descr:        /24 assigned
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """,
            "EARLY-ALLOC": """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      RIPE-NET1
                descr:        /8 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                """
    ]}

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PI not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status EARLY-REGISTRATION"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status EARLY-REGISTRATION not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status NOT-SET"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status NOT-SET not allowed when more specific object has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED UNSPECIFIED and ALLOCATED PA tests

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PI not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status EARLY-REGISTRATION"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status EARLY-REGISTRATION not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status NOT-SET"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status NOT-SET not allowed when more specific object has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED UNSPECIFIED and ALLOCATED PI tests

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PI not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status EARLY-REGISTRATION"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status EARLY-REGISTRATION not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PI, with status NOT-SET"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status NOT-SET not allowed when more specific object has status ALLOCATED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED PA and SUB-ALLOCATED PA tests

    def "create between ALLOCATED PA and SUB-ALLOCATED PA, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and SUB-ALLOCATED PA, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and SUB-ALLOCATED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status SUB-ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and SUB-ALLOCATED PA, with status NOT-SET, with override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST
                override:       denis,override1

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") == [
                "Authorisation override used"]

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED PA and LIR-PARTITIONED PA tests

    def "create between ALLOCATED PA and LIR-PARTITIONED PA, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and LIR-PARTITIONED PA, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and LIR-PARTITIONED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status LIR-PARTITIONED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED PA and ASSIGNED PA tests

    def "create between ALLOCATED PA and ASSIGNED PA, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and ASSIGNED PA, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and ASSIGNED PA, with status ASSIGNED PA"() {
      when:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")

      then:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")

      then:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status ASSIGNED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between user ALLOCATED PA and ASSIGNED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ERX-ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED PI and ASSIGNED PI tests

    def "create between ALLOCATED PI and ASSIGNED PI, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY-ASSPI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PI and ASSIGNED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSPI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ASSIGNED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between user ALLOCATED PI and RS ASSIGNED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ERX-ALLOC-PI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSPI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ASSIGNED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between user ALLOCATED PI and user ASSIGNED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ERX-ALLOC-PI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY-ASSPI") + "password: owner3\npassword: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST tests

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status EARLY-REGISTRATION"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status EARLY-REGISTRATION not allowed when more specific object has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED UNSPECIFIED and EARLY-REGISTRATION tests

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PI not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object has status EARLY-REGISTRATION"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and EARLY-REGISTRATION, with status EARLY-REGISTRATION"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("EARLY") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       EARLY-REGISTRATION
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values EARLY-REGISTRATION and LIR-PARTITIONED PI tests

    def "create between EARLY-REGISTRATION and LIR-PARTITIONED PI, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PI") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object has status LIR-PARTITIONED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between EARLY-REGISTRATION and LIR-PARTITIONED PI, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PI") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between EARLY-REGISTRATION and LIR-PARTITIONED PI, with status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PI") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object has status LIR-PARTITIONED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between EARLY-REGISTRATION and LIR-PARTITIONED PI, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PI") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object has status LIR-PARTITIONED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between EARLY-REGISTRATION and LIR-PARTITIONED PI, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PI") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status LIR-PARTITIONED PI"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values EARLY-REGISTRATION and SUB-ALLOCATED PA tests

    def "create between EARLY-REGISTRATION and SUB-ALLOCATED PA, with status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PI not allowed when more specific object has status SUB-ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between EARLY-REGISTRATION and SUB-ALLOCATED PA, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("EARLY-ALLOC") + "override: denis,override1")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                changed:      dbtest@ripe.net 20020101
                source:       TEST

                password: hm
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object has status SUB-ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

}
