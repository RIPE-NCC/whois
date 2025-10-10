package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class AsSetSpec extends BaseQueryUpdateSpec {

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
            "AS-SET": """\
                as-set:       AS7775535:AS-TEST
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "AS-SET-2LEVEL": """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "AS-SET-3LEVEL": """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER3-MNT
                mnt-lower:    LIR3-MNT
                source:  TEST
                """,
            "TOP-AS-SET": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "TEST-TEST": """\
                as-set:       AS-TEST:AS-TEST
                descr:        test as-set
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

    def "create hierarchical as-set object"() {
      given:
        dbfixture(getTransient("ASN123"))
      expect:
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                source:  TEST

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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}

        queryObject("-rBT as-set as123:As-TEst", "as-set", "AS123:AS-TEST")
    }

    def "create top level as-set object fails"() {
        expect:
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.errorMessagesFor("Create", "[as-set] AS-TEST") == [
                "Cannot create AS-SET object with a short format name. Only hierarchical " +
                        "AS-SET creation is allowed, i.e. at least one ASN must be referenced"]
    }

    def "create as-set objects with invalid members"() {
      expect:
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T as-set AS-TEST2", "as-set", "AS-TEST2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS1.1309
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                as-set:       AS-TEST2
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS4294967299
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
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 2, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS-TEST") == [
                "Syntax error in AS1.1309"]
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS-TEST2") == [
                "Syntax error in AS4294967299"]

        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T as-set AS-TEST2", "as-set", "AS-TEST2")
    }

    def "create 32 bit as-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET"))

      expect:
        queryObject("-r -T as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS7775535:AS-TEST:AS94967295
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS7775535:AS-TEST:AS94967295"}

        queryObject("-rBT as-set AS7775535:AS-TEST:AS94967295", "as-set", "AS7775535:AS-TEST:AS94967295")
    }

    def "create >32 bit as-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET"))

      expect:
        queryObject("-r -T as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS7775535:AS-TEST:AS7777777234
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS7775535:AS-TEST:AS7777777234"}
        ack.errorMessagesFor("Create", "[as-set] AS7775535:AS-TEST:AS7777777234") ==
                ["Syntax error in AS7775535:AS-TEST:AS7777777234"]

        queryObjectNotFound("-rBT as-set AS7775535:AS-TEST:AS7777777234", "as-set", "AS7775535:AS-TEST:AS7777777234")
    }

    def "create leading zero as-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET"))

      expect:
        queryObject("-r -T as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS7775535:AS-TEST:AS0777234
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS7775535:AS-TEST:AS0777234"}
        ack.errorMessagesFor("Create", "[as-set] AS7775535:AS-TEST:AS0777234") ==
                ["Syntax error in AS7775535:AS-TEST:AS0777234"]

        queryObjectNotFound("-rBT as-set AS7775535:AS-TEST:AS0777234", "as-set", "AS7775535:AS-TEST:AS0777234")
    }

    def "create asdot as-set object with existing 32 bit parent ASN, parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET"))

      expect:
        queryObject("-r -T as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS7775535:AS-TEST:AS777.234
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS7775535:AS-TEST:AS777.234"}
        ack.errorMessagesFor("Create", "[as-set] AS7775535:AS-TEST:AS777.234") ==
                ["Syntax error in AS7775535:AS-TEST:AS777.234"]

        queryObjectNotFound("-rBT as-set AS7775535:AS-TEST:AS777.234", "as-set", "AS7775535:AS-TEST:AS777.234")
    }

    def "create 32 bit as-set object, 32 bit parent ASN does NOT exist"() {
      expect:
        queryObjectNotFound("-r -T as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS7775535:AS-TEST:AS94967295
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS7775535:AS-TEST:AS94967295"}
        ack.errorMessagesFor("Create", "[as-set] AS7775535:AS-TEST:AS94967295") == [
                "Parent object AS7775535:AS-TEST not found"]

        queryObjectNotFound("-rGB -T as-set AS7775535:AS-TEST:AS94967295", "as-set", "AS7775535:AS-TEST:AS94967295")
    }

    def "create as-set object with existing parent ASN, parent mnt-by auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}

        queryObject("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create as-set object with existing parent ASN, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS352:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS352:AS-TEST"}

        queryObject("-rBT as-set AS352:AS-TEST", "as-set", "AS352:AS-TEST")
    }

    def "create as-set object with existing parent ASN, parent mnt-by auth supplied, parent mnt-lower exists"() {
      given:
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T aut-num AS352", "aut-num", "AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS352:AS-TEST
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS352:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS352:AS-TEST") == [
                "Authorisation for parent [aut-num] AS352 failed using \"mnt-lower:\" not authenticated by: OWNER2-MNT"]

        queryObjectNotFound("-rBT as-set AS352:AS-TEST", "as-set", "AS352:AS-TEST")
    }

    def "create as-set object with existing parent ASN, no parent auth supplied"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST") == [
                "Authorisation for parent [aut-num] AS123 failed using \"mnt-by:\" not authenticated by: OWNER-MNT"]

        queryObjectNotFound("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create as-set object with no existing parent ASN"() {
      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create 3 level as-set object with existing 2 level parent set, parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST:AS-TEST2"}

        queryObject("-rBT as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create 3 level as-set object with existing 2 level parent set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST:AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST:AS-TEST2") == [
                "Authorisation for parent [as-set] AS123:AS-TEST failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create 3 level as-set object, no 2 level parent set exists"() {
      expect:
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST:AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST:AS-TEST2") == [
                "Parent object AS123:AS-TEST not found"]

        queryObjectNotFound("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create as-set object with existing parent as-set, parent mnt-lower auth supplied"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS-TEST:AS-TEST2"}

        queryObject("-rBT as-set AS-TEST:AS-TEST2", "as-set", "AS-TEST:AS-TEST2")
    }

    def "create as-set object with existing parent as-set, no parent auth supplied"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS-TEST:AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS-TEST:AS-TEST2") == [
                "Authorisation for parent [as-set] AS-TEST failed using \"mnt-lower:\" not authenticated by: LIR2-MNT"]

        queryObjectNotFound("-rBT as-set AS-TEST:AS-TEST2", "as-set", "AS-TEST:AS-TEST2")
    }

    def "create as-set object with no existing parent as-set"() {
      expect:
        queryObjectNotFound("-r -T aut-num AS-TEST", "aut-num", "AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS-TEST:AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS-TEST:AS-TEST2") == [
                "Parent object AS-TEST not found"]

        queryObjectNotFound("-rBT as-set AS-TEST:AS-TEST2", "as-set", "AS-TEST:AS-TEST2")
    }

    def "create 3 level as-set obj, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST:AS-TEST2"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST:AS-TEST2") == [
                "Parent object AS123:AS-TEST not found"]

        queryObjectNotFound("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create top level parent ASN when child as-set exists, pw for parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

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

    def "create parent as-set when child as-set exists, grand parent ASN exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("AS-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}

        queryObject("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create parent as-set when child as-set exists, grand parent ASN not exists, pw for parent"() {
      given:
        dbfixture(getTransient("AS-SET-3LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create grand parent ASN and parent as-set when child as-set exists, pw for parent & grand parent"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("AS-SET-3LEVEL"))

      expect:
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")

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

                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create parent as-set and grand parent ASN when child as-set exists, pw for parent & grand parent, note wrong order"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("AS-SET-3LEVEL"))

      expect:
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")

      when:
        def ack = syncUpdateWithResponse("""
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS123"}

        queryObject("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create 3 level as-set obj, 2 level parent set exists, grand parent ASN not exists, pw for obj & parent"() {
      given:
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST:AS-TEST2"}

        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create 3 level as-set obj with double ASN, no 2 level parent set exists, grand parent ASN exists, pw for obj and grand parent"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:AS123", "as-set", "AS123:AS123")
        queryObjectNotFound("-r -T as-set AS123:AS123:AS-TEST", "as-set", "AS123:AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS123:AS-TEST
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS123:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS123:AS-TEST") == [
                "Parent object AS123:AS123 not found"]

        queryObjectNotFound("-r -T as-set AS123:AS123:AS-TEST", "as-set", "AS123:AS123:AS-TEST")
    }

    def "create 2 level as-set obj with double ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:AS123", "as-set", "AS123:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS123
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS123"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS123") == [
                "Syntax error in AS123:AS123"]

        queryObjectNotFound("-r -T as-set AS123:AS123", "as-set", "AS123:AS123")
    }

    def "create as-set obj with only ASN, pw for obj and parent ASN"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:", "as-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123
                descr:        test as-set
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
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123"}
        ack.errorMessagesFor("Create", "[as-set] AS123") == [
                "Syntax error in AS123"]

        queryObjectNotFound("-r -T as-set AS123", "as-set", "AS123")
    }

    def "create 2 level as-set object with existing parent set, ASN in set name not exist, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS-TEST:AS123", "as-set", "AS-TEST:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST:AS123
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS-TEST:AS123"}

        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS-TEST:AS123", "as-set", "AS-TEST:AS123")
    }

    def "create 2 level as-set object with existing parent set, repeated name part, obj & parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T as-set AS-TEST:AS-TEST", "as-set", "AS-TEST:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS-TEST:AS-TEST"}

        queryObject("-r -T as-set AS-TEST:AS-TEST", "as-set", "AS-TEST:AS-TEST")
    }

    def "modify as-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set updated
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
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS123:AS-TEST"}

        query_object_matches("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST", "test as-set updated")
    }

    def "modify as-set object, no parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObjectNotFound("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set updated
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
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS123:AS-TEST"}

        query_object_matches("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST", "test as-set updated")
    }

    def "delete as-set object with existing parent ASN, only obj pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[as-set] AS123:AS-TEST"}

        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "delete parent ASN object, child as-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("AS-SET-2LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

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

    def "delete parent as-set object, child as-set exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("TEST-TEST"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-r -T as-set AS-TEST:AS-TEST", "as-set", "AS-TEST:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[as-set] AS-TEST"}

        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
    }

    def "delete parent as-set object, child as-set exists, grand parent ASN exists, only parent pw supplied"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("AS-SET-2LEVEL"))
        dbfixture(getTransient("AS-SET-3LEVEL"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
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
        ack.successes.any {it.operation == "Delete" && it.key == "[as-set] AS123:AS-TEST"}

        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

    def "create as-set object with all optional attrs"() {
      given:
        dbfixture(getTransient("ASN123"))
      expect:
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
                members:      AS1, AS2, AS3, AS4:AS-TEST2
                tech-c:       TP2-TEST
                members:      AS65536, AS7775535, AS94967295
                mbrs-by-ref:  ANY
                org:          ORG-OTO1-TEST
                notify:       dbtest@ripe.net
                mbrs-by-ref:  tst-mnt
                tech-c:       TP1-TEST
                org:          ORG-LIR1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    OWNER-MNT
                source:  TEST
                admin-c:      TP3-TEST
                notify:       unread@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    SUB-MNT

                password: lir2
                password: owner3
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
        ack.successes.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}

        queryObject("-rBT as-set as123:As-TEst", "as-set", "AS123:AS-TEST")
    }

    def "create 3 level as-set obj, no 2 level parent set exists, grand parent ASN exists, override used"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")

      when:
        def message = syncUpdate("""\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
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

        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
    }

}
