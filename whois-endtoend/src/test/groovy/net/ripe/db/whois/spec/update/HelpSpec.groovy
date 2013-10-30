package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

class HelpSpec extends BaseQueryUpdateSpec {

    def "send a help message and check the response"() {
      when:
        def message = send new Message(
            subject: "help",
            body: ""
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: help"
        ack.contents =~ """\
            You have requested Help information from the RIPE NCC Database,
            therefore the body of your message has been ignored.
            """.stripIndent()
    }

    def "send a HeLp message and check the response"() {
      when:
        def message = send new Message(
            subject: "HeLp",
            body: ""
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: HeLp"
        ack.contents =~ """\
            You have requested Help information from the RIPE NCC Database,
            therefore the body of your message has been ignored.
            """.stripIndent()
    }

    def "send a howto message and check the response"() {
      when:
        def message = send new Message(
            subject: "howto",
            body: ""
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: howto"
        ack.contents =~ """\
            You have requested Help information from the RIPE NCC Database,
            therefore the body of your message has been ignored.
            """.stripIndent()
    }

    def "send a hOwTo message and check the response"() {
      when:
        def message = send new Message(
            subject: "hOwTo",
            body: ""
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: hOwTo"
        ack.contents =~ """\
            You have requested Help information from the RIPE NCC Database,
            therefore the body of your message has been ignored.
            """.stripIndent()
    }

//    def "send a HeLp and HowTo message and check the response"() {
//        when:
//
//        def message = send new Message(
//                subject: "HeLp HowTo",
//                body: ""
//        )
//
//        then:
//
//        def ack = ackFor message
//
//        ack.subject == "Failed: HeLp HowTo"
//        ack.contents =~ """\
//            Subject: HELP
//            """.stripIndent()
//
//    }
}
