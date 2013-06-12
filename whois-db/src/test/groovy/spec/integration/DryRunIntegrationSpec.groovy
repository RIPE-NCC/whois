package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class DryRunIntegrationSpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return [
                "TST-MNT": """\
            mntner:  TST-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT
            referral-by: TST-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "TST-MNT2": """\
            mntner:  TST-MNT2
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT2
            referral-by: TST-MNT2
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \\\$1\\\$fU9ZMQN9\\\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "PWR-MNT": """\
            mntner:  RIPE-NCC-HM-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  RIPE-NCC-HM-MNT
            referral-by: RIPE-NCC-HM-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source:  TEST
            """,
                "ADMIN-PN": """\
            person:  Admin Person
            address: Admin Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: TEST-RIPE
            mnt-by:  TST-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """,
                "ORG1": """\
            organisation: ORG-TOL1-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TST-MNT
            mnt-by:       TST-MNT
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            """,
                "ORG2": """\
            organisation: ORG-TOL2-TEST
            org-name:     Test Organisation Ltd
            org-type:     OTHER
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TST-MNT
            mnt-ref:      TST-MNT2
            mnt-by:       TST-MNT
            mnt-by:       TST-MNT2
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            """,
                "ABUSE-ROLE": """\
            role:    Abuse Me
            address: St James Street
            address: Burnley
            address: UK
            e-mail:  dbtest@ripe.net
            admin-c: TEST-RIPE
            tech-c:  TEST-RIPE
            nic-hdl: AB-NIC
            abuse-mailbox: abuse@test.net
            mnt-by:  TST-MNT2
            changed: dbtest@ripe.net 20121016
            source:  TEST
            """,
                "NOT-ABUSE-ROLE": """\
            role:    Not Abused
            address: St James Street
            address: Burnley
            address: UK
            e-mail:  dbtest@ripe.net
            admin-c: TEST-RIPE
            tech-c:  TEST-RIPE
            nic-hdl: NAB-NIC
            mnt-by:  TST-MNT2
            changed: dbtest@ripe.net 20121016
            source:  TEST
            """
        ]
    }

    def "multiple updates with dry run"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation 1
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            organisation: AUTO-2
            org-name:     Ripe NCC organisation 2
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            password:     update
            dry-run:      some reason
            """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        response =~ /FAILED: \[organisation\] AUTO-1/
        response =~ /FAILED: \[organisation\] AUTO-1/
        response =~ /\*\*\*Error:\s*Dry-run is only supported when a single update is specified\n/
    }
}
