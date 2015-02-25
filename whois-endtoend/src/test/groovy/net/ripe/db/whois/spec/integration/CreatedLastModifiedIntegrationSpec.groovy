package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.TestDateTimeProvider
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

@org.junit.experimental.categories.Category(IntegrationTest.class)
class CreatedLastModifiedIntegrationSpec extends BaseWhoisSourceSpec {
    @Override
    Map<String, String> getFixtures() {
        return ["TST-MNT": """\
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
            """];
    }
    static TestDateTimeProvider dateTimeProvider;

    def setupSpec() {
        dateTimeProvider = getApplicationContext().getBean(net.ripe.db.whois.common.TestDateTimeProvider.class);
    }


    def "create object with created and last-modified generates new values"() {
        given:
        dateTimeProvider.setTime(new DateTime())
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());

        def update = new SyncUpdate(data: """\
        person:        Test Person
        address:       Singel 258
        phone:         +3112346
        nic-hdl:       TP3-TEST
        changed:       admin@test.com 20120505
        mnt-by:        TST-MNT
        created:       2012-05-03T11:23:66Z
        last-modified: 2012-05-03T11:23:66Z
        source:        TEST
        password: update
        """.stripIndent())

        when:
        def response = syncUpdate update

        then:
        response =~ /Create SUCCEEDED: \[person\] TP3-TEST   Test Person/
        response =~ /Warning: Supplied attribute 'created' has been replaced with a generated
            value/
        response =~ /Warning: Supplied attribute 'last-modified' has been replaced with a
            generated value/

        when:
        def created = query("TP3-TEST")

        then:
        created =~ /created:        ${currentDate}/
        created =~ /last-modified:  ${currentDate}/
    }

    def "create object with no created or last-modified generates new values"() {
        def update = new SyncUpdate(data: """\
        person:        Test Person
        address:       Singel 258
        phone:         +3112346
        nic-hdl:       AUTO-1
        changed:       admin@test.com 20120505
        mnt-by:        TST-MNT
        source:        TEST
        password: update
        """.stripIndent())

        when:
        def response = syncUpdate update

        then:
        response =~ /Create SUCCEEDED: \[person\] TP1-TEST   Test Person/

        queryLineMatches("TP1-TEST", "created:\\s*\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ");
        queryLineMatches("TP1-TEST", "last-modified:\\s*\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ");
    }

    def "modify object created attribute stays the same"() {
        given:
        dateTimeProvider.setTime(new DateTime().minusDays(1))
        def yesterday = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());

        syncUpdate(new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            password: update
            """.stripIndent()))

        when:
        def created = query("OP1-TEST");

        then:
        created =~/created:        ${yesterday}/


        when:
        dateTimeProvider.setTime(new DateTime())

        def update = new SyncUpdate(data: """\
            person:  Other Person
            address: New Road
            address: Town
            address: UK
            phone:   +44 282 411141
            nic-hdl: OP1-TEST
            mnt-by:  TST-MNT
            changed: dbtest@ripe.net 20120101
            source:  TEST
            password: update
            """.stripIndent())

        def response = syncUpdate update

        then:
        response =~ /Modify SUCCEEDED: \[person\] OP1-TEST   Other Person/

        when:
        def updated = query("OP1-TEST")

        then:
        updated =~/created:        ${yesterday}/
    }

    def "modify object without created generates last-modified only"() {
        given:
        dateTimeProvider.setTime(new DateTime())
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST")

        when:
        def update = syncUpdate(new SyncUpdate(data:
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n"+
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "remarks:  updated\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST\n" +
                "password: update"))

        then:
        update =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updated = query("-rBG LOOP-MNT")

        then:
        updated !=~ /created:/
        updated =~ /last-modified:  ${currentDate}/
    }

    def "modify object with last-modified updates last-modified"() {
        given:
        dateTimeProvider.setTime(new DateTime().minusDays(1))
        def yesterday = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST")

        when:
        def updateYesterday = syncUpdate(new SyncUpdate(data:
                        "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: yesterday\n" +
                        "mnt-by:  LOOP-MNT\n" +
                        "referral-by: TST-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "changed: dbtest@ripe.net 20120707\n" +
                        "source:  TEST\n" +
                        "password: update"))

        then:
        updateYesterday =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updated = query("-rBG LOOP-MNT")

        then:
        updated =~ /last-modified:  ${yesterday}/

        when:
        dateTimeProvider.setTime(new DateTime())
        def today = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());

        def updateToday = syncUpdate(new SyncUpdate(data:
                        "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: today\n" +
                        "mnt-by:  LOOP-MNT\n" +
                        "referral-by: TST-MNT\n" +
                        "upd-to:  dbtest@ripe.net\n" +
                        "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                        "changed: dbtest@ripe.net 20120707\n" +
                        "source:  TEST\n" +
                        "password: update"))

        then:
        updateToday =~ /Modify SUCCEEDED: \[mntner\] LOOP-MNT/

        when:
        def updatedToday = query("-rBG LOOP-MNT")

        then:
        updatedToday =~ /last-modified:  ${today}/
    }

    def "delete object with incorrect created or last-modified succeeds"() {
        given:
        dateTimeProvider.setTime(new DateTime().minusDays(1))

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent()))
        then:
        update =~ /SUCCESS/


        when:
        dateTimeProvider.setTime(new DateTime())
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDate}
                last-modified:  ${currentDate}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        delete =~ /SUCCESS/
    }

    def "delete object with correct created and last-modified succeeds"() {
        given:
        dateTimeProvider.setTime(new DateTime())

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent()))
        then:
        update =~ /SUCCESS/


        when:
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentUtcTime());
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDate}
                last-modified:  ${currentDate}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        delete =~ /SUCCESS/
    }
    def "delete object with no created or last-modified succeeds"() {
        given:
        dateTimeProvider.setTime(new DateTime())

        when:
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                """.stripIndent()))
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
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        delete =~ /SUCCESS/
    }
}
