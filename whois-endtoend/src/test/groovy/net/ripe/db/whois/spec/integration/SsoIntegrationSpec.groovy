package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.common.rpsl.AttributeType
import net.ripe.db.whois.common.rpsl.ObjectType
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class SsoIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return ["TEST-PN": """\
                    person: some one
                    nic-hdl: TEST-PN
                    source: TEST
                """,
                "TEST-MNT3": """\
                    mntner: TEST-MNT3
                    descr: description
                    admin-c: TEST-PN
                    mnt-by: TEST-MNT3
                    mnt-nfy: nfy@ripe.net
                    upd-to: dbtest@ripe.net
                    auth:   MD5-PW \$1\$dNvmHMUm\$5A3Q0AlFopJ662JB2FY/w. # update3
                    source: TEST
                """]
    }

    def setupSpec() {
        resetTime()
    }

    def "create sso mntner stores uuid in db, shows username in ack, filters auth in query"() {
      given:
        databaseHelper.updateObject("" +
                "person: some one\n" +
                "nic-hdl: TEST-PN\n" +
                "mnt-by: TEST-MNT3\n" +
                "source: TEST")
        def response = syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            mnt-by: TEST-MNT3
                            source: TEST
                            password: update3
                            """.stripIndent(true)))
      expect:
        response =~ /SUCCESS/

      when:
        def ssoObjectInfo = whoisFixture.getRpslObjectDao().findByAttribute(AttributeType.AUTH, "SSO 906635c2-0405-429a-800b-0602bd716124")

      then:
        ssoObjectInfo.size() == 1
        ssoObjectInfo.get(0).getKey().toString() == "SSO-MNT"

      when:
        def mntner = databaseHelper.lookupObject(ObjectType.MNTNER, "SSO-MNT")

      then:
        def currentDateTime = getTimeUtcString()
        mntner.toString().equals(String.format(
                "mntner:         SSO-MNT\n" +
                "descr:          sso mntner\n" +
                "admin-c:        TEST-PN\n" +
                "upd-to:         test@ripe.net\n" +
                "auth:           SSO 906635c2-0405-429a-800b-0602bd716124\n" +
                "auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "mnt-by:         TEST-MNT3\n" +
                "created:        %s\n" +
                "last-modified:  %s\n" +
                "source:         TEST\n", currentDateTime, currentDateTime))

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
mnt-by:         TEST-MNT3
created:        ${currentDateTime}
last-modified:  ${currentDateTime}
source:         TEST # Filtered/
    }

    def "update sso mntner stores uuid in db, shows username in ack, filters auth in query"() {
      given:
        databaseHelper.updateObject("" +
                "person: some one\n" +
                "nic-hdl: TEST-PN\n" +
                "mnt-by: TEST-MNT3\n" +
                "source: TEST")

        syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            mnt-by: TEST-MNT3
                            source: TEST
                            password: update3
                            """.stripIndent(true)))

        notificationFor("nfy@ripe.net")

        def response = syncUpdate(new SyncUpdate(data: """\
                            mntner: SSO-MNT
                            descr: updated sso mntner
                            admin-c: TEST-PN
                            upd-to: test@ripe.net
                            auth: SSO person@net.net
                            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                            mnt-by: TEST-MNT3
                            source: TEST
                            password: update3
                            """.stripIndent(true)))
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
mnt-by:         TEST-MNT3
created:        \d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z
last-modified:  \d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z
source:         TEST # Filtered/
    }
}
