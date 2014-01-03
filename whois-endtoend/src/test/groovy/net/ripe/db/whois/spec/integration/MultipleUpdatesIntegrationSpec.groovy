package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class MultipleUpdatesIntegrationSpec extends BaseWhoisSourceSpec {

    @Override
    Map<String, String> getFixtures() {
        return [
                "UPD-MNT": """\
            mntner: UPD-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT
            referral-by: UPD-MNT
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
            mnt-by:  UPD-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            """
        ]
    }

    def "create two persons"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person1
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP1-TEST
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP2-TEST
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  2")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
        response.contains("Create SUCCEEDED: [person] TP2-TEST   Test Person2")
    }

    def "create two persons with same AUTO-1"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person1
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  1")
        response.contains("Number of objects processed with errors:   1")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
        response.contains("Create FAILED: [person] AUTO-1   Test Person2")
        response.contains("***Error:   Key AUTO-1 already used (AUTO-nnn must be unique per update message)")
    }

    def "create multiple persons with AUTO-nn atttribute"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person1
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-2
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person3
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-3
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person4
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-4
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   4")
        response.contains("Number of objects processed successfully:  4")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
        response.contains("Create SUCCEEDED: [person] TP2-TEST   Test Person2")
        response.contains("Create SUCCEEDED: [person] TP3-TEST   Test Person3")
        response.contains("Create SUCCEEDED: [person] TP4-TEST   Test Person4")
    }

    def "create persons with gaps in AUTO-nn atttribute"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person1
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-11
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-22
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  2")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
        response.contains("Create SUCCEEDED: [person] TP2-TEST   Test Person2")
    }

    def "create person and maintainer with AUTO-1 references in wrong order"() {
      given:
        def update = new SyncUpdate(data: """\
                mntner: TST-MNT
                descr: description
                admin-c: AUTO-1
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
                changed: dbtest@ripe.net 20120707
                source: TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  2")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person2")
        response.contains("Create SUCCEEDED: [mntner] TST-MNT")
    }

    def "delete and create person in wrong order"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP1-TEST
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                delete:  reason

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP1-TEST
                mnt-by:  UPD-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                password: update
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  2")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person2")
        response.contains("Delete SUCCEEDED: [person] TP1-TEST   Test Person2")
    }

    def "create person use mntner authentication with later created maintainer"() {
      given:
        def update = new SyncUpdate(data: """\
                person:  Test Person1
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  TST-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update
                password: emptypassword
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Number of objects processed successfully:  2")
        response.contains("Create SUCCEEDED: [mntner] TST-MNT")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
    }

    def "create with multiple missing references"() {
      given:
        def update = new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                tech-c:       AUTO-2
                changed:      admin@test.com 20120505
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST

                person:       Test Person1
                address:      UK
                phone:        +44 282 411141
                fax-no:       +44 282 411140
                nic-hdl:      AUTO-2
                mnt-by:       TST-MNT
                changed:      dbtest@ripe.net 20120101
                source:       TEST

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST

                password: update
                password: emptypassword
                """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   3")
        response.contains("Number of objects processed successfully:  3")
        response.contains("Create SUCCEEDED: [mntner] TST-MNT")
        response.contains("Create SUCCEEDED: [person] TP1-TEST   Test Person1")
        response.contains("Create SUCCEEDED: [organisation] ORG-RNO1-TEST")
    }

    def "create with invalid AUTO-nnn reference types"() {
      given:
        def update = new SyncUpdate(data: """\
                mntner: TST-MNT
                descr: description
                admin-c: AUTO-1
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST

                person:       Test Person1
                address:      UK
                phone:        +44 282 411141
                fax-no:       +44 282 411140
                nic-hdl:      AUTO-2
                mnt-by:       UPD-MNT
                changed:      dbtest@ripe.net 20120101
                source:       TEST

                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                tech-c:       AUTO-2
                changed:      admin@test.com 20120505
                mnt-by:       UPD-MNT
                mnt-ref:      UPD-MNT
                source:       TEST

                password: update
                password: emptypassword
                """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   3")
        response.contains("Number of objects processed successfully:  2")
        response.contains("" +
                "mntner:         TST-MNT\n" +
                "descr:          description\n" +
                "admin-c:        AUTO-1\n" +
                "***Error:   Invalid reference to [organisation] ORG-RNO1-TEST")
    }

    def "delete maintaner with person reference removed later"() {
      given:
        def update = new SyncUpdate(data: """\
                password: update
                password: emptypassword

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP2-TEST
                mnt-by:  TST-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST
                delete: only possible when reference from person is removed

                person:  Test Person2
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP2-TEST
                mnt-by:  TST-MNT
                changed: dbtest@ripe.net 20120101
                source:  TEST
                delete: allows removing maintainer
                """.stripIndent())


      when:
        def response = syncUpdate update

      then:
        response.contains("" +
                "Number of objects found:                   4\n" +
                "Number of objects processed successfully:  4\n" +
                "  Create:         2\n" +
                "  Modify:         0\n" +
                "  Delete:         2\n" +
                "  No Operation:   0")

        response.contains("Create SUCCEEDED: [mntner] TST-MNT")
        response.contains("Create SUCCEEDED: [person] TP2-TEST   Test Person2")
        response.contains("Delete SUCCEEDED: [person] TP2-TEST   Test Person2")
        response.contains("Delete SUCCEEDED: [mntner] TST-MNT")
    }

    def "create same maintaner twice"() {
      given:
        def update = new SyncUpdate(data: """\
                password: emptypassword

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST

                mntner: TST-MNT
                descr: description
                admin-c: TEST-RIPE
                mnt-by: TST-MNT
                referral-by: TST-MNT
                upd-to: dbtest@ripe.net
                auth: MD5-PW \$1\$/7f2XnzQ\$p5ddbI7SXq4z4yNrObFS/0 # emptypassword
                changed: dbtest@ripe.net 20120707
                source: TEST
                """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Number of objects found:                   2")
        response.contains("Create SUCCEEDED: [mntner] TST-MNT")
        response.contains("No operation: [mntner] TST-MNT")
    }
}

