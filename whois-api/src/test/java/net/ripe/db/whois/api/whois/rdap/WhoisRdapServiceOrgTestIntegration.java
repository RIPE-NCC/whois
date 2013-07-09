package net.ripe.db.whois.api.whois.rdap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Category(IntegrationTest.class)
public class WhoisRdapServiceOrgTestIntegration extends AbstractRestClientTest {

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
            "source:      TEST\n");

    private static final RpslObject ORG_ONE = RpslObject.parse("" +
            "organisation:  ORG-ONE-TEST\n" +
            "org-name:      Organisation One\n" +
            "org-type:      LIR\n" +
            "address:       One Org Street\n" +
            "e-mail:        test@ripe.net\n" +
            "admin-c:       TP2-TEST\n" +
            "tech-c:        TP1-TEST\n" +
            "tech-c:        TP2-TEST\n" +
            "mnt-ref:       OWNER-MNT\n" +
            "mnt-by:        OWNER-MNT\n" +
            "changed:       test@test.net.au 20000228\n" +
            "source:        TEST\n");

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject(TP1_TEST_BOOT);
        databaseHelper.addObject(OWNER_MNT);
        databaseHelper.updateObject(TP1_TEST);
        databaseHelper.addObject(TP2_TEST);
        databaseHelper.addObject(ORG_ONE);
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider());
        client = Client.create(cc);
    }

    @Test
    public void noOrg() throws Exception {
        final ClientResponse clientResponse = createResource(AUDIENCE, "entity/ORG-NONE-TEST").get(ClientResponse.class);

        assertThat(clientResponse.getStatus(), equalTo(404));
        assertThat(clientResponse.getEntity(String.class), equalTo(""));
    }

    @Test
    public void lookupOrg() throws Exception {
        final ClientResponse clientResponse = createResource(AUDIENCE, "entity/ORG-ONE-TEST").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);

        assertThat(clientResponse.getStatus(), equalTo(200));
        final Entity entity = clientResponse.getEntity(Entity.class);
        assertThat(entity.getHandle(), equalTo("ORG-ONE-TEST"));

        final List<Event> events = entity.getEvents();
        assertThat(events.size(), equalTo(1));

        final Event event = events.get(0);
        DateTime eventDateTime = new DateTime(event.getEventDate().toGregorianCalendar());
        DateTime checkDate = DateTimeFormat.forPattern("yyyyMMdd").parseLocalDate("20000228").toDateTime(new LocalTime(0, 0, 0));
        assertThat(eventDateTime.toString(), equalTo(checkDate.toString()));

        final List<Entity> entities = entity.getEntities();
        assertThat(entities.size(), equalTo(2));

        Collections.sort(entities, new Comparator<Entity>() {
            public int compare(final Entity e1, final Entity e2) {
                return e1.getHandle().compareTo(e2.getHandle());
            }
        });

        final Entity entityTp1 = entities.get(0);
        assertThat(entityTp1.getHandle(), equalTo("TP1-TEST"));

        final List<String> tp1Roles = entityTp1.getRoles();
        assertThat(tp1Roles.size(), equalTo(1));
        assertThat(tp1Roles.get(0), equalTo("technical"));

        final Entity entityTp2 = entities.get(1);
        assertThat(entityTp2.getHandle(), equalTo("TP2-TEST"));

        final List<String> tp2Roles = entityTp2.getRoles();
        Collections.sort(tp2Roles);
        assertThat(tp2Roles.size(), equalTo(2));
        assertThat(tp2Roles.get(0), equalTo("administrative"));
        assertThat(tp2Roles.get(1), equalTo("technical"));

        final String linkValue = createResource(AUDIENCE, "entity/ORG-ONE-TEST").toString();
        final String tp1Link = createResource(AUDIENCE, "entity/TP1-TEST").toString();
        final String tp2Link = createResource(AUDIENCE, "entity/TP2-TEST").toString();

        final List<Link> tp1Links = entityTp1.getLinks();
        assertThat(tp1Links.size(), equalTo(1));
        assertThat(tp1Links.get(0).getRel(), equalTo("self"));
        assertThat(tp1Links.get(0).getValue(), equalTo(linkValue));
        assertThat(tp1Links.get(0).getHref(), equalTo(tp1Link));

        final List<Link> tp2Links = entityTp2.getLinks();
        assertThat(tp2Links.size(), equalTo(1));
        assertThat(tp2Links.get(0).getRel(), equalTo("self"));
        assertThat(tp2Links.get(0).getValue(), equalTo(linkValue));
        assertThat(tp2Links.get(0).getHref(), equalTo(tp2Link));

        final List<Link> links = entity.getLinks();
        assertThat(links.size(), equalTo(1));

        final Link selfLink = links.get(0);
        assertThat(selfLink.getRel(), equalTo("self"));
        assertThat(selfLink.getValue(), equalTo(linkValue));
        assertThat(selfLink.getHref(), equalTo(linkValue)); 
    }

    @Override
    protected WebResource createResource(final Audience audience, final String path) {
        return client.resource(String.format("http://localhost:%s/rdap/%s", getPort(audience), path));
    }
}
