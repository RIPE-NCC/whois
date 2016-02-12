package net.ripe.db.whois.api.freetext;

import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import java.io.StringReader;

public class FreeTextSolrUtils {

    public static QueryResponse parseResponse(final String freeTextResponse) {
        final NamedList<Object> namedList = new XMLResponseParser().processResponse(new StringReader(freeTextResponse));
        final QueryResponse queryResponse = new QueryResponse();
        queryResponse.setResponse(namedList);
        return queryResponse;
    }
}
