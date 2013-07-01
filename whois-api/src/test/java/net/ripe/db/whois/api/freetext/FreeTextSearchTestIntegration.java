package net.ripe.db.whois.api.freetext;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class FreeTextSearchTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Autowired FreeTextIndex freeTextIndex;

    @Before
    public void setUp() throws Exception {
        freeTextIndex.rebuild();
    }

    @Test
    public void search_no_params() throws Exception {
        try {
            query("");
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(ClientResponse.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("No query parameter."));
        }
    }

    @Test
    public void search_empty_query_param() throws Exception {
        try {
            query("q=");
            fail();
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(ClientResponse.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), containsString("Invalid query"));
        }
    }

    @Test
    public void search_single_result() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "source: RIPE"));

        freeTextIndex.update();

        final String searchResult = query("q=DEV-MNT");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));

        final SolrDocument solrDocument = queryResponse.getResults().get(0);
        solrDocument.addField("primary-key", "1");
        solrDocument.addField("object-type", "mntner");
        solrDocument.addField("lookup-key", "DEV-MNT");
        solrDocument.addField("mntner", "DEV-MNT");
        assertThat(solrDocument.getFirstValue("primary-key").toString(), is("1"));
        assertThat(solrDocument.getFirstValue("object-type").toString(), is("mntner"));
        assertThat(solrDocument.getFirstValue("lookup-key").toString(), is("DEV-MNT"));
        assertThat(solrDocument.getFirstValue("mntner").toString(), is("DEV-MNT"));
    }

    @Test
    public void search_multiple_results_with_highlighting() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV1-MNT\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV2-MNT\n" +
                "remarks: Second remark\n" +
                "source: RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV3-MNT\n" +
                "remarks: Other remark\n" +
                "source: RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV4-MNT\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=remark&hl=true");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(3L));

        final Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        assertThat(highlighting.keySet(), hasSize(3));
        final Map<String, List<String>> map = highlighting.get("1");
        assertThat(map.keySet(), contains("remarks"));
    }

    @Test
    public void search_multiple_results_with_facet() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV1-MNT\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "person: First Last\n" +
                "nic-hdl: AA1-RIPE\n" +
                "remarks: Some remark\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=remark&facet=true");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(2L));

        final List<FacetField> facets = queryResponse.getFacetFields();
        assertThat(facets.size(), is(1));
        final FacetField facet = facets.get(0);
        assertThat(facet.getName(), is("object-type"));
        assertThat(facet.getValueCount(), is(2));
        assertThat(facet.getValues().toString(), containsString("mntner (1)"));
        assertThat(facet.getValues().toString(), containsString("person (1)"));
    }

    @Test
    public void no_exact_match_for_highlighting() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  AARD-MNT\n" +
                "source: RIPE"));

        databaseHelper.addObject(RpslObject.parse("" +
                "domain:          198.76.217.in-addr.arpa\n" +
                "descr:           T.E.S.T. Ltd\n" +
                "nserver:         ns.foo.ua\n" +
                "nserver:         ns2.foo.ua\n" +
                "changed:         bar@foo.ua 20120911\n" +
                "notify:          bar@foo.ua\n" +
                "source:          RIPE\n" +
                "mnt-by:          AARD-MNT"));

        freeTextIndex.update();

        final String searchResult = query("q=test&hl=true");
        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));

        final Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        assertThat(highlighting.keySet(), contains("2"));
        final Map<String, List<String>> map = highlighting.get("2");
        assertThat(map.keySet(), contains("descr"));
        assertThat(map.get("descr"), contains("<b>T.E.S.T</b>. Ltd"));
    }

    @Test
    public void search_no_match() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "source: RIPE"));

        freeTextIndex.update();

        final String searchResult = query("q=10.0.0.0");
        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
    }

    @Test
    public void search_word_match_subword_case_change() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=donald");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_subword_dash_separator() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  MNT-TESTUA\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=TESTUA");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_match_original() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald1\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=mcdonald1");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_matching_object_type() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=mcdonald+AND+object-type%3Aperson");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_word_with_non_matching_object_type() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=mcdonald+AND+object-type%3Ainetnum");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(0L));
    }

    @Test
    public void search_by_attribute_and_object_type() {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: AA1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=(nic-hdl:(AA1-RIPE))+AND+(object-type:person)");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }


    @Test
    public void search_hyphenated_complete_word() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
        freeTextIndex.rebuild();

        final String searchResult = query("q=NINJA-MNT");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_hyphenated_partial_word() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  TESTUA-MNT\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:  NINJA-MNT\n" +
                "source: RIPE"));
        freeTextIndex.rebuild();

        final String searchResult = query("q=NINJA");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_match_all_terms() throws Exception {
        databaseHelper.addObject(RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: JM1-RIPE\n" +
                "source: RIPE"));
        databaseHelper.addObject(RpslObject.parse("" +
                "person: Kate McDonald\n" +
                "nic-hdl: KM1-RIPE\n" +
                "source: RIPE"));

        freeTextIndex.rebuild();

        final String searchResult = query("q=John+McDonald");

        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(searchResult));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        assertThat(queryResponse.getStatus(), is(0));
        assertThat(queryResponse.getResults().getNumFound(), is(1L));
    }

    @Test
    public void search_for_deleted_object() {
        final RpslObject object = RpslObject.parse("" +
                "person: John McDonald\n" +
                "nic-hdl: JM1-RIPE\n" +
                "source: RIPE");
        databaseHelper.addObject(object);
        freeTextIndex.rebuild();

        assertThat(query("q=JM1-RIPE"), containsString("numFound=\"1\""));

        databaseHelper.removeObject(object);
        freeTextIndex.scheduledUpdate();

        assertThat(query("q=JM1-RIPE"), containsString("numFound=\"0\""));
    }

    @Test
    public void search_with_forward_slash() {
        databaseHelper.addObject(RpslObject.parse(
                "inet6num: 2a00:1f78::fffe/48\n" +
                        "netname: RIPE-NCC\n" +
                        "descr: some description\n" +
                        "source: TEST"));
        freeTextIndex.rebuild();

        assertThat(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48"), containsString("numFound=\"1\""));
        assertThat(query("q=212.166.64.0%2F19"), containsString("numFound=\"0\""));
    }

    @Test
    public void nullpointerbug() {
        assertThat(query("q=%28http%5C%3A%2F%2Fvv.uka.ru%29"), containsString("numFound=\"0\""));
    }

    @Test
    public void search_inetnum() throws Exception {
        databaseHelper.addObject(
               "inetnum:        193.0.0.0 - 193.0.0.255\n" +
               "netname:        RIPE-NCC\n" +
               "source:         RIPE");

        freeTextIndex.rebuild();

        assertThat(query("q=193.0.0.0"), containsString("numFound=\"1\""));
        assertThat(query("q=193.0.0.255"), containsString("numFound=\"1\""));
        assertThat(query("q=193%2E0%2E0%2E255"), containsString("numFound=\"1\""));
        assertThat(query("q=%28193%2E0%2E0%2E255%29"), containsString("numFound=\"1\""));
        assertThat(query("q=193"), containsString("numFound=\"1\""));
        assertThat(query("q=193.0"), containsString("numFound=\"1\""));
        assertThat(query("q=193.0.0"), containsString("numFound=\"1\""));
        assertThat(query("q=ripe-ncc"), containsString("numFound=\"1\""));
        assertThat(query("q=ripe"), containsString("numFound=\"1\""));
        assertThat(query("q=ncc"), containsString("numFound=\"1\""));
        assertThat(query("q=ripencc"), containsString("numFound=\"1\""));
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

        freeTextIndex.rebuild();

        assertThat(query("q=193.0.0.0"), containsString("numFound=\"1\""));
        assertThat(query("q=193.1.0.0"), containsString("numFound=\"1\""));
        assertThat(query("q=193"), containsString("numFound=\"2\""));
    }

    @Test
    public void search_inet6num() throws Exception {
        databaseHelper.addObject(
                "inet6num: 2001:0638:0501::/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");

        freeTextIndex.rebuild();

        assertThat(query("q=%282001%29"), containsString("numFound=\"1\""));
        assertThat(query("q=%282001%5C%3A0638%29"), containsString("numFound=\"1\""));
        assertThat(query("q=%282001%5C%3A0638%5C%3A0501%29"), containsString("numFound=\"1\""));
        assertThat(query("q=%282001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48%29"), containsString("numFound=\"1\""));
        assertThat(query("q=2001"), containsString("numFound=\"1\""));
        assertThat(query("q=2001%5C%3A0638"), containsString("numFound=\"1\""));
        assertThat(query("q=2001%5C%3A0638%5C%3A0501"), containsString("numFound=\"1\""));
        assertThat(query("q=2001%5C%3A0638%5C%3A0501%5C%3A%5C%3A%2F48"), containsString("numFound=\"1\""));
    }

    @Test
    public void search_inet6num_double_colons() throws Exception {
        databaseHelper.addObject(
                "inet6num: 2a00:1f78::fffe/48\n" +
                "netname: RIPE-NCC\n" +
                "source: RIPE\n");

        freeTextIndex.rebuild();

        assertThat(query("q=2a00"), containsString("numFound=\"1\""));
        assertThat(query("q=2a00%5C%3A1f78"), containsString("numFound=\"1\""));       // need to escape single colon (used as separator by lucene)
        assertThat(query("q=2a00%5C%3A1f78%5C%3A%5C%3Afffe%2F48"), containsString("numFound=\"1\""));

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

        freeTextIndex.rebuild();

        assertThat(query("q=2a00"), containsString("numFound=\"2\""));
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
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       RIPE\n"));
        freeTextIndex.rebuild();

        assertThat(query("q=Company"), containsString("numFound=\"1\""));
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
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       RIPE\n"));
        freeTextIndex.rebuild();

        assertThat(query("q=company,"), containsString("numFound=\"1\""));
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
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       RIPE\n"));
        freeTextIndex.rebuild();

        assertThat(query("q=company"), containsString("numFound=\"1\""));
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
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       RIPE\n"));
        freeTextIndex.rebuild();

        assertThat(query("q=ORG-TOS1-TEST"), containsString("numFound=\"1\""));
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
                "changed:      dbtest@ripe.net 20120505\n" +
                "source:       RIPE\n"));
        freeTextIndex.rebuild();

        assertThat(query("q=test.com"), containsString("numFound=\"1\""));
    }

    private final String query(final String queryString) {
        return client
                .resource(String.format("http://localhost:%s/search?%s", getPort(AUDIENCE), queryString))
                .get(String.class);
    }
}
