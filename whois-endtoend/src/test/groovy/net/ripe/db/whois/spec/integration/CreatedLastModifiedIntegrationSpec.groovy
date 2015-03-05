package net.ripe.db.whois.spec.integration

import net.ripe.db.whois.common.IntegrationTest
import net.ripe.db.whois.common.TestDateTimeProvider
import net.ripe.db.whois.common.rpsl.*
import net.ripe.db.whois.spec.domain.SyncUpdate
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat

import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

import static org.hamcrest.core.StringContains.containsString
import static org.junit.Assert.assertThat

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
    static TestTimestampsMode testTimestampsMode;

    def setupSpec() {
        dateTimeProvider = getApplicationContext().getBean(net.ripe.db.whois.common.TestDateTimeProvider.class);
        testTimestampsMode = getApplicationContext().getBean(net.ripe.db.whois.common.rpsl.TestTimestampsMode.class);
    }


    def "create object with created and last-modified generates new values"() {
        given:
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());

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
        dateTimeProvider.setTime(new LocalDateTime().minusDays(1))
        def yesterday = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());

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
        dateTimeProvider.setTime(new LocalDateTime())

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
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.currentDateTimeUtc);
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
        dateTimeProvider.setTime(new LocalDateTime().minusDays(1))
        def yesterday = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());
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
        dateTimeProvider.setTime(new LocalDateTime())
        def today = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());

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
        dateTimeProvider.setTime(new LocalDateTime().minusDays(1))

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
        dateTimeProvider.setTime(new LocalDateTime())
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());
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
        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(dateTimeProvider.getCurrentDateTimeUtc());
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

    def mode_off_syncupdates_created_last_modified_raises_warnings() {
        testTimestampsMode.setTimestampsOff(true);

        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(testDateTimeProvider.getCurrentDateTimeUtc());
        def pauleth = RpslObject.parse("person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")
        def object = new RpslObjectBuilder(pauleth)
                .addAttributeAfter(new RpslAttribute("created", currentDate), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDate), AttributeType.MNT_BY)
                .get();

        def result = syncUpdate(new SyncUpdate(data: object.toString() + "\npassword: test\n"))

        assertThat(result, containsString("" +
                "Create FAILED: [person] PP1-TEST   Pauleth Palthen"));
        assertThat(result, containsString(String.format("" +
                "last-modified:  %s\n" +
                "***Error:   \"last-modified\" is not a known RPSL attribute\n" +
                "created:        %s\n" +
                "***Error:   \"created\" is not a known RPSL attribute", currentDate, currentDate)));
    }

    def mode_off_mailcreate_created_last_modified_raises_warnings() throws MessagingException, IOException {
        testTimestampsMode.setTimestampsOff(true);

        def currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(testDateTimeProvider.getCurrentDateTimeUtc());

        def pauleth = RpslObject.parse("person:    Pauleth Palthen\n" +
                "address:   Singel 258\n" +
                "phone:     +31-1234567890\n" +
                "e-mail:    noreply@ripe.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "nic-hdl:   PP1-TEST\n" +
                "changed:   noreply@ripe.net 20120101\n" +
                "source:    TEST\n")
        def object = new RpslObjectBuilder(pauleth)
                .addAttributeAfter(new RpslAttribute("created", currentDate), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDate), AttributeType.MNT_BY)
                .get();

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
                object.toString() + "\npassword: test\n");
        final MimeMessage message = mailSender.getMessage("noreply@ripe.net");
        final String result = message.getContent().toString();

        assertThat(result, containsString("" +
                "Create FAILED: [person] PP1-TEST   Pauleth Palthen"));
        assertThat(result, containsString(String.format("" +
                "last-modified:  %s\n" +
                "***Error:   \"last-modified\" is not a known RPSL attribute\n" +
                "created:        %s\n" +
                "***Error:   \"created\" is not a known RPSL attribute", currentDate, currentDate)));
    }
}
