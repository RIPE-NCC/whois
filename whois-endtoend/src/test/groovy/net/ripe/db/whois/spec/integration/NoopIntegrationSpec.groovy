package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class NoopIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "INVALID-MNT": """\
            mntner: INVALID-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: abcdef
            source: TEST
            """,
                "ADMIN-MNT": """\
            mntner: ADMIN-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: ADMIN-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            """,
                "ADMIN-PN": """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: TEST-RIPE
            mnt-by:  ADMIN-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """,
                "OWNER-MNT": """\
            mntner:      OWNER-MNT
            descr:       used to maintain other MNTNERs
            admin-c:     TEST-RIPE
            upd-to:      updto_owner@ripe.net
            mnt-nfy:     mntnfy_owner@ripe.net
            notify:      notify_owner@ripe.net
            auth:        MD5-PW \$1\$fyALLXZB\$V5Cht4.DAIM3vi64EpC0w/  #owner
            mnt-by:      OWNER-MNT
            referral-by: OWNER-MNT
            changed:     dbtest@ripe.net
            source:      TEST
            """,
                "OWNER2-MNT": """\
            mntner:      OWNER2-MNT
            descr:       used to maintain other MNTNERs
            admin-c:     TEST-RIPE
            upd-to:      updto_owner2@ripe.net
            mnt-nfy:     mntnfy_owner2@ripe.net
            notify:      notify_owner2@ripe.net
            auth:        MD5-PW \$1\$9vNwegLB\$SrX4itajapDaACGZaLOIY1  #owner2
            mnt-by:      OWNER2-MNT
            referral-by: OWNER2-MNT
            changed:     dbtest@ripe.net
            source:      TEST
            """,


        ]
    }

    def "update invalid maintainer NOOP"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: INVALID-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: abcdef
            source: TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("" +
                "Modify FAILED: [mntner] INVALID-MNT\n" +
                "\n" +
                "mntner:         INVALID-MNT\n" +
                "descr:          description\n" +
                "admin-c:        TEST-RIPE\n" +
                "mnt-by:         UPD-MNT\n" +
                "referral-by:    ADMIN-MNT\n" +
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:        abcdef\n" +
                "***Error:   Syntax error in abcdef\n" +
                "source:         TEST")
    }

    def "syncupdate NOOP does not send notifications"() {
      given:
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TEST-RIPE
                tech-c:      TEST-RIPE
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER2-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("AS10000", "aut-num", "AS10000")

      when:
        def response = syncUpdate("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TEST-RIPE
                tech-c:      TEST-RIPE
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER2-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                password:   owner
                """.stripIndent())

      then:
        response =~ /Warning: Submitted object identical to database object/
        noMoreMessages()
    }

    def "mailupdates NOOP does not send notifications"() {
      given:
        databaseHelper.addObject("""\
                aut-num:     AS10000
                as-name:     TEST-AS
                descr:       Testing Authorisation code
                admin-c:     TEST-RIPE
                tech-c:      TEST-RIPE
                mnt-by:      OWNER-MNT
                mnt-by:      OWNER2-MNT
                changed:     dbtest@ripe.net
                source:      TEST
                """.stripIndent())
      expect:
        queryObject("AS10000", "aut-num", "AS10000")

      when:
        def message = send "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: update\n" +
                "Message-Id: <220284EA-D739-4453-BBD2-807C87666F23@ripe.net>\n" +
                "User-Agent: Alpine 2.00 (LFD 1167 2008-08-23)\n" +
                "Date: Mon, 20 Aug 2012 11:50:58 +0100\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: TEXT/PLAIN; format=flowed; charset=US-ASCII\n" +
                "\n" +
                "aut-num:     AS10000\n" +
                "as-name:     TEST-AS\n" +
                "descr:       Testing Authorisation code\n" +
                "admin-c:     TEST-RIPE\n" +
                "tech-c:      TEST-RIPE\n" +
                "mnt-by:      OWNER-MNT\n" +
                "mnt-by:      OWNER2-MNT\n" +
                "changed:     dbtest@ripe.net\n" +
                "source:      TEST\n" +
                "password:   owner\n"

      then:
        def ack = ackFor message
        ack.summary.nrFound == 1
        ack.summary.assertSuccess(1, 0, 0, 0, 1)

        // TODO: [ES] why are there two notifications for a NOOP from mailupdates?
        def ownernfy = notificationFor "mntnfy_owner@ripe.net"
        def owner2nfy = notificationFor "mntnfy_owner2@ripe.net"

        noMoreMessages()
    }

}
