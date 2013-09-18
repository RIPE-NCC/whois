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

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

        final String response = doPostOrPutRequest("http://localhost:" + getPort(AUDIENCE) + "/api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey", "POST", "email=email@email.net", MediaType.TEXT_PLAIN, 200);

        assertThat(response, is("http://apps.db.ripe.net/whois/lookup/TEST/organisation/ORG-TOL1-TEST.html\n"));

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
        databaseHelper.addObject("role: Abuse Contact\nnic-hdl:tst-nic");
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

        final String response = doPostOrPutRequest("http://localhost:" + getPort(AUDIENCE) + "/api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-abusectestapikey", "POST", "email=email@email.net", MediaType.TEXT_PLAIN, 409);
        assertThat(response, is("This organisation already has an abuse contact\n"));
    }

    @Test
    public void wrong_apikey() throws IOException {
        final String response = doPostOrPutRequest("http://localhost:" + getPort(AUDIENCE) + "/api/abusec/ORG-TOL1-TEST?apiKey=DB-WHOIS-totallywrongkey", "POST", "email=email@email.net", MediaType.TEXT_PLAIN, 403);
        assertThat(response, is("Invalid apiKey\n"));
    }
}
