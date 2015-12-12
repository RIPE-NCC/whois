package net.ripe.db.whois.spec.integration
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

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
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:  TEST
            """,
                "TST-MNT2": """\
            mntner:  TST-MNT2
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT2
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \\\$1\\\$fU9ZMQN9\\\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:  TEST
            """,
                "PWR-MNT": """\
            mntner:  RIPE-NCC-HM-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  RIPE-NCC-HM-MNT
            upd-to:  dbtest@ripe.net
            auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
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
            source:  TEST
            """
        ]
    }

    def "dry run create organisation with AUTO key"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            password:     update
            dry-run:      some reason
            """.stripIndent())

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        response =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/
        queryNothing("ORG-RNO1-TEST")
        noMoreMessages()

      when:
        def response2 = syncUpdate new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            password:     update
            dry-run:      some reason
            """.stripIndent())

      then:
        response2 =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        response2 =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/
        queryNothing("ORG-RNO1-TEST")
        noMoreMessages()
    }

    def "create organisation, update with dry run"() {
      when:
        def create = syncUpdate new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password:     update
            """.stripIndent())

      then:
        create =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        queryObject("ORG-RNO1-TEST", "organisation", "ORG-RNO1-TEST")

      when:
        def modify = syncUpdate new SyncUpdate(data: """\
            organisation: ORG-RNO1-TEST
            org-name:     Updated name
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            password:     update
            dry-run:      some reason
            """.stripIndent())

      then:
        modify =~ /Modify SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        modify =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/

        modify.contains("""\
            @@ -1,3 +1,3 @@
             organisation:   ORG-RNO1-TEST
            -org-name:       Ripe NCC organisation
            +org-name:       Updated name
             org-type:       OTHER
            """.stripIndent())

        query_object_matches("ORG-RNO1-TEST", "organisation", "ORG-RNO1-TEST", "Ripe NCC organisation")
        noMoreMessages()
    }

    def "dry run other mntners than powermaintainers"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Other Organisation Ltd
                org-type:     LIR
                descr:        test org
                address:      street 5
                e-mail:       org1@test.com
                mnt-ref:      TST-MNT
                mnt-by:       RIPE-NCC-HM-MNT
                mnt-by:       TST-MNT2
                source:       TEST
                password:     update
                dry-run:""".stripIndent())

      then:
        response =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/
    }

    def "dry run incorrect password"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password:     invalid
            dry-run:
            """.stripIndent())

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-RNO1-TEST failed
                        using "mnt-by:"
                        not authenticated by: TST-MNT
            """.stripIndent()

        response =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/
    }

    def "dry run delete organisation"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST

            password:     update
            """.stripIndent())

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        queryObject("ORG-RNO1-TEST", "organisation", "ORG-RNO1-TEST")
        noMoreMessages()

      when:
        def delete = syncUpdate new SyncUpdate(data: """\
            organisation: ORG-RNO1-TEST
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:       bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            delete:       dry run

            password:     update
            dry-run:      some reason
            """.stripIndent())

      then:
        delete =~ /Delete SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        delete =~ /\*\*\*Info:    Dry-run performed, no changes to the database have been made/
        queryObject("ORG-RNO1-TEST", "organisation", "ORG-RNO1-TEST")
        noMoreMessages()
    }
}
