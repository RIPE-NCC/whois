package net.ripe.db.whois.internal.api.abusec;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.httpserver.JettyBootstrap;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import net.ripe.db.whois.api.rest.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-internal-test.xml"}, inheritLocations = false)
public class AbuseCServiceTestIntegration extends AbstractInternalTest {

    @Autowired private AbuseCService abuseCService;
    @Autowired private RestClient restClient;

    private ClassPathXmlApplicationContext applicationContextRest;
    private Collection<ApplicationService> applicationServicesRest;
    protected DatabaseHelper databaseHelperRest;

    @Before
    public void startRestServer() {
        WhoisProfile.setEndtoend();
        applicationContextRest = new ClassPathXmlApplicationContext("applicationContext-api-test.xml");
        databaseHelperRest = applicationContextRest.getBean(DatabaseHelper.class);
        applicationServicesRest = applicationContextRest.getBeansOfType(ApplicationService.class).values();

        for (final ApplicationService applicationService : applicationServicesRest) {
            applicationService.start();

            if (applicationService instanceof JettyBootstrap) {
                final int port = ((JettyBootstrap) applicationService).getPort();
                final String url = String.format("http://localhost:%d/whois", port);
                restClient.setRestApiUrl(url);
            }
        }

        databaseHelperRest.setup();
        databaseHelperRest.insertApiKey(apiKey, "/api/abusec", "abuse-c automagic creation");

        databaseHelperRest.insertUser(User.createWithPlainTextPassword("agoston", "zoh", ObjectType.values()));
        abuseCService.setOverride("agoston,zoh");
    }

    @After
    public void stopRestServer() {
        for (final ApplicationService applicationService : applicationServicesRest) {
            applicationService.stop(true);
        }

        applicationContextRest.close();
    }

    @Test
    public void post_abusec_role_created_for_organisation_without_abusec() throws IOException {
        databaseHelperRest.addObject("" +
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelperRest.addObject("" +
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        final String response = RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("http://apps.db.ripe.net/search/lookup.html?source=TEST&key=ORG-TOL1-TEST&type=ORGANISATION"));
        final RpslObject organisation = databaseHelperRest.lookupObject(ORGANISATION, "ORG-TOL1-TEST");
        assertThat(organisation.getValueForAttribute(AttributeType.ABUSE_C), is(CIString.ciString("AR1-TEST")));
        final RpslObject role = databaseHelperRest.lookupObject(ROLE, "AR1-TEST");
        assertThat(role.getValueForAttribute(ABUSE_MAILBOX), is(CIString.ciString("email@email.net")));
        assertThat(role.findAttribute(ADDRESS), is(organisation.findAttribute(ADDRESS)));
        assertThat(role.findAttribute(E_MAIL), is(organisation.findAttribute(E_MAIL)));
        assertThat(role.getValueForAttribute(MNT_BY), is(organisation.getValueForAttribute(MNT_REF)));
    }

    @Test
    public void post_organisation_abusec_role_already_exists() throws IOException {
        databaseHelperRest.addObject("" +
                "mntner:        TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");
        databaseHelperRest.addObject("" +
                "role:          Abuse Contact\n" +
                "nic-hdl:       tst-nic\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "source:        TEST");
        databaseHelperRest.addObject("" +
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
            RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
            assertThat(e.getResponse().readEntity(String.class), containsString("abuse@test.net"));
        }
    }

    @Test(expected = ForbiddenException.class)
    public void post_wrong_apikey() throws IOException {
        RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, "DB-WHOIS-totallywrongkey")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
    }

    @Test
    public void post_organisation_without_abusec_role_has_ripe_mntner() {
        databaseHelperRest.addObject("" +
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelperRest.addObject("" +
                "mntner:    RIPE-NCC-HM-MNT\n" +
                "mnt-by:    RIPE-NCC-HM-MNT\n" +
                "source:    TEST");
        databaseHelperRest.addObject("" +
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        final String result = RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(result, is("http://apps.db.ripe.net/search/lookup.html?source=TEST&key=ORG-TOL1-TEST&type=ORGANISATION"));
    }

    @Test
    public void get_abusecontact_exists() {
        databaseHelperRest.addObject("" +
                "mntner:        TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "source:        TEST");
        databaseHelperRest.addObject("" +
                "role:          Abuse Contact\n" +
                "nic-hdl:       tst-nic\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "source:        TEST");
        databaseHelperRest.addObject("" +
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

        final String result = RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);

        assertThat(result, is("abuse@test.net"));
    }


    @Test(expected = NotFoundException.class)
    public void get_abusecontact_not_found() {
        databaseHelperRest.addObject(
                "mntner:    TEST-MNT\n" +
                "mnt-by:    TEST-MNT\n" +
                "source:    TEST");
        databaseHelperRest.addObject(
                "organisation:  ORG-TOL1-TEST\n" +
                "org-name:      Test Organisation Left\n" +
                "org-type:      OTHER\n" +
                "address:       street\n" +
                "e-mail:        some@email.net\n" +
                "mnt-ref:       TEST-MNT\n" +
                "mnt-by:        TEST-MNT\n" +
                "changed:       denis@ripe.net 20121016\n" +
                "source:        TEST");

        RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
    }

    @Test(expected = NotFoundException.class)
    public void get_organisation_not_found() {
        RestTest.target(getPort(), "api/abusec/ORG-TOL1-TEST", null, apiKey)
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
    }
}
