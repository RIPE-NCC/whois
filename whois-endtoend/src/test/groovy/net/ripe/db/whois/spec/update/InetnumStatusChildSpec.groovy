package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.experimental.categories.Category(IntegrationTest.class)
class InetnumStatusChildSpec extends BaseQueryUpdateSpec {

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
            "ALLOC-PA2": """\
                inetnum:      192.170.0.0 - 192.170.255.255
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
            "ALLOC-PA3": """\
                inetnum:      192.171.0.0 - 192.171.255.255
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
                source:       TEST
                """,
            "RS-ALLOC-PI": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST
                """,
            "USER-ALLOC-PI": """\
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
                inetnum:      192.168.128.0 - 192.168.255.255
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
            "PART2-PA": """\
                inetnum:      192.168.200.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       SUB-MNT
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
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
            "NOTSET": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      RIPE-NET1
                descr:        /16 ERX
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """
    ]}

    def "create ASSIGNED PA, with multiple parent objects"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ALLOC-PA2") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.170.0.0 - 192.170.255.255", "inetnum", "192.170.0.0 - 192.170.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.169.0.0 - 192.170.255.255", "inetnum", "192.169.0.0 - 192.170.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.169.0.0 - 192.170.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.169.0.0 - 192.170.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.169.0.0 - 192.170.255.255") == [
                "This range overlaps with 192.168.0.0 - 192.169.255.255",
                "Status ASSIGNED PA not allowed when more specific object '192.170.0.0 - 192.170.255.255' has status ALLOCATED PA"
        ]

        queryObjectNotFound("-rGBT inetnum 192.169.0.0 - 192.170.255.255", "inetnum", "192.169.0.0 - 192.170.255.255")
    }

    def "create ALLOCATED PA, overlapping with existing object"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.169.0.0 - 192.170.255.255", "inetnum", "192.169.0.0 - 192.170.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.169.0.0 - 192.170.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.169.0.0 - 192.170.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.169.0.0 - 192.170.255.255") == [
                "This range overlaps with 192.168.0.0 - 192.169.255.255"
        ]

        queryObjectNotFound("-rGBT inetnum 192.169.0.0 - 192.170.255.255", "inetnum", "192.169.0.0 - 192.170.255.255")
    }

    def "create ALLOCATED PA, overlapping with 2 existing objects"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ALLOC-PA3") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.171.0.0 - 192.171.255.255", "inetnum", "192.171.0.0 - 192.171.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.169.0.0 - 192.171.255.255", "inetnum", "192.169.0.0 - 192.171.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.169.0.0 - 192.171.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.169.0.0 - 192.171.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.169.0.0 - 192.171.255.255") == [
                "This range overlaps with 192.168.0.0 - 192.169.255.255",
                "Status ALLOCATED PA not allowed when more specific object '192.171.0.0 - 192.171.255.255' has status ALLOCATED PA"
        ];

        queryObjectNotFound("-rGBT inetnum 192.169.0.0 - 192.171.255.255", "inetnum", "192.169.0.0 - 192.171.255.255")
    }

    // Create child object with status ALLOCATED UNSPECIFIED tests

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED"() {
      expect:
        queryObjectNotFound("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.0.0.0 - 192.255.255.255" }

        queryObject("-rGBT inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
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
                password: sub
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                password: end
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO: needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
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

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Value ALLOCATED UNSPECIFIED converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      25.168.200.0 - 25.168.200.127
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
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED, no required org"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED UNSPECIFIED, parent status ALLOCATED UNSPECIFIED, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    // Create child object with status ALLOCATED PA tests

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
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

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    @Ignore("Ref. Override should not bypass Inetnum status check")
    def "create child ALLOCATED PA, parent status ALLOCATED PA with override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                override: denis,override1
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }


    def "create child ALLOCATED PA, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ALLOCATED PA, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED PA, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED PA, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
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
                password: sub
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ALLOCATED PA, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                password: end
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PA, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PA, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PA, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["Value ALLOCATED PA converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PA, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PA, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      25.168.200.0 - 25.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED, no required org"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PA, parent status ALLOCATED UNSPECIFIED, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    // Create child object with status ALLOCATED PI tests

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
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
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ALLOCATED PI, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED PI, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ALLOCATED PI, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: sub
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ALLOCATED PI, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: end
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PI, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PI, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PI, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)


        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["Value ALLOCATED PI converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PI, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ALLOCATED PI, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      25.168.200.0 - 25.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED, no required org"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Missing required \"org:\" attribute"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.169.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [IANA, RIR, LIR]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    def "create child ALLOCATED PI, parent status ALLOCATED UNSPECIFIED, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.169.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
    }

    // Create child object with status SUB-ALLOCATED PA tests

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA smaller than /24, parent status ALLOCATED PA"() {
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
                status:       SUB-ALLOCATED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
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

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       lIr-MNT
                mnt-lower:    LIR-MNT
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
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LiR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child SUB-ALLOCATED PA, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child SUB-ALLOCATED PA, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: sub
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate("""\
            inetnum:      192.168.199.0 - 192.168.200.255
            netname:      RIPE-NET1
            descr:        /23 assigned
            country:      NL
            admin-c:      TP1-TEST
            tech-c:       TP1-TEST
            status:       ASSIGNED PA
            mnt-by:       END-USER-MNT
            source:       TEST

            password:     end
            password:     hm
            """.stripIndent())

        queryObject("-r -T inetnum 192.168.199.0 - 192.168.200.255", "inetnum", "192.168.199.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
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
                password: end
                password: lir
                """.stripIndent())

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate("""\
            inetnum:      192.168.199.0 - 192.168.200.255
            netname:      RIPE-NET1
            descr:        /23 assigned
            country:      NL
            admin-c:      TP1-TEST
            tech-c:       TP1-TEST
            status:       ASSIGNED PI
            mnt-by:       RIPE-NCC-HM-MNT
            mnt-by:       LIR-MNT
            mnt-lower:    RIPE-NCC-HM-MNT
            source:       TEST

            password: hm
            password: lir
            """.stripIndent())  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.199.0 - 192.168.200.255", "inetnum", "192.168.199.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child SUB-ALLOCATED PA, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.255
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
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["Value SUB-ALLOCATED PA converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.255", "inetnum", "25.168.200.0 - 25.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      25.168.200.0 - 25.168.200.255
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
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.255") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.255", "inetnum", "25.168.200.0 - 25.168.200.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA, no org"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA, referenced org type OTHER"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA, referenced org type RIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA, referenced org type NIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child SUB-ALLOCATED PA, parent status ALLOCATED PA, referenced org type IANA"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       SUB-ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status LIR-PARTITIONED PA tests

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       lIr-MNT
                mnt-lower:    LIR-MNT
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
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LiR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child LIR-PARTITIONED PA, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child LIR-PARTITIONED PA, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: sub
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                password: end
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["Value LIR-PARTITIONED PA converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      25.168.200.0 - 25.168.200.127
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
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA, no org"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA, referenced org type OTHER"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA, referenced org type RIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA, referenced org type NIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PA, parent status ALLOCATED PA, referenced org type IANA"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status LIR-PARTITIONED PI tests

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PA"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
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
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       lIr-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LiR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child LIR-PARTITIONED PI, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child LIR-PARTITIONED PI, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       lir-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: sub
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
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: end
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override:  denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.summary.assertSuccess(1, 1, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["Value LIR-PARTITIONED PI converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      25.168.200.0 - 25.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI, no org"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child LIR-PARTITIONED PI, parent status ALLOCATED PI, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LIR-PARTITIONED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status ASSIGNED ANYCAST tests

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PA, no parent pw"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for parent [inetnum] 192.168.0.0 - 192.169.255.255 failed using \"mnt-lower:\" not authenticated by: LIR-MNT, LIR2-MNT",
                "inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED ANYCAST, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED ANYCAST, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: sub
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: end
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       lir-MNT
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Value ASSIGNED ANYCAST converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      25.168.200.0 - 25.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI, no org"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED ANYCAST, parent status ALLOCATED PI, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED ANYCAST
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status ASSIGNED PI tests

    def "create child ASSIGNED PI, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["inetnum parent has incorrect status: ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED PI, parent status LIR-PARTITIONED PI, mnt-by RS"() {
      given:
        syncUpdate(getTransient("RS-ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED PI, parent status LIR-PARTITIONED PI, not mnt-by RS"() {
      given:
        syncUpdate(getTransient("USER-ALLOC-PI") + "password: lir\npassword: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       lir-MNT
                mnt-lower:    lir-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED PI, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: sub
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.255") ==
                ["inetnum parent has incorrect status: SUB-ALLOCATED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: end
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED PI, mnt-by RS"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")  // <--- TODO needs bug fix for assigned PI creation
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED PI, not mnt-by RS, parent mnt-by RS"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                mnt-lower:    lir-MNT
                source:       TEST

                password: hm
                password: owner3
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       lir-MNT
                source:       TEST

                password: lir
                password: owner3
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") == [
                "Value ASSIGNED PI converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PI, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      25.168.200.0 - 25.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: owner3
                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI, no org"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: hm
                password: lir
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI, referenced org type OTHER"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI, referenced org type RIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI, referenced org type NIR"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [RIR, LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PI, parent status ALLOCATED PI, referenced org type IANA"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    RIPE-NCC-HM-MNT
                source:       TEST

                password: lir
                password: hm
                password: owner3
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [RIR, LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status ASSIGNED PA tests

    def "create child ASSIGNED PA, parent status ALLOCATED UNSPECIFIED"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.255.255
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
                ["inetnum parent has incorrect status: ALLOCATED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status LIR-PARTITIONED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LiR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED PA, parent status LIR-PARTITIONED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PI") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.0.0 - 192.168.127.255
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
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.127.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.127.255") ==
                ["inetnum parent has incorrect status: LIR-PARTITIONED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.127.255", "inetnum", "192.168.0.0 - 192.168.127.255")
    }

    def "create child ASSIGNED PA, parent status SUB-ALLOCATED PA"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.255
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
                password: sub
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

    def "create child ASSIGNED PA, parent status ASSIGNED PA, less specific ALLOCATED PA has RS mnt-by"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ALLOC-PA") + "password: hm\npassword: owner3")
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: hm\npassword:lir\npassword:lir2")

      expect:
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                password: end
                password: lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PA"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PA, parent status ASSIGNED PI"() {
      given:
        syncUpdate(getTransient("ALLOC-PI") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("ASSPI") + "password: hm\npassword: lir")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED PI"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PA, parent status ASSIGNED ANYCAST"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: hm\npassword: owner3")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ASSANY") + "password: hm")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: ASSIGNED ANYCAST"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PA, parent status LEGACY"() {
      given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

      expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
      def message = syncUpdate("""
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:
      def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["Value ASSIGNED PA converted to LEGACY"]

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PA, parent status NOT-SET"() {
      given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.200.0 - 192.168.200.127
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
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.200.0 - 192.168.200.127") ==
                ["inetnum parent has incorrect status: NOT-SET"]

        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
    }

    def "create child ASSIGNED PA, no parent status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def message = syncUpdate("""\
                inetnum:      25.168.200.0 - 25.168.200.127
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
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") ==
                ["Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA, no org"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA, referenced org type OTHER"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-OTO1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

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
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        queryObject("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA, referenced org type RIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-RIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA, referenced org type NIR"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-NIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child ASSIGNED PA, parent status ALLOCATED PA, referenced org type IANA"() {
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
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-IANA1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Referenced organisation has wrong \"org-type\". Allowed values are [LIR, OTHER]"]

        queryObjectNotFound("-rGBT inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // Create child object with status LEGACY tests

    def "create child LEGACY, parent no status"() {
      expect:
        queryObject("-r -T inetnum 25.168.0.0 - 25.168.255.255", "inetnum", "25.168.0.0 - 25.168.255.255")
        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")

      when:
        def message = syncUpdate("""\
                inetnum:      25.168.200.0 - 25.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
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
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 25.168.200.0 - 25.168.200.127" }
        ack.errorMessagesFor("Create", "[inetnum] 25.168.200.0 - 25.168.200.127") == [
                "Parent 25.168.0.0 - 25.168.255.255 does not have \"status:\""]

        queryObjectNotFound("-r -T inetnum 25.168.200.0 - 25.168.200.127", "inetnum", "25.168.200.0 - 25.168.200.127")
    }

    def "create hierarchy with status LEGACY, parent status LEGACY"() {
        given:
        syncUpdate(getTransient("LEGACYROOT") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.201.255")
        queryObjectNotFound("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
        queryObjectNotFound("-rGBT inetnum 192.168.200.128 - 192.168.200.255", "inetnum", "192.168.200.128 - 192.168.200.255")
        queryObjectNotFound("-rGBT inetnum 192.168.200.255 - 192.168.200.255", "inetnum", "192.168.200.255 - 192.168.200.255")
        queryObjectNotFound("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.200.0 - 192.168.201.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.200.0 - 192.168.200.127
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.200.128 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.200.255 - 192.168.200.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                inetnum:      192.168.201.0 - 192.168.201.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       LEGACY
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password:  lir
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 5
        ack.summary.assertSuccess(5, 5, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.201.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.127" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.128 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.255 - 192.168.200.255" }
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.201.0 - 192.168.201.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.201.255")
        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.127", "inetnum", "192.168.200.0 - 192.168.200.127")
        queryObject("-rGBT inetnum 192.168.200.128 - 192.168.200.255", "inetnum", "192.168.200.128 - 192.168.200.255")
        queryObject("-rGBT inetnum 192.168.200.255 - 192.168.200.255", "inetnum", "192.168.200.255 - 192.168.200.255")
        queryObject("-rGBT inetnum 192.168.201.0 - 192.168.201.255", "inetnum", "192.168.201.0 - 192.168.201.255")
    }

    // Create child object with status NOT-SET tests

    def "create child NOT-SET, parent has any status, without override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       NOT-SET
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: owner3
                password: lir
                password: hm
                """.stripIndent()
        )

      then:

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["Status NOT-SET can only be created by the database administrator"]

        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    def "create child NOT-SET, parent has any status, with override"() {
      given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

      expect:
        queryObjectNotFound("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

      when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                override:       denis,override1
                tech-c:       TP1-TEST
                status:       NOT-SET
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Create", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation override used"]

        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
    }

    // misc creation tests

    def "create ASSIGNED PA, parent status LIR-PARTITIONED PA, with several objects in hierarchy"() {
      given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.0.0.0 - 192.255.255.255", "inetnum", "192.0.0.0 - 192.255.255.255")
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        syncUpdate(getTransient("PART-PA") + "password: lir")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")
        syncUpdate(getTransient("SUB-ALLOC-PA") + "password: lir\npassword: owner3")
        queryObject("-r -T inetnum 192.168.128.0 - 192.168.255.255", "inetnum", "192.168.128.0 - 192.168.255.255")
        syncUpdate(getTransient("PART2-PA") + "password: sub")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.255.255", "inetnum", "192.168.200.0 - 192.168.255.255")

      expect:
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
                mnt-by:       SUB-MNT
                source:       TEST

                password: sub
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.255" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
    }

}
