package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.spec.domain.SyncUpdate
import org.junit.jupiter.api.Tag

@Tag("IntegrationTest")
class OrganisationIntegrationSpec extends BaseWhoisSourceSpec {

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
            abuse-c:      AB-NIC
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

    def "create organisation"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     OTHER
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent(true))

      when:
        def response = syncUpdate org

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
    }

    def "blank line in organisation object is converted to a continuation character"() {
        def org = new SyncUpdate(data:
            "organisation: AUTO-1\n" +
            "org-name:     Ripe NCC organisation\n" +
            "org-type:     OTHER\n" +
            "address:      Singel 258\n" +
            "phone: +31-2-12345678\n" +
            " \n" +
            "e-mail:        bitbucket@ripe.net\n" +
            "mnt-by:       TST-MNT\n" +
            "mnt-ref:      TST-MNT\n" +
            "source:       TEST\n" +
            "password: update\n")

      when:
        def response = syncUpdate org

      then:
        response =~ /Create SUCCEEDED: \[organisation\] ORG-RNO1-TEST/
        query("ORG-RNO1-TEST") =~ /organisation/
    }


    def "update organisation"() {
      given:
        def update = new SyncUpdate(data: fixtures["ORG1"].stripIndent(true) + "password:update")

      when:
        def result = syncUpdate update

      then:
        result =~ /SUCCESS/
    }

    def "delete organisation"() {
      given:
        def delete = new SyncUpdate(data: fixtures["ORG1"].stripIndent(true) + "delete: true\npassword:update")

      when:
        def result = syncUpdate delete

      then:
        result =~ /SUCCESS/
    }

    def "incorrect organisation attribute fail"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "password:update"
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
        def data = fixtures["ORG1"].stripIndent(true) + "password:update"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: WRONG")
        def incorrect = new SyncUpdate(data: data)

      when:
        def result = syncUpdate incorrect

      then:
        result =~ /FAILED: \[organisation\] ORG-TOL1-TEST/
        result =~ /Syntax error in WRONG/
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
            source:       TEST
            password:     update
            """.stripIndent(true)

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result =~ /SUCCESS/
    }

    def "create organisation no mntner"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            source:       TEST
              """.stripIndent(true))

      when:
        def response = syncUpdate org

      then:
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
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: invalid
              """.stripIndent(true))

      when:
        def response = syncUpdate org

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-RNO1-TEST failed
                        using "mnt-by:"
                        not authenticated by: TST-MNT
            """.stripIndent(true)
    }

    def "create organisation with non-existent org attribute"() {
        def org = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            org:          ORG-NON1-EXISTENT
            e-mail:        bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent(true))

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
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: update
              """.stripIndent(true))

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
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            password: invalid
              """.stripIndent(true))

      when:
        def response = syncUpdate org

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-TOL1-TEST failed
                        using "mnt-ref:"
                        not authenticated by: TST-MNT
            """.stripIndent(true)
    }

    def "create selfreferencing organisation"() {
      given:
        def data = new SyncUpdate(data: """\
            organisation: AUTO-1
            org-name:     Tesco
            org-type:     OTHER
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-by:       TST-MNT
            mnt-ref:      TST-MNT
            source:       TEST
            org:    AUTO-1
            password: update
              """.stripIndent(true))

      when:
        def response = syncUpdate data

      then:
        response =~ /SUCCESS/
        response =~ /ORG-TA1-TEST/
    }

    def "update selfreferencing organisation"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "org:ORG-TOL1-TEST\npassword:update"
        def update = new SyncUpdate(data: data)

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/

    }

    def "delete selfreferencing organisation"() {
      given:
        def update = new SyncUpdate(data: fixtures["ORG1"].stripIndent(true) + "org:ORG-TOL1-TEST\npassword:update")
        syncUpdate update

        def delete = new SyncUpdate(data: fixtures["ORG1"].stripIndent(true) + "org:ORG-TOL1-TEST\ndelete:true\npassword:update")

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
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))
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
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))
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
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))
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
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /FAIL/
        response =~ /"abuse-c:" references a PERSON object/
        response =~ /This must reference a ROLE object with an "abuse-mailbox:"/
    }

    def "modify organisation, remove abuse-c, LIR"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-RNO-TEST\n" +
                "org-name:     Ripe NCC organisation\n" +
                "org-type:     LIR\n" +
                "abuse-c:      AB-NIC\n" +
                "address:      Singel 258\n" +
                "e-mail:        bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        def update = new SyncUpdate(data: """\
                  organisation: ORG-RNO-TEST
                  org-name:     Ripe NCC organisation
                  org-type:     LIR
                  address:      Singel 258
                  e-mail:        bitbucket@ripe.net
                  mnt-by:       TST-MNT
                  mnt-ref:      TST-MNT
                  source:       TEST
                  password: update
                  """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /Modify FAILED: \[organisation\] ORG-RNO-TEST/
        response =~ "Error:   \"abuse-c:\" cannot be removed from an ORGANISATION object referenced\n            by a resource object"
    }

    def "modify organisation, remove abuse-c, not LIR"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-RNO-TEST\n" +
                "org-name:     Ripe NCC organisation\n" +
                "org-type:     OTHER\n" +
                "abuse-c:      AB-NIC\n" +
                "address:      Singel 258\n" +
                "e-mail:        bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        def update = new SyncUpdate(data: """\
                  organisation: ORG-RNO-TEST
                  org-name:     Ripe NCC organisation
                  org-type:     OTHER
                  address:      Singel 258
                  e-mail:        bitbucket@ripe.net
                  mnt-by:       TST-MNT
                  mnt-ref:      TST-MNT
                  source:       TEST
                  password: update
                  """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-RNO-TEST/
    }

    def "modify organisation, never had abuse-c, LIR"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-RNO-TEST\n" +
                "org-name:     Ripe NCC organisation\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:        bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        def update = new SyncUpdate(data: """\
                  organisation: ORG-RNO-TEST
                  org-name:     Ripe NCC organisation
                  org-type:     LIR
                  remarks:       update
                  address:      Singel 258
                  e-mail:        bitbucket@ripe.net
                  mnt-by:       TST-MNT
                  mnt-ref:      TST-MNT
                  source:       TEST
                  password: update
                  """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-RNO-TEST/
    }

    def "modify organisation, remove abuse-c, not LIR, referenced by resource"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-RNO-TEST\n" +
                "org-name:     Ripe NCC organisation\n" +
                "org-type:     OTHER\n" +
                "abuse-c:      AB-NIC\n" +
                "address:      Singel 258\n" +
                "e-mail:        bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
      databaseHelper.addObject("" +
              "aut-num: AS123\n" +
              "as-name: AS-TEST\n" +
              "org: ORG-RNO-TEST\n" +
              "descr: descr\n" +
              "admin-c: TEST-RIPE\n" +
              "tech-c: TEST-RIPE\n" +
              "mnt-by: TST-MNT\n" +
              "mnt-by: RIPE-NCC-HM-MNT\n" +
              "source: TEST")
        def update = new SyncUpdate(data: """\
                      organisation: ORG-RNO-TEST
                      org-name:     Ripe NCC organisation
                      org-type:     OTHER
                      address:      Singel 258
                      e-mail:        bitbucket@ripe.net
                      mnt-by:       TST-MNT
                      mnt-ref:      TST-MNT
                      source:       TEST
                      password: update
                      """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /Modify FAILED: \[organisation\] ORG-RNO-TEST/
        response =~ "Error:   \"abuse-c:\" cannot be removed from an ORGANISATION object referenced\n            by a resource object"
    }

    def "modify organisation, remove abuse-c, not LIR, referenced by resource, not maintained by rs succeeds"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-RNO-TEST\n" +
                "org-name:     Ripe NCC organisation\n" +
                "org-type:     OTHER\n" +
                "abuse-c:      AB-NIC\n" +
                "address:      Singel 258\n" +
                "e-mail:        bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        databaseHelper.addObject("" +
                "aut-num: AS123\n" +
                "as-name: AS-TEST\n" +
                "org: ORG-RNO-TEST\n" +
                "descr: descr\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "mnt-by: TST-MNT\n" +
                "source: TEST")
        def update = new SyncUpdate(data: """\
                        organisation: ORG-RNO-TEST
                        org-name:     Ripe NCC organisation
                        org-type:     OTHER
                        address:      Singel 258
                        e-mail:        bitbucket@ripe.net
                        mnt-by:       TST-MNT
                        mnt-ref:      TST-MNT
                        source:       TEST
                        password: update
                        """.stripIndent(true))
      when:
        def response = syncUpdate update

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-RNO-TEST/
    }

    def "org attribute added by override any mntner"() {
      given:
        databaseHelper.addObject("" +
                "aut-num: AS123\n" +
                "as-name: asname\n" +
                "descr: descr\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "mnt-by: TST-MNT\n" +
                "source: TEST");

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num: AS123
                as-name: asname2
                descr: descr
                org: ORG-TOL1-TEST
                admin-c: TEST-RIPE
                tech-c: TEST-RIPE
                mnt-by: TST-MNT
                source: TEST
                password: update
                override: denis,override1
                """)
      then:
        response =~ /Modify SUCCEEDED: \[aut-num\] AS123/
    }

    def "org attribute changed by override RS mntner"() {
      given:
        databaseHelper.addObject("" +
                "aut-num: AS123\n" +
                "as-name: asname\n" +
                "descr: descr\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num: AS123
                as-name: asname2
                descr: descr
                org: ORG-TOL1-TEST
                admin-c: TEST-RIPE
                tech-c: TEST-RIPE
                mnt-by: RIPE-NCC-HM-MNT
                source: TEST
                password: update
                override: denis,override1
                """)
      then:
        response =~ /Modify SUCCEEDED: \[aut-num\] AS123/
    }

    def "org attribute changed any mntner not override"() {
      given:
        databaseHelper.addObject("" +
                "aut-num: AS123\n" +
                "as-name: asname\n" +
                "descr: descr\n" +
                "org: ORG-TOL2-TEST\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "mnt-by: TST-MNT\n" +
                "source: TEST");

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num: AS123
                as-name: asname2
                descr: descr
                org: ORG-TOL1-TEST
                admin-c: TEST-RIPE
                tech-c: TEST-RIPE
                mnt-by: TST-MNT
                source: TEST
                password: update
                """)
      then:
        response =~ /Modify SUCCEEDED: \[aut-num\] AS123/
    }

    def "org attribute changed RS mntner not override"() {
      given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$lg/7YFfk\$X6ScFx7wATYpuuh/VNU631 #end\n" +
                "source: TEST");

        databaseHelper.addObject("" +
                "aut-num: AS123\n" +
                "as-name: asname\n" +
                "descr: descr\n" +
                "org: ORG-TOL2-TEST\n" +
                "admin-c: TEST-RIPE\n" +
                "tech-c: TEST-RIPE\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST");

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                aut-num: AS123
                as-name: asname2
                descr: descr
                org: ORG-TOL1-TEST
                admin-c: TEST-RIPE
                tech-c: TEST-RIPE
                mnt-by: RIPE-NCC-END-MNT
                source: TEST
                password: update
                """)
      then:
        response =~ /Modify SUCCEEDED: \[aut-num\] AS123/
    }

    def "org-name changed organisation not ref"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     OTHER
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "org-name changed organisation ref by mntner"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "mntner: REF-MNT\n" +
                "org:    ORG-TO1-TEST\n" +
                "mnt-by: REF-MNT\n" +
                "source: TEST")

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     OTHER
                address:      Singel 258
                e-mail:        bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "org-name changed organisation ref by resource without RSmntner not auth by RS mntner"() {
      given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org:     ORG-TO1-TEST\n" +
                "mnt-by:  TST-MNT\n" +
                "source:  TEST")

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "org-name changed organisation ref by resource with RSmntner auth by RS mntner"() {
      given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$lg/7YFfk\$X6ScFx7wATYpuuh/VNU631 #end\n" +
                "source: TEST");

        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       RIPE-NCC-END-MNT\n" +
                "mnt-ref:      RIPE-NCC-END-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org: ORG-TO1-TEST\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       RIPE-NCC-END-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password:     end
                """.stripIndent(true))

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "org-name changed organisation ref by resource with RSmntner auth by override"() {
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-ref:      RIPE-NCC-HM-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org: ORG-TO1-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST")

      when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                override:   denis,override1
                """.stripIndent(true))

      then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "lir org-name and address changed organisation not ref"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:        bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute \"address:\" can only be changed via the LIR portal./
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "lir org-name changed case sensitive organisation not ref"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     TEST ORG
                org-type:     LIR
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                remarks:      Not a NOOP
                source:       TEST
                password: update
                """.stripIndent(true))
        then:
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "other org-name changed case sensitive organisation not ref"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     TEST ORG
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                remarks:      Not a NOOP
                source:       TEST
                password: update
                """.stripIndent(true))
        then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "lir org-name and address changed organisation ref by mntner"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "mntner: REF-MNT\n" +
                "org:    ORG-TO1-TEST\n" +
                "mnt-by: REF-MNT\n" +
                "source: TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:        bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute \"address:\" can only be changed via the LIR portal./
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "lir org-name changed case sensitive organisation ref by mntner"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "mntner: REF-MNT\n" +
                "org:    ORG-TO1-TEST\n" +
                "mnt-by: REF-MNT\n" +
                "source: TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     TEST ORG
                org-type:     LIR
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                remarks:      Not a NOOP
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "lir org-name and address changed organisation ref by resource without RSmntner not auth by RS mntner"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org:     ORG-TO1-TEST\n" +
                "mnt-by:  TST-MNT\n" +
                "source:  TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute \"address:\" can only be changed via the LIR portal./
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "lir org-name and address changed organisation ref by resource with RSmntner auth by RS mntner"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$lg/7YFfk\$X6ScFx7wATYpuuh/VNU631 #end\n" +
                "source: TEST");

        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       RIPE-NCC-END-MNT\n" +
                "mnt-ref:      RIPE-NCC-END-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org: ORG-TO1-TEST\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       RIPE-NCC-END-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password:     end
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute \"address:\" can only be changed via the LIR portal./
        response =~ /\\*\\*\\*Error:   Attribute \"org-name:\" can only be changed via the LIR portal./
    }

    def "other org-name changed organisation ref by resource with RSmntner auth by RS mntner"() {
        given:
        databaseHelper.addObject("" +
                "mntner: RIPE-NCC-END-MNT\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "auth: MD5-PW \$1\$lg/7YFfk\$X6ScFx7wATYpuuh/VNU631 #end\n" +
                "source: TEST");

        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     OTHER\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       RIPE-NCC-END-MNT\n" +
                "mnt-ref:      RIPE-NCC-END-MNT\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org: ORG-TO1-TEST\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "source: TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     TEST ORG
                org-type:     OTHER
                address:      Singel 258
                e-mail:       bitbucket@ripe.net
                mnt-by:       RIPE-NCC-END-MNT
                mnt-ref:      RIPE-NCC-END-MNT
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                remarks:      Not a NOOP
                source:       TEST
                password:     update
                """.stripIndent(true))

        then:
        response =~ /\\*\\*\\*Error:   Attribute "org-name:" can only be changed by the RIPE NCC for this
            object.
            Please contact \"ncc@ripe.net\" to change it/
    }

    def "lir org-name and address changed organisation ref by resource with RSmntner auth by override"() {
        databaseHelper.addObject("" +
                "organisation: ORG-TO1-TEST\n" +
                "org-name:     Test Org\n" +
                "org-type:     LIR\n" +
                "address:      Singel 258\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-ref:      RIPE-NCC-HM-MNT\n" +
                "source:       TEST")

        databaseHelper.addObject("" +
                "aut-num: AS1234\n" +
                "org: ORG-TO1-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST")

        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-TO1-TEST
                org-name:     Updated Org
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                override:   denis,override1
                """.stripIndent(true))

        then:
        response =~ /Modify SUCCEEDED: \[organisation\] ORG-TO1-TEST/
    }

    def "inconsistent org-name not allowed on create OTHER organisation"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Inconsistent  Org\tName
                org-type:     OTHER
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /Create FAILED: \[organisation\] AUTO-1/
        response =~ """
            \\*\\*\\*Error:   Tab characters, multiple lines, or multiple whitespaces are not
                        allowed in the "org-name:" value.
            """.stripIndent(true)
    }

    def "inconsistent org-name not allowed on create LIR organisation"() {
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: AUTO-1
                org-name:     Inconsistent  Org\tName
                org-type:     LIR
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                override:   denis,override1
                """.stripIndent(true))

        then:
        response =~ /Create FAILED: \[organisation\] AUTO-1/
        response =~ """
            \\*\\*\\*Error:   Tab characters, multiple lines, or multiple whitespaces are not
                        allowed in the "org-name:" value.
            """.stripIndent(true)
    }

    def "inconsistent org-name not allowed on modify OTHER organisation"() {
        given:
        databaseHelper.addObject("" +
                "organisation: ORG-ION1-TEST\n" +
                "org-name:     Inconsistent Org Name\n" +
                "org-type:     OTHER\n" +
                "address:      Stationsplein 11\n" +
                "e-mail:       bitbucket@ripe.net\n" +
                "mnt-by:       TST-MNT\n" +
                "mnt-ref:      TST-MNT\n" +
                "source:       TEST")
        when:
        def response = syncUpdate new SyncUpdate(data: """\
                organisation: ORG-ION1-TEST
                org-name:     Inconsistent  Org\tName
                remarks:      Updated # only changing org-name: formatting is a NOOP
                org-type:     OTHER
                address:      Stationsplein 11
                e-mail:       bitbucket@ripe.net
                mnt-by:       TST-MNT
                mnt-ref:      TST-MNT
                source:       TEST
                password: update
                """.stripIndent(true))

        then:
        response =~ /Modify FAILED: \[organisation\] ORG-ION1-TEST/
        response =~ """
            \\*\\*\\*Error:   Tab characters, multiple lines, or multiple whitespaces are not
                        allowed in the "org-name:" value.
            """.stripIndent(true)
    }

}
