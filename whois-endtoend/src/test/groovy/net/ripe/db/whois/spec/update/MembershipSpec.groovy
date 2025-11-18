package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class MembershipSpec extends BaseQueryUpdateSpec {

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
                member-of:      AS-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST
                """,
            "ASN1309": """\
                aut-num:        AS1309
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                member-of:      AS-TEST2
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST
                """,
            "ASN94967295": """\
                aut-num:        AS94967295
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST
                """,
            "TOP-AS-SET": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  LIR-MNT
                source:  TEST
                """,
            "TOP-SET-NOREF": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "TOP-SET-ANY": """\
                as-set:       AS-TEST2
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  ANY
                source:  TEST
                """,
            "REF-AS-SET": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  LIR-MNT
                mbrs-by-ref:  REF-MNT
                source:  TEST
                """,
            "REF-AS-SET2": """\
                as-set:       AS-TEST2
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  LIR2-MNT
                source:  TEST
                """,
            "REF-AS-SET3": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  LIR-MNT
                mbrs-by-ref:  LIR2-MNT
                source:  TEST
                """,
            "AS-SET-2LEVEL": """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  LIR-MNT
                mbrs-by-ref:  LIR3-MNT
                source:  TEST
                """,
            "AS-SET-3LEVEL": """\
                as-set:       AS123:AS-TEST:AS-TEST2
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER3-MNT
                mnt-lower:    LIR3-MNT
                mbrs-by-ref:  owner-MNT
                source:  TEST
                """,
            "AS-SET-MEMBERS": """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                members:      AS1, AS123, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
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
            "REF-MNT": """\
                mntner:      REF-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$hDtpxh4D\$.mEfvYAiRmAQCynxMuE4J1  #ref
                mnt-by:      ref-MNT
                source:      TEST
                """
    ]}

    def "create aut-num obj, member-of existing set, ref mntner used for auth"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password: lir
                password: locked
                password: owner3
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

    def "create aut-num obj, member-of existing set, ref mntner included but not used for auth, auth mntner not mbrs-by-ref"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password: hm
                password: locked
                password: owner3
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

    def "create aut-num obj, member-of existing set, no ref mntner in set"() {
      given:
        dbfixture(getTransient("TOP-SET-NOREF"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password: lir
                password: locked
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[aut-num] AS123"}
        ack.errorMessagesFor("Create", "[aut-num] AS123") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST]"]
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create aut-num obj, member-of existing set, no ref mntner in set, override"() {
        given:
        dbfixture(getTransient("TOP-SET-NOREF"))
        dbfixture(getTransient("ASB16"))

        expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:     denis,override1
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)

        ack.countErrorWarnInfo(0, 1, 1)
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS123") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST]"]

        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create aut-num obj, member-of multiple existing set"() {
        given:
            dbfixture(getTransient("REF-AS-SET2"))
            dbfixture(getTransient("REF-AS-SET3"))
            dbfixture(getTransient("ASB16"))

        expect:
            queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
            query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:")
            queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
            queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST2
                member-of:      AS-TEST
                mnt-by:         LIR3-MNT
                source:         TEST

                password: lir3
                password: locked
                password: owner3
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)

            ack.countErrorWarnInfo(1, 1, 0)
            ack.errors.any {it.operation == "Create" && it.key == "[aut-num] AS123"}
            ack.errorMessagesFor("Create", "[aut-num] AS123") == [
                    "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST2, AS-TEST]"]
            queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "modify aut-num obj, member-of existing set, remove ref mntner"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                password: hm
                """.stripIndent(true)
        )

      then:
        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 4, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[aut-num] AS352"}
        ack.errorMessagesFor("Modify", "[aut-num] AS352") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST]"]
        ack.warningMessagesFor("Modify", "[aut-num] AS352") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
              "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
    }

    def "modify aut-num obj, member-of existing set, remove ref mntner, override"() {
        given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

        expect:
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

        when:
        def data = """\
                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-lower:      owner2-mnt
                source:         TEST
                override:     denis,override1
               """
        def createResponse = syncUpdate(new SyncUpdate(data: data.stripIndent(true)))

        then:
        createResponse =~ /SUCCEEDED/
        createResponse.contains("***Warning: Membership claim is not supported by mbrs-by-ref: attribute of the\n" +
                "            referenced set [AS-TEST]")
    }

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, remove mbrs-by-ref"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}

        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
    }

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, remove mbrs-by-ref from as-set, then delete as-set"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.errors.any {it.operation == "Delete" && it.key == "[as-set] AS-TEST"}
        ack.errorMessagesFor("Delete", "[as-set] AS-TEST") == [
                "Object [as-set] AS-TEST is referenced from other objects"]

        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
    }

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, remove mbrs-by-ref from as-set, then modify ASN keeping member-of"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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

                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                password: owner2
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 5, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.errors.any {it.operation == "Modify" && it.key == "[aut-num] AS352"}
        ack.errorMessagesFor("Modify", "[aut-num] AS352") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST]"]
      ack.warningMessagesFor("Modify", "[aut-num] AS352") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
               "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
    }

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, remove mbrs-by-ref from as-set, then modify ASN removing member-of"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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

                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                password: owner2
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 5, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS352"}
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS352") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
               "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_not_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
    }

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, remove mbrs-by-ref from as-set, then modify ASN removing member-of, then delete as-set"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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

                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       testing

                password: owner2
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 3
        ack.summary.assertSuccess(3, 0, 2, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 5, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS352"}
        ack.successes.any {it.operation == "Delete" && it.key == "[as-set] AS-TEST"}
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS352") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
               "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_not_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
    }

    def "modify aut-num obj, add member-of non existing set"() {
      given:
        dbfixture(getTransient("ASN123"))

      expect:
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:\\s*AS-TEST")

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
                member-of:      AS-TEST
                mnt-by:         owner-MNT
                source:         TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Modify" && it.key == "[aut-num] AS123"}
        ack.errorMessagesFor("Modify", "[aut-num] AS123") == [
                "Unknown object referenced AS-TEST"
        ]

        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:\\s*AS-TEST")
    }

    def "delete mntner obj, in mbrs-by-ref of existing set"() {
      given:
        dbfixture(getTransient("REF-MNT"))
        dbfixture(getTransient("REF-AS-SET"))

      expect:
        queryObject("-rBT mntner REF-MNT", "mntner", "REF-MNT")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*REF-MNT")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                mntner:      REF-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$hDtpxh4D\$.mEfvYAiRmAQCynxMuE4J1  #ref
                mnt-by:      ref-MNT
                source:      TEST
                delete:      testing

                password: ref
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[mntner] REF-MNT"}
        ack.errorMessagesFor("Delete", "[mntner] REF-MNT") == [
                "Object [mntner] REF-MNT is referenced from other objects"]

        queryObject("-rBT mntner REF-MNT", "mntner", "REF-MNT")
    }

    def "delete as-set obj, referenced by an ASN member-of using mbrs-by-ref in set"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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
                mbrs-by-ref:  LIR-MNT
                source:  TEST
                delete:      testing

                password: owner2
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.errors
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Delete" && it.key == "[as-set] AS-TEST"}
        ack.errorMessagesFor("Delete", "[as-set] AS-TEST") == [
                "Object [as-set] AS-TEST is referenced from other objects"]

        queryObject("-rBT as-set AS-TEST", "as-set", "AS-TEST")
    }

    def "create as-set obj, mbrs-by-ref ANY"() {
       given:
         dbfixture(getTransient("ASN123"))
       expect:
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS123:AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  ANY
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

        query_object_matches("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST", "mbrs-by-ref:\\s*ANY")
    }

    def "create aut-num obj, member-of existing set, mbrs-by-ref ANY"() {
      given:
        dbfixture(getTransient("TOP-SET-ANY"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST2", "as-set", "AS-TEST2")
        query_object_matches("-r -T as-set AS-TEST2", "as-set", "AS-TEST2", "mbrs-by-ref:\\s*ANY")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST2
                mnt-by:         LIR-MNT
                source:         TEST

                password: lir
                password: locked
                password: owner3
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

    def "modify as-set obj, ASN member-of set using mbrs-by-ref, change mbrs-by-ref to ANY, then modify ASN keeping member-of"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("ASN352"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-rBT aut-num AS352", "aut-num", "AS352")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")

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
                mbrs-by-ref:  ANY
                source:  TEST

                aut-num:        AS352
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                password: owner2
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 5, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS352"}
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS352") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
               "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*ANY")
        query_object_matches("-rBT aut-num AS352", "aut-num", "AS352", "member-of:\\s*AS-TEST")
    }

    def "create aut-num obj, member-of 2 existing sets, ref mntner used for auth"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("TOP-SET-ANY"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-r -T as-set AS-TEST2", "as-set", "AS-TEST2")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-r -T as-set AS-TEST2", "as-set", "AS-TEST2", "mbrs-by-ref:\\s*ANY")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                member-of:      AS-TEST2
                mnt-by:         LIR-MNT
                mnt-by:         LIR2-MNT
                source:         TEST

                password: lir
                password: locked
                password: owner3
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

    def "modify as-set obj, ASN member-of 2 sets using mbrs-by-ref, remove mbrs-by-ref from 1 as-set, then modify ASN keeping member-of"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("TOP-SET-ANY"))
        dbfixture(getTransient("ASB16"))
        dbfixture(getTransient("ASN1309"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        queryObject("-r -T as-set AS-TEST2", "as-set", "AS-TEST2")
        query_object_matches("-r -T as-set AS-TEST2", "as-set", "AS-TEST2", "mbrs-by-ref:\\s*ANY")
        queryObject("-rBT aut-num AS1309", "aut-num", "AS1309")
        query_object_matches("-rBT aut-num AS1309", "aut-num", "AS1309", "mnt-by:\\s*LIR-MNT")
        query_object_matches("-rBT aut-num AS1309", "aut-num", "AS1309", "member-of:\\s*AS-TEST")
        query_object_matches("-rBT aut-num AS1309", "aut-num", "AS1309", "member-of:\\s*AS-TEST2")

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

                aut-num:        AS1309
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                member-of:      AS-TEST2
                status:         ASSIGNED
                mnt-by:         RIPE-NCC-HM-MNT
                mnt-by:         LIR-MNT
                mnt-lower:      owner2-mnt
                source:         TEST

                password: owner2
                password: lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 5, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.errors.any {it.operation == "Modify" && it.key == "[aut-num] AS1309"}
        ack.errorMessagesFor("Modify", "[aut-num] AS1309") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS-TEST]"]
        ack.warningMessagesFor("Modify", "[aut-num] AS1309") ==
              ["Deprecated attribute \"mnt-lower\". This attribute has been removed.",
               "Supplied attribute 'status' has been replaced with a generated value",
               "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBT aut-num AS1309", "aut-num", "AS1309", "member-of:\\s*AS-TEST")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
    }

    def "create 2 aut-num objs, both member-of existing 2 level set, ref mntners used for auth"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("AS-SET-2LEVEL"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-rBT aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        query_object_matches("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-rBT as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST", "mbrs-by-ref:\\s*LIR3-MNT")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS456
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS123:AS-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                aut-num:        AS789
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS123:AS-TEST
                mnt-by:         LIR3-MNT
                source:         TEST

                password: lir
                password: lir3
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
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS456"}
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS789"}

        queryObject("-rBT aut-num AS456", "aut-num", "AS456")
        queryObject("-rBT aut-num AS789", "aut-num", "AS789")
    }

    def "create aut-num obj, member-of 2 existing sets, 1 set no corresponding mbrs-by-ref"() {
      given:
        dbfixture(getTransient("TOP-AS-SET"))
        dbfixture(getTransient("AS-SET-3LEVEL"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObject("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")
        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*LIR-MNT")
        query_object_matches("-r -T as-set AS123:AS-TEST:AS-TEST2", "as-set", "AS123:AS-TEST:AS-TEST2", "mbrs-by-ref:\\s*owner-MNT")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-TEST
                member-of:      AS123:AS-TEST:AS-TEST2
                mnt-by:         LIR-MNT
                mnt-by:         LIR2-MNT
                mnt-by:         LIR3-MNT
                source:         TEST

                password: lir
                password: locked
                password: owner3
                """.stripIndent(true)
        )

      then:
        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[aut-num] AS123"}
        ack.errorMessagesFor("Create", "[aut-num] AS123") == [
                "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS123:AS-TEST:AS-TEST2]"]

        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create as-set obj, mbrs-by-ref non existent mntner"() {
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
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                mnt-by:       OWNER-MNT
                mnt-lower:    LIR2-MNT
                mbrs-by-ref:  aardvark-mnt
                source:  TEST

                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any {it.operation == "Create" && it.key == "[as-set] AS123:AS-TEST"}
        ack.errorMessagesFor("Create", "[as-set] AS123:AS-TEST") == [
                "Unknown object referenced aardvark-mnt"]

        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
    }

    def "create as-set object with non existent 16 & 32 bit members"() {
        given:
        dbfixture(getTransient("ASN123"))

        expect:
        queryObjectNotFound("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS94967295", "aut-num", "AS94967295")

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

        queryObject("-rBT as-set AS123:As-TEst", "as-set", "AS123:AS-TEST")
    }

    def "create as-set object with existing & non existing 16 & 32 bit members"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))

      expect:
        queryObjectNotFound("-r -T as-set AS123:AS-TEST", "as-set", "AS123:AS-TEST")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")

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

        queryObject("-rBT as-set AS123:As-TEst", "as-set", "AS123:AS-TEST")
    }

    def "delete as-set object with existing & non existing 16 & 32 bit members"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))
        dbfixture(getTransient("AS-SET-MEMBERS"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                members:      AS1, AS123, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       testing members

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

        queryObjectNotFound("-rBT as-set As-TEst", "as-set", "AS-TEST")
    }

    def "delete aut-num object referenced as member in as-set"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))
        dbfixture(getTransient("AS-SET-MEMBERS"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-rBT as-set AS-TEST", "as-set", "AS-TEST", "members:\\s*AS1, AS123")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")

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
                delete:       testing members

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

        queryObjectNotFound("-rBT aut-num AS123", "aut-num", "AS123")
    }

    def "create aut-num object referenced as member in as-set"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))
        dbfixture(getTransient("AS-SET-MEMBERS"))
        dbfixture(getTransient("ASB16"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-rBT as-set AS-TEST", "as-set", "AS-TEST", "members:\\s*AS1, AS123")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")
        queryObject("-r -T as-block AS0 - AS65535", "as-block", "AS0 - AS65535")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS1
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
        ack.successes.any {it.operation == "Create" && it.key == "[aut-num] AS1"}

        queryObject("-rBT aut-num AS1", "aut-num", "AS1")
    }

    def "modify as-set & aut-num objs, ASN listed in set as member, add mbrs-by-ref & member-of for same pair, objs in wrong order"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))
        dbfixture(getTransient("AS-SET-MEMBERS"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-rBT as-set AS-TEST", "as-set", "AS-TEST", "members:\\s*AS1, AS123")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*owner-MNT")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")

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
                member-of:      AS-test
                mnt-by:         owner-MNT
                source:         TEST

                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                members:      AS1, AS123, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                mbrs-by-ref:  owner-mnt
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST

                password: owner2
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.success
        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)

        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS123"}

        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*owner-MNT")
        query_object_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:\\s*as-test")
    }

    def "modify as-set & aut-num objs, ASN listed in set as member, add mbrs-by-ref & member-of for same pair, then delete as-set"() {
      given:
        dbfixture(getTransient("ASN123"))
        dbfixture(getTransient("ASN94967295"))
        dbfixture(getTransient("AS-SET-MEMBERS"))

      expect:
        queryObject("-r -T as-set AS-TEST", "as-set", "AS-TEST")
        query_object_matches("-rBT as-set AS-TEST", "as-set", "AS-TEST", "members:\\s*AS1, AS123")
        query_object_not_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*owner-MNT")
        queryObjectNotFound("-r -T aut-num AS1", "aut-num", "AS1")
        queryObjectNotFound("-r -T aut-num AS7775535", "aut-num", "AS7775535")
        queryObject("-r -T aut-num AS123", "aut-num", "AS123")
        query_object_not_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:")
        queryObject("-r -T aut-num AS94967295", "aut-num", "AS94967295")
        query_object_not_matches("-rBT aut-num AS94967295", "aut-num", "AS94967295", "member-of:")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                members:      AS1, AS123, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                mbrs-by-ref:  owner-mnt
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST

                aut-num:        AS123
                as-name:        some-name
                descr:          description
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                member-of:      AS-test
                mnt-by:         owner-MNT
                source:         TEST

                as-set:       AS-TEST
                descr:        test as-set
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                members:      AS1, AS123, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                mbrs-by-ref:  owner-mnt
                mnt-by:       OWNER2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                delete:       test ing

                password: owner2
                password: owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.failed
        ack.summary.nrFound == 3
        ack.summary.assertSuccess(2, 0, 2, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any {it.operation == "Modify" && it.key == "[as-set] AS-TEST"}
        ack.successes.any {it.operation == "Modify" && it.key == "[aut-num] AS123"}
        ack.errors.any {it.operation == "Delete" && it.key == "[as-set] AS-TEST"}
        ack.errorMessagesFor("Delete", "[as-set] AS-TEST") == [
                "Object [as-set] AS-TEST is referenced from other objects"]

        query_object_matches("-r -T as-set AS-TEST", "as-set", "AS-TEST", "mbrs-by-ref:\\s*owner-MNT")
        query_object_matches("-rBT aut-num AS123", "aut-num", "AS123", "member-of:\\s*as-test")
    }

}
