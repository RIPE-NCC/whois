package spec.integration

import net.ripe.db.whois.common.IntegrationTest
import spec.domain.SyncUpdate

@org.junit.experimental.categories.Category(IntegrationTest.class)
class PoeticFormIntegrationSpec extends BaseSpec {

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
            auth:   MD5-PW \$1\$5aMDZg3w\$zL59TnpAszf6Ft.zs148X0 # update2
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

    def "add poetic form"() {
      when:
        def update = new SyncUpdate(data: """\
            poetic-form:     FORM-LIMERICK
            descr:           The object consists of a verse
            descr:           in a format approaching the terse
            descr:           The rhymes, very strict
            descr:           Must be carefully picked
            descr:           and it<92>s funny and often perverse
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            password:        update
            """.stripIndent())

        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
        response.contains("Create SUCCEEDED: [poetic-form] FORM-LIMERICK")
    }

    def "modify poetic form"() {
      when:
        def create = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            password:        update
            """.stripIndent())

        def createResponse = syncUpdate create

      then:
        createResponse =~ /SUCCESS/
        createResponse.contains("Create SUCCEEDED: [poetic-form] FORM-HAIKU")

      when:
        def update = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           The haiku object
            descr:           only seven syllables
            descr:           in its density
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            password:        update
            """.stripIndent())

        def updateResponse = syncUpdate update

      then:
        updateResponse =~ /SUCCESS/
        updateResponse.contains("Modify SUCCEEDED: [poetic-form] FORM-HAIKU")
    }

    def "delete poetic form"() {
      when:
        def create = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            password:        update
            """.stripIndent())

        def createResponse = syncUpdate create

      then:
        createResponse =~ /SUCCESS/
        createResponse.contains("Create SUCCEEDED: [poetic-form] FORM-HAIKU")

      when:
        def delete = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            changed:         ripe-dbm@ripe.net 20060913
            source:          TEST
            password:        update
            delete:          test
            """.stripIndent())

        def deleteResponse = syncUpdate delete

      then:
        deleteResponse =~ /SUCCESS/
        deleteResponse.contains("Delete SUCCEEDED: [poetic-form] FORM-HAIKU")
    }
}

