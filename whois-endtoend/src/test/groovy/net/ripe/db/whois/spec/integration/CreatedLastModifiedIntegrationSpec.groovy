package net.ripe.db.whois.spec.integration


import net.ripe.db.whois.spec.domain.SyncUpdate
import java.time.LocalDateTime

@org.junit.jupiter.api.Tag("IntegrationTest")
class CreatedLastModifiedIntegrationSpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return ["TST-MNT": """\
            mntner:  TST-MNT
            descr:   description
            admin-c: TEST-RIPE
            mnt-by:  TST-MNT
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
            """];
    }

    // CREATE

    def "create object with created and last-modified generates new values"() {
        given:
        def currentDateTime = getTimeUtcString()

        def update = new SyncUpdate(data: """\
        person:        Test Person
        address:       Singel 258
        phone:         +3112346
        nic-hdl:       TP3-TEST
        mnt-by:        TST-MNT
        created:       2012-05-03T11:23:66Z
        last-modified: 2012-05-03T11:23:66Z
        source:        TEST
        password: update
        """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST   Test Person/
        response !=~ /Warning: Supplied attribute 'created' has been replaced with a generated\n\s+value/
        response !=~ /Warning: Supplied attribute 'last-modified' has been replaced with a\n\s+generated value/

        when:
        def created = query("TP3-TEST")

        then:
        created =~ /created:        ${currentDateTime}/
        created =~ /last-modified:  ${currentDateTime}/
    }

    def "create object with no created or last-modified generates new values"() {
        def update = new SyncUpdate(data: """\
        person:        Test Person
        address:       Singel 258
        phone:         +3112346
        nic-hdl:       AUTO-1
        mnt-by:        TST-MNT
        source:        TEST
        password: update
        """.stripIndent(true))

        when:
        def response = syncUpdate update

        then:
        response =~ /Create SUCCEEDED: \[person\] TP1-TEST   Test Person/

        queryLineMatches("TP1-TEST", "created:\\s*\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ");
        queryLineMatches("TP1-TEST", "last-modified:\\s*\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ");
    }

    def "create object with multiple last-modified attributes"() {
      given:
        def update = new SyncUpdate(data: """\
        person:        Test Person
        address:       Singel 258
        phone:         +3112346
        nic-hdl:       TP3-TEST
        mnt-by:        TST-MNT
        created:       2012-05-03T11:23:66Z
        last-modified: 2012-05-03T11:23:66Z
        last-modified: 2012-05-03T11:23:66Z
        source:        TEST
        password: update
        """.stripIndent(true))
      when:
        def response = syncUpdate update
      then:
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST   Test Person/
        response =~ /Warning: Supplied attribute 'created' has been replaced with a generated\n\s+value/
        response =~ /Warning: Supplied attribute 'last-modified' has been replaced with a\n\s+generated value/
    }

    // MODIFY

    def "modify object created attribute stays the same"() {
        given:
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = getTimeUtcString()

        syncUpdate(new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            remarks: created
            source:  TEST
            password: update
            """.stripIndent(true)))

        when:
        def created = query("OP1-TEST");

        then:
        created =~/created:        ${yesterdayDateTime}/

        when:
        setTime(LocalDateTime.now())

        def update = new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            remarks: updated
            source:  TEST
            password: update
            """.stripIndent(true))

        def response = syncUpdate update

        then:
        response =~ /Modify SUCCEEDED: \[person\] OP1-TEST   Other Person/

        when:
        def updated = query("OP1-TEST")

        then:
        updated =~/created:        ${yesterdayDateTime}/
    }

    def "modify object no operation"() {
      given:
        setTime(LocalDateTime.now().minusDays(1))
        queryObjectNotFound("-r OP1-TEST", "person", "OP1-TEST")
      when:
        def createResponse = syncUpdate new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            source:  TEST
            password: update
            """.stripIndent(true))
      then:
        createResponse =~ /Create SUCCEEDED: \[person\] OP1-TEST   Other Person/
      then:
        queryObject("OP1-TEST", "person", "Other Person")
      when:
        setTime(LocalDateTime.now())
        def updateResponse = syncUpdate new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            source:  TEST
            password: update
            """.stripIndent(true))
        then:
          updateResponse =~ /No operation: \[person\] OP1-TEST   Other Person/
    }

    def "modify object without created generates last-modified only"() {
        given:
        def currentDateTime = getTimeUtcString()
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source:  TEST")

        when:
        def update = syncUpdate(new SyncUpdate(data:
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n"+
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "remarks:  updated\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source:  TEST\n" +
                "password: update"))

        then:
        update =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updated = query("-rBG LOOP-MNT")

        then:
        updated !=~ /created:/
        updated =~ /last-modified:  ${currentDateTime}/
    }

    def "modify object with last-modified updates last-modified"() {
        given:
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = getTimeUtcString()
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source:  TEST")

        when:
        def updateYesterday = syncUpdate(new SyncUpdate(data:
                        "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: yesterday\n" +
                        "mnt-by:  LOOP-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "source:  TEST\n" +
                        "password: update"))

        then:
        updateYesterday =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updated = query("-rBG LOOP-MNT")

        then:
        updated =~ /last-modified:  ${yesterdayDateTime}/

        when:
        setTime(LocalDateTime.now())
        def currentDateTime = getTimeUtcString()

        def updateToday = syncUpdate(new SyncUpdate(data:
                        "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: today\n" +
                        "mnt-by:  LOOP-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "source:  TEST\n" +
                        "password: update"))

        then:
        updateToday =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updatedToday = query("-rBG LOOP-MNT")

        then:
        updatedToday =~ /last-modified:  ${currentDateTime}/
    }

    def "modify should retain the correct order of the timestamp attributes"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def createAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                mnt-by:  TST-MNT
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        createAck.contains("Create SUCCEEDED: [person] NP1-TEST   New Person")

      when:
        setTime(LocalDateTime.parse("2013-06-26T09:00:00"))
      then:
        def updateAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                created: 2013-06-25T09:00:00Z
                mnt-by:  TST-MNT
                last-modified: 2013-06-25T09:00:00Z
                remarks: testing
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        updateAck.contains("Modify SUCCEEDED: [person] NP1-TEST   New Person")
      then:
        query("-rBG NP1-TEST").contains(
                "person:         New Person\n" +
                "address:        St James Street\n" +
                "phone:          +44 282 420469\n" +
                "nic-hdl:        NP1-TEST\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "mnt-by:         TST-MNT\n" +
                "last-modified:  2013-06-26T09:00:00Z\n" +
                "remarks:        testing\n" +
                "source:         TEST\n")
    }

    def "modify object with created and last-modified generates new values"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def createAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                mnt-by:  TST-MNT
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        createAck.contains("Create SUCCEEDED: [person] NP1-TEST   New Person")
      when:
        setTime(LocalDateTime.parse("2013-06-26T09:00:00"))
      then:
        def updateAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                mnt-by:  TST-MNT
                remarks: testing
                created: 2001-01-01T09:00:00Z
                last-modified: 2001-01-01T09:00:00Z
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        updateAck =~ /Modify SUCCEEDED: \[person\] NP1-TEST   New Person/
        updateAck =~ /Warning: Supplied attribute 'created' has been replaced with a generated\n\s+value/
        updateAck =~ /Warning: Supplied attribute 'last-modified' has been replaced with a\n\s+generated value/
      then:
        query("-rBG NP1-TEST").contains(
                "person:         New Person\n" +
                "address:        St James Street\n" +
                "phone:          +44 282 420469\n" +
                "nic-hdl:        NP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "remarks:        testing\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-26T09:00:00Z\n" +
                "source:         TEST\n")
    }

    def "modify object with multiple created and last-modified attributes"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def createAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                mnt-by:  TST-MNT
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        createAck.contains("Create SUCCEEDED: [person] NP1-TEST   New Person")
      when:
        setTime(LocalDateTime.parse("2013-06-26T09:00:00"))
      then:
        def updateAck = syncUpdate new SyncUpdate(data: """
                person:  New Person
                address: St James Street
                phone:   +44 282 420469
                nic-hdl: NP1-TEST
                mnt-by:  TST-MNT
                remarks: testing
                created: 2001-01-01T09:00:00Z
                last-modified: 2001-01-01T09:00:00Z
                created: 2001-01-01T09:00:00Z
                last-modified: 2001-01-01T09:00:00Z
                last-modified: 2001-01-01T09:00:00Z
                source:  TEST
                password: update
             """.stripIndent(true))
      then:
        updateAck =~ /Modify SUCCEEDED: \[person\] NP1-TEST   New Person/
        updateAck =~ /Warning: Supplied attribute 'created' has been replaced with a generated\n\s+value/
        updateAck =~ /Warning: Supplied attribute 'last-modified' has been replaced with a\n\s+generated value/
      then:
        query("-rBG NP1-TEST").contains(
                "person:         New Person\n" +
                "address:        St James Street\n" +
                "phone:          +44 282 420469\n" +
                "nic-hdl:        NP1-TEST\n" +
                "mnt-by:         TST-MNT\n" +
                "remarks:        testing\n" +
                "created:        2013-06-25T09:00:00Z\n" +
                "last-modified:  2013-06-26T09:00:00Z\n" +
                "source:         TEST\n")
    }


    // DELETE

    def "delete object with incorrect created or last-modified succeeds"() {

        given:
        setTime(LocalDateTime.now().minusDays(1))

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent(true)))
        then:
        update =~ /SUCCESS/


        when:
        setTime(LocalDateTime.now())
        def currentDateTime = getTimeUtcString()
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent(true)))

        then:
        delete =~ /SUCCESS/
    }

    def "delete object with correct created and last-modified succeeds"() {
        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent(true)))
        then:
        update =~ /SUCCESS/


        when:
        def currentDateTime = getTimeUtcString()
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent(true)))

        then:
        delete =~ /SUCCESS/
    }

    def "delete object with no created or last-modified succeeds"() {
        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent(true)))
        then:
        update =~ /SUCCESS/

        when:
        def updated = query("TP3-TEST")

        then:
        updated =~ /created:/
        updated =~ /last-modified:/


        when:
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent(true)))

        then:
        delete =~ /SUCCESS/
    }

    def "delete should not fail if created and last-modified attributes are separated"() {
      given:
        setTime(LocalDateTime.parse("2013-06-25T09:00:00"))
      when:
        def createAck = syncUpdate new SyncUpdate(data: """
                    person:  New Person
                    address: St James Street
                    phone:   +44 282 420469
                    nic-hdl: NP1-TEST
                    mnt-by:  TST-MNT
                    source:  TEST
                    password: update
                 """.stripIndent(true))
      then:
        createAck.contains("Create SUCCEEDED: [person] NP1-TEST   New Person")

      when:
        setTime(LocalDateTime.parse("2013-06-26T09:00:00"))
      then:
        def updateAck = syncUpdate new SyncUpdate(data: """
                    person:  New Person
                    address: St James Street
                    nic-hdl: NP1-TEST
                    mnt-by:  TST-MNT
                    last-modified: 2013-06-25T09:00:00Z
                    phone:   +44 282 420469
                    created: 2013-06-25T09:00:00Z
                    remarks: testing
                    source:  TEST
                    password: update
                 """.stripIndent(true))
      then:
        updateAck.contains("Modify SUCCEEDED: [person] NP1-TEST   New Person")
      then:
        query("-rBG NP1-TEST").contains(
                    "person:         New Person\n" +
                    "address:        St James Street\n" +
                    "nic-hdl:        NP1-TEST\n" +
                    "mnt-by:         TST-MNT\n" +
                    "last-modified:  2013-06-26T09:00:00Z\n" +
                    "phone:          +44 282 420469\n" +
                    "created:        2013-06-25T09:00:00Z\n" +
                    "remarks:        testing\n" +
                    "source:         TEST")

      when:
        setTime(LocalDateTime.parse("2013-06-27T09:00:00"))
      then:
        def deleteAck = syncUpdate new SyncUpdate(data: """
                    person:  New Person
                    address: St James Street
                    nic-hdl: NP1-TEST
                    mnt-by:  TST-MNT
                    last-modified: 2013-06-25T09:00:00Z
                    phone:   +44 282 420469
                    created: 2013-06-25T09:00:00Z
                    remarks: testing
                    source:  TEST
                    password: update
                    delete: reason
                 """.stripIndent(true))
      then:
        deleteAck.contains("Delete SUCCEEDED: [person] NP1-TEST   New Person")
    }
}
