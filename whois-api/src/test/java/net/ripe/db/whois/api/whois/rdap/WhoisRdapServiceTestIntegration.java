package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.api.whois.rdap.domain.Action;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import net.ripe.db.whois.api.whois.rdap.domain.Remark;
import net.ripe.db.whois.api.whois.rdap.domain.Role;
import net.ripe.db.whois.api.whois.rdap.domain.SearchResult;
import net.ripe.db.whois.common.IntegrationTest;
import org.joda.time.LocalDateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class WhoisRdapServiceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    FreeTextIndex freeTextIndex;

    @BeforeClass
    public static void setProperties() throws Exception {
        System.setProperty("rdap.sources", "TEST-GRS");
        System.setProperty("rdap.redirect.test", "https://rdap.test.net");
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
        // We only enable freetext indexing here, so it doesn't slow down the rest of the test suite
        System.setProperty("dir.freetext.index", "${dir.var}/idx");
    }

    @AfterClass
    public static void clearProperties() throws Exception {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.test");
        System.clearProperty("rdap.public.baseUrl");
        System.clearProperty("dir.freetext.index");
    }

    @Before
    public void setup() throws Exception {
        databaseHelper.addObject("" +
                "person:        Test Person\n" +
                "nic-hdl:       TP1-TEST");
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
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Test Person2\n" +
                "address:       Test Address\n" +
                "phone:         +61-1234-1234\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       TP2-TEST\n" +
                "changed:       noreply@ripe.net 20120101\n" +
                "source:        TEST");
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
                "source:        TEST");
        databaseHelper.addObject("" +
                "role:          First Role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "admin-c:       PP1-TEST\n" +
                "tech-c:        PP1-TEST\n" +
                "nic-hdl:       FR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20121016\n" +
                "source:        TEST");
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
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "changed:       test@test.net.au 20010816\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
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
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:       AS100 - AS200\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TEST1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        dbtest@ripe.net   20121214\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        dbtest@ripe.net 20020101\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "changed:        dbtest@ripe.net 20020101\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();
    }

    // inetnum

    // Ref. draft-ietf-weirds-json-response, section 5.9 "An Example"
    @Test
    public void lookup_inetnum_range() throws Exception {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
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

        final Ip ip = createResource("ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getLang(), is("en"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.2.0/32"));
        assertThat(ip.getEndAddress(), is("192.0.2.255/32"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getType(), is("OTHER"));
        assertThat(ip.getPort43(), is("whois.ripe.net"));

        final List<String> rdapConformance = ip.getRdapConformance();
        assertThat(rdapConformance, hasSize(1));
        assertThat(rdapConformance, contains("rdap_level_0"));

        final List<Remark> remarks = ip.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription(), contains("TEST network"));

        final List<Event> events = ip.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(0).getDescription(), contains("This output has been filtered."));
        assertThat(notices.get(0).getLinks(), hasSize(0));
        assertThat(notices.get(1).getTitle(), is("Source"));                                                            // TODO: [ES] should source be specified?
        assertThat(notices.get(1).getDescription(), contains("Objects returned came from source", "TEST"));
        assertThat(notices.get(1).getLinks(), hasSize(0));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getDescription(), contains("This is the RIPE Database query service. The objects are in RDAP format."));
        assertThat(notices.get(2).getLinks(), hasSize(1));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/ip/192.0.2.0/24"));
        assertThat(notices.get(2).getLinks().get(0).getRel(), is("terms-of-service"));
        assertThat(notices.get(2).getLinks().get(0).getHref(), is("http://www.ripe.net/db/support/db-terms-conditions.pdf"));
        assertThat(notices.get(2).getLinks().get(0).getType(), is("application/pdf"));
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

        final Ip ip = createResource("ip/192.0.0.255")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.0.0/32"));
        assertThat(ip.getEndAddress(), is("192.255.255.255/32"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
    }

//    @Ignore("TODO: if 0/0 is found, then return 404 (or redirect if found in GRS)")
    @Test
    public void lookup_inetnum_not_found() {
        try {
            createResource("ip/193.0.0.0")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final NotFoundException e) {
            // expected
        }
    }

    @Test
    public void lookup_inetnum_invalid_syntax_multislash() {
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

        try {
            createResource("ip/192.0.0.0//32")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final BadRequestException e) {
            assertErrorResponse(e, "Invalid syntax.");
        }
    }

    @Test
    public void lookup_inetnum_invalid_syntax() {
        try {
            createResource("ip/invalid")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final BadRequestException e) {
            assertErrorResponse(e, "Invalid syntax.");
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
                "language:       EN\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/2001:2002:2003::/48")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2002:2003::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getLang(), is("EN"));
        assertThat(ip.getStartAddress(), is("2001:2002:2003::/128"));
        assertThat(ip.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff/128"));
        assertThat(ip.getName(), is("RIPE-NCC"));
        assertThat(ip.getType(), is("ASSIGNED PA"));

        final List<String> rdapConformance = ip.getRdapConformance();
        assertThat(rdapConformance, hasSize(1));
        assertThat(rdapConformance, contains("rdap_level_0"));

        final List<Remark> remarks = ip.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription(), contains("Private Network"));

        final List<Event> events = ip.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/ip/2001:2002:2003::/48"));
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

        final Ip ip = createResource("ip/2001:2002:2003:2004::")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2002:2003::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("2001:2002:2003::/128"));
        assertThat(ip.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff/128"));
        assertThat(ip.getName(), is("RIPE-NCC"));
    }

//    @Ignore("TODO: if ::0 is found, then return 404 (or redirect if found in GRS)")
    @Test
    public void lookup_inet6num_not_found() {
        try {
            createResource("ip/2001:2002:2003::/48")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final NotFoundException e) {
            // expected
        }
    }

    // person entity

    @Test
    public void lookup_person_entity() throws Exception {
        final Entity entity = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("PP1-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getPort43(), is("whois.ripe.net"));
        assertThat(entity.getEntitySearchResults(), hasSize(1));
        assertThat(entity.getVCardArray().size(), is(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), equalTo("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Pauleth Palthen], " +
                "[kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, null], " +
                "[tel, {type=voice}, text, +31-1234567890], " +
                "[email, {}, text, noreply@ripe.net]]"));
        assertThat(entity.getRdapConformance(), hasSize(1));
        assertThat(entity.getRdapConformance().get(0), equalTo("rdap_level_0"));

        assertThat(entity.getRemarks(), hasSize(0));

        final List<Event> events = entity.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/entity/PP1-TEST"));
    }

    @Test
    public void lookup_entity_not_found() throws Exception {
        try {
            createResource("entity/ORG-BAD1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (final NotFoundException e) {
            // expected
        }
    }

    @Test
    public void lookup_entity_invalid_syntax() throws Exception {
        try {
            createResource("entity/12345")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            // expected
        }
    }

    @Test
    public void lookup_entity_no_accept_header() {
        final Entity entity = createResource("entity/PP1-TEST")
                .request()
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("PP1-TEST"));
        assertThat(entity.getRdapConformance(), hasSize(1));
        assertThat(entity.getRdapConformance().get(0), equalTo("rdap_level_0"));
    }

    // role entity

    @Test
    public void lookup_role_entity() throws Exception {
        final Entity entity = createResource("entity/FR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("FR1-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getPort43(), is("whois.ripe.net"));
        assertThat(entity.getVCardArray().size(), is(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), equalTo("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, First Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, null], " +
                "[email, {}, text, dbtest@ripe.net]]"));

        assertThat(entity.getEntitySearchResults(), hasSize(2));
        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("PP1-TEST"));
        assertThat(entity.getEntitySearchResults().get(1).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));
        assertThat(entity.getRdapConformance(), hasSize(1));
        assertThat(entity.getRdapConformance().get(0), equalTo("rdap_level_0"));

        final List<Event> events = entity.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        assertThat(entity.getRemarks(), hasSize(0));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/entity/FR1-TEST"));
    }

    // domain

    @Test
    public void lookup_domain_object() throws Exception {
        final Domain domain = createResource("domain/31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getRdapConformance(), hasSize(1));
        assertThat(domain.getRdapConformance().get(0), equalTo("rdap_level_0"));
        assertThat(domain.getPort43(), is("whois.ripe.net"));

        assertThat(domain.getNameservers(), hasSize(2));
        assertThat(domain.getNameservers().get(0).getLdhName(), is("ns1.test.com.au"));
        assertThat(domain.getNameservers().get(0).getIpAddresses(), equalTo(new Nameserver.IpAddresses(Lists.newArrayList("10.0.0.1/32"), null)));
        assertThat(domain.getNameservers().get(1).getLdhName(), is("ns2.test.com.au"));
        assertThat(domain.getNameservers().get(1).getIpAddresses(), equalTo(new Nameserver.IpAddresses(null, Lists.newArrayList("2001:10::2/128"))));

        assertThat(domain.getSecureDNS().isDelegationSigned(), is(Boolean.TRUE));
        assertThat(domain.getSecureDNS().getDsData(), hasSize(3));
        assertThat(domain.getSecureDNS().getDsData().get(0).getDigest(), is("13ee60f7499a70e5aadaf05828e7fc59e8e70bc1"));
        assertThat(domain.getSecureDNS().getDsData().get(0).getKeyTag(), is(52151L));
        assertThat(domain.getSecureDNS().getDsData().get(0).getAlgorithm(), is(1));
        assertThat(domain.getSecureDNS().getDsData().get(0).getDigestType(), is(1));
        assertThat(domain.getSecureDNS().getDsData().get(1).getDigest(), is("2e58131e5fe28ec965a7b8e4efb52d0a028d7a78"));
        assertThat(domain.getSecureDNS().getDsData().get(2).getDigest(), is("8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e"));

        assertThat(domain.getRemarks(), hasSize(1));
        assertThat(domain.getRemarks().get(0).getDescription(), contains("Test domain"));

        final List<Event> events = domain.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        final List<Entity> entities = domain.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));

        final List<Notice> notices = domain.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/domain/31.12.202.in-addr.arpa"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(2));
        Collections.sort(links);
        assertThat(links.get(0).getRel(), equalTo("copyright"));
        assertThat(links.get(1).getRel(), equalTo("self"));
    }

    @Test
    public void domain_not_found() throws Exception {
        try {
            createResource("domain/10.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void lookup_forward_domain() {
        try {
            createResource("domain/ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "RIPE NCC does not support forward domain queries.");
        }
    }

    // autnum

    @Test
    public void lookup_autnum_not_found() throws Exception {
        try {
            createResource("autnum/1")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Ignore("TODO: [ES] fix test data")
    @Test
    public void lookup_autnum_redirect_to_test() {
        try {
            createResource("autnum/102")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
            fail();
        } catch (final RedirectionException e) {
            assertThat(e.getResponse().getHeaders().getFirst("Location").toString(), is("https://rdap.test.net/autnum/102"));
        }
    }

    @Test
    public void lookup_autnum_invalid_syntax() throws Exception {
        try {
            createResource("autnum/XYZ")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "Invalid syntax.");
        }
    }

    @Test
    public void lookup_autnum_head_method() {
        final Response response = createResource("autnum/102").request().head();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void lookup_autnum_not_found_head_method() {
        final Response response = createResource("autnum/1").request().head();

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void lookup_single_autnum() throws Exception {
        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getHandle(), equalTo("AS102"));
        assertThat(autnum.getStartAutnum(), is(nullValue()));
        assertThat(autnum.getEndAutnum(), is(nullValue()));
        assertThat(autnum.getName(), equalTo("AS-TEST"));
        assertThat(autnum.getType(), equalTo("DIRECT ALLOCATION"));

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(1));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));

        final List<Entity> entities = autnum.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(1).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));

        final List<Link> links = autnum.getLinks();
        assertThat(links, hasSize(2));
        assertThat(links.get(0).getRel(), equalTo("self"));
        assertThat(links.get(1).getRel(), equalTo("copyright"));

        final List<Notice> notices = autnum.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));

        final List<Remark> remarks = autnum.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription().get(0), is("A single ASN"));
    }

    @Test
    public void lookup_autnum_with_rdap_json_content_type() {
        final Response response = createResource("autnum/102")
                .request("application/rdap+json")
                .get();

        assertThat(response.getMediaType(), is(new MediaType("application", "rdap+json")));
        final String entity = response.readEntity(String.class);
        assertThat(entity, containsString("\"handle\" : \"AS102\""));
        assertThat(entity, containsString("\"rdapConformance\" : [ \"rdap_level_0\" ]"));
    }

    @Test
    public void lookup_autnum_with_application_json_content_type() {
        final Response response = createResource("autnum/102")
                .request("application/json")
                .get();

        assertThat(response.getMediaType(), is(new MediaType("application", "rdap+json")));
        final String entity = response.readEntity(String.class);
        assertThat(entity, containsString("\"handle\" : \"AS102\""));
        assertThat(entity, containsString("\"rdapConformance\" : [ \"rdap_level_0\" ]"));
    }

    @Test
    public void lookup_autnum_within_block() throws Exception {
        try {
            createResource("autnum/1500")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void lookup_autnum_has_abuse_contact() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       dbtest@ripe.net 20120101\n" +
                "source:        TEST");
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
                "source:        TEST");
        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TO2-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "changed:       test@test.net.au 20010816\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(autnum.getEntitySearchResults().get(1).getHandle(), is("TP1-TEST"));
        assertThat(autnum.getEntitySearchResults().get(2).getHandle(), is("AB-TEST"));
        assertThat(autnum.getEntitySearchResults().get(2).getRoles(), contains(Role.ABUSE));
        assertThat(autnum.getEntitySearchResults().get(2).getVCardArray(), hasSize(2));
        assertThat(autnum.getEntitySearchResults().get(2).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(autnum.getEntitySearchResults().get(2).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 358}, text, null], " +
                "[tel, {type=voice}, text, +31 6 12345678]]"));
    }

    // general

    @Test
    public void multiple_modification_gives_correct_events() throws Exception {
        final String response = syncupdate(
                        "aut-num:   AS102\n" +
                        "as-name:   AS-TEST\n" +
                        "descr:     Modified ASN\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "changed:   test@test.net.au 20010816\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n" +
                        "password: test");
        assertThat(response, containsString("Modify SUCCEEDED: [aut-num] AS102"));

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(1));
        assertThat(events.get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertTrue(events.get(0).getEventDate().isBefore(LocalDateTime.now()));
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
                "source:        TEST");
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
                "source:        TEST");
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

        final Ip ip = createResource("ip/192.0.0.128")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getEntitySearchResults().get(0).getHandle(), is("AB-TEST"));
        assertThat(ip.getEntitySearchResults().get(0).getRoles(), contains(Role.ABUSE));
        assertThat(ip.getEntitySearchResults().get(0).getVCardArray(), hasSize(2));
        assertThat(ip.getEntitySearchResults().get(0).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(ip.getEntitySearchResults().get(0).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, null], " +                         // TODO: [ES] no value?
                "[tel, {type=voice}, text, +31 6 12345678]]"));
    }

    // organisation entity

    @Test
    public void lookup_org_entity_handle() throws Exception {
        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void lookup_org_not_found() throws Exception {
        try {
            createResource("entity/ORG-NONE-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void lookup_org_invalid_syntax() throws Exception {
        try {
            createResource("entity/ORG-INVALID")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "Invalid syntax.");
        }
    }

    @Test
    public void lookup_org_entity() throws Exception {
        databaseHelper.addObject("" +
                "organisation:  ORG-ONE-TEST\n" +
                "org-name:      Organisation One\n" +
                "org-type:      LIR\n" +
                "descr:         Test organisation\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "language:      EN\n" +
                "admin-c:       TP2-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "tech-c:        TP2-TEST\n" +
                "mnt-ref:       OWNER-MNT\n" +
                "mnt-by:        OWNER-MNT\n" +
                "changed:       test@test.net.au 20000228\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-ONE-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("ORG-ONE-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getPort43(), is("whois.ripe.net"));
        assertThat(entity.getRdapConformance(), hasSize(1));
        assertThat(entity.getRdapConformance().get(0), equalTo("rdap_level_0"));
        assertThat(entity.getLang(), is("EN"));

        assertThat(entity.getEvents().size(), equalTo(1));
        final Event event = entity.getEvents().get(0);
        assertTrue(event.getEventDate().isBefore(LocalDateTime.now()));
        assertThat(event.getEventAction(), equalTo(Action.LAST_CHANGED));
        assertThat(event.getEventActor(), is(nullValue()));

        assertThat(entity.getEntitySearchResults(), hasSize(3));
        final List<Entity> entities = entity.getEntitySearchResults();
        Collections.sort(entities);
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(1).getRoles(), contains(Role.TECHNICAL));
        assertThat(entities.get(2).getHandle(), is("TP2-TEST"));
        assertThat(entities.get(2).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));

        assertThat(entity.getVCardArray(), hasSize(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Organisation One], " +
                "[kind, {}, text, org], " +
                "[adr, {label=One Org Street}, text, null], " +
                "[email, {}, text, test@ripe.net]]"));

        final List<Link> links = entity.getLinks();
        assertThat(links, hasSize(2));
        Collections.sort(links);
        assertThat(links.get(0).getRel(), equalTo("copyright"));
        assertThat(links.get(0).getValue(), equalTo("http://www.ripe.net/data-tools/support/documentation/terms"));
        assertThat(links.get(0).getHref(), equalTo("http://www.ripe.net/data-tools/support/documentation/terms"));
        assertThat(links.get(1).getRel(), equalTo("self"));
        assertThat(links.get(1).getValue(), equalTo("https://rdap.db.ripe.net/entity/ORG-ONE-TEST"));
        assertThat(links.get(1).getHref(), equalTo("https://rdap.db.ripe.net/entity/ORG-ONE-TEST"));

        assertThat(entity.getRemarks(), hasSize(1));
        assertThat(entity.getRemarks().get(0).getDescription(), contains("Test organisation"));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(3));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(2).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/entity/ORG-ONE-TEST"));
    }

    // search

    // search - domain

    @Test
    public void search_domain_not_found() throws Exception {
        try {
            freeTextIndex.rebuild();
            createResource("domains?name=ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void search_domain_exact_match() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("domains?name=31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    @Test
    public void search_domain_with_wildcard() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("domains?name=*.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    // search - nameserver

    @Test
    public void search_nameserver_not_found() throws Exception {
        try {
            freeTextIndex.rebuild();
            createResource("nameservers?name=ns1.ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void search_nameserver_empty_name() throws Exception {
        try {
            freeTextIndex.rebuild();
            createResource("nameservers?name=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "empty lookup key");
        }
    }

    // search - entities

    // search - entities - person

    @Test
    public void search_entity_person_by_name() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=Test%20Person")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_by_name_lowercase() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=test%20person")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_umlaut() throws Exception {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST");
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=Tëst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_latin1_encoded() throws Exception {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST");
        freeTextIndex.rebuild();

        try {
            createResource("entities?fn=T%EBst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);
        } catch (NotFoundException e) {
            // expected - Jetty uses UTF-8 when decoding characters, not latin1
        }
    }

    @Test
    public void search_entity_person_umlaut_utf8_encoded() throws Exception {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST");
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=T%C3%ABst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_substitution() throws Exception {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST");
        freeTextIndex.rebuild();

        try {
            createResource("entities?fn=Test%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);
            fail();
        } catch (NotFoundException e) {
            // expected (no character substitution)
        }
    }

    @Test
    public void search_entity_person_by_name_not_found() throws Exception {
        try {
            freeTextIndex.rebuild();
            createResource("entities?fn=Santa%20Claus")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void search_entity_person_by_handle() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=TP2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP2-TEST"));
    }

    @Test
    public void search_entity_person_by_handle_not_found() throws Exception {
        try {
            freeTextIndex.rebuild();
            createResource("entities?handle=XYZ-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorResponse(e, "");
        }
    }

    // search - entities - role

    @Test
    public void search_entity_role_by_name() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=FR*-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    @Test
    public void search_entity_role_by_handle() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=F*st%20Role")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    // search - entities - organisation

    @Test
    public void search_entity_organisation_by_name() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=organisation")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_name_mixed_case() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=ORGanisAtioN")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_name_with_wildcard() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=organis*tion")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_prefix() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=*TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_middle() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG*TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_suffix() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG*")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_prefix_middle_and_suffix() throws Exception {
        freeTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=*ORG*TEST*")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_without_query_params() throws Exception {
        try {
            createResource("entities")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void search_entity_both_fn_and_handle_query_params() throws Exception {
        try {
            createResource("entities?fn=XXXX&handle=YYYY")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "");
        }
    }

    @Test
    public void search_entity_empty_name() throws Exception {
        try {
            createResource("entities?fn=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            // expected
        }
    }

    @Test
    public void search_entity_empty_handle() throws Exception {
        try {
            createResource("entities?handle=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorResponse(e, "empty search term");
        }
    }

    @Test
    public void search_entity_multiple_object_response() {
        freeTextIndex.rebuild();

        final SearchResult result = createResource("entities?handle=*TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(
                Lists.transform(entities, new Function<Entity, String>() {
                    @Override
                    public String apply(@Nullable Entity entity) {
                        return entity.getHandle();
                    }
            }), containsInAnyOrder("TP1-TEST", "TP2-TEST", "PP1-TEST", "FR1-TEST", "ORG-TEST1-TEST"));
        assertThat(
                Iterables.transform(
                        Iterables.filter(entities, new Predicate<Entity>() {
                            @Override
                            public boolean apply(@Nullable Entity entity) {
                                if (entity.getHandle().equals("ORG-TEST1-TEST")) {
                                    return true;
                                }
                                return false;
                            }
                }).iterator().next().getNotices(), new Function<Notice, String>() {
                    @Override
                    public String apply(@Nullable Notice notice) {
                        return notice.getTitle();
                    }
            }), containsInAnyOrder("Source", "Filtered"));
        assertThat(result.getNotices(), hasSize(1));
        assertThat(result.getNotices().get(0).getTitle(), is("Terms and Conditions"));
    }

    // helper methods

    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("rdap/%s", path));
    }

    private String syncupdate(String data) {
        WebTarget resource = RestTest.target(getPort(), String.format("whois/syncupdates/test"));
        return resource.request()
                .post(javax.ws.rs.client.Entity.entity("DATA=" + RestClientUtils.encode(data),
                        MediaType.APPLICATION_FORM_URLENCODED),
                        String.class);

    }

    private void assertErrorResponse(final ClientErrorException exception, final String expectedErrorText) {
        assertThat(exception.getResponse().readEntity(String.class), containsString(String.format("{\n" +
                "  \"links\" : [ {\n" +
                "    \"rel\" : \"self\"\n" +
                "  }, {\n" +
                "    \"value\" : \"http://www.ripe.net/data-tools/support/documentation/terms\",\n" +
                "    \"rel\" : \"copyright\",\n" +
                "    \"href\" : \"http://www.ripe.net/data-tools/support/documentation/terms\"\n" +
                "  } ],\n" +
                "  \"rdapConformance\" : [ \"rdap_level_0\" ],\n" +
                "  \"notices\" : [ {\n" +
                "    \"title\" : \"Terms and Conditions\",\n" +
                "    \"description\" : [ \"This is the RIPE Database query service. The objects are in RDAP format.\" ],\n" +
                "    \"links\" : [ {\n" +
                "      \"rel\" : \"terms-of-service\",\n" +
                "      \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\",\n" +
                "      \"type\" : \"application/pdf\"\n" +
                "    } ]\n" +
                "  } ],\n" +
                "  \"port43\" : \"whois.ripe.net\",\n" +
                "  \"errorCode\" : %d,\n" +
                "  \"title\" : \"%s\",\n" +
                "  \"description\" : [ ]\n" +           // TODO: [ES] omit empty arrays
                "}", exception.getResponse().getStatus(), expectedErrorText)));
    }
}
