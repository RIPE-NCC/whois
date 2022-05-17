package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
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

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ElasticIndexService {
    private static final Logger LOGGER = getLogger(ElasticIndexService.class);

    private static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.CERTIF, AttributeType.CHANGED, AttributeType.SOURCE), AttributeType.class);
    private static final Set<AttributeType> FILTERED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH), AttributeType.class);

    private static final String SERIAL_DOC_ID = "1";

    private final RestHighLevelClient client;
    private final String whoisIndex;
    private final String metadataIndex;

    @Autowired
    public ElasticIndexService(@Value("#{'${elastic.host:localhost:9200}'.split(',')}") final List<String> elasticHosts,
                               @Value("${elastic.whois.index:whois}") final String whoisIndexName,
                               @Value("${elastic.commit.index:metadata}") final String whoisMetadataIndexName) {
        this.whoisIndex = whoisIndexName;
        this.metadataIndex = whoisMetadataIndexName;
        final RestClientBuilder clientBuilder = RestClient.builder(elasticHosts.stream().map( (host) -> HttpHost.create(host)).toArray(HttpHost[]::new));
        client = new RestHighLevelClient(clientBuilder);
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        client.close();
    }

    public boolean isEnabled() {
        if (!isElasticRunning()) {
            LOGGER.debug("Elasticsearch cluster is not running");
            return false;
        }

        if (!isWhoisIndexExist()) {
            LOGGER.debug("Elasticsearch index does not exist");
            return false;
        }

        if (!isMetaIndexExist()) {
            LOGGER.debug("Elasticsearch meta index does not exists");
            return false;
        }

        return true;
    }

    public void addEntry(RpslObject rpslObject) throws IOException {
        final IndexRequest request = new IndexRequest(whoisIndex);
        request.id(String.valueOf(rpslObject.getObjectId()));
        request.source(json(rpslObject));
        client.index(request, RequestOptions.DEFAULT);
    }

    public void deleteEntry(int objectId) throws IOException {
        final DeleteRequest request = new DeleteRequest(whoisIndex, String.valueOf(objectId));
        client.delete(request, RequestOptions.DEFAULT);
    }

    public void deleteAll() throws IOException {
        final DeleteByQueryRequest request = new DeleteByQueryRequest(whoisIndex);
        request.setQuery(QueryBuilders.matchAllQuery());

        client.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    public long getWhoisDocCount() throws IOException {
        final CountRequest countRequest = new CountRequest(whoisIndex);
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        final CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    public ElasticIndexMetadata getMetadata() throws IOException {
        final GetRequest request = new GetRequest(metadataIndex, SERIAL_DOC_ID);
        final GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        if (documentFields.getSource() == null) {
            return null;
        }
        return new ElasticIndexMetadata(
            Integer.parseInt(documentFields.getSource().get("serial").toString()),
            documentFields.getSource().get("source").toString());
    }

    public void updateMetadata(ElasticIndexMetadata metadata) throws IOException {
        final UpdateRequest updateRequest = new UpdateRequest(metadataIndex, SERIAL_DOC_ID);

        final XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("serial", metadata.getSerial())
                .field("source", metadata.getSource())
                .endObject();
        final UpdateRequest request = updateRequest.doc(builder).upsert(builder);

        client.update(request, RequestOptions.DEFAULT);
    }

    private boolean isElasticRunning() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.info("ElasticSearch is not running, caught {}: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }
    private boolean isWhoisIndexExist() {
        final GetIndexRequest request = new GetIndexRequest(whoisIndex);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.info("Whois index does not exist");
            return false;
        }
    }

    private boolean isMetaIndexExist() {
        final GetIndexRequest request = new GetIndexRequest(metadataIndex);
        try {
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.info("Metadata index does not exist");
            return false;
        }
    }

    private XContentBuilder json(final RpslObject rpslObject) throws IOException {
        final XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

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

        builder.field(FullTextIndex.LOOKUP_KEY_FIELD_NAME, rpslObject.getKey().toString());
        builder.field(FullTextIndex.OBJECT_TYPE_FIELD_NAME, filterRpslObject.getType().getName());

        return builder.endObject();
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public String getWhoisIndex() {
        return whoisIndex;
    }

    public RpslObject filterRpslObject(final RpslObject rpslObject) {
        final List<RpslAttribute> attributes = Lists.newArrayList();

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
