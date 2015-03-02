package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.syncupdate.SyncUpdateBuilder;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TestTimestampsMode;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.hamcrest.Matchers;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.common.rpsl.AttributeType.CREATED;
import static net.ripe.db.whois.common.rpsl.AttributeType.LAST_MODIFIED;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class TimestampsOffRestServiceTestIntegration extends AbstractIntegrationTest {

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_ROLE = RpslObject.parse("" +
            "role:      Test Role\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TR1-TEST\n" +
            "admin-c:   TR1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestTimestampsMode testTimestampsMode;
    @Autowired private MailUpdatesTestSupport mailUpdatesTestSupport;
    @Autowired private MailSenderStub mailSender;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject("role: Test Role\nnic-hdl: TR1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.updateObject(TEST_ROLE);
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void mode_off_created_last_modified_raise_warnings() throws Exception {
        testTimestampsMode.setTimestampsOff(true);

        final RpslObject object = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get();

        try {
            RestTest.target(getPort(), "whois/test/person?password=test")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, object), MediaType.APPLICATION_XML), WhoisResources.class);
        } catch (BadRequestException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            final List<ErrorMessage> errorMessages = whoisResources.getErrorMessages();
            assertThat(errorMessages, hasSize(2));
            assertThat(errorMessages.get(0).toString(), is(new ErrorMessage(new Message(Messages.Type.ERROR, "\"created\" is not a known RPSL attribute")).toString()));
            assertThat(errorMessages.get(1).toString(), is(new ErrorMessage(new Message(Messages.Type.ERROR, "\"last-modified\" is not a known RPSL attribute")).toString()));
        }
    }

    @Test
    public void mode_off_timestamps_in_db_lookup() {
        testTimestampsMode.setTimestampsOff(true);

        databaseHelper.addObject(new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get());

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), Matchers.empty());
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    //TODO TP this test should be in whoisrestservicetestintegration
    public void mode_on_timestamps_in_db_lookup() {
        testTimestampsMode.setTimestampsOff(false);

        databaseHelper.addObject(new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get());

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST?unfiltered").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), empty());
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);

        assertThat(whoisObject.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("e-mail", "noreply@ripe.net"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("changed", "noreply@ripe.net 20120101"),
                new Attribute("source", "TEST", null, null, null)));
    }

    @Test
    public void mode_off_timestamps_in_db_search() {
        testTimestampsMode.setTimestampsOff(true);

        final RpslObject pp1 = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get();

        final RpslObject pp2 = new RpslObjectBuilder(pp1).replaceAttribute(
                new RpslAttribute(AttributeType.NIC_HDL, "PP1-TEST"), new RpslAttribute(AttributeType.NIC_HDL, "PP2-TEST")).get();

        databaseHelper.addObjects(pp1, pp2);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=Pauleth&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject obj1 = whoisResources.getWhoisObjects().get(0);
        assertThat(obj1.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));

        final WhoisObject obj2 = whoisResources.getWhoisObjects().get(1);
        assertThat(obj2.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("nic-hdl", "PP2-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    @Test
    //TODO TP this test should be in whoisrestservicetestintegration
    public void mode_on_timestamps_in_db_search() {
        testTimestampsMode.setTimestampsOff(false);

        final RpslObject pp1 = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get();

        final RpslObject pp2 = new RpslObjectBuilder(pp1).replaceAttribute(
                new RpslAttribute(AttributeType.NIC_HDL, "PP1-TEST"), new RpslAttribute(AttributeType.NIC_HDL, "PP2-TEST")).get();

        databaseHelper.addObjects(pp1, pp2);

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/search?query-string=Pauleth&source=TEST")
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(2));

        final WhoisObject obj1 = whoisResources.getWhoisObjects().get(0);
        assertThat(obj1.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("nic-hdl", "PP1-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST", "Filtered", null, null)));

        final WhoisObject obj2 = whoisResources.getWhoisObjects().get(1);
        assertThat(obj2.getAttributes(), containsInAnyOrder(
                new Attribute("person", "Pauleth Palthen"),
                new Attribute("address", "Singel 258"),
                new Attribute("phone", "+31-1234567890"),
                new Attribute("nic-hdl", "PP2-TEST"),
                new Attribute("mnt-by", "OWNER-MNT", null, "mntner", new Link("locator", "http://rest-test.db.ripe.net/test/mntner/OWNER-MNT")),
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z"),
                new Attribute("source", "TEST", "Filtered", null, null)));
    }

    public void explicit_mode_on_allows_created_last_modified() {
        testTimestampsMode.setTimestampsOff(false);

        final String currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(testDateTimeProvider.getCurrentDateTimeUtc());

        final RpslObject object = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", currentDate), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDate), AttributeType.MNT_BY)
                .get();

        final WhoisResources result = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, object), MediaType.APPLICATION_XML), WhoisResources.class);
        assertThat(result.getErrorMessages(), hasSize(2));

        assertThat(result.getErrorMessages().get(0), is(new ErrorMessage(new Message(Messages.Type.WARNING, "Supplied attribute '%s' has been replaced with a generated value", CREATED))));
        assertThat(result.getErrorMessages().get(1), is(new ErrorMessage(new Message(Messages.Type.WARNING, "Supplied attribute '%s' has been replaced with a generated value", LAST_MODIFIED))));
    }

    @Test
    public void mode_off_syncupdates_created_last_modified_raises_warnings() {
        testTimestampsMode.setTimestampsOff(true);

        final String currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(testDateTimeProvider.getCurrentDateTimeUtc());

        final RpslObject object = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", currentDate), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDate), AttributeType.MNT_BY)
                .get();

        final String result = new SyncUpdateBuilder()
                .setHost("localhost")
                .setPort(getPort())
                .setSource("TEST")
                .setData(object.toString() + "\npassword: test\n")
                .setHelp(false)
                .setDiff(false)
                .setNew(true)
                .setRedirect(false)
                .build()
                .post();

        assertThat(result, containsString("" +
                "Create FAILED: [person] PP1-TEST   Pauleth Palthen"));
        assertThat(result, containsString(String.format("" +
                "last-modified:  %s\n" +
                "***Error:   \"last-modified\" is not a known RPSL attribute\n" +
                "created:        %s\n" +
                "***Error:   \"created\" is not a known RPSL attribute", currentDate, currentDate)));
    }

    @Test
    public void mode_off_mailcreate_created_last_modified_raises_warnings() throws MessagingException, IOException {
        testTimestampsMode.setTimestampsOff(true);

        final String currentDate = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(testDateTimeProvider.getCurrentDateTimeUtc());

        final RpslObject object = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", currentDate), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDate), AttributeType.MNT_BY)
                .get();

        mailUpdatesTestSupport.insert("" +
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
