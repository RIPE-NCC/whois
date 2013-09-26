package net.ripe.db.whois.api.whois;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.domain.AbuseResources;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class AbuseFinderTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Before
    public void setup() {
        databaseHelper.addObject(
                "person: Test Person\n" +
                "nic-hdl: TP1-TEST");
        databaseHelper.addObject(
                "role: Test Role\n" +
                "nic-hdl: TR1-TEST");
        databaseHelper.addObject(
                "mntner:      OWNER-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     TP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:      OWNER-MNT\n" +
                "referral-by: OWNER-MNT\n" +
                "changed:     dbtest@ripe.net 20120101\n" +
                "source:      TEST");
        databaseHelper.updateObject(
                "person:  Test Person\n" +
                "address: Singel 258\n" +
                "phone:   +31 6 12345678\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST\n");
        databaseHelper.updateObject(
                "role:      Test Role\n" +
                "address:   Singel 258\n" +
                "phone:     +31 6 12345678\n" +
                "nic-hdl:   TR1-TEST\n" +
                "admin-c:   TR1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:    OWNER-MNT\n" +
                "changed:   dbtest@ripe.net 20120101\n" +
                "source:    TEST\n");
    }

    @Test
    public void abusec_inetnum_found_json() throws IOException {
        databaseHelper.addObject("" +
                "organisation: ORG-OT1-TEST\n" +
                "org-type: OTHER\n" +
                "abuse-c: TR1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");
        databaseHelper.addObject("" +
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "org: ORG-OT1-TEST\n" +
                "country: DK\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: SUB-ALLOCATED PA\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: TEST");
        ipTreeUpdater.rebuild();

        final String result = createResource(AUDIENCE, "whois/abuse-finder/test/2a00:1f78::fffe/48")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertThat(result, is("" +
                "{\n" +
                "  \"service\" : \"abuse-finder\",\n" +
                "  \"link\" : {\n" +
                "    \"xlink:type\" : \"locator\",\n" +
                "    \"xlink:href\" : \"http://rest.db.ripe.net/abuse-finder/test/2a00:1f78::fffe/48\"\n" +
                "  },\n" +
                "  \"parameters\" : {\n" +
                "    \"primary-key\" : {\n" +
                "      \"value\" : \"2a00:1f78::fffe/48\"\n" +
                "    },\n" +
                "    \"sources\" : {\n" +
                "      \"source\" : [ {\n" +
                "        \"name\" : \"TEST\",\n" +
                "        \"id\" : \"test\"\n" +
                "      } ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"abuse-contacts\" : {\n" +
                "    \"email\" : \"abuse@test.net\"\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"xlink:type\" : \"locator\",\n" +
                "    \"xlink:href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void abusec_inetnum_found_xml() {
        databaseHelper.addObject("" +
                "organisation: ORG-OT1-TEST\n" +
                "org-type: OTHER\n" +
                "abuse-c: TR1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: TEST");
        databaseHelper.addObject("" +
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "org: ORG-OT1-TEST\n" +
                "country: DK\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: SUB-ALLOCATED PA\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: TEST");
        ipTreeUpdater.rebuild();

        final String result = createResource(AUDIENCE, "whois/abuse-finder/test/2a00:1f78::fffe/48")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);
        final String readable = Joiner.on(">\n").join(Splitter.on(">").split(result)).trim();

        assertThat(readable, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<abuse-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\" service=\"abuse-finder\">\n" +
                "<link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/abuse-finder/test/2a00:1f78::fffe/48\"/>\n" +
                "<parameters>\n" +
                "<primary-key value=\"2a00:1f78::fffe/48\"/>\n" +
                "<sources>\n" +
                "<source name=\"TEST\" id=\"test\"/>\n" +
                "</sources>\n" +
                "</parameters>\n" +
                "<abuse-contacts email=\"abuse@test.net\"/>\n" +
                "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
                "</abuse-resources>"));
    }

    @Test
    public void abusec_autnum_found() {
        databaseHelper.addObject("" +
                "organisation: ORG-OT1-TEST\n" +
                "org-type: OTHER\n" +
                "abuse-c: TR1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: test");
        databaseHelper.addObject("" +
                "aut-num: AS333\n" +
                "as-name: Test-User-1\n" +
                "descr: some description\n" +
                "org: ORG-OT1-TEST\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: test");

        final AbuseResources result = createResource(AUDIENCE, "whois/abuse-finder/test/AS333")
                .request(MediaType.APPLICATION_XML)
                .get(AbuseResources.class);

        assertThat(result.getAbuseContact().getEmail(), is("abuse@test.net"));
        assertThat(result.getParameters().getSources().getSources().get(0).getId(), is("test"));
    }

    @Test
    public void abuse_contact_not_found() {
        databaseHelper.addObject("" +
                "organisation: ORG-OT1-TEST\n" +
                "org-type: OTHER\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: test");
        databaseHelper.addObject("" +
                "aut-num: AS333\n" +
                "as-name: Test-User-1\n" +
                "descr: some description\n" +
                "org: ORG-OT1-TEST\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: test");

        try {
            createResource(AUDIENCE, "whois/abuse-finder/test/AS333")
                .request(MediaType.APPLICATION_XML)
                .get(AbuseResources.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void abuse_object_not_found() {
        try {
            createResource(AUDIENCE, "whois/abuse-finder/test/AS333")
                    .request(MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void abuse_explicit_json() {
        databaseHelper.addObject("" +
                "organisation: ORG-OT1-TEST\n" +
                "org-type: OTHER\n" +
                "abuse-c: TR1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "source: test");
        databaseHelper.addObject("" +
                "aut-num: AS333\n" +
                "as-name: Test-User-1\n" +
                "descr: some description\n" +
                "org: ORG-OT1-TEST\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "mnt-by: OWNER-MNT\n" +
                "changed: org@ripe.net 20120505\n" +
                "source: test");

        final String result = createResource(AUDIENCE, "whois/abuse-finder/test/AS333.json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(result, containsString("" +
                "{\n" +
                "  \"service\" : \"abuse-finder\",\n" +
                "  \"link\" : {\n" +
                "    \"xlink:type\" : \"locator\",\n" +
                "    \"xlink:href\" : \"http://rest.db.ripe.net/abuse-finder/test/AS333\"\n" +
                "  },"));
    }

    // helper methods

    @Override
    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%d/%s", getPort(audience), path));
    }
}
