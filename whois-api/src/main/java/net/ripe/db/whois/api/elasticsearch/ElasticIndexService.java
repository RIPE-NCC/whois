package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
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

    public static final String OBJECT_TYPE_FIELD_NAME = "object-type";

    public static final String PRIMARY_KEY_FIELD_NAME = "primary-key";

    public static final String LOOKUP_KEY_FIELD_NAME = "lookup-key";
    private static final Logger LOGGER = getLogger(ElasticIndexService.class);

    public static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.CERTIF,
            AttributeType.CHANGED, AttributeType.SOURCE), AttributeType.class);
    public static final Set<AttributeType> FILTERED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH),
            AttributeType.class);

    private static final String SERIAL_DOC_ID = "1";
    public static final String SERIAL = "serial";
    public static final String SOURCE = "source";

    private final RestHighLevelClient client;
    private final String whoisAliasIndex;
    private final String metadataIndex;

    @Autowired
    public ElasticIndexService(final ElasticRestHighlevelClient elasticRestHighlevelClient,
                               @Value("${elastic.whois.index:whois}") final String whoisAliasName,
                               @Value("${elastic.commit.index:metadata}") final String whoisMetadataIndexName) {
        this.whoisAliasIndex = whoisAliasName;
        this.metadataIndex = whoisMetadataIndexName;
        this.client = elasticRestHighlevelClient.getClient();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        if (isElasticRunning()) {
            client.close();
        }
    }

    public boolean isEnabled() {
        if (!isElasticRunning()) {
            LOGGER.error("Elasticsearch cluster is not running");
            return false;
        }

        if (!isWhoisIndexExist()) {
            LOGGER.error("Elasticsearch index does not exist");
            return false;
        }

        if (!isMetaIndexExist()) {
            LOGGER.error("Elasticsearch meta index does not exists");
            return false;
        }

        return true;
    }

    protected void createOrUpdateEntry(final RpslObject rpslObject) throws IOException {
        if (!isElasticRunning()) {
            return;
        }

        try {
            final IndexRequest request = new IndexRequest(whoisAliasIndex);
            request.id(String.valueOf(rpslObject.getObjectId()));
            request.source(json(rpslObject));
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception ioe) {
            LOGGER.error("Failed to ES index {}: {}", rpslObject.getKey(), ioe);
        }
    }

    protected void deleteEntry(final int objectId) {
        if (!isElasticRunning()) {
           return;
        }

        try {
            final DeleteRequest request = new DeleteRequest(whoisAliasIndex, String.valueOf(objectId));
            client.delete(request, RequestOptions.DEFAULT);
        }  catch (Exception ioe) {
            LOGGER.error("Failed to delete ES index object id {}: {}", objectId, ioe);
        }
    }

    protected void deleteAll() throws IOException {
        if (!isElasticRunning()) {
            return;
        }

        final DeleteByQueryRequest request = new DeleteByQueryRequest(whoisAliasIndex);
        request.setQuery(QueryBuilders.matchAllQuery());

        client.deleteByQuery(request, RequestOptions.DEFAULT);
    }

    protected void refreshIndex(){
        try {
            client.indices().refresh(new RefreshRequest(whoisAliasIndex), RequestOptions.DEFAULT);
        } catch (IOException ex){
            LOGGER.error("Failed to refresh ES index {}: {}", whoisAliasIndex, ex);
        }
    }

    protected long getWhoisDocCount() throws IOException {
        return getWhoisDocCount(whoisAliasIndex);
    }

    public long getWhoisDocCount(final String indexName) throws IOException {
        if (!isElasticRunning()) {
            throw new IllegalStateException("ES is not running");
        }

        final CountRequest countRequest = new CountRequest(indexName);
        countRequest.query(QueryBuilders.matchAllQuery());

        final CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
        return countResponse.getCount();
    }

    protected ElasticIndexMetadata getMetadata() throws IOException {
        if (!isElasticRunning()) {
            throw new IllegalStateException("ES is not running");
        }

        final GetRequest request = new GetRequest(metadataIndex, SERIAL_DOC_ID);
        final GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        if (documentFields.getSource() == null) {
            return null;
        }

        return new ElasticIndexMetadata(
            Integer.parseInt(documentFields.getSource().get(SERIAL).toString()),
            documentFields.getSource().get(SOURCE).toString());
    }

    public void updateMetadata(final ElasticIndexMetadata metadata) throws IOException {
        updateMetadata(metadata, metadataIndex);
    }

    public String getMetadataIndex() {
        return metadataIndex;
    }

    public void updateMetadata(final ElasticIndexMetadata metadata, final String metadatIndexName) throws IOException {
        if (!isElasticRunning()) {
            return;
        }

        final UpdateRequest updateRequest = new UpdateRequest(metadatIndexName, SERIAL_DOC_ID);

        final XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field(SERIAL, metadata.getSerial())
                .field(SOURCE, metadata.getSource())
                .endObject();
        final UpdateRequest request = updateRequest.doc(builder).upsert(builder);

        client.update(request, RequestOptions.DEFAULT);
    }

    private boolean isElasticRunning() {
        try {
            return client !=null && client.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("ElasticSearch is not running, caught {}: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }
    private boolean isWhoisIndexExist() {
        final GetIndexRequest request = new GetIndexRequest(whoisAliasIndex);
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

    public XContentBuilder json(final RpslObject rpslObject) throws IOException {
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

        builder.field(LOOKUP_KEY_FIELD_NAME, rpslObject.getKey().toString());
        builder.field(OBJECT_TYPE_FIELD_NAME, filterRpslObject.getType().getName());

        return builder.endObject();
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public String getWhoisAliasIndex() {
        return whoisAliasIndex;
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
