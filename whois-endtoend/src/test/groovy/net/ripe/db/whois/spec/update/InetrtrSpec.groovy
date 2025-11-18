package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class InetrtrSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "test.net":"""\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST
                """,
            "AS250":"""\
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                remarks:        remarkable
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                notify:         noreply@ripe.net
                mnt-by:         RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "AS222 - AS333":"""\
                as-block:       AS222 - AS333
                descr:          ARIN ASN block
                remarks:        These AS numbers are further assigned by ARIN
                remarks:        to ARIN members and end-users in the ARIN region.
                remarks:        Authoritative registration information for AS
                remarks:        Numbers within this block remains in the ARIN
                remarks:        whois database: whois.arin.net or
                remarks:        web site: http://www.arin.net
                remarks:        You may find aut-num objects for AS Numbers
                remarks:        within this block in the RIPE Database where a
                remarks:        routing policy is published in the RIPE Database
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST
                """,
            "RTRS-TESTNET":"""\
                rtr-set:      RTRS-TESTNET
                descr:        Company
                descr:        Router Set
                mbrs-by-ref:  TST-MNT2
                mbrs-by-ref:  TST-MNT3
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       TST-MNT
                source:       TEST
            """,
            "AS28816":"""\
                rtr-set:      RTRS-TESTNET:AS28816
                descr:        Company
                descr:        Router Set
                mbrs-by-ref:  TST-MNT2
                mbrs-by-ref:  TST-MNT3
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       TST-MNT
                source:       TEST
            """,
            "test2.net":"""\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST
            """,
            "INET6NUM-2001":"""\
                inet6num:    2001::/64
                netname:     RIPE-NCC
                descr:       some descr
                country:     DK
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                status:      ASSIGNED
                mnt-by:      TST-MNT
                source:      TEST
            """
    ]}

    def "create inetrtr"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
        expect:
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")

        when:
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
        then:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT")
    }

    def "create inetrtr with all values"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router description1
                descr:       test router description2
                alias:       Company.net
                alias:       Company2.net
                alias:       Company3.net
                alias:       Company4.net
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                ifaddr:      128.86.1.2 masklen 24
                ifaddr:      193.63.94.2 masklen 24
                interface:   1.2.3.4 masklen 30 action community.append(12356:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     MPBGP 2001:658:21E::2 asno(AS8627)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                remarks:     =======================================================
                remarks:     Remarkable
                remarks:     =======================================================
                org:         ORG-OTO1-TEST
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                admin-c:     TP2-TEST
                tech-c:      TP2-TEST
                mnt-by:      TST-MNT2
                notify:      dbtest@ripe.net
                source:      TEST

                password: test2
                password: owner3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT2")
    }

    def "create inetrtr, alias value with a trailing dot"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                alias:       Company.net.
                alias:       Company3.net
                alias:       Company4.net.
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   1.2.3.4 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     MPBGP 2001:658:21E::2 asno(AS8627)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT2
                notify:      dbtest@ripe.net
                source:      TEST

                password: test2
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 2)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net", "alias:\\s*Company.net")
    }

    def "create inetrtr with primary key with a trailing dot"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
        expect:
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net.
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT")
        query_object_not_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","inet-rtr:\\s*test.net.")
    }

    def "create inetrtr with member-of an rtr-set"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT3")
    }

    def "create inetrtr with member-of a non-existing rtr-set"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObjectNotFound("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        ack.errorMessagesFor("Create", "[inet-rtr] test.net") ==
                ["Unknown object referenced RTRS-TESTNET"]
    }

    def "delete rtr-set which is referenced by an inet-rtr in a member-of"() {
        given:
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = syncUpdate(
                """\
                rtr-set:      RTRS-TESTNET
                descr:        Company
                descr:        Router Set
                mbrs-by-ref:  TST-MNT2
                mbrs-by-ref:  TST-MNT3
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       TST-MNT
                source:       TEST
                delete:       test delete

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Delete" && it.key == "[rtr-set] RTRS-TESTNET" }
        queryObject("-rGBT rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
        ack.errorMessagesFor("Delete", "[rtr-set] RTRS-TESTNET") ==
                ["Object [rtr-set] RTRS-TESTNET is referenced from other objects"]
    }

    def "delete inetrtr"() {
        given:
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = syncUpdate(
                """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST
                delete:      test delete

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[inet-rtr] test2.net" }
        queryObjectNotFound("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
    }

    def "create inetrtr referencing hierarchial rtr-set"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("AS28816") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET:AS28816", "rtr-set", "RTRS-TESTNET:AS28816")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET:AS28816
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT3")
    }

    def "create inetrtr with non-existing local-as value"() {
        given:
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObjectNotFound("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","local-as:\\s*AS250")
    }

    def "create inetrtr, query non-existing ifaddr IP"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inetnum 146.188.49.14","inetnum","0.0.0.0 - 255.255.255.255")
            query_object_not_matches("-r -T inetnum 146.188.49.14","inetnum","0.0.0.0 - 255.255.255.255","146.188.49.14")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","ifaddr:\\s*146.188.49.14 masklen")
    }

    def "create inetrtr, query non-existing interface IP"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inetnum 1.2.3.4","inetnum","0.0.0.0 - 255.255.255.255")
            query_object_not_matches("-r -T inetnum 1.2.3.4","inetnum","0.0.0.0 - 255.255.255.255","1.2.3.4")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   1.2.3.4 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","interface:\\s*1.2.3.4 masklen")
    }

    def "create inetrtr, query existing interface"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("INET6NUM-2001") + "password:test\npassword:hm")
        expect:
            queryObject("-r -T as-block AS222", "as-block", "AS222 - AS333")
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet6num 2001::","inet6num","2001::")
            queryObjectNotFound("-r -T inet6num 2001::","inet6num","0::/0")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","interface:\\s*2001:: masklen")
    }

    def "create inetrtr, query non-existing interface"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T as-block AS222", "as-block", "AS222 - AS333")
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObjectNotFound("-r -T inet6num 2001::","inet6num","2001::")
            queryObjectNotFound("-r -T inet6num 2001::","inet6num","0::/0")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","interface:\\s*2001:: masklen")
    }

    def "create inetrtr without local-as attribute"() {
        given:
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObjectNotFound("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        ack.errorMessagesFor("Create", "[inet-rtr] test.net") ==
                ["Mandatory attribute \"local-as\" is missing"]
    }

    def "create inetrtr with member-of an rtr-set which has NO mbrs-by-ref: TST-MNT"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
        expect:
            queryObject("-r -T as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test

                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)

        ack.errors.any { it.operation == "Create" && it.key == "[inet-rtr] test.net" }
        queryObjectNotFound("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        ack.errorMessagesFor("Create", "[inet-rtr] test.net") ==
                ["Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [RTRS-TESTNET]"]
    }

    def "modify inetrtr change description"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test.net") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router description
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","descr:\\s*test router description")
    }

    def "modify inetrtr, change ifaddr value with invalid syntax"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 31 action
                interface:   2001:: masklen 30 action community.append(12356:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet-rtr] test2.net" }
        query_object_not_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","ifaddr:\\s*146.188.49.14 masklen 31 action")
        ack.errorMessagesFor("Modify","[inet-rtr] test2.net") == ["Syntax error in 146.188.49.14 masklen 31 action"]
    }

    def "modify inetrtr, change interface value with invalid syntax"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:1578:200:FFFF::2 masklen 129
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet-rtr] test2.net" }
        query_object_not_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","interface:\\s*2001:1578:200:FFFF::2 masklen 129")
        ack.errorMessagesFor("Modify","[inet-rtr] test2.net") == ["Syntax error in 2001:1578:200:FFFF::2 masklen 129"]
    }

    def "modify inetrtr, change peer value with invalid syntax"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(13456:20);
                peer:        BGP4 192.168.1.2 asno(PeerAS_), flap_damp()
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet-rtr] test2.net" }
        query_object_not_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","peer:\\s*BGP4 192.168.1.2 asno(PeerAS_), flap_damp()")
        ack.errorMessagesFor("Modify","[inet-rtr] test2.net") == ["Syntax error in BGP4 192.168.1.2 asno(PeerAS_), flap_damp()"]
    }

    def "modify inetrtr, change mp-peer value with invalid syntax"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                interface:   2001:: masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     146.188.49.13 asno(AS7775535)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[inet-rtr] test2.net" }

        queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        queryObject("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net")
        query_object_not_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","mp-peer:\\s*146.188.49.13 asno(AS7775535)")
        ack.errorMessagesFor("Modify","[inet-rtr] test2.net") == ["Syntax error in 146.188.49.13 asno(AS7775535)"]
    }

    def "modify inetrtr with valid syntax"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("RTRS-TESTNET") + "password:test")
            syncUpdate(getTransient("test2.net") + "password:test3")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T rtr-set RTRS-TESTNET", "rtr-set", "RTRS-TESTNET")
            queryObject("-r -T inet-rtr test2.net", "inet-rtr", "test2.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test2.net
                descr:       test router
                ifaddr:      146.188.49.14 masklen 31
                interface:   2001:: masklen 31
                peer:        BGP4 rtrs-ibgp-peers asno(AS3333), flap_damp()
                mp-peer:     BGP4 192.168.1.2 asno(PeERaS)
                member-of:   RTRS-TESTNET
                local-as:    AS250
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT3
                notify:      dbtest@ripe.net
                source:      TEST

                password: test3
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[inet-rtr] test2.net" }
        queryObject("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net")
        query_object_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","ifaddr:\\s*146.188.49.14 masklen 31")
        query_object_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","interface:\\s*2001:: masklen 31")
        query_object_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","peer:\\s*BGP4 rtrs-ibgp-peers asno\\(AS3333\\), flap_damp\\(\\)")
        query_object_matches("-rGBT inet-rtr test2.net", "inet-rtr", "test2.net","mp-peer:\\s*BGP4 192.168.1.2 asno\\(PeERaS\\)")
    }

    def "modify inetrtr remove mandatory attributes admin-c,tech-c"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test.net") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T inet-rtr test.net", "inet-rtr", "test.net")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(2, 1, 0)

        ack.errors.any { it.operation == "Modify" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        ack.errorMessagesFor("Modify", "[inet-rtr] test.net") ==
                ["Mandatory attribute \"admin-c\" is missing",
                    "Mandatory attribute \"tech-c\" is missing"]
    }

    def "modify inetrtr change ifaddr"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test.net") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T inet-rtr test.net", "inet-rtr", "test.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.13 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","ifaddr:\\s*146.188.49.13 masklen")
    }

    def "modify inetrtr change mnt-by, old mnt-by pw supplied"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "password:dbm")
            syncUpdate(getTransient("AS250") + "password:hm\npassword:locked\npassword:owner3")
            syncUpdate(getTransient("test.net") + "password:test")
        expect:
            queryObject("-r -T aut-num AS250", "aut-num", "AS250")
            queryObject("-r -T inet-rtr test.net", "inet-rtr", "test.net")
        when:
        def message = send new Message(
                subject: "",
                body: """\
                inet-rtr:    test.net
                descr:       test router
                local-as:    AS250
                ifaddr:      146.188.49.14 masklen 30 action community.append(12456:20);
                peer:        BGP4 rtrs--my-test:AS7775535:rtrs-test asno (AS7775535)
                mp-peer:     BGP4 146.188.49.13 asno(AS7775535)
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      TST-MNT2
                notify:      dbtest@ripe.net
                source:      TEST

                password: test
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        ack.successes.any { it.operation == "Modify" && it.key == "[inet-rtr] test.net" }
        queryObject("-rGBT inet-rtr test.net", "inet-rtr", "test.net")
        query_object_matches("-rGBT inet-rtr test.net", "inet-rtr", "test.net","mnt-by:\\s*TST-MNT2")
    }
}
