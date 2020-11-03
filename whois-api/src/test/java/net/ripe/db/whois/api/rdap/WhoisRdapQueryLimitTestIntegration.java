package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.query.support.TestWhoisLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhoisRdapQueryLimitTestIntegration extends AbstractRdapIntegrationTest {

    @Autowired
    FullTextIndex fullTextIndex;
    @Autowired
    TestWhoisLog queryLog;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("rdap.search.max.results", "2");
    }

    @Before
    public void setup() {
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
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
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
                "domain:        17.45.212.in-addr.arpa\n" +
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
                "domain:        64.67.217.in-addr.arpa\n" +
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
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test2 organisation\n" +
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
                "organisation:  ORG-TEST3-TEST\n" +
                "org-name:      Test3 organisation\n" +
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
        ipTreeUpdater.rebuild();
    }

    // search - domain
    @Test
    public void search_domain_with_wildcard() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("domains?name=*.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().size(), equalTo(2));
        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("17.45.212.in-addr.arpa"));
        assertThat(response.getDomainSearchResults().get(1).getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(response.getNotices(), hasSize(2));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("limited search results to 2 maximum"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_prefix() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=*TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getNotices(), hasSize(1));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("Terms and Conditions"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_middle() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG*TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults(), hasSize(2));
        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getEntitySearchResults().get(1).getHandle(), equalTo("ORG-TEST2-TEST"));
        assertThat(response.getNotices(), hasSize(2));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("limited search results to 2 maximum"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_suffix() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG*")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults(), hasSize(2));
        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getEntitySearchResults().get(1).getHandle(), equalTo("ORG-TEST2-TEST"));
        assertThat(response.getNotices(), hasSize(2));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("limited search results to 2 maximum"));
    }

    @Test
    public void search_entity_organisation_by_handle_with_wildcard_prefix_middle_and_suffix() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=*ORG*TEST*")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults(), hasSize(2));
        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getEntitySearchResults().get(1).getHandle(), equalTo("ORG-TEST2-TEST"));
        assertThat(response.getNotices(), hasSize(2));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("limited search results to 2 maximum"));
    }

    @Test
    public void search_entity_organisation_by_name_with_wildcard() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=organis*tion")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults(), hasSize(2));
        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getEntitySearchResults().get(1).getHandle(), equalTo("ORG-TEST2-TEST"));
        assertThat(response.getNotices(), hasSize(2));
        assertThat(response.getNotices().get(0).getTitle(), equalTo("limited search results to 2 maximum"));
    }
}
