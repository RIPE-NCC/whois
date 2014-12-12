package net.ripe.db.whois.spec.update

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.BaseQueryUpdateSpec
import net.ripe.db.whois.spec.domain.Message

@org.junit.experimental.categories.Category(IntegrationTest.class)
class AckSpec extends BaseQueryUpdateSpec {

    @Override
    Map<String, String> getTransients() {
        [
        "DEL-MNT": """\
            mntner:      DEL-MNT
            descr:       MNTNER for test
            admin-c:     TP1-TEST
            upd-to:      dbtest@ripe.net
            auth:        MD5-PW \$1\$T6B4LEdb\$5IeIbPNcRJ35P1tNoXFas/  #delete
            mnt-by:      DEL-MNT
            referral-by: DEL-MNT
            changed:     dbtest@ripe.net 20120202
            source:      TEST
            """
    ]}

    // Check basic structure of a mail ack message
    def "ack mail msg structure"() {
      given:
        def toDelete = dbfixture(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete MNTNER DEL-MNT",
                body: toDelete + "delete: testing\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: delete MNTNER DEL-MNT"
        ack.contents =~ "(?s)>  From:.+?SUMMARY OF UPDATE:.+?Number of objects found:.+?DETAILED EXPLANATION:.+?~~~~\nThe following.+?---\nDelete SUCCEEDED:.+?~~~~\n+The RIPE Database is subject to Terms and Conditions:"
    }

    // Check basic structure of a sync ack message
    def "ack sync msg structure"() {
      when:
        def result = syncUpdate(getTransient("DEL-MNT") + "password: owner")
      then:
        result =~ "(?s)- From-Host:.+?" +
                  "SUMMARY OF UPDATE:.+?" +
                  "Number of objects found:.+?" +
                  "DETAILED EXPLANATION:.+?" +
                  "~~~~\nThe following.+?" +
                  "---\nCreate FAILED:.+?" +
                  "~~~~\n+The RIPE Database is subject to Terms and Conditions:"
    }

    // Check the ack message summary structure
    def "ack msg summary structure"() {
      given:

        def toDelete = dbfixture(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete MNTNER DEL-MNT",
                body: toDelete + "delete: testing\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: delete MNTNER DEL-MNT"
        ack.contents =~ "SUMMARY OF UPDATE:\n\nNumber of objects found:\\s*1\nNumber of objects processed successfully:\\s*1\n" \
                              + "\\s*Create:\\s*0\n\\s*Modify:\\s*0\n\\s*Delete:\\s*1\n\\s*No Operation:\\s*0\n" \
                              + "Number of objects processed with errors:\\s*0\n" \
                              + "\\s*Create:\\s*0\n\\s*Modify:\\s*0\n\\s*Delete:\\s*0\n"
    }

    // Check the ack message SUCCESS structure
    def "ack msg success structure"() {
      given:
        def toDelete = dbfixture(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete MNTNER DEL-MNT",
                body: toDelete + "delete: testing\npassword: delete"
        )

      then:
        def ack = ackFor message
        ack.subject == "SUCCESS: delete MNTNER DEL-MNT"
        ack.contents =~ "(?s)~~~~\nThe following object\\(s\\) were processed SUCCESSFULLY:\n\n---\nDelete SUCCEEDED: \\[mntner\\] DEL-MNT\n*~+~~~~\n"
    }

    // Check the ack message FAILED structure
    def "ack msg failed structure"() {
      given:
        def toDelete = dbfixture(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete MNTNER DEL-MNT",
                body: toDelete + "delete: testing\npassword: fred"
        )

      then:
        def ack = ackFor message
        ack.subject == "FAILED: delete MNTNER DEL-MNT"
        ack.contents =~ "(?s)~~~~\nThe following object\\(s\\) were found to have ERRORS:\n\n---\nDelete FAILED: \\[mntner\\] DEL-MNT\\s*mntner:\\s*DEL-MNT\n.+?~+~~~~\n"
    }

    // Check the ack message garbage structure
    def "ack msg garbage structure"() {
      given:
        dbfixture(getTransient("DEL-MNT"))

      when:
        def message = send new Message(
                subject: "delete MNTNER DEL-MNT",
                body: "delete: testing\npassword: fred"
        )

      then:
        def ack = ackFor message
        ack.subject == "FAILED: delete MNTNER DEL-MNT"
        ack.contents =~ "(?s)~~~~\nThe following paragraph\\(s\\) do not look like objects\nand were NOT PROCESSED:\n\ndelete: testing\n.+?~+~~~~\n"
    }
}
