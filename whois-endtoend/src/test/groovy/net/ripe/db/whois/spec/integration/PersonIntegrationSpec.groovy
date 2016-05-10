package net.ripe.db.whois.spec.integration
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.spec.domain.Message
import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class PersonIntegrationSpec extends BaseWhoisSourceSpec {

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
                "UPD-MNT2": """\
            mntner: UPD-MNT2
            descr: description
            admin-c: TEST-RIPE
            mnt-by: UPD-MNT2
            upd-to: dbtest@ripe.net
            auth:   MD5-PW \$1\$5aMDZg3w\$zL59TnpAszf6Ft.zs148X0 # update2
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
            source:  TEST
            """,
                "TEST-PN2": """\
            person:  Test Person2
            address: Hebrew Road
            address: Burnley
            address: UK
            phone:   +44 282 411141
            nic-hdl: TP2-TEST
            mnt-by:  UPD-MNT
            source:  TEST
            """,
                "ROLE": """\
            role: Test Admin
            address: Hebrew Road
            address: Burnley
            address: UK
            phone:   +44 282 411141
            fax-no:  +44 282 411140
            e-mail:  admin@test.com
            admin-c: TEST-RIPE
            tech-c:  TEST-RIPE
            nic-hdl: RL-TEST
            mnt-by:  UPD-MNT
            notify:  admin@test.com
            abuse-mailbox: admin@test.com
            source:  TEST
            """,
                "SELF-REF": """\
            role: Other Admin
            address: Hebrew Road
            address: Burnley
            address: UK
            phone:   +44 282 411141
            fax-no:  +44 282 411140
            e-mail:  admin@test.com
            admin-c: RL2-RIPE
            tech-c:  RL2-RIPE
            nic-hdl: RL2-RIPE
            mnt-by:  UPD-MNT
            notify:  admin@test.com
            abuse-mailbox: admin@test.com
            source:  TEST
            """,
                "ORG-NCC1-RIPE": """\
            organisation: ORG-NCC1-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-ref:      UPD-MNT
            mnt-by:       UPD-MNT
            source:       TEST
            """,
                "ORG-NCC2-RIPE": """\
            organisation: ORG-NCC2-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-ref:      UPD-MNT2
            mnt-by:       UPD-MNT2
            source:       TEST
            """,
                "ORG-NCC3-RIPE": """\
            organisation: ORG-NCC3-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            mnt-ref:      UPD-MNT
            mnt-ref:      UPD-MNT2
            mnt-by:       UPD-MNT
            source:       TEST
            """
        ]
    }

    def "delete non-existing person"() {
      given:
        def update = new SyncUpdate(data: """\
                        person:  Test Person
                        address: St James Street
                        address: Burnley
                        address: UK
                        phone:   +44 282 420469
                        nic-hdl: TP1-TEST
                        mnt-by:  UPD-MNT
                        source:  TEST
                        delete: some reason
                        password: update
                        """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ "\\*\\*\\*Error:   Object \\[person\\] TP1-TEST   Test Person does not exist in the\n            database"
    }


    def "delete existing person"() {
      given:
        def update = new SyncUpdate(data: "" +
                fixtures["TEST-PN2"].stripIndent() +
                "delete: some reason\n" +
                "password: update")

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
    }


    def "delete person with references"() {
      given:
        def update = new SyncUpdate(data: "" +
                fixtures["ADMIN-PN"].stripIndent() +
                "delete: some reason\n" +
                "password: update")

      when:
        def response = syncUpdate update

      then:
        response =~ /Object \[person\] TEST-RIPE is referenced from other objects/
    }



    def "delete existing role "() {
      given:
        def update = new SyncUpdate(data: "" +
                fixtures["ROLE"].stripIndent() +
                "delete: some reason\n" +
                "password: update")

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
    }


    def "delete self referencing role"() {
      given:
        def update = new SyncUpdate(data: "" +
                fixtures["SELF-REF"].stripIndent() +
                "password:update\n" +
                "delete:some reason")
      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
    }


    def "update self referencing role"() {
      given:
        def role = new SyncUpdate(data: ("" +
                fixtures["SELF-REF"].stripIndent() +
                "password:update\n" =~ /admin-c: RL2-RIPE/).replaceFirst("admin-c:RL-TEST"))

      when:
        def response = syncUpdate role

      then:
        response =~ /FAIL/
        response =~ /Error:   Self reference is not allowed for attribute type "tech-c"/
    }

    def "create person"() {
      given:
        def person = new SyncUpdate(data: """\
                person:  New Test Person
                address: UK
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: TP3-TEST
                mnt-by:  UPD-MNT
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST/
    }

    def "create person returns non-ASCII characters in query response"() {
      given:
        def create = syncUpdate new SyncUpdate(data: """\
                person:     Test Person2
                address:    Flughafenstraße 109/a
                address:    München, Germany
                phone:      +49 282 411141
                fax-no:     +49 282 411140
                nic-hdl:    TP3-TEST
                mnt-by:     UPD-MNT
                source:     TEST
                password: update
                """.stripIndent())

      when:
        create =~ /Create SUCCEEDED: \[person\] TP3-TEST/

      then:
        def query = query "-r -T person TP3-TEST"
          query =~ /address:\s+Flughafenstraße 109\/a/
          query =~ /address:\s+München, Germany/
    }

    def "update person returns non-ASCII characters in query response"() {
      given:
        def update = syncUpdate new SyncUpdate(data: """\
                person:     Admin Person
                address:    Flughafenstraße 109/a
                address:    München, Germany
                address:    DE
                phone:      +49 282 411141
                nic-hdl:    TEST-RIPE
                mnt-by:     UPD-MNT
                source:     TEST
                password: update
                """.stripIndent())

      when:
        update =~ /Modify SUCCEEDED: \[person\] TEST-RIPE/

      then:
        def query = query "-r -T person TEST-RIPE"
        query =~ /address:\s+Flughafenstraße 109\/a/
        query =~ /address:\s+München, Germany/
    }

    def "create person returns non-ASCII characters with syncupdate"() {
      given:
        def person = new SyncUpdate(data: """\
                person:     New Test Person
                address:    Flughafenstraße 120
                address:    D - 40474 Düsseldorf
                phone:      +49 282 411141
                fax-no:     +49 282 411140
                nic-hdl:    AUTO-1
                source:     TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /address:\s+Flughafenstraße 120/
        response =~ /address:\s+D \- 40474 Düsseldorf/
        response =~ /Mandatory attribute \"mnt-by\" is missing/
    }

    def "create person returns non-ASCII characters with mail update"() {
      given:
        def person = new Message(body: """\
                person:     New Test Person
                address:    Flughafenstraße 120
                address:    D - 40474 Düsseldorf
                phone:      +49 282 411141
                fax-no:     +49 282 411140
                nic-hdl:    AUTO-1
                source:     TEST
                password: update
                """.stripIndent())

      when:
        def message = send person
        def response = ackFor message

      then:
        response =~ /address:\s+Flughafenstraße 120/
        response =~ /address:\s+D \- 40474 Düsseldorf/
        response =~ /Mandatory attribute \"mnt-by\" is missing/
    }

    def "create person with generated nic-hdl"() {
      given:
        def person = new SyncUpdate(data: """\
                person:  Some person
                address: Somewhere
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /SUCCESS/
        response =~ /Create SUCCEEDED: \[person\] SP1-TEST/
    }

    def "create second person with generated nic-hdl is incremented"() {
      given:
        def firstPerson = syncUpdate new SyncUpdate(data: """\
                person:  Some person
                address: Somewhere
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                source:  TEST
                password: update
                """.stripIndent())
      when:
        firstPerson =~ /SUCCESS/
        firstPerson =~ /Create SUCCEEDED: \[person\] SP1-TEST/
      then:
        def secondPerson = syncUpdate new SyncUpdate(data: """\
                person:  Some person
                address: Somewhere Else
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                source:  TEST
                password: update
                """.stripIndent())
      then:
        secondPerson =~ /SUCCESS/
        secondPerson =~ /Create SUCCEEDED: \[person\] SP2-TEST/
    }

    def "create person without nic-hdl"() {
      given:
        def person = new SyncUpdate(data: """\
                person:  Some person
                address: Somewhere
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                mnt-by:  UPD-MNT
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            person:  Some person
            """.stripIndent()
    }

    def "create person with empty nic-hdl"() {
      given:
        def person = new SyncUpdate(data: """\
                person:  Some person
                address: Somewhere
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                mnt-by:  UPD-MNT
                nic-hdl:
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ """\
            The following paragraph\\(s\\) do not look like objects
            and were NOT PROCESSED:

            person:  Some person
            """.stripIndent()
    }

    def "create role with generated nic-hdl and self reference"() {
      given:
        def person = new SyncUpdate(data: """\
                role: Some admin
                address: Hebrew Road
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                e-mail:  admin@test.com
                admin-c: auto-5
                tech-c:  AuTo-5
                nic-hdl: AuTo-5
                mnt-by:  UPD-MNT
                notify:  admin@test.com
                abuse-mailbox: admin@test.com
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /FAIL/
        response =~ """admin-c:        auto-5\n\\*\\*\\*Error:   Self reference is not allowed for attribute type \"admin-c\""""
        response =~ """tech-c:         AuTo-5\n\\*\\*\\*Error:   Self reference is not allowed for attribute type \"tech-c\""""
    }

    def "create role with generated nic_hdl and invalid self reference"() {
      given:
        def person = new SyncUpdate(data: """\
                role: Some admin
                address: Hebrew Road
                phone:   +44 282 411141
                fax-no:  +44 282 411140
                e-mail:  admin@test.com
                admin-c: AUTO-3
                tech-c:  AUTO-2
                nic-hdl: AUTO-1
                mnt-by:  UPD-MNT
                notify:  admin@test.com
                abuse-mailbox: admin@test.com
                source:  TEST
                password: update
                """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /FAILED/
        response =~ /admin-c:        AUTO-3\n\*\*\*Error:   Reference "AUTO-3" not found/
        response =~ /tech-c:         AUTO-2\n\*\*\*Error:   Reference "AUTO-2" not found/
        response =~ /nic-hdl:        AUTO-1/
    }

    def "create person with invalid nic_handle"() {
        def person = new SyncUpdate(data: """\
            person:  Test Person
            address: Hebrew Road
            address: Burnley
            address: UK
            phone:   +44 282 411141
            fax-no:  +44 282 411140
            nic-hdl: INVALID_HANDLE
            mnt-by:  UPD-MNT
            source:  TEST
            password: update
            """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /\*\*\*Error:   Syntax error in INVALID_HANDLE/
    }

    def "update person"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person2
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP2-TEST
              mnt-by:  UPD-MNT
              source:  TEST
              password: update
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /Modify SUCCEEDED: \[person\] TP2-TEST   Test Person2/
    }

    def "update person without password"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person2
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP2-TEST
              mnt-by:  UPD-MNT
              source:  TEST
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[person\\] TP2-TEST failed
                        using "mnt-by:"
                        not authenticated by: UPD-MNT
            """.stripIndent()
    }

    def "create person succeeds with org with nonexistant mnt-ref"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     ORG-NCC3-RIPE
              source:  TEST
              password: update
              """.stripIndent())

      when:
        deleteObject("UPD-MNT2");
        def response = syncUpdate person

      then:
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST   Test Person3/
        response =~ "\\*\\*\\*Warning: Referenced organisation ORG-NCC3-RIPE has mnt-ref attribute UPD-MNT2\n            which does not exist in the database"
    }

    def "create person fails with org with nonexistant mnt-ref"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     ORG-NCC2-RIPE
              source:  TEST
              password: invalid
              """.stripIndent())

      when:
        deleteObject("UPD-MNT2");
        def response = syncUpdate person

      then:
        response =~ """
            \\*\\*\\*Warning: Referenced organisation ORG-NCC2-RIPE has mnt-ref attribute UPD-MNT2
                        which does not exist in the database""".stripIndent()
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[person\\] TP3-TEST failed
                        using "mnt-by:"
                        not authenticated by: UPD-MNT""".stripIndent()
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-NCC2-RIPE failed
                        using "mnt-ref:"
                        no valid maintainer found""".stripIndent()
    }

    def "modify person fails with org with nonexistant mnt-ref"() {
        def person = new SyncUpdate(data:
                getFixtures().get("TEST-PN2").stripIndent() + "org: ORG-NCC2-RIPE\n" + "password: invalid\n")

      when:
        deleteObject("UPD-MNT2")
        def response = syncUpdate person

      then:
        response =~ "\\*\\*\\*Warning: Referenced organisation ORG-NCC2-RIPE has mnt-ref attribute UPD-MNT2\n            which does not exist in the database"
    }

    def "create person with org reference is authorised"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     ORG-NCC1-RIPE
              source:  TEST
              password: update
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST   Test Person3/
    }

    def "modify person with org reference is authorised"() {
        def person = new SyncUpdate(data:
                getFixtures().get("TEST-PN2").stripIndent() + "org: ORG-NCC1-RIPE\n" + "password: update")

      when:
        def response = syncUpdate person

      then:
        response =~ /Modify SUCCEEDED: \[person\] TP2-TEST   Test Person2/
    }

    def "create person with org reference is not authorised"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     ORG-NCC2-RIPE
              source:  TEST
              password: update
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-NCC2-RIPE failed
                        using "mnt-ref:"
                        not authenticated by: UPD-MNT2
            """.stripIndent()
    }

    def "modify person with org reference is not authorised"() {
        def person = new SyncUpdate(data:
                getFixtures().get("TEST-PN2").stripIndent() + "org: ORG-NCC2-RIPE\n" + "password: update")

      when:
        def response = syncUpdate person

      then:
        response =~ """
            \\*\\*\\*Error:   Authorisation for \\[organisation\\] ORG-NCC2-RIPE failed
                        using "mnt-ref:"
                        not authenticated by: UPD-MNT2
            """.stripIndent()
    }

    def "create person with non-existant org attribute"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     ORG-NON-EXISTANT
              source:  TEST
              password: update
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /Create FAILED: \[person\] TP3-TEST   Test Person3/
        response =~ /\\*\\*\\*Error:   Unknown object referenced ORG-NON-EXISTANT/
    }

    def "create person with syntax error on org attribute value"() {
        def person = new SyncUpdate(data: """\
              person:  Test Person3
              address: Hebrew Road
              address: Burnley
              address: UK
              phone:   +44 282 411141
              fax-no:  +44 282 411140
              nic-hdl: TP3-TEST
              mnt-by:  UPD-MNT
              org:     INVALID
              source:  TEST
              password: update
              """.stripIndent())

      when:
        def response = syncUpdate person

      then:
        response =~ /Create FAILED: \[person\] TP3-TEST   Test Person3/
        response =~ /\\*\\*\\*Error:   Syntax error in INVALID/
    }

    def "change person attribute of existing person succeeds"() {
      given:
        def data = fixtures["TEST-PN2"].stripIndent() + "password:update"
        data = (data =~ /person:  Test Person2/).replaceFirst("person: Modify Person")

        def modify = new SyncUpdate(data: data)

      when:
        def response = syncUpdate modify

      then:
        response =~ /Modify SUCCEEDED: \[person\] TP2-TEST   Modify Person/
    }

    def "role with referenced abuse-c removed"() {
        given:
            def role = syncUpdate(new SyncUpdate(data: """\
            role: Abuse Role
            e-mail: test@ripe.net
            admin-c: TEST-RIPE
            tech-c: TEST-RIPE
            address: Hebrew Road
            phone:   +44 282 411141
            fax-no:  +44 282 41114
            nic-hdl: AB-TEST
            abuse-mailbox: abuse@ripe.net
            mnt-by: UPD-MNT
            source:  TEST
            password: update
            """.stripIndent()))

        expect:
            role =~ /SUCCESS/

        when:
            def org = syncUpdate(new SyncUpdate(data: """\
            organisation: ORG-NCC2-RIPE
            org-name:     Ripe NCC organisation
            org-type:     LIR
            address:      Singel 258
            e-mail:        bitbucket@ripe.net
            abuse-c:      AB-TEST
            mnt-ref:      UPD-MNT2
            mnt-by:       UPD-MNT2
            source:       TEST
            password: update2
            """.stripIndent()))

        then:
            org =~ /SUCCESS/

        when:
            def roleUpdate = syncUpdate(new SyncUpdate(data: """\
            role: Abuse Role
            nic-hdl: AB-TEST
            e-mail: test@ripe.net
            admin-c: TEST-RIPE
            tech-c: TEST-RIPE
            address: Hebrew Road
            phone:   +44 282 411141
            fax-no:  +44 282 41114
            mnt-by: UPD-MNT
            source:  TEST
            password: update
            """.stripIndent()))

        then:
            roleUpdate =~ /There is an organisation referencing role Abuse Role's abuse-mailbox/
    }

}

