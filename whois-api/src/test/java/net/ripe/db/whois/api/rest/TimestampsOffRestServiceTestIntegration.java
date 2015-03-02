package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TestTimestampsMode;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class TimestampsOffRestServiceTestIntegration extends AbstractIntegrationTest {

    private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "remarks:   remark\n" +
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
            assertThat(errorMessages, hasItems(new ErrorMessage(ValidationMessages.unknownAttribute("created"))));
            assertThat(errorMessages, hasItems(new ErrorMessage(ValidationMessages.unknownAttribute("last-modified"))));
        }
    }

    @Test
    public void lookup_person_timestamps_in_db_timestamps_switched_off() {
        testTimestampsMode.setTimestampsOff(true);

        databaseHelper.addObject(new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get());

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);


        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), not(hasItems(
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "last-modified"))));
    }

    @Test
    //TODO TP this test should be in whoisrestservicetestintegration
    public void lookup_person_timestamps_in_db_timestamps_switched_on() {
        testTimestampsMode.setTimestampsOff(false);

        databaseHelper.addObject(new RpslObjectBuilder(PAULETH_PALTHEN)
                .addAttributeAfter(new RpslAttribute("created", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .addAttributeAfter(new RpslAttribute("last-modified", "2001-02-04T17:00:00Z"), AttributeType.MNT_BY)
                .get());

        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST").request().get(WhoisResources.class);

        assertThat(whoisResources.getErrorMessages(), is(empty()));
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));
        final WhoisObject whoisObject = whoisResources.getWhoisObjects().get(0);
        assertThat(whoisObject.getAttributes(), hasItems(
                new Attribute("created", "2001-02-04T17:00:00Z"),
                new Attribute("last-modified", "2001-02-04T17:00:00Z")));
    }
}
