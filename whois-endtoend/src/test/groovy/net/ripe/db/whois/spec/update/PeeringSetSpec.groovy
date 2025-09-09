package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PeeringSetSpec extends BaseQueryUpdateSpec {

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
           "PRNG": """\
                peering-set:   prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                mp-peering:   AS2320834 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG-2LEVEL": """\
                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                mp-peering:   AS2320834 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG-2LEVEL-NO-MP": """\
                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                """,
            "PRNG-3LEVEL": """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                mp-peering:   AS2320834 at 193.109.219.24
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

    def "create peering-set object with peering & mp-peering"() {
      expect:
        queryObjectNotFound("-r -T peering-set prng-partners", "peering-set", "prng-partners")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                mp-peering:   AS2320834 at 193.109.219.24
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
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] prng-partners"}
        ack.errorMessagesFor("Create", "[peering-set] prng-partners") == [
                "A peering-set object cannot contain both peering and mp-peering attributes"]

        queryObjectNotFound("-rBT peering-set prng-partners", "peering-set", "prng-partners")
    }

    def "create 2 peering-set objects with peering and mp-peering, ASN > 32bit"() {
      expect:
        queryObjectNotFound("-r -T peering-set prng-partners", "peering-set", "prng-partners")
        queryObjectNotFound("-r -T peering-set prng-partners2", "peering-set", "prng-partners2")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   prng-partners
                descr:        This peering contains partners
                peering:      AS4294967299 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                peering-set:   prng-partners2
                descr:        This peering contains partners
                mp-peering:   AS4294967299 at 193.109.219.24
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
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] prng-partners"}
        ack.errorMessagesFor("Create", "[peering-set] prng-partners") == [
                "Syntax error in AS4294967299 at 193.109.219.24"]
        ack.errorMessagesFor("Create", "[peering-set] prng-partners2") == [
                "Syntax error in AS4294967299 at 193.109.219.24"]

        queryObjectNotFound("-rBT peering-set prng-partners", "peering-set", "prng-partners")
        queryObjectNotFound("-rBT peering-set prng-partners", "peering-set", "prng-partners2")
    }

    def "create 2 level peering-set object, top level ASN exists"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS123 at 193.109.219.24
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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners"}

        queryObject("-rBT peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
    }

    def "create 2 level peering-set object, no top level ASN"() {
      given:
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                mp-peering:   AS2320834 at 193.109.219.24
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
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners"}
        ack.errorMessagesFor("Create", "[peering-set] AS123:prng-partners") == [
                "Parent object AS123 not found"]

        queryObjectNotFound("-rBT peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
    }

    def "create 2 level peering-set object, top level set exists, no ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("PRNG"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T peering-set prng-partners", "peering-set", "prng-partners")
        queryObjectNotFound("-r -T peering-set prng-partners:AS123", "peering-set", "prng-partners:AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   prng-partners:AS123
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] prng-partners:AS123"}

        queryObject("-rBT peering-set prng-partners:AS123", "peering-set", "prng-partners:AS123")
    }

    def "create 3 level peering-set object with mp-peering, 2 level set exists with peering"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("PRNG-2LEVEL-NO-MP"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                mp-peering:   AS2320834 at 193.109.219.24
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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners:AS352"}

        queryObject("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")
    }

    def "create 3 level peering-set object with mp-peering, 2 level set exists with peering, no top level ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("PRNG-2LEVEL-NO-MP"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                mp-peering:   AS2320834 at 193.109.219.24
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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners:AS352"}

        queryObject("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")
    }

    def "create 3 level peering-set object with mp-peering, 2 level set exists with peering, delete 2 level set, modify 3 level"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("PRNG-2LEVEL-NO-MP"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                mp-peering:   AS2320834 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                delete:       test ing

                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                remarks:      updated
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners:AS352"}
        ack.successes.any {it.operation == "Delete" && it.key == "[peering-set] AS123:prng-partners"}
        ack.successes.any {it.operation == "Modify" && it.key == "[peering-set] AS123:prng-partners:AS352"}

        queryObject("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")
        queryObjectNotFound("-rBT peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        query_object_matches("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352", "updated")
    }

    def "create 3 level peering-set object with mp-peering, 2 level set exists with peering, delete 2 level set, modify 3 level, re-create 2 level"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("PRNG-2LEVEL-NO-MP"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        queryObjectNotFound("-r -T peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                mp-peering:   AS2320834 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST

                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                source:  TEST
                delete:       test ing

                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                remarks:      updated
                source:  TEST

                peering-set:   AS123:prng-partners
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       LIR-MNT
                mnt-lower:    LIR-MNT
                remarks:      updated
                source:  TEST

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
        ack.successes.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners:AS352"}
        ack.successes.any {it.operation == "Delete" && it.key == "[peering-set] AS123:prng-partners"}
        ack.successes.any {it.operation == "Modify" && it.key == "[peering-set] AS123:prng-partners:AS352"}

        queryObject("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")
        queryObject("-rBT peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners")
        query_object_matches("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352", "updated")
        query_object_matches("-rBT peering-set AS123:prng-partners", "peering-set", "AS123:prng-partners", "updated")
    }

    def "create peering-set object with only ASN"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T peering-set AS123", "peering-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123
                descr:        This peering contains partners
                peering:      AS2320834 at 193.109.219.24
                peering:      AS123 at 193.109.219.24
                mp-peering:   AS2320834 at 193.109.219.24
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
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] AS123"}
        ack.errorMessagesFor("Create", "[peering-set] AS123") == [
                "Syntax error in AS123"]

        queryObjectNotFound("-rBT peering-set AS123", "peering-set", "AS123")
    }

    def "create peering-set object with invalid chars in peering"() {
      given:
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN123"))

      expect:
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObjectNotFound("-r -T peering-set AS123", "peering-set", "AS123")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:   AS123:prng-partners:AS352
                descr:        This peering contains partners
                peering:      AS2320834@%# at 193.109.219.24
                peering:      AS123
                mp-peering:   AS2320834
                              at
                +
                               193.109.219.24.35
                mp-peering:    AS2320834 at 193.999
                peering:      AS2320834 at 193.109.219.9999
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

        ack.countErrorWarnInfo(4, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] AS123:prng-partners:AS352"}
        ack.errorMessagesFor("Create", "[peering-set] AS123:prng-partners:AS352") == [
                "Syntax error in AS2320834@%",
                "Syntax error in AS2320834 at 193.109.219.24.35",
                "Syntax error in AS2320834 at 193.999",
                "Syntax error in AS2320834 at 193.109.219.9999"]

        queryObjectNotFound("-rBT peering-set AS123:prng-partners:AS352", "peering-set", "AS123:prng-partners:AS352")
    }

    def "create peering-set object with no peering or mp-peering"() {
      expect:
        queryObjectNotFound("-r -T peering-set prng-partners", "peering-set", "prng-partners")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                peering-set:  prng-partners
                descr:        This peering contains partners
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
        ack.errors.any {it.operation == "Create" && it.key == "[peering-set] prng-partners"}
        ack.errorMessagesFor("Create", "[peering-set] prng-partners") == [
                "A peering-set object must contain either peering or mp-peering attribute"]

        queryObjectNotFound("-rBT peering-set prng-partners", "peering-set", "prng-partners")
    }

}
