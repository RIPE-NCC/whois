package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class RouteSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "AS0 - AS4294967295": """\
                as-block:       AS0 - AS4294967295
                descr:          Full ASN range
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                source:         TEST
                """,
            "AS10000": """\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      ORIGIN-MB-MNT
                source:      TEST
                """,
            "AS200200": """\
                aut-num:     AS200200
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "AS200": """\
                aut-num:     AS200
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "ROUTE-SET": """\
                route-set:   AS200200:rs-test
                descr:       test route set
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                members:     1.2.3.0/24
                mbrs-by-ref: CHILD-MB-MNT
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "ROUTE-SET-16": """\
                route-set:   AS200:rs-test
                descr:       test route set
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                members:     1.2.3.0/24
                mbrs-by-ref: CHILD-MB-MNT
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "ROUTE-SET-NO-REF": """\
                route-set:   AS200200:rs-test2
                descr:       test route set
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                members:     1.2.3.0/24
                mnt-by:      OWNER-MNT
                source:      TEST
                """,
            "ROUTE6-PARENT30": """\
                route6:         2001:600::/30
                descr:          Route
                origin:         AS10000
                mnt-by:         PARENT-MB-MNT
                source:         TEST
                """,
            "ROUTE6-CHILD32": """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                """
   ]}

    def "create child route, community attrs, do -i origin query"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")

      expect:
        // AS0 - AS4294967295
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        // AS10000
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                aggr-bndry: AS1234:AS-mytest:AS3:AS-test:AS7775234
                aggr-mtd: outbound AS1234:AS-mytest:AS3:AS-test:AS7775234
                components: ATOMIC protocol BGP4 community.contains(65535:2)
                inject: at 193.0.0.1
                inject: at rtrs-myset:AS2:rtrs-test:AS234
                inject: at AS234:rtrs-myset:AS2:rtrs-test:AS777234
                inject: action community = {30303:20};
                inject: upon HAVE-COMPONENTS {128.8.0.0/16, 128.9.0.0/16}
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route -i origin AS10000", "route", "99.13.0.0/16", "AS10000")
    }

    def "create child route, community attrs, syntax errs"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                aggr-bndry: AS0.1234:AS-mytest:AS3:AS-test:AS7775234
                aggr-bndry: AS1234:AS-mytest:AS3:AS-test:A05234
                aggr-bndry: AS1234:AS-mytest:AS3:AS-test:AS7777777234
                aggr-mtd: outbound AS1234:AS-mytest:AS3:AS-test:AS7777777234
                aggr-mtd: outbound AS0295:AS-mytest:AS3:AS-test:AS7775234
                aggr-mtd: outbound AS777.234:AS-mytest:AS3:AS-test:AS7775234
                components: ATOMIC protocol BGP4 community.contains(65535:65536)
                components: ATOMIC protocol BGP4 community.contains(65536:65535)
                inject: at 2001:600::/48
                inject: at rtrs-myset:AS2:rtrs-test:AS7777777234
                inject: at AS2.34:rtrs-myset:AS2:rtrs-test:AS777234
                inject: at AS234:rtrs-myset:AS2:rtrs-test:AS0123
                inject: action community = {30303:75535};
                inject: action community = {655350:20};
                inject: upon HAVE-COMPONENTS {2001:600::/48, 128.9.0.0/16}
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(18, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS10000") == [
                "Syntax error in AS0.1234:AS-mytest:AS3:AS-test:AS7775234",
                "Syntax error in AS1234:AS-mytest:AS3:AS-test:A05234",
                "Syntax error in AS1234:AS-mytest:AS3:AS-test:AS7777777234",
                "Syntax error in outbound AS1234:AS-mytest:AS3:AS-test:AS7777777234",
                "Syntax error in outbound AS0295:AS-mytest:AS3:AS-test:AS7775234",
                "Syntax error in outbound AS777.234:AS-mytest:AS3:AS-test:AS7775234",
                "Syntax error in ATOMIC protocol BGP4 community.contains(65535:65536)",
                "Syntax error in ATOMIC protocol BGP4 community.contains(65536:65535)",
                "Syntax error in at 2001:600::/48",
                "Syntax error in at rtrs-myset:AS2:rtrs-test:AS7777777234",
                "Syntax error in at AS2.34:rtrs-myset:AS2:rtrs-test:AS777234",
                "Syntax error in at AS234:rtrs-myset:AS2:rtrs-test:AS0123",
                "Syntax error in action community = {30303:75535};",
                "Syntax error in action community = {655350:20};",
                "Syntax error in upon HAVE-COMPONENTS {2001:600::/48, 128.9.0.0/16}",
                "Attribute \"aggr-mtd\" appears more than once",
                "Attribute \"aggr-bndry\" appears more than once",
                "Attribute \"components\" appears more than once",
        ]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, member-of existing 32 bit route-set, do -i member-of query"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200200", "aut-num", "AS200200")
        syncUpdate(getTransient("ROUTE-SET") + "password: owner")
        queryObject("-r -T route-set AS200200:rs-test", "route-set", "AS200200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200200:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route -i origin AS10000", "route", "99.13.0.0/16", "AS10000")
        query_object_matches("-rGBT route -i member-of AS200200:rs-test", "route", "99.13.0.0/16", "AS200200:rs-test")
    }

    def "create child route, member-of existing 16 bit route-set, do -i member-of query"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200", "aut-num", "AS200")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route -i origin AS10000", "route", "99.13.0.0/16", "AS10000")
        query_object_matches("-rGBT route -i member-of AS200:rs-test", "route", "99.13.0.0/16", "AS200:rs-test")
    }

    def "create child route, member-of existing 32 bit route-set, do -i member-of query, no mbrs-by-ref attr"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200200", "aut-num", "AS200200")
        syncUpdate(getTransient("ROUTE-SET-NO-REF") + "password: owner")
        queryObject("-r -T route-set AS200200:rs-test2", "route-set", "AS200200:rs-test2")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200200:rs-test
                member-of:      AS200200:rs-test2
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS10000") ==
              ["Unknown object referenced AS200200:rs-test",
                      "Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS200200:rs-test2]"
              ]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, non exist 32 bit origin"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        queryObjectNotFound("-r -T aut-num AS10000", "aut-num", "AS10000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, invalid inject address"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        queryObjectNotFound("-r -T aut-num AS10000", "aut-num", "AS10000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                inject:         upon HAVE-COMPONENTS {::/8}
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS10000") ==
              ["Syntax error in upon HAVE-COMPONENTS {::/8}"]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, member-of existing 16 bit route-set, delete reference route-set"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200", "aut-num", "AS200")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200:rs-test
                source:         TEST

                route-set:   AS200:rs-test
                descr:       test route set
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                members:     1.2.3.0/24
                mbrs-by-ref: CHILD-MB-MNT
                mnt-by:      OWNER-MNT
                source:      TEST
                delete: referenced

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.errors.any { it.operation == "Delete" && it.key == "[route-set] AS200:rs-test" }
        ack.errorMessagesFor("Delete", "[route-set] AS200:rs-test") ==
              ["Object [route-set] AS200:rs-test is referenced from other objects"]

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        queryObject("-rGBT route-set AS200:rs-test", "route-set", "AS200:rs-test")
    }

    def "modify child route, add member-of"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200", "aut-num", "AS200")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route -i member-of AS200:rs-test", "route", "99.13.0.0/16", "AS200:rs-test")
    }

    def "modify child route, remove member-of"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("AS200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200", "aut-num", "AS200")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200:rs-test
                source:         TEST

                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        queryObjectNotFound("-rGBT route -i member-of AS200:rs-test", "route", "99.13.0.0/16")
    }

    def "create child route, all op values"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                mnt-routes:     LIR-MNT {99.13.0.0/16^+, 99.13.0.0/16^-, 99.13.0.0/16^16-32, 99.13.0.0/16^24, 99.13.0.0/24}
                mnt-routes:     LIR2-MNT      anY
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route6"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                components:     protocol BGP4 { 0:0:0:0:1:1:1:1/10^+}
                                protocol OSPF { 0:0:0:0:1:1:1:1/12^+}
                inject:         at 1.2.3.4 action pref=100; upon HAVE-COMPONENTS { 0:0:0:0:1:1:1:1/0, 0:0:0:0:1:1:1:1/0 }
                export-comps:   { 2001:600::/48 }
                holes:          2001:600::/48, 2001:600::/56, 2001:600::/64
                mnt-routes:     LIR-MNT {2001:600::/36^+, 2001:600::/36^-, 2001:600::/16^36-48, 2001:600::/16^42, 2001:600::/56}
                mnt-routes:     LIR2-MNT      anY
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }

        queryObject("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "create child route6, holes outside prefix"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                components:     protocol BGP4 { 0:0:0:0:1:1:1:1/10^+}
                                protocol OSPF { 0:0:0:0:1:1:1:1/12^+}
                inject:         at 1.2.3.4 action pref=100; upon HAVE-COMPONENTS { 0:0:0:0:1:1:1:1/0, 0:0:0:0:1:1:1:1/0 }
                export-comps:   { 2001:600::/48 }
                holes:          2001::/30
                mnt-routes:     LIR-MNT {2001:600::/36^+, 2001:600::/36^-, 2001:600::/16^36-48, 2001:600::/16^42, 2001:600::/56}
                mnt-routes:     LIR2-MNT      anY
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.errorMessagesFor("Create", "[route6] 2001:600::/32AS10000") ==
                ["2001::/30 is outside the range of this object"]

        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "modify child route6"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE6-CHILD32") + "password: mb-origin\npassword: mb-child\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/32", "route6", "2001:600::/32")

      expect:
        queryObjectNotFound("-rGBT route 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                components:     protocol BGP4 { 0:0:0:0:1:1:1:1/10^+}
                                protocol OSPF { 0:0:0:0:1:1:1:1/12^+}
                inject:         at 1.2.3.4 action pref=100; upon HAVE-COMPONENTS { 0:0:0:0:1:1:1:1/0, 0:0:0:0:1:1:1:1/0 }
                export-comps:   { 2001:600::/48 }
                holes:          2001:600::/48, 2001:600::/56, 2001:600::/64
                mnt-routes:     LIR-MNT {2001:600::/36^+, 2001:600::/36^-, 2001:600::/16^36-48, 2001:600::/16^42, 2001:600::/56}
                mnt-routes:     LIR2-MNT      anY
                remarks:        just added
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route6] 2001:600::/32AS10000" }

        query_object_matches("-rGBT route6 2001:600::/32", "route6", "2001:600::/32", "just added")
    }

    def "modify child route6, different prefix format"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE6-CHILD32") + "password: mb-origin\npassword: mb-child\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/32", "route6", "2001:600::/32")

      expect:
        queryObjectNotFound("-rGBT route 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::1/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                components:     protocol BGP4 { 0:0:0:0:1:1:1:1/10^+}
                                protocol OSPF { 0:0:0:0:1:1:1:1/12^+}
                inject:         at 1.2.3.4 action pref=100; upon HAVE-COMPONENTS { 0:0:0:0:1:1:1:1/0, 0:0:0:0:1:1:1:1/0 }
                export-comps:   { 2001:600::/48 }
                holes:          2001:600::/48, 2001:600::/56, 2001:600::/64
                mnt-routes:     LIR-MNT {2001:600::/36^+, 2001:600::/36^-, 2001:600::/16^36-48, 2001:600::/16^42, 2001:600::/56}
                mnt-routes:     LIR2-MNT      anY
                remarks:        just added
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 1)
        ack.successes.any { it.operation == "Modify" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.infoSuccessMessagesFor("Modify", "[route6] 2001:600::/32AS10000") == [
                "Value 2001:600::1/32 converted to 2001:600::/32"]

        query_object_matches("-rGBT route6 2001:600::/32", "route6", "2001:600::/32", "just added")
    }

    def "delete child route6"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE6-CHILD32") + "password: mb-origin\npassword: mb-child\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/32", "route6", "2001:600::/32")

      expect:
        queryObjectNotFound("-rGBT route 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete:

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route6] 2001:600::/32AS10000" }

        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "create child route6, community"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 ::/16", "route6", "::/16")

      when:
          def ack = syncUpdateWithResponse("""
                route6:         ::/16
                descr:          test route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                aggr-bndry:     AS771234:AS-mytest:AS3:AS-test:AS775234
                aggr-mtd:       outbound AS771234:AS-mytest:AS3:AS-test:AS775234
                components:     ATOMIC protocol BGP4 community.contains(7295:2)
                inject:         at 193.0.0.1
                inject:         at rtrs-myset:AS2:rtrs-test:AS777234
                inject:         at AS777234:rtrs-myset:AS2:rtrs-test:AS7777234
                inject:         action community = {65535:295};
                inject:         action community = {65535:20};
                inject:         upon HAVE-COMPONENTS {::/8}
                source:         TEST

                password:   mb-child
                password:   mb-origin
                password:   hm
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] ::/16AS10000" }

        queryObject("-rGBT route6 ::/16", "route6", "::/16")
    }

    def "create child route6, community syntax"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 ::/16", "route6", "::/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         ::/16
                descr:          test route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                aggr-bndry:     AS771234:AS-mytest:AS3:AS-test:AS775234
                aggr-mtd:       outbound AS771234:AS-mytest:AS3:AS-test:AS775234
                components:     ATOMIC protocol BGP4 community.contains(94967295:2)
                components:     ATOMIC protocol BGP4 community.contains(7295:94967295)
                inject:         at 193.0.0.1
                inject:         at rtrs-myset:AS2:rtrs-test:AS777234
                inject:         at AS777234:rtrs-myset:AS2:rtrs-test:AS7777234
                inject:         action community = {65535:94967295};
                inject:         action community = {65536:20};
                inject:         upon HAVE-COMPONENTS {::/8}
                inject:         upon HAVE-COMPONENTS {128.8.0.0/16, 128.9.0.0/16, ::/8}
                source:         TEST

                password:   mb-child
                password:   mb-origin
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(6, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] ::/16AS10000" }
        ack.errorMessagesFor("Create", "[route6] ::/16AS10000") ==
              ["Syntax error in ATOMIC protocol BGP4 community.contains(94967295:2)",
                      "Syntax error in ATOMIC protocol BGP4 community.contains(7295:94967295)",
                      "Syntax error in action community = {65535:94967295};",
                      "Syntax error in action community = {65536:20};",
                      "Syntax error in upon HAVE-COMPONENTS {128.8.0.0/16, 128.9.0.0/16, ::/8}",
                      "Attribute \"components\" appears more than once"
              ]

        queryObjectNotFound("-rGBT route6 ::/16", "route6", "::/16")
    }

    def "create child route6, community syntax 2"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 ::/16", "route6", "::/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         ::/16
                descr:          test route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                aggr-bndry:     AS2.234:AS-mytest:AS3:AS-test:AS9294967295
                aggr-mtd:       outbound AS9294967295:AS-mytest:AS3:AS-test:AS2.234
                components:     ATOMIC protocol BGP4 community.contains(20:9294967295)
                inject:         at rtrs-myset:AS3.2:rtrs-test:AS9294967295
                inject:         at AS-1:rtrs-myset:AS2:rtrs-test:AS7295
                inject:         at AS234:rtrs-myset:AS2:rtrs-test:AS0234
                inject:         action community = {9294967295:20};
                inject:         action community = {0:0};
                source:         TEST

                password:   mb-child
                password:   mb-origin
                password:   hm
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(7, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] ::/16AS10000" }
        ack.errorMessagesFor("Create", "[route6] ::/16AS10000") ==
              ["Syntax error in AS2.234:AS-mytest:AS3:AS-test:AS9294967295",
                      "Syntax error in outbound AS9294967295:AS-mytest:AS3:AS-test:AS2.234",
                      "Syntax error in ATOMIC protocol BGP4 community.contains(20:9294967295)",
                      "Syntax error in at rtrs-myset:AS3.2:rtrs-test:AS9294967295",
                      "Syntax error in at AS-1:rtrs-myset:AS2:rtrs-test:AS7295",
                      "Syntax error in at AS234:rtrs-myset:AS2:rtrs-test:AS0234",
                      "Syntax error in action community = {9294967295:20};"
              ]

        queryObjectNotFound("-rGBT route6 ::/16", "route6", "::/16")
    }

    def "create child route6, 32 bit origin"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS200200") + "password: owner\npassword: hm")
        queryObject("-r -T aut-num AS200200", "aut-num", "AS200200")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS200200
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS200200" }

        queryObject("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
        queryObject("-rGBT route6 -i origin AS200200", "route6", "2001:600::/32")
    }

    def "create child route6, member-of 32 bit route-set"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE-SET") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200200:rs-test", "route-set", "AS200200:rs-test")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200200:rs-test
                member-of:      AS200:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }

        queryObject("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "create child route6, member-of 32 bit route-set, no mbrs-by-ref"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE-SET-NO-REF") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200200:rs-test2", "route-set", "AS200200:rs-test2")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200200:rs-test2
                member-of:      AS200:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.errorMessagesFor("Create", "[route6] 2001:600::/32AS10000") ==
              ["Membership claim is not supported by mbrs-by-ref: attribute of the referenced set [AS200200:rs-test2]"]

        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "create child route6, member-of non exist 32 bit route-set"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
        queryObjectNotFound("-r -T route-set AS300300:rs-test", "route-set", "AS300300:rs-test")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS300300:rs-test
                member-of:      AS2000:rs-test
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(2, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.errorMessagesFor("Create", "[route6] 2001:600::/32AS10000") ==
              ["Unknown object referenced AS300300:rs-test",
                      "Unknown object referenced AS2000:rs-test"
              ]

        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "create child route6, member-of 32 bit route-set, then delete referenced route-set"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")
        syncUpdate(getTransient("ROUTE-SET") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200200:rs-test", "route-set", "AS200200:rs-test")
        syncUpdate(getTransient("ROUTE-SET-16") + "password: owner\noverride: denis,override1")
        queryObject("-r -T route-set AS200:rs-test", "route-set", "AS200:rs-test")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                member-of:      AS200200:rs-test
                member-of:      AS200:rs-test
                source:         TEST

                route-set:   AS200200:rs-test
                descr:       test route set
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                members:     1.2.3.0/24
                mbrs-by-ref: CHILD-MB-MNT
                mnt-by:      OWNER-MNT
                source:      TEST
                delete: referenced

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                password:   owner
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2001:600::/32AS10000" }
        ack.errors.any { it.operation == "Delete" && it.key == "[route-set] AS200200:rs-test" }
        ack.errorMessagesFor("Delete", "[route-set] AS200200:rs-test") ==
              ["Object [route-set] AS200200:rs-test is referenced from other objects"]

        queryObject("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
        queryObject("-r -T route-set AS200200:rs-test", "route-set", "AS200200:rs-test")
    }

    def "create child route6, 32 bit origin not exist"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE6-PARENT30") + "password: mb-origin\npassword: hm\npassword: mb-parent")
        queryObject("-r -T route6 2001:600::/30", "route6", "2001:600::/30")

      expect:
        queryObjectNotFound("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2001:600::/32
                descr:          Route
                origin:         AS300300
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT route6 2001:600::/32", "route6", "2001:600::/32")
    }

    def "not create route with reserved as number origin"() {
        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS64496
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 2, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS64496" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS64496") ==
                ["Cannot use reserved AS number 64496"]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create route, warn about unknown as number"() {
        when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS12666
                mnt-by:         CHILD-MB-MNT
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent(true)
        )

        then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 3, 0)
        ack.warningSuccessMessagesFor("Create", "[route] 99.13.0.0/16AS12666") ==
                ["Specified origin AS number 12666 is allocated to the RIPE region but doesn't exist in the RIPE database"]

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create route, hole outside prefix range"() {
        when:
            def message = send new Message(
                    subject: "",
                    body: """\
                    route:          10.1.224.0/21
                    holes:          10.1.0.0/16
                    descr:          Route
                    origin:         AS1000
                    mnt-by:         OWNER-MNT
                    source:         TEST
    
                    password:   owner
                    password:   owner3
                    """.stripIndent(true)
            )

        then:
            def ack = ackFor message

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)

            ack.countErrorWarnInfo(1, 2, 0)
            ack.errors.any { it.operation == "Create" && it.key == "[route] 10.1.224.0/21AS1000" }
            ack.errorMessagesFor("Create", "[route] 10.1.224.0/21AS1000") ==
                    ["10.1.0.0/16 is outside the range of this object"]

            queryObjectNotFound("-rGBT route 10.1.224.0/21AS1000", "route", "10.1.224.0/21AS1000")
    }

    def "create route, hole has invalid prefix length"() {
        when:
            def message = send new Message(
                    subject: "",
                    body: """\
                    route:          10.1.224.0/21
                    holes:          10.1.226.0/21
                    descr:          Route
                    origin:         AS1000
                    mnt-by:         OWNER-MNT
                    source:         TEST
    
                    password:   owner
                    password:   owner3
                    """.stripIndent(true)
            )

        then:
            def ack = ackFor message

            ack.summary.nrFound == 1
            ack.summary.assertSuccess(0, 0, 0, 0, 0)
            ack.summary.assertErrors(1, 1, 0, 0)

            ack.countErrorWarnInfo(1, 1, 0)
            ack.errors.any { it.operation == "Create" && it.key == "[route] 10.1.224.0/21AS1000" }
            ack.errorMessagesFor("Create", "[route] 10.1.224.0/21AS1000") ==
                    ["Syntax error in 10.1.226.0/21"]

            queryObjectNotFound("-rGBT route 10.1.224.0/21AS1000", "route", "10.1.224.0/21AS1000")
    }
}
