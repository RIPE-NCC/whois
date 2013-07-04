package net.ripe.db.whois.api.whois;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class WhoisRdapServiceTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:  Pauleth Palthen\n" +
            "address: Singel 258\n" +
            "phone:   +31-1234567890\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: PP1-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "changed: noreply@ripe.net 20120102\n" +
            "changed: noreply@ripe.net 20120103\n" +
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

    private static final RpslObject TEST_ORG = RpslObject.parse("" +
            "organisation:  ORG-TEST1-TEST\n" +
            "org-name:      Test organisation\n" +
            "org-type:      OTHER\n" +
            "descr:         Drugs and gambling\n" +
            "remarks:       Nice to deal with generally\n" +
            "address:       1 Fake St. Fauxville\n" +
            "phone:         +01-000-000-000\n" +
            "fax-no:        +01-000-000-000\n" +
            "e-mail:        org@test.com\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       test@test.net.au 20121121\n" +
            "source:        TEST\n");

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
        databaseHelper.addObject(TEST_DOMAIN);
        ipTreeUpdater.rebuild();
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider());
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
        //TODO assert something
    }

    @Test
    public void lookup_person_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        ClientResponse response = createResource(AUDIENCE, "entity/PP1-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        final Entity en = response.getEntity(Entity.class);

        assertThat(en.getHandle(), equalTo("PP1-TEST"));
        assertThat(en.getRdapConformance().get(0), equalTo("rdap_level_0"));
        //TODO complete
    }

    @Test
    public void lookup_domain_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);

        ClientResponse response = createResource(AUDIENCE, "domain/31.12.202.in-addr.arpa")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        final Domain dn = response.getEntity(Domain.class);

        assertThat(dn.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(dn.getLdhName(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(dn.getRdapConformance().get(0), equalTo("rdap_level_0"));
        //TODO complete
    }

    @Test
    public void lookup_org_object() throws Exception {
        databaseHelper.addObject(TEST_ORG);

        ClientResponse response = createResource(AUDIENCE, "entity/ORG-TEST1-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        final Entity en = response.getEntity(Entity.class);

        assertThat(en.getHandle(), equalTo("ORG-TEST1-TEST"));
        //TODO complete
    }

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
