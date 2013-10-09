package net.ripe.db.whois.api.abusec;

import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.WhoisRestService;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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

@Category(IntegrationTest.class)
public class AbuseCServiceTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;

    @Autowired RpslObjectDao objectDao;
    @Autowired WhoisRestService restService;

    @Before
    public void setup() {
        databaseHelper.insertApiKey("DB-WHOIS-abusectestapikey", "/api/abusec", "abuse-c automagic creation");
    }

    @Test
    public void create_role() throws IOException {
        databaseHelper.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT");
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation: ORG-TOL1-TEST\n" +
                "org-name: Test Organisation Left\n" +
                "org-type: OTHER\n" +
                "address: street\n" +
                "e-mail: some@email.net\n" +
                "mnt-ref: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: denis@ripe.net 20121016\n" +
                "source: test"));

        final String response = createResource(AUDIENCE, "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);

        assertThat(response, containsString("http://rest.db.ripe.net/TEST/organisation/ORG-TOL1-TEST.html"));
        final RpslObject organisation = databaseHelper.lookupObject(ORGANISATION, "ORG-TOL1-TEST");
        assertThat(organisation.getValueForAttribute(AttributeType.ABUSE_C), is(CIString.ciString("AR1-TEST")));
        final RpslObject role = databaseHelper.lookupObject(ROLE, "AR1-TEST");
        assertThat(role.getValueForAttribute(ABUSE_MAILBOX), is(CIString.ciString("email@email.net")));
        assertThat(role.findAttribute(ADDRESS), is(organisation.findAttribute(ADDRESS)));
        assertThat(role.findAttribute(E_MAIL), is(organisation.findAttribute(E_MAIL)));
        assertThat(role.getValueForAttribute(MNT_BY), is(organisation.getValueForAttribute(MNT_REF)));
    }

    @Test
    public void abuseC_already_exists() throws IOException {
        databaseHelper.addObject("mntner: TEST-MNT\nmnt-by:TEST-MNT");
        databaseHelper.addObject("role: Abuse Contact\nnic-hdl:tst-nic\nabuse-mailbox:abuse@test.net");
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation: ORG-TOL1-TEST\n" +
                "org-name: Test Organisation Left\n" +
                "org-type: OTHER\n" +
                "address: street\n" +
                "abuse-c: tst-nic\n" +
                "e-mail: some@email.net\n" +
                "mnt-ref: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: denis@ripe.net 20121016\n" +
                "source: test"));

        try {
            createResource(AUDIENCE, "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey")
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
            fail();
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.CONFLICT.getStatusCode()));
            assertThat(e.getResponse().readEntity(String.class), containsString("abuse@test.net"));
        }
    }

    @Test
    public void wrong_apikey() throws IOException {
        try {
            createResource(AUDIENCE, "api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-totallywrongkey")
                    .request(MediaType.TEXT_PLAIN)
                    .post(Entity.entity("email=email@email.net", MediaType.APPLICATION_FORM_URLENCODED), String.class);
            fail();
        } catch (ForbiddenException e) {
            // expected
        }
    }

    @Override
    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%d/%s", getPort(audience), path));
    }
}
