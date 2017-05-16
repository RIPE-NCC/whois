package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import spock.lang.Ignore

@org.junit.experimental.categories.Category(IntegrationTest.class)
class InetnumStatusNotSetSpec extends BaseQueryUpdateSpec {

    // in the database the only parent that exists for NOT-SET resources is ALLOCATED UNSPECIFIED
    // and the only child resource status with a NOT-SET resource as a parent is LEGACY
    @Override
    Map<String, String> getTransients() {
        [
                "ALLOC-UNS": """\
                inetnum:      192.168.0.0 - 192.169.255.255
                netname:      TEST-NET-NAME
                descr:        TEST network
                country:      NL
                org:          ORG-HR1-TEST
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                status:       ALLOCATED UNSPECIFIED
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                """,
                "LEGACY-CHILD": """\
                inetnum:    192.168.1.0 - 192.168.1.50
                netname:    TEST-NETNAME
                country:    NL
                admin-c:    TP1-TEST
                tech-c:     TP1-TEST
                status:     LEGACY
                mnt-by:     LIR-MNT
                source:     TEST
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

    //TODO: ALLOCATED should NOT be allowed but currently is
    def "change NOT-SET status to allowed statuses with ALLOCATED UNSPECIFIED parent"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm\npassword: lir")
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

                password: lir
                password: owner3
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
                "ASSIGNED PA",
                "ASSIGNED PI",
                "ALLOCATED PA",
                "ALLOCATED PI",
                "LIR-PARTITIONED PA",
                "LIR-PARTITIONED PI"
        ]
    }

    //other disallowed statuses covered by unrelated tests : ALLOCATED UNSPECIFIED, ASSIGNED ANYCAST, EARLY-REGISTRATION, LEGACY
    def "change NOT-SET status to disallowed statuses with ALLOCATED UNSPECIFIED parent"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm\npassword: lir")
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

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") == ["inetnum parent has incorrect status: ALLOCATED UNSPECIFIED"]

        where:
        status << [
                "SUB-ALLOCATED PA"
        ]
    }

    // NOT-SET statuses only gave LEGACY for children
    def "change NOT-SET status to disallowed statuses with ALLOCATED UNSPECIFIED parent and LEGACY child"() {
        given:
        syncUpdate(getTransient("ALLOC-UNS") + "password: owner3\npassword: hm\npassword: lir")
        syncUpdate(getTransient("NOTSET") + "override: denis,override1")
        syncUpdate(getTransient("LEGACY-CHILD") + "override: denis,override1")

        expect:
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.255", "inetnum", "192.168.1.0 - 192.168.1.255")
        queryObject("-r -T inetnum 192.168.0.0 - 192.169.255.255", "inetnum", "192.168.0.0 - 192.169.255.255")
        queryObject("-r -T inetnum 192.168.1.0 - 192.168.1.50", "inetnum", "192.168.1.0 - 192.168.1.50")

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

        ack.errorMessagesFor("Modify", "[inetnum] 192.168.1.0 - 192.168.1.255") ==
                ["Status " + status + " not allowed when more specific object '192.168.1.0 - 192.168.1.50' has status LEGACY"]

        where:
        status << [
                "ASSIGNED PA",
                "ASSIGNED PI",
                "ALLOCATED PA",
                "ALLOCATED PI",
                "LIR-PARTITIONED PA",
                "LIR-PARTITIONED PI"
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
