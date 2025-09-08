package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class FilterSetsSpec extends BaseQueryUpdateSpec {

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
            "FLTR": """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "FLTR-2LEVEL": """\
                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "FLTR-3LEVEL": """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG": """\
                peering-set:   prng-customers
                descr:        This peering contains customers
                peering:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG-2LEVEL": """\
                peering-set:   AS123:prng-customers
                descr:        This peering contains customers
                peering:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG-3LEVEL": """\
                peering-set:   AS123:prng-customers:AS352
                descr:        This peering contains customers
                mp-peering:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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

    def "create filter-set object"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with mp-filter"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                mp-filter:    <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                              AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with filter & mp-filter"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                              AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                mp-filter:    <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                              AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}
        ack.errorMessagesFor("Create", "[filter-set] fltr-customers") == [
                "A filter-set object cannot contain both filter and mp-filter attributes"]

        queryObjectNotFound("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with no filter or mp-filter"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}
        ack.errorMessagesFor("Create", "[filter-set] fltr-customers") == [
                "A filter-set object must contain either filter or mp-filter attribute"]

        queryObjectNotFound("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create 2 filter-set objects with filter and mp-filter, ASN > 32bit"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")
        queryObjectNotFound("-r -T filter-set fltr-customers2", "filter-set", "fltr-customers2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       <^AS4294967299> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                filter-set:   fltr-customers2
                descr:        This filter contains customers
                mp-filter:    <^AS4294967299> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                              AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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

        ack.failed
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(2, 2, 0, 0)

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}
        ack.errorMessagesFor("Create", "[filter-set] fltr-customers") == [
                "Syntax error in <^AS4294967299> OR <^AS8501 AS20965> AND community.contains(12345:7295) AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test"]
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers2"}
        ack.errorMessagesFor("Create", "[filter-set] fltr-customers2") == [
                "Syntax error in <^AS4294967299> OR <^AS8501 AS20965> AND community.contains(12345:7295) AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test"]

        queryObjectNotFound("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
        queryObjectNotFound("-rBT filter-set fltr-customers", "filter-set", "fltr-customers2")
    }

    def "create 2 level filter-set object, top level ASN exists"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers"}

        queryObject("-rBT filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
    }

    def "create 2 level filter-set object, no top level ASN"() {
      given:
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers"}
        ack.errorMessagesFor("Create", "[filter-set] AS123:fltr-customers") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
    }

    def "create 2 level filter-set object, top level set exists, no ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("FLTR"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")
        queryObjectNotFound("-r -T filter-set fltr-customers:AS123", "filter-set", "fltr-customers:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers:AS123
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers:AS123"}

        queryObject("-rBT filter-set fltr-customers:AS123", "filter-set", "fltr-customers:AS123")
    }

    def "create 3 level filter-set object with mp-filter, 2 level set exists with filter"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("FLTR-2LEVEL"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}

        queryObject("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create 3 level filter-set object with mp-filter, 2 level set exists with filter, no top level ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("FLTR-2LEVEL"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}

        queryObject("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create 3 level filter-set object with mp-filter, 2 level set exists with filter, delete 2 level set, modify 3 level"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("FLTR-2LEVEL"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                delete:       test ing

                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                remarks:      updated
                source:  TEST

                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 1, 1, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.successes.any {it.operation == "Delete" && it.key == "[filter-set] AS123:fltr-customers"}
        ack.successes.any {it.operation == "Modify" && it.key == "[filter-set] AS123:fltr-customers:AS352"}

        queryObject("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
        queryObjectNotFound("-rBT filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        query_object_matches("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352", "updated")
    }

    def "create 3 level filter-set object with mp-filter, 2 level set exists with filter, delete 2 level set, modify 3 level, re-create 2 level"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("FLTR-2LEVEL"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        queryObjectNotFound("-r -T filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST

                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:       TEST
                delete:       test ing

                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                remarks:      updated
                source:       TEST

                filter-set:   AS123:fltr-customers
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                remarks:      updated
                source:       TEST

                password: lir
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 4
        ack.summary.assertSuccess(4, 2, 1, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.successes.any {it.operation == "Delete" && it.key == "[filter-set] AS123:fltr-customers"}
        ack.successes.any {it.operation == "Modify" && it.key == "[filter-set] AS123:fltr-customers:AS352"}

        queryObject("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
        queryObject("-rBT filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers")
        query_object_matches("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352", "updated")
        query_object_matches("-rBT filter-set AS123:fltr-customers", "filter-set", "AS123:fltr-customers", "updated")
    }

    def "create filter-set object with only ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123", "filter-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123"}
        ack.errorMessagesFor("Create", "[filter-set] AS123") == [
                "Syntax error in AS123"]

        queryObjectNotFound("-rBT filter-set AS123", "filter-set", "AS123")
    }

    def "create filter-set object with invalid chars in filter"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123", "filter-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                filter:       <^AS7775535@#%>
                              AND as-customers:AS94967295:as-test
                              OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.errorMessagesFor("Create", "[filter-set] AS123:fltr-customers:AS352") == [
                "Syntax error in <^AS7775535@ AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test"]

        queryObjectNotFound("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create filter-set object with invalid value in community"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123", "filter-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(94967295999999:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.errorMessagesFor("Create", "[filter-set] AS123:fltr-customers:AS352") == [
                "Syntax error in <^AS7775535> OR <^AS8501 AS20965> AND community.contains(94967295999999:7295) AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test"]

        queryObjectNotFound("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create filter-set object with invalid second value in community"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123", "filter-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(94967295:7295999999)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.errorMessagesFor("Create", "[filter-set] AS123:fltr-customers:AS352") == [
                "Syntax error in <^AS7775535> OR <^AS8501 AS20965> AND community.contains(94967295:7295999999) AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test"]

        queryObjectNotFound("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create filter-set object with multiple filter & mp-filter attrs"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T filter-set AS123", "filter-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   AS123:fltr-customers:AS352
                descr:        This filter contains customers
                filter:       <^AS7775535>
                              AND as-customers:AS94967295:as-test
                              OR rs-customers:AS94967295:rs-test
                filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
                mp-filter:       <^AS77755> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS949295:as-test OR rs-customers:AS949695:rs-test
                mp-filter:       <^AS7775535> OR <^AS8501 AS20965> AND community.contains(12345:7295)
                 AND as-customers:AS94967295:as-test OR rs-customers:AS94967295:rs-test
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

        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] AS123:fltr-customers:AS352"}
        ack.errorMessagesFor("Create", "[filter-set] AS123:fltr-customers:AS352") == [
                "Attribute \"filter\" appears more than once",
                "Attribute \"mp-filter\" appears more than once"]

        queryObjectNotFound("-rBT filter-set AS123:fltr-customers:AS352", "filter-set", "AS123:fltr-customers:AS352")
    }

    def "create filter-set object with filter {}"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       {}
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with filter community only"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       community(8856:10030)
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with filter ASN list only"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:          AS5489  AS8281  AS8388  AS8573  AS8618  AS8991  AS9128
                                 AS12391 AS12402 AS13038 AS15414 AS15536 AS15617 AS15764
                                 AS15853 AS15948 AS16013 AS20615 AS20813 AS21168 AS21291
                                 AS24849 AS28953 AS29052 AS29162 AS29241 AS29353 AS29368
                                 AS30770
                                 AS9128  AS8499  AS12391 AS13153 AS20615 AS28718
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with filter another ASN list only"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       <.* (AS701|AS1239|AS7018|AS209|AS3561|AS1|AS3356|AS3549|AS702|AS2914|AS3786|AS4766|AS3246|AS6347|AS4323|AS1299|AS6461|AS8220|AS703|AS5673) .*>
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with mp-filter IPv6 addrs"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                mp-filter:    {
                                3FFE:0000::/18^24,
                                3FFE:4000::/18^32,
                                3FFE:8000::/17^28,
                                2001:478::/32^40-48,    # EP.NET: IXP
                                2001:500::/30^48,       # ARIN: infrastructure
                                2001:504::/32^48,       # ARIN: IXP
                                2001:7F8::/32^48,       # RIPE: IXP
                                2001:7FA::/32^48,       # APNIC: IXP
                                2001::/16^35-35,
                                2001::/16^19-32,
                                2002::/16
                              }
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with mp-filter IPv6 addrs AND NOT"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                mp-filter:    {2001::/20^20-32} AND NOT {2001:DB8::/32^+}
                              OR {0::/0^+} OR { ::/8^+} AND <[AS64512-AS65534]>
                                OR
                                {2001:0500::/30^48}
                                OR
                                {2001:1000::/20^20-32} AND NOT {2001:1000::/23^+}
                                OR
                                {2001:2000::/19^19-32} AND NOT {2001:3C00::/22^+}
                                OR
                                {2001:4000::/20^20-32} AND NOT {2001:4E00::/23^+}
                                OR
                                {2001:5000::/20^20-32}
                                OR
                                {2001:8000::/19^19-32}
                                OR
                                {2001:A000::/19^19-32}
                                OR
                                {2001::/16^35} AND NOT {2001:DB8::/32^+} AND NOT {2001:1000::/23^+} AND NOT {2001:3C00::/22^+} AND NOT {2001:4E00::/23^+}
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with long filter list"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       {
                        1.0.0.0/8^- ,
                        2.0.0.0/8^+ ,
                        5.0.0.0/8^26-32 ,
                        7.0.0.0/8^- ,
                        10.0.0.0/8^- ,
                        23.0.0.0/8^- ,
                        27.0.0.0/8^- ,
                        31.0.0.0/8^- ,
                        36.0.0.0/8^- ,
                        37.0.0.0/8^- ,
                        39.0.0.0/8^- ,
                        41.0.0.0/8^- ,
                        42.0.0.0/8^- ,
                        49.0.0.0/8^- ,
                        50.0.0.0/8^- ,
                        58.0.0.0/8^- ,
                        59.0.0.0/8^- ,
                        60.0.0.0/8^- ,
                        70.0.0.0/8^- ,
                        71.0.0.0/8^- ,
                        72.0.0.0/8^- ,
                        73.0.0.0/8^- ,
                        74.0.0.0/8^- ,
                        75.0.0.0/8^- ,
                        76.0.0.0/8^- ,
                        77.0.0.0/8^- ,
                        78.0.0.0/8^- ,
                        79.0.0.0/8^- ,
                        83.0.0.0/8^- ,
                        84.0.0.0/8^- ,
                        85.0.0.0/8^- ,
                        86.0.0.0/8^- ,
                        87.0.0.0/8^- ,
                        88.0.0.0/8^- ,
                        89.0.0.0/8^- ,
                        90.0.0.0/8^- ,
                        91.0.0.0/8^- ,
                        92.0.0.0/8^- ,
                        93.0.0.0/8^- ,
                        94.0.0.0/8^- ,
                        95.0.0.0/8^- ,
                        96.0.0.0/8^- ,
                        97.0.0.0/8^- ,
                        98.0.0.0/8^- ,
                        99.0.0.0/8^- ,
                        100.0.0.0/8^- ,
                        101.0.0.0/8^- ,
                        102.0.0.0/8^- ,
                        103.0.0.0/8^- ,
                        104.0.0.0/8^- ,
                        105.0.0.0/8^- ,
                        106.0.0.0/8^- ,
                        107.0.0.0/8^- ,
                        108.0.0.0/8^- ,
                        109.0.0.0/8^- ,
                        110.0.0.0/8^- ,
                        111.0.0.0/8^- ,
                        112.0.0.0/8^- ,
                        113.0.0.0/8^- ,
                        114.0.0.0/8^- ,
                        115.0.0.0/8^- ,
                        116.0.0.0/8^- ,
                        117.0.0.0/8^- ,
                        118.0.0.0/8^- ,
                        119.0.0.0/8^- ,
                        120.0.0.0/8^- ,
                        121.0.0.0/8^- ,
                        122.0.0.0/8^- ,
                        123.0.0.0/8^- ,
                        124.0.0.0/8^- ,
                        125.0.0.0/8^- ,
                        126.0.0.0/8^- ,
                        127.0.0.0/8^- ,
                        169.254.0.0/16^- ,
                        172.16.0.0/12^- ,
                        192.0.2.0/24^- ,
                        192.168.0.0/16^- ,
                        197.0.0.0/8^- ,
                        201.0.0.0/8^- ,
                        222.0.0.0/8^- ,
                        223.0.0.0/8^- ,
                        224.0.0.0/3^-
                        }
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
        ack.successes.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}

        queryObject("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

    def "create filter-set object with long filter list, missing comma"() {
      expect:
        queryObjectNotFound("-r -T filter-set fltr-customers", "filter-set", "fltr-customers")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                filter-set:   fltr-customers
                descr:        This filter contains customers
                filter:       {
                        1.0.0.0/8^- ,
                        2.0.0.0/8^- ,
                        5.0.0.0/8^- ,
                        7.0.0.0/8^- ,
                        10.0.0.0/8^- ,
                        23.0.0.0/8^- ,
                        27.0.0.0/8^- ,
                        31.0.0.0/8^- ,
                        36.0.0.0/8^- ,
                        37.0.0.0/8^- ,
                        39.0.0.0/8^- ,
                        41.0.0.0/8^- ,
                        42.0.0.0/8^- ,
                        49.0.0.0/8^- ,
                        50.0.0.0/8^- ,
                        58.0.0.0/8^- ,
                        59.0.0.0/8^- ,
                        60.0.0.0/8^- ,
                        70.0.0.0/8^- ,
                        71.0.0.0/8^- ,
                        72.0.0.0/8^- ,
                        73.0.0.0/8^- ,
                        74.0.0.0/8^- ,
                        75.0.0.0/8^- ,
                        76.0.0.0/8^- ,
                        77.0.0.0/8^- ,
                        78.0.0.0/8^- ,
                        79.0.0.0/8^- ,
                        83.0.0.0/8^- ,
                        84.0.0.0/8^- ,
                        85.0.0.0/8^- ,
                        86.0.0.0/8^- ,
                        87.0.0.0/8^- ,
                        88.0.0.0/8^- ,
                        89.0.0.0/8^- ,
                        90.0.0.0/8^- ,
                        91.0.0.0/8^- ,
                        92.0.0.0/8^- ,
                        93.0.0.0/8^- ,
                        94.0.0.0/8^- ,
                        95.0.0.0/8^- ,
                        96.0.0.0/8^- ,
                        97.0.0.0/8^- ,
                        98.0.0.0/8^- ,
                        99.0.0.0/8^- ,
                        100.0.0.0/8^-
                        101.0.0.0/8^- ,
                        102.0.0.0/8^- ,
                        103.0.0.0/8^- ,
                        104.0.0.0/8^- ,
                        105.0.0.0/8^- ,
                        106.0.0.0/8^- ,
                        107.0.0.0/8^- ,
                        108.0.0.0/8^- ,
                        109.0.0.0/8^- ,
                        110.0.0.0/8^- ,
                        111.0.0.0/8^- ,
                        112.0.0.0/8^- ,
                        113.0.0.0/8^- ,
                        114.0.0.0/8^- ,
                        115.0.0.0/8^- ,
                        116.0.0.0/8^- ,
                        117.0.0.0/8^- ,
                        118.0.0.0/8^- ,
                        119.0.0.0/8^- ,
                        120.0.0.0/8^- ,
                        121.0.0.0/8^- ,
                        122.0.0.0/8^- ,
                        123.0.0.0/8^- ,
                        124.0.0.0/8^- ,
                        125.0.0.0/8^- ,
                        126.0.0.0/8^- ,
                        127.0.0.0/8^- ,
                        169.254.0.0/16^- ,
                        172.16.0.0/12^- ,
                        192.0.2.0/24^- ,
                        192.168.0.0/16^- ,
                        197.0.0.0/8^- ,
                        201.0.0.0/8^- ,
                        222.0.0.0/8^- ,
                        223.0.0.0/8^- ,
                        224.0.0.0/3^-
                        }
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

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[filter-set] fltr-customers"}
        ack.errorMessagesFor("Create", "[filter-set] fltr-customers") == [
                "Syntax error in { 1.0.0.0/8^- , 2.0.0.0/8^- , 5.0.0.0/8^- , 7.0.0.0/8^- , 10.0.0.0/8^- , 23.0.0.0/8^- , 27.0.0.0/8^- , 31.0.0.0/8^- , 36.0.0.0/8^- , 37.0.0.0/8^- , 39.0.0.0/8^- , 41.0.0.0/8^- , 42.0.0.0/8^- , 49.0.0.0/8^- , 50.0.0.0/8^- , 58.0.0.0/8^- , 59.0.0.0/8^- , 60.0.0.0/8^- , 70.0.0.0/8^- , 71.0.0.0/8^- , 72.0.0.0/8^- , 73.0.0.0/8^- , 74.0.0.0/8^- , 75.0.0.0/8^- , 76.0.0.0/8^- , 77.0.0.0/8^- , 78.0.0.0/8^- , 79.0.0.0/8^- , 83.0.0.0/8^- , 84.0.0.0/8^- , 85.0.0.0/8^- , 86.0.0.0/8^- , 87.0.0.0/8^- , 88.0.0.0/8^- , 89.0.0.0/8^- , 90.0.0.0/8^- , 91.0.0.0/8^- , 92.0.0.0/8^- , 93.0.0.0/8^- , 94.0.0.0/8^- , 95.0.0.0/8^- , 96.0.0.0/8^- , 97.0.0.0/8^- , 98.0.0.0/8^- , 99.0.0.0/8^- , 100.0.0.0/8^- 101.0.0.0/8^- , 102.0.0.0/8^- , 103.0.0.0/8^- , 104.0.0.0/8^- , 105.0.0.0/8^- , 106.0.0.0/8^- , 107.0.0.0/8^- , 108.0.0.0/8^- , 109.0.0.0/8^- , 110.0.0.0/8^- , 111.0.0.0/8^- , 112.0.0.0/8^- , 113.0.0.0/8^- , 114.0.0.0/8^- , 115.0.0.0/8^- , 116.0.0.0/8^- , 117.0.0.0/8^- , 118.0.0.0/8^- , 119.0.0.0/8^- , 120.0.0.0/8^- , 121.0.0.0/8^- , 122.0.0.0/8^- , 123.0.0.0/8^- , 124.0.0.0/8^- , 125.0.0.0/8^- , 126.0.0.0/8^- , 127.0.0.0/8^- , 169.254.0.0/16^- , 172.16.0.0/12^- , 192.0.2.0/24^- , 192.168.0.0/16^- , 197.0.0.0/8^- , 201.0.0.0/8^- , 222.0.0.0/8^- , 223.0.0.0/8^- , 224.0.0.0/3^- }"]

        queryObjectNotFound("-rBT filter-set fltr-customers", "filter-set", "fltr-customers")
    }

}
