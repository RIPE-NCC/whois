package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.CharMatcher;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.elasticsearch.ElasticSearchInstance;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.elasticsearch.ElasticSearchConfigurations.getMappings;
import static net.ripe.db.whois.api.elasticsearch.ElasticSearchConfigurations.getSettings;

@Component
public class ElasticFullTextRebuild {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFullTextRebuild.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ElasticSearchInstance elasticSearchInstance;
    private static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.CERTIF, AttributeType.CHANGED, AttributeType.SOURCE), AttributeType.class);
    private static final Set<AttributeType> FILTERED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH), AttributeType.class);

    private static final int BATCH_SIZE = 100;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String source;

    @Autowired
    public ElasticFullTextRebuild(final ElasticSearchInstance elasticSearchInstance,
                                  @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
                                  @Value("${whois.source}") final String source) {
        this.elasticSearchInstance = elasticSearchInstance;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public void rebuild() {

        LOGGER.info("Source is  {}", source);

        final Stopwatch stopwatch = Stopwatch.createStarted();
        final RestHighLevelClient client = elasticSearchInstance.getClient();


        final String indexName = "whois-" + DATE_TIME_FORMATTER.format(LocalDateTime.now());

        LOGGER.info("Start building index {}", indexName);

        createIndex(client, indexName, elasticHosts);

        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        LOGGER.info("Indexing upto serial number {}", maxSerial);

        final List<List<Integer>> objectIds = Lists.partition(jdbcTemplate.queryForList("select object_id from last where sequence_id > 0", Integer.class), BATCH_SIZE);

        final Timer timer = new Timer(true);
        final AtomicInteger totalProcessed = new AtomicInteger(0);
        printProgress(totalProcessed, objectIds.stream().mapToInt(i -> i.size()).sum(), timer);

        final List<Integer> failedToIndexed = Lists.newArrayList();
        final List<CIString> failedToParse = Lists.newArrayList();

        objectIds.parallelStream().forEach(batchObjectIds -> {
            final BulkRequest bulkRequest = new BulkRequest();

            final List<RpslObject> objects = getObjects(batchObjectIds);
            objects.forEach(rpslObject -> {
                try {
                    bulkRequest.add(new IndexRequest(indexName)
                            .id(String.valueOf(rpslObject.getObjectId()))
                            .source(json(rpslObject))
                    );
                } catch (final Exception ioe) {
                    failedToParse.add(rpslObject.getKey());
                    LOGGER.error("Failed to parse rpslObject , skipping indexing {}: {}", rpslObject.getKey(), ioe);
                }
            });
            totalProcessed.addAndGet(objects.size());

            performBulkIndexing(client, failedToIndexed, bulkRequest);
        });

        timer.cancel();

        LOGGER.info("Total objects processed {}", totalProcessed.get());
        LOGGER.info("Total objects indexed {}", getWhoisDocCount(indexName, client));

        LOGGER.warn("This many {} Objects failed to indexed, these are {}", failedToIndexed.size(), failedToIndexed);
        LOGGER.warn("This many {} Objects failed to parsed, these are {}", failedToParse.size(), failedToParse);

        setNewIndexAsWhoisAlias(client, indexName);
        updateMetadata(source, client, maxSerial, elasticHosts);

        deleteOldIndexes(client, indexName);

        LOGGER.info("ES indexing complete {}", stopwatch);

    }

    private void updateMetadata(final String source, final RestHighLevelClient client, final int maxSerial, final String[] elasticHosts) {
        LOGGER.info("Setting metadata Index");

        try {
            updateMetadata(client, maxSerial, source, elasticHosts.length);
        } catch (final Exception ioe) {
            LOGGER.info("Caught {} on {}: {}", ioe.getClass(), ioe.getMessage());
            throw new RuntimeException("Failed to set default metadata index", ioe);
        }
    }

    private static void performBulkIndexing(final RestHighLevelClient client, final List<Integer> failedToIndexed, final BulkRequest bulkRequest) {
        try {
            final BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if(response.hasFailures()) {
                failedToIndexed.addAll(Arrays.stream(response.getItems()).filter(BulkItemResponse::isFailed).map(BulkItemResponse::getItemId).collect(Collectors.toSet()));
                LOGGER.error("Failures in bulk request {}", response.buildFailureMessage());
            }
        } catch (final IOException e) {
            LOGGER.error("Error while indexing bulk request, due to {}", e);
            throw new RuntimeException(e);
        }
    }

    private void createIndex(final RestHighLevelClient client, final String indexName, final String[] elasticHosts) throws IOException {
        final CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(getSettings(elasticHosts.length));
        request.mapping(getMappings());

        client.indices().create(request, RequestOptions.DEFAULT);
    }

    private static void setNewIndexAsWhoisAlias(final RestHighLevelClient client, final String indexName) {
        LOGGER.info("Setting index {} as default whois index", indexName);

        // swap whois alias to newly built index so it becomes the default
        final IndicesAliasesRequest aliasRequest = new IndicesAliasesRequest();
        final IndicesAliasesRequest.AliasActions addIndexAction =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                        .index(indexName)
                        .alias("whois");
        aliasRequest.addAliasAction(addIndexAction);

        try {
            client.indices().updateAliases(aliasRequest, RequestOptions.DEFAULT);
        } catch (final IOException ioe) {
            LOGGER.warn("Failed to set {} as default index", indexName);
        }
    }

    private void deleteOldIndexes(final RestHighLevelClient client, final String currentIndex) {
        LOGGER.info("Deleting existing indexes");

        try {
            final GetIndexRequest request = new GetIndexRequest("*");
            final GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);


            for (final String index : response.getIndices()) {
                if(index.equals(currentIndex) || index.equals("metadata")) {
                    continue;
                }

                LOGGER.info("Deleting existing index:" + index);
                deleteIndex(client, index);
            }
        } catch (final Exception ex) {
        }
    }

    private void deleteIndex(final RestHighLevelClient client, final String indexName) {
        try {
            client.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            LOGGER.warn("{} Index deleting failed , still continuing {}", indexName, ex.getCause());
        }
    }

    public void updateMetadata(final  RestHighLevelClient client, final int maxSerial, final String source, final int nodes) throws IOException {
        final UpdateRequest updateRequest = new UpdateRequest("metadata", "1");

        final XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("serial", maxSerial)
                .field("source", source)
                .endObject();

        final UpdateRequest request = updateRequest.doc(builder).upsert(builder);

        client.update(request, RequestOptions.DEFAULT);

        final UpdateSettingsRequest settingRequest = new UpdateSettingsRequest("metadata");

        final Map<String, Object> map = new HashMap<>();
        map.put("index.number_of_replicas", nodes-1);
        map.put("index.auto_expand_replicas", false);
        settingRequest.settings(map);

        client.indices().putSettings(settingRequest, RequestOptions.DEFAULT);

        LOGGER.info("Setting metadata Index");
    }

    private XContentBuilder json(final RpslObject rpslObject) throws IOException {
        final XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

        final RpslObject filterRpslObject = filterRpslObject(rpslObject);
        final ObjectTemplate template = ObjectTemplate.getTemplate(filterRpslObject.getType());

        for (final AttributeType attributeType : template.getAllAttributes()) {
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

        builder.field("lookup-key", rpslObject.getKey().toString());
        builder.field("object-type", filterRpslObject.getType().getName());

        return builder.endObject();
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

    public List<RpslObject> getObjects(final List<Integer> objectIds) {
        final List<RpslObject> results = Lists.newArrayList();
        final Map<String, Object> params = Maps.newHashMap();
        params.put("objectids", objectIds);

        namedParameterJdbcTemplate.query(
                "SELECT object_id, object FROM last WHERE object_id IN (:objectids) and sequence_id > 0",
                params,
                rs -> {
                    final int objectId = rs.getInt(1);
                    try {
                        results.add(RpslObject.parse(objectId, rs.getBytes(2)));
                    } catch (final Exception ex) {
                        LOGGER.warn("failed to parse rpsl object for {} {}", objectId, ex );
                    }
                });

        return results;
    }

    private void printProgress(final AtomicInteger totalProcessed, final int totalInDb, final Timer timer) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int done = totalProcessed.get();
                LOGGER.info("Processed {} objects out of {} ({}%).", done, totalInDb,  (int) (done  * 100.0f/ (totalInDb)));
            }
        }, 0, 10000);
    }

    protected long getWhoisDocCount(final String indexName, final RestHighLevelClient client) {
        try {
            return client.count(new CountRequest(indexName), RequestOptions.DEFAULT).getCount();
        } catch (final IOException e) {
            LOGGER.error("Failed to get the count of objects indexed");
            return 0;
        }
    }

}
