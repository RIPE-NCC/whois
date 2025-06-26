package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.SyncUpdate

@org.junit.jupiter.api.Tag("IntegrationTest")
class PoeticFormIntegrationSpec extends BaseWhoisSourceSpec {

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
                "RIPE-DBM-MNT": """\
            mntner: RIPE-DBM-MNT
            descr: description
            admin-c: TEST-RIPE
            mnt-by: RIPE-DBM-MNT
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
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

        def response = syncUpdate update

      then:
        response =~ /SUCCESS/
        response.contains("Create SUCCEEDED: [poetic-form] FORM-LIMERICK")
    }

    def "add poetic form different mntner"() {
        given:
        def update = new SyncUpdate(data: """\
            poetic-form:     FORM-SONNET-INDONESIAN
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            source:          TEST
            password:        update
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response.contains("Create FAILED: [poetic-form] FORM-SONNET-INDONESIAN")
        response.contains("Poetic-form must only be maintained by 'RIPE-DBM-MNT'")
    }

    def "add poetic form incorrect mntner"() {
        given:
        def update = new SyncUpdate(data: """\
            poetic-form:     FORM-SONNET-INDONESIAN
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response.contains("Create FAILED: [poetic-form] FORM-SONNET-INDONESIAN")
        response.contains("Poetic-form must only be maintained by 'RIPE-DBM-MNT'")
    }

    def "add poetic form multiple mntners"() {
        given:
        def update = new SyncUpdate(data: """\
            poetic-form:     FORM-SONNET-INDONESIAN
            admin-c:         TEST-RIPE
            mnt-by:          UPD-MNT
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response.contains("Create FAILED: [poetic-form] FORM-SONNET-INDONESIAN")
        response.contains("Attribute \"mnt-by\" appears more than once")
    }

    def "modify poetic form"() {
      when:
        def create = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

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
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

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
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            """.stripIndent(true))

        def createResponse = syncUpdate create

      then:
        createResponse =~ /SUCCESS/
        createResponse.contains("Create SUCCEEDED: [poetic-form] FORM-HAIKU")

      when:
        def delete = new SyncUpdate(data: """\
            poetic-form:     FORM-HAIKU
            descr:           haiku
            admin-c:         TEST-RIPE
            mnt-by:          RIPE-DBM-MNT
            source:          TEST
            password:        update2
            delete:          test
            """.stripIndent(true))

        def deleteResponse = syncUpdate delete

      then:
        deleteResponse =~ /SUCCESS/
        deleteResponse.contains("Delete SUCCEEDED: [poetic-form] FORM-HAIKU")
    }
}

