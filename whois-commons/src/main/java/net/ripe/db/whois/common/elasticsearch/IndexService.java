package net.ripe.db.whois.common.elasticsearch;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;

import static org.slf4j.LoggerFactory.getLogger;


@Component
public class IndexService {
    private static final Logger LOGGER = getLogger(IndexService.class);
    private final RestHighLevelClient client;
    private static final String SERIAL_DOC_ID = "1";
    private final String WHOIS_INDEX;
    private final String METADATA_INDEX;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public IndexService(@Value("${elastic.host:localhost}") final String elasticHost,
                        @Value("${elastic.port:9200}") final int elasticPort,
                        @Value("${elastic.whois.index:whois}") final String whoisIndexName,
                        @Value("${elastic.commit.index:metadata}") final String whoisMetadataIndexName) {
        this.WHOIS_INDEX = whoisIndexName;
        this.METADATA_INDEX = whoisMetadataIndexName;
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(elasticHost, elasticPort));
        client = new RestHighLevelClient(clientBuilder);
    }

    public boolean isEnabled() {
        try {
            return isWhoisIndexExist() && isSerialIndexExist();
        } catch (IOException e) {
            LOGGER.error("Checking if Elastic is available failed", e);
            return false;
        }
    }

    public void addEntry(RpslObject rpslObject) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(rpslObject);
        IndexRequest request = new IndexRequest(WHOIS_INDEX);
        request.source(bytes, XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    public void deleteEntry(int objectId) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(WHOIS_INDEX);
        request.setQuery(new TermQueryBuilder("objectId", objectId));
        client.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    public void deleteAll() throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(WHOIS_INDEX);
        request.setQuery(QueryBuilders.matchAllQuery());

        client.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    public long getWhoisDocCount() throws IOException {
        CountRequest countRequest = new CountRequest(WHOIS_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    public IndexMetadata getMetadata() throws IOException {
        GetRequest request = new GetRequest(METADATA_INDEX, SERIAL_DOC_ID);
        GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        if (documentFields.getSource() == null) {
            return null;
        }
        return new IndexMetadata(Integer.parseInt(documentFields.getSource().get("serial").toString()), documentFields.getSource().get("source").toString());
    }

    public void updateMetadata(IndexMetadata metadata) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(METADATA_INDEX, SERIAL_DOC_ID);

        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("serial", metadata.getSerial())
                .field("source", metadata.getSource())
                .endObject();
        UpdateRequest request = updateRequest.doc(builder).upsert(builder);

        client.update(request, RequestOptions.DEFAULT);
    }

    private boolean isWhoisIndexExist() {
        GetIndexRequest request = new GetIndexRequest(WHOIS_INDEX);
        try {
            IndicesClient indices = client.indices();
            return indices.exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Elastic Instance is not running");
            return false;
        }
    }

    private boolean isSerialIndexExist() throws IOException {
        GetIndexRequest request = new GetIndexRequest(METADATA_INDEX);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }
}
