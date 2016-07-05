package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.experimental.categories.Category(IntegrationTest.class)
class InetnumStatusNotSetSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
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
                mnt-by:       LIR-MNT
                mnt-lower:    LIR2-MNT
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
                mnt-by:       LIR-MNT
                source:       TEST
                """,
                "NOTSET": """\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     NOT-SET
                mnt-by:     LIR-MNT
                source:     TEST
                """,
                "NOTSET-200": """\
                inetnum:    192.168.200.0 - 192.168.200.50
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     NOT-SET
                mnt-by:     LIR-MNT
                source:     TEST
                """
        ]}

    def "change NOT-SET status to allowed statuses with ALLOCATED PA parent"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse(sprintf("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     %s
                mnt-by:     LIR-MNT
                source:     TEST

                password: hm
                password: lir
                """.stripIndent(), status));

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }

        queryObject("-rGBT inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")

        where:
        status << [
                "LIR-PARTITIONED PA",
                "SUB-ALLOCATED PA",
                "ASSIGNED PA"
        ]
    }

    def "change NOT-SET status to disallowed statuses with ALLOCATED PA parent"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse(sprintf("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                org:        ORG-LIR1-TEST
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     %s
                mnt-by:     LIR-MNT
                source:     TEST

                password: hm
                password: lir
                password: owner3
                """.stripIndent(), status));

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == ["inetnum parent has incorrect status: ALLOCATED PA"]

        where:
        status << [
                "LIR-PARTITIONED PI",
                "ASSIGNED PI",
                "ALLOCATED PI",
                "ALLOCATED PA"
        ]
    }

    def "change NOT-SET status to ALLOCATED UNSPECIFIED with ALLOCATED PA parent"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     ALLOCATED UNSPECIFIED
                mnt-by:     LIR-MNT
                source:     TEST

                password: hm
                password: lir
                """.stripIndent());

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == [
                "Missing required \"org:\" attribute",
                "Status ALLOCATED UNSPECIFIED can only be created by the database administrator",
                "inetnum parent has incorrect status: ALLOCATED PA"]
    }

    def "change NOT-SET status to ASSIGNED ANYCAST with ALLOCATED PA parent"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     ASSIGNED ANYCAST
                mnt-by:     LIR-MNT
                source:     TEST

                password: hm
                password: lir
                """.stripIndent());

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == [
                "Status ASSIGNED ANYCAST can only be created by the database administrator",
                "inetnum parent has incorrect status: ALLOCATED PA"]
    }

    def "change NOT-SET status to LEGACY with ALLOCATED PA parent"() {
        given:
        syncUpdate(getTransient("ALLOC-PA") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     LEGACY
                mnt-by:     LIR-MNT
                source:     TEST

                password: hm
                password: lir
                """.stripIndent());

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(2, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == [
                "Only RIPE NCC can create/delete a top level object with status 'LEGACY' Contact legacy@ripe.net for more info",
                "inetnum parent has incorrect status: ALLOCATED PA"]
    }

    def "change NOT-SET status to ASSIGNED PA with ASSIGNED PA parent"() {
        given:
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: lir\npassword: hm")
        syncUpdate(getTransient("NOTSET-200") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.50", "inetnum", "192.168.200.0 - 192.168.200.50")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:    192.168.200.0 - 192.168.200.50
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     ASSIGNED PA
                mnt-by:     LIR-MNT
                source:     TEST

                password: lir
                """.stripIndent());

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.50" }

        queryObject("-rGBT inetnum 192.168.200.0 - 192.168.200.50", "inetnum", "192.168.200.0 - 192.168.200.50")
    }

    def "change NOT-SET to disallowed statuses with ASSIGNED PA parent"() {
        given:
        syncUpdate(getTransient("ASS-END") + "password: end\npassword: lir\npassword: hm")
        syncUpdate(getTransient("NOTSET-200") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.255", "inetnum", "192.168.200.0 - 192.168.200.255")
        queryObject("-r -T inetnum 192.168.200.0 - 192.168.200.50", "inetnum", "192.168.200.0 - 192.168.200.50")

        when:
        def ack = syncUpdateWithResponse(sprintf("""\
                inetnum:    192.168.200.0 - 192.168.200.50
                netname:    TEST-NETNAME
                org:        ORG-LIR1-TEST
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     %s
                mnt-by:     LIR-MNT
                source:     TEST

                password: lir
                password: owner3
                """.stripIndent(), status));

        then:
        ack.errors

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inetnum] 192.168.200.0 - 192.168.200.50" }
        ack.errorMessagesFor("Modify", "[inetnum] 192.168.200.0 - 192.168.200.50") == ["inetnum parent has incorrect status: ASSIGNED PA"]

        where:
        status << [
                "SUB-ALLOCATED PA",
                "LIR-PARTITIONED PA",
                "LIR-PARTITIONED PI",
                "ASSIGNED PI",
                "ALLOCATED PA",
                "ALLOCATED PI"
        ]
    }

    def "delete resource with NOT-SET status"() {
        given:
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")

        when:
        def ack = syncUpdateWithResponse("""\
                inetnum:    192.168.1.0 - 192.168.1.255
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     NOT-SET
                mnt-by:     LIR-MNT
                source:     TEST
                delete: testing

                password: hm
                password: lir
                """.stripIndent());

        then:
        ack.success

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inetnum] 192.168.1.0 - 192.168.1.255" }

        queryObjectNotFound("-rGBT inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
    }
}