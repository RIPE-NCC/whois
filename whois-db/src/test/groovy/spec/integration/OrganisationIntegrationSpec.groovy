package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class OrganisationIntegrationSpec extends BaseSpec {

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

    def "create organisation"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
    }


    def "update organisation"() {
      given:
        def update = new SyncUpdate(data: fixtures["ORG1"].stripIndent() + "password:update")

      when:
        def result = syncUpdate update

      then:
        result =~ /SUCCESS/
    }

    def "delete organisation"() {
      given:
        def delete = new SyncUpdate(data: fixtures["ORG1"].stripIndent() + "delete: true\npassword:update")

      when:
        def result = syncUpdate delete

      then:
        result =~ /SUCCESS/
    }

    def "incorrect organisation attribute fail"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "password:update"
        data = (data =~ /organisation: ORG-TOL1-TEST/).replaceFirst("organisation: ORG-something")

        def incorrect = new SyncUpdate(data: data)
      when:
        def result = syncUpdate incorrect

      then:
        result =~ /FAIL/
        result =~ /Syntax error in ORG-something/
    }

    def "incorrect orgtype fail"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "password:update"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: WRONG")
        def incorrect = new SyncUpdate(data: data)

      when:
        def result = syncUpdate incorrect

      then:
        result =~ /FAILED: \[organisation\] ORG-TOL1-TEST/
        result =~ /Syntax error in WRONG/
    }

    def "orgtype IANA (and others) requires mnt-by from rip.config"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "password:update"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result =~ /FAIL/
        result =~ /Error:   This org-type value can only be set by administrative mntners/
    }

    def "orgtype LIR (and others) passes when mnt-by from rip.config in create"() {
      given:
        def data = """\
            organisation: AUTO-1
            org-name:     L I R
            org-type:     LIR
            descr:        test org
            address:      street 5
            e-mail:       org1@test.com
            mnt-ref:      TST-MNT
            mnt-by:       RIPE-NCC-HM-MNT
            changed:      dbtest@ripe.net 20120505
            source:       TEST
            password:     update
            """.stripIndent()

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result =~ /SUCCESS/
    }

    def "orgtype LIR (and others) fails when mnt-by from rip.config only in update"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "password:update"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: LIR")
        data = (data =~ /mnt-by:       TST-MNT/).replaceFirst("mnt-by:RIPE-NCC-HM-MNT")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result =~ /\*\*\*Error:   This org-type value can only be set by administrative mntners/
    }

    def "other mntners than powermaintainers fail update"() {
      given:
        def update = new SyncUpdate(data: "organisation: ORG-TOL1-TEST\n" +
                "org-name:     Test Organisation Ltd\n" +
                "org-type: LIR\n" +
                "descr:        test org\n" +
                "address:      street 5\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      TST-MNT\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TST-MNT2\n" +
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       TEST\n" +
                "password:update".stripIndent())

      when:
        def result = syncUpdate update

      then:
        result =~ /\*\*\*Error:   This org-type value can only be set by administrative mntners/
    }

    def "other mntners than powermaintainers fail create"() {
      given:
        def update = new SyncUpdate(data: "organisation: AUTO-1\n" +
                "org-name:     Other Organisation Ltd\n" +
                "org-type: LIR\n" +
                "descr:        test org\n" +
                "address:      street 5\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      TST-MNT\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TST-MNT2\n" +
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       TEST\n" +
                "password:update".stripIndent())

      when:
        def result = syncUpdate update

      then:
        result =~ /\*\*\*Error:   This org-type value can only be set by administrative mntners/
    }

    def "create organisation no mntner"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            source:       TEST
              """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        println(response)
        response =~ /Create FAILED: \[organisation\] AUTO-1/
        response =~ /\\*\\*\\*Error:   Mandatory attribute \"mnt-by\" is missing/
        response =~ /\\*\\*\\*Error:   Mandatory attribute \"mnt-ref\" is missing/
    }

    def "create organisation incorrect mntner password"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: invalid
              """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-RNO1-TEST failed
                        using "mnt-by:"
                        not authenticated by: TST-MNT
            """.stripIndent()
    }

    def "create organisation with non-existent org attribute"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            org:          ORG-NON1-EXISTENT
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        response =~ /\\*\\*\\*Error:   Unknown object referenced ORG-NON1-EXISTENT/
    }

    def "create organisation with org attribute with invalid mnt-ref attribute"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            org:          ORG-TOL2-TEST
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent())

      when:
        deleteObject("TST-MNT2")
        def response = syncUpdate org

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        response =~ "Warning: Referenced organisation ORG-TOL2-TEST has mnt-ref attribute TST-MNT2\n            which does not exist in the database"
    }

    def "create organisation not authorised to add org attribute"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            org:          ORG-TOL1-TEST
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: invalid
              """.stripIndent())

      when:
        def response = syncUpdate org

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-TOL1-TEST failed
                        using "mnt-ref:"
                        not authenticated by: TST-MNT
            """.stripIndent()
    }

    def "changed before 1984"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "password:update"
        data = (data =~ /changed:      dbtest@ripe.net 20120505/).replaceFirst("changed:      dbtest@ripe.net 19830505")

        def org = new SyncUpdate(data: data)

      when:
        def response = syncUpdate org

      then:
        response =~ /FAIL/
        response =~ "Error:   Date is older than the database itself in changed: attribute\n            \"19830505\""
    }

    def "create selfreferencing organisation"() {
      given:
        def data = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Tesco
            org-type:     OTHER
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            changed:      admin@test.com 20120505
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            org:    AUTO-1
            password: update
              """.stripIndent())

      when:
        def response = syncUpdate data

      then:
        response =~ /SUCCESS/
        response =~ /ORG-TA1-TEST/
    }

    def "update selfreferencing organisation"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "org:ORG-TOL1-TEST\npassword:update"
        def update = new SyncUpdate(data: data)

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/

    }

    def "delete selfreferencing organisation"() {
      given:
        def update = new SyncUpdate(data: fixtures["ORG1"].stripIndent() + "org:ORG-TOL1-TEST\npassword:update")
        syncUpdate update

        def delete = new SyncUpdate(data: fixtures["ORG1"].stripIndent() + "org:ORG-TOL1-TEST\ndelete:true\npassword:update")

      when:
        def response = syncUpdate delete

      then:
        response =~ /Delete SUCCEEDED: \[organisation\] ORG-TOL1-TEST/
    }

    def "add organisation referencing abuse-c role which has no abuse-mailbox"() {
      given:
        def update = new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                abuse-c:      NAB-NIC
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                changed:      admin@test.com 20120505
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent())
      when:
        def response = syncUpdate update

      then:
        response =~ /FAIL/

        response =~ /The "abuse-c" ROLE object 'NAB-NIC' has no "abuse-mailbox:"
            Add "abuse-mailbox:" to the ROLE object, then update the
            ORGANISATION object/
    }

    def "add organisation referencing nonexisting abuse-c"() {
      given:
        def update = new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                abuse-c:      FAKE-NIC
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                changed:      admin@test.com 20120505
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent())
      when:
        def response = syncUpdate update

      then:
        response =~ /FAIL/
        response =~ /Unknown object referenced FAKE-NIC/
    }

    def "add organisation referencing abuse-c role succeeds"() {
      given:
        def update = new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                abuse-c:      AB-NIC
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                changed:      admin@test.com 20120505
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent())
      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
    }

    def "add organisation where abuse-c references person"() {
      given:
        def update = new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Ripe NCC organisation
                org-type:     OTHER
                abuse-c:      TEST-RIPE
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                changed:      admin@test.com 20120505
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent())
      when:
        def response = syncUpdate update

      then:
        println(response)
        response =~ /FAIL/
        response =~ /"abuse-c:" references a PERSON object/
        response =~ /This must reference a ROLE object with an "abuse-mailbox:"/
    }
}
