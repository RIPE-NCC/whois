package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.experimental.categories.Category(IntegrationTest.class)
class NonAllocationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["ASSIGN-PI"             : """\
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "NON-TOPLEVEL-ASSIGN-PI": """\
                inetnum:      193.168.255.0 - 193.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "ASSIGNMENT-PI"         : """\
                inetnum:      192.170.0.0 - 192.170.255.255
                netname:      SOME-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "END-USER-ASSIGNMENT"   : """\
                inetnum:      192.180.0.0 - 192.180.255.255
                netname:      SOME-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                source:       TEST
                """
        ]
    }

    def "modify inetnum, change sponsoring-org with lir mntner is not possible"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.255.0 - 192.168.255.255", "inetnum", " 192.168.255.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST  # changed
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent()
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.255.0 - 192.168.255.255") == [
                "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
    }

    def "modify inetnum, change netname with lir mnt is possible for non-toplevel allocations"() {
        given:
        syncUpdate(getTransient("NON-TOPLEVEL-ASSIGN-PI") + "override: denis, override1")


        expect:
        queryObject("-GBr -T inetnum 193.168.255.0 - 193.168.255.255", "inetnum", "193.168.255.0 - 193.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      193.168.255.0 - 193.168.255.255
                netname:      DIFFERENT-TEST-NET-NAME              # changed
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 193.168.255.0 - 193.168.255.255" }
    }

    def "modify assignment inetnum, change netname attribute by lir"() {
        given:
        syncUpdate(getTransient("ASSIGNMENT-PI") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum 192.170.0.0 - 192.170.255.255", "inetnum", "192.170.0.0 - 192.170.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.170.0.0 - 192.170.255.255
                netname:      SOME-OTHER-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.170.0.0 - 192.170.255.255" }
    }


    def "modify inetnum without ripe mnt, change netname attribute by lir"() {
        given:
        syncUpdate(getTransient("END-USER-ASSIGNMENT") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum 192.180.0.0 - 192.180.255.255", "inetnum", "192.180.0.0 - 192.180.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.180.0.0 - 192.180.255.255
                netname:      SOME-OTHER-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.180.0.0 - 192.180.255.255" }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MODIFY allocations attributes WITH OVERRIDE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    def "modify inetnum, change sponsoring-org with override is possible"() {
        given:
        syncUpdate(getTransient("ASSIGN-PI") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.255.0 - 192.168.255.255", "inetnum", " 192.168.255.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""
                inetnum:      192.168.255.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override: denis,override1
                """.stripIndent()
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.255.0 - 192.168.255.255" }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  MODIFY allocations attributes WITH RS PASSWORD
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // .....
}
