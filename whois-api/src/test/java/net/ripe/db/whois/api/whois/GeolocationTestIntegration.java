package net.ripe.db.whois.api.whois;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class GeolocationTestIntegration extends AbstractRestClientTest {

    @Autowired
    private IpTreeUpdater ipTreeUpdater;

    @Before
    public void setup() {
        databaseHelper.addObject(
                "mntner:  OWNER-MNT\n" +
                "descr:   Owner Maintainer\n" +
                "auth:    MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST");
        databaseHelper.addObject(
                "person:  Test Person\n" +
                "address: Singel 258\n" +
                "phone:   +31 6 12345678\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST\n");
    }

    @Test
    public void inetnum_with_geolocation_only() throws Exception {
       databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "geoloc:         52.375599 4.899902\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=10.0.0.0")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

        assertThat(response, containsString("service=\"geolocation-finder\""));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\""));
        assertThat(response, containsString("<geolocation-attributes>"));
        assertThat(response, containsString("<location value=\"52.375599 4.899902\">"));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\"http://apps.db.ripe.net/whois/lookup/test/inetnum/10.0.0.0 - 10.255.255.255\"/>"));
    }

    @Test
    public void inetnum_with_geolocation_and_language() throws Exception {
        databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "geoloc:         52.375599 4.899902\n" +
               "language:       EN\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=10.0.0.0")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

        assertThat(response, containsString("service=\"geolocation-finder\""));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\""));
        assertThat(response, containsString("<geolocation-attributes>"));
        assertThat(response, containsString("<location value=\"52.375599 4.899902\">"));
        assertThat(response, containsString("<language value=\"EN\">"));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\"http://apps.db.ripe.net/whois/lookup/test/inetnum/10.0.0.0 - 10.255.255.255\"/>"));
    }

    @Test
    public void inetnum_with_geolocation_and_language_json_response() throws Exception {
        databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "geoloc:         52.375599 4.899902\n" +
               "language:       EN\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=10.0.0.0")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(String.class);

        assertThat(response, containsString("\"whois-resources\""));
        assertThat(response, containsString("\"service\" : \"geolocation-finder\""));
        assertThat(response, containsString("\"geolocation-attributes\""));
        assertThat(response, containsString("\"location\""));
        assertThat(response, containsString("\"language\""));
    }

    @Test
    public void inetnum_without_geolocation() throws Exception {
       databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        try {
            createResource("whois/geolocation?source=test&ipkey=10.0.0.0")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("No geolocation data was found for the given ipkey: 10.0.0.0"));
        }
    }

    @Test
    public void inetnum_not_found() throws Exception {
        try {
            createResource("whois/geolocation?source=test&ipkey=127.0.0.1")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("No geolocation data was found for the given ipkey: 127.0.0.1"));
        }
    }

    @Test
    public void organisation_with_geolocation() throws Exception {
        databaseHelper.addObject(
                "organisation:  ORG-LIR1-TEST\n" +
                "org-type:      LIR\n" +
                "org-name:      Local Internet Registry\n" +
                "address:       RIPE NCC\n" +
                "geoloc:        52.375599 4.899902\n" +
                "language:      EN\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "ref-nfy:       dbtest-org@ripe.net\n" +
                "mnt-ref:       OWNER-MNT\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20121016\n" +
                "source:        TEST\n");
        databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "org:            ORG-LIR1-TEST\n" +
               "descr:          Private Network\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=10.0.0.0")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

        assertThat(response, containsString("service=\"geolocation-finder\""));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\""));
        assertThat(response, containsString("<geolocation-attributes>"));
        assertThat(response, containsString("<location value=\"52.375599 4.899902\">"));
        assertThat(response, containsString("<language value=\"EN\">"));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\"http://apps.db.ripe.net/whois/lookup/test/organisation/ORG-LIR1-TEST\"/>"));
    }

    @Test
    public void parent_inetnum_with_geolocation_and_language() throws Exception {
        databaseHelper.addObject(
               "inetnum:        10.0.0.0 - 10.255.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "geoloc:         52.375599 4.899902\n" +
               "language:       EN\n" +
               "descr:          Private Network\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        databaseHelper.addObject(
               "inetnum:        10.1.0.0 - 10.1.255.255\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PI\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=10.1.0.0%20-%2010.1.255.255")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

        assertThat(response, containsString("<location value=\"52.375599 4.899902\">"));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\"http://apps.db.ripe.net/whois/lookup/test/inetnum/10.0.0.0 - 10.255.255.255\"/>"));
    }

    @Test
    public void inet6num_with_geolocation_and_language() throws Exception {
        databaseHelper.addObject(
               "inet6num:       2001::/20\n" +
               "netname:        RIPE-NCC\n" +
               "descr:          Private Network\n" +
               "geoloc:         52.375599 4.899902\n" +
               "language:       EN\n" +
               "country:        NL\n" +
               "tech-c:         TP1-TEST\n" +
               "status:         ASSIGNED PA\n" +
               "mnt-by:         OWNER-MNT\n" +
               "mnt-lower:      OWNER-MNT\n" +
               "source:         TEST");
        ipTreeUpdater.rebuild();

        final String response = createResource("whois/geolocation?source=test&ipkey=2001::/20")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);

        assertThat(response, containsString("service=\"geolocation-finder\""));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\""));
        assertThat(response, containsString("<geolocation-attributes>"));
        assertThat(response, containsString("<location value=\"52.375599 4.899902\">"));
        assertThat(response, containsString("<language value=\"EN\">"));
        assertThat(response, containsString("<link xlink:type=\"locator\" xlink:href=\"http://apps.db.ripe.net/whois/lookup/test/inet6num/2001::/20\"/>"));
    }

    @Test
    public void invalid_inetnum_argument() throws Exception {
        try {
            createResource("whois/geolocation?source=test&ipkey=invalid")
                    .accept(MediaType.APPLICATION_XML)
                    .get(String.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("No inetnum/inet6num resource has been found"));
        }
    }

    protected WebResource createResource(final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(Audience.PUBLIC), path));
    }
}
