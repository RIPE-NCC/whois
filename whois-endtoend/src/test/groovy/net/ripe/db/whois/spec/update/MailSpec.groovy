package net.ripe.db.whois.spec.update

import net.ripe.db.whois.spec.BaseSpec
import net.ripe.db.whois.spec.BasicFixtures
import spec.domain.Message

class MailSpec extends BaseSpec {

    @Override
    Map<String, String> getBasicFixtures() {
        return BasicFixtures.permanentFixtures
    }

    @Override
    Map<String, String> getFixtures() {
        [
                "DEL-MNT": """\
                mntner:      DEL-MNT
                descr:       MNTNER for test
                admin-c:     TP1-TEST
                upd-to:      dbtest@ripe.net
                auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
                mnt-by:      DEL-MNT
                referral-by: DEL-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """
        ]
    }

    def "all keywords invalid"() {
      given:
        def toDelete = getFixture("DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete this MNTNER DEL-MNT",
                body: toDelete + "\ndelete: testing invalid keywords\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: delete this MNTNER DEL-MNT"
        ack.contents =~ "\\*\\*\\*Warning: Invalid keyword\\(s\\) found: delete this MNTNER DEL-MNT"
        ack.contents =~ "\\*\\*\\*Warning: All keywords were ignored"
    }

    def "mixed keywords valid/invalid"() {
      given:
        def toDelete = getFixture("DEL-MNT")

      when:
        def message = send new Message(
                subject: "howto delete this new MNTNER DEL-MNT",
                body: toDelete + "\ndelete: testing invalid keywords\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: howto delete this new MNTNER DEL-MNT"
        ack.contents =~ "\\*\\*\\*Warning: Invalid keyword\\(s\\) found: howto delete this new MNTNER DEL-MNT"
        ack.contents =~ "\\*\\*\\*Warning: All keywords were ignored"
    }

    def "sender email header in ack msg"() {
      given:
        def toDelete = getFixture("DEL-MNT")

      when:
        def message = send new Message(
                subject: "delete DEL-MNT",
                body: toDelete + "\ndelete: testing invalid keywords\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: delete DEL-MNT"
        ack.contents =~ ">  From:.+?\n" \
                              + ">  Subject:    delete DEL-MNT\n" \
                              + ">  Date:.+?\n" \
                              + ">  Reply-To:.+?@ripe.net\n" \
                              + ">  Message-ID: <.+?>"
    }
}
