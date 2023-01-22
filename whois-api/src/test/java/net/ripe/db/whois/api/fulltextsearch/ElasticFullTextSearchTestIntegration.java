package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.elasticsearch.AbstractElasticSearchIntegrationTest;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.dao.jdbc.JdbcAccessControlListDao;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.Inet4Address;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.fulltextsearch.FullTextSolrUtils.parseResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("ElasticSearchTest")
public class ElasticFullTextSearchTestIntegration  extends AbstractElasticSearchIntegrationTest {

    private static final String WHOIS_INDEX = "whois_fulltext";
    private static final String METADATA_INDEX = "metadata_fulltext";


    @Autowired TestPersonalObjectAccounting testPersonalObjectAccounting;
    @Autowired JdbcAccessControlListDao jdbcAccessControlListDao;
    @Autowired IpResourceConfiguration ipResourceConfiguration;

    private JdbcTemplate aclJdbcTemplate;

    @BeforeAll
    public static void setUpProperties() {
        System.setProperty("elastic.whois.index", WHOIS_INDEX);
        System.setProperty("elastic.commit.index", METADATA_INDEX);
        System.setProperty("fulltext.search.max.results", "3");
    }

    @AfterAll
    public static void resetProperties() {
        System.clearProperty("elastic.commit.index");
        System.clearProperty("elastic.whois.index");
        System.clearProperty("fulltext.search.max.results");
    }

    @BeforeEach
    public void setUp() throws IOException {
       rebuildIndex();

        testPersonalObjectAccounting.resetAccounting();
    }

    @Override
    public String getWhoisIndex() {
        return WHOIS_INDEX;
    }

    @Override
    public String getMetadataIndex() {
        return METADATA_INDEX;
    }

    @Autowired
    public void setAclDataSource(@Qualifier("aclDataSource") DataSource dataSource) {
        this.aclJdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void search_no_params() {
        try {
            query("");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("No query parameter."));
        }
    }

    @Test
    public void search_empty_query_param() {
        try {
            query("q=");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Invalid query"));
        }
    }

    @Test
    public void search_single_result() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE"));
        rebuildIndex();

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
    public void search_single_result_object_deleted_before_index_updated() {
        final RpslObject mntner = RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE");
        databaseHelper.addObject(mntner);
         rebuildIndex();
        databaseHelper.deleteObject(mntner);

        final QueryResponse queryResponse = query("q=DEV-MNT");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
        assertThat(queryResponse.getResults(), hasSize(0));
    }

    @Test
    public void search_single_result_json() {
        databaseHelper.addObject(RpslObject.parse("mntner: DEV-MNT\n" +
                "source: RIPE"));
         rebuildIndex();

        final SearchResponse searchResponse = queryJson("q=DEV-MNT");

        assertThat(searchResponse.getResult().getDocs(), hasSize(1));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs(), hasSize(4));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(0).getName(), is("primary-key"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(0).getValue(), is("1"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(1).getName(), is("object-type"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(1).getValue(), is("mntner"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(2).getName(), is("lookup-key"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(2).getValue(), is("DEV-MNT"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(3).getName(), is("mntner"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(3).getValue(), is("DEV-MNT"));
    }


    @Test
    public void search_multiple_results_with_highlighting() {
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
         rebuildIndex();

        final QueryResponse queryResponse = query("q=remark&hl=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(getHighlightKeys(queryResponse), containsInAnyOrder("1", "2", "3"));
        assertThat(getHighlightValues(queryResponse), containsInAnyOrder("Some <b>remark<\\/b>", "Second <b>remark<\\/b>", "Other <b>remark<\\/b>"));
    }

    @Test
    public void search_multiple_results_with_facet() {
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
         rebuildIndex();

        final QueryResponse queryResponse = query("q=remark&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets, hasSize(1));
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(2));
        assertThat(facet.getValues().toString(), containsString("mntner (2)"));
        assertThat(facet.getValues().toString(), containsString("person (1)"));
    }

    @Test
    public void search_multiple_results_with_search_limit() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                        "remarks: Some remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV2-MNT\n" +
                        "remarks: Another remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV3-MNT\n" +
                        "remarks: Some remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "remarks: Other remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Middle Last\n" +
                        "nic-hdl: AA2-RIPE\n" +
                        "remarks: Other remark\n" +
                        "source: RIPE"));

         rebuildIndex();

        final QueryResponse queryResponse = query("q=remark&facet=true");

        //search limit to 3
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));


        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets, hasSize(1));

        //will show true count
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(2));
        assertThat(facet.getValues().toString(), containsString("mntner (3)"));
        assertThat(facet.getValues().toString(), containsString("person (2)"));
    }

    @Test
    public void search_list_value_attribute_single_value_and_comment() {
        databaseHelper.addObject("mntner: AA1-MNT\nsource: RIPE");
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-AA1-RIPE\n" +
                "mnt-ref: AA1-MNT # include this comment\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(AA1)+AND+(object-type:organisation)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        assertThat(solrDocument.getFirstValue("primary-key"), is("2"));
        assertThat(solrDocument.getFirstValue("object-type"), is("organisation"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("organisation"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("mnt-ref"), is("AA1-MNT # include this comment"));
    }

    @Test
    public void search_list_value_attribute_comment() {
        databaseHelper.addObject("mntner: AA1-MNT\nsource: RIPE");
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-AA1-RIPE\n" +
                "mnt-ref: AA1-MNT # include this comment\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(include)+AND+(object-type:organisation)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        assertThat(solrDocument.getFirstValue("primary-key"), is("2"));
        assertThat(solrDocument.getFirstValue("object-type"), is("organisation"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("organisation"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("mnt-ref"), is("AA1-MNT # include this comment"));
    }

    @Test
    public void search_list_value_attribute_single_value_and_comment_with_facet() {
        databaseHelper.addObject("mntner: AA1-MNT\nsource: RIPE");
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-AA1-RIPE\n" +
                "mnt-ref: AA1-MNT # include this comment\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(AA1)+AND+(object-type:organisation)&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets, hasSize(1));
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(1));
        assertThat(facet.getValues().toString(), containsString("organisation (1)"));
    }

    @Test
    public void search_list_value_attribute_multiple_values_and_comment() {
        databaseHelper.addObject("mntner: AA1-MNT\nsource: RIPE");
        databaseHelper.addObject("mntner: AA2-MNT\nsource: RIPE");
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-AA1-RIPE\n" +
                "mnt-ref: AA1-MNT, AA2-MNT # include this comment\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(AA1)+AND+(object-type:organisation)&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        assertThat(solrDocument.getFirstValue("primary-key"), is("3"));
        assertThat(solrDocument.getFirstValue("object-type"), is("organisation"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("organisation"), is("ORG-AA1-RIPE"));
        assertThat(solrDocument.getFirstValue("mnt-ref"), is("AA1-MNT, AA2-MNT # include this comment"));
    }

    @Test
    public void search_object_contains_control_character() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                "descr: acc\u0003\u0028s 4 Mbps\n" +
                "source: RIPE"));
         rebuildIndex();

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
         rebuildIndex();

        final QueryResponse queryResponse = query("q=TEST&hl=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        final Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        assertThat(highlighting.keySet(), hasSize(1));
        assertThat(highlighting.get("2").keySet(), contains("descr"));
        assertThat(highlighting.get("2").values(), contains(Lists.newArrayList("<b>T.E.S.T<\\/b>. Ltd")));
    }

    @Test
    public void search_no_match() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=10.0.0.0");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
    }

    @Test
    public void search_word_match_subword_case_change() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=Donald");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_subword_dash_separator() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  MNT-TESTUA\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=TESTUA");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_first_subword() {
        databaseHelper.addObject(
                "person: Test Person\n" +
                 "nic-hdl: TP1-TEST");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=TP1");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_original() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald1\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=mcdonald1");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_matching_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=mcdonald+AND+object-type%3Aperson");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_non_matching_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

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
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(nic-hdl:(AA1-RIPE))+AND+(object-type:person)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }


    @Test
    public void search_hyphenated_complete_word() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=NINJA-MNT");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_hyphenated_partial_word() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=NINJA");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_match_all_terms() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: JM1-RIPE\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: Kate McDonald\n" +
                "nic-hdl: KM1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

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
         rebuildIndex();

        assertThat(numFound(query("q=JM1-RIPE")), is(1L));

        databaseHelper.deleteObject(object);
        rebuildIndex();

        assertThat(numFound(query("q=JM1-RIPE")), is(0L));
    }

    @Test
    public void search_with_forward_slash() {
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "source: TEST"));
         rebuildIndex();

        assertThat(numFound(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48")), is(1L));
        assertThat(numFound(query("q=212.166.64.0%2F19")), is(0L));
    }

    @Test
    public void nullpointerbug() {
        assertThat(numFound(query("q=%28http%5C%3A%2F%2Fvv.uka.ru%29")), is(0L));
    }

    @Test
    public void search_inetnum() {
        databaseHelper.addObject(
               "inetnum:        193.0.0.0 - 193.0.0.255\n" +
               "netname:        RIPE-NCC\n" +
               "source:         RIPE");
         rebuildIndex();

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
    public void search_inetnum_with_prefix_length() {
        databaseHelper.addObject(
                "inetnum:        10.0.0.0/24\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
         rebuildIndex();

        assertThat(numFound(query("q=10.0.0.0/24")), is(1L));
    }

    @Test
    public void search_inetnum_multiple_matches() {
        databaseHelper.addObject(
                "inetnum:        193.0.0.0 - 193.0.0.255\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
        databaseHelper.addObject(
                "inetnum:        193.1.0.0 - 193.1.0.255\n" +
                "netname:        RIPE-NCC\n" +
                "source:         RIPE");
         rebuildIndex();

        assertThat(numFound(query("q=193.0.0.0")), is(1L));
        assertThat(numFound(query("q=193.1.0.0")), is(1L));
        assertThat(numFound(query("q=193")), is(2L));
    }

    @Test
    public void search_inet6num() {
        databaseHelper.addObject(
                "inet6num: 2001:0638:0501::/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
         rebuildIndex();

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
    public void search_inet6num_double_colons() {
        databaseHelper.addObject(
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
         rebuildIndex();

        assertThat(numFound(query("q=2a00")), is(1L));
        assertThat(numFound(query("q=2a00%5C%3A1f78")), is(1L));       // need to escape single colon (used as separator by lucene)
        assertThat(numFound(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48")), is(1L));
    }

    @Test
    public void search_inet6num_multiple_matches() {
        databaseHelper.addObject(
                "inet6num: 2a00:1f78:7a2b:2001::/64\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
        databaseHelper.addObject(
                "inet6num: 2a00:1f11:7777:2a98::/64\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");
         rebuildIndex();

        assertThat(numFound(query("q=2a00")), is(2L));
    }

    @Test
    public void search_filter_comma_when_indexing() {
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
         rebuildIndex();

        assertThat(numFound(query("q=Company")), is(1L));
    }

    @Test
    public void search_filter_comma_on_query_term() {
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
         rebuildIndex();

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
         rebuildIndex();

        assertThat(numFound(query("q=company")), is(1L));
    }

    @Test
    public void search_multiple_matches_but_only_one_result() {
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
         rebuildIndex();

        assertThat(numFound(query("q=ORG-TOS1-TEST")), is(1L));
    }

    @Test
    public void search_match_partial_email_address() {
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
         rebuildIndex();

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
         rebuildIndex();

        assertThat(numFound(query("q=org1@test.com")), is(1L));
    }

    @Test
    public void search_full_match_person_name() {
        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=john%20mcdonald");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void temporary_block() {
        testPersonalObjectAccounting.accountPersonalObject(Inet4Address.getLoopbackAddress(), 5001);

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
         rebuildIndex();

        try {
            query("q=john%20mcdonald");
            fail("request should have been blocked");
        } catch (ClientErrorException cee) {
            assertThat(cee.getResponse().getStatus(), is(429));
        }
    }

    @Test
    public void permanent_block() {
        final IpInterval localhost = IpInterval.parse(Inet4Address.getLoopbackAddress().getHostAddress());
        jdbcAccessControlListDao.savePermanentBlock(localhost, LocalDate.now(), 1, "test");
        ipResourceConfiguration.reload();

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
         rebuildIndex();

        try {
            query("q=john%20mcdonald");
            fail("request should have been blocked");
        } catch (ClientErrorException cee) {
            assertThat(cee.getResponse().getStatus(), is(429));
        } finally {
            assertThat(aclJdbcTemplate.update("DELETE FROM acl_denied WHERE prefix = ?", localhost.toString()), is(1));
            ipResourceConfiguration.reload();
        }
    }

    @Test
    public void search_inet6num_escape_forward_slash() {
        databaseHelper.addObject(RpslObject.parse("inet6num: 2001:0638:0501::/48"));
         rebuildIndex();

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
    public void search_highlight_escaping_xml() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: DEV mntner\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=DEV&hl=true&hl.simple.pre=%3Cb%3E&hl.simple.post=%3C/b%3E");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        assertThat(queryResponse.getHighlighting().keySet(), contains("1"));
        assertThat(queryResponse.getHighlighting().get("1").keySet(), hasSize(3));
        assertThat(queryResponse.getHighlighting().get("1").get("lookup-key"), contains("<b>DEV-MNT<\\/b>"));
        assertThat(queryResponse.getHighlighting().get("1").get("mntner"), contains("<b>DEV-MNT<\\/b>"));
        assertThat(queryResponse.getHighlighting().get("1").get("remarks"), contains("<b>DEV<\\/b> mntner"));
    }

    @Test
    public void search_highlight_escaping_json() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: DEV mntner\n" +
                "source: RIPE"));
         rebuildIndex();

        final SearchResponse searchResponse = queryJson("q=DEV&hl=true&hl.simple.pre=%3Cb%3E&hl.simple.post=%3C/b%3E&wt=json");

        // document
        assertThat(searchResponse.getResult().getDocs(), hasSize(1));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs(), hasSize(5));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(0).getName(), is("primary-key"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(0).getValue(), is("1"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(1).getName(), is("object-type"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(1).getValue(), is("mntner"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(2).getName(), is("lookup-key"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(2).getValue(), is("DEV-MNT"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(3).getName(), is("mntner"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(3).getValue(), is("DEV-MNT"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(4).getName(), is("remarks"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(4).getValue(), is("DEV mntner"));

        // highlighting
        assertThat(searchResponse.getLsts(), hasSize(3));
        assertThat(searchResponse.getLsts().get(0).getName(), is("responseHeader"));
        assertThat(searchResponse.getLsts().get(1).getName(), is("highlighting"));
        assertThat(searchResponse.getLsts().get(1).getLsts(), hasSize(1));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getName(), is("1"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs(), hasSize(3));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(0).getName(), is("lookup-key"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(0).getStr().getValue(), is("<b>DEV-MNT</b>"));
        assertThat(searchResponse.getLsts().get(2).getName(), is("version"));
    }

    @Test
    public void search_quote_escaping_xml() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: \"DEV mntner\"\n" +
                "source: RIPE"));
         rebuildIndex();

        final QueryResponse queryResponse = query("q=DEV&hl=true&hl.simple.pre=%3Cb%3E&hl.simple.post=%3C/b%3E");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
        assertThat(queryResponse.getHighlighting().keySet(), contains("1"));
        assertThat(queryResponse.getHighlighting().get("1").keySet(), hasSize(3));
        assertThat(queryResponse.getHighlighting().get("1").get("lookup-key"), contains("<b>DEV-MNT<\\/b>"));
        assertThat(queryResponse.getHighlighting().get("1").get("mntner"), contains("<b>DEV-MNT<\\/b>"));
        assertThat(queryResponse.getHighlighting().get("1").get("remarks"), contains("\"<b>DEV<\\/b> mntner\""));
    }

    @Test
    public void search_quote_escaping_json() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: \"DEV mntner\"\n" +
                "source: RIPE"));
         rebuildIndex();

        final SearchResponse searchResponse = queryJson("q=DEV&hl=true&hl.simple.pre=%3Cb%3E&hl.simple.post=%3C/b%3E&wt=json");

        // document
        assertThat(searchResponse.getResult().getDocs(), hasSize(1));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs(), hasSize(5));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(4).getName(), is("remarks"));
        assertThat(searchResponse.getResult().getDocs().get(0).getStrs().get(4).getValue(), is("\"DEV mntner\""));

        // highlighting
      /*  assertThat(searchResponse.getLsts(), hasSize(3));
        assertThat(searchResponse.getLsts().get(0).getName(), is("responseHeader"));
        assertThat(searchResponse.getLsts().get(1).getName(), is("highlighting"));
        assertThat(searchResponse.getLsts().get(1).getLsts(), hasSize(1));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getName(), is("1"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs(), hasSize(3));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(2).getName(), is("remarks"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(2).getStr().getValue(), is("<b>\"DEV</b> mntner\""));
        assertThat(searchResponse.getLsts().get(2).getName(), is("version"));*/
    }

    @Test
    public void basic_search_nonauth_aut_num_object() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=AS101");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
    }

    @Test
    public void basic_search_nonauth_route_object() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "route: 211.43.192.0/19\n" +
                        "origin: AS101\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=211.43.192.0/19");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
    }

    @Test
    public void basic_search_found_all_objects_with_nonauth_value_in_attribute() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "route: 211.43.192.0/19\n" +
                        "origin: AS101\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=RIPE-NONAUTH");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(queryResponse.getResults(), hasSize(3));
    }

    @Test
    public void basic_search_ignore_source_attribute() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "route: 211.43.192.0/19\n" +
                        "origin: AS101\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=RIPE-NONAUTH");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
        assertThat(queryResponse.getResults(), hasSize(0));
    }

    @Test
    public void advanced_search_nonauth_aut_num_objects() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(descr:(RIPE%5C-NONAUTH))+AND+(object-type:aut-num)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(2L));
        assertThat(queryResponse.getResults(), hasSize(2));
    }

    @Test
    public void advanced_search_aut_num_route_with_same_descr() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "route: 211.43.192.0/19\n" +
                        "origin: AS101\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(descr:(RIPE%5C-NONAUTH))+AND+(object-type:aut-num+OR+object-type:route)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(queryResponse.getResults(), hasSize(3));
    }

    @Test
    public void advanced_search_nonauth_ignore_source() {
        databaseHelper.addObject(
                "aut-num: AS101\n" +
                        "as-name: End-User-1\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
        databaseHelper.addObject(
                "aut-num: AS102\n" +
                        "as-name: End-User-2\n" +
                        "descr: RIPE-NONAUTH\n" +
                        "source: RIPE-NONAUTH\n");
         rebuildIndex();

        final QueryResponse queryResponse = query("q=(source:(RIPE%5C-NONAUTH))+AND+(object-type:aut-num)");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
        assertThat(queryResponse.getResults(), hasSize(0));
    }

    @Test
    public void dont_include_null_elements() {
        databaseHelper.addObject(
            RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "remarks: DEV mntner\n" +
                "source: RIPE"));
         rebuildIndex();

        final String response = RestTest.target(getPort(), "whois/fulltextsearch/select.json?facet=true&format=xml&hl=true&q=(mntner)&start=0&wt=json")
                .request()
                .get(String.class);

        assertThat(response, not(containsString("null")));
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
