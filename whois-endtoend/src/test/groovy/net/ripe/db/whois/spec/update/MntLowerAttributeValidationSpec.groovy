package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec

@org.junit.jupiter.api.Tag("IntegrationTest")
class MntLowerAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["ALLOC-PA-CO-MAINTAINED"     : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ALLOC-PA-RIPE-MAINTAINED"   : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """,
         "ALLOC-PA-LIR-MAINTAINED"    : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ALLOCATED-8"  : """\
                inetnum:      192.0.0.0 - 192.255.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGNED-PA-CO-MAINTAINED"  : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGNED-PA-RIPE-MAINTAINED": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """,
         "ASSIGNED-PA-LIR-MAINTAINED" : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGNED-PI-CO-MAINTAINED"  : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGNED-PI-RIPE-MAINTAINED": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                source:       TEST
                """,
         "ASSIGNED-PI-LIR-MAINTAINED" : """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ALLOC6-PA-CO-MAINTAINED"    : """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ALLOC6-PA-RIPE-MAINTAINED"  : """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                source:       TEST
                """,
         "ALLOC6-PA-LIR-MAINTAINED"   : """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       LIR-MNT
                source:       TEST
                """
        ]
    }

    //  MODIFY mnt-lower attribute by LIR

    def "modify inetnum (allocated pa and co-maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (allocated pa and ripe maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"
        ]
    }


    def "modify inetnum (allocated pa and lir maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (assigned pa and co-maintained): add mnt-lower of lir mnt by lir"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (assigned pa and ripe maintained): add mnt-lower of lir mnt by lir"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"
        ]
    }

    def "modify inetnum (assigned pa and lir maintained): add mnt-lower of lir mnt by lir"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (assigned pi and co-maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ASSIGNED-PI-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") ==
                ["\"mnt-lower:\" attribute not allowed for resources with \"ASSIGNED PI:\" status"]
    }

    def "modify inetnum (assigned pi and ripe maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ASSIGNED-PI-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       RIPE-NCC-END-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-END-MNT",
                "\"mnt-lower:\" attribute not allowed for resources with \"ASSIGNED PI:\" status"
        ]
    }

    def "modify inetnum (assigned pi and lir maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ASSIGNED-PI-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "\"mnt-lower:\" attribute not allowed for resources with \"ASSIGNED PI:\" status"
        ]
    }

    def "modify inet6num (allocated-by-rir and co-maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC6-PA-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
    }

    def "modify inet6num (allocated-by-rir and ripe maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC6-PA-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
        ack.errorMessagesFor("Modify", "[inet6num] 2001::/20") == [
                "Authorisation for [inet6num] 2001::/20 failed using \"mnt-by:\" not authenticated by: RIPE-NCC-HM-MNT"
        ]
    }


    def "modify inet6num (allocated-by-rir and lir maintained): add mnt-lower of lir mnt by lir"() {
        given:
        syncUpdate(getTransient("ALLOC6-PA-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def ack = syncUpdateWithResponse("""\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
    }

    //  MODIFY mnt-lower attribute WITH RS PASSWORD

    def "modify inetnum (allocated pa and co-maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (allocated pa and ripe maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }


    def "modify inetnum (allocated pa and lir maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed using \"mnt-by:\" not authenticated by: LIR-MNT"
        ]
    }


    def "modify inetnum (assigned pa and co-maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-CO-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }

    def "modify inetnum (assigned pa and ripe maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-RIPE-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
    }


    def "modify inetnum (assigned pa and lir maintained): add mnt-lower of lir mnt by ripe"() {
        given:
        dbfixture(getTransient("ALLOCATED-8"))
        syncUpdate(getTransient("ASSIGNED-PA-LIR-MAINTAINED") + "override: denis,override1")

        expect:
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT         # added
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation for [inetnum] 192.168.0.0 - 192.168.255.255 failed using \"mnt-by:\" not authenticated by: LIR-MNT"
        ]
    }
}