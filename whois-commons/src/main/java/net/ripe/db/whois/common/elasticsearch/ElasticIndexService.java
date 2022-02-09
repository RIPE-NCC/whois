package net.ripe.db.whois.common.elasticsearch;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
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
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ElasticIndexService {
    private static final Logger LOGGER = getLogger(ElasticIndexService.class);
    private final RestHighLevelClient client;
    private static final String SERIAL_DOC_ID = "1";
    private final String WHOIS_INDEX;
    private final String METADATA_INDEX;

    private static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.CERTIF, AttributeType.CHANGED), AttributeType.class);
    private static final Set<AttributeType> FILTERED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH), AttributeType.class);


    @Autowired
    public ElasticIndexService(@Value("${elastic.host:localhost}") final String elasticHost,
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
        DeleteRequest request = new DeleteRequest(WHOIS_INDEX, String.valueOf(objectId));
        client.delete(request, RequestOptions.DEFAULT);
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

    public ElasticIndexMetadata getMetadata() throws IOException {
        GetRequest request = new GetRequest(METADATA_INDEX, SERIAL_DOC_ID);
        GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        if (documentFields.getSource() == null) {
            return null;
        }
        return new ElasticIndexMetadata(Integer.parseInt(documentFields.getSource().get("serial").toString()), documentFields.getSource().get("source").toString());
    }

    public void updateMetadata(ElasticIndexMetadata metadata) throws IOException {
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
            LOGGER.warn("ElasticSearch is not running");
            return false;
        }
    }
    private boolean isWhoisIndexExist() {
        GetIndexRequest request = new GetIndexRequest(WHOIS_INDEX);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("Whois index does not exist");
            return false;
        }
    }

    private boolean isMetaIndexExist() {
        GetIndexRequest request = new GetIndexRequest(METADATA_INDEX);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("Metadata index does not exist");
            return false;
        }
    }

    private XContentBuilder json(final RpslObject rpslObject) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

        final RpslObject filterRpslObject = filterRpslObject(rpslObject);
        final ObjectTemplate template = ObjectTemplate.getTemplate(filterRpslObject.getType());

        for (AttributeType attributeType : template.getAllAttributes()) {
            if(filterRpslObject.containsAttribute(attributeType)) {
                if (template.getMultipleAttributes().contains(attributeType)) {
                    builder.array(
                            attributeType.getName(),
                            filterRpslObject.findAttributes(attributeType).stream().map((attribute) -> attribute.getValue().trim()).toArray(String[]::new)
                    );
                } else {
                    builder.field(attributeType.getName(), filterRpslObject.findAttribute(attributeType).getValue().trim());
                }
            }
        }

        builder.field("primary-key", filterRpslObject.getKey().toString());
        builder.field("object-type", filterRpslObject.getType().getName());

        return builder.endObject();
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public String getWHOIS_INDEX() {
        return WHOIS_INDEX;
    }

    public RpslObject filterRpslObject(final RpslObject rpslObject) {
        List<RpslAttribute> attributes = Lists.newArrayList();

        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            if (SKIPPED_ATTRIBUTES.contains(attribute.getType())) {
                continue;
            }
            attributes.add(new RpslAttribute(attribute.getKey(), filterRpslAttribute(attribute.getType(), attribute.getValue())));
        }
        return new RpslObject(rpslObject.getObjectId(), attributes);
    }

    public String filterRpslAttribute(final AttributeType attributeType, final String attributeValue) {

        if (FILTERED_ATTRIBUTES.contains(attributeType)) {
            return sanitise(filterAttribute(attributeValue.trim()));
        }

        return sanitise(attributeValue.trim());
    }

    private String filterAttribute(final String value) {
        if (value.toLowerCase().startsWith("md5-pw")) {
            return "MD5-PW";
        }

        if (value.toLowerCase().startsWith("sso")) {
            return "SSO";
        }

        return value;
    }

    private static String sanitise(final String value) {
        // TODO: [ES] also strips newlines, attribute cannot be re-constructed later
        return CharMatcher.javaIsoControl().removeFrom(value);
    }
}
