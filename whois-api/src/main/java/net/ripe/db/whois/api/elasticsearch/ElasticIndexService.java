package net.ripe.db.whois.api.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.annotation.PreDestroy;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final ElasticsearchClient client;
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

    protected void createOrUpdateEntry(final RpslObject rpslObject) {
        if (!isElasticRunning()) {
            return;
        }

        try {
            client.index( i -> i
                    .index(whoisAliasIndex)
                    .id(String.valueOf(rpslObject.getObjectId()))
                    .document(json(rpslObject)));
        } catch (Exception ioe) {
            LOGGER.error("Failed to ES index {}: {}", rpslObject.getKey(), ioe);
        }
    }

    protected void deleteEntry(final int objectId) {
        if (!isElasticRunning()) {
            return;
        }

        try {
            client.delete(d -> d
                    .index(whoisAliasIndex)
                    .id(String.valueOf(objectId))
            );

        } catch (Exception e) {
            LOGGER.error("Failed to delete ES index object id {}: {}", objectId, e);
        }
    }

    protected void deleteAll() {
        if (!isElasticRunning()) {
            return;
        }

        try {
            client.deleteByQuery(d -> d
                    .index(whoisAliasIndex)
                    .query(q -> q.matchAll(m -> m))
            );
        } catch (Exception e) {
            LOGGER.error("Failed to delete all documents in index {}: {}", whoisAliasIndex, e);
        }
    }

    protected void refreshIndex(){
        try {
            client.indices().refresh(r -> r.index(whoisAliasIndex));
        } catch (IOException ex){
            LOGGER.error("Failed to refresh ES index {}: {}", whoisAliasIndex, ex);
        }
    }

    protected long getWhoisDocCount() throws IOException {
        return getWhoisDocCount(whoisAliasIndex);
    }

    public long getWhoisDocCount(final String indexName) {
        if (!isElasticRunning()) {
            throw new IllegalStateException("ES is not running");
        }

        try {
            return client.count(c -> c
                    .index(indexName)
                    .query(q -> q.matchAll(m -> m))
            ).count();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get document count for index " + indexName, e);
        }
    }


    protected ElasticIndexMetadata getMetadata() {
        if (!isElasticRunning()) {
            throw new IllegalStateException("ES is not running");
        }

        try {
            var response = client.get(g -> g
                            .index(metadataIndex)
                            .id(SERIAL_DOC_ID),
                    Map.class // deserialize source as Map<String,Object>
            );

            final Map<String, Object> source = response.source();
            if (source == null) {
                return null;
            }

            return new ElasticIndexMetadata(
                    Integer.parseInt(source.get(SERIAL).toString()),
                    source.get(SOURCE).toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to get metadata from index " + metadataIndex, e);
        }
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

        final Map<String, Object> doc = Map.of( SERIAL, metadata.getSerial(), SOURCE, metadata.getSource());

        client.update( u -> u.index(metadatIndexName)
                .id(SERIAL_DOC_ID)
                .doc(doc)
                .upsert(doc), Map.class);
    }

    private boolean isElasticRunning() {
        try {
            return client !=null && client.ping().value();
        } catch (Exception e) {
            LOGGER.error("ElasticSearch is not running, caught {}: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    private boolean isWhoisIndexExist() {
        try {
            return client.indices().exists(e -> e.index(whoisAliasIndex)).value();
        } catch (Exception e) {
            LOGGER.info("Whois index does not exist");
            return false;
        }
    }

    private boolean isMetaIndexExist() {
        try {
            return client.indices().exists(e -> e.index(metadataIndex)).value();
        } catch (Exception e) {
            LOGGER.info("Metadata index does not exist");
            return false;
        }
    }

    public Map<String, Object> json(final RpslObject rpslObject) {
        final Map<String, Object> doc = new HashMap<>();

        // Filter the object
        final RpslObject filterRpslObject = filterRpslObject(rpslObject);
        final ObjectTemplate template = ObjectTemplate.getTemplate(filterRpslObject.getType());

        // Loop over attributes
        for (AttributeType attributeType : template.getAllAttributes()) {
            if (filterRpslObject.containsAttribute(attributeType)) {
                if (template.getMultipleAttributes().contains(attributeType)) {
                    // Multiple values -> List of Strings
                    List<String> values = filterRpslObject.findAttributes(attributeType).stream()
                            .map(attr -> attr.getValue().trim())
                            .toList();
                    doc.put(attributeType.getName(), values);
                } else {
                    // Single value -> String
                    doc.put(attributeType.getName(), filterRpslObject.findAttribute(attributeType).getValue().trim());
                }
            }
        }

        // Add extra fields
        doc.put(LOOKUP_KEY_FIELD_NAME, rpslObject.getKey().toString());
        doc.put(OBJECT_TYPE_FIELD_NAME, filterRpslObject.getType().getName());

        return doc;
    }

    public ElasticsearchClient getClient() {
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
