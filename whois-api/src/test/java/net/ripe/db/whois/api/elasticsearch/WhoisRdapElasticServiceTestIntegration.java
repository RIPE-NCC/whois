package net.ripe.db.whois.api.elasticsearch;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Event;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("ElasticSearchTest")
public class WhoisRdapElasticServiceTestIntegration extends AbstractElasticSearchIntegrationTest {

    private static final String WHOIS_INDEX = "whois_rdap";
    private static final String METADATA_INDEX = "metadata_rdap";

    private static final String LOCALHOST_WITH_PREFIX = "127.0.0.1/32";
    private static final String LOCALHOST = "127.0.0.1";

    @Autowired
    private AccessControlListManager accessControlListManager;
    @Autowired
    private IpResourceConfiguration ipResourceConfiguration;
    @Autowired
    private TestPersonalObjectAccounting testPersonalObjectAccounting;

    @BeforeAll
    public static void setUpProperties() {
        System.setProperty("elastic.whois.index", WHOIS_INDEX);
        System.setProperty("elastic.commit.index", METADATA_INDEX);
        System.setProperty("rdap.sources", "TEST-GRS");
        System.setProperty("rdap.redirect.test", "https://rdap.test.net");
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");

        // We only enable fulltext indexing here, so it doesn't slow down the rest of the test suite
        System.setProperty("dir.fulltext.index", "var${jvmId:}/idx");
    }

    @AfterAll
    public static void resetProperties() {
        System.clearProperty("elastic.commit.index");
        System.clearProperty("elastic.whois.index");
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.test");
        System.clearProperty("rdap.public.baseUrl");
        System.clearProperty("dir.fulltext.index");
    }

    @BeforeEach
    public void setup() throws IOException {
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
                "source:        TEST");
        databaseHelper.updateObject("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Test Person2\n" +
                "address:       Test Address\n" +
                "phone:         +61-1234-1234\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       TP2-TEST\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
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
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
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
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:       AS100 - AS200\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TEST1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
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
                "source:         TEST");
        ipTreeUpdater.rebuild();

        elasticFullTextIndex.update();
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    @Override
    public String getWhoisIndex() {
        return WHOIS_INDEX;
    }

    @Override
    public String getMetadataIndex() {
        return METADATA_INDEX;
    }

    // domain

    @Test
    public void lookup_domain_object() {
        final Domain domain = createResource("domain/31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getObjectClassName(), is("domain"));

        assertThat(domain.getNameservers(), hasSize(2));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getLdhName(), domain.getNameservers().get(1).getLdhName()),
                    containsInAnyOrder("ns1.test.com.au", "ns2.test.com.au"));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getLdhName(), domain.getNameservers().get(1).getLdhName()),
                    containsInAnyOrder("ns1.test.com.au", "ns2.test.com.au"));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getIpAddresses(), domain.getNameservers().get(1).getIpAddresses()),
                    containsInAnyOrder(new Nameserver.IpAddresses(Lists.newArrayList("10.0.0.1/32"), null),
                                        new Nameserver.IpAddresses(null, Lists.newArrayList("2001:10::2/128"))));

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
        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/domain/31.12.202.in-addr.arpa");

        assertCopyrightLink(domain.getLinks(), "https://rdap.db.ripe.net/domain/31.12.202.in-addr.arpa");
    }

    @Test
    public void lookup_domain_object_is_case_insensitive() {
        final Domain domain = createResource("domain/31.12.202.IN-AddR.ARPA")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getObjectClassName(), is("domain"));
    }

    @Test
    public void domain_not_found() {
        try {
            createResource("domain/10.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorStatus(e, 404);
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
            assertErrorTitle(e, "RIPE NCC does not support forward domain queries.");
        }
    }


    // search - domain

    @Test
    public void search_domain_not_found() {
        try {
            createResource("domains?name=ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorTitle(e, "not found");
        }
    }

    @Test
    public void search_domain_exact_match() {
        final SearchResult response = createResource("domains?name=31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    @Test
    public void search_domain__not_exact_match() {

        final SearchResult response = createResource("domains?name=in-addr")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    @Test
    public void search_domain_is_case_insensitive() {

        final SearchResult response = createResource("domains?name=31.12.202.IN-AddR.arpa")     // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }


    // search - entities - person

    @Test
    public void search_entity_person_by_name() {

        final SearchResult response = createResource("entities?fn=Test%20Person")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_object_deleted_before_index_updated() {
        final RpslObject person = RpslObject.parse("person: Lost Person\nnic-hdl: LP1-TEST\nsource: TEST");
        databaseHelper.addObject(person);
        rebuildIndex();
        databaseHelper.deleteObject(person);

        try {
            createResource("entities?fn=Lost%20Person")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorTitle(e, "not found");
        }
    }

    @Test
    public void search_entity_person_by_name_is_case_insensitive() {
        final SearchResult response = createResource("entities?fn=tESt%20PeRSOn")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_umlaut() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST\nsource: TEST");
        rebuildIndex();
        final SearchResult response = createResource("entities?fn=Tëst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_latin1_encoded() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST");
        rebuildIndex();

        try {
            createResource("entities?fn=T%EBst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);
            fail();
        } catch (NotFoundException e) {
            // expected - Jetty uses UTF-8 when decoding characters, not latin1
        }
    }

    @Test
    public void search_entity_person_umlaut_utf8_encoded() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST\nsource: TEST");
        rebuildIndex();

        final SearchResult response = createResource("entities?fn=T%C3%ABst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_substitution() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST\nsource: TEST");
        rebuildIndex();

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
    public void search_entity_person_by_name_not_found() {
        try {
            createResource("entities?fn=Santa%20Claus")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorTitle(e, "not found");
        }
    }

    @Test
    public void search_entity_person_by_handle() {

        final SearchResult response = createResource("entities?handle=TP2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP2-TEST"));
    }

    @Test
    public void search_entity_person_by_handle_is_case_insensitive() {

        final SearchResult response = createResource("entities?handle=Tp2-tESt")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP2-TEST"));
    }

    @Test
    public void search_entity_person_by_handle_not_found() {
        try {
            createResource("entities?handle=XYZ-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (NotFoundException e) {
            assertErrorTitle(e, "not found");
        }
    }

    // search - entities - role

    @Test
    public void search_entity_role_by_name() {

        final SearchResult response = createResource("entities?handle=FR*-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    @Test
    public void search_entity_role_by_handle() {

        final SearchResult response = createResource("entities?fn=F*st%20Role")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    // search - entities - organisation

    @Test
    public void search_entity_organisation_by_name() {

        final SearchResult response = createResource("entities?fn=organisation")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_name_is_case_insensitive() {

        final SearchResult response = createResource("entities?fn=ORGanisAtioN")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle() {

        final SearchResult response = createResource("entities?handle=ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_without_query_params() {
        try {
            createResource("entities")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorTitle(e, "bad request");
        }
    }

    @Test
    public void search_entity_both_fn_and_handle_query_params() {
        try {
            createResource("entities?fn=XXXX&handle=YYYY")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorTitle(e, "bad request");
        }
    }

    @Test
    public void search_entity_empty_name() {
        try {
            createResource("entities?fn=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorTitle(e, "empty search term");
        }
    }

    @Test
    public void search_entity_empty_handle() {
        try {
            createResource("entities?handle=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
            fail();
        } catch (BadRequestException e) {
            assertErrorTitle(e, "empty search term");
        }
    }

    @Test
    public void search_entity_multiple_object_response() {

        final SearchResult result = createResource("entities?handle=*TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(
            result.getEntitySearchResults()
                .stream()
                .map(Entity::getHandle)
                .collect(Collectors.toList()),
            containsInAnyOrder("TP1-TEST", "TP2-TEST", "PP1-TEST", "FR1-TEST", "ORG-TEST1-TEST"));
        assertThat(
            result.getEntitySearchResults()
                .stream()
                .filter(entity -> entity.getHandle().equals("ORG-TEST1-TEST"))
                .map(RdapObject::getNotices)
                .flatMap(Collection::stream)
                .map(Notice::getTitle)
                .collect(Collectors.toList()),
            containsInAnyOrder("Source", "Filtered"));
        assertThat(result.getNotices(), hasSize(1));
        assertThat(result.getNotices().get(0).getTitle(), is("Terms and Conditions"));
    }

    @Test
    public void lookup_person_entity_acl_denied() {
        try {
            databaseHelper.insertAclIpDenied(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();

            try {
                createResource("entities?handle=PP1-TEST")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(SearchResult.class);
                fail();
            } catch (ClientErrorException e) {
                assertErrorStatus(e, 429);
                assertErrorTitleContains(e, "%ERROR:201: access denied for 127.0.0.1");
            }
        } finally {
            databaseHelper.unban(LOCALHOST_WITH_PREFIX);
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    @Test
    public void lookup_person_acl_counted() throws Exception {
        final InetAddress localhost = InetAddress.getByName(LOCALHOST);
        try {
            final int limit = accessControlListManager.getPersonalObjects(localhost);

            createResource("entities?handle=PP1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);

            final int remaining = accessControlListManager.getPersonalObjects(localhost);
            assertThat(remaining, is(limit-1));

        } finally {
            ipResourceConfiguration.reload();
            testPersonalObjectAccounting.resetAccounting();
        }
    }

    private void assertCommon(RdapObject object) {
        assertThat(object.getPort43(), is("whois.ripe.net"));
        assertThat(object.getRdapConformance(), hasSize(1));
        assertThat(object.getRdapConformance().get(0), equalTo("rdap_level_0"));
    }

    private void assertCopyrightLink(final List<Link> links, final String value) {
        assertThat(links, hasSize(2));
        Collections.sort(links);

        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));

        assertThat(links.get(1).getRel(), is("self"));
        assertThat(links.get(1).getValue(), is(value));
        assertThat(links.get(1).getHref(), is(value));
    }

    private void assertTnCNotice(final Notice notice, final String value) {
        assertThat(notice.getTitle(), is("Terms and Conditions"));
        assertThat(notice.getDescription(), contains("This is the RIPE Database query service. The objects are in RDAP format."));
        assertThat(notice.getLinks().get(0).getHref(), is("http://www.ripe.net/db/support/db-terms-conditions.pdf"));

        assertThat(notice.getLinks().get(0).getRel(), is("terms-of-service"));
        assertThat(notice.getLinks().get(0).getHref(), is("http://www.ripe.net/db/support/db-terms-conditions.pdf"));
        assertThat(notice.getLinks().get(0).getType(), is("application/pdf"));
        assertThat(notice.getLinks().get(0).getValue(), is(value));
    }

    // helper methods

    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("rdap/%s", path));
    }

    protected String syncupdate(String data) {
        WebTarget resource = RestTest.target(getPort(), String.format("whois/syncupdates/test"));
        return resource.request()
                .post(javax.ws.rs.client.Entity.entity("DATA=" + RestClientUtils.encode(data),
                                MediaType.APPLICATION_FORM_URLENCODED),
                        String.class);

    }

    protected void assertErrorTitle(final ClientErrorException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), is(title));
    }

    protected void assertErrorStatus(final ClientErrorException exception, final int status) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorCode(), is(status));
    }

    protected void assertErrorTitleContains(final ClientErrorException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), containsString(title));
    }
}
