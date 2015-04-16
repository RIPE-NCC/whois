package net.ripe.db.whois.spec.integration
import net.ripe.db.whois.common.FormatHelper
import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.rpsl.RpslObject
import net.ripe.db.whois.common.rpsl.TestTimestampsMode
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.joda.time.LocalDateTime

import javax.mail.internet.MimeMessage

@org.junit.experimental.categories.Category(IntegrationTest.class)
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

    static TestTimestampsMode testTimestampsMode;

    def setupSpec() {
        testTimestampsMode = getApplicationContext().getBean(net.ripe.db.whois.common.rpsl.TestTimestampsMode.class);
    }

    def "create object with created and last-modified generates new values"() {
        given:
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

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
        response !=~ /Warning: Supplied attribute 'created' has been replaced with a generated
            value/
        response !=~ /Warning: Supplied attribute 'last-modified' has been replaced with a
            generated value/

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
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

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
        updated =~/created:        ${yesterdayDateTime}/
    }

    def "modify object without created generates last-modified only"() {
        given:
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
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
        updated =~ /last-modified:  ${currentDateTime}/
    }

    def "modify object with last-modified updates last-modified"() {
        given:
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
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
        updated =~ /last-modified:  ${yesterdayDateTime}/

        when:
        setTime(LocalDateTime.now())
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

        def updateToday = syncUpdate(new SyncUpdate(data:
                        "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: today\n" +
                        "mnt-by:  LOOP-MNT\n" +
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
        updatedToday =~ /last-modified:  ${currentDateTime}/
    }

    def "mode off: modify object with existimg created and last-modified"() {
        given:
        testTimestampsMode.setTimestampsOff(false);
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "created:       2012-05-03T11:23:66Z\n" +
                "last-modified: 2012-05-03T11:23:66Z\n" +
                "source:  TEST")

        when:
        testTimestampsMode.setTimestampsOff(true);

        def updateYesterday = syncUpdate(new SyncUpdate(data:
                "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: yesterday\n" +
                        "mnt-by:  LOOP-MNT\n" +
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
        updated !=~ /last-modified:  ${yesterdayDateTime}/

    }

    def "mode off: modify object without existimg created and last-modified"() {
        given:
        testTimestampsMode.setTimestampsOff(false);
        setTime(LocalDateTime.now().minusDays(1))
        def yesterdayDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        databaseHelper.addObject("" +
                "mntner:  LOOP-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  LOOP-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW \$1\$fU9ZMQN9\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST")

        when:
        testTimestampsMode.setTimestampsOff(true);

        def updateYesterday = syncUpdate(new SyncUpdate(data:
                "mntner:  LOOP-MNT\n" +
                        "descr:   description\n" +
                        "admin-c: TEST-RIPE\n" +
                        "remarks: yesterday\n" +
                        "mnt-by:  LOOP-MNT\n" +
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
        updated !=~ /last-modified:  ${yesterdayDateTime}/

    }


    def "delete object with incorrect created or last-modified succeeds"() {

        given:
        testTimestampsMode.setTimestampsOff(false);
        setTime(LocalDateTime.now().minusDays(1))

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
        setTime(LocalDateTime.now())
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        delete =~ /SUCCESS/
    }

    def "delete object with correct created and last-modified succeeds"() {
        given:
        testTimestampsMode.setTimestampsOff(false);

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
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        def delete = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        delete =~ /SUCCESS/
    }

    def "mode off: delete object with created and last-modified present"() {
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

        when:
        testTimestampsMode.setTimestampsOff(false);
        def update = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                """.stripIndent()))
        then:
        update =~ /SUCCESS/

        when:
        testTimestampsMode.setTimestampsOff(true);
        def result = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        result =~ /Delete FAILED: \[person\] TP3-TEST   Test Person/
        result =~ /created:        ${currentDateTime}
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  ${currentDateTime}
\*\*\*Error:   "last-modified" is not a known RPSL attribute/

    }

    def "mode off: delete object with created and last-modified not present"() {
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

        when:
        testTimestampsMode.setTimestampsOff(false);
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
        testTimestampsMode.setTimestampsOff(true);
        def result = syncUpdate(new SyncUpdate(data: """\
                person:        Test Person
                address:       Singel 258
                phone:         +3112346
                nic-hdl:       TP3-TEST
                changed:       admin@test.com 20120505
                mnt-by:        TST-MNT
                created:        ${currentDateTime}
                last-modified:  ${currentDateTime}
                source:        TEST
                password: update
                delete:   reason
                """.stripIndent()))

        then:
        result =~ /Delete FAILED: \[person\] TP3-TEST   Test Person/
        result =~ /created:        ${currentDateTime}
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  ${currentDateTime}
\*\*\*Error:   "last-modified" is not a known RPSL attribute/

    }

    def "delete object with no created or last-modified succeeds"() {
        given:
        testTimestampsMode.setTimestampsOff(false);

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

    def "mode off: create using syncupdates raises errors"() {
        given:
        testTimestampsMode.setTimestampsOff(true);

        when:
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)
        def object = RpslObject.parse("person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "created:  ${currentDateTime}\n" +
                "last-modified:  ${currentDateTime}\n" +
                "source:    TEST\n")

        def result = syncUpdate(new SyncUpdate(data: object.toString() + "\npassword: test\n"))

        then:
        println(result)
        result =~ /Create FAILED: \[person\] PP1-TEST   Pauleth Palthen/
        result =~ /created:        ${currentDateTime}
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  ${currentDateTime}
\*\*\*Error:   "last-modified" is not a known RPSL attribute/
    }

    def "mode off: create using mail raises errors"() {
        given:
        testTimestampsMode.setTimestampsOff(true);

        when:
        def currentDateTime = FormatHelper.dateTimeToUtcString(whoisFixture.testDateTimeProvider.currentDateTimeUtc)

        def object = RpslObject.parse(
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    TST-MNT\n" +
                "created:   ${currentDateTime}\n" +
                "last-modified:   ${currentDateTime}\n" +
                "nic-hdl:   PP1-TEST\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")

                send(
                "Date: Fri, 4 Jan 2013 15:29:59 +0100\n" +
                "From: noreply@ripe.net\n" +
                "To: test-dbm@ripe.net\n" +
                "Subject: NEW\n" +
                "Message-Id: <9BC09C2C-D017-4C4A-9A22-1F4F530F1881@ripe.net>\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" +
                "MIME-Version: 1.0\n" +
                "Content-Transfer-Encoding: UTF-8\n" +
                "\n" +
                object.toString() + "\npassword: update\n");
        final MimeMessage message = mailSender.getMessage("noreply@ripe.net");
        final String result = message.getContent().toString();

        then:
        result =~ /created:        ${currentDateTime}
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  ${currentDateTime}
\*\*\*Error:   "last-modified" is not a known RPSL attribute/
    }


    def "mode off: create with invalid attributes"() {
        given:
        testTimestampsMode.setTimestampsOff(true);

        when:
        def object = RpslObject.parse(
                "person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    TST-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "invalid:   some text\n" +
                "inv-again: more text\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")

        def result = syncUpdate(new SyncUpdate(data: object.toString() + "\npassword: update\n"))

        then:
        result =~ /invalid:        some text
\*\*\*Error:   "invalid" is not a known RPSL attribute
inv-again:      more text
\*\*\*Error:   "inv-again" is not a known RPSL attribute/

        when:
        def comparison = RpslObject.parse(
                "person:    Created LastModified\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    TST-MNT\n" +
                "nic-hdl:   CLM1-TEST\n" +
                "invalid:   some text\n" +
                "created:   should show up\n" +
                "last-modified:   should also show up\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")

        def comparisonResult = syncUpdate(new SyncUpdate(data: comparison.toString() + "\npassword: update\n"))

        then:
        comparisonResult =~ /invalid:        some text
\*\*\*Error:   "invalid" is not a known RPSL attribute
created:        should show up
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  should also show up
\*\*\*Error:   "last-modified" is not a known RPSL attribute/

        when:
        def onlyCreatedLastModified = RpslObject.parse(
                "person:    Created LastModified\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    TST-MNT\n" +
                "nic-hdl:   CLM1-TEST\n" +
                "created:   should show up only once\n" +
                "last-modified:   should also show up only once\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")

        def response = syncUpdate(new SyncUpdate(data: onlyCreatedLastModified.toString() + "\npassword: update\n"))

        then:
        response =~ /created:        should show up only once
\*\*\*Error:   "created" is not a known RPSL attribute
last-modified:  should also show up only once
\*\*\*Error:   "last-modified" is not a known RPSL attribute/
    }
}
