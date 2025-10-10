package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class RouteSetSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "ASN123": """\
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                source:         TEST
                """,
            "ASN352": """\
                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                mnt-lower:      owner2-mnt
                source:         TEST
                """,
            "ROUTE-SET": """\
                route-set:    AS7775535:RS-CUSTOMERS
                descr:        test route-set
                members:      47.247.0.0/16,
                              rs-customers:AS123234:rs-test
                mp-members:   2001:1578::/32,
                              192.233.33.0/24^+,
                              AS123:rs-customers
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "ROUTE-SET-2LEVEL": """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "ROUTE-SET-3LEVEL": """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER3-MNT
                mnt-lower:    LIR3-MNT
                source:  TEST
                """,
            "TOP-ROUTE-SET": """\
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "TEST-TEST": """\
                route-set:    RS-CUSTOMERS:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "ASB16":"""\
                as-block:       AS0 - AS65535
                descr:          ASN block
                remarks:        yes
                org:            ORG-OTO1-TEST
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-LOCKED-MNT
                source:         TEST
                """,
    ]}

    def "create top level route-set object"() {
      expect:
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def ack = syncUpdateWithResponse("""
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                members:      47.247.0.0/16,
                              rs-customers:AS123234:rs-test
                mp-members:   2001:1578::/32
                mp-members:   212.5.128.0/19^19-24
                mp-members:   rs-customers:AS123234:rs-test^+
                mp-members:   2001:1578::/32, 2002:1578::/32
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true))

      then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS"}

        queryObject("-rBT route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
    }

    def "create top level route-set object with ASNs & as-sets in members & mp-members"() {
      expect:
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                members:      128.9.0.0/16, 213.137.33.0/24,
                              rs-test
                mp-members:   2001:1578::/32, RS-KROOT-V6
                mp-members:   rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS"}

        queryObject("-rBT route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
    }

    def "create route-set objects with invalid members & mp-members"() {
      expect:
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                members:      47.2479.0.0/16,
                              rs-customers:AS123234:as-test
                members:      209.239.78.0/23^23-22
                members:      2001:1568::/32
                mp-members:   2001::1578::/32
                mp-members:   2001:15789::/32
                mp-members:   RS-KROOT-V6::rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(7, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] RS-CUSTOMERS") == [
                "Syntax error in 47.2479.0.0/16",
                "Syntax error in rs-customers:AS123234:as-test",
                "Syntax error in 209.239.78.0/23^23-22",
                "Syntax error in 2001:1568::/32",
                "Syntax error in 2001::1578::/32",
                "Syntax error in 2001:15789::/32",
                "Syntax error in RS-KROOT-V6::rs-test"]

        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
    }

    def "create 32 bit route-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("ROUTE-SET"))

      expect:
        queryObject("-r -T route-set AS7775535:RS-CUSTOMERS", "route-set", "AS7775535:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS7775535:RS-CUSTOMERS:AS94967295
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS7775535:RS-CUSTOMERS:AS94967295"}

        queryObject("-rBT route-set AS7775535:RS-CUSTOMERS:AS94967295", "route-set", "AS7775535:RS-CUSTOMERS:AS94967295")
    }

    def "create 32 bit route-set object, 32 bit parent ASN does NOT exist"() {
      expect:
        queryObjectNotFound("-r -T route-set AS7775535:RS-CUSTOMERS", "route-set", "AS7775535:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS7775535:RS-CUSTOMERS:AS94967295
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS7775535:RS-CUSTOMERS:AS94967295"}
        ack.errorMessagesFor("Create", "[route-set] AS7775535:RS-CUSTOMERS:AS94967295") == [
                "Parent object AS7775535:RS-CUSTOMERS not found"]

        queryObjectNotFound("-rGB -T route-set AS7775535:RS-CUSTOMERS:AS94967295", "route-set", "AS7775535:RS-CUSTOMERS:AS94967295")
    }

    def "create route-set object with existing parent ASN, parent mnt-by auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        queryObject("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "create route-set object with existing parent ASN, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS352:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS352:RS-CUSTOMERS"}

        queryObject("-rBT route-set AS352:RS-CUSTOMERS", "route-set", "AS352:RS-CUSTOMERS")
    }

    def "create route-set object with existing parent ASN, parent mnt-by auth supplied, parent mnt-lower exists"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS352:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS352:RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] AS352:RS-CUSTOMERS") == [
                "Authorisation for parent [aut-num] AS352 failed using \"mnt-lower:\" not authenticated by: OWNER2-MNT"]

        queryObjectNotFound("-rBT route-set AS352:RS-CUSTOMERS", "route-set", "AS352:RS-CUSTOMERS")
    }

    def "create route-set object with existing parent ASN, no parent auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS") == [
                "Authorisation for parent [aut-num] AS123 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "create route-set object with no existing parent ASN"() {
      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "create 3 level route-set object with existing 2 level parent set, parent auth supplied"() {
      given:
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2"}

        queryObject("-rBT route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create 3 level route-set object with existing 2 level parent set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2") == [
                "Authorisation for parent [route-set] AS123:RS-CUSTOMERS failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create 3 level route-set object, no 2 level parent set exists"() {
      expect:
        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2") == [
                "Parent object AS123:RS-CUSTOMERS not found"]

        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create route-set object with existing parent route-set, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("TOP-ROUTE-SET"))

      expect:
        queryObject("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS:RS-CUSTOMERS2"}

        queryObject("-rBT route-set RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create route-set object with existing parent route-set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("TOP-ROUTE-SET"))

      expect:
        queryObject("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS:RS-CUSTOMERS2"}
        ack.errorMessagesFor("Create", "[route-set] RS-CUSTOMERS:RS-CUSTOMERS2") == [
                "Authorisation for parent [route-set] RS-CUSTOMERS failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT route-set RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create route-set object with no existing parent route-set"() {
      expect:
        queryObjectNotFound("-r -T aut-num RS-CUSTOMERS", "aut-num", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS:RS-CUSTOMERS2"}
        ack.errorMessagesFor("Create", "[route-set] RS-CUSTOMERS:RS-CUSTOMERS2") == [
                "Parent object RS-CUSTOMERS not found"]

        queryObjectNotFound("-rBT route-set RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create 3 level route-set obj, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2") == [
                "Parent object AS123:RS-CUSTOMERS not found"]

        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create top level parent ASN when child route-set exists, pw for parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def ack = syncUpdateWithResponse("""\
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                source:         TEST

                password: owner
                password: owner3
                password: locked
                """.stripIndent(true)
        )

      then:
        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create parent route-set when child route-set exists, grand parent ASN exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ROUTE-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        queryObject("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "create parent route-set when child route-set exists, grand parent ASN not exists, pw for parent"() {
      given:
        dbfixture(getTransient("ROUTE-SET-3LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] AS123:RS-CUSTOMERS") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "create grand parent ASN and parent route-set when child route-set exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ROUTE-SET-3LEVEL"))

      expect:
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                source:         TEST

                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                password: lir
                password: owner
                password: owner3
                password: locked
                """.stripIndent(true)
        )

      then:
        ack.success
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create parent route-set and grand parent ASN when child route-set exists, pw for parent & grand parent, note wrong order"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ROUTE-SET-3LEVEL"))

      expect:
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")

      when:
        def ack = syncUpdateWithResponse("""
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                source:         TEST

                password: lir
                password: owner
                password: owner3
                password: locked
                """.stripIndent(true)
        )

      then:
        ack.success
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 2, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create 3 level route-set obj, 2 level parent set exists, grand parent ASN not exists, pw for obj & parent"() {
      given:
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.successes
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] AS123:RS-CUSTOMERS:RS-CUSTOMERS2"}

        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create 3 level route-set obj with double ASN, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:AS123", "route-set", "AS123:AS123")
        queryObjectNotFound("-r -T route-set AS123:AS123:RS-CUSTOMERS", "route-set", "AS123:AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:AS123:RS-CUSTOMERS"}
        ack.errorMessagesFor("Create", "[route-set] AS123:AS123:RS-CUSTOMERS") == [
                "Parent object AS123:AS123 not found"]

        queryObjectNotFound("-r -T route-set AS123:AS123:RS-CUSTOMERS", "route-set", "AS123:AS123:RS-CUSTOMERS")
    }

    def "create 2 level route-set obj with double ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:AS123", "route-set", "AS123:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:AS123
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123:AS123"}
        ack.errorMessagesFor("Create", "[route-set] AS123:AS123") == [
                "Syntax error in AS123:AS123"]

        queryObjectNotFound("-r -T route-set AS123:AS123", "route-set", "AS123:AS123")
    }

    def "create route-set obj with only ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:", "route-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[route-set] AS123"}
        ack.errorMessagesFor("Create", "[route-set] AS123") == [
                "Syntax error in AS123"]

        queryObjectNotFound("-r -T route-set AS123", "route-set", "AS123")
    }

    def "create 2 level route-set object with existing parent set, ASN in set name not exist, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-ROUTE-SET"))

      expect:
        queryObject("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS:AS123", "route-set", "RS-CUSTOMERS:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS:AS123
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS:AS123"}

        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set RS-CUSTOMERS:AS123", "route-set", "RS-CUSTOMERS:AS123")
    }

    def "create 2 level route-set object with existing parent set, repeated name part, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-ROUTE-SET"))

      expect:
        queryObject("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS:RS-CUSTOMERS", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                password: lir
                password: lir2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS:RS-CUSTOMERS"}

        queryObject("-r -T route-set RS-CUSTOMERS:RS-CUSTOMERS", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS")
    }

    def "modify route-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set updated
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        query_object_matches("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS", "test route-set updated")
    }

    def "modify route-set object, no parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set updated
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        query_object_matches("-rBT route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS", "test route-set updated")
    }

    def "delete route-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       testing

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
    }

    def "delete parent ASN object, child route-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         owner-MNT
                source:         TEST
                delete:       testing

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[aut-num] AS123"}

        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
    }

    def "delete parent route-set object, child route-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-ROUTE-SET"))
        dbfixture(getTransient("TEST-TEST"))

      expect:
        queryObject("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
        queryObject("-r -T route-set RS-CUSTOMERS:RS-CUSTOMERS", "route-set", "RS-CUSTOMERS:RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       testing

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[route-set] RS-CUSTOMERS"}

        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
    }

    def "delete parent route-set object, child route-set exists, grand parent ASN exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ROUTE-SET-2LEVEL"))
        dbfixture(getTransient("ROUTE-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    AS123:RS-CUSTOMERS
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       testing

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Delete" && it.key == "[route-set] AS123:RS-CUSTOMERS"}

        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")
        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

    def "create route-set object with all optional attrs"() {
      expect:
        queryObjectNotFound("-r -T route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route-set:    RS-CUSTOMERS
                descr:        test route-set
                members:      47.247.0.0/16,
                              rs-customers:AS123234:rs-test:RS-CUSTOMERS2
                tech-c:       TP2-TEST
                mp-members:   2001:1578::/32,
                              rs-AS12-customers
                mbrs-by-ref:  ANY
                org:          ORG-OTO1-TEST
                notify:       dbtest@ripe.net
                mbrs-by-ref:  tst-mnt
                tech-c:       TP1-TEST
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                admin-c:      TP3-TEST
                notify:       unread@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    SUB-MNT

                password: lir2
                password: owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[route-set] RS-CUSTOMERS"}

        queryObject("-rBT route-set RS-CUSTOMERS", "route-set", "RS-CUSTOMERS")
    }

    def "create 3 level route-set obj, no 2 level parent set exists, grand parent ASN exists, override used"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T route-set AS123:RS-CUSTOMERS", "route-set", "AS123:RS-CUSTOMERS")

      when:
        def message = syncUpdate("""\
                route-set:    AS123:RS-CUSTOMERS:RS-CUSTOMERS2
                descr:        test route-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                override:     denis,override1
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)

        queryObject("-r -T route-set AS123:RS-CUSTOMERS:RS-CUSTOMERS2", "route-set", "AS123:RS-CUSTOMERS:RS-CUSTOMERS2")
    }

}
