package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class MaintainerIntegrationSpec extends BaseWhoisSourceSpec {

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
                fixtures["UPD-MNT"].stripIndent(true) +
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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            delete: some reason
            password: update
            """.stripIndent(true))

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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

      when:
        def response = syncUpdate update

      then:
        response =~ /mntner:         NEW-MNT\n\*\*\*Error:   Reserved name used/
    }

    def "create maintainer with invalid org reference"() {
      given:
        def data = fixtures["ADMIN-MNT"].stripIndent(true) + "org:ORG-ACME-DE\npassword:update"
        def maintainerWithOrg = (data =~ /ADMIN-MNT/).replaceFirst("ORG-MNT")
        def update = new SyncUpdate(data: maintainerWithOrg)

      when:
        def response = syncUpdate update

      then:
        response =~ /Error:   Unknown object referenced ORG-ACME-DE/
    }

    def "create maintainer with sso authentication"() {
      when:
        def response = syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """)
      then:
        response =~ /Create SUCCEEDED: \[mntner\] SSO-MNT/
    }

    def "modify maintainer with sso authentication"() {
      when:
        syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """)

        def response = syncUpdate new SyncUpdate(data: """\
            mntner: SSO-MNT
            descr: update
            admin-c: TEST-RIPE
            mnt-by: SSO-MNT
            upd-to: dbtest@ripe.net
            auth: SSO person@net.net
            auth: MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true)))

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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true)))

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
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true)))

      then:
        ! (update =~ /To create the first person\/mntner pair of objects /)
    }

    def "updating mntner with no changes should result in noop"() {
      setup:
        def mntner = """
            mntner:         OTHER-MNT
            descr:          description
            admin-c:        TEST-RIPE
            mnt-by:         OTHER-MNT
            upd-to:         dbtest@ripe.net
            auth:           MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source:         TEST
            password:       update
        """.stripIndent(true)
      when:
        def create = syncUpdate new SyncUpdate(data: mntner)
      then:
        create =~ /Create SUCCEEDED: \[mntner\] OTHER-MNT/
      when:
        // force time change (changes last-modified)
        setTime(getTime().plusSeconds(10))
      then:
        def update =  syncUpdate new SyncUpdate(data: mntner)
      then:
        update =~ /No operation: \[mntner\] OTHER-MNT/
    }

    def "create maintainer with abuse-mailbox"() {
        given:
        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            abuse-mailbox: abuse@ripe.net
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /"abuse-mailbox" is not valid for this object type/
    }

    def "modify maintainer, add abuse-mailbox"() {
        given:
        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n" +
                "password: update")

        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            abuse-mailbox: abuse2@ripe.net
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /"abuse-mailbox" is not valid for this object type/
    }

    def "modify maintainer, remove abuse-mailbox"() {
        given:
        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n" +
                "password: update")

        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /Modify SUCCEEDED: \[mntner] DEV-MNT/
    }


    def "create succeeds self-referenced maintainer correct passwd"() {
        given:
        def create = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            mnt-ref: DEV-MNT
            password: update
            password: emptypassword
            """.stripIndent(true))

        when:
        def response = syncUpdate create

        then:
        response =~ /Create SUCCEEDED: \[mntner\] DEV-MNT/
    }

    def "create succeeds self-referenced maintainer incorrect passwd"() {
        given:
        def create = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            mnt-ref: DEV-MNT
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate create

        then:
        response =~ /Create SUCCEEDED: \[mntner\] DEV-MNT/
    }

    def "create succeeds referencing another mntner correct passwd"() {
        given:

        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n" +
                "password: update")

        def create = new SyncUpdate(data: """\
            mntner: TEST-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: TEST-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            mnt-ref: DEV-MNT
            password: emptypassword
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate create

        then:
        response =~ /Create SUCCEEDED: \[mntner\] TEST-MNT/
    }

    def "update succeeds self-referenced maintainer correct passwd"() {
        given:
        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n" +
                "password: update")

        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            mnt-ref: DEV-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /Modify SUCCEEDED: \[mntner] DEV-MNT/
    }

    def "create fails referencing another mntner incorrect passwd"() {
        given:

        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n" +
                "password: update")

        def create = new SyncUpdate(data: """\
            mntner: TEST-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: TEST-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            mnt-ref: DEV-MNT
            password: emptypassword
            """.stripIndent(true))

        when:
        def response = syncUpdate create

        then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[mntner\\] TEST-MNT failed
                        using "mnt-ref:"
                        not authenticated by: DEV-MNT""".stripIndent(true)
    }

    def "update fails self-referenced maintainer incorrect passwd"() {
        given:
        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword\n" +
                "source: TEST\n" +
                "password: emptypassword\n" +
                "password: update")

        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            mnt-ref: DEV-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            password: update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[mntner\\] DEV-MNT failed
                        using "mnt-ref:"
                        not authenticated by: DEV-MNT""".stripIndent(true)
    }

    def "update succeeds self-referenced maintainer incorrect passwd override"() {
        given:
        databaseHelper.addObject("mntner: DEV-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "abuse-mailbox: abuse2@ripe.net\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword\n" +
                "source: TEST\n" +
                "password: emptypassword\n" +
                "password: update")

        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            mnt-ref: DEV-MNT
            upd-to: dbtest@ripe.net
            auth:    MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
            source: TEST
            override:     denis,override1
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /Modify SUCCEEDED: \[mntner] DEV-MNT/
    }
}
