package net.ripe.db.whois.api.whois;

import com.Ostermiller.util.LineEnds;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class WhoisRdapServiceTestIntegration extends AbstractRestClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapServiceTestIntegration.class);
    private static final Audience AUDIENCE = Audience.RDAP;
    private static final String VERSION_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";
    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:  Pauleth Palthen\n" +
            "address: Singel 258\n" +
            "phone:   +31-1234567890\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: PP1-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "remarks: remark\n" +
            "source:  TEST\n");
    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");
    private static final RpslObject TEST_PERSON = RpslObject.parse("" +
            "person:  Test Person\n" +
            "address: Singel 258\n" +
            "phone:   +31 6 12345678\n" +
            "nic-hdl: TP1-TEST\n" +
            "mnt-by:  OWNER-MNT\n" +
            "changed: dbtest@ripe.net 20120101\n" +
            "source:  TEST\n");
    private static final RpslObject TEST_DOMAIN = RpslObject.parse("" +
            "domain:  31.12.202.in-addr.arpa\n" +
            "descr:   Test domain\n" +
            "admin-c: TP1-TEST\n" +
            "tech-c:  TP1-TEST\n" +
            "zone-c:  TP1-TEST\n" +
            "notify:  notify@test.net.au\n" +
            "nserver: ns1.test.com.au\n" +
            "nserver: ns2.test.com.au\n" +
            "changed: test@test.net.au 20010816\n" +
            "changed: test@test.net.au 20121121\n" +
            "mnt-by:  OWNER-MNT\n" +
            "source:  TEST\n");

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_DOMAIN);
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true));
        client = Client.create(cc);
    }

    @Test
    public void lookup_inet6num_with_prefix_length() throws Exception {
        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                        "netname:        RIPE-NCC\n" +
                        "descr:          Private Network\n" +
                        "country:        NL\n" +
                        "tech-c:         TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "mnt-lower:      OWNER-MNT\n" +
                        "source:         TEST");
        ipTreeUpdater.rebuild();

        createResource(AUDIENCE, "inet6num/2001:2002:2003::/48");
        //final WhoisResources whoisResources = createResource(AUDIENCE, "inet6num/2001:2002:2003::/48").get(WhoisResources.class);
        /*assertThat(whoisResources.getWhoisObjects(), hasSize(2));
        final RpslObject inet6num = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(inet6num.getKey(), is(ciString("2001:2002:2003::/48")));
        final RpslObject person = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(1));
        assertThat(person.getKey(), is(ciString("TP1-TEST")));   */

        //Thread.sleep(1500000);
    }

    @Test
    public void lookup_person_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        ClientResponse response = createResource(AUDIENCE, "person/PP1-TEST").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        String responseContent = response.getEntity(String.class);
        LOGGER.info("Response:" + responseContent);
        String textEntity = convertEOLToUnix(responseContent);

        assertEquals("" +
                "{\n" +
                "  \"handle\" : \"PP1-TEST\",\n" +
                "  \"vcardArray\" : [ \"vcard\", [ [ \"version\", {\n" +
                "  }, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "  }, \"text\", \"Pauleth Palthen\" ], [ \"adr\", {\n" +
                "    \"label\" : \"Singel 258\"\n" +
                "  }, \"text\", [ \"\", \"\", \"\", \"\", \"\", \"\", \"\" ] ], [ \"tel\", {\n" +
                "  }, \"uri\", \"+31-1234567890\" ] ] ]\n" +
                "}", textEntity);
    }

    //@Test
    public void lookup_domain_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        ClientResponse response = createResource(AUDIENCE, "domain/31.12.202.in-addr.arpa").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        String responseContent = response.getEntity(String.class);
        LOGGER.info("Response:" + responseContent);
        String textEntity = convertEOLToUnix(responseContent);

        assertEquals("" +
                "{\n" +
                "  \"handle\" : \"PP1-TEST\",\n" +
                "  \"vcardArray\" : [ \"vcard\", [ [ \"version\", {\n" +
                "  }, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "  }, \"text\", \"Pauleth Palthen\" ] ] ]\n" +
                "}", textEntity);
    }

    // helper methods
    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }

    private String convertEOLToUnix(String str) throws IOException {
        StringOutputStream resultStream = new StringOutputStream();
        LineEnds.convert(new StringInputStream(str), resultStream, LineEnds.STYLE_UNIX);
        return resultStream.toString();
    }
}
