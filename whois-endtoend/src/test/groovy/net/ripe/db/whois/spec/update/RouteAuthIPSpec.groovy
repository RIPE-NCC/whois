package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

class RouteAuthIPSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
            "AS0 - AS4294967295": """\
                as-block:       AS0 - AS4294967295
                descr:          Full ASN range
                mnt-by:         RIPE-DBM-MNT
                mnt-lower:      RIPE-NCC-HM-MNT
                changed:        dbtest@ripe.net
                source:         TEST
                """,
            "AS100": """\
                aut-num:        AS100
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "AS200": """\
                aut-num:        AS200
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "AS300": """\
                aut-num:        AS300
                as-name:        ASTEST
                descr:          description
                admin-c:        TP1-TEST
                tech-c:         TP1-TEST
                mnt-by:         RIPE-NCC-END-MNT
                mnt-by:         LIR-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                """,
            "PARENT-ROUTE": """\
                route:       20.128.0.0/9
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "PARENT-ROUTE-LOW-ROUTES": """\
                route:   20.128.0.0/9
                descr:   parent route object
                origin:  AS1000
                mnt-by:  PARENT-MB-MNT
                mnt-lower:   PARENT-ML-MNT
                mnt-routes:  PARENT-MR-MNT
                changed: dbtest@ripe.net 20020101
                source:  TEST
                """,
            "PARENT-ROUTE-ROUTES": """\
                route:   20.128.0.0/9
                descr:   parent route object
                origin:  AS1000
                mnt-by:  PARENT-MB-MNT
                mnt-routes:  PARENT-MR-MNT
                changed: dbtest@ripe.net 20020101
                source:  TEST
                """,
            "PARENT-ROUTE-LOW": """\
                route:       20.128.0.0/9
                descr:       parent route object
                origin:      AS1000
                mnt-by:      PARENT-MB-MNT
                mnt-lower:   PARENT-ML-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "PARENT-ROUTE-LOW2": """\
                route:       20.128.0.0/9
                descr:       parent route object
                origin:      AS3000
                mnt-by:      PARENT-MB-MNT
                mnt-lower:   LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-MATCH-ROUTE-AS2000": """\
                route:   20.13.0.0/16
                descr:   exact match route object
                origin:  AS2000
                mnt-by:  CHILD-MB-MNT
                changed: dbtest@ripe.net 20020101
                source:  TEST
                """,
            "EXACT-ROUTE-LOW": """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-lower:   EXACT-ML-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-ROUTE-ROUTES": """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-ROUTE-LOW-ROUTES": """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-lower:   EXACT-ML-MNT
                mnt-routes:  EXACT-MR-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-INET": """\
                inetnum: 20.13.0.0 - 20.13.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  EXACT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "PARENT-INET": """\
                inetnum: 20.0.0.0 - 20.255.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  PARENT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-INET2": """\
                inetnum: 20.130.0.0 - 20.130.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  EXACT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "PARENT-INET2": """\
                inetnum: 20.128.0.0 - 20.255.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  PARENT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "EXACT-INET3": """\
                inetnum: 21.130.0.0 - 21.130.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  EXACT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
            "PARENT-INET3": """\
                inetnum: 21.128.0.0 - 21.255.255.255
                netname: EXACT-INETNUM
                descr:   Exact match inetnum object
                country: EU
                admin-c: TP1-TEST
                tech-c:  TP1-TEST
                status:  ASSIGNED PA
                mnt-by:  PARENT-INETNUM-MB-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """,
    ]}

    def "create exact match route, no exact pw supplied"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-by:\" not authenticated by: EXACT-MB-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact pw supplied"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, with override"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = syncUpdate("""\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST
                override:  override1

                password:   mb-child
                """.stripIndent()
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.infoSuccessMessagesFor("Create", "[route] 20.13.0.0/16AS2000") == [
                "Authorisation override used"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-LOW") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-lower")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   ml-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-by:\" not authenticated by: EXACT-MB-MNT",
                "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mnt-lower, exact mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-LOW") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-lower")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mnt-routes, exact mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-ROUTES") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-routes:\" not authenticated by: EXACT-MR-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mnt-routes, exact mnt-routes pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-ROUTES") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mb ml mr, exact mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-LOW-ROUTES") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-lower")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-routes:\" not authenticated by: EXACT-MR-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mb ml mr, exact mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-LOW-ROUTES") + "password: mb-exact\npassword: hm")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-lower")

      expect:

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   ml-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-routes:\" not authenticated by: EXACT-MR-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, exact has mb ml mr, exact mnt-routes pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-ROUTE-LOW-ROUTES") + "password: mb-exact\npassword: hm")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-lower")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "modify exact match route, own pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-MATCH-ROUTE-AS2000") + "password: mb-child\npassword: mb-exact")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                remarks:        just added
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "just added")
    }

    def "delete exact match route, own pw supplied"() {
      given:
        syncUpdate(getTransient("EXACT-MATCH-ROUTE-AS2000") + "password: mb-child\npassword: mb-exact")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:   20.13.0.0/16
                descr:   exact match route object
                origin:  AS2000
                mnt-by:  CHILD-MB-MNT
                changed: dbtest@ripe.net 20020101
                source:  TEST
                delete:   exact match

                password:   mb-child
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create child route, parent has mb ml, parent mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-lower:\" not authenticated by: PARENT-ML-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb ml, no parent pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-lower:\" not authenticated by: PARENT-ML-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb ml, parent mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW") + "password: mb-parent")
        query_object_matches("-rGBT route 20.0.0.0/9", "route", "20.0.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.0.0.0/9", "route", "20.0.0.0/9", "mnt-lower")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   ml-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }

        queryObject("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb mr, parent mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-ROUTES") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-routes:\" not authenticated by: PARENT-MR-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb mr, parent mnt-routes pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-ROUTES") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mr-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }

        queryObject("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb ml mr, parent mnt-by pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW-ROUTES") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-routes:\" not authenticated by: PARENT-MR-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb ml mr, parent mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW-ROUTES") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   ml-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-routes:\" not authenticated by: PARENT-MR-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create child route, parent has mb ml mr, parent mnt-routes pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW-ROUTES") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mr-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16", "AS2000")
    }

    def "create child route, 2 exact matching parents, each has mb ml, 2nd parent mnt-lower pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-ROUTE-LOW") + "password: mb-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")
        syncUpdate(getTransient("PARENT-ROUTE-LOW2") + "password: mb-parent\npassword: mb-exact")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-lower")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "mnt-routes")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16", "AS2000")
    }

    def "create route, exact+parent route and exact+parent inet exist with mb, exact route pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.0.0.0 - 20.255.255.255", "inetnum", "20.0.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.13.0.0 - 20.13.255.255", "inetnum", "20.13.0.0 - 20.13.255.255")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.0.0.0/8", "route", "20.0.0.0/8", "AS1000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create route, exact+parent route and exact+parent inet exist with mb, parent route pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.0.0.0 - 20.255.255.255", "inetnum", "20.0.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.13.0.0 - 20.13.255.255", "inetnum", "20.13.0.0 - 20.13.255.255")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.0.0.0/8", "route", "20.0.0.0/8", "AS1000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-by:\" not authenticated by: EXACT-MB-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create route, exact+parent route and exact+parent inet exist with mb, exact inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.0.0.0 - 20.255.255.255", "inetnum", "20.0.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.13.0.0 - 20.13.255.255", "inetnum", "20.13.0.0 - 20.13.255.255")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.0.0.0/8", "route", "20.0.0.0/8", "AS1000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-by:\" not authenticated by: EXACT-MB-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create route, exact+parent route and exact+parent inet exist with mb, parent inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.0.0.0 - 20.255.255.255", "inetnum", "20.0.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.13.0.0 - 20.13.255.255", "inetnum", "20.13.0.0 - 20.13.255.255")

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.0.0.0/8", "route", "20.0.0.0/8", "AS1000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-by:\" not authenticated by: EXACT-MB-MNT",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create route, parent route and exact+parent inet exist with mb, parent route pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET2") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.128.0.0 - 20.255.255.255", "inetnum", "20.128.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET2") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.130.0.0 - 20.130.255.255", "inetnum", "20.130.0.0 - 20.130.255.255")
        syncUpdate(getTransient("PARENT-ROUTE") + "password: mb-parent\npassword: mbi-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mb-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16", "AS2000")
    }

    def "create route, parent route and exact+parent inet exist with mb, exact inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET2") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.128.0.0 - 20.255.255.255", "inetnum", "20.128.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET2") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.130.0.0 - 20.130.255.255", "inetnum", "20.130.0.0 - 20.130.255.255")
        syncUpdate(getTransient("PARENT-ROUTE") + "password: mb-parent\npassword: mbi-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-by:\" not authenticated by: PARENT-MB-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create route, parent route and exact+parent inet exist with mb, parent inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET2") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 20.128.0.0 - 20.255.255.255", "inetnum", "20.128.0.0 - 20.255.255.255")
        syncUpdate(getTransient("EXACT-INET2") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 20.130.0.0 - 20.130.255.255", "inetnum", "20.130.0.0 - 20.130.255.255")
        syncUpdate(getTransient("PARENT-ROUTE") + "password: mb-parent\npassword: mbi-parent")
        query_object_matches("-rGBT route 20.128.0.0/9", "route", "20.128.0.0/9", "AS1000")

      expect:
        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          20.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.130.0.0/16AS2000") ==
                ["Authorisation for [route] 20.128.0.0/9AS1000 failed using \"mnt-by:\" not authenticated by: PARENT-MB-MNT",
                 "The route object 20.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 20.130.0.0/16", "route", "20.130.0.0/16")
    }

    def "create route, exact+parent inet exist with mb, exact inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET3") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 21.128.0.0 - 21.255.255.255", "inetnum", "21.128.0.0 - 21.255.255.255")
        syncUpdate(getTransient("EXACT-INET3") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 21.130.0.0 - 21.130.255.255", "inetnum", "21.130.0.0 - 21.130.255.255")

      expect:
        queryObjectNotFound("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          21.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 21.130.0.0/16AS2000" }

        query_object_matches("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16", "AS2000")
    }

    def "create route, exact+parent inet exist with mb, parent inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET3") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 21.128.0.0 - 21.255.255.255", "inetnum", "21.128.0.0 - 21.255.255.255")
        syncUpdate(getTransient("EXACT-INET3") + "password: mbi-exact\npassword: mbi-parent")
        queryObject("-rGBT inetnum 21.130.0.0 - 21.130.255.255", "inetnum", "21.130.0.0 - 21.130.255.255")

      expect:
        queryObjectNotFound("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          21.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 21.130.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 21.130.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 21.130.0.0/16AS2000") ==
                ["Authorisation for [inetnum] 21.130.0.0 - 21.130.255.255 failed using \"mnt-by:\" not authenticated by: EXACT-INETNUM-MB-MNT",
                 "The route object 21.130.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        queryObjectNotFound("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16")
    }

    def "create route, parent inet exist with mb, parent inet pw supplied"() {
      given:
        syncUpdate(getTransient("PARENT-INET3") + "password: mbi-parent\npassword: hm")
        queryObject("-rGBT inetnum 21.128.0.0 - 21.255.255.255", "inetnum", "21.128.0.0 - 21.255.255.255")

      expect:
        queryObjectNotFound("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          21.130.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-child
                password:   mbi-parent
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 21.130.0.0/16AS2000" }

        query_object_matches("-rGBT route 21.130.0.0/16", "route", "21.130.0.0/16", "AS2000")
    }

    def "create exact match route, mnt-routes test 1"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, mnt-routes test 2"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT   aNy
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, mnt-routes test 3"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/24^+}
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 0, 1, 0, 1)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 2)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.pendingUpdates.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }
        ack.warningPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["This update has only passed one of the two required hierarchical authorisations"]
        ack.infoPendingMessagesFor("Create", "[route] 20.13.0.0/16AS2000") ==
                ["Authorisation for [route] 20.13.0.0/16AS3000 failed using \"mnt-routes:\" no valid maintainer found",
                 "The route object 20.13.0.0/16AS2000 will be saved for one week pending the second authorisation"]

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, mnt-routes test 4"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^-, 20.13.0.0/16^+}
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

    def "create exact match route, mnt-routes test 5"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^-, 20.13.0.0/16^16-24, 21.13.0.0/16^+}
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
    }

    def "modify exact match route, mnt-routes test 6"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}
                mnt-routes:  EXACT-MR-MNT  aNY
                mnt-routes:  EXACT-MR-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(3, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.errorMessagesFor("Modify", "[route] 20.13.0.0/16AS3000") ==
                [
                        "Syntax error in EXACT-MR-MNT {20.13.0.0/16^+} (ANY can only occur as a single value)",
                        "Syntax error in EXACT-MR-MNT aNY (ANY can only occur as a single value)",
                        "Syntax error in EXACT-MR-MNT (ANY can only occur as a single value)"
                ]

        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "mnt-routes")
    }

    def "create exact match route, mnt-routes test 7"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {any, 20.13.0.0/16^+}
                changed:     dbtest@ripe.net
                source:      TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.errorMessagesFor("Modify", "[route] 20.13.0.0/16AS3000") ==
                ["Syntax error in EXACT-MR-MNT {any, 20.13.0.0/16^+}"]

        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "any")
    }

    def "create exact match route, mnt-routes test 8"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.2.3/16^11-12, 20.13.2.3/16^+}
                changed:     dbtest@ripe.net
                source:      TEST

                password:   mb-exact
                password:   mb-child
                password:   mr-exact
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 1, 0)
        ack.countErrorWarnInfo(1, 0, 0)
        ack.errors.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.errorMessagesFor("Modify", "[route] 20.13.0.0/16AS3000") ==
                ["Syntax error in EXACT-MR-MNT {20.13.2.3/16^11-12, 20.13.2.3/16^+}"]

        query_object_not_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "any")
    }

    def "create exact match route, mnt-routes test 9"() {
      given:

      expect:
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       20.13.0.0/16
                descr:       exact match route object
                origin:      AS3000
                mnt-by:      EXACT-MB-MNT
                mnt-routes:  EXACT-MR-MNT {20.13.0.0/16^+}
                mnt-routes:  LIR-MNT
                changed:     dbtest@ripe.net
                source:      TEST

                route:          20.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                changed:        noreply@ripe.net 20120101
                source:         TEST

                password:   mb-exact
                password:   mb-child
                password:   lir
                """.stripIndent()
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 2
        ack.summary.assertSuccess(2, 1, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 20.13.0.0/16AS3000" }
        ack.successes.any { it.operation == "Create" && it.key == "[route] 20.13.0.0/16AS2000" }

        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS3000")
        query_object_matches("-rGBT route 20.13.0.0/16", "route", "20.13.0.0/16", "AS2000")
    }

}
