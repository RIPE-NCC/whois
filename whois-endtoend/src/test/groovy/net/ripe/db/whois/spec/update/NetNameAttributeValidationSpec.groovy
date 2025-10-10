package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse

@org.junit.jupiter.api.Tag("IntegrationTest")
class NetNameAttributeValidationSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        ["ALLOC-PA-HM-MNT": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "ALLOC-PA-END-MNT": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PI
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "ALLOC-END": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                """,
         "ASSIGN-END": """\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "V6-ALLOC-PA-HM-MNT": """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
         "V6-ALLOC-PA-END-MNT": """\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-END-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """
        ]
    }

    def "inetnum: modify netname with hm mnt and lir mnt is not possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "The \"netname\" attribute can only be changed by the RIPE NCC"]
    }

    def "inetnum: modify netname with end mnt and lir mnt is possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-END-MNT") + "override: denis,override1")
        queryObject("-GBr -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
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
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == []
    }

    def "inetnum: modify netname attributes with RS mntner"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == []
    }

    def "inetnum: modify netname attributes with override should be possible"() {
        given:
        syncUpdate(getTransient("ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED PA
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:     denis,override1
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == [
                "Authorisation override used"]
    }



    def "inetnum: modify netname attributes with end mtner for assignment"() {
        given:
        syncUpdate(getTransient("ALLOC-END") + "override: denis,override1")
        syncUpdate(getTransient("ASSIGN-END") + "override: denis,override1")
        queryObject("-r -T inetnum 192.168.0.0 - 192.168.255.255", "inetnum", "192.168.0.0 - 192.168.255.255")

        when:
        def message = syncUpdate("""\
                inetnum:      192.168.0.0 - 192.168.255.255
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                descr:        /24 assigned
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ASSIGNED PA
                mnt-by:       RIPE-NCC-END-MNT
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

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.0.0 - 192.168.255.255" }
        ack.infoSuccessMessagesFor("Modify", "[inetnum] 192.168.0.0 - 192.168.255.255") == []
    }

    def "inet6num: modify netname with hm mnt and lir mnt is not possible"() {
        given:
        syncUpdate(getTransient("V6-ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def message = syncUpdate("""
                inet6num:     2001::/20
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                mnt-by:       LIR-MNT
                source:       TEST
                password: lir
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }

        ack.errorMessagesFor("Modify", "[inet6num] 2001::/20") == [
                "The \"netname\" attribute can only be changed by the RIPE NCC"]
    }

    def "inet6num: modify netname with end mnt and lir mnt is possible"() {
        given:
        syncUpdate(getTransient("V6-ALLOC-PA-END-MNT") + "override: denis,override1")
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def message = syncUpdate("""
                inet6num:     2001::/20
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-END-MNT
                mnt-lower:    LIR-MNT
                mnt-by:       LIR-MNT
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
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001::/20") == []
    }

    def "inet6num: modify netname attributes with RS mntner"() {
        given:
        syncUpdate(getTransient("V6-ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                password: hm
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001::/20") == []
    }

    def "inet6num: modify netname attributes with override should be possible"() {
        given:
        syncUpdate(getTransient("V6-ALLOC-PA-HM-MNT") + "override: denis,override1")
        queryObject("-GBr -T inet6num 2001::/20", "inet6num", "2001::/20")

        when:
        def message = syncUpdate("""\
                inet6num:     2001::/20
                netname:      TEST-NET-NAME-MODIFIED
                descr:        TEST network
                country:      NL
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED-BY-RIR
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                override:     denis,override1
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet6num] 2001::/20" }
        ack.infoSuccessMessagesFor("Modify", "[inet6num] 2001::/20") == [
                "Authorisation override used"]
    }
}

