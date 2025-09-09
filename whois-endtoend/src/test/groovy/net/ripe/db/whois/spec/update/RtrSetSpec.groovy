package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class RtrSetSpec extends BaseQueryUpdateSpec {

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
            "RTR-SET": """\
                rtr-set:      AS7775535:rtrs-foo
                descr:        test rtr-set
                members:      rtr1.isp.net,
                              rtrs-foo:AS123234:rtrs-test,
                              10.233.33.1,
                mp-members:   2001:1578::/32,
                              192.233.33.1,
                              AS123:rtrs-foo
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "RTR-SET-2LEVEL": """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "RTR-SET-3LEVEL": """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER3-MNT
                mnt-lower:    LIR3-MNT
                source:  TEST
                """,
            "TOP-RTR-SET": """\
                rtr-set:      rtrs-foo
                descr:        test rtr-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "TEST-TEST": """\
                rtr-set:      rtrs-foo:rtrs-foo
                descr:        test rtr-set
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

    def "create top level rtr-set object"() {
      expect:
        queryObjectNotFound("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo
                descr:        test rtr-set
                members:      rtr1.isp.net,
                              rtrs-foo:AS123234:rtrs-test,
                              10.233.128.1
                mp-members:   2001:1578::/32
                mp-members:   212.5.128.1
                mp-members:   rtrs-foo:AS123234:rtrs-test
                mp-members:   2001:1578::/32, 2002:1578::/32
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo"}

        queryObject("-rBT rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
    }

    def "create rtr-set objects with invalid members & mp-members"() {
      expect:
        queryObjectNotFound("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo
                descr:        test rtr-set
                members:      47.2479.0.0/16,
                              rtrs-foo:AS123234:as-test
                members:      2001:1568::/32
                mp-members:   2001::1578::/32
                mp-members:   2001:15789::/32
                mp-members:   RS-KROOT-V6::rtrs-test
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

        ack.countErrorWarnInfo(6, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] rtrs-foo") == [
                "Syntax error in 47.2479.0.0/16",
                "Syntax error in rtrs-foo:AS123234:as-test",
                "Syntax error in 2001:1568::/32",
                "Syntax error in 2001::1578::/32",
                "Syntax error in 2001:15789::/32",
                "Syntax error in RS-KROOT-V6::rtrs-test"]

        queryObjectNotFound("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
    }

    def "create 32 bit rtr-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("RTR-SET"))

      expect:
        queryObject("-r -T rtr-set AS7775535:rtrs-foo", "rtr-set", "AS7775535:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS7775535:rtrs-foo:AS94967295
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS7775535:rtrs-foo:AS94967295"}

        queryObject("-rBT rtr-set AS7775535:rtrs-foo:AS94967295", "rtr-set", "AS7775535:rtrs-foo:AS94967295")
    }

    def "create 32 bit rtr-set object, 32 bit parent ASN does NOT exist"() {
      expect:
        queryObjectNotFound("-r -T rtr-set AS7775535:rtrs-foo", "rtr-set", "AS7775535:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS7775535:rtrs-foo:AS94967295
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS7775535:rtrs-foo:AS94967295"}
        ack.errorMessagesFor("Create", "[rtr-set] AS7775535:rtrs-foo:AS94967295") == [
                "Parent object AS7775535:rtrs-foo not found"]

        queryObjectNotFound("-rGB -T rtr-set AS7775535:rtrs-foo:AS94967295", "rtr-set", "AS7775535:rtrs-foo:AS94967295")
    }

    def "create rtr-set object with existing parent ASN, parent mnt-by auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}

        queryObject("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "create rtr-set object with existing parent ASN, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS352:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS352:rtrs-foo"}

        queryObject("-rBT rtr-set AS352:rtrs-foo", "rtr-set", "AS352:rtrs-foo")
    }

    def "create rtr-set object with existing parent ASN, parent mnt-by auth supplied, parent mnt-lower exists"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS352:rtrs-foo
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS352:rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] AS352:rtrs-foo") == [
                "Authorisation for parent [aut-num] AS352 failed using \"mnt-lower:\" not authenticated by: OWNER2-MNT"]

        queryObjectNotFound("-rBT rtr-set AS352:rtrs-foo", "rtr-set", "AS352:rtrs-foo")
    }

    def "create rtr-set object with existing parent ASN, no parent auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo") == [
                "Authorisation for parent [aut-num] AS123 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "create rtr-set object with no existing parent ASN"() {
      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "create 3 level rtr-set object with existing 2 level parent set, parent auth supplied"() {
      given:
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo:rtrs-foo2"}

        queryObject("-rBT rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create 3 level rtr-set object with existing 2 level parent set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo:rtrs-foo2"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo:rtrs-foo2") == [
                "Authorisation for parent [rtr-set] AS123:rtrs-foo failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create 3 level rtr-set object, no 2 level parent set exists"() {
      expect:
        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo:rtrs-foo2"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo:rtrs-foo2") == [
                "Parent object AS123:rtrs-foo not found"]

        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create rtr-set object with existing parent rtr-set, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("TOP-RTR-SET"))

      expect:
        queryObject("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo:rtrs-foo2"}

        queryObject("-rBT rtr-set rtrs-foo:rtrs-foo2", "rtr-set", "rtrs-foo:rtrs-foo2")
    }

    def "create rtr-set object with existing parent rtr-set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("TOP-RTR-SET"))

      expect:
        queryObject("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo:rtrs-foo2"}
        ack.errorMessagesFor("Create", "[rtr-set] rtrs-foo:rtrs-foo2") == [
                "Authorisation for parent [rtr-set] rtrs-foo failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT rtr-set rtrs-foo:rtrs-foo2", "rtr-set", "rtrs-foo:rtrs-foo2")
    }

    def "create rtr-set object with no existing parent rtr-set"() {
      expect:
        queryObjectNotFound("-r -T aut-num rtrs-foo", "aut-num", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo:rtrs-foo2"}
        ack.errorMessagesFor("Create", "[rtr-set] rtrs-foo:rtrs-foo2") == [
                "Parent object rtrs-foo not found"]

        queryObjectNotFound("-rBT rtr-set rtrs-foo:rtrs-foo2", "rtr-set", "rtrs-foo:rtrs-foo2")
    }

    def "create 3 level rtr-set obj, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo:rtrs-foo2"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo:rtrs-foo2") == [
                "Parent object AS123:rtrs-foo not found"]

        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create top level parent ASN when child rtr-set exists, pw for parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

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

    def "create parent rtr-set when child rtr-set exists, grand parent ASN exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("RTR-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}

        queryObject("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "create parent rtr-set when child rtr-set exists, grand parent ASN not exists, pw for parent"() {
      given:
        dbfixture(getTransient("RTR-SET-3LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:rtrs-foo") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "create grand parent ASN and parent rtr-set when child rtr-set exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("RTR-SET-3LEVEL"))

      expect:
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")

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

                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create parent rtr-set and grand parent ASN when child rtr-set exists, pw for parent & grand parent, note wrong order"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("RTR-SET-3LEVEL"))

      expect:
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")

      when:
        def ack = syncUpdateWithResponse("""
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create 3 level rtr-set obj, 2 level parent set exists, grand parent ASN not exists, pw for obj & parent"() {
      given:
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] AS123:rtrs-foo:rtrs-foo2"}

        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create 3 level rtr-set obj with double ASN, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:AS123", "rtr-set", "AS123:AS123")
        queryObjectNotFound("-r -T rtr-set AS123:AS123:rtrs-foo", "rtr-set", "AS123:AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:AS123:rtrs-foo"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:AS123:rtrs-foo") == [
                "Parent object AS123:AS123 not found"]

        queryObjectNotFound("-r -T rtr-set AS123:AS123:rtrs-foo", "rtr-set", "AS123:AS123:rtrs-foo")
    }

    def "create 2 level rtr-set obj with double ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:AS123", "rtr-set", "AS123:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:AS123
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123:AS123"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123:AS123") == [
                "Syntax error in AS123:AS123"]

        queryObjectNotFound("-r -T rtr-set AS123:AS123", "rtr-set", "AS123:AS123")
    }

    def "create rtr-set obj with only ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:", "rtr-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123
                descr:        test rtr-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[rtr-set] AS123"}
        ack.errorMessagesFor("Create", "[rtr-set] AS123") == [
                "Syntax error in AS123"]

        queryObjectNotFound("-r -T rtr-set AS123", "rtr-set", "AS123")
    }

    def "create 2 level rtr-set object with existing parent set, ASN in set name not exist, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-RTR-SET"))

      expect:
        queryObject("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set rtrs-foo:AS123", "rtr-set", "rtrs-foo:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo:AS123
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo:AS123"}

        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set rtrs-foo:AS123", "rtr-set", "rtrs-foo:AS123")
    }

    def "create 2 level rtr-set object with existing parent set, repeated name part, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-RTR-SET"))

      expect:
        queryObject("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
        queryObjectNotFound("-r -T rtr-set rtrs-foo:rtrs-foo", "rtr-set", "rtrs-foo:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo:rtrs-foo"}

        queryObject("-r -T rtr-set rtrs-foo:rtrs-foo", "rtr-set", "rtrs-foo:rtrs-foo")
    }

    def "modify rtr-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                remarks:      updated
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
        ack.successes.any {it.operation == "Modify" && it.key == "[rtr-set] AS123:rtrs-foo"}

        query_object_matches("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo", "updated")
    }

    def "modify rtr-set object, no parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                remarks:      updated
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
        ack.successes.any {it.operation == "Modify" && it.key == "[rtr-set] AS123:rtrs-foo"}

        query_object_matches("-rBT rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo", "updated")
    }

    def "delete rtr-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[rtr-set] AS123:rtrs-foo"}

        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
    }

    def "delete parent ASN object, child rtr-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("RTR-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

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

    def "delete parent rtr-set object, child rtr-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-RTR-SET"))
        dbfixture(getTransient("TEST-TEST"))

      expect:
        queryObject("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
        queryObject("-r -T rtr-set rtrs-foo:rtrs-foo", "rtr-set", "rtrs-foo:rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[rtr-set] rtrs-foo"}

        queryObjectNotFound("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
    }

    def "delete parent rtr-set object, child rtr-set exists, grand parent ASN exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("RTR-SET-2LEVEL"))
        dbfixture(getTransient("RTR-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      AS123:rtrs-foo
                descr:        test rtr-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[rtr-set] AS123:rtrs-foo"}

        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")
        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

    def "create rtr-set object with all optional attrs"() {
      expect:
        queryObjectNotFound("-r -T rtr-set rtrs-foo", "rtr-set", "rtrs-foo")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                rtr-set:      rtrs-foo
                descr:        test rtr-set
                members:      rtr1.isp.net,
                              rtrs-foo:AS123234:rtrs-test:rtrs-foo2
                tech-c:       TP2-TEST
                mp-members:   2001:1578::/32,
                              AS12rtrs-foo
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
        ack.successes.any {it.operation == "Create" && it.key == "[rtr-set] rtrs-foo"}

        queryObject("-rBT rtr-set rtrs-foo", "rtr-set", "rtrs-foo")
    }

    def "create 3 level rtr-set obj, no 2 level parent set exists, grand parent ASN exists, override used"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T rtr-set AS123:rtrs-foo", "rtr-set", "AS123:rtrs-foo")

      when:
        def message = syncUpdate("""\
                rtr-set:      AS123:rtrs-foo:rtrs-foo2
                descr:        test rtr-set
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

        queryObject("-r -T rtr-set AS123:rtrs-foo:rtrs-foo2", "rtr-set", "AS123:rtrs-foo:rtrs-foo2")
    }

}
