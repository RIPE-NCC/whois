package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class OutOfRegionSpec extends BaseQueryUpdateSpec {

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
                "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
//                "PN": """\
//                person:  First Person
//                address: St James Street
//                address: Burnley
//                address: UK
//                phone:   +44 282 420469
//                nic-hdl: FP1-TEST
//                mnt-by:  OWNER-MNT
//                source:  TEST
//                """,
//                "ALLOC-PA": """\
//                inetnum:      192.168.0.0 - 192.169.255.255
//                netname:      TEST-NET-NAME
//                descr:        TEST network
//                country:      NL
//                org:          ORG-LIR1-TEST
//                admin-c:      TP1-TEST
//                tech-c:       TP1-TEST
//                status:       ALLOCATED PA
//                mnt-by:       RIPE-NCC-HM-MNT
//                mnt-lower:    LIR-MNT
//                source:       TEST
//                """,
        ]
    }

    def "not create out of region aut-num"() {
      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                password:   owner3
                """.stripIndent()
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)

      ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
      ack.errorMessagesFor("Create", "[aut-num] AS252") == [
              "Authorisation for [as-block] AS222 - AS333 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT",
              "Cannot create out of region objects"
      ]
      ack.warningMessagesFor("Create", "[aut-num] AS252") ==
              ["Out of region object has wrong source"]

      queryObjectNotFound("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "not create out of region aut-num, nonauth source"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST-NONAUTH

                password:   lir
                password:   owner3
                """.stripIndent()
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(2, 0, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.errorMessagesFor("Create", "[aut-num] AS252") == [
                "Authorisation for [as-block] AS222 - AS333 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT",
                "Cannot create out of region objects"
        ]

        queryObjectNotFound("-rBG -T aut-num AS252", "aut-num", "AS252")
    }

    def "create out of region aut-num, with override"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1

                password:   lir
                password:   owner3
                
                """.stripIndent()
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS252") ==
                ["Out of region object has wrong source"]

        queryObject("-rGBT aut-num AS252", "aut-num", "AS252")
    }

    def "create out of region aut-num, rs-maintainer"() {
        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS252
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-LIR1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   hm
                password:   lir
                password:   owner3
                
                """.stripIndent()
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS252" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS252") ==
                ["Out of region object has wrong source"]

        queryObject("-rGBT aut-num AS252", "aut-num", "AS252")
    }

    def "not create inetnum with nonauth source"() {
        whoisFixture.dumpSchema();
        when:
        def ack = syncUpdateWithResponse("""
                inetnum:     10.1.0.0 - 10.1.255.255
                netname:     invalid-net
                country:     NL
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED PA
                mnt-by:      OWNER-MNT
                source:      TEST-NONAUTH

                password:   owner
                password:   owner2
                
                """.stripIndent()
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 0, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[inetnum] 10.1.0.0 - 10.1.255.255" }
        ack.errorMessagesFor("Create", "[inetnum] 10.1.0.0 - 10.1.255.255") == [
                "Source TEST-NONAUTH is not allowed for inetnum objects"
        ]

        queryObjectNotFound("-r -T inetnum 10.1.0.0 - 10.1.255.255", "inetnum", "10.1.0.0 - 10.1.255.255")
    }

}
