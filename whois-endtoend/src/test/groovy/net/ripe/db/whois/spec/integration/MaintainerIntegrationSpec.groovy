package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate
import spock.lang.Ignore

@org.junit.experimental.categories.Category(IntegrationTest.class)
class MaintainerIntegrationSpec extends BaseWhoisSourceSpec {

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

    def "delete non-existent maintainer"() {
      given:
        def update = new SyncUpdate(data: """
                mntner:      DEL-MNT
                source:      TEST
                delete:      reason
                password:    password
                """
        )
      when:
        def response = syncUpdate update

      then:
        response =~ /Object \[mntner\] DEL-MNT does not exist in the database/
    }

    def "delete existing maintainer"() {
      given:
        def update = new SyncUpdate(data: "" +
                fixtures["UPD-MNT"].stripIndent() +
                "delete: some reason\n" +
                "password: update")

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
    }

    def "delete maintainer with references"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: ADMIN-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: ADMIN-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            delete: some reason
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Object \[mntner\] ADMIN-MNT is referenced from other objects/
    }

    def "create maintainer"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Create SUCCEEDED: \[mntner\] DEV-MNT/
    }

    def "create maintainer with invalid name"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: NEW-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /mntner:         NEW-MNT\n\*\*\*Error:   Reserved name used/
    }

    def "create self referencing maintainer"() {
      given:
        def data = fixtures["UPD-MNT"].stripIndent() + "password:update"
        def selfReferringAndNotYetPersisted = ((data =~ /referral-by: ADMIN-MNT/).replaceFirst("referral-by: UPD-MNT") =~ /UPD-MNT/).replaceAll("DE-MNT")
        def selfRef = new SyncUpdate(data: selfReferringAndNotYetPersisted)

      when:
        def response = syncUpdate selfRef

      then:
        response =~ /Create SUCCEEDED: \[mntner\] DE-MNT/
    }

    def "create maintainer with invalid org reference"() {
      given:
        def data = fixtures["ADMIN-MNT"].stripIndent() + "org:ORG-ACME-DE\npassword:update"
        def maintainerWithOrg = (data =~ /ADMIN-MNT/).replaceFirst("ORG-MNT")
        def update = new SyncUpdate(data: maintainerWithOrg)

      when:
        def response = syncUpdate update

      then:
        response =~ /Error:   Unknown object referenced ORG-ACME-DE/
    }

    @Ignore
    def "create maintainer with sso authentication"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """)
      then:
        response =~ /Create SUCCEEDED: \[mntner\] SSO-MNT/
    }

    @Ignore
    def "modify maintainer with sso authentication"() {
      when:
        syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """)

        def response = syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: update
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """)
      then:
        response =~ /Modify SUCCEEDED: \[mntner\] SSO-MNT/
    }

    def "update maintainer"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: UPD-MNT
            descr: updated description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /Modify SUCCEEDED: \[mntner\] UPD-MNT/
    }

    def "create any object gives startup errormessage when referencing nonexisting person/role and maintainer"() {
        when:
            def update = syncUpdate(new SyncUpdate(data: """\
            mntner: UPD-MNT
            descr: updated description
            admin-c: FAKE-RIPE
            mnt-by: FAKE-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent()))

        then:
            update =~ /To create the first person\/mntner pair of objects /
    }

    def "create any object gives no startup errormessage when nonexisting person/role but existing mntner"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
            mntner: UPD-MNT
            descr: updated description
            admin-c: FAKE-RIPE
            mnt-by: UPD-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent()))

      then:
        ! (update =~ /To create the first person\/mntner pair of objects /)
    }

    def "create any object gives no startup errormessage when existing mntner but nonexisting person/role"() {
      when:
        def update = syncUpdate(new SyncUpdate(data: """\
            mntner: UPD-MNT
            descr: updated description
            admin-c: TEST-RIPE
            mnt-by: FAKE-MNT
            referral-by: ADMIN-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent()))

      then:
        ! (update =~ /To create the first person\/mntner pair of objects /)
    }
}
