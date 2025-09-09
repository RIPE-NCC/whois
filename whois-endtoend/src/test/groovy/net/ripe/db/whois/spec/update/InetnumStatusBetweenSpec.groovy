package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import spock.lang.Ignore

@org.junit.jupiter.api.Tag("IntegrationTest")
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
                source:       TEST
                """,
            "USER-ALLOC-PA": """\
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
                mnt-lower:    RIPE-NCC-HM-MNT
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
                source:       TEST
                """,
            "LEGACYROOT": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                source:       TEST
                """,
            "LEGACY": """\
                inetnum:      192.168.100.0 - 192.168.100.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                source:       TEST
                """,
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
        def ack = syncUpdateWithResponse("""
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status ALLOCATED-ASSIGNED PA"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
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

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED-ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED UNSPECIFIED, with status LEGACY"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ALLOC-UNS2") + "password: owner3\npassword: hm")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LEGACY not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED UNSPECIFIED"]

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
        def ack = syncUpdateWithResponse("""\
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status ALLOCATED-ASSIGNED PA"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
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

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED-ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ALLOCATED PA, with status LEGACY"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LEGACY not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status SUB-ALLOCATED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED PA and LIR-PARTITIONED PA tests

    def "create between ALLOCATED PA and LIR-PARTITIONED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LIR-PARTITIONED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects for status AGGREAGATED BY LIR

    def "create between ALLOCATED PA and ASSIGNED PA, with status AGGREGATED-BY-LIR"() {
        given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       AGGREGATED-BY-LIR
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and LIR-PARTITIONED PA, with status AGGREGATED-BY-LIR"() {
        given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("PART-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       AGGREGATED-BY-LIR
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LIR-PARTITIONED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED PA and SUB-ALLOCATED PA, with status AGGREGATED-BY-LIR"() {
        given:
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: owner3\npassword: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       AGGREGATED-BY-LIR
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status AGGREGATED-BY-LIR not allowed when more specific object '192.168.0.0 - 192.169.255.255' has status SUB-ALLOCATED PA"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    @Ignore("TODO: confirmed issue - this scenario shouldn't succeed")
    def "create between user ALLOCATED PA and ASSIGNED PA, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("USER-ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("ASS-END") + "password: owner3\npassword: end\npassword: lir")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
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
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
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
        def ack = syncUpdateWithResponse("""\
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status SUB-ALLOCATED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

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
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

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
        def ack = syncUpdateWithResponse("""\
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and ASSIGNED ANYCAST, with status LEGACY"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("ASSANY") + "password: owner3\npassword: hm")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LEGACY not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED ANYCAST"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values ALLOCATED UNSPECIFIED and LEGACY tests

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ALLOCATED PA not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LEGACY"]

        queryObjectNotFound("-rGBT inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       liR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status LIR-PARTITIONED PA not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LEGACY"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       lIr-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LEGACY"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = syncUpdate("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PI not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LEGACY"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
        def message = syncUpdate("""
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED ANYCAST not allowed when more specific object '192.168.0.0 - 192.168.255.255' has status LEGACY"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    def "create between ALLOCATED UNSPECIFIED and LEGACY, with status LEGACY"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.100.0.0 - 192.200.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }

        queryObject("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

    // Create object between objects with status values LEGACY and LEGACY tests

    def "create between LEGACY and LEGACY, with status ASSIGNED PA"() {
        given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")
        syncUpdate(getTransient("LEGACY") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObject("-r -T inetnum 192.168.100.0 - 192.168.100.255", "inetnum", "192.168.100.0 - 192.168.100.255")
        queryObjectNotFound("-r -T inetnum 192.168.100.0 - 192.168.200.255", "inetnum", "192.168.100.0 - 192.168.200.255")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.100.0 - 192.168.200.255
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

                password: owner3
                password: lir
                """.stripIndent(true)
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.100.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.100.0 - 192.168.200.255") ==
                ["Value ASSIGNED PA converted to LEGACY"]

        queryObject("-r -T inetnum 192.168.100.0 - 192.168.200.255", "inetnum", "192.168.100.0 - 192.168.200.255")
    }

    // TODO: confirmed issue, for two reasons:
    //      (1) cannot have hierarchy of assignments
    //      (2) cannot have an assignment under an allocated unspecified
    @Ignore("TODO: failing test")
    def "create between ALLOCATED UNSPECIFIED and ASSIGNED PA, with status ASSIGNED PA"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

        expect:
        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent(true)
        )

        then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.100.0.0 - 192.200.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.100.0.0 - 192.200.255.255") ==
                ["Status ASSIGNED PA not allowed when more specific object '192.168.200.0 - 192.168.200.255' has status ASSIGNED PA"]

        queryObjectNotFound("-r -T inetnum 192.100.0.0 - 192.200.255.255", "inetnum", "192.100.0.0 - 192.200.255.255")
    }

}
