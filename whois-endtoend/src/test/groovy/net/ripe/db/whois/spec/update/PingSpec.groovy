package net.ripe.db.whois.spec.update


import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.jupiter.api.Tag("IntegrationTest")
class PingSpec extends BaseQueryUpdateSpec {

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
   ]}

    def "create route6, pingable, no ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")

      when:
          def ack = syncUpdateWithResponse("""
                route6:         2013:600::/32
                descr:          Route6
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       2013:600::
                source:         TEST

                password:   mb-child
                password:   hm
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2013:600::/32AS2000" }

        queryObject("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")
        query_object_matches("-rGBT route6 2013:600::/32", "route6", "2013:600::/32", "pingable")
    }

    def "create route6, no pingable, ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")

      when:
          def ack = syncUpdateWithResponse("""
                route6:         2013:600::/32
                descr:          Route6
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                ping-hdl:       TP1-test
                source:         TEST

                password:   mb-child
                password:   hm
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 1, 0, 0, 0)
        ack.summary.assertErrors(0, 0, 0, 0)
        ack.countErrorWarnInfo(0, 1, 0)
        ack.successes.any { it.operation == "Create" && it.key == "[route6] 2013:600::/32AS2000" }

        queryObject("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")
        query_object_matches("-rGBT route6 2013:600::/32", "route6", "2013:600::/32", "TP1-test")
    }

    def "create route, 2 pingable, 2 ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       99.13.0.1
                pingable:       99.13.0.30
                ping-hdl:       TP1-test
                ping-hdl:       TP2-test
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
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16", "TP1-test")
        query_object_matches("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16", "pingable")
    }

    def "create route, 2 pingable one IPv4 and one IPv6, ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       99.13.0.1
                pingable:       2013:600::
                ping-hdl:       TP1-test
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
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS2000") ==
              ["2013:600:: is not a valid IPv4 address"]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create route6, 2 pingable one IPv4 and one IPv6, ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route6:         2013:600::/32
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       99.13.0.1
                pingable:       2013:600::
                ping-hdl:       TP1-test
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
        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2013:600::/32AS2000" }
        ack.errorMessagesFor("Create", "[route6] 2013:600::/32AS2000") =~
              ["99.13.0.1 is not a valid IPv6 address"]

        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")
    }

    def "create route6, pingable address outside prefix"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")

      when:
          def ack = syncUpdateWithResponse("""
                route6:         2013:600::/32
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       2014:600::
                ping-hdl:       TP1-test
                source:         TEST

                password:   mb-child
                password:   hm
                password:   mb-origin
                """.stripIndent(true)
        )

      then:
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(0, 0, 0, 0, 0)
        ack.summary.assertErrors(1, 1, 0, 0)
        ack.countErrorWarnInfo(1, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route6] 2013:600::/32AS2000" }
        ack.errorMessagesFor("Create", "[route6] 2013:600::/32AS2000") ==
              ["2014:600:: is outside the range of this object"]

        queryObjectNotFound("-rGBT route6 2013:600::/32", "route6", "2013:600::/32")
    }

    def "create route, pingable address outside prefix"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       100.13.0.1
                ping-hdl:       TP1-test
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
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS2000") ==
              ["100.13.0.1 is outside the range of this object"]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create route, pingable with nic-hdl, ping-hdl with address"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       TP1-test
                ping-hdl:       99.13.0.1
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
        ack.countErrorWarnInfo(2, 1, 0)
        ack.errors.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }
        ack.errorMessagesFor("Create", "[route] 99.13.0.0/16AS2000") ==
              ["Syntax error in TP1-test",
                      "Syntax error in 99.13.0.1"
              ]

        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
    }

    def "create route, 2 pingable, 2 ping-hdl, inverse query ping-hdl"() {
      given:

      expect:
        queryObjectNotFound("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")

      when:
        def message = send new Message(
                subject: "",
                body: """\
                route:          99.13.0.0/16
                descr:          Route
                origin:         AS2000
                mnt-by:         CHILD-MB-MNT
                pingable:       99.13.0.1
                pingable:       99.13.0.30
                ping-hdl:       TP1-test
                ping-hdl:       TP2-test
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
        ack.successes.any { it.operation == "Create" && it.key == "[route] 99.13.0.0/16AS2000" }

        queryObject("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16")
        query_object_matches("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16", "TP1-test")
        query_object_matches("-rGBT route 99.13.0.0/16", "route", "99.13.0.0/16", "pingable")
        query_object_matches("-rGBT route -i ping-hdl TP1-test", "route", "99.13.0.0/16", "ping-hdl:")
    }

}
