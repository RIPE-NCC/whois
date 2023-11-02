package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class OverrideIntegrationSpec extends BaseWhoisSourceSpec {

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
            descr:        test org #comment
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
            """
        ]
    }

    def "override with mail update"() {
      when:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def message = send new Message(body: data)
        def ack = ackFor message

      then:
        ack.contents.contains("***Error:   Override not allowed in email update")
    }

    def "override outside ripe range"() {
      when:
        setRipeRanges();

        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)
        def result = syncUpdate update

      then:
        result.contains("***Error:   Override only allowed by database administrators")
    }

    def "override"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("" +
                "Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "***Warning: Value 'IANA' can only be set by the RIPE NCC for this organisation.\n" +
              "\n" +
                "***Info:    Authorisation override used")
    }

    def "override option invalid id"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:user,pw,{oid=11111}"

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("Create FAILED: [organisation] ORG-TOL1-TEST")
        result.contains("***Error:   Original object with id 11111 specified in override not found")
    }

    def "override not on update"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "\n\noverride:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update
      then:
        result.contains("" +
                "***Warning: An override password was found not attached to an object and was\n" +
                "            ignored")

        result.contains("***Error:   Value 'IANA' can only be set by the RIPE NCC for this organisation.");
    }

    def "override wrong password"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:oops"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("***Error:   Override authentication failed")
        result.contains("Modify FAILED: [organisation] ORG-TOL1-TEST");
    }

    def "override wrong password fails overriding business rules"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:agoston,oops\npassword: update\n"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        result.contains("***Error:   Override authentication failed")
        result.contains("Modify FAILED: [organisation] ORG-TOL1-TEST");
    }

    def "override with spaces"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override: denis, override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("" +
                "Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "***Warning: Value 'IANA' can only be set by the RIPE NCC for this organisation.\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }


    def "override specified multiple times"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1\noverride:denis2,override2"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("***Error:   Multiple override passwords used")
    }

    def "override no password"() {
      given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("***Error:   Override authentication failed")
    }

    def "create maintainer with correct password but wrong override"() {
      given:
        def update = new SyncUpdate(data: """\
            mntner: DEV-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: TST-MNT
            override: SOME WRONG PASSWORD
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            source: TEST
            password: update
            """.stripIndent(true))

      when:
        def response = syncUpdate update

      then:
        response.contains("***Error:   Override authentication failed")
    }

    def "override is noop on case-sensitive change without "() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /org-name:     Test Organisation Ltd/).replaceFirst("org-name: test organisation ltd")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        result.contains("Warning: Submitted object identical to database object\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "add comment is not a noop for whois update"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /address:      street 5/).replaceFirst("address:      street 5 #test comment noop")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        !result.contains("Warning: Submitted object identical to database object\n" +
                "\n" +
                "***Info:    Authorisation override used")
        result.contains("Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "modify comment is not a noop for whois update"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /descr:        test org #comment/).replaceFirst("descr:        test org #updated")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        !result.contains("Warning: Submitted object identical to database object\n" +
                "\n" +
                "***Info:    Authorisation override used")
        result.contains("Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "remove comment is not a noop for whois update"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1"
        data = (data =~ /descr:        test org #comment/).replaceFirst("descr:        test org")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        !result.contains("Warning: Submitted object identical to database object\n" +
                "\n" +
                "***Info:    Authorisation override used")
        result.contains("Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "override is noop on case-sensitive change with update-on-noop set to false"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1, {update-on-noop=false}"
        data = (data =~ /org-name:     Test Organisation Ltd/).replaceFirst("org-name: test organisation ltd")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        result.contains("Warning: Submitted object identical to database object\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "override on case-sensitive change with update-on-noop set to true"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1, {update-on-noop=true}"
        data = (data =~ /org-name:     Test Organisation Ltd/).replaceFirst("org-name: test organisation ltd")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        result.contains("Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "override on comment changes change with update-on-noop set to true"() {
        given:
        def data = fixtures["ORG1"].stripIndent(true) + "override:denis,override1, {update-on-noop=true}"
        data = (data =~ /org-name:     Test Organisation Ltd/).replaceFirst("org-name:     Test Organisation Ltd #comment")

        def update = new SyncUpdate(data: data)

        when:
        def result = syncUpdate update

        then:
        result.contains("Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "***Warning: Comments are not allowed on RIPE NCC managed Attribute \"org-name:\"\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }
}
