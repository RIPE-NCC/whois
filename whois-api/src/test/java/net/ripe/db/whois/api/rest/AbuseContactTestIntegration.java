package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AbuseContactTestIntegration extends AbstractIntegrationTest {

    @Before
    public void setup() {
        databaseHelper.addObject(
                "person:        Test Person\n" +
                "nic-hdl:       TP1-TEST");
        databaseHelper.addObject(
                "role:          Test Role\n" +
                "nic-hdl:       TR1-TEST");
        databaseHelper.addObject(
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");
        databaseHelper.updateObject(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.updateObject(
                "role:          Test Role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TR1-TEST\n" +
                "admin-c:       TR1-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject(
                "inet6num:     ::/0\n" +
                "netname:      IANA-BLK\n" +
                "descr:        The whole IPv6 address space\n" +
                "country:      EU\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED-BY-RIR\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      ripe@test.net 20120505\n" +
                "remarks:      This network in not allocated.\n" +
                "source:       TEST");
        databaseHelper.addObject(
                "inetnum:      0.0.0.0 - 255.255.255.255\n" +
                "netname:      IANA-BLK\n" +
                "descr:        The whole IPv4 address space\n" +
                "country:      NL\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED UNSPECIFIED\n" +
                "remarks:      The country is really worldwide.\n" +
                "mnt-by:       OWNER-MNT\n" +
                "mnt-lower:    OWNER-MNT\n" +
                "mnt-routes:   OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();
    }

    // inetnum

    @Test
    public void lookup_inetnum_exact_match_abuse_contact_found() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inetnum:       193.0.0.0 - 193.0.0.255\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "country:       DK\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "whois/abuse-contact/193.0.0.0 - 193.0.0.255")
                .request()
                .get(String.class);

        assertThat(result, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<abuse-resources service=\"abuse-contact\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/abuse-contact/193.0.0.0 - 193.0.0.255\"/>\n" +
                "    <parameters>\n" +
                "        <primary-key value=\"193.0.0.0 - 193.0.0.255\"/>\n" +
                "    </parameters>\n" +
                "    <abuse-contacts email=\"abuse@test.net\"/>\n" +
                "    <terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
                "</abuse-resources>"));
    }

    @Test
    public void lookup_inetnum_child_address_abuse_contact_found() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inetnum:       193.0.0.0 - 193.0.0.255\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "country:       DK\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "whois/abuse-contact/193.0.0.1")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, is("" +
                "{\n" +
                "  \"service\" : \"abuse-contact\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/abuse-contact/193.0.0.1\"\n" +
                "  },\n" +
                "  \"parameters\" : {\n" +
                "    \"primary-key\" : {\n" +
                "      \"value\" : \"193.0.0.0 - 193.0.0.255\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"abuse-contacts\" : {\n" +
                "    \"email\" : \"abuse@test.net\"\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void lookup_inetnum_abuse_contact_not_found() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inetnum:       193.0.0.0 - 193.0.0.255\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "country:       DK\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "whois/abuse-contact/193.0.0.1")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, is("" +
                "{\n" +
                "  \"service\" : \"abuse-contact\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/abuse-contact/193.0.0.1\"\n" +
                "  },\n" +
                "  \"parameters\" : {\n" +
                "    \"primary-key\" : {\n" +
                "      \"value\" : \"193.0.0.0 - 193.0.0.255\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"abuse-contacts\" : {\n" +
                "    \"email\" : \"\"\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void lookup_inetnum_not_found_xml() {
        try {
            RestTest.target(getPort(), "whois/abuse-contact/193.0.1.2")
                    .request(MediaType.APPLICATION_XML)
                    .get(String.class);
        } catch (NotFoundException e) {
            final String responseEntity = e.getResponse().readEntity(String.class);
            assertThat(responseEntity, is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><abuse-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\"><message>No abuse contact found for 193.0.1.2</message></abuse-resources>"));
        }
    }

    // inet6num

    @Test
    public void lookup_inet6num_abuse_contact_found_accept_json() throws IOException {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inet6num:      2a00:1f78::/32\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "country:       DK\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "whois/abuse-contact/2a00:1f78::/32")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        assertThat(result, is("" +
                "{\n" +
                "  \"service\" : \"abuse-contact\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/abuse-contact/2a00:1f78::/32\"\n" +
                "  },\n" +
                "  \"parameters\" : {\n" +
                "    \"primary-key\" : {\n" +
                "      \"value\" : \"2a00:1f78::/32\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"abuse-contacts\" : {\n" +
                "    \"email\" : \"abuse@test.net\"\n" +
                "  },\n" +
                "  \"terms-and-conditions\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void lookup_inet6num_abuse_contact_found_accept_xml() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inet6num:      2a00:1f78::/32\n" +
                "netname:       RIPE-NCC\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "country:       DK\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        SUB-ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");
        ipTreeUpdater.rebuild();

        final String result = RestTest.target(getPort(), "whois/abuse-contact/2a00:1f78::/32")
                .request(MediaType.APPLICATION_XML)
                .get(String.class);

        assertThat(result, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<abuse-resources service=\"abuse-contact\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/abuse-contact/2a00:1f78::/32\"/>\n" +
                "    <parameters>\n" +
                "        <primary-key value=\"2a00:1f78::/32\"/>\n" +
                "    </parameters>\n" +
                "    <abuse-contacts email=\"abuse@test.net\"/>\n" +
                "    <terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
                "</abuse-resources>"));
    }

    @Test
    public void lookup_inet6num_not_found_json() {
        try {
            RestTest.target(getPort(), "whois/abuse-contact/2a00:1234::/32")
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
        } catch (NotFoundException e) {
            final AbuseResources result = e.getResponse().readEntity(AbuseResources.class);
            assertThat(result.getMessage(), is("No abuse contact found for 2a00:1234::/32"));
        }
    }

    // autnum

    @Test
    public void lookup_autnum_abuse_contact_found() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS333\n" +
                "as-name:       Test-User-1\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        TEST");

        final AbuseResources result = RestTest.target(getPort(), "whois/abuse-contact/AS333")
                .request(MediaType.APPLICATION_XML)
                .get(AbuseResources.class);

        assertThat(result.getAbuseContact().getEmail(), is("abuse@test.net"));
    }

    @Test
    public void lookup_autnum_abuse_contact_not_found() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS333\n" +
                "as-name:       Test-User-1\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        test");

        final AbuseResources abuseResources = RestTest.target(getPort(), "whois/abuse-contact/AS333")
                .request(MediaType.APPLICATION_XML)
                .get(AbuseResources.class);

        assertThat(abuseResources.getParameters().getPrimaryKey().getValue(), is("AS333"));
        assertThat(abuseResources.getAbuseContact().getEmail(), is(""));
    }

    @Test
    public void lookup_autnum_not_found_json() {
        try {
            RestTest.target(getPort(), "whois/abuse-contact/AS333")
                    .request(MediaType.APPLICATION_JSON)
                    .get(AbuseResources.class);
        } catch (NotFoundException e) {
            assertThat(e.getResponse().readEntity(String.class),
                    is("{\n" +
                       "  \"message\" : \"No abuse contact found for AS333\"\n" +
                       "}"));
        }
    }

    @Test
    public void lookup_autnum_abuse_contact_found_json_extension() {
        databaseHelper.addObject("" +
                "organisation:  ORG-OT1-TEST\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       TR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        test");
        databaseHelper.addObject("" +
                "aut-num:       AS333\n" +
                "as-name:       Test-User-1\n" +
                "descr:         some description\n" +
                "org:           ORG-OT1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       org@ripe.net 20120505\n" +
                "source:        test");

        final String result = RestTest.target(getPort(), "whois/abuse-contact/AS333.json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(result, containsString("" +
                "{\n" +
                "  \"service\" : \"abuse-contact\",\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/abuse-contact/AS333\"\n" +
                "  },"));
    }
}
