package net.ripe.db.whois.api.fulltextsearch;

import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import java.io.StringReader;

public class FullTextSolrUtils {

    public static QueryResponse parseResponse(final String fullTextResponse) {
        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(fullTextResponse));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        return queryResponse;
    }
}
