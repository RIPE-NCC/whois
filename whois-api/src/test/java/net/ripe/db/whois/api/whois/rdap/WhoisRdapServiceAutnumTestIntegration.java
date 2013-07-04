package net.ripe.db.whois.api.whois.rdap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Category(IntegrationTest.class)
public class WhoisRdapServiceAutnumTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    private static final RpslObject TP1_TEST_BOOT = RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST");

    private static final RpslObject TP1_TEST = RpslObject.parse("" +
            "person:  Test Person\n" +
            "address: Test Address\n" +
            "phone:   +61-1234-1234\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: TP1-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "source:  TEST\n");

    private static final RpslObject TP2_TEST = RpslObject.parse("" +
            "person:  Test Person2\n" +
            "address: Test Address\n" +
            "phone:   +61-1234-1234\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: TP2-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
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

    private static final RpslObject ASN_RANGE_ONE = RpslObject.parse("" +
            "as-block:  AS1000-AS2000\n" +
            "descr:     An ASN range\n" +
            "remarks:   a remark\n" +
            "remarks:   another remark\n" +
            "admin-c:   TP1-TEST\n" +
            "tech-c:    TP1-TEST\n" +
            "country:   AU\n" +
            "changed:   test@test.net.au 20010816\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject ASN_RANGE_TWO = RpslObject.parse("" +
            "as-block:  AS10000-AS20000\n" +
            "descr:     An ASN range\n" +
            "admin-c:   TP1-TEST\n" +
            "tech-c:    TP2-TEST\n" +
            "country:   AU\n" +
            "changed:   test@test.net.au 20010816\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    private static final RpslObject ASN_SINGLE = RpslObject.parse("" +
            "aut-num:   AS12345\n" +
            "as-name:   AS-TEST\n" +
            "descr:     A single ASN\n" +
            "admin-c:   TP1-TEST\n" +
            "tech-c:    TP2-TEST\n" +
            "country:   AU\n" +
            "changed:   test@test.net.au 20010816\n" +
            "mnt-by:    OWNER-MNT\n" +
            "source:    TEST\n");

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject(TP1_TEST_BOOT);
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TP1_TEST);
        databaseHelper.addObject(TP2_TEST);
        databaseHelper.addObject(ASN_RANGE_ONE);
        databaseHelper.addObject(ASN_RANGE_TWO);
        databaseHelper.addObject(ASN_SINGLE);
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider());
        client = Client.create(cc);
    }

    @Test
    public void noAutnum() throws Exception {
        final ClientResponse clientResponse = createResource(AUDIENCE, "autnum/1").get(ClientResponse.class);

        assertThat(clientResponse.getStatus(), equalTo(404));
    }

    @Test
    public void lookupSingleAutnum() throws Exception {
        final ClientResponse clientResponse = createResource(AUDIENCE, "autnum/12345").get(ClientResponse.class);
        final Autnum autnum = clientResponse.getEntity(Autnum.class);

        assertThat(autnum.getHandle(), equalTo("AS12345"));
        assertThat(autnum.getStartAutnum(), equalTo((long) 12345));
        assertThat(autnum.getEndAutnum(), equalTo((long) 12345));
        assertThat(autnum.getName(), equalTo("AS-TEST"));
        assertThat(autnum.getCountry(), equalTo("AU"));
        assertThat(autnum.getType(), equalTo("DIRECT ALLOCATION"));

        final List<Event> events = autnum.getEvents();
        assertThat(events.size(), equalTo(1));

        final Event event = events.get(0);
        assertThat(event.getEventDate().toGregorianCalendar().getTime(), equalTo(new GregorianCalendar(2001, 7, 16).getTime()));    // TODO: use Joda time

        final List<Entity> entities = autnum.getEntities();
        assertThat(entities.size(), equalTo(2));

        Collections.sort(entities, new Comparator<Entity>() {
            public int compare(final Entity e1, final Entity e2) {
                return e1.getHandle().compareTo(e2.getHandle());
            }
        });

        final Entity entityTp1 = entities.get(0);
        assertThat(entityTp1.getHandle(), equalTo("TP1-TEST"));

        final List<String> adminRoles = entityTp1.getRoles();
        assertThat(adminRoles.size(), equalTo(1));
        assertThat(adminRoles.get(0), equalTo("administrative"));

        final Entity entityTp2 = entities.get(1);
        assertThat(entityTp2.getHandle(), equalTo("TP2-TEST"));

        final List<String> techRoles = entityTp2.getRoles();
        assertThat(techRoles.size(), equalTo(1));
        assertThat(techRoles.get(0), equalTo("technical"));

        final List<Link> links = autnum.getLinks();
        assertThat(links.size(), equalTo(1));

        final Link selfLink = links.get(0);
        assertThat(selfLink.getRel(), equalTo("self"));

        final String ru = createResource(AUDIENCE, "autnum/12345").toString();
        assertThat(selfLink.getValue(), equalTo(ru));
        assertThat(selfLink.getHref(), equalTo(ru));
    }

    @Test
    public void lookupAutnumWithinBlock() throws Exception {
        final ClientResponse clientResponse = createResource(AUDIENCE, "autnum/1500").get(ClientResponse.class);
        final Autnum autnum = clientResponse.getEntity(Autnum.class);

        assertThat(autnum.getHandle(), equalTo("AS1000-AS2000"));
        assertThat(autnum.getStartAutnum(), equalTo((long) 1000));
        assertThat(autnum.getEndAutnum(), equalTo((long) 2000));
        assertThat(autnum.getName(), equalTo("AS1000-AS2000"));
        assertThat(autnum.getCountry(), equalTo("AU"));
        assertThat(autnum.getType(), equalTo("DIRECT ALLOCATION"));

        final List<Link> links = autnum.getLinks();
        assertThat(links.size(), equalTo(1));

        final Link self = links.get(0);
        assertThat(self.getRel(), equalTo("self"));

        final String selfUrl = createResource(AUDIENCE, "autnum/1500").toString();
        assertThat(self.getValue(), equalTo(selfUrl));
        assertThat(self.getHref(), equalTo(selfUrl));

        final List<Entity> entities = autnum.getEntities();
        assertThat(entities.size(), equalTo(1));

        final List<String> roles = entities.get(0).getRoles();
        assertThat(roles.size(), equalTo(2));

        java.util.Collections.sort(roles);
        assertThat(roles.get(0), equalTo("administrative"));
        assertThat(roles.get(1), equalTo("technical"));
    }

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/%s", getPort(audience), path));
    }
}
