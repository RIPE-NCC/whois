package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TestTimestampsMode;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.glassfish.jersey.uri.UriComponent;
import org.hamcrest.Matchers;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

// TODO: [ES] simplify tests - only test one thing per test method
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
            "upd-to:      updnoreply@ripe.net\n" +
            "notify:      notify@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
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
            "admin-c:   TP1-TEST\n" +
            "abuse-mailbox: abuse@test.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "changed:   dbtest@ripe.net 20120101\n" +
            "source:    TEST\n");

    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private TestTimestampsMode testTimestampsMode;
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
    public void update_using_skiplastmodified_should_not_warn() {
        testTimestampsMode.setTimestampsOff(false);
        databaseHelper.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.ROLE));
        final RpslObject object = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttribute(3, new RpslAttribute("e-mail", "test@ripe.net"))
                .get();
        final String encodedQueryParam = UriComponent.encode("agoston,zoh,reason {skip-last-modified=true}", UriComponent.Type.QUERY_PARAM, false);

        final WhoisResources result = RestTest.target(getPort(), "whois/test/role/TR1-TEST?password=test")
                .queryParam("override", encodedQueryParam)
                .request()
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, object), MediaType.APPLICATION_XML), WhoisResources.class);
        final List<ErrorMessage> messages = result.getErrorMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).toString(), is(new ErrorMessage(UpdateMessages.overrideAuthenticationUsed()).toString()));
    }

    @Test
    public void update_with_same_created_should_not_warn() {
        testTimestampsMode.setTimestampsOff(false);
        final RpslObject object = new RpslObjectBuilder(TEST_ROLE)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttribute(3, new RpslAttribute("e-mail", "test@ripe.net"))
                .replaceAttribute(new RpslAttribute("nic-hdl", "TR1-TEST"), new RpslAttribute("nic-hdl", "WO1-TEST"))
                .get();

        // create the object
        RestTest.target(getPort(), "whois/test/role?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, object), MediaType.APPLICATION_XML), String.class);

        final RpslObject updated = new RpslObjectBuilder(object)
                .addAttribute(3, new RpslAttribute(AttributeType.REMARKS, "updated"))
                .get();

        testDateTimeProvider.setTime(LocalDateTime.parse("2005-12-12T09:13:00"));
        // supplying correct created shouldn't warn
        final WhoisResources result = RestTest.target(getPort(), "whois/test/role/WO1-TEST?password=test")
                .request()
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, updated), MediaType.APPLICATION_XML), WhoisResources.class);

        final List<ErrorMessage> messages = result.getErrorMessages();
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0).toString(), is(new ErrorMessage(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED)).toString()));
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

        final String currentDateTime = FormatHelper.dateTimeToUtcString(testDateTimeProvider.getCurrentDateTimeUtc());
        final RpslObject object = new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", currentDateTime), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", currentDateTime), AttributeType.MNT_BY)
                .get();

        final WhoisResources result = RestTest.target(getPort(), "whois/test/person?password=test")
                .request()
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, object), MediaType.APPLICATION_XML), WhoisResources.class);
        assertThat(result.getErrorMessages(), hasSize(2));

        assertThat(result.getErrorMessages().get(0), is(new ErrorMessage(new Message(Messages.Type.WARNING, "Supplied attribute '%s' has been replaced with a generated value", AttributeType.CREATED))));
        assertThat(result.getErrorMessages().get(1), is(new ErrorMessage(new Message(Messages.Type.WARNING, "Supplied attribute '%s' has been replaced with a generated value", AttributeType.LAST_MODIFIED))));
    }

    @Test
    public void mode_on_create_object_then_mode_off_update_then_check_notification() throws MessagingException, IOException {
        testTimestampsMode.setTimestampsOff(false);

        final RpslObject rpslObject = RpslObject.parse("" +
                "person: Switch Mode\n" +
                "address: masd asdf\n" +
                "phone: +311234678\n" +
                "nic-hdl: auto-1\n" +
                "notify: switchmodenotif@ripe.net\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: sw@ripe.net\n" +
                "source: TEST");

        final String createResult = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObject), MediaType.APPLICATION_JSON), String.class);
        assertThat(createResult, containsString("<attribute name=\"created\""));
        assertThat(createResult, containsString("<attribute name=\"last-modified\""));
        mailSender.getMessage("switchmodenotif@ripe.net");
        testTimestampsMode.setTimestampsOff(true);

        final RpslObject update = new RpslObjectBuilder(rpslObject)
                .replaceAttribute(new RpslAttribute(AttributeType.NIC_HDL, "auto-1"), new RpslAttribute(AttributeType.NIC_HDL, "SM1-TEST"))
                .addAttribute(5, new RpslAttribute(AttributeType.REMARKS, "update"))
                .get();
        final String updateResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_JSON), String.class);

        assertThat(updateResult, not(containsString("<attribute name=\"created\"")));
        assertThat(updateResult, not(containsString("<attribute name=\"last-modified\"")));

        // check notifications
        final MimeMessage notificationMail = mailSender.getMessage("switchmodenotif@ripe.net");
        final String notification = notificationMail.getContent().toString();

        assertThat(notification, not(containsString("-created:")));
        assertThat(notification, not(containsString("-last-modified:")));
    }


    @Test
    public void mode_off_create_object_then_mode_on_query_and_update_and_query_again() throws MessagingException, IOException {
        testTimestampsMode.setTimestampsOff(true);

        final RpslObject rpslObject = RpslObject.parse("" +
                "person: Switch Mode\n" +
                "address: masd asdf\n" +
                "phone: +311234678\n" +
                "nic-hdl: SM1-TEST\n" +
                "notify: switchmodenotif@ripe.net\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: sw@ripe.net\n" +
                "source: TEST");

        final String createResult = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObject), MediaType.APPLICATION_JSON), String.class);
        assertThat(createResult, not(containsString("<attribute name=\"created\"")));
        assertThat(createResult, not(containsString("<attribute name=\"last-modified\"")));

        mailSender.getMessage("switchmodenotif@ripe.net");

        testTimestampsMode.setTimestampsOff(false);

        final String queryResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(queryResult, not(containsString("<attribute name=\"created\"")));
        assertThat(queryResult, not(containsString("<attribute name=\"last-modified\"")));



        final RpslObject update = new RpslObjectBuilder(rpslObject)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "update"))
                .get();

        final String updateResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_JSON), String.class);

        assertThat(updateResult, not(containsString("<attribute name=\"created\"")));
        assertThat(updateResult, containsString("<attribute name=\"last-modified\""));

        // check notifications
        final MimeMessage notificationMail = mailSender.getMessage("switchmodenotif@ripe.net");
        final String notification = notificationMail.getContent().toString();

        assertThat(notification, not(containsString("created:")));
        assertThat(notification, containsString("+last-modified:"));

        final String queryUpdatedResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(queryUpdatedResult, not(containsString("<attribute name=\"created\"")));
        assertThat(queryUpdatedResult, containsString("<attribute name=\"last-modified\""));
    }

    @Test
    public void mode_on_create_object_query_then_mode_off_query_and_update_then_check_notification_and_query() throws MessagingException, IOException {
        testTimestampsMode.setTimestampsOff(false);

        final RpslObject rpslObject = RpslObject.parse("" +
                "person: Switch Mode\n" +
                "address: masd asdf\n" +
                "phone: +311234678\n" +
                "nic-hdl: SM1-TEST\n" +
                "notify: switchmodenotif@ripe.net\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: sw@ripe.net\n" +
                "source: TEST");

        final String createResult = RestTest.target(getPort(), "whois/test/person?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, rpslObject), MediaType.APPLICATION_JSON), String.class);
        assertThat(createResult, containsString("<attribute name=\"created\""));
        assertThat(createResult, containsString("<attribute name=\"last-modified\""));
        mailSender.getMessage("switchmodenotif@ripe.net");


        final String queryResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(queryResult, containsString("<attribute name=\"created\""));
        assertThat(queryResult, containsString("<attribute name=\"last-modified\""));

        testTimestampsMode.setTimestampsOff(true);

        final String queryResultModeOff = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(queryResultModeOff, not(containsString("<attribute name=\"created\"")));
        assertThat(queryResultModeOff, not(containsString("<attribute name=\"last-modified\"")));


        final RpslObject update = new RpslObjectBuilder(rpslObject)
                .addAttributeSorted(new RpslAttribute(AttributeType.REMARKS, "update"))
                .get();
        final String updateResult = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, update), MediaType.APPLICATION_JSON), String.class);

        assertThat(updateResult, not(containsString("<attribute name=\"created\"")));
        assertThat(updateResult, not(containsString("<attribute name=\"last-modified\"")));

        // check notifications
        final MimeMessage notificationMail = mailSender.getMessage("switchmodenotif@ripe.net");
        final String notification = notificationMail.getContent().toString();

        assertThat(notification, not(containsString("-created:")));
        assertThat(notification, not(containsString("-last-modified:")));

        final String queryResultModeOffAfterUpdate = RestTest.target(getPort(), "whois/test/person/SM1-TEST?password=test")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(queryResultModeOffAfterUpdate, not(containsString("<attribute name=\"created\"")));
        assertThat(queryResultModeOffAfterUpdate, not(containsString("<attribute name=\"last-modified\"")));

    }

    @Ignore("TODO: [ES] validate this scenario")
    @Test
    public void delete_object_containing_timestamps_when_timestamps_turned_off() {
        databaseHelper.updateObject(
                "role:      Test Role\n" +
                        "address:   Singel 258\n" +
                        "phone:     +31 6 12345678\n" +
                        "nic-hdl:   TR1-TEST\n" +
                        "admin-c:   TP1-TEST\n" +
                        "abuse-mailbox: abuse@test.net\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "changed:   dbtest@ripe.net 20120101\n" +
                        "created:   2001-02-04T10:00:00Z\n" +
                        "last-modified: 2001-02-04T15:00:00Z\n" +
                        "source:    TEST\n");
        testTimestampsMode.setTimestampsOff(true);

        try {
            RestTest.target(getPort(), "whois/test/role/TR1-TEST")
                    .queryParam("password", "test")
                    .request()
                    .delete(WhoisResources.class);
        } catch (BadRequestException e) {
            // TODO: [ES] fails with "is not a known timestamp" error
            assertThat(e.getResponse().readEntity(String.class), Matchers.containsString("xxxx"));
        }
    }

    @Ignore("TODO: [ES] validate this scenario")
    @Test
    public void delete_object_containing_timestamps_when_timestamps_turned_on() {
        databaseHelper.updateObject(
                "role:      Test Role\n" +
                "address:   Singel 258\n" +
                "phone:     +31 6 12345678\n" +
                "nic-hdl:   TR1-TEST\n" +
                "admin-c:   TP1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "changed:   dbtest@ripe.net 20120101\n" +       // TODO: [ES] attributes MUST be in this order for delete to succeed? cross-check with batch and create/update code
                "created:   2001-02-04T10:00:00Z\n" +
                "last-modified: 2001-02-04T15:00:00Z\n" +
                "source:    TEST\n");
        testTimestampsMode.setTimestampsOff(false);

        RestTest.target(getPort(), "whois/test/role/TR1-TEST")
                .queryParam("password", "test")
                .request()
                .delete(WhoisResources.class);
    }
}
