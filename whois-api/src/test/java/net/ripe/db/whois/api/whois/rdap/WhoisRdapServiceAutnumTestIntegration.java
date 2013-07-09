package net.ripe.db.whois.api.whois.rdap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRdapServiceAutnumTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
            "person: Test Person\n" +
            "nic-hdl: TP1-TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     TP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST"));
        databaseHelper.updateObject(RpslObject.parse("" +
            "person:    Test Person\n" +
            "address:   Test Address\n" +
            "phone:     +61-1234-1234\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   TP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "source:    TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
            "person:  Test Person2\n" +
            "address: Test Address\n" +
            "phone:   +61-1234-1234\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: TP2-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "source:  TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
            "as-block:  AS1000-AS2000\n" +
            "descr:     An ASN range\n" +
            "remarks:   a remark\n" +
            "remarks:   another remark\n" +
            "admin-c:   TP1-TEST\n" +
            "tech-c:    TP1-TEST\n" +
            "country:   AU\n" +
            "changed:   test@test.net.au 20010816\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n"));
        databaseHelper.addObject(RpslObject.parse("" +
            "aut-num:   AS123\n" +
            "as-name:   AS-TEST\n" +
            "descr:     A single ASN\n" +
            "admin-c:   TP1-TEST\n" +
            "tech-c:    TP2-TEST\n" +
            "country:   AU\n" +
            "changed:   test@test.net.au 20010816\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n"));
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider());
        client = Client.create(cc);
    }

    @Test
    public void autnum_not_found() throws Exception {
        try {
            createResource(AUDIENCE, "autnum/1")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_single_autnum() throws Exception {
        final Autnum autnum = createResource(AUDIENCE, "autnum/123")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getHandle(), equalTo("AS123"));
        assertThat(autnum.getStartAutnum(), equalTo(123L));
        assertThat(autnum.getEndAutnum(), equalTo(123L));
        assertThat(autnum.getName(), equalTo("AS-TEST"));
        assertThat(autnum.getCountry(), equalTo("AU"));
        assertThat(autnum.getType(), equalTo("DIRECT ALLOCATION"));

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(1));

        final Event event = events.get(0);
        assertThat(event.getEventDate(), is(LocalDateTime.parse("2001-08-16T00:00:00")));

//        final List<Entity> entities = autnum.getEntities();                           // TODO: implement
//        assertThat(entities, hasSize(2));
//        Collections.sort(entities, new Comparator<Entity>() {
//            public int compare(final Entity e1, final Entity e2) {
//                return e1.getHandle().compareTo(e2.getHandle());
//            }
//        });
//
//        final Entity entityTp1 = entities.get(0);
//        assertThat(entityTp1.getHandle(), equalTo("TP1-TEST"));
//
//        final List<String> adminRoles = entityTp1.getRoles();
//        assertThat(adminRoles, hasSize(1));
//        assertThat(adminRoles.get(0), equalTo("administrative"));
//
//        final Entity entityTp2 = entities.get(1);
//        assertThat(entityTp2.getHandle(), equalTo("TP2-TEST"));
//
//        final List<String> techRoles = entityTp2.getRoles();
//        assertThat(techRoles, hasSize(1));
//        assertThat(techRoles.get(0), equalTo("technical"));

        final List<Link> links = autnum.getLinks();
        assertThat(links, hasSize(1));
        final Link selfLink = links.get(0);
        assertThat(selfLink.getRel(), equalTo("self"));

        final String ru = createResource(AUDIENCE, "autnum/123").toString();
        assertThat(selfLink.getValue(), equalTo(ru));
        assertThat(selfLink.getHref(), equalTo(ru));
    }

    @Test
    public void lookup_autnum_within_block() throws Exception {
        try {
            createResource(AUDIENCE, "autnum/1500")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/rdap/%s", getPort(audience), path));
    }
}
