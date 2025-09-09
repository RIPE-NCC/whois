package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.AckResponse
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class RouteAuthASSpec extends BaseQueryUpdateSpec {

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
            "AS10000-LOW": """\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      ORIGIN-MB-MNT
                mnt-lower:   ORIGIN-ML-MNT
                source:      TEST
                """,
            "AS10000-ROUTES": """\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      ORIGIN-MB-MNT
                source:      TEST
                """,
            "AS10000-LOW-ROUTES": """\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TP1-TEST
                tech-c:      TP1-TEST
                mnt-by:      ORIGIN-MB-MNT
                mnt-lower:   ORIGIN-ML-MNT
                source:      TEST
                """,
            "ROUTE": """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                """,
   ]}

    def "create child route, no origin exists"() {
      given:

      expect:
      queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS9000
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
        ack.countErrorWarnInfo(0, 2, 0)
    }

    def "create child route, no origin exists, override"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = syncUpdate("""\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS9000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                override:   denis,override1

                password:   mb-child
                password:   mb-parent
                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.infoSuccessMessagesFor("Create", "[route] 99.13.0.0/16AS9000") == [
                "Authorisation override used"]

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, no passwords, override"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = syncUpdate("""\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                override:    denis,override1

                """.stripIndent(true)
        )

      then:
        def ack = new AckResponse("", message)

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 0, 1)
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.infoSuccessMessagesFor("Create", "[route] 99.13.0.0/16AS2000") == [
                "Authorisation override used"]

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create child route, origin mb"() {
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

    def "create child route, origin mb ml, mb password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000-LOW") + "password: mb-origin\npassword: hm")
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

    def "create child route, origin mb mr, mr password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000-ROUTES") + "password: mb-origin\npassword: hm")
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
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mr-origin
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

    def "create child route, origin mb ml mr, mr password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000-LOW-ROUTES") + "password: mb-origin\npassword: hm")
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
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mr-origin
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

    def "modify child route, origin mb ml mr, child mb password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE") + "password: mb-origin\npassword: mb-parent\npassword: mb-child")
        queryObject("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                remarks:        just added
                source:         TEST

                password:   mb-child
                password:   mb-parent
                password:   mr-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Modify" && it.key == "[route] 99.13.0.0/16AS10000" }

        query_object_matches("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16", "just added")
    }

    def "modify child route, no origin, child mb password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")

      expect:
        query_object_matches("-r -T route 255.13.0.0/16", "route", "255.13.0.0/16", "AS999000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          255.13.0.0/16
                descr:          Route
                origin:         AS999000
                mnt-by:         EXACT-MB-MNT
                remarks:        just added
                source:         TEST

                password:   mb-exact
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 1, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)

        queryObject("-rGBT route 255.13.0.0/16", "route", "255.13.0.0/16")
    }

    def "delete child route, no origin, child mb password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")

      expect:
        query_object_matches("-r -T route 255.13.0.0/16", "route", "255.13.0.0/16", "AS999000")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:       255.13.0.0/16
                descr:       exact match route object
                origin:      AS999000
                mnt-by:      EXACT-MB-MNT
                source:      TEST
                delete:   no origin

                password:   mb-exact
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 255.13.0.0/16AS999000" }

        queryObjectNotFound("-rGBT route 255.13.0.0/16AS999000", "route", "255.13.0.0/16AS999000")
    }

    def "delete child route, origin mb ml mr, child mb password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000") + "password: mb-origin\npassword: hm")
        queryObject("-r -T aut-num AS10000", "aut-num", "AS10000")
        syncUpdate(getTransient("ROUTE") + "password: mb-origin\npassword: mb-parent\npassword: mb-child")
        queryObject("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete:   not needed

                password:   mb-child
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 1, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 2, 0)
        ack.successes.any { it.operation == "Delete" && it.key == "[route] 99.13.0.0/16AS10000" }

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "delete child route, origin mb ml mr, origin mr password supplied"() {
      given:
        syncUpdate(getTransient("AS0 - AS4294967295") + "override: denis,override1")
        queryObject("-r -T as-block AS0 - AS4294967295", "as-block", "AS0 - AS4294967295")
        syncUpdate(getTransient("AS10000-LOW-ROUTES") + "password: mb-origin\npassword: hm")
        syncUpdate(getTransient("ROUTE") + "password: mr-origin\npassword: mb-parent\npassword: mb-child")
        queryObject("-r -T route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS10000
                mnt-by:         CHILD-MB-MNT
                source:         TEST
                delete:   not needed

                password:   mr-origin
                """.stripIndent(true)
        )

      then:
        def ack = ackFor message

        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 0, 0, 1)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Delete" && it.key == "[route] 99.13.0.0/16AS10000" }
        ack.errorMessagesFor("Delete", "[route] 99.13.0.0/16AS10000") ==
              ["Authorisation for [route] 99.13.0.0/16AS10000 failed using \"mnt-by:\" not authenticated by: CHILD-MB-MNT"]

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

}
