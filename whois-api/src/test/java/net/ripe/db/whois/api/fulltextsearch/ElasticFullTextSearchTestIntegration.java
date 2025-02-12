package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.elasticsearch.AbstractElasticSearchIntegrationTest;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.dao.jdbc.JdbcIpAccessControlListDao;
import net.ripe.db.whois.query.support.TestPersonalObjectAccounting;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.net.Inet4Address;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("ElasticSearchTest")
public class ElasticFullTextSearchTestIntegration extends AbstractElasticSearchIntegrationTest {

    private static final String WHOIS_INDEX = "whois_fulltext";
    private static final String METADATA_INDEX = "metadata_fulltext";
    public static final String VALID_TOKEN_USER_NAME = "person@net.net";
    public static final String VALID_TOKEN = "valid-token";


    @Autowired TestPersonalObjectAccounting testPersonalObjectAccounting;
    @Autowired
    JdbcIpAccessControlListDao jdbcIpAccessControlListDao;
    @Autowired IpResourceConfiguration ipResourceConfiguration;

    @Value("${api.rest.baseurl}")
    private String restApiBaseUrl;
    private JdbcTemplate aclJdbcTemplate;

    @BeforeAll
    public static void setUpProperties() {
        System.setProperty("elastic.whois.index", WHOIS_INDEX);
        System.setProperty("elastic.commit.index", METADATA_INDEX);
        System.setProperty("fulltext.search.max.results", "10");
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
        assertThat(solrDocument.getFirstValue("object-type"), is("mntner"));
        assertThat(solrDocument.getFirstValue("lookup-key"), is("DEV-MNT"));
        assertThat(solrDocument.getFirstValue("mntner"), is("DEV-MNT"));
    }

    @Test
    public void search_single_result_even_deleted_from_database() {
        final RpslObject mntner = RpslObject.parse(
                "mntner: DEV-MNT\n" +
                "source: RIPE");
        databaseHelper.addObject(mntner);
        rebuildIndex();
        databaseHelper.deleteObject(mntner);

        final QueryResponse queryResponse = query("q=DEV-MNT");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getResults(), hasSize(1));
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
    public void search_different_object_types_with_facets() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                "nic-hdl: AA1-RIPE\n" +
                "remarks: Other remark\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "irt: irt-IRT1\n" +
                "mnt-ref:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "role: role test\n" +
                "nic-hdl: AA2-RIPE\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "inetnum:  109.107.192.0 - 109.107.223.255\n" +
                "netname:  CZ-OSKARMOBIL-20091021\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "inet6num:  2a01:820::/32\n" +
                "netname:  VODAFONE-ITALY\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "domain: 112.109.in-addr.arpa\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "aut-num:         AS34419\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "as-set:          AS-VODAFONE\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "route:           206.29.144.0/20\n" +
                "origin:          AS34419\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));

        databaseHelper.addObject(RpslObject.parse(
                "route6:          2a00::/22\n" +
                "origin:          AS34419\n" +
                "mnt-by:   DEV1-MNT\n" +
                "remarks: Other remark\n" +
                "source:    RIPE"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=remark&facet=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(11L));
        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets, hasSize(1));
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(11));
        assertThat(facet.getValues().toString(), containsString("as-set (1)"));
        assertThat(facet.getValues().toString(), containsString("aut-num (1)"));
        assertThat(facet.getValues().toString(), containsString("domain (1)"));
        assertThat(facet.getValues().toString(), containsString("inet6num (1)"));
        assertThat(facet.getValues().toString(), containsString("inetnum (1)"));
        assertThat(facet.getValues().toString(), containsString("irt (1)"));
        assertThat(facet.getValues().toString(), containsString("mntner (1)"));
        assertThat(facet.getValues().toString(), containsString("person (1)"));
        assertThat(facet.getValues().toString(), containsString("role (1)"));
        assertThat(facet.getValues().toString(), containsString("route (1)"));
        assertThat(facet.getValues().toString(), containsString("route6 (1)"));
    }

    @Test
    public void search_multiple_results_with_first_three_records() {

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

        final QueryResponse queryResponse = query("q=remark&facet=true&rows=3&start=0");

        //rows to return 3, however the total that ES is able to find is 5
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults(), hasSize(3));
        assertThat(queryResponse.getResults().getNumFound(), is(5L));


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
    public void search_list_object_type_single_value_and_comment_with_facet() {
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
    public void search_list_value_attribute_single_value_and_comment_with_facet() {
        databaseHelper.addObject("mntner: AA1-MNT\nsource: RIPE");
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-AA1-RIPE\n" +
                        "mnt-ref: AA1-MNT # include this comment\n" +
                        "source: RIPE"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=(mnt-ref:(AA1))+AND+(object-type:organisation)&facet=true");

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
    public void search_dot_results_with_highlighting() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV1-MNT\n" +
                        "remarks: Some.remark1\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV2-MNT\n" +
                        "remarks: Some.remark2\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV3-MNT\n" +
                        "remarks: Some.remark3\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV4-MNT\n" +
                        "source: RIPE"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=Some.remark&hl=true");

        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(getHighlightKeys(queryResponse), containsInAnyOrder("1", "2", "3"));
        assertThat(getHighlightValues(queryResponse), containsInAnyOrder("<b>Some.remark<\\/b>1", "<b>Some.remark<\\/b>2", "<b>Some.remark<\\/b>3"));
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

        final QueryResponse queryResponse = query("q=(nic-hdl:(AA1-RIPE))+AND+" +
                "(object-type:person)");

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
    public void search_inet6num_no_escape_colons() {
        databaseHelper.addObject(
                "inet6num: 2001:0638:0501::/48\n" +
                        "netname: RIPE-NCC\n" +
                        "source: RIPE\n");
        rebuildIndex();
        assertThat(numFound(query("q=(2001:0638:0501::/48+OR+2001:0638:0502::/48)")), is(1L));
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
        jdbcIpAccessControlListDao.savePermanentBlock(localhost, LocalDate.now(), 1, "test");
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
    public void permanent_block_sso() {
        final IpInterval localhost = IpInterval.parse(Inet4Address.getLoopbackAddress().getHostAddress());
        jdbcIpAccessControlListDao.savePermanentBlock(localhost, LocalDate.now(), 1, "test");
        ipResourceConfiguration.reload();

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
        rebuildIndex();

        try {
            parseResponse(
                    RestTest.target(getPort(), "whois/fulltextsearch/select?q=john%20mcdonald")
                            .request()
                            .cookie(AuthServiceClient.TOKEN_KEY, VALID_TOKEN)
                            .get(String.class));
            fail("request should have been blocked");
        } catch (ClientErrorException cee) {
            assertThat(cee.getResponse().getStatus(), is(429));
        } finally {
            assertThat(aclJdbcTemplate.update("DELETE FROM acl_denied WHERE prefix = ?", localhost.toString()), is(1));
            ipResourceConfiguration.reload();
        }
    }

    @Test
    public void too_many_personal_object_temporary_block() {
        testPersonalObjectAccounting.accountPersonalObject(Inet4Address.getLoopbackAddress(), 5000);

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
    public void too_many_personal_object_temporary_block_sso() {
        testPersonalObjectAccounting.accountPersonalObject(VALID_TOKEN_USER_NAME, 5000);

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
        rebuildIndex();

        try {
            parseResponse(
                    RestTest.target(getPort(), "whois/fulltextsearch/select?q=john%20mcdonald")
                            .request()
                            .cookie(AuthServiceClient.TOKEN_KEY, VALID_TOKEN)
                            .get(String.class));
            fail("request should have been blocked");
        } catch (ClientErrorException cee) {
            assertThat(cee.getResponse().getStatus(), is(429));
        }
    }

    @Test
    public void should_account_for_personal_objects() {
        testPersonalObjectAccounting.accountPersonalObject(Inet4Address.getLoopbackAddress(), 1);

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
        rebuildIndex();

        query("q=john%20mcdonald");

        int totalCount = testPersonalObjectAccounting.getQueriedPersonalObjects(Inet4Address.getLoopbackAddress());
        assertThat(totalCount, is(2));
    }

    @Test
    public void should_account_for_personal_objects_using_sso() {
        testPersonalObjectAccounting.accountPersonalObject(Inet4Address.getLoopbackAddress(), 1);
        testPersonalObjectAccounting.accountPersonalObject(VALID_TOKEN_USER_NAME, 1);

        databaseHelper.addObject(RpslObject.parse(
                "person: John McDonald\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));
        rebuildIndex();

        RestTest.target(getPort(), "whois/fulltextsearch/select?q=john%20mcdonald")
                .request()
                .cookie(AuthServiceClient.TOKEN_KEY, VALID_TOKEN)
                .get(String.class);

        int totalCountIp = testPersonalObjectAccounting.getQueriedPersonalObjects(Inet4Address.getLoopbackAddress());
        int totalCountSSO = testPersonalObjectAccounting.getQueriedPersonalObjects(VALID_TOKEN_USER_NAME);
        assertThat(totalCountIp, is(1)) ;
        assertThat(totalCountSSO, is(2));
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
        assertThat(queryResponse.getHighlighting().get("1").keySet(), hasSize(2));
        assertThat(queryResponse.getHighlighting().get("1").get("mntner"), contains("<b>DEV<\\/b>-MNT"));
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
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs(), hasSize(2));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(0).getName(), is("mntner"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(0).getStr().getValue(), is("<b>DEV</b>-MNT"));

        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(1).getName(), is("remarks"));
        assertThat(searchResponse.getLsts().get(1).getLsts().get(0).getArrs().get(1).getStr().getValue(), is("<b>DEV</b> mntner"));
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
        assertThat(queryResponse.getHighlighting().get("1").keySet(), hasSize(2));
        assertThat(queryResponse.getHighlighting().get("1").get("mntner"), contains("<b>DEV<\\/b>-MNT"));
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

    @Test
    public void search_email_full_email() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     test@domain1.domain2.nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=test@domain1.domain2.nl&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_full_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=domain1.domain2.nl&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_before_at() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=testemail&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_first_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=domain1&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_second_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=domain2&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }
    @Test
    public void search_email_tld() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=nl&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_second_domain_tld() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=domain2.nl&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_second_domain_tld_with_email_ALL() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       test@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(e-mail:(domain2.nl))+AND+" +
                "(object-type:organisation)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }
    @Test
    public void search_email_first_second_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=domain1.domain2&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_email_first_second_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=testemail@domain1.domain2&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_before_at_first_domain() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        test org\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain1.domain2.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("q=testemail@domain1&hl=true");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
    }

    @Test
    public void search_email_before_at_first_domain_with_email_ALL() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     test org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail@domain.second.domain.nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.second.domain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     test org\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail@domain.thirddomain.nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.thirddomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();

        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(e-mail:(second.domain.nl))+AND+" +
                "(object-type:organisation)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("descr"), is(false));

        assertThat(queryResponse.getHighlighting().get("2").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").get(0), is("<b>organisation</b>"));
    }

    @Test
    public void search_organisation_with_filters() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(org-name:(secondDomain+AND+nl))+AND+" +
                "(object-type:organisation)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("org-name"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("descr"), is(false));

        assertThat(queryResponse.getHighlighting().get("2").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").get(0), is("<b>organisation</b>"));
    }

    @Test
    public void search_organisation_with_OR_attribute_filters() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(org-name:(secondDomain+OR+nl))" +
                "+AND+" +
                "(object-type:organisation)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(2L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("org-name"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));
    }

    @Test
    public void search_organisation_with_exact_match_filter() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));
        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(org-name:(secondDomain%20nl))" +
                "+AND+" +
                "(object-type:organisation)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));
    }

    @Test
    public void search_organisation_with_ALL_multiple_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "remarks:      testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail domain secondDomain nl\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain+AND+nl))" +
                "+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(2L));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("4").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").get(0), is("<b>person</b>"));
    }

    @Test
    public void search_organisation_with_ANY_multiple_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "remarks:      testemail domain secondDomain\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail domain nl\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain+OR+nl))" +
                "+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(2L));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("4").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").get(0), is("<b>person</b>"));
    }

    @Test
    public void search_organisation_with_exact_match_multiple_object_type() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "remarks:      testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail secondDomain\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain%20nl))" +
                "+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));
    }

    @Test
    public void search_organisation_with_ALL_multiple_object_type_multiple_attributes() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail secondDomain nl\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain+AND+nl)" +
                "+OR+descr:(secondDomain+AND+nl))+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");
        assertThat(queryResponse.getResults().getNumFound(), is(2L));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("4").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").get(0), is("<b>person</b>"));
    }


    @Test
    public void search_organisation_with_ANY_multiple_object_type_multiple_attributes() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail secondDomain\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain+OR+nl)" +
                "+OR+descr:(secondDomain+OR+nl))+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");

        assertThat(queryResponse.getResults().getNumFound(), is(3L));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("remarks"), is(false));
        assertThat(queryResponse.getHighlighting().get("2").containsKey("descr"), is(true));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("2").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("4").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").get(0), is("<b>person</b>"));
    }

    @Test
    public void search_organisation_with_exact_match_multiple_object_type_multiple_attributes() {
        databaseHelper.addObject(RpslObject.parse(
                "mntner: OWNER-MNT\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS1-TEST\n" +
                        "org-name:     testemail domain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail domain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "organisation: ORG-TOS2-TEST\n" +
                        "org-name:     testemail thirdDomain secondDomain nl\n" +
                        "org-type:     OTHER\n" +
                        "descr:        testemail secondDomain nl\n" +
                        "address:      street 1\n" +
                        "e-mail:       testemail@domain.secondDomain.nl\n" +
                        "mnt-ref:      OWNER-MNT\n" +
                        "mnt-by:       OWNER-MNT\n" +
                        "source:       RIPE\n"));

        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "remarks: testemail secondDomain nl\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "source: RIPE"));

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(remarks:(secondDomain%20nl)" +
                "+OR+descr:(secondDomain%20nl))+AND+" +
                "(object-type:organisation+OR+object-type:person)&start=0&wt=json");

        assertThat(queryResponse.getResults().getNumFound(), is(2L));

        assertThat(queryResponse.getHighlighting().get("3").containsKey("org-name"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("e-mail"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("remarks"), is(false));
        assertThat(queryResponse.getHighlighting().get("3").containsKey("descr"), is(true));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("3").get("object-type").get(0), is("<b>organisation</b>"));

        assertThat(queryResponse.getHighlighting().get("4").containsKey("object-type"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("remarks"), is(true));
        assertThat(queryResponse.getHighlighting().get("4").containsKey("descr"), is(false));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").size(), is(1));
        assertThat(queryResponse.getHighlighting().get("4").get("object-type").get(0), is("<b>person</b>"));
    }

    @Test
    public void query_returns_maximum_results_and_mixed_objects_sorted_by_score_lookup() {
        databaseHelper.addObject("mntner: TEST-SE-MNT");

        databaseHelper.addObject("""
                person:          Niels Christian Bank-Pedersen
                address:         RC
                mnt-by:          TEST-SE-MNT
                e-mail:          bank.es
                nic-hdl:         TP1-TEST
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         81.128.169.144 - 81.128.169.159
                netname:         TEST-BANK
                descr:           RC Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2a00:2381:b2f::/48
                netname:         TEST-BANK
                descr:           RC BANK
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2a00:2381:b2f::/56
                netname:         TEST-BANK
                descr:           RC Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         31.15.49.116 - 31.15.49.119
                netname:         BANK-NET
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2001:6f0:2501::/48
                netname:         BANK-NET
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         87.54.47.216 - 87.54.47.223
                netname:         BANK-NET
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         95.58.17.72 - 95.58.17.75
            netname:         Bank
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         83.92.220.64 - 83.92.220.71
            netname:         BANK-NET
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         193.89.255.72 - 193.89.255.79
            netname:         BANK-NET
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         195.249.50.128 - 195.249.50.191
            netname:         BANK
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         31.15.33.192 - 31.15.33.195
                netname:         BANK-NET
                descr:           Bank AB
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         37.233.74.12 - 37.233.74.12
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         88.131.111.160 - 88.131.111.191
                netname:         BANK
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         212.214.152.144 - 212.214.152.151
                netname:         AVANZA-BANK-NET
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         217.119.169.120 - 217.119.169.123
                netname:         NORDEA-BANK-NET
                country:         SE
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                person:          Bjorn Kogge
                address:         Forex Bank AB
                nic-hdl:         TP2-TEST
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(RC%20AND%20BANK)" +
                "&start=0&wt=json");

        assertThat(queryResponse.getResults().size(), is(4));

        assertThat(queryResponse.getResults().get(0).get("lookup-key"), is("TP1-TEST"));
        assertThat(queryResponse.getResults().get(1).get("lookup-key"), is("2a00:2381:b2f::/48"));
        assertThat(queryResponse.getResults().get(2).get("lookup-key"), is("2a00:2381:b2f::/56"));
        assertThat(queryResponse.getResults().get(3).get("lookup-key"), is("81.128.169.144 - 81.128.169.159"));
    }

    @Test
    public void query_returns_maximum_results_and_mixed_objects_sorted_by_score_lookup_with_phrase() {
        databaseHelper.addObject("mntner: TEST-SE-MNT");

        databaseHelper.addObject("""
                person:          Niels Christian Bank-Pedersen
                address:         RC
                mnt-by:          TEST-SE-MNT
                e-mail:          bank.es
                nic-hdl:         TP1-TEST
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         81.128.169.144 - 81.128.169.159
                netname:         TEST-BANK
                descr:           RC Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2a00:2381:b2f::/48
                netname:         TEST-BANK
                descr:           RC BANK
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2a00:2381:b2f::/56
                netname:         TEST-BANK
                descr:           RC Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                mnt-lower:       TEST-SE-MNT
                mnt-routes:      TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         31.15.49.116 - 31.15.49.119
                netname:         BANK-NET
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inet6num:        2001:6f0:2501::/48
                netname:         BANK-NET
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         87.54.47.216 - 87.54.47.223
                netname:         BANK-NET
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         95.58.17.72 - 95.58.17.75
            netname:         Bank
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         83.92.220.64 - 83.92.220.71
            netname:         BANK-NET
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         193.89.255.72 - 193.89.255.79
            netname:         BANK-NET
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
            inetnum:         195.249.50.128 - 195.249.50.191
            netname:         BANK
            admin-c:         TP1-TEST
            tech-c:          TP1-TEST
            status:          ALLOCATED UNSPECIFIED
            mnt-by:          TEST-SE-MNT
            source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         31.15.33.192 - 31.15.33.195
                netname:         BANK-NET
                descr:           Bank AB
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         37.233.74.12 - 37.233.74.12
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         88.131.111.160 - 88.131.111.191
                netname:         BANK
                descr:           Bank
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         212.214.152.144 - 212.214.152.151
                netname:         AVANZA-BANK-NET
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                inetnum:         217.119.169.120 - 217.119.169.123
                netname:         NORDEA-BANK-NET
                country:         SE
                admin-c:         TP1-TEST
                tech-c:          TP1-TEST
                status:          ALLOCATED UNSPECIFIED
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        databaseHelper.addObject("""
                person:          Bjorn Kogge
                address:         Forex Bank AB
                nic-hdl:         TP2-TEST
                mnt-by:          TEST-SE-MNT
                source:          TEST
                """);

        rebuildIndex();
        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(RC%20%20BANK)" +
                "&start=0&wt=json");

        assertThat(queryResponse.getResults().size(), is(3));

        assertThat(queryResponse.getResults().get(0).get("lookup-key"), is("2a00:2381:b2f::/48"));
        assertThat(queryResponse.getResults().get(1).get("lookup-key"), is("2a00:2381:b2f::/56"));
        assertThat(queryResponse.getResults().get(2).get("lookup-key"), is("81.128.169.144 - 81.128.169.159"));
    }

    @Test
    public void search_multiple_results_paginating() {

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

        final QueryResponse queryResponse = query("q=remark&facet=true&rows=3&start=1");

        //rows to return 3, however the total that ES is able to find is 5
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults(), hasSize(3));
        assertThat(queryResponse.getResults().getNumFound(), is(5L));
    }

    @Test
    public void search_more_matches_than_max_row_just_one_requested() {

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
                "mntner: DEV4-MNT\n" +
                        "remarks: Some remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV5-MNT\n" +
                        "remarks: Another remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "mntner: DEV6-MNT\n" +
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
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "nic-hdl: AA3-RIPE\n" +
                        "remarks: Other remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Middle Last\n" +
                        "nic-hdl: AA4-RIPE\n" +
                        "remarks: Other remark\n" +
                        "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Middle Last\n" +
                        "nic-hdl: AA5-RIPE\n" +
                        "remarks: Other remark\n" +
                        "source: RIPE"));

        rebuildIndex();

        final QueryResponse queryResponse = query("q=remark&facet=true&rows=1&start=10");

        //rows to return 3, however the total that ES is able to find is 5
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults(), hasSize(1));
        assertThat(queryResponse.getResults().getNumFound(), is(11L));
        assertThat(queryResponse.getResults().get(0).get("lookup-key"), is("DEV6-MNT"));
    }
    @Test
    public void search_multiple_results_paginating_last_records() {

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

        final QueryResponse queryResponse = query("q=remark&facet=true&rows=3&start=3");

        //rows to return 3, however the total that ES is able to find is 5
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults(), hasSize(2));
        assertThat(queryResponse.getResults().getNumFound(), is(5L));
    }

    @Test
    public void search_starting_lower_value_than_rows() {

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

        final QueryResponse queryResponse = query("q=remark&facet=true&rows=1&start=3");

        //rows to return 3, however the total that ES is able to find is 5
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults(), hasSize(1));
        assertThat(queryResponse.getResults().getNumFound(), is(5L));
    }

    @Test
    public void fulltext_search() {
        Response response = RestTest.target(getPort(), "whois/fulltextsearch/select?facet=true&format=xml&hl=true&q=remark&start=0&wt=json")
                .request()
                .header(HttpHeaders.HOST, getHost(restApiBaseUrl))
                .get();

        assertThat(response.getStatus(), is(HttpStatus.OK_200));
    }


    private String getHost(final String url) {
        final URI uri = URI.create(url);
        return uri.getHost();
    }
    @Test
    public void request_bad_syntax_query_bad_request() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            query("facet=true&format=xml&hl=true&q=(TEST%20AND%20BANK%20NOT)&start=0&wt=json&rows=10");
        });
        assertThat(badRequestException.getMessage(), is("HTTP 400 Bad Request"));
        assertThat(badRequestException.getResponse().readEntity(String.class), is("Invalid query syntax"));
    }
    @Test
    public void request_more_than_allowed_rows_bad_request() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            query("facet=true&format=xml&hl=true&q=(TEST%20AND%20BANK)&start=0&wt=json&rows=11");
        });
        assertThat(badRequestException.getMessage(), is("HTTP 400 Bad Request"));
        assertThat(badRequestException.getResponse().readEntity(String.class), is("Too many results requested, the maximum allowed is 10"));
    }

    @Test
    public void request_from_higher_position_than_allowed() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            query("facet=true&format=xml&hl=true&q=(TEST%20AND%20BANK)&start=99991&wt=json&rows=10");
        });
        assertThat(badRequestException.getMessage(), is("HTTP 400 Bad Request"));
        assertThat(badRequestException.getResponse().readEntity(String.class), is("Exceeded maximum 100000 documents"));
    }

    @Test
    public void search() {
        try {
            searchQuery("q=test");
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    @Test
    public void request_for_ipv6_prefix(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=2a00:1f78::fffe/48")), is(1L));
    }

    @Test
    public void request_for_ipv6_mixing_filtering(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "remarks: 2a00:1f78::ffff\n" +
                        "source: RIPE"));
        rebuildIndex();

        assertThat(numFound(query("q=(inet6num:(2a00:1f78::fffe/48)" +
                "+OR+remarks:(2a00:1f78::ffff))+AND+" +
                "(object-type:inet6num+OR+object-type:person)")), is(2L));
    }

    @Test
    public void request_for_ipv6_prefix_filtering(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "remarks: 2a00:1f78::ffff/48\n" +
                        "source: RIPE"));
        rebuildIndex();

        assertThat(numFound(query("q=(inet6num:(2a00:1f78::fffe/48)" +
                "+OR+remarks:(2a00:1f78::ffff/48))+AND+" +
                "(object-type:inet6num+OR+object-type:person)")), is(2L));
    }

    @Test
    public void request_for_ipv6_without_prefix(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=2a00:1f78::fffe")), is(1L));
    }

    @Test
    public void request_for_full_ipv6(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2001:0000:130F:0000:0000:09C0:876A:130B\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=2001:0000:130F:0000:0000:09C0:876A:130B")), is(1L));
    }

    @Test
    public void request_for_ipv6_range_without_prefix(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2001:0000:130F:0000:0000:09C0:876A:130B\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=2001:0000:130F:0000:0000:09C0:876A:130B+-+" +
                "2001:0000:130F:0000:0000:09C0:876A:130f")), is(1L));
    }

    @Test
    public void request_for_ipv6_range_with_prefix(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=2a00:1f78::fffe/48+-+2a00:1f78::ffff/48")), is(1L));
    }

    @Test
    public void request_for_root_ipv6(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: ::/0\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=::/0")), is(1L));
    }

    @Test
    public void request_for_big_ipv6(){
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: ::123:2\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        rebuildIndex();

        assertThat(numFound(query("q=::123:2")), is(1L));
    }

    @Test
    public void request_for_email_english_stopword(){
        databaseHelper.addObject(RpslObject.parse(
                "person: First Last\n" +
                        "nic-hdl: AA1-RIPE\n" +
                        "remarks: Other remark\n" +
                        "e-mail:  it@test.it\n" +
                        "source: RIPE"));
        rebuildIndex();

        assertThat(numFound(query("q=test.it")), is(1L));
    }

    @Test
    public void fulltext_search_expected_hl() {
        databaseHelper.addObject("""
                person: First Last
                nic-hdl: AA1-RIPE
                remarks: Other remark
                e-mail:  it@test.it
                source: RIPE
                """);

        databaseHelper.addObject("""
                mntner:         MHM-MNT
                admin-c:        AA1-RIPE
                mnt-by:         MHM-MNT
                created:        2024-07-01T11:02:24Z
                last-modified:  2025-02-03T08:54:01Z
                source:         TEST
                """);

        rebuildIndex();

        final QueryResponse queryResponse = query("facet=true&format=xml&hl=true&q=(%22MHM%5C-MNT%22)&start=0&wt=json");
        assertThat(getHighlightRecordsKeys(queryResponse), containsInAnyOrder("mnt-by", "mntner"));
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
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
    }

    private List<String> getHighlightRecordsKeys(final QueryResponse queryResponse) {
        return queryResponse.getHighlighting()
                .values().stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String searchQuery(final String queryString) {
        return RestTest.target(getPort(), "search?" + queryString)
                .request()
                .get(String.class);
    }
    private long numFound(final QueryResponse queryResponse) {
        return queryResponse.getResults().getNumFound();
    }

    private static QueryResponse parseResponse(final String fullTextResponse) {
        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(fullTextResponse));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        return queryResponse;
    }
}

