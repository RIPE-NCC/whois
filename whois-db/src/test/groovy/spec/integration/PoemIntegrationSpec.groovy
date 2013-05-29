package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class PoemIntegrationSpec extends BaseWhoisSourceSpec {

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
                "LIM-MNT": """\
            mntner: LIM-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: LIM-MNT
            referral-by: LIM-MNT
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
            """,
                "FORM-HAIKU": """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            """
        ]
    }

    def "add poem"() {
      given:
        def update = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            The haiku object
            text:            Never came to life as such
            text:            It's now generic
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
        response.contains("Create SUCCEEDED: [poem] POEM-HAIKU-OBJECT")
    }

    def "add poem different LIM-MNT"() {
      given:
        def update = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            The haiku object
            text:            Never came to life as such
            text:            It's now generic
            author:          TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Create FAILED: [poem] POEM-HAIKU-OBJECT")
        response.contains("***Error:   Poem must be maintained by 'LIM-MNT', which has a public password")
    }

    def "add poem multiple maintainers"() {
      given:
        def update = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            The haiku object
            text:            Never came to life as such
            text:            It's now generic
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            mnt-by:          UPD-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

      when:
        def response = syncUpdate update

      then:
        response.contains("Create FAILED: [poem] POEM-HAIKU-OBJECT")
        response.contains("***Error:   Attribute \"mnt-by\" appears more than once")
    }

    def "modify poem"() {
      when:
        def create = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            ...
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

        def createResponse = syncUpdate create

      then:
        createResponse =~ /SUCCESS/
        createResponse.contains("Create SUCCEEDED: [poem] POEM-HAIKU-OBJECT")

      when:
        def update = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            The haiku object
            text:            Never came to life as such
            text:            It's now generic
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

        def updateResponse = syncUpdate update

      then:
        updateResponse =~ /SUCCESS/
        updateResponse.contains("Modify SUCCEEDED: [poem] POEM-HAIKU-OBJECT")
    }

    def "delete poem"() {
      when:
        def create = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            ...
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            """.stripIndent())

        def createResponse = syncUpdate create

      then:
        createResponse =~ /SUCCESS/
        createResponse.contains("Create SUCCEEDED: [poem] POEM-HAIKU-OBJECT")

      when:
        def delete = new SyncUpdate(data: """\
            poem:            POEM-HAIKU-OBJECT
            form:            FORM-HAIKU
            text:            ...
            author:          TEST-RIPE
            mnt-by:          LIM-MNT
            changed:         noc@plaul.de 20050614
            source:          TEST
            password:        update
            delete:          test
            """.stripIndent())

        def deleteResponse = syncUpdate delete

      then:
        deleteResponse =~ /SUCCESS/
        deleteResponse.contains("Delete SUCCEEDED: [poem] POEM-HAIKU-OBJECT")
    }
}

