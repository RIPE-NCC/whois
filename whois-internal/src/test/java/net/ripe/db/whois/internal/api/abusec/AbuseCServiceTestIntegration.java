package net.ripe.db.whois.internal.api.abusec;

import net.ripe.db.whois.api.RestClient;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

// TODO: [AH] fix test class
@Ignore
@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"}, inheritLocations = false)
public class AbuseCServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setup() {
        databaseHelper.insertApiKey(apiKey, "/api/abusec", "abuse-c automagic creation");
    }

    @Test
    public void post_abusec_role_created_for_organisation_without_abusec() throws IOException {
        databaseHelper.addObject(
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        final String response = RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("http://apps.db.ripe.net/search/lookup.html?source=TEST&key=ORG-TOL1-TEST&type=ORGANISATION"));
        final RpslObject organisation = databaseHelper.lookupObject(ORGANISATION, "ORG-TOL1-TEST");
        assertThat(organisation.getValueForAttribute(AttributeType.ABUSE_C), is(CIString.ciString("AR1-TEST")));
        final RpslObject role = databaseHelper.lookupObject(ROLE, "AR1-TEST");
        assertThat(role.getValueForAttribute(ABUSE_MAILBOX), is(CIString.ciString("email@email.net")));
        assertThat(role.findAttribute(ADDRESS), is(organisation.findAttribute(ADDRESS)));
        assertThat(role.findAttribute(E_MAIL), is(organisation.findAttribute(E_MAIL)));
        assertThat(role.getValueForAttribute(MNT_BY), is(organisation.getValueForAttribute(MNT_REF)));
    }

    @Test
    public void post_organisation_abusec_role_already_exists() throws IOException {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "role:          Abuse Contact\n" +
                "nic-hdl:       tst-nic\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "abuse-c:       tst-nic\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        try {
            RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
            assertThat(e.getResponse().readEntity(String.class), containsString("abuse@test.net"));
        }
    }

    @Test
    public void post_wrong_apikey() throws IOException {
        try {
            RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-totallywrongkey")
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
            fail();
        } catch (ForbiddenException e) {
            // expected
        }
    }

    @Test
    public void post_organisation_without_abusec_role_has_ripe_mntner() {
        databaseHelper.addObject(
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "mntner:    RIPE-NCC-HM-MNT\n" +
                "mnt-by:    RIPE-NCC-HM-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        final String result = RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(result, is("http://apps.db.ripe.net/search/lookup.html?source=TEST&key=ORG-TOL1-TEST&type=ORGANISATION"));
    }

    @Test
    public void get_abusecontact_exists() {
        databaseHelper.addObject(
                "mntner:        TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "role:          Abuse Contact\n" +
                "nic-hdl:       tst-nic\n" +
                "abuse-mailbox: abuse@test.net");
        databaseHelper.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "abuse-c:       tst-nic\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        final String result = RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(result, is("abuse@test.net"));
    }


    @Test
    public void get_abusecontact_not_found() {
        databaseHelper.addObject(
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        try {
            RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                    .request(MediaType.TEXT_PLAIN)
                    .get(String.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void get_organisation_not_found() {
        try {
            RestClient.target(getPort(), "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }
}
