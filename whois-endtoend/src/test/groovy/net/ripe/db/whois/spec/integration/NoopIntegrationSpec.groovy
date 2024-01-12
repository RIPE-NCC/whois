package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class NoopIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            """,
                "INVALID-MNT": """\
            mntner: INVALID-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            """,
                "ADMIN-MNT": """\
            mntner: ADMIN-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            notify: dbtest@@ripe.net
            source: TEST
            password: update
            """.stripIndent(true))

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
                "upd-to:         dbtest@ripe.net\n" +
                "auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "notify:         dbtest@@ripe.net\n" +
                "***Error:   Syntax error in dbtest@@ripe.net\n" +
                "source:         TEST")
    }

}
