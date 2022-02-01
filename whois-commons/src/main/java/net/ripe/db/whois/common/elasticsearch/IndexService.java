package net.ripe.db.whois.common.elasticsearch;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

        if(!isElasticRunning()) {
            LOGGER.info("ES cluster is not running");
            return false;
        }

        if(!isWhoisIndexExist()) {
            LOGGER.info("ES index does not exists");
            return false;
        }

        if(!isMetaIndexExist()) {
            LOGGER.info("ES metaIndex does not exists");
            return false;
        }

        return true;
    }

    public void addEntry(RpslObject rpslObject) throws IOException {
        IndexRequest request = new IndexRequest(WHOIS_INDEX);
        request.id(String.valueOf(rpslObject.getObjectId()));
        request.source(json(rpslObject));
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

    private boolean isElasticRunning() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("ElasticSearch is not running");
            return false;
        }
    }
    private boolean isWhoisIndexExist() {
        GetIndexRequest request = new GetIndexRequest(WHOIS_INDEX);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Whois index does not exist");
            return false;
        }
    }

    private boolean isMetaIndexExist() {
        GetIndexRequest request = new GetIndexRequest(METADATA_INDEX);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("Metadata index does not exist");
            return false;
        }
    }
    
    private XContentBuilder json(final RpslObject rpslObject) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

        builder.field(
                "object",
                new RpslObjectBuilder(rpslObject).removeAttributeType(AttributeType.AUTH).get().toString()
        );

        return builder.endObject();
    }
}
