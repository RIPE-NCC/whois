package net.ripe.db.whois.api.whois.rdap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.common.IntegrationTest;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRdapServiceTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("" +
                "person: Test Person\n" +
                "nic-hdl: TP1-TEST");
        databaseHelper.addObject("" +
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");
        databaseHelper.updateObject("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "person:        Test Person2\n" +
                "address:       Test Address\n" +
                "phone:         +61-1234-1234\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       TP2-TEST\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "changed:       noreply@ripe.net 20120102\n" +
                "changed:       noreply@ripe.net 20120103\n" +
                "remarks:       remark\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "domain:        31.12.202.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "changed:       test@test.net.au 20010816\n" +
                "changed:       test@test.net.au 20121121\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "aut-num:       AS123\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "changed:       test@test.net.au 20010816\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST1-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "admin-c:       PP1-TEST\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       test@test.net.au 20121121\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
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
        databaseHelper.addObject("" +
                "as-block:       AS100 - AS200\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TEST1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        dbtest@ripe.net   20121214\n" +
                "source:         TEST\n" +
                "password:       test\n");
        ipTreeUpdater.rebuild();
    }

    @Before
    @Override
    public void setUpClient() throws Exception {
        ClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(new JacksonJaxbJsonProvider());
        client = Client.create(cc);
    }

    // inetnum

    @Test
    public void lookup_inetnum_range() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip response = createResource(AUDIENCE, "ip/192.0.0.0/8")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(response.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(response.getIpVersion(), is("v4"));
        assertThat(response.getLang(), is("en"));
        assertThat(response.getCountry(), is("NL"));
        assertThat(response.getStartAddress(), is("192.0.0.0/32"));
        assertThat(response.getEndAddress(), is("192.255.255.255/32"));
        assertThat(response.getName(), is("TEST-NET-NAME"));
    }

    @Test
    public void lookup_inetnum_less_specific() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip response = createResource(AUDIENCE, "ip/192.0.0.255")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(response.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(response.getIpVersion(), is("v4"));
        assertThat(response.getCountry(), is("NL"));
        assertThat(response.getStartAddress(), is("192.0.0.0/32"));
        assertThat(response.getEndAddress(), is("192.255.255.255/32"));
        assertThat(response.getName(), is("TEST-NET-NAME"));
    }

    @Test
    public void lookup_inetnum_not_found() {
        try {
            createResource(AUDIENCE, "ip/193.0.0.0")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        } catch (final UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    // inet6num

    @Test
    public void lookup_inet6num_with_prefix_length() throws Exception {
        databaseHelper.addObject("" +
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

        final Ip response = createResource(AUDIENCE, "ip/2001:2002:2003::/48")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(response.getHandle(), is("2001:2002:2003::/48"));
        assertThat(response.getIpVersion(), is("v6"));
        assertThat(response.getCountry(), is("NL"));
        assertThat(response.getStartAddress(), is("2001:2002:2003::/128"));
        assertThat(response.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff/128"));
        assertThat(response.getName(), is("RIPE-NCC"));
    }

    @Test
    public void lookup_inet6num_less_specific() throws Exception {
        databaseHelper.addObject("" +
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

        final Ip response = createResource(AUDIENCE, "ip/2001:2002:2003:2004::")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(response.getHandle(), is("2001:2002:2003::/48"));
        assertThat(response.getIpVersion(), is("v6"));
        assertThat(response.getCountry(), is("NL"));
        assertThat(response.getStartAddress(), is("2001:2002:2003::/128"));
        assertThat(response.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff/128"));
        assertThat(response.getName(), is("RIPE-NCC"));
    }

    // person entity

    @Test
    public void lookup_person_entity() throws Exception {
        final Entity response = createResource(AUDIENCE, "entity/PP1-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("PP1-TEST"));
        assertThat(response.getVCardArray().size(), is(2));
        assertThat(response.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(response.getVCardArray().get(1).toString().toString(), equalTo("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Pauleth Palthen], " +
                "[kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, null], " +
                "[tel, {}, uri, +31-1234567890], " +
                "[email, {}, text, noreply@ripe.net]]"));
        assertThat(response.getRdapConformance().get(0), equalTo("rdap_level_0"));
    }

    @Test
    public void lookup_entity_not_found() throws Exception {
        try {
            createResource(AUDIENCE, "entity/nonexistant")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        } catch (final UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_entity_no_accept_header() {

        final Entity response = createResource(AUDIENCE, "entity/PP1-TEST")
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("PP1-TEST"));
        assertThat(response.getRdapConformance().get(0), equalTo("rdap_level_0"));
    }

    // domain

    @Test
    public void lookup_domain_object() throws Exception {
        final Domain response = createResource(AUDIENCE, "domain/31.12.202.in-addr.arpa")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertThat(response.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(response.getLdhName(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(response.getRdapConformance().get(0), equalTo("rdap_level_0"));
    }

    // autnum

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
        assertThat(autnum.getType(), equalTo("DIRECT ALLOCATION"));

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(2));

        final Event firstEvent = events.get(0);
        assertTrue(firstEvent.getEventDate().isBefore(LocalDateTime.now()));
        assertThat(firstEvent.getEventAction(), is("registration"));

        final Event lastEvent = events.get(1);
        assertTrue(lastEvent.getEventDate().isAfter(firstEvent.getEventDate()) || lastEvent.getEventDate().equals(firstEvent.getEventDate()));
        assertThat(lastEvent.getEventAction(), is("last changed"));

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
        assertThat(links, hasSize(3));                                              // TODO: verify number of links
        final Link selfLink = links.get(0);
        assertThat(selfLink.getRel(), equalTo("self"));

//        final String ru = createResource(AUDIENCE, "autnum/123").toString();      // TODO: self link includes prefix
//        assertThat(selfLink.getValue(), equalTo(ru));
//        assertThat(selfLink.getHref(), equalTo(ru));

        final List<Remark> remarks = autnum.getRemarks();
        assertThat(remarks, hasSize(2));                                            // TODO: two remarks
        assertThat(remarks.get(0).getDescription().get(0), is("A single ASN"));
        assertThat(remarks.get(1).getDescription().get(0), is("A single ASN"));
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

    // general

    @Test
    public void multiple_modification_gives_correct_events() throws Exception {
        final String start = "" +
                "aut-num:   AS123\n" +
                "as-name:   AS-TEST\n" +
                "descr:     Modified ASN\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "changed:   test@test.net.au 20010816\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n" +
                "password:  test\n";
        final String response = doPostOrPutRequest(getSyncupdatesUrl("test", ""), "POST", "DATA=" + encode(start), MediaType.APPLICATION_FORM_URLENCODED, HttpURLConnection.HTTP_OK);
        assertThat(response, containsString("Modify SUCCEEDED: [aut-num] AS123"));

        final String deleteString = "" +
                "aut-num:   AS123\n" +
                "as-name:   AS-TEST\n" +
                "descr:     Modified ASN\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "changed:   test@test.net.au 20010816\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n" +
                "delete:    reason\n" +
                "password:  test\n";

        final String delete = doPostOrPutRequest(getSyncupdatesUrl("test", ""), "POST", "DATA=" + encode(deleteString), MediaType.APPLICATION_FORM_URLENCODED, HttpURLConnection.HTTP_OK);
        assertThat(delete, containsString("Delete SUCCEEDED: [aut-num] AS123"));

        final String recreate = doPostOrPutRequest(getSyncupdatesUrl("test", ""), "POST", "DATA=" + encode(start) + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED, HttpURLConnection.HTTP_OK);
        assertThat(recreate, containsString("Create SUCCEEDED: [aut-num] AS123"));

        final String modifiedAgain = "" +
                "aut-num:   AS123\n" +
                "as-name:   AS-TEST\n" +
                "descr:     Final version ASN\n" +
                "admin-c:   TP1-TEST\n" +
                "tech-c:    TP1-TEST\n" +
                "changed:   test@test.net.au 20010816\n" +
                "mnt-by:    OWNER-MNT\n" +
                "source:    TEST\n" +
                "password:  test\n";
        final String last = doPostOrPutRequest(getSyncupdatesUrl("test", ""), "POST", "DATA=" + encode(modifiedAgain), MediaType.APPLICATION_FORM_URLENCODED, HttpURLConnection.HTTP_OK);
        assertThat(last, containsString("Modify SUCCEEDED: [aut-num] AS123"));

        final Autnum autnum = createResource(AUDIENCE, "autnum/123")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(2));

        assertThat(events.get(0).getEventAction(), is("registration"));
        assertThat(events.get(1).getEventAction(), is("last changed"));
    }

    @Test
    public void lookup_inetnum_abuse_contact_as_vcard() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "organisation:  ORG-TO2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       AB-TEST\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       test@test.net.au 20121121\n" +
                "source:        TEST\n");
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TO2-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST");
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.0.0.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "changed:      dbtest@ripe.net 20020101\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource(AUDIENCE, "ip/192.0.0.128")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getEntities().get(0).getHandle(), is("AB-TEST"));
        assertThat(ip.getEntities().get(0).getVCardArray(), hasSize(2));
        assertThat(ip.getEntities().get(0).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(ip.getEntities().get(0).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, null], " +
                "[tel, {}, uri, +31 6 12345678]]"));
    }

    // organisation entity

    @Test
    public void lookup_org_entity_handle() throws Exception {
        final Entity response = createResource(AUDIENCE, "entity/ORG-TEST1-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void lookup_org_not_found() throws Exception {
        try {
            createResource(AUDIENCE, "entity/ORG-NONE-TEST")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void lookup_org_entity() throws Exception {
        final Entity entity = createResource(AUDIENCE, "entity/ORG-ONE-TEST")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("ORG-ONE-TEST"));

        final List<Event> events = entity.getEvents();
        assertThat(events.size(), equalTo(1));

        final Event event = events.get(0);
        assertThat(event.getEventDate().toString(), equalTo(""));

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

    private String getSyncupdatesUrl(final String instance, final String command) {
        return "http://localhost:" + getPort(Audience.PUBLIC) + String.format("/whois/syncupdates/%s?%s", instance, command);
    }
}
