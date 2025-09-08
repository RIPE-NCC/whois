package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.jupiter.api.Tag("IntegrationTest")
class NonAllocationAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["ALLOCATED-UNS-192-168"       : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      ALLOCATION-192-168
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "ASSIGNED-PI-192-168-1"          : """\
                inetnum:      192.168.1.0 - 192.168.1.255
                netname:      ASSIGNED-192-168-1
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGNMENT-END-USER": """\
                inetnum:      192.168.2.0 - 192.168.2.255
                netname:      ASSIGNED-192-168-2
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

    def "modify inetnum, change sponsoring-org by lir mntner is not possible"() {
        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNED-PI-192-168-1") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.1.0 - 192.168.1.255", "inetnum", " 192.168.1.0 - 192.168.1.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.1.0 - 192.168.1.255
                netname:      ASSIGNED-192-168-1
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == [
                "The \"sponsoring-org\" attribute can only be changed by the RIPE NCC"]
    }

    def "modify inetnum, change netname (ASSIGNED PI) by lir mntner is possible"() {
        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNED-PI-192-168-1") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.1.0 - 192.168.1.255", "inetnum", " 192.168.1.0 - 192.168.1.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.1.0 - 192.168.1.255
                netname:      DIFFERENT-192-168-1  # changed
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
    }

    def "modify inetnum without ripe mnt, change netname (ASSIGNED PI) by lir mntner is possible"() {
        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNMENT-END-USER") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.2.0 - 192.168.2.255", "inetnum", " 192.168.2.0 - 192.168.2.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.2.0 - 192.168.2.255
                netname:      DIFFERENT-192-168-2          # changed
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.2.0 - 192.168.2.255" }
    }


    def "modify inetnum, add mnt-lower by lir mntner"() {
        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNED-PI-192-168-1") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.1.0 - 192.168.1.255", "inetnum", " 192.168.1.0 - 192.168.1.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.1.0 - 192.168.1.255
                netname:      ASSIGNED-192-168-1
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR2-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                mnt-lower:    LIR2-MNT                     # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == [
                "\"mnt-lower:\" attribute not allowed for resources with \"ASSIGNED PI:\" status"
        ]
    }

    def "modify inetnum without ripe mnt, add mnt-lower by lir mntner"() {
        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNMENT-END-USER") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.2.0 - 192.168.2.255", "inetnum", " 192.168.2.0 - 192.168.2.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.2.0 - 192.168.2.255
                netname:      ASSIGNED-192-168-2
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT        # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.2.0 - 192.168.2.255") == [
                "\"mnt-lower:\" attribute not allowed for resources with \"ASSIGNED PI:\" status"
        ]
    }

    //  MODIFY allocations attributes WITH OVERRIDE

    def "modify inetnum, change sponsoring-org with override is possible"() {

        given:
        syncUpdate(getTransient("ALLOCATED-UNS-192-168") + "override: denis, override1")
        syncUpdate(getTransient("ASSIGNED-PI-192-168-1") + "override: denis, override1")

        expect:
        queryObject("-GBr -T inetnum  192.168.0.0 - 192.168.255.255", "inetnum", " 192.168.0.0 - 192.168.255.255")
        queryObject("-GBr -T inetnum  192.168.1.0 - 192.168.1.255", "inetnum", " 192.168.1.0 - 192.168.1.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.1.0 - 192.168.1.255
                netname:      ASSIGNED-192-168-255
                country:      NL
                org:          ORG-LIR1-TEST
                sponsoring-org: ORG-LIR1-TEST  # changed
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                override: denis,override1
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
    }
}
