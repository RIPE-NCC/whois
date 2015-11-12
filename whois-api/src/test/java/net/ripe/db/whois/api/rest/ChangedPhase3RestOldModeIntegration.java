package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class ChangedPhase3RestOldModeIntegration extends AbstractIntegrationTest {


    @Autowired private WhoisObjectMapper whoisObjectMapper;
    @Autowired private MailSenderStub mailSenderStub;
    @Autowired private MaintenanceMode maintenanceMode;

    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Singel 258\n" +
            "phone:     +31 6 12345678\n" +
            "nic-hdl:   TP1-TEST\n" +
            "mnt-by:    TEST-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject TEST_MNTNER = RpslObject.parse("" +
            "mntner:        TEST-MNT\n" +
            "descr:         Test maintainer\n" +
            "admin-c:       TP1-TEST\n" +
            "upd-to:        upd-to@ripe.net\n" +
            "mnt-nfy:       mnt-nfy@ripe.net\n" +
            "auth:          MD5-PW $1$EmukTVYX$Z6fWZT8EAzHoOJTQI6jFJ1  # 123\n" +
            "mnt-by:        TEST-MNT\n" +
            "source:        TEST");

    private static final RpslObject TEST_OBJECT = RpslObject.parse("" +
            "person:  Pauleth Palthen\n" +
            "address: Singel 258\n" +
            "phone:   +31-1234567890\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  TEST-MNT\n" +
            "nic-hdl: PP1-TEST\n" +
            "remarks: remark\n" +
            "source:  TEST\n");

    private static String CHANGED_VALUE = "test@ripe.net 20121016";
    private static boolean MUST_CONTAIN_CHANGED = true;
    private static boolean MUST_NOT_CONTAIN_CHANGED = false;

    @Before
    public void setup() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(TEST_MNTNER);
        databaseHelper.updateObject(TEST_PERSON);
        maintenanceMode.set("FULL,FULL");
    }

    @Test
    public void rest_create_with_changed_old_mode() throws Exception {
        verifyObjectNotExists();

        final RpslObject output = restCreateObject(PERSON_WITH_CHANGED());

        verifyResponse(output, MUST_CONTAIN_CHANGED);
        verifyMail(MUST_CONTAIN_CHANGED);
    }

    @Test
    public void rest_create_without_changed_old_mode() throws Exception {
        verifyObjectNotExists();

        final RpslObject output = restCreateObject(PERSON_WITHOUT_CHANGED());

        verifyResponse(output, MUST_NOT_CONTAIN_CHANGED);
        verifyMail(MUST_NOT_CONTAIN_CHANGED);
    }

    @Test
    public void todo_use_scenario_table_to_drive_tests() {
        for( ChangedPhased3Scenario scenario: ChangedPhased3Scenario.getScenarios() ) {
            // TODO: Needs implementation
            scenario.run();
        }
    }


    private RpslObject PERSON_WITHOUT_CHANGED() {
        return TEST_OBJECT;
    }

    private RpslObject PERSON_WITH_CHANGED() {
        return new RpslObjectBuilder(TEST_OBJECT)
                .addAttributeSorted(new RpslAttribute(AttributeType.CHANGED, CHANGED_VALUE))
                .get();
    }

    private void verifyObjectNotExists() {
        try {
            RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                    .request()
                    .get(WhoisResources.class);
            fail();
        } catch (javax.ws.rs.NotFoundException exc) {
        }
    }

    private void verifyObjectExists() {
        final WhoisResources whoisResources = RestTest.target(getPort(), "whois/test/person/PP1-TEST")
                .request()
                .get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(),hasSize(1));
        assertThat(whoisResources.getErrorMessages(),hasSize(0));
    }

    private RpslObject restCreateObject(final RpslObject obj) {
        try {
            final WhoisResources resp = RestTest.target(getPort(), "whois/test/person?password=123")
                    .request()
                    .post(Entity.entity(whoisObjectMapper.mapRpslObjects(FormattedClientAttributeMapper.class, obj), MediaType.APPLICATION_XML), WhoisResources.class);
            return whoisObjectMapper.map(resp.getWhoisObjects().get(0), FormattedClientAttributeMapper.class);
        } catch( ClientErrorException exc) {
//            WhoisResources whoisResources = exc.getResponse().readEntity(WhoisResources.class);
//            for( ErrorMessage em: whoisResources.getErrorMessages() ) {
//                System.err.println("restCreateObject:" + em.toString() );
//            }
            throw exc;
        }
    }

    private void verifyResponse(final RpslObject obj, final boolean mustContainChanged) {
        if( mustContainChanged ) {
            assertThat(obj.findAttribute(AttributeType.CHANGED).getValue(), is(CHANGED_VALUE));
        } else {
            assertThat(obj.containsAttribute(AttributeType.CHANGED), is(false));
        }
    }

    private void verifyMail(final boolean mustContainChanged) throws MessagingException, IOException {
        final String message = mailSenderStub.getMessage("mnt-nfy@ripe.net").getContent().toString();

        if( mustContainChanged ) {
            assertThat(message, containsString(CHANGED_VALUE));
        } else {
            assertThat(message, not(containsString(CHANGED_VALUE)));
        }
        assertFalse(mailSenderStub.anyMoreMessages());

    }
}
