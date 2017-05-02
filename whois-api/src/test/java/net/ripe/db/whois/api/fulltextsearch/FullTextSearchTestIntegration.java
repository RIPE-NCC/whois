package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.fulltextsearch.FullTextSolrUtils.parseResponse;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class FullTextSearchTestIntegration extends AbstractIntegrationTest {
    @Autowired FullTextIndex fullTextIndex;

    @BeforeClass
    public static void setProperty() {
        // We only enable fulltext indexing here, so it doesn't slow down the rest of the test suite
        System.setProperty("dir.fulltext.index", "var${jvmId:}/idx");
    }

    @AfterClass
    public static void clearProperty() {
        System.clearProperty("dir.fulltext.index");
    }

    @Before
    public void setUp() throws Exception {
        fullTextIndex.rebuild();
    }

    @Test
    public void search_no_params() throws Exception {
        try {
            query("");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("No query parameter."));
        }
    }

    @Test
    public void search_empty_query_param() throws Exception {
        try {
            query("q=");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Invalid query"));
        }
    }

    @Test
    public void search_single_result() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE"));
        fullTextIndex.update();

        final QueryResponse queryResponse = query("q=DEV-MNT");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        assertThat(solrDocument.getFirstValue("primary-key"), is("1"));
        assertThat(solrDocument.getFirstValue("object-type"), is("mntner"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("DEV-MNT"));
        assertThat(solrDocument.getFirstValue("mntner"), is("DEV-MNT"));
    }

    @Test
    public void search_single_result_json() throws Exception {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT\n" +
                "source: RIPE"));
        fullTextIndex.update();

        final SearchResponse queryResponse = queryJson("q=DEV-MNT");

        assertThat(queryResponse.getResult().getDocs(), hasSize(1));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs(), hasSize(4));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(0).getName(), is("primary-key"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(0).getValue(), is("1"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(1).getName(), is("object-type"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(1).getValue(), is("mntner"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(2).getName(), is("lookup-key"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(2).getValue(), is("DEV-MNT"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(3).getName(), is("mntner"));
        assertThat(queryResponse.getResult().getDocs().get(0).getStrs().get(3).getValue(), is("DEV-MNT"));
    }


    @Test
    public void search_multiple_results_with_highlighting() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV2-MNT\n" +
                "remarks: Second remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV3-MNT\n" +
                "remarks: Other remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV4-MNT\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=remark&hl=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(getHighlightKeys(queryResponse), containsInAnyOrder("1", "2", "3"));
        assertThat(getHighlightValues(queryResponse), containsInAnyOrder("Some <b>remark<\\/b>", "Second <b>remark<\\/b>", "Other <b>remark<\\/b>"));
    }

    @Test
    public void search_multiple_results_with_facet() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV2-MNT\n" +
                "remarks: Another remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                "nic-hdl: AA1-RIPE\n" +
                "remarks: Other remark\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=remark&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets.size(), is(1));
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(2));
        assertThat(facet.getValues().toString(), containsString("mntner (2)"));
        assertThat(facet.getValues().toString(), containsString("person (1)"));
    }

    @Test
    public void search_object_contains_control_character() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                "descr: acc\u0003\u0028s 4 Mbps\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=DEV1-MNT&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults().get(0).getFirstValue("descr"), is("acc(s 4 Mbps"));
    }

    @Test
    public void no_exact_match_for_highlighting() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  AARD-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "domain:          198.76.217.in-addr.arpa\n" +
                "descr:           T.E.S.T. Ltd\n" +
                "nserver:         ns.foo.ua\n" +
                "nserver:         ns2.foo.ua\n" +
                "notify:          bar@foo.ua\n" +
                "source:          RIPE\n" +
                "mnt-by:          AARD-MNT"));
        fullTextIndex.update();

        final QueryResponse queryResponse = query("q=test&hl=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        final Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        assertThat(highlighting.keySet(), hasSize(1));
        assertThat(highlighting.get("2").keySet(), contains("descr"));
        assertThat(highlighting.get("2").values(), contains(Lists.newArrayList("<b>T.E.S.T<\\/b>. Ltd")));
    }

    @Test
    public void search_no_match() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE"));
        fullTextIndex.update();

        final QueryResponse queryResponse = query("q=10.0.0.0");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
    }

    @Test
    public void search_word_match_subword_case_change() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=donald");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_subword_dash_separator() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  MNT-TESTUA\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=TESTUA");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_first_subword() throws Exception {
        databaseHelper.addObject(
                "person: Test Person\n" +
                 "nic-hdl: TP1-TEST");
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=TP1");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_original() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald1\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=mcdonald1");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_matching_object_type() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=mcdonald+AND+object-type%3Aperson");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_non_matching_object_type() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=mcdonald+AND+object-type%3Ainetnum");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
    }

    @Test
    public void search_by_attribute_and_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=(nic-hdl:(AA1-RIPE))+AND+(object-type:person)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }


    @Test
    public void search_hyphenated_complete_word() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=NINJA-MNT");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_hyphenated_partial_word() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=NINJA");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_match_all_terms() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: JM1-RIPE\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: Kate McDonald\n" +
                "nic-hdl: KM1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=John+McDonald");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_for_deleted_object() {
        final RpslObject object = RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: JM1-RIPE\n" +
                "source: RIPE");
        databaseHelper.addObject(object);
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=JM1-RIPE")), is(1L));

        databaseHelper.deleteObject(object);
        fullTextIndex.scheduledUpdate();

        assertThat(numFound(query("q=JM1-RIPE")), is(0L));
    }

    @Test
    public void search_with_forward_slash() {
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "source: TEST"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48")), is(1L));
        assertThat(numFound(query("q=212.166.64.0%2F19")), is(0L));
    }

    @Test
    public void nullpointerbug() {
        assertThat(numFound(query("q=%28http%5C%3A%2F%2Fvv.uka.ru%29")), is(0L));
    }

    @Test
    public void search_inetnum() throws Exception {
        databaseHelper.addObject(
               "inetnum:        193.0.0.0 - 193.0.0.255\n" +
               "netname:        RIPE-NCC\n" +
               "source:         RIPE");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=193.0.0.0")), is(1L));
        assertThat(numFound(query("q=193.0.0.255")), is(1L));
        assertThat(numFound(query("q=193%2E0%2E0%2E255")), is(1L));
        assertThat(numFound(query("q=%28193%2E0%2E0%2E255%29")), is(1L));
        assertThat(numFound(query("q=193")), is(1L));
        assertThat(numFound(query("q=193.0")), is(1L));
        assertThat(numFound(query("q=193.0.0")), is(1L));
        assertThat(numFound(query("q=ripe-ncc")), is(1L));
        assertThat(numFound(query("q=ripe")), is(1L));
        assertThat(numFound(query("q=ncc")), is(1L));
        assertThat(numFound(query("q=ripencc")), is(1L));
    }


    @Test
    public void search_inetnum_with_prefix_length() throws Exception {
        databaseHelper.addObject(
                "inetnum:        10.0.0.0/24\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=10.0.0.0/24")), is(1L));
    }

    @Test
    public void search_inetnum_multiple_matches() throws Exception {
        databaseHelper.addObject(
                "inetnum:        193.0.0.0 - 193.0.0.255\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
        databaseHelper.addObject(
                "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=193.0.0.0")), is(1L));
        assertThat(numFound(query("q=193.1.0.0")), is(1L));
        assertThat(numFound(query("q=193")), is(2L));
    }

    @Test
    public void search_inet6num() throws Exception {
        databaseHelper.addObject(
                "inet6num: 2001:0638:0501::/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=%282001%29")), is(1L));
        assertThat(numFound(query("q=%282001%5C%3A0638%29")), is(1L));
        assertThat(numFound(query("q=%282001%5C%3A0638%5C%3A0501%29")), is(1L));
        assertThat(numFound(query("q=%282001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48%29")), is(1L));
        assertThat(numFound(query("q=2001")), is(1L));
        assertThat(numFound(query("q=2001%5C%3A0638")), is(1L));
        assertThat(numFound(query("q=2001%5C%3A0638%5C%3A0501")), is(1L));
        assertThat(numFound(query("q=2001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48")), is(1L));
    }

    @Test
    public void search_inet6num_double_colons() throws Exception {
        databaseHelper.addObject(
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=2a00")), is(1L));
        assertThat(numFound(query("q=2a00%5C%3A1f78")), is(1L));       // need to escape single colon (used as separator by lucene)
        assertThat(numFound(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48")), is(1L));
    }

    @Test
    public void search_inet6num_multiple_matches() throws Exception {
        databaseHelper.addObject(
                "inet6num: 2a00:1f78:7a2b:2001::/64\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
        databaseHelper.addObject(
                "inet6num: 2a00:1f11:7777:2a98::/64\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=2a00")), is(2L));
    }

    @Test
    public void search_filter_comma_when_indexing() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     Company, Ltd\n" +
                "org-type:     OTHER\n" +
                "descr:        test org\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=Company")), is(1L));
    }

    @Test
    public void search_filter_comma_on_query_term() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     Company Ltd\n" +
                "org-type:     OTHER\n" +
                "descr:        test org\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=company,")), is(1L));
    }

    @Test
    public void search_filter_quotes_when_indexing() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     \"Company\" Ltd\n" +
                "org-type:     OTHER\n" +
                "descr:        test org\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=company")), is(1L));
    }

    @Test
    public void search_multiple_matches_but_only_one_result() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     ORG-TOS1-TEST\n" +
                "org-type:     OTHER\n" +
                "descr:        ORG-TOS1-TEST\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=ORG-TOS1-TEST")), is(1L));
    }

    @Test
    public void search_match_partial_email_address() throws Exception {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     ORG-TOS1-TEST\n" +
                "org-type:     OTHER\n" +
                "descr:        ORG-TOS1-TEST\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=test.com")), is(1L));
    }

    @Test
    public void search_full_match_email_address() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                "org-name:     ORG-TOS1-TEST\n" +
                "org-type:     OTHER\n" +
                "descr:        ORG-TOS1-TEST\n" +
                "address:      street 1\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       RIPE\n"));
        fullTextIndex.rebuild();

        assertThat(numFound(query("q=org1@test.com")), is(1L));
    }

    @Test
    public void search_full_match_person_name() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=john%20mcdonald");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_inet6num_escape_forward_slash() {
        databaseHelper.addObject(RpslObject.parse("inet6num: 2001:0638:0501::/48"));
        fullTextIndex.rebuild();

        final QueryResponse queryResponse = query("q=%282001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48%29");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        assertThat(solrDocument.getFirstValue("primary-key"), is("1"));
        assertThat(solrDocument.getFirstValue("object-type"), is("inet6num"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("2001:0638:0501::/48"));
        assertThat(solrDocument.getFirstValue("inet6num"), is("2001:0638:0501::/48"));
    }

    @Test
    public void search_highlight_escaping() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: DEV mntner\n" +
                "source: RIPE"));
        fullTextIndex.update();

        final QueryResponse queryResponse = query("q=DEV&hl=true&hl.simple.pre=%3Cb%3E&hl.simple.post=%3C/b%3E");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        assertThat(queryResponse.getHighlighting().keySet(), contains("1"));
        assertThat(queryResponse.getHighlighting().get("1").keySet(), hasSize(3));
        assertThat(queryResponse.getHighlighting().get("1").get("lookup-key"), contains("<b>DEV<\\/b>-MNT"));
        assertThat(queryResponse.getHighlighting().get("1").get("mntner"), contains("<b>DEV<\\/b>-MNT"));
        assertThat(queryResponse.getHighlighting().get("1").get("remarks"), contains("<b>DEV<\\/b> mntner"));
    }

    // helper methods

    private QueryResponse query(final String queryString) {
        return parseResponse(
            RestTest.target(getPort(), String.format("whois/fulltextsearch/select?%s",queryString))
                    .request()
                    .get(String.class));
    }

    private SearchResponse queryJson(final String queryString) {
        return RestTest.target(getPort(), String.format("whois/fulltextsearch/select.json?%s", queryString))
                .request()
                .get(SearchResponse.class);
    }

    private Set<String> getHighlightKeys(final QueryResponse queryResponse) {
        return queryResponse.getHighlighting().keySet();
    }

    private List<String> getHighlightValues(final QueryResponse queryResponse) {
        return queryResponse.getHighlighting()
                .values().stream()
                    .map(entry -> entry.values())
                    .flatMap(next -> next.stream())
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
    }

    private long numFound(final QueryResponse queryResponse) {
        return queryResponse.getResults().getNumFound();
    }

}

