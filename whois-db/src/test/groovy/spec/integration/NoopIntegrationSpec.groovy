package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class NoopIntegrationSpec extends BaseSpec {

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
            """
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
}
