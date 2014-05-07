package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class OverrideIntegrationSpec extends BaseWhoisSourceSpec {

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
            """
        ]
    }

    def "override with mail update"() {
      when:
        def data = fixtures["ORG1"].stripIndent() + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def message = send new Message(body: data)
        def ack = ackFor message

      then:
        ack.contents.contains("***Error:   Override not allowed in email update")
    }

    def "override outside ripe range"() {
      when:
        setRipeRanges();

        def data = fixtures["ORG1"].stripIndent() + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)
        def result = syncUpdate update

      then:
        result.contains("***Error:   Override only allowed by database administrators")
    }

    def "override"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "override:denis,override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("" +
                "Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }

    def "override option invalid id"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "override:user,pw,{oid=11111}"

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("Create FAILED: [organisation] ORG-TOL1-TEST")
        result.contains("***Error:   Original object with id 11111 specified in override not found")
    }

    def "override not on update"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "\n\noverride:denis,override1"
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
        def data = fixtures["ORG1"].stripIndent() + "override:oops"
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
        def data = fixtures["ORG1"].stripIndent() + "override:agoston,oops\npassword: update\n"
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
        def data = fixtures["ORG1"].stripIndent() + "override: denis, override1"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("" +
                "Modify SUCCEEDED: [organisation] ORG-TOL1-TEST\n" +
                "\n" +
                "\n" +
                "***Info:    Authorisation override used")
    }


    def "override specified multiple times"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "override:denis,override1\noverride:denis2,override2"
        data = (data =~ /org-type:     OTHER/).replaceFirst("org-type: IANA")

        def update = new SyncUpdate(data: data)

      when:
        def result = syncUpdate update

      then:
        result.contains("***Error:   Multiple override passwords used")
    }

    def "override no password"() {
      given:
        def data = fixtures["ORG1"].stripIndent() + "override:"
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
            referral-by: TST-MNT
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update
            changed: dbtest@ripe.net 20120707
            source: TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("***Error:   Override authentication failed")
    }
}
