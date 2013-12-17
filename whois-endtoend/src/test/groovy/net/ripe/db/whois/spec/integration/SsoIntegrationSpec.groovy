package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class SsoIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return ["TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    changed: ripe@test.net
                    source: TEST
                """,
                "TEST-MNT3": """\
                    mntner: TEST-MNT3
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT3
                    mnt-nfy: nfy@ripe.net
                    referral-by: TEST-MNT3
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$dNvmHMUm\$5A3Q0AlFopJ662JB2FY/w. # update3
                    changed: dbtest@ripe.net 20120707
                    source: TEST
                """]
    }

    def "create sso mntner stores uuid in db, shows username in ack, filters auth in query"() {
      given:
        databaseHelper.updateObject("" +
                "person: some one\n" +
                "nic-hdl: TEST-PN\n" +
                "changed: ripe@test.net\n" +
                "mnt-by: TEST-MNT3\n" +
                "source: TEST")
        def response = syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            referral-by: TEST-MNT3
                            mnt-by: TEST-MNT3
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update3
                            """.stripIndent()))
      expect:
        response =~ /SUCCESS/

      when:
        def mntner = databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-MNT")

      then:
        mntner.toString() ==
                "mntner:         SSO-MNT\n" +
                "descr:          sso mntner\n" +
                "admin-c:        TEST-PN\n" +
                "upd-to:         test@ripe.net\n" +
                "auth:           SSO 906635c2-0405-429a-800b-0602bd716124\n" +
                "auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "referral-by:    TEST-MNT3\n" +
                "mnt-by:         TEST-MNT3\n" +
                "changed:        ripe@test.net 20091015\n" +
                "source:         TEST\n"

      when:
        def query = query("SSO-MNT")

      then:
        query =~/auth:           SSO # Filtered/

      when:
        def ack = ackFor("nfy@ripe.net")

      then:
        ack =~ /mntner:         SSO-MNT
descr:          sso mntner
admin-c:        TEST-PN
upd-to:         test@ripe.net
auth:           SSO # Filtered
auth:           MD5-PW # Filtered
referral-by:    TEST-MNT3
mnt-by:         TEST-MNT3
changed:        ripe@test.net 20091015
source:         TEST # Filtered/
    }

    def "update sso mntner stores uuid in db, shows username in ack, filters auth in query"() {
      given:
        databaseHelper.updateObject("" +
                "person: some one\n" +
                "nic-hdl: TEST-PN\n" +
                "changed: ripe@test.net\n" +
                "mnt-by: TEST-MNT3\n" +
                "source: TEST")

        syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            referral-by: TEST-MNT3
                            mnt-by: TEST-MNT3
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update3
                            """.stripIndent()))

        def response = syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: updated sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            referral-by: TEST-MNT3
                            mnt-by: TEST-MNT3
                            changed: ripe@test.net 20091015
                            source: TEST
                            password: update3
                            """.stripIndent()))
      expect:
        response =~ /SUCCESS/

      when:
        def mntner = restLookup(ObjectType.MNTNER, "SSO-MNT", "update");

      then:
        hasAttribute(mntner, "auth", "SSO person@net.net", null);
        hasAttribute(mntner, "auth", "MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7.", "update");

      when:
        def query = query("SSO-MNT")

      then:
        query =~/auth:           SSO # Filtered/

      when:
        def notif = notificationFor("nfy@ripe.net")

      then:
        notif =~ /mntner:         SSO-MNT
descr:          updated sso mntner
admin-c:        TEST-PN
upd-to:         test@ripe.net
auth:           SSO # Filtered
auth:           MD5-PW # Filtered
referral-by:    TEST-MNT3
mnt-by:         TEST-MNT3
changed:        ripe@test.net 20091015
source:         TEST # Filtered/
    }
}
