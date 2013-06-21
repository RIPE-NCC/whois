package net.ripe.db.whois.api.whois;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class WhoisRdapServiceTestIntegrationblah extends AbstractRestClientTest {

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

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TEST_PERSON);
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
    public void lookup_object() throws Exception {
        databaseHelper.addObject(PAULETH_PALTHEN);


        createResource(AUDIENCE, "person/PP1-TEST");

        /* blah */

        /*final WhoisResources whoisResources = createResource(AUDIENCE, "person/PP1-TEST").get(WhoisResources.class);
        assertThat(whoisResources.getWhoisObjects(), hasSize(1));

        final RpslObject rpslObject = WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
        assertThat(rpslObject.getKey(), is(ciString("PP1-TEST")));
          */
        Thread.sleep(1500000);
    }

    // helper methods

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
