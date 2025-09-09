package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class AutNumAuthSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getFixtures() {
        [
            "AS251NOSTAT": """\
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS445NOSTAT": """\
                aut-num:        AS445
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS12667NOSTAT": """\
                aut-num:        AS12667
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                """,
        ]
    }

    @Override
    Map<String, String> getTransients() {
        [
            "IRT1": """\
                irt:          irt-test
                address:      RIPE NCC
                e-mail:       irt-dbtest@ripe.net
                signature:    PGPKEY-D83C3FBD
                encryption:   PGPKEY-D83C3FBD
                auth:         PGPKEY-D83C3FBD
                auth:         MD5-PW \$1\$qxm985sj\$3OOxndKKw/fgUeQO7baeF/  #irt
                irt-nfy:      irt_nfy1_dbtest@ripe.net
                notify:       nfy_dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       OWNER-MNT
                source:       TEST
                """,
            "AS222 - AS333": """\
                as-block:       AS222 - AS333
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "AS222 - AS333-NOLOW": """\
                as-block:       AS222 - AS333
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-DBM-MNT
                source:         TEST
                """,
            "AS12557 - AS13223": """\
                as-block:       AS12557 - AS13223
                descr:          RIPE NCC ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "AS444 - AS555": """\
                as-block:       AS444 - AS555
                descr:          APNIC ASN block
                mnt-by:         RIPE-DBM-MNT
                source:         TEST
                """,
            "AS0 - AS4294967295": """\
                as-block:       AS0 - AS4294967295
                descr:          RIPE ASN block
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "AS-SET": """\
                as-set:         as7775535:as-test:AS94967295
                descr:          test set
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                members:        AS1
                mbrs-by-ref:    LIR-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS-SET-NO-REF": """\
                as-set:         as7775535:as-test:AS94967295
                descr:          test set
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                members:        AS1
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS-SET-200": """\
                as-set:       AS7775535:AS-TEST
                descr:        test as-set
                members:      AS1, AS200, AS3, AS4
                members:      AS65536, AS7775535, AS94967295
                tech-c:       TP1-TEST
                admin-c:      TP1-TEST
                notify:       dbtest@ripe.net
                mnt-by:       LIR2-MNT
                mnt-lower:    LIR2-MNT
                source:  TEST
                """,
            "AS200": """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS250": """\
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS300": """\
                aut-num:        AS300
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS200 accept ANY
                export:         to AS200 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS200
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS444": """\
                aut-num:        AS444
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "AS12666": """\
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                """,
            "ROUTE": """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS200
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                """,
            "AS12668": """\
                aut-num:        AS12668
                as-name:        End-User-1
                descr:          description
                status:         legacy
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                """,
        ]
    }

    def "create aut-num, with mnt-by RS and LIR, and a parent mnt-lower RS"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, with mnt-by RS and LIR, and a parent no mnt-lower, mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("AS222 - AS333-NOLOW") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                default:        to AS8505
                                action pref=100;
                                networks ANY
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   dbm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, with mnt-by RS and LIR, mnt-by pw supplied, and a parent mnt-lower RS, no pw supplied"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
        ack.errorMessagesFor("Create", "[aut-num] AS250") ==
              ["Authorisation for [as-block] AS222 - AS333 failed using \"mnt-lower:\" not authenticated by: RIPE-NCC-HM-MNT"]

        queryObjectNotFound("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, with mnt-by RS and LIR, no pw for parent mnt-lower RS, with override"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:       denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
        ack.infoSuccessMessagesFor("Create", "[aut-num] AS250") == [
                "Authorisation override used"]

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, mnt-by pw supplied, no parent"() {
      expect:
        queryObjectNotFound("-rBG -T aut-num AS650", "aut-num", "AS650")

    when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS650
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST

                password:   nccend
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS650" }
        ack.errorMessagesFor("Create", "[aut-num] AS650") ==
              ["No parent as-block found for AS650"]

        queryObjectNotFound("-rBG -T aut-num AS650", "aut-num", "AS650")
    }

    def "create aut-num, mnt-by pw supplied, no parent, with override"() {
      expect:
        queryObjectNotFound("-rBG -T aut-num AS650", "aut-num", "AS650")

      when:
        def message = syncUpdate("""
                aut-num:        AS650
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                source:         TEST
                override:      denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS650" }
        ack.infoSuccessMessagesFor("Create", "[aut-num] AS650") == [
                "Authorisation override used"]

        queryObject("-rBG -T aut-num AS650", "aut-num", "AS650")
    }

    // Create aut-num, testing some elements of policy attrs
    // this objects tests various flavors of the attributes:
    // mp-import mp-export mp-default
    // import export default
    // we test AS 32 number on itself
    // we test AS 32 number as a part of the set name
    def "create aut-num, testing some elements of policy attrs"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:      AS250
                as-name:      ASTEST
                descr:        TEST TELEKOM
                import:       from AS1 accept {1.2.3.4/24}
                export:       to AS2 announce {1.2.3.4/24}
                import:       from AS1 accept (AS75535 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                import:       from AS1:as-myset:AS94967295:As-otherset accept community.contains(65536:65535)
                import:       from AS1:as-myset:AS94967295:As-otherset accept <.* AS7775535 .*>
                import:       from AS1 accept community.contains(4294967295:65535)
                import:       from AS1 accept community.contains(0:0)
                import:       from AS1 accept community.contains(1:65535)
                import:       from AS1 accept community.contains(1:65535)
                import:       from AS1 accept community.contains(65536:2)
                import:       from AS1 accept community.contains(1:2)
                import:       from AS1 accept community.contains(1000)
                mp-import:       from AS1 accept (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-import:       from AS1 accept community.contains(1:65535)
                mp-import:       from AS1:as-myset:AS94967295:As-otherset accept <.* AS7775535 .*>
                mp-import:       from AS1:as-myset:AS94967295:As-otherset accept community.contains(1:65535)
                mp-import:       from AS1 accept community.contains(65536:2)
                mp-import:       from AS1 accept community.contains(1:2)
                mp-import:       from AS1 accept community.contains(1000)
                export:       to AS1 announce (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                export:       to AS1 announce community.contains(1:65535)
                export:       to AS1:as-myset:AS94967295:As-otherset announce <.* AS7775535 .*>
                export:       to AS1:as-myset:AS94967295:As-otherset announce community.contains(1:65535)
                export:       to AS1 announce community.contains(65536:2)
                export:       to AS1 announce community.contains(1:2)
                export:       to AS1 announce community.contains(1000)
                mp-export:       to AS1 announce (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-export:       to AS1 announce community.contains(1:65535)
                mp-export:       to AS1:as-myset:AS94967295:As-otherset announce <.* AS7775535 .*>
                mp-export:       to AS1:as-myset:AS94967295:As-otherset announce community.contains(1:65535)
                mp-export:       to AS1 announce community.contains(65536:2)
                mp-export:       to AS1 announce community.contains(1:2)
                mp-export:       to AS1 announce community.contains(1000)
                default:      to AS1 networks (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                default:      to AS1 networks <.* AS7775535 .*>
                default:      to AS1 networks community.contains(1:0)
                default:      to AS1:as-myset:AS94967295:As-otherset networks community.contains(1:0)
                default:      to AS1 networks community.contains(1:65535)
                default:      to AS1 networks community.contains(65536:2)
                default:      to AS1 networks community.contains(1:2)
                default:      to AS1 networks community.contains(1000)
                mp-default:      to AS1 networks (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-default:      to AS1:as-myset:AS94967295:As-otherset networks community.contains(1:0)
                mp-default:      to AS1 networks <.* AS7775535 .*>
                mp-default:      to AS1 networks community.contains(1:65535)
                mp-default:      to AS1 networks community.contains(65536:2)
                mp-default:      to AS1 networks community.contains(1:2)
                mp-default:      to AS1 networks community.contains(1000)
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(8, 0, 0)
        ack.errorMessagesFor("Create", "[aut-num] AS250") ==
                ["Syntax error in from AS1:as-myset:AS94967295:As-otherset accept community.contains(65536:65535)",
                 "Syntax error in from AS1 accept community.contains(4294967295:65535)",
                 "Syntax error in from AS1 accept community.contains(65536:2)",
                 "Syntax error in from AS1 accept community.contains(65536:2)",
                 "Syntax error in to AS1 announce community.contains(65536:2)",
                 "Syntax error in to AS1 announce community.contains(65536:2)",
                 "Syntax error in to AS1 networks community.contains(65536:2)",
                 "Syntax error in to AS1 networks community.contains(65536:2)"]

        queryObjectNotFound("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    // Create aut-num, testing some elements of policy attrs
    // this objects tests various flavors of the attributes:
    // import mp-default
    // we test AS 32 number on itself
    // we test AS 32 number as a part of the set name
    def "create aut-num, testing some more elements of policy attrs"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:      ASTEST
                descr:        TEST TELEKOM
                import:       from AS1 accept (AS65536 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                import:       from AS1 accept community.contains(22:65536)
                import:       from AS1 accept community.contains(2:-1)
                import:       from AS1:as-myset:AS94967295:As-otherset accept <.* AS234 .*>
                import:       from AS1:as-myset:AS34:As-otherset accept community.contains(1:65535)
                import:       from AS1:as-myset:AS4294967299:As-otherset accept community.contains(6553555:6553566)
                mp-default:      to AS1 networks (AS1 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-default:      to AS1:as-myset:AS34:As-otherset networks community.contains(0:0)
                mp-default:      to AS1 networks <.* AS23 .*>
                mp-default:      to AS1 networks community.contains(1:65535)
                mp-default:      to AS1 networks community.contains(2:65536)
                mp-default:      to AS1 networks community.contains(3:-1)
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(5, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
        ack.errorMessagesFor("Create", "[aut-num] AS250") ==
                ["Syntax error in from AS1 accept community.contains(22:65536)",
                 "Syntax error in from AS1 accept community.contains(2:-1)",
                 "Syntax error in from AS1:as-myset:AS4294967299:As-otherset accept community.contains(6553555:6553566)",
                 "Syntax error in to AS1 networks community.contains(2:65536)",
                 "Syntax error in to AS1 networks community.contains(3:-1)"]

        queryObjectNotFound("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, mix up import/export syntax"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:        ASTEST
                descr:          TEST TELEKOM
                remarks:        following import is missing the 'and'
                remarks:        from AS1 accept (AS65536 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                import:         from AS1 accept (AS65536 not AS7775535  AS1:as-myset:AS94967295:As-otherset)
                import:         to AS1 announce ANY
                import:         from AS1 announce ANY
                import:         from AS1 accept FRED
                export:         from AS1 accept AS2
                export:         to AS1 accept AS2
                mp-import:      afi ipv6.unicast to AS1 announce ANY
                mp-export:      afi ipv6.unicast from AS1 accept AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(7, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
        ack.errorMessagesFor("Create", "[aut-num] AS250") ==
                ["Syntax error in to AS1 announce ANY",
                 "Syntax error in from AS1 announce ANY",
                 "Syntax error in from AS1 accept FRED",
                 "Syntax error in from AS1 accept AS2",
                 "Syntax error in to AS1 accept AS2",
                 "Syntax error in afi ipv6.unicast to AS1 announce ANY",
                 "Syntax error in afi ipv6.unicast from AS1 accept AS2"]

        queryObjectNotFound("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, mp- attrs"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:      ASTEST
                descr:        TEST TELEKOM
                status:       ASSIGNED
                import:       from AS1 accept {1.2.3.4/24}
                export:       to AS2 announce {1.2.3.4/24}
                mp-import:    afi ipv4.unicast from AS1 accept ANY;
                mp-export:    afi ipv6 to AS1 announce {1.2.3.4/24};
                mp-import:    afi ipv4.multicast, ipv6, ipv4.unicast
                              from AS1 1.2.3.4 at 1.2.3.4
                              accept AS2 AND { 1.2.3.4/2, 11:22:33:44:55:66:77:88/35} OR as10;
                mp-import:    afi ipv4
                              from AS1 11:22:33:44:55:66:77:88 at 11:22:33:44:55::87
                              action community.append(100:10);
                              accept ANY;
                mp-default:   afi ipv4, ipv4.unicast, ipv6
                              to AS1 11:22:33:44:55:66:77:88 at 11:22:33:44:55:66:77:87
                              action pref=100;
                              networks {1.2.3.4/24, 1::2/35}
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, mnt-routes attributes are filtered out"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:      ASTEST
                descr:        TEST TELEKOM
                status:       ASSIGNED
                import:       from AS1 accept {1.2.3.4/24}
                export:       to AS2 announce {1.2.3.4/24}
                mp-import:    afi ipv4.unicast from AS1 accept ANY;
                mp-export:    afi ipv6 to AS1 announce {1.2.3.4/24};
                mp-import:    afi ipv4.multicast, ipv6, ipv4.unicast
                              from AS1 1.2.3.4 at 1.2.3.4
                              accept AS2 AND { 1.2.3.4/2, 11:22:33:44:55:66:77:88/35} OR as10;
                mp-import:    afi ipv4
                              from AS1 11:22:33:44:55:66:77:88 at 11:22:33:44:55::87
                              action community.append(100:10);
                              accept ANY;
                mp-default:   afi ipv4, ipv4.unicast, ipv6
                              to AS1 11:22:33:44:55:66:77:88 at 11:22:33:44:55:66:77:87
                              action pref=100;
                              networks {1.2.3.4/24, 1::2/35}
                mnt-routes:   LIR2-MNT {1.2.3.0/25, 1::0/35}
                mnt-routes:   LIR2-MNT {1.2.3.0/25, 1::0/35}
                mnt-routes:   LIR2-MNT {1.2.3.0/25}
                mnt-routes:   LIR2-MNT {1.2.3.0/25^26}
                mnt-routes:   LIR2-MNT {1.2.3.0/25^+, 1.2.3.0/25^-}
                mnt-routes:   LIR2-MNT {1.2.3.0/25^25-25}
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        ack.warningSuccessMessagesFor("Create", "[aut-num] AS250") ==
                ["Deprecated attribute \"mnt-routes\". This attribute has been removed."]

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
        !queryMatches("-rGBT aut-num AS250", "mnt-routes")
    }

    def "create aut-num, import only"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:        ASTEST
                descr:          TEST TELEKOM
                status:         OTHER
                remarks:        following import is missing the 'and'
                remarks:        from AS1 accept (AS65536 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                import:         from AS1 accept ANY
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create aut-num, export only"() {
      given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

      when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:        ASTEST
                descr:          TEST TELEKOM
                remarks:        following import is missing the 'and'
                remarks:        from AS1 accept (AS65536 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                export:         to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "create 32 bit aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS94967295", "aut-num", "AS94967295")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS94967295
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS94967295" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS94967295") ==
              ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT aut-num AS94967295", "aut-num", "AS94967295")
    }

    def "create max 32 bit aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS4294967295", "aut-num", "AS4294967295")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS4294967295
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                sponsoring-org: ORG-LIRA-TEST
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS4294967295" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS4294967295") ==
              ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT aut-num AS4294967295", "aut-num", "AS4294967295")
    }

    def "create highest 16 bit aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS65535", "aut-num", "AS65535")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS65535
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS65535" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS65535") ==
            ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT aut-num AS65535", "aut-num", "AS65535")
    }

    def "create lowest 16 bit aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS0", "aut-num", "AS0")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        As0
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] As0" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] As0") ==
              ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rGBT aut-num As0", "aut-num", "As0")
    }

    def "create aut-num range"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS0 - AS1", "aut-num", "AS0 - AS1")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS0 - AS1
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS0 - AS1" }
        ack.errorMessagesFor("Create", "[aut-num] AS0 - AS1") ==
                ["Syntax error in AS0 - AS1"]

        queryObjectNotFound("-rGBT aut-num AS0 - AS1", "aut-num", "AS0 - AS1")
    }

    def "create -ve aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS-1", "aut-num", "AS-1")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS-1
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS-1" }
        ack.errorMessagesFor("Create", "[aut-num] AS-1") ==
                ["Syntax error in AS-1"]

        queryObjectNotFound("-rGBT aut-num AS-1", "aut-num", "AS-1")
    }

    def "create leading 0 aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS01", "aut-num", "AS01")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS01
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS01" }
        ack.errorMessagesFor("Create", "[aut-num] AS01") ==
                ["Syntax error in AS01"]

        queryObjectNotFound("-rGBT aut-num AS01", "aut-num", "AS01")
    }

    def "create aut-num > 32 bit"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS01", "aut-num", "AS4294967299")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS4294967299
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS4294967299" }
        ack.errorMessagesFor("Create", "[aut-num] AS4294967299") ==
                ["Syntax error in AS4294967299"]

        queryObjectNotFound("-rGBT aut-num AS4294967299", "aut-num", "AS4294967299")
    }

    def "create aut-num AS2.3"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS2.3", "aut-num", "AS2.3")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS2.3
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] AS2.3" }
        ack.errorMessagesFor("Create", "[aut-num] AS2.3") ==
                ["Syntax error in AS2.3"]

        queryObjectNotFound("-rGBT aut-num AS2.3", "aut-num", "AS2.3")
    }

    def "create aut-num, member-of, mbrs-by-ref"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS-SET") + "password: lir\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT as-set as7775535:as-test:AS94967295", "as-set", "as7775535:as-test:AS94967295")
        queryObjectNotFound("-rBG -T aut-num AS200", "aut-num", "AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        As200
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                member-of:      as7775535:as-test:AS94967295
                source:         TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] As200" }

        queryObject("-rGBT aut-num As200", "aut-num", "As200")
    }

    def "create aut-num, member-of, mbrs-by-ref, syntax errors"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS-SET") + "password: lir\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT as-set as7775535:as-test:AS94967295", "as-set", "as7775535:as-test:AS94967295")
        queryObjectNotFound("-rBG -T aut-num AS200", "aut-num", "AS200")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        As200
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                member-of:      as777.5535:as-test:AS94967295
                member-of:      as7775535:as-test:AS7777777234
                member-of:      as7775535:as-test:AS0777234
                source:         TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(3, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] As200" }
        ack.errorMessagesFor("Create", "[aut-num] As200") ==
                ["Syntax error in as777.5535:as-test:AS94967295",
                        "Syntax error in as7775535:as-test:AS7777777234",
                        "Syntax error in as7775535:as-test:AS0777234"
                ]

        queryObjectNotFound("-rGBT aut-num As200", "aut-num", "As200")
    }

    def "create aut-num, member-of, mbrs-by-ref, wrong mntner"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS-SET") + "password: lir\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT as-set as7775535:as-test:AS94967295", "as-set", "as7775535:as-test:AS94967295")
        queryObjectNotFound("-rBG -T aut-num AS200", "aut-num", "AS200")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        As200
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR2-MNT
                member-of:      as7775535:as-test:AS94967295
                source:         TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] As200" }
        ack.errorMessagesFor("Create", "[aut-num] As200") ==
                ["Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [as7775535:as-test:AS94967295]"]

        queryObjectNotFound("-rGBT aut-num As200", "aut-num", "As200")
    }

    def "create aut-num, member-of, no mbrs-by-ref"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS-SET-NO-REF") + "password: lir\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT as-set as7775535:as-test:AS94967295", "as-set", "as7775535:as-test:AS94967295")
        queryObjectNotFound("-rBG -T aut-num AS200", "aut-num", "AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        As200
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                member-of:      as7775535:as-test:AS94967295
                source:         TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] As200" }
        ack.errorMessagesFor("Create", "[aut-num] As200") ==
                ["Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [as7775535:as-test:AS94967295]"]

        queryObjectNotFound("-rGBT aut-num As200", "aut-num", "As200")
    }

    def "create aut-num, member-of, set does not exist"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS200", "aut-num", "AS200")
        queryObjectNotFound("-rGBT as-set as7775535:as-test:AS94967295", "as-set", "as7775535:as-test:AS94967295")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:        As200
                as-name:        ASTEST
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                member-of:      as7775535:as-test:AS94967295
                source:         TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] As200" }
        ack.errorMessagesFor("Create", "[aut-num] As200") ==
                ["Unknown object referenced as7775535:as-test:AS94967295"]

        queryObjectNotFound("-rGBT aut-num As200", "aut-num", "As200")
    }

    def "delete aut-num, RS auth"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                delete:  RS delete

                password:   nccend
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[aut-num] AS200" }

        queryObjectNotFound("-rGBT aut-num AS200", "aut-num", "AS200")
    }

    def "delete aut-num, LIR auth"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                delete:  RS delete

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[aut-num] AS200" }
        ack.errorMessagesFor("Delete", "[aut-num] AS200") ==
                ["Deleting this object requires administrative authorisation"]

        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")
    }

    def "delete aut-num, RS auth, referenced in route"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("ROUTE") + "override: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")
        queryObject("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16")
        query_object_matches("-r -T route -i origin AS200", "route", "20.13.0.0/16", "origin:\\s*AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                delete:  RS delete
                password:   nccend
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[aut-num] AS200" }

        queryObjectNotFound("-rGBT aut-num AS200", "aut-num", "AS200")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "origin:\\s*AS200")
    }

    def "delete aut-num, RS auth, referenced in other aut-num"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS300") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")
        queryObject("-rGBT aut-num AS300", "aut-num", "AS300")
        query_object_matches("-r -T aut-num AS300", "aut-num", "AS300", "AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                delete:  RS delete

                password:   nccend
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[aut-num] AS200" }

        queryObjectNotFound("-rGBT aut-num AS200", "aut-num", "AS200")
        query_object_matches("-r -T aut-num AS300", "aut-num", "AS300", "AS200")
    }

    def "delete aut-num, RS auth, referenced in as-set"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS-SET-200") + "password: lir\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")
        query_object_matches("-rGBT as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST", "AS200")

      when:
          def ack = syncUpdateWithResponse("""
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                delete:  RS delete

                password:   nccend
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[aut-num] AS200" }

        queryObjectNotFound("-rGBT aut-num AS200", "aut-num", "AS200")
        query_object_matches("-rGBT as-set AS7775535:AS-TEST", "as-set", "AS7775535:AS-TEST", "AS200")
    }

    def "modify aut-num, LIR auth"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                remarks:        just added
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS200" }

        query_object_matches("-rGBT aut-num AS200", "aut-num", "AS200", "just added")
    }


    def "modify aut-num, LIR auth, remove RS mntner"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                remarks:        just added
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS200" }
        ack.errorMessagesFor("Modify", "[aut-num] AS200") ==
                ["You cannot add or remove a RIPE NCC maintainer"]

        query_object_matches("-rGBT aut-num AS200", "aut-num", "AS200", "RIPE-NCC-END-MNT")
    }

    def "modify aut-num, added mnt-routes are filtered out"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")
        syncUpdate(getTransient("AS200") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObject("-rGBT aut-num AS200", "aut-num", "AS200")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                status:         ASSIGNED
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                mnt-routes:     ROUTES-MNT      # added
                source:         TEST
                password:   lir
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)
        ack.warningSuccessMessagesFor("No operation", "[aut-num] AS200") ==
                ["Deprecated attribute \"mnt-routes\". This attribute has been removed.",
                 "Submitted object identical to database object"]

        !queryMatches("-rGBT aut-num AS200", "mnt-routes")
    }


    def "create aut-num, (mp-)import/export/default have invalid AS values"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS0", "aut-num", "AS0")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        As0
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS01 accept ANY
                import:         from AS2.1 accept ANY
                import:         from AS7777777234 accept ANY
                export:         to AS1 announce AS02
                export:         to AS1 announce AS2.2
                export:         to AS1 announce AS27777777234
                default:        to AS01 networks community.contains(1000)
                default:        to AS2.1 networks community.contains(1000)
                default:        to AS27777777234 networks community.contains(1000)
                default:        to AS8505
                                action pref=100;
                                networks ANY
                mp-import:      afi ipv6.unicast from AS01 accept ANY
                mp-import:      afi ipv6.unicast from AS2.1 accept ANY
                mp-import:      afi ipv6.unicast from AS17777777234 accept ANY
                mp-export:      afi ipv6.unicast to AS01 announce AS2
                mp-export:      afi ipv6.unicast to AS2.1 announce AS2
                mp-export:      afi ipv6.unicast to AS1 announce AS17777777234
                mp-default:     to AS1 networks (AS065565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-default:     to AS2.1 networks (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-default:     to AS1 networks (AS27777777234 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)
                mp-default:     to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS17777777234:As-otherset)
                mp-default:     to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS777.234:As-otherset)
                mp-default:     to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS077234:As-otherset)
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(21, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[aut-num] As0" }
        ack.errorMessagesFor("Create", "[aut-num] As0") ==
                ["Syntax error in from AS01 accept ANY",
                        "Syntax error in from AS2.1 accept ANY",
                        "Syntax error in from AS7777777234 accept ANY",
                        "Syntax error in to AS1 announce AS02",
                        "Syntax error in to AS1 announce AS2.2",
                        "Syntax error in to AS1 announce AS27777777234",
                        "Syntax error in to AS01 networks community.contains(1000)",
                        "Syntax error in to AS2.1 networks community.contains(1000)",
                        "Syntax error in to AS27777777234 networks community.contains(1000)",
                        "Syntax error in afi ipv6.unicast from AS01 accept ANY",
                        "Syntax error in afi ipv6.unicast from AS2.1 accept ANY",
                        "Syntax error in afi ipv6.unicast from AS17777777234 accept ANY",
                        "Syntax error in afi ipv6.unicast to AS01 announce AS2",
                        "Syntax error in afi ipv6.unicast to AS2.1 announce AS2",
                        "Syntax error in afi ipv6.unicast to AS1 announce AS17777777234",
                        "Syntax error in to AS1 networks (AS065565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)",
                        "Syntax error in to AS2.1 networks (AS65565 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)",
                        "Syntax error in to AS1 networks (AS27777777234 and not AS7775535 and AS1:as-myset:AS94967295:As-otherset)",
                        "Syntax error in to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS17777777234:As-otherset)",
                        "Syntax error in to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS777.234:As-otherset)",
                        "Syntax error in to AS1 networks (AS65565 and not AS7775535 and AS01:as-myset:AS077234:As-otherset)"
                ]

        queryObjectNotFound("-rGBT aut-num As0", "aut-num", "As0")
    }



    def "create very long aut-num, no org ref"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "password: dbm\noverride: denis,override1")

      expect:
        queryObject("-rGBT as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        queryObjectNotFound("-rBG -T aut-num AS702", "aut-num", "AS702")

      when:
        def ack = syncUpdateWithResponse("""
                aut-num:      AS702
                as-name:      AS702
                descr:        ISP
                status:       OTHER
                import:       from AS72 194.98.169.195 at 194.98.169.196 accept AS72
                import:       from AS109 213.53.49.50 at 213.53.49.49 accept AS109
                import:       from AS137 194.242.224.15 at 194.242.224.18 accept AS-FOO1
                import:       from AS286 134.222.249.153 at 134.222.249.154 accept AS-FOO2 AS-FOO3
                import:       from AS286 80.81.192.22 at 80.81.192.1 accept AS-FOO4
                import:       from AS286 148.188.57.158 at 148.188.57.157 accept AS-FOO5
                import:       from AS334 195.66.224.25 at 195.66.224.17 accept AS-FOO6
                import:       from AS553 80.81.192.175 at 80.81.192.1 accept AS-FOO7
                import:       from AS553 192.67.199.71 at 192.67.199.7 accept AS553
                import:       from AS680 149.227.129.22 at 149.227.129.21 accept AS-FOO8
                import:       from AS680 149.227.129.26 at 149.227.129.25 accept AS-FOO8
                import:       from AS701 137.39.30.109 at 137.39.30.110 accept ANY
                import:       from AS701 137.39.30.121 at 137.39.30.122 accept ANY
                import:       from AS701 137.39.30.137 at 137.39.30.138 accept ANY
                import:       from AS701 137.39.30.141 at 137.39.30.142 accept ANY
                import:       from AS701 137.39.30.133 at 137.39.30.134 accept ANY
                import:       from AS701 137.39.5.167 at 146.188.0.134 accept ANY
                import:       from AS701 137.39.30.117 at 137.39.30.118 accept ANY
                import:       from AS701 137.39.30.105 at 137.39.30.106 accept ANY
                import:       from AS701 137.39.30.145 at 137.39.30.146 accept ANY
                import:       from AS701 137.39.5.168 at 146.188.0.135 accept ANY
                import:       from AS701 137.39.7.46 at 146.188.0.114 accept ANY
                import:       from AS701 137.39.7.45 at 146.188.0.113 accept ANY
                import:       from AS703 146.188.8.33 at 146.188.8.34 accept ANY
                import:       from AS705 146.188.31.133 at 146.188.31.130 accept AS705
                import:       from AS705 146.188.31.149 at 146.188.31.145 accept AS705
                import:       from AS705 194.53.78.94 at 194.53.78.93 accept AS705
                import:       from AS705 212.136.177.106 at 212.136.177.105 accept AS705
                import:       from AS705 146.188.30.124 at 146.188.30.122 accept AS705
                import:       from AS705 212.136.177.110 at 212.136.177.109 accept AS705
                import:       from AS705 146.188.30.67 at 146.188.30.65 accept AS705
                import:       from AS705 146.188.11.214 at 146.188.11.213 accept AS705
                import:       from AS705 146.188.12.122 at 146.188.12.121 accept AS705
                import:       from AS705 158.43.201.6 at 158.43.201.5 accept AS705
                import:       from AS705 158.43.201.2 at 158.43.201.1 accept AS705
                import:       from AS705 146.188.30.68 at 146.188.30.65 accept AS705
                import:       from AS705 212.136.177.186 at 212.136.177.185 accept AS705
                import:       from AS705 194.229.233.2 at 194.229.233.1 accept AS705
                import:       from AS705 146.188.30.123 at 146.188.30.121 accept AS705
                import:       from AS705 194.229.233.6 at 194.229.233.5 accept AS705
                import:       from AS705 193.128.46.78 at 193.128.46.77 accept AS705
                import:       from AS705 193.128.46.74 at 193.128.46.73 accept AS705
                import:       from AS705 158.43.253.82 at 158.43.253.81 accept AS705
                import:       from AS705 158.43.253.78 at 158.43.253.77 accept AS705
                import:       from AS705 146.188.13.222 at 146.188.13.221 accept AS705
                import:       from AS705 146.188.13.218 at 146.188.13.217 accept AS705
                import:       from AS705 194.229.233.10 at 194.229.233.9 accept AS705
                import:       from AS705 194.229.233.14 at 194.229.233.13 accept AS705
                import:       from AS705 146.188.2.201 at 146.188.2.202 accept AS705
                import:       from AS705 146.188.30.124 at 146.188.30.121 accept AS705
                import:       from AS705 146.188.30.123 at 146.188.30.122 accept AS705
                import:       from AS705 212.136.177.182 at 212.136.177.181 accept AS705
                import:       from AS705 193.128.40.18 at 193.128.40.17 accept AS705
                import:       from AS705 193.128.40.14 at 193.128.40.13 accept AS705
                import:       from AS705 146.188.9.246 at 146.188.9.245 accept AS705
                import:       from AS705 146.188.11.218 at 146.188.11.217 accept AS705
                import:       from AS705 146.188.2.197 at 146.188.2.198 accept AS705
                import:       from AS705 146.188.30.67 at 146.188.30.66 accept AS705
                import:       from AS705 146.188.30.66 at 146.188.30.68 accept AS705
                import:       from AS705 146.188.9.238 at 146.188.9.237 accept AS705
                import:       from AS705 146.188.9.234 at 146.188.9.233 accept AS705
                import:       from AS705 146.188.9.242 at 146.188.9.241 accept AS705
                import:       from AS705 146.188.31.211 at 146.188.31.209 accept AS705
                import:       from AS705 146.188.2.254 at 146.188.2.253 accept AS705
                import:       from AS705 146.188.30.211 at 146.188.30.210 accept AS705
                import:       from AS705 146.188.16.99 at 146.188.16.98 accept AS705
                import:       from AS705 146.188.16.99 at 146.188.16.97 accept AS705
                import:       from AS705 146.188.30.37 at 146.188.30.35 accept AS705
                import:       from AS705 146.188.30.242 at 146.188.30.241 accept AS705
                import:       from AS786 158.43.37.202 at 158.43.37.201 accept AS-FOO9
                import:       from AS789 194.68.129.208 at 194.68.129.236 accept AS789
                import:       from AS790 193.110.226.18 at 193.110.226.42 accept AS-FOO1
                import:       from AS1103 145.145.166.73 at 145.145.166.74 accept AS-FOO11
                import:       from AS1136 193.148.15.89 at 193.148.15.98 accept AS-FOO12
                import:       from AS1136 193.148.15.144 at 193.148.15.98 accept AS-FOO12
                import:       from AS1140 193.148.15.49 at 193.148.15.98 accept AS1140
                import:       from AS1140 213.53.28.166 at 213.53.28.165 accept AS1140
                import:       from AS1200 146.188.15.1 at 193.148.15.77 accept AS1200
                import:       from AS1200 193.148.15.1 at 193.148.15.98 accept AS1200
                import:       from AS1257 130.244.193.50 at 130.244.193.49 accept AS-FOO13
                import:       from AS1257 146.188.49.14 at 146.188.49.13 accept AS-FOO13
                import:       from AS1267 146.188.37.54 at 146.188.37.53 accept AS-FOO14
                import:       from AS1273 80.81.192.33 at 80.81.192.1 accept AS1273
                import:       from AS1273 192.67.199.9 at 192.67.199.7 accept AS-ECRC
                import:       from AS1299 146.188.67.86 at 143.188.67.85 accept AS-FOO15
                import:       from AS1299 213.248.78.85 at 213.248.78.86 accept AS-FOO15
                import:       from AS1299 149.188.64.114 at 146.188.64.113 accept AS-FOO16
                import:       from AS1299 146.188.57.154 at 146.188.57.153 accept AS-FOO15
                import:       from AS1299 213.248.70.73 at 213.248.70.74 accept AS-FOO15
                import:       from AS1342 146.188.52.174 at 146.188.52.173 accept AS1342
                import:       from AS1653 130.242.94.117 at 130.242.94.118 accept AS-FOO17
                import:       from AS1653 130.242.94.113 at 130.242.94.114 accept AS-FOO17
                import:       from AS1680 194.7.12.162 at 194.7.0.13 accept AS1680
                import:       from AS1759 146.188.40.138 at 146.188.40.137 accept AS-FOO19 AS719 AS790 AS1741 AS2686 AS3246 AS5556 AS6667 AS6743 AS6793
                import:       from AS1760 213.174.192.45 at 213.174.192.46 accept AS1760
                import:       from AS1835 192.38.7.1 at 192.38.7.60 accept AS-FOO20
                import:       from AS1880 194.68.128.34 at 194.68.128.81 accept AS1880
                import:       from AS1883 194.68.128.26 at 194.68.128.81 accept AS1883
                import:       from AS1889 158.43.127.22 at 158.43.127.21 accept AS1889
                import:       from AS1889 212.155.124.29 at 212.155.124.30 accept AS1889
                import:       from AS1889 213.53.46.174 at 213.53.46.173 accept AS1889
                import:       from AS1890 212.206.255.217 at 212.206.255.218 accept AS-FOO21
                import:       from AS1890 212.136.184.5 at 212.136.184.6 accept AS-FOO21
                import:       from AS1890 212.136.184.25 at 212.136.184.26 accept AS-FOO21
                import:       from AS1902 194.50.100.220 at 194.50.100.25 accept AS1902
                import:       from AS1913 146.188.38.154 at 146.188.38.153 accept AS419 AS450 AS466 AS473 AS509 AS655 AS721 AS1559 AS1562 AS1569 AS1569 AS1580 AS1583 AS1584 AS1589 AS1913 AS1959 AS1986 AS2746 AS3739 AS3895 AS5180 AS5188 AS5248 AS5329 AS5839
                import:       from AS1913 139.4.44.246 at 139.4.44.245 accept AS419 AS450 AS509 AS655 AS1551 AS1562 AS1564 AS1565 AS1569 AS1580 AS1583 AS1589 AS1733 AS1913 AS2629 AS3542 AS3872 AS3888 AS3895 AS4483 AS5180 AS5232 AS5248 AS5836
                import:       from AS2047 146.188.35.18 at 146.188.35.17 accept AS2047
                import:       from AS2110 146.188.38.122 at 146.188.38.121 accept AS-FOO22
                import:       from AS2116 193.156.90.3 at 193.156.90.20 accept AS-FOO23
                import:       from AS2119 193.156.90.2 at 193.156.90.20 accept AS-FOO24
                import:       from AS2120 193.156.90.4 at 193.156.90.20 accept AS2120
                import:       from AS2129 146.188.66.2 at 146.188.66.1 accept AS2129
                import:       from AS2162 146.188.37.18 at 146.188.37.17 accept AS2162
                import:       from AS2164 146.188.53.134 at 146.188.53.133 accept AS2164
                import:       from AS2167 158.43.25.150 at 158.43.25.149 accept AS2167
                import:       from AS2172 158.43.58.162 at 158.43.58.161 accept AS2172
                import:       from AS2200 194.68.129.103 at 194.68.129.236 accept AS-FOO26
                import:       from AS2486 194.68.129.238 at 194.68.129.236 accept AS-FOO27
                import:       from AS2529 195.66.224.13 at 195.66.224.16 accept AS-FOO28
                import:       from AS2578 146.188.65.230 at 146.188.65.229 accept AS-FOO29 AS-FOO30
                import:       from AS2503 193.10.252.5 at 146.188.1.40 accept AS-FOO31
                import:       from AS2611 194.53.172.65 at 194.53.172.78 accept AS-FOO32
                import:       from AS2611 193.148.15.43 at 193.148.15.98 accept AS-FOO32
                import:       from AS2647 146.188.35.22 at 146.188.35.21 accept AS2647
                import:       from AS2686 193.148.15.191 at 193.148.15.77 accept AS-FOO33
                import:       from AS2686 193.148.15.37 at 193.148.15.77 accept AS-FOO33
                import:       from AS2686 193.148.15.194 at 193.148.15.77 accept AS-FOO33
                import:       from AS2686 80.81.192.199 at 80.81.192.1 accept AS2686
                import:       from AS2686 193.156.90.9 at 193.156.90.20 accept AS-FOO33
                import:       from AS2686 195.66.226.27 at 195.66.226.16 accept AS-FOO33
                import:       from AS2686 195.66.224.27 at 195.66.224.16 accept AS-FOO33
                import:       from AS2686 192.65.185.143 at 192.65.185.167 accept AS-FOO33
                import:       from AS2686 194.68.128.44 at 194.68.128.81 accept AS-FOO33
                import:       from AS2686 194.50.100.70 at 194.50.100.25 accept AS-FOO33
                import:       from AS2686 194.53.172.77 at 194.53.172.78 accept AS-FOO34
                import:       from AS2686 193.203.0.3 at 193.203.0.72 accept AS-FOO34
                import:       from AS2686 193.149.1.153 at 193.149.1.144 accept AS-FOO34
                import:       from AS2686 193.110.226.10 at 193.110.226.42 accept AS-FOO34
                import:       from AS2686 192.38.7.8 at 192.38.7.60 accept AS-FOO34
                import:       from AS2818 158.43.66.242 at 158.43.66.241 accept AS2818
                import:       from AS2818 195.66.224.103 at 195.66.224.16 accept AS-FOO36
                import:       from AS2818 195.66.226.103 at 195.66.226.16 accept AS-FOO36
                import:       from AS2819 194.50.100.51 at 194.50.100.25 accept AS2819
                import:       from AS2819 194.50.100.50 at 194.50.100.25 accept AS2819
                import:       from AS2822 146.188.48.34 at 146.188.48.33 accept AS2822
                import:       from AS2822 146.188.48.38 at 146.188.48.37 accept AS-FOO37
                import:       from AS2830 195.54.64.186 at 195.54.64.185 accept AS2830
                import:       from AS2830 213.129.16.94 at 213.129.16.93 accept AS2830
                import:       from AS2830 195.54.84.42 at 195.54.84.41 accept AS2830
                import:       from AS2830 195.54.64.198 at 195.54.64.197 accept AS2830
                import:       from AS2830 195.24.1.254 at 195.24.1.253 accept AS2830
                import:       from AS2830 213.53.61.142 at 213.53.61.141 accept AS2830
                import:       from AS2830 213.53.98.14 at 213.53.98.13 accept AS2830
                import:       from AS2830 192.16.190.22 at 192.16.190.21 accept AS2830
                import:       from AS2830 213.129.5.22 at 213.129.5.21 accept AS2830
                import:       from AS2830 195.54.84.45 at 146.188.0.90 accept AS2830
                import:       from AS2830 213.237.141.158 at 213.237.141.157 accept AS2830
                import:       from AS2830 195.54.64.190 at 195.54.64.189 accept AS2830
                import:       from AS2830 212.190.98.34 at 212.190.98.33 accept AS2830
                import:       from AS2830 195.24.4.78 at 195.24.4.77 accept AS2830
                import:       from AS2830 212.190.98.70 at 212.190.98.69 accept AS2830
                import:       from AS2830 213.53.45.18 at 213.53.45.17 accept AS2830
                import:       from AS2830 212.190.126.74 at 212.190.126.73 accept AS2830
                import:       from AS2830 212.190.98.22 at 212.190.98.21 accept AS2830
                import:       from AS2830 213.53.108.18 at 213.53.108.17 accept AS2830
                import:       from AS2830 213.237.144.206 at 213.237.144.205 accept AS2830
                import:       from AS2830 213.237.141.154 at 213.237.141.153 accept AS2830
                import:       from AS2830 213.237.144.202 at 213.237.144.201 accept AS2830
                import:       from AS2830 212.190.126.246 at 212.190.126.245 accept AS2830
                import:       from AS2830 213.53.27.221 at 213.53.27.222 accept AS2830
                import:       from AS2830 212.190.98.74 at 212.190.98.73 accept AS2830
                import:       from AS2830 212.190.251.162 at 212.190.251.161 accept AS2830
                import:       from AS2830 212.190.251.158 at 212.190.251.157 accept AS2830
                import:       from AS2830 212.190.126.186 at 212.190.126.185 accept AS2830
                import:       from AS2830 212.190.126.102 at 212.190.126.101 accept AS2830
                import:       from AS2830 158.169.128.5 at 146.188.0.115 accept AS2830
                import:       from AS2830 212.190.126.106 at 212.190.126.105 accept AS2830
                import:       from AS2830 212.190.126.82 at 212.190.126.81 accept AS2830
                import:       from AS2830 212.190.126.70 at 212.190.126.69 accept AS2830
                import:       from AS2830 212.190.251.186 at 212.190.251.185 accept AS2830
                import:       from AS2830 212.190.126.86 at 212.190.126.85 accept AS2830
                import:       from AS2830 213.53.35.86 at 213.53.35.85 accept AS2830
                import:       from AS2830 212.190.126.66 at 212.190.126.65 accept AS2830
                import:       from AS2830 194.7.12.198 at 194.7.12.197 accept AS2830
                import:       from AS2830 158.169.122.5 at 194.7.0.207 accept AS2830
                import:       from AS2830 158.169.122.5 at 194.7.0.243 accept AS2830
                import:       from AS2830 212.190.126.166 at 212.190.126.165 accept AS2830
                import:       from AS2830 213.53.49.2 at 213.53.49.1 accept AS2830
                import:       from AS2830 212.190.251.146 at 212.190.251.145 accept AS2830
                import:       from AS2830 213.53.49.58 at 213.53.49.57 accept AS2830
                import:       from AS2830 212.190.251.190 at 212.190.251.189 accept AS2830
                import:       from AS2830 213.53.43.2 at 213.53.43.1 accept AS2830
                import:       from AS2830 212.190.251.46 at 212.190.251.45 accept AS2830
                import:       from AS2830 213.53.52.22 at 213.53.52.21 accept AS2830
                import:       from AS2830 213.53.47.30 at 213.53.47.29 accept AS2830
                import:       from AS2830 213.53.43.170 at 213.53.43.169 accept AS2830
                import:       from AS2830 213.53.108.82 at 213.53.108.81 accept AS2830
                import:       from AS2830 213.53.97.2 at 213.53.97.1 accept AS2830
                import:       from AS2830 213.53.108.94 at 213.53.108.93 accept AS2830
                import:       from AS2830 212.190.151.238 at 212.190.151.237 accept AS2830
                import:       from AS2830 212.190.98.18 at 212.190.98.17 accept AS2830
                import:       from AS2830 212.190.126.242 at 212.190.126.241 accept AS2830
                import:       from AS2830 158.43.21.226 at 158.43.21.225 accept AS2830
                import:       from AS2830 158.43.16.178 at 158.43.16.177 accept AS2830
                import:       from AS2830 158.43.125.226 at 158.43.125.225 accept AS2830
                import:       from AS2830 146.188.48.166 at 146.188.48.165 accept AS2830
                import:       from AS2830 158.43.125.222 at 158.43.125.221 accept AS2830
                import:       from AS2830 213.53.97.22 at 213.53.97.21 accept AS2830
                import:       from AS2830 158.43.18.50 at 158.43.18.49 accept AS2830
                import:       from AS2830 158.43.18.46 at 158.43.18.45 accept AS2830
                import:       from AS2830 158.43.16.70 at 158.43.16.69 accept AS2830
                import:       from AS2830 158.43.10.190 at 158.43.10.189 accept AS2830
                import:       from AS2830 146.188.33.242 at 146.188.33.241 accept AS2830
                import:       from AS2830 146.188.48.74 at 146.188.48.73 accept AS2830
                import:       from AS2830 213.53.61.182 at 213.53.61.181 accept AS2830
                import:       from AS2830 213.53.53.82 at 213.53.53.81 accept AS2830
                import:       from AS2830 213.53.39.246 at 213.53.39.245 accept AS2830
                import:       from AS2830 146.188.48.78 at 146.188.48.77 accept AS2830
                import:       from AS2830 139.4.234.250 at 139.4.234.249 accept AS2830
                import:       from AS2830 213.53.51.58 at 213.53.51.57 accept AS2830
                import:       from AS2830 213.53.28.34 at 213.53.28.33 accept AS2830
                import:       from AS2830 213.53.53.238 at 213.53.53.237 accept AS2830
                import:       from AS2830 146.188.35.126 at 146.188.35.125 accept AS2830
                import:       from AS2830 146.188.38.198 at 146.188.38.197 accept AS2830
                import:       from AS2830 146.188.52.38 at 146.188.52.37 accept AS2830
                import:       from AS2830 213.53.31.18 at 213.53.31.17 accept AS2830
                import:       from AS2830 213.53.32.18 at 213.53.32.17 accept AS2830
                import:       from AS2830 213.53.48.74 at 213.53.48.73 accept AS2830
                import:       from AS2830 213.53.61.186 at 213.53.61.185 accept AS2830
                import:       from AS2830 149.230.224.42 at 149.230.224.41 accept AS2830
                import:       from AS2830 149.230.225.58 at 149.230.225.57 accept AS2830
                import:       from AS2830 146.188.33.226 at 146.188.33.225 accept AS2830
                import:       from AS2830 146.188.59.10 at 146.188.59.9 accept AS2830
                import:       from AS2830 213.53.47.122 at 213.53.47.121 accept AS2830
                import:       from AS2830 213.53.28.174 at 213.53.28.173 accept AS2830
                import:       from AS2830 146.188.59.6 at 146.188.59.5 accept AS2830
                import:       from AS2830 212.249.7.2 at 212.249.7.1 accept AS2830
                import:       from AS2830 213.53.51.54 at 213.53.51.53 accept AS2830
                import:       from AS2830 213.53.54.86 at 213.53.54.85 accept AS2830
                import:       from AS2830 213.53.26.170 at 213.53.26.169 accept AS2830
                import:       from AS2830 213.53.43.182 at 213.53.43.181 accept AS2830
                import:       from AS2830 213.53.28.134 at 213.53.28.133 accept AS2830
                import:       from AS2830 213.53.28.170 at 213.53.28.169 accept AS2830
                import:       from AS2856 193.128.43.50 at 193.128.43.49 accept AS-FOO40
                import:       from AS2871 80.81.192.14 at 80.81.192.1 accept AS-FOO41
                import:       from AS2874 194.68.123.17 at 194.68.123.81 accept AS-FOO42
                import:       from AS2874 194.68.128.17 at 194.68.128.81 accept AS-FOO42
                import:       from AS3083 158.43.56.234 at 158.43.56.233 accept AS3083
                import:       from AS3092 146.188.34.26 at 146.188.34.25 accept AS3092
                import:       from AS3096 158.43.58.74 at 158.43.58.73 accept AS3096
                import:       from AS3209 80.81.193.117 at 80.81.192.1 accept AS-FOO43
                import:       from AS3209 80.81.192.117 at 80.81.192.1 accept AS-FOO43
                import:       from AS3215 198.32.247.11 at 198.32.247.66 accept AS-FOO44
                import:       from AS3215 198.32.247.64 at 198.32.247.66 accept AS-FOO44
                import:       from AS3243 213.13.129.77 at 213.13.129.78 accept AS-3243
                import:       from AS3246 192.38.7.73 at 192.38.7.60 accept AS-FOO45
                import:       from AS3246 193.156.90.21 at 193.156.90.20 accept AS-FOO45
                import:       from AS3246 193.110.226.36 at 193.110.226.42 accept AS-FOO45
                import:       from AS3246 146.188.36.42 at 146.188.36.41 accept AS3246
                import:       from AS3246 194.68.128.41 at 194.68.128.81 accept AS-FOO46
                import:       from AS3257 192.38.7.61 at 192.38.7.60 accept AS-FOO47
                import:       from AS3257 193.156.90.51 at 193.156.90.20 accept AS-FOO47
                import:       from AS3257 193.203.0.19 at 193.203.0.72 accept AS-FOO47
                import:       from AS3257 193.148.15.85 at 193.148.15.77 accept AS-FOO47
                import:       from AS3257 213.200.76.125 at 213.200.76.126 accept AS-FOO47
                import:       from AS3257 80.81.192.30 at 80.81.192.1 accept AS-FOO47
                import:       from AS3257 195.66.226.32 at 195.66.226.16 accept AS-FOO47
                import:       from AS3257 194.50.100.123 at 194.50.100.25 accept AS-FOO47
                import:       from AS3257 195.66.224.32 at 195.66.224.16 accept AS-FOO47
                import:       from AS3257 195.245.196.34 at 195.245.196.32 accept AS-FOO47
                import:       from AS3257 194.68.128.43 at 194.68.128.81 accept AS-FOO47
                import:       from AS3262 146.188.33.142 at 146.188.33.141 accept AS3262 AS15711
                import:       from AS3265 193.148.15.166 at 193.148.15.98 accept AS-FOO48
                import:       from AS3265 146.188.49.158 at 146.188.49.157 accept AS-FOO48
                import:       from AS3265 193.148.15.48 at 193.148.15.98 accept AS-FOO48
                import:       from AS3269 146.188.56.190 at 146.188.56.189 accept AS-FOO49
                import:       from AS3278 139.4.252.174 at 139.4.252.173 accept AS3278
                import:       from AS3286 195.66.224.53 at 195.66.224.16 accept AS-FOO50
                import:       from AS3286 146.188.32.206 at 146.188.32.205 accept AS-IOL
                import:       from AS3291 146.188.37.22 at 146.188.37.21 accept AS-FOO51
                import:       from AS3291 154.14.66.197 at 154.14.66.198 accept AS-FOO51
                import:       from AS3292 193.148.15.104 at 193.148.15.98 accept AS3292
                import:       from AS3292 192.38.7.39 at 192.38.7.60 accept AS-FOO52
                import:       from AS3293 193.156.90.5 at 193.156.90.20 accept AS-3293
                import:       from AS3295 194.98.157.26 at 194.98.157.25 accept AS3295
                import:       from AS3300 146.188.55.226 at 146.188.55.225 accept AS-FOO53
                import:       from AS3300 194.68.128.18 at 194.68.128.23 accept AS-FOO54
                import:       from AS3300 146.188.52.246 at 146.188.52.245 accept AS-FOO54
                import:       from AS3300 195.206.66.217 at 195.206.66.218 accept AS-FOO55
                import:       from AS3302 146.188.39.190 at 146.188.39.189 accept AS-FOO56
                import:       from AS3303 164.128.33.177 at 164.128.33.178 accept AS-FOO56
                import:       from AS3303 164.128.33.177 at 164.128.33.178 accept AS-FOO57
                import:       from AS3304 193.121.42.5 at 193.121.42.6 accept AS-FOO508
                import:       from AS3305 194.68.129.232 at 194.68.129.236 accept AS-FOO59
                import:       from AS3307 146.188.57.206 at 146.188.57.205 accept AS-FOO59 AS-FOO60
                import:       from AS3308 192.38.7.34 at 192.38.7.60 accept AS-FOO61
                import:       from AS3320 146.188.57.94 at 146.188.57.93 accept AS-FOO62
                import:       from AS3320 62.156.133.109 at 62.156.133.110 accept AS-FOO60
                import:       from AS3320 149.227.129.30 at 149.227.129.29 accept AS-FOO60
                import:       from AS3320 149.227.129.18 at 149.227.129.17 accept AS-FOO60
                import:       from AS3320 149.227.129.14 at 149.227.129.13 accept AS-FOO60
                import:       from AS3320 149.227.129.6 at 149.227.129.5 accept AS-FOO63
                import:       from AS3328 146.188.33.26 at 146.188.33.25 accept AS-FOO64
                import:       from AS3328 195.66.224.75 at 195.66.224.16 accept AS-FOO64
                import:       from AS3328 195.66.226.75 at 195.66.226.16 accept AS-FOO64
                import:       from AS3329 62.38.0.143 at 146.188.0.226 accept AS3329 AS-FOO65
                import:       from AS3333 193.148.15.68 at 193.148.15.77 accept AS3333
                import:       from AS3333 193.148.15.68 at 193.148.15.98 accept AS3333
                import:       from AS3336 146.188.67.194 at 146.188.67.193 accept AS3336 AS-FOO66
                import:       from AS3342 146.188.32.102 at 146.188.32.101 accept AS3342
                import:       from AS3347 146.188.63.246 at 146.188.63.245 accept AS3347
                import:       from AS3352 193.149.1.155 at 193.149.1.144 accept AS-TDE AS-FOO67 AS766
                import:       from AS3352 193.149.1.57 at 193.149.1.16 accept AS-TDE AS-FOO67 AS766
                import:       from AS3356 212.73.240.205 at 212.73.240.206 accept AS-FOO68
                import:       from AS3356 146.188.67.202 at 146.188.67.201 accept AS-FOO68
                import:       from AS3561 146.188.41.46 at 146.188.41.45 accept AS-3561
                import:       from AS3561 166.63.195.181 at 166.63.195.182 accept AS-3561
                import:       from AS3561 146.188.39.186 at 146.188.39.185 accept AS-3561
                import:       from AS3561 208.173.212.61 at 208.173.212.62 accept AS-3561
                import:       from AS3569 158.43.58.66 at 158.43.58.65 accept AS3569
                import:       from AS3569 213.11.179.241 at 213.11.179.242 accept AS3569
                import:       from AS3569 194.98.49.249 at 194.98.49.250 accept AS3569
                import:       from AS3569 194.98.203.80 at 194.98.203.81 accept AS3569
                import:       from AS3569 146.188.48.214 at 146.188.48.213 accept AS3569
                import:       from AS3573 146.188.50.82 at 146.188.50.81 accept AS3573
                import:       from AS3573 139.4.170.150 at 139.4.170.149 accept AS3573
                import:       from AS3573 146.188.40.86 at 146.188.40.85 accept AS3573
                import:       from AS3573 146.188.58.86 at 146.188.58.85 accept AS3573
                import:       from AS3615 212.120.129.210 at 212.120.129.209 accept AS3615
                import:       from AS3680 192.16.190.242 at 192.16.190.241 accept AS3680
                import:       from AS3741 146.188.58.162 at 146.188.58.161 accept AS3741
                import:       from AS3917 213.53.31.78 at 213.53.31.77 accept AS3917
                import:       from AS3917 158.43.84.34 at 158.43.84.33 accept AS3917
                import:       from AS3917 213.53.31.74 at 213.53.31.73 accept AS3917
                import:       from AS3917 213.53.61.166 at 213.53.61.165 accept AS3917
                import:       from AS3917 134.146.255.193 at 134.146.255.194 accept AS3917
                import:       from AS3917 146.188.35.70 at 146.188.35.69 accept AS3917
                import:       from AS3922 213.53.97.90 at 213.53.97.89 accept AS3922
                import:       from AS3922 213.53.61.122 at 213.53.61.121 accept AS3922
                import:       from AS4589 195.66.226.43 at 195.66.226.16 accept AS-FOO64
                import:       from AS4589 198.32.247.70 at 198.32.247.66 accept AS-4589
                import:       from AS4589 195.66.224.43 at 195.66.224.16 accept AS-FOO64
                import:       from AS4589 194.68.129.216 at 194.68.129.236 accept AS-FOO64
                import:       from AS4983 158.43.56.230 at 158.43.56.229 accept AS4983
                import:       from AS4983 158.43.16.150 at 158.43.16.149 accept AS4983
                import:       from AS4996 158.43.58.150 at 158.43.58.149 accept AS4996
                export:       to AS15602 146.188.53.34 at 146.188.53.33 announce ANY
                export:       to AS15613 139.4.170.154 at 139.4.170.153 announce ANY
                export:       to AS15623 146.188.41.42 at 146.188.41.41 announce ANY
                export:       to AS15625 213.53.39.130 at 213.53.39.129 announce ANY
                export:       to AS15625 213.53.46.30 at 213.53.46.29 announce ANY
                export:       to AS15633 146.188.39.42 at 146.188.39.41 announce ANY
                export:       to AS15641 146.188.35.54 at 146.188.35.53 announce ANY
                export:       to AS15646 194.7.2.14 at 194.7.2.13 announce ANY
                export:       to AS15653 146.188.38.222 at 146.188.38.221 announce ANY
                export:       to AS15659 193.156.90.31 at 193.156.90.20 announce AS-FOO69 (AS702 AND RS-FOO69)
                export:       to AS15662 62.193.132.33 at 146.188.0.178 announce ANY
                export:       to AS15670 62.177.132.65 at 62.177.132.68 announce ANY
                export:       to AS15674 194.98.157.77 at 194.98.157.78 announce ANY
                export:       to AS15677 158.43.88.94 at 158.43.88.93 announce ANY
                export:       to AS15680 158.43.96.250 at 158.43.96.249 announce ANY
                export:       to AS15681 212.190.126.26 at 212.190.126.25 announce ANY
                export:       to AS15685 194.50.100.16 at 194.50.100.25 announce AS-FOO70 (AS702 AND RS-FOO70)
                export:       to AS15685 194.50.100.15 at 194.50.100.25 announce AS-FOO70 (AS702 AND RS-FOO70)
                export:       to AS15687 195.24.5.106 at 146.188.0.90 announce ANY
                export:       to AS15698 139.4.51.94 at 139.4.51.93 announce ANY
                export:       to AS15709 139.4.50.238 at 139.4.50.237 announce ANY
                export:       to AS15713 213.16.91.254 at 213.16.91.2 announce AS-FOO70 (AS702 AND RS-FOO71)
                export:       to AS15715 146.188.38.142 at 146.188.38.141 announce ANY
                export:       to AS15717 80.81.192.26 at 80.81.192.1 announce AS-FOO71 (AS702 AND RS-FOO71)
                export:       to AS15729 146.188.55.214 at 146.188.55.213 announce ANY
                export:       to AS15730 194.7.12.94 at 194.7.12.93 announce ANY
                export:       to AS15732 146.188.39.194 at 146.188.39.193 announce ANY
                export:       to AS15734 146.188.48.202 at 146.188.48.201 announce ANY
                export:       to AS15740 158.43.56.178 at 158.43.56.177 announce ANY
                export:       to AS15745 213.237.138.246 at 146.188.0.90 announce ANY
                export:       to AS15748 139.4.152.102 at 139.4.152.101 announce ANY
                export:       to AS15763 139.4.187.38 at 139.4.187.37 announce ANY
                export:       to AS15765 146.188.51.206 at 146.188.51.205 announce ANY
                export:       to AS15769 158.43.59.26 at 158.43.59.25 announce ANY
                export:       to AS15770 158.43.96.66 at 158.43.96.65 announce ANY
                export:       to AS15776 212.190.126.130 at 212.190.126.129 announce ANY
                export:       to AS15796 146.188.35.218 at 146.188.35.217 announce ANY
                export:       to AS15809 213.11.88.217 at 213.11.88.218 announce ANY
                export:       to AS15814 212.190.98.206 at 212.190.98.205 announce ANY
                export:       to AS15816 213.237.146.130 at 146.188.0.95 announce ANY
                export:       to AS15826 80.247.224.1 at 80.247.224.2 announce ANY
                export:       to AS15841 212.157.239.9 at 212.157.239.10 announce ANY
                export:       to AS15845 158.43.10.42 at 158.43.10.41 announce ANY
                export:       to AS15846 194.98.6.221 at 194.98.6.222 announce ANY
                export:       to AS15846 194.98.6.201 at 194.98.6.202 announce ANY
                export:       to AS15849 195.24.7.130 at 195.24.7.129 announce ANY
                export:       to AS15852 217.26.224.1 at 146.188.0.129 announce ANY
                export:       to AS15867 139.4.175.38 at 139.4.175.37 announce ANY
                export:       to AS15869 62.3.128.129 at 146.188.0.208 announce ANY
                export:       to AS15890 213.11.88.233 at 213.11.88.234 announce ANY
                export:       to AS15890 213.11.5.73 at 213.11.5.74 announce ANY
                export:       to AS15903 212.157.239.25 at 212.157.239.26 announce ANY
                export:       to AS15904 139.4.174.178 at 139.4.174.177 announce ANY
                export:       to AS15920 146.188.50.198 at 146.188.50.197 announce ANY
                export:       to AS15933 139.4.192.38 at 139.4.192.37 announce ANY
                export:       to AS15954 146.188.49.130 at 146.188.49.129 announce ANY
                export:       to AS15959 139.4.190.26 at 139.4.190.25 announce ANY
                export:       to AS15961 146.188.38.54 at 146.188.38.53 announce ANY
                export:       to AS15961 146.188.52.114 at 146.188.52.113 announce ANY
                export:       to AS15965 212.190.98.170 at 212.190.98.169 announce ANY
                export:       to AS15968 195.214.79.1 at 139.4.174.161 announce ANY
                export:       to AS15986 146.188.56.134 at 146.188.56.133 announce ANY
                export:       to AS15990 158.43.47.2 at 158.43.47.1 announce ANY
                export:       to AS16008 146.188.52.90 at 146.188.52.89 announce ANY
                export:       to AS16013 146.188.55.6 at 146.188.55.5 announce ANY
                export:       to AS16017 212.155.98.249 at 212.155.98.250 announce ANY
                export:       to AS16022 217.69.0.2 at 146.188.0.226 announce ANY
                export:       to AS16032 158.43.116.6 at 158.43.116.5 announce ANY
                export:       to AS16034 158.43.2.62 at 158.43.2.61 announce ANY
                export:       to AS16036 158.43.69.86 at 158.43.69.85 announce ANY
                export:       to AS16036 158.43.58.242 at 158.43.58.241 announce ANY
                export:       to AS16039 146.188.40.86 at 146.188.40.85 announce ANY
                export:       to AS16049 146.188.49.218 at 146.188.49.217 announce ANY
                export:       to AS16050 158.43.234.11 at 194.201.219.10 announce ANY
                export:       to AS16057 194.98.74.253 at 194.98.74.254 announce ANY
                export:       to AS16073 194.98.23.22 at 194.98.23.23 announce ANY
                export:       to AS16079 141.122.9.159 at 146.188.0.68 announce ANY
                export:       to AS16080 212.157.239.101 at 212.157.239.102 announce ANY
                export:       to AS16081 146.188.50.26 at 146.188.50.25 announce ANY
                export:       to AS16082 146.188.52.178 at 146.188.52.177 announce ANY
                export:       to AS16084 139.4.186.114 at 139.4.186.113 announce ANY
                export:       to AS16086 146.188.53.86 at 146.188.53.85 announce ANY
                export:       to AS16086 146.188.50.58 at 146.188.50.57 announce ANY
                export:       to AS16095 192.38.7.66 at 192.38.7.60 announce AS-FOO72 (AS702 AND RS-FOO72)
                export:       to AS16119 139.4.244.186 at 139.4.244.185 announce ANY
                export:       to AS16147 193.148.15.170 at 193.148.15.98 announce AS-FOO73 (AS702 AND RS-FOO73)
                export:       to AS16151 139.4.239.250 at 139.4.239.249 announce ANY
                export:       to AS16152 139.4.145.66 at 139.4.145.65 announce ANY
                export:       to AS16166 158.43.7.86 at 158.43.7.85 announce ANY
                export:       to AS16186 213.179.40.254 at 146.188.1.33 announce ANY
                export:       to AS16191 81.30.0.254 at 146.188.0.201 announce ANY
                export:       to AS16193 146.188.51.186 at 146.188.51.185 announce ANY
                export:       to AS16196 212.157.239.89 at 212.157.239.90 announce ANY
                export:       to AS16204 194.98.18.249 at 194.98.18.250 announce ANY
                export:       to AS16206 146.188.50.254 at 146.188.50.253 announce ANY
                export:       to AS16207 193.156.90.44 at 193.156.90.20 announce AS-FOO74 AS702
                export:       to AS16208 212.157.239.49 at 212.157.239.50 announce ANY
                export:       to AS16209 212.190.126.254 at 212.190.126.253 announce ANY
                export:       to AS16216 212.157.239.209 at 212.157.239.210 announce ANY
                export:       to AS16221 146.188.56.54 at 146.188.56.53 announce ANY
                export:       to AS16233 139.4.102.250 at 139.4.102.249 announce ANY
                export:       to AS16236 212.157.239.197 at 212.157.239.198 announce ANY
                export:       to AS16243 192.16.190.214 at 213.53.42.157 announce ANY
                export:       to AS16246 213.180.32.79 at 146.188.0.202 announce ANY
                export:       to AS16252 212.208.245.55 at 212.208.245.56 announce ANY
                export:       to AS16254 212.208.245.245 at 212.208.245.246 announce ANY
                export:       to AS16266 213.53.28.30 at 213.53.28.29 announce ANY
                export:       to AS16267 146.188.35.62 at 146.188.35.61 announce ANY
                export:       to AS16268 158.43.47.54 at 158.43.47.53 announce ANY
                export:       to AS16272 139.4.160.190 at 139.4.160.189 announce ANY
                export:       to AS16289 146.188.51.66 at 146.188.51.65 announce ANY
                export:       to AS16294 212.190.126.118 at 212.190.126.117 announce ANY
                export:       to AS16294 212.190.126.194 at 212.190.126.193 announce ANY
                export:       to AS16309 146.188.35.226 at 146.188.35.225 announce ANY
                export:       to AS16311 213.53.61.110 at 213.53.61.109 announce ANY
                export:       to AS16311 213.53.38.174 at 213.53.38.173 announce ANY
                export:       to AS16318 213.53.34.122 at 213.53.34.121 announce ANY
                export:       to AS16325 158.43.116.10 at 158.43.116.9 announce ANY
                export:       to AS16350 213.53.56.26 at 213.53.56.25 announce ANY
                export:       to AS16352 146.188.35.162 at 146.188.35.161 announce ANY
                export:       to AS16352 146.188.35.166 at 146.188.35.165 announce ANY
                export:       to AS16356 139.4.159.102 at 139.4.159.101 announce ANY
                export:       to AS16358 139.4.45.218 at 139.4.45.217 announce ANY
                export:       to AS16360 139.4.168.50 at 139.4.168.49 announce ANY
                export:       to AS16368 146.188.35.202 at 146.188.35.201 announce ANY
                export:       to AS16377 146.188.34.18 at 146.188.34.17 announce ANY
                export:       to AS16383 146.188.54.190 at 146.188.54.189 announce ANY
                export:       to AS16638 158.43.53.10 at 158.43.53.9 announce ANY
                export:       to AS17071 158.43.40.126 at 158.43.40.125 announce ANY
                export:       to AS17175 146.188.58.6 at 146.188.58.5 announce ANY
                export:       to AS17175 146.188.58.10 at 146.188.58.9 announce ANY
                export:       to AS18676 194.7.12.250 at 194.7.12.249 announce ANY
                export:       to AS20507 213.53.53.122 at 213.53.53.121 announce ANY
                export:       to AS20507 213.53.37.2 at 213.53.37.1 announce ANY
                export:       to AS20507 213.53.28.198 at 213.53.28.197 announce ANY
                export:       to AS20514 212.209.132.45 at 146.188.0.118 announce ANY
                export:       to AS20542 146.188.52.146 at 146.188.52.145 announce ANY
                export:       to AS20561 159.51.236.65 at 139.4.42.85 announce ANY
                export:       to AS20571 146.188.56.150 at 146.188.56.149 announce ANY
                export:       to AS20573 146.188.51.50 at 146.188.51.49 announce ANY
                export:       to AS20583 194.98.78.9 at 194.98.78.10 announce ANY
                export:       to AS20584 194.98.49.237 at 194.98.49.238 announce ANY
                export:       to AS20585 212.157.239.165 at 212.157.239.166 announce ANY
                export:       to AS20599 146.188.32.34 at 146.188.32.33 announce ANY
                export:       to AS20599 146.188.34.90 at 146.188.34.89 announce ANY
                export:       to AS20618 195.54.64.33 at 195.54.64.34 announce ANY
                export:       to AS20631 146.188.65.14 at 146.188.65.13 announce ANY
                export:       to AS20631 217.172.128.1 at 146.188.1.13 announce ANY
                export:       to AS20676 139.4.42.98 at 139.4.42.97 announce ANY
                export:       to AS20684 146.188.52.98 at 146.188.0.179 announce ANY
                export:       to AS20688 212.155.250.153 at 212.155.250.154 announce AS-FOO74 (AS702 AND RS-FOO74)
                export:       to AS20694 139.4.210.106 at 139.4.210.105 announce ANY
                export:       to AS20696 146.188.40.90 at 146.188.40.89 announce ANY
                export:       to AS20698 146.188.54.26 at 146.188.54.25 announce ANY
                export:       to AS20700 194.11.204.1 at 146.188.0.67 announce ANY
                export:       to AS20705 158.43.48.222 at 158.43.48.221 announce ANY
                export:       to AS20706 139.4.232.58 at 139.4.232.57 announce ANY
                export:       to AS20708 193.108.107.239 at 146.188.0.202 announce ANY
                export:       to AS20713 146.188.40.78 at 146.188.40.77 announce ANY
                export:       to AS20717 212.157.239.185 at 212.157.239.185 announce ANY
                export:       to AS20736 146.188.39.182 at 146.188.39.181 announce ANY
                export:       to AS20740 139.4.23.166 at 139.4.23.165 announce ANY
                export:       to AS20743 146.188.53.102 at 146.188.53.101 announce ANY
                export:       to AS20750 139.4.220.66 at 139.4.220.65 announce ANY
                export:       to AS20752 213.237.172.5 at 213.237.172.6 announce ANY
                export:       to AS20757 146.188.56.30 at 146.188.56.29 announce ANY
                export:       to AS20764 146.188.55.118 at 146.188.55.117 announce ANY
                export:       to AS20765 139.4.32.238 at 139.4.2.57 announce ANY
                export:       to AS20775 139.4.96.42 at 139.4.96.41 announce ANY
                export:       to AS20795 139.4.72.174 at 139.4.72.173 announce ANY
                export:       to AS20798 146.188.53.214 at 146.188.53.213 announce ANY
                export:       to AS20799 146.188.63.98 at 146.188.63.97 announce ANY
                export:       to AS20804 139.4.253.30 at 139.4.253.29 announce ANY
                export:       to AS20805 139.4.253.178 at 139.4.253.177 announce ANY
                export:       to AS20822 139.4.138.106 at 139.4.138.105 announce ANY
                export:       to AS20838 62.151.30.1 at 146.188.1.55 announce ANY
                export:       to AS20843 146.188.49.94 at 146.188.49.93 announce ANY
                export:       to AS20844 146.188.34.46 at 146.188.34.45 announce ANY
                export:       to AS20844 146.188.36.186 at 146.188.36.185 announce ANY
                export:       to AS20855 139.4.99.226 at 139.4.99.225 announce ANY
                export:       to AS20881 158.43.58.222 at 158.43.58.221 announce ANY
                export:       to AS20890 146.188.55.130 at 146.188.55.129 announce ANY
                export:       to AS20896 213.53.55.102 at 213.53.55.101 announce ANY
                export:       to AS20899 139.4.205.238 at 139.4.205.237 announce ANY
                export:       to AS20902 139.4.236.234 at 139.4.236.233 announce ANY
                export:       to AS20914 146.188.32.174 at 146.188.32.173 announce ANY
                export:       to AS20951 213.53.45.226 at 213.53.45.225 announce ANY
                export:       to AS20969 213.53.26.174 at 213.53.26.173 announce ANY
                export:       to AS20984 194.11.79.1 at 146.188.0.166 announce ANY
                export:       to AS20987 139.4.73.214 at 139.4.73.213 announce ANY
                export:       to AS20988 146.188.55.162 at 146.188.55.161 announce ANY
                export:       to AS20998 158.43.22.102 at 158.43.22.101 announce ANY
                export:       to AS20998 158.43.116.194 at 158.43.116.193 announce ANY
                export:       to AS21040 146.188.55.82 at 146.188.0.58 announce ANY
                export:       to AS21047 146.188.56.130 at 146.188.56.129 announce ANY
                export:       to AS21057 146.188.54.226 at 146.188.54.225 announce ANY
                export:       to AS21060 195.24.4.42 at 195.24.4.41 announce ANY
                export:       to AS21069 80.74.134.6 at 80.74.134.5 announce ANY
                export:       to AS21070 194.98.203.205 at 194.98.203.206 announce ANY
                export:       to AS21073 213.53.28.190 at 213.53.28.189 announce ANY
                export:       to AS21076 146.188.34.146 at 146.188.34.145 announce ANY
                export:       to AS21113 146.188.55.182 at 146.188.55.181 announce ANY
                export:       to AS21118 193.109.83.193 at 158.43.191.16 announce ANY
                export:       to AS21126 194.98.172.248 at 194.98.172.249 announce ANY
                export:       to AS21139 212.190.251.26 at 212.190.251.25 announce ANY
                export:       to AS21159 80.89.224.1 at 213.53.42.65 announce ANY
                export:       to AS21176 146.188.49.134 at 146.188.49.133 announce ANY
                export:       to AS21197 139.4.139.30 at 139.4.139.29 announce ANY
                export:       to AS21207 139.4.61.18 at 139.4.61.17 announce ANY
                export:       to AS21221 212.29.160.185 at 212.29.160.186 announce ANY
                export:       to AS21223 146.188.52.150 at 146.188.52.149 announce ANY
                export:       to AS21237 213.53.97.58 at 213.53.97.57 announce ANY
                export:       to AS21239 212.190.120.238 at 212.190.120.237 announce ANY
                export:       to AS21239 212.190.120.226 at 212.190.120.225 announce ANY
                export:       to AS21241 146.188.53.162 at 146.188.53.161 announce ANY
                export:       to AS21253 195.129.99.62 at 146.188.0.68 announce ANY
                export:       to AS21254 212.249.3.225 at 146.188.0.67 announce ANY
                export:       to AS21264 146.188.56.82 at 146.188.56.81 announce ANY
                export:       to AS21273 146.188.56.142 at 146.188.56.141 announce ANY
                export:       to AS21309 213.174.162.1 at 146.188.1.2 announce ANY
                export:       to AS21358 139.4.165.130 at 139.4.165.129 announce ANY
                export:       to AS21366 146.188.56.70 at 146.188.56.69 announce ANY
                export:       to AS21373 194.7.12.182 at 194.7.12.181 announce ANY
                export:       to AS21382 193.189.157.130 at 139.4.43.225 announce ANY
                export:       to AS21397 146.188.38.194 at 146.188.38.193 announce ANY
                export:       to AS21407 146.188.34.210 at 146.188.34.209 announce ANY
                export:       to AS21413 139.4.232.26 at 139.4.232.25 announce ANY
                export:       to AS21414 146.188.60.62 at 146.188.60.61 announce ANY
                export:       to AS21414 146.188.66.134 at 146.188.66.133 announce ANY
                export:       to AS21419 193.98.219.254 at 139.4.166.77 announce ANY
                export:       to AS21424 212.190.126.150 at 212.190.126.149 announce ANY
                export:       to AS21426 193.194.129.4 at 149.227.0.69 announce ANY
                export:       to AS21456 213.129.18.150 at 213.129.18.149 announce ANY
                export:       to AS21467 139.4.237.190 at 139.4.237.189 announce ANY
                export:       to AS21473 139.4.27.202 at 139.4.27.201 announce ANY
                export:       to AS21478 213.53.28.178 at 213.53.28.177 announce ANY
                export:       to AS21489 212.190.98.26 at 212.190.98.25 announce ANY
                export:       to AS21490 212.190.126.42 at 212.190.126.41 announce ANY
                export:       to AS21490 146.188.58.174 at 146.188.58.173 announce ANY
                export:       to AS21595 212.190.126.114 at 212.190.126.113 announce ANY
                export:       to AS21716 146.188.57.177 at 146.188.57.178 announce ANY
                export:       to AS21856 146.188.56.26 at 146.188.56.25 announce ANY
                export:       to AS22435 213.53.46.50 at 213.53.46.49 announce ANY
                export:       to AS22570 212.249.45.225 at 146.188.0.221 announce ANY
                export:       to AS22967 146.188.33.78 at 146.188.33.77 announce ANY
                export:       to AS24581 139.4.148.54 at 139.4.148.53 announce ANY
                export:       to AS24584 195.24.29.26 at 213.237.162.221 announce ANY
                export:       to AS24586 146.188.35.90 at 146.188.35.89 announce ANY
                export:       to AS24586 213.53.35.198 at 213.53.35.197 announce ANY
                export:       to AS24596 158.43.69.222 at 158.43.69.221 announce ANY
                export:       to AS24596 158.43.66.98 at 158.43.66.97 announce ANY
                export:       to AS24598 194.98.50.214 at 194.98.50.213 announce ANY
                export:       to AS24604 158.43.48.206 at 158.43.48.205 announce ANY
                export:       to AS24628 158.43.127.230 at 158.43.127.229 announce ANY
                export:       to AS24648 146.188.58.158 at 146.188.58.157 announce ANY
                export:       to AS24652 213.53.49.126 at 213.53.49.125 announce ANY
                export:       to AS24654 146.188.35.46 at 146.188.35.45 announce ANY
                export:       to AS24657 139.4.148.250 at 139.4.148.249 announce ANY
                export:       to AS24661 158.43.127.110 at 158.43.127.109 announce ANY
                export:       to AS24677 158.43.76.198 at 158.43.76.197 announce ANY
                export:       to AS24677 158.43.76.202 at 158.43.76.201 announce ANY
                export:       to AS24678 158.43.17.182 at 158.43.17.181 announce ANY
                export:       to AS24689 158.43.116.190 at 158.43.116.189 announce ANY
                export:       to AS24760 146.188.48.206 at 146.188.48.205 announce ANY
                export:       to AS24763 194.98.157.110 at 194.98.157.109 announce ANY
                export:       to AS24772 146.188.48.94 at 146.188.48.93 announce ANY
                export:       to AS24773 139.4.71.162 at 139.4.71.161 announce ANY
                export:       to AS24780 193.111.71.252 at 146.188.0.84 announce ANY
                export:       to AS24781 146.188.64.86 at 146.188.64.85 announce ANY
                export:       to AS24849 193.218.254.3 at 146.188.0.226 announce ANY
                export:       to AS24855 146.188.63.218 at 146.188.63.217 announce ANY
                export:       to AS24860 81.27.224.254 at 139.4.230.161 announce ANY
                export:       to AS24951 146.188.48.38 at 146.188.48.37 announce ANY
                export:       to AS24954 194.98.157.217 at 194.98.157.218 announce AS-FOO78 AS702
                export:       to AS25009 194.98.157.177 at 194.98.157.178 announce ANY
                export:       to AS25021 146.188.60.174 at 146.188.60.173 announce ANY
                export:       to AS25027 158.43.127.190 at 158.43.127.189 announce ANY
                export:       to AS25030 193.201.160.12 at 158.43.187.3 announce ANY
                export:       to AS25031 146.188.64.194 at 146.188.64.193 announce ANY
                export:       to AS25058 139.4.160.18 at 139.4.160.17 announce ANY
                export:       to AS25062 146.188.58.90 at 146.188.58.89 announce ANY
                export:       to AS25077 213.53.38.202 at 213.53.38.201 announce ANY
                export:       to AS25090 146.188.40.122 at 146.188.40.121 announce ANY
                export:       to AS25093 146.188.34.202 at 146.188.34.201 announce ANY
                export:       to AS25111 213.237.141.154 at 213.237.141.153 announce ANY
                export:       to AS25117 146.188.33.46 at 146.188.33.45 announce ANY
                export:       to AS25120 146.188.38.158 at 146.188.38.157 announce ANY
                export:       to AS25153 195.24.4.206 at 195.24.4.205 announce ANY
                export:       to AS25160 139.4.65.102 at 139.4.65.101 announce ANY
                export:       to AS25161 146.188.37.78 at 146.188.37.77 announce ANY
                export:       to AS25169 146.188.32.42 at 146.188.32.41 announce ANY
                export:       to AS25175 139.4.213.218 at 139.4.213.217 announce ANY
                export:       to AS25182 212.136.199.186 at 212.136.199.185 announce ANY
                export:       to AS25182 212.136.199.178 at 212.136.199.177 announce ANY
                export:       to AS25182 212.136.199.170 at 212.136.199.169 announce ANY
                export:       to AS25200 158.43.6.166 at 158.43.6.165 announce ANY
                export:       to AS25215 213.11.88.105 at 213.11.88.106 announce ANY
                export:       to AS25222 146.188.66.30 at 146.188.66.29 announce ANY
                export:       to AS25245 139.4.163.182 at 139.4.163.181 announce ANY
                export:       to AS25273 146.188.55.218 at 146.188.55.217 announce ANY
                export:       to AS25281 139.4.199.14 at 139.4.199.13 announce ANY
                export:       to AS25320 158.43.81.78 at 158.43.81.77 announce ANY
                export:       to AS25322 194.98.136.93 at 194.98.136.94 announce ANY
                export:       to AS25324 146.188.54.94 at 146.188.54.93 announce ANY
                export:       to AS25333 139.4.97.38 at 139.4.97.37 announce ANY
                export:       to AS25339 146.188.58.14 at 146.188.58.13 announce ANY
                export:       to AS25346 146.188.63.106 at 146.188.63.105 announce ANY
                export:       to AS25346 146.188.56.98 at 146.188.56.97 announce ANY
                export:       to AS25346 146.188.63.130 at 146.188.63.129 announce ANY
                export:       to AS25346 146.188.56.102 at 146.188.56.101 announce ANY
                export:       to AS25367 212.190.220.218 at 212.190.220.217 announce ANY
                export:       to AS25384 213.237.143.150 at 213.237.143.149 announce ANY
                export:       to AS25391 146.188.54.166 at 146.188.54.165 announce ANY
                export:       to AS25399 195.20.123.9 at 158.43.18.185 announce ANY
                export:       to AS25423 139.4.157.186 at 139.4.157.185 announce ANY
                export:       to AS25432 139.4.253.186 at 139.4.253.185 announce ANY
                export:       to AS25435 146.188.41.10 at 146.188.41.9 announce ANY
                export:       to AS25442 212.155.250.101 at 212.155.250.102 announce ANY
                export:       to AS25458 146.188.61.122 at 146.188.61.121 announce ANY
                export:       to AS25472 62.169.255.5 at 146.188.0.225 announce ANY
                export:       to AS25479 217.16.240.125 at 146.188.0.132 announce ANY
                export:       to AS25485 146.188.55.198 at 146.188.55.197 announce ANY
                export:       to AS25551 213.11.88.129 at 213.11.88.130 announce AS-FOO79 AS702
                export:       to AS25558 158.43.0.42 at 158.43.0.41 announce ANY
                export:       to AS25558 158.43.12.122 at 158.43.12.121 announce ANY
                export:       to AS25579 213.70.29.169 at 139.4.65.57 announce ANY
                export:       to AS28693 213.70.131.10 at 139.4.21.225 announce ANY
                export:       to AS28739 212.157.111.70 at 212.157.111.71 announce ANY
                export:       to AS28754 213.53.66.38 at 213.53.66.37 announce ANY
                export:       to AS28801 139.4.173.42 at 139.4.173.41 announce ANY
                export:       to AS28806 213.53.29.10 at 213.53.29.9 announce ANY
                export:       to AS28807 212.155.242.253 at 212.155.242.254 announce ANY
                export:       to AS28930 212.155.120.229 at 212.155.120.230 announce ANY
                export:       to AS28959 158.43.70.86 at 158.43.70.85 announce ANY
                remarks:      --------------------------------------------------------------
                remarks:      -------------------------------------------------------------
                remarks:      --------------------------------------------------------------
                remarks:      --------------------------------------------------------------
                remarks:      ----------------------------------------------------------------
                remarks:      --------------------------------------------------------------
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                mnt-by:       RIPE-NCC-END-MNT
                source:       TEST

                password:   nccend
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS702" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS702") ==
              ["Supplied attribute 'source' has been replaced with a generated value"]

        queryObject("-rBG -T aut-num AS702", "aut-num", "AS702")
    }


    def "create aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }

    def "create aut-num, ripe as-block, with mnt-by RS and LIR, no status, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T aut-num AS260", "aut-num", "AS260")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS260
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS260" }
            ack.warningSuccessMessagesFor("Create", "[aut-num] AS260") ==
                ["Supplied attribute 'source' has been replaced with a generated value"]

            query_object_matches("-rBG -T aut-num AS260", "aut-num", "AS260", "status:\\s*OTHER")
    }


    def "create aut-num, ripe as-block, with mnt-by RS and LIR, status LEGACY, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
            ack.warningSuccessMessagesFor("Create", "[aut-num] AS250") ==
                    ["Supplied attribute 'status' has been replaced with a generated value"]

            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "create aut-num, ripe as-block, with mnt-by RS and LIR, status OTHER, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 1, 0, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }
            ack.warningSuccessMessagesFor("Create", "[aut-num] AS250") ==
                    ["Supplied attribute 'status' has been replaced with a generated value"]

            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "create aut-num, ripe as-block, with mnt-by LIR, status ASSIGNED, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        queryObjectNotFound("-rBG -T aut-num AS12666", "aut-num", "AS12666")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "create aut-num, ripe as-block, with mnt-by LIR, no status, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        queryObjectNotFound("-rBG -T aut-num AS12666", "aut-num", "AS12666")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS12666" }

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "create aut-num, ripe as-block, with mnt-by LIR, status LEGACY, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        queryObjectNotFound("-rBG -T aut-num AS12666", "aut-num", "AS12666")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS12666" }

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "create aut-num, ripe as-block, with mnt-by LIR, status OTHER, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        queryObjectNotFound("-rBG -T aut-num AS12666", "aut-num", "AS12666")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Create", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, remove status, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")
            syncUpdate(getTransient("AS250") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          updated description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
            ack.warningSuccessMessagesFor("Modify", "[aut-num] AS250") ==
                    ["\"status:\" attribute cannot be removed"]

            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, remove status, LIR pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")
            syncUpdate(getTransient("AS250") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
            ack.warningSuccessMessagesFor("Modify", "[aut-num] AS250") ==
                    ["\"status:\" attribute cannot be removed"]

            query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, remove status, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")
        syncUpdate(getTransient("AS250") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")

        when:
        def message = syncUpdate("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          new description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS250") ==
                ["\"status:\" attribute cannot be removed"]

        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, status LEGACY, remove status, LIR pw, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        syncUpdate(getTransient("AS12666") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          new description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["\"status:\" attribute cannot be removed"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, status LEGACY, remove status, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        syncUpdate(getTransient("AS12666") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          new description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["\"status:\" attribute cannot be removed"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, status OTHER, remove status, LIR pw, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")
        syncUpdate(getTransient("AS444") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_not_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS444
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS444" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS444") ==
                ["\"status:\" attribute cannot be removed","Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*OTHER")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, status OTHER, remove status, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")
        syncUpdate(getTransient("AS444") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_not_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*LEGACY")

        when:
        def message = syncUpdate("""
                aut-num:        AS444
                as-name:        End-User-1
                descr:          description
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS444" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS444") ==
                ["\"status:\" attribute cannot be removed", "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*OTHER")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add ASSIGNED, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          new description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 1, 0)
            ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }

            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add LEGACY, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }
            ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                    ["Supplied attribute 'status' has been replaced with a generated value"]

            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add OTHER, RS pw, not on legacy list"() {
        given:
            syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
            queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
            def ack = syncUpdateWithResponse("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                """.stripIndent(true)
            )

        then:
            ack.summary.nrFound == 1
            ack.summary.assertSuccess(1, 0, 1, 0, 0)
            ack.summary.assertErrors(0, 0, 0, 0)
            ack.countErrorWarnInfo(0, 2, 0)
            ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }
            ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                    ["Supplied attribute 'status' has been replaced with a generated value"]

            query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add ASSIGNED, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }

        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add LEGACY, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, no status, add OTHER, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS251
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS251" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS251") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS251", "aut-num", "AS251", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, no status, add ASSIGNED, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        databaseHelper.addObject("" +
                "aut-num:        AS12666\n" +
                "as-name:        End-User-1\n" +
                "descr:          new description\n" +
                "status:         ASSIGNED\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          new description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }

    def "modify aut-num, ripe as-block, with mnt-by LIR, no status, add OTHER, override, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        databaseHelper.addObject("" +
                "aut-num:        AS12666\n" +
                "as-name:        End-User-1\n" +
                "descr:          description\n" +
                "status:         LEGACY\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "org:            ORG-OTO1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "mnt-by:         LIR-MNT\n" +
                "source:         TEST")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, no status, add ASSIGNED, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_matches("-rBG -T aut-num AS445", "aut-num", "AS445", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS445
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS445" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS445") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS445", "aut-num", "AS445", "status:\\s*OTHER")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, no status, add LEGACY, override, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_matches("-rBG -T aut-num AS445", "aut-num", "AS445", "status:")

        when:
        def message = syncUpdate("""
                aut-num:        AS445
                as-name:        End-User-1
                descr:          new description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS445" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS445") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS445", "aut-num", "AS445", "status:\\s*OTHER")
    }

    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, change to LEGACY, LIR pw, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")
        syncUpdate(getTransient("AS250") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS250
                as-name:        End-User-1
                descr:          new description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS250") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, ripe as-block, with mnt-by RS and LIR, status ASSIGNED, change to OTHER, LIR pw, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "override: denis,override1")
        syncUpdate(getTransient("AS250") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS250
                as-name:        End-User-1
                descr:          new description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                sponsoring-org: ORG-LIRA-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS250") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS250", "aut-num", "AS250", "status:\\s*ASSIGNED")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, status OTHER, change to ASSIGNED, LIR pw, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")
        syncUpdate(getTransient("AS444") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_not_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS444
                as-name:        End-User-1
                descr:          description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS444" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS444") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*OTHER")
    }


    def "modify aut-num, apnic as-block, with mnt-by LIR, status OTHER, change to LEGACY, LIR pw, not on legacy list"() {
        given:
        syncUpdate(getTransient("AS444 - AS555") + "override: denis,override1")
        syncUpdate(getTransient("AS444") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS444 - AS555", "as-block", "AS444 - AS555")
        query_object_not_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS444
                as-name:        End-User-1
                descr:          other description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 4, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS444" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS444") ==
                ["Supplied attribute 'status' has been replaced with a generated value",
                "Supplied attribute 'source' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS444", "aut-num", "AS444", "status:\\s*OTHER")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, status LEGACY, change to ASSIGNED, LIR pw, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        syncUpdate(getTransient("AS12666") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          other description
                status:         ASSIGNED
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, status LEGACY, change to OTHER, LIR pw, on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        syncUpdate(getTransient("AS12666") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")

        when:
        def message = send new Message(
                subject: "",
                body: """\
                aut-num:        AS12666
                as-name:        End-User-1
                descr:          new description
                status:         OTHER
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         LIR-MNT
                source:         TEST

                password:   lir
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12666" }
        ack.warningSuccessMessagesFor("Modify", "[aut-num] AS12666") ==
                ["Supplied attribute 'status' has been replaced with a generated value"]

        query_object_matches("-rBG -T aut-num AS12666", "aut-num", "AS12666", "status:\\s*LEGACY")
    }


    def "modify aut-num, ripe as-block, with mnt-by LIR, status LEGACY, change to ASSIGNED, add mnt-by RS, override, no longer on legacy list"() {
        given:
        syncUpdate(getTransient("AS12557 - AS13223") + "override: denis,override1")
        syncUpdate(getTransient("AS12668") + "override: denis,override1")

        expect:
        queryObject("-rGBT as-block AS12557 - AS13223", "as-block", "AS12557 - AS13223")
        query_object_matches("-rBG -T aut-num AS12668", "aut-num", "AS12668", "status:\\s*LEGACY")

        when:
        def message = syncUpdate("""
                aut-num:        AS12668
                as-name:        End-User-1
                descr:          description
                status:         LEGACY
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST
                override:   denis,override1

                """.stripIndent(true)
        )

        then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[aut-num] AS12668" }

        query_object_matches("-rBG -T aut-num AS12668", "aut-num", "AS12668", "status:\\s*LEGACY")
    }

    def "create aut-num with abuse-c"() {
        given:
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")

        expect:
        queryObject("-rGBT as-block AS222 - AS333", "as-block", "AS222 - AS333")
        queryObjectNotFound("-rBG -T aut-num AS250", "aut-num", "AS250")

        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                abuse-c:        AH1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[aut-num] AS250" }

        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")
    }

    def "not update autnum with abuse-c that references role without abuse-mailbox"() {
        given:
        dbfixture(  """\
                role:         Abuse Handler2
                address:      St James Street
                address:      Burnley
                address:      UK
                e-mail:       dbtest@ripe.net
                admin-c:      TP1-TEST
                tech-c:       TP1-TEST
                nic-hdl:      AH2-TEST
                mnt-by:       LIR-MNT
                source:       TEST
            """.stripIndent(true)
        )
        syncUpdate(getTransient("AS222 - AS333") + "password: dbm\noverride: denis,override1")
        syncUpdate("                aut-num:        AS250\n" +
                "                as-name:        End-User-1\n" +
                "                descr:          description\n" +
                "                sponsoring-org: ORG-LIRA-TEST\n" +
                "                import:         from AS1 accept ANY\n" +
                "                export:         to AS1 announce AS2\n" +
                "                mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "                mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "                org:            ORG-OTO1-TEST\n" +
                "                admin-c:        TP1-TEST\n" +
                "                tech-c:         TP1-TEST\n" +
                "                mnt-by:         RIPE-NCC-END-MNT\n" +
                "                mnt-by:         LIR-MNT\n" +
                "                source:         TEST\n" +
                "\n" +
                "                password:   nccend\n" +
                "                password:   hm\n" +
                "                password:   owner3")

        expect:
        queryObject("-r -T role AH2-TEST", "role", "Abuse Handler2")
        queryObject("-rGBT aut-num AS250", "aut-num", "AS250")

        when:
        def ack = syncUpdateWithResponse("""
                aut-num:        AS250
                as-name:        End-User-1
                descr:          description
                sponsoring-org: ORG-LIRA-TEST
                import:         from AS1 accept ANY
                export:         to AS1 announce AS2
                mp-import:      afi ipv6.unicast from AS1 accept ANY
                mp-export:      afi ipv6.unicast to AS1 announce AS2
                org:            ORG-OTO1-TEST
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                abuse-c:        AH2-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                source:         TEST

                password:   nccend
                password:   hm
                password:   owner3
                """.stripIndent(true)
        )

        then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)

        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[aut-num] AS250" }
        ack.errorMessagesFor("Modify", "[aut-num] AS250") ==
                ["The \"abuse-c\" ROLE object 'AH2-TEST' has no \"abuse-mailbox:\" Add \"abuse-mailbox:\" to the ROLE object, then update the AUT-NUM object"]
    }

}
