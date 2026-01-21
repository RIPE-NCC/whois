package net.ripe.db.whois.api.fulltextsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexMetadata;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.elasticsearch.ElasticSearchConfigurations.getMappings;
import static net.ripe.db.whois.api.elasticsearch.ElasticSearchConfigurations.getSettings;

@Component
public class ElasticFullTextRebuild {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFullTextRebuild.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ElasticIndexService elasticIndexService;

    private static final int BATCH_SIZE = 100;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String source;
    private final List<String> elasticHosts;
    private final String logLevel;

    @Autowired
    public ElasticFullTextRebuild(final ElasticIndexService elasticIndexService,
                                  @Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts,
                                  @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
                                  @Value("${whois.source}") final String source,
                                  @Value("${elastic.deprecation.log.level:DEBUG}") final String logLevel) {
        this.elasticIndexService = elasticIndexService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
        this.elasticHosts = elasticHosts;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.logLevel = logLevel;
    }

    public void run() throws IOException {
        LOGGER.info("Source is  {}", source);
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final String indexName = "whois-" + DATE_TIME_FORMATTER.format(LocalDateTime.now());

        rebuild(indexName, elasticIndexService.getMetadataIndex(), true);

        deleteOldIndexes(indexName);

        LOGGER.info("ES indexing complete {}", stopwatch);
    }

    public void rebuild(final String indexName, final String metadataName, final boolean shouldSetAlias) throws IOException {

        final ElasticsearchClient client = elasticIndexService.getClient();

        LOGGER.info("Start building index {}", indexName);

        createIndex(indexName);

        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        LOGGER.info("Indexing upto serial number {}", maxSerial);

        final List<List<Integer>> objectIds = Lists.partition(jdbcTemplate.queryForList("select object_id from last where sequence_id > 0", Integer.class), BATCH_SIZE);

        final Timer timer = new Timer(true);
        final AtomicInteger totalProcessed = new AtomicInteger(0);
        printProgress(totalProcessed, objectIds.stream().mapToInt(i -> i.size()).sum(), timer);

        final List<String> failedToIndexed = Lists.newArrayList();
        final List<CIString> failedToParse = Lists.newArrayList();

        final ForkJoinPool customThreadPool = new ForkJoinPool(4);

        Future future = customThreadPool.submit(
                () -> objectIds.parallelStream().forEach(batchObjectIds -> {
                    final List<BulkOperation> bulkOperations = new ArrayList<>();

                    final List<RpslObject> objects = getObjects(batchObjectIds);
                    objects.forEach(rpslObject -> {
                        try {

                            bulkOperations.add(
                                    BulkOperation.of( op -> op.index(
                                            IndexOperation.of( idx ->  idx
                                                            .index(indexName)
                                                            .id(String.valueOf(rpslObject.getObjectId()))
                                                            .document(elasticIndexService.json(rpslObject))
                                            )
                                          )
                                    )
                            );
                        } catch (final Exception ioe) {
                            failedToParse.add(rpslObject.getKey());
                            LOGGER.warn("Failed to parse rpslObject , skipping indexing {}: {}", rpslObject.getKey(), ioe);
                        }
                    });
                    totalProcessed.addAndGet(objects.size());

                    performBulkIndexing(client, failedToIndexed, new BulkRequest.Builder().operations(bulkOperations).build());
                }));

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while rebuilding indexes, due to {}", e);
            throw new RuntimeException(e);
        } finally {
            customThreadPool.shutdown();
        }

        timer.cancel();

        LOGGER.info("Total objects processed {}", totalProcessed.get());
        LOGGER.info("Total objects indexed {}", elasticIndexService.getWhoisDocCount(indexName));

        LOGGER.warn("This many {} Objects failed to indexed, these are {}", failedToIndexed.size(), failedToIndexed);
        LOGGER.warn("This many {} Objects failed to parsed, these are {}", failedToParse.size(), failedToParse);

        if(shouldSetAlias) {
          setNewIndexAsWhoisAlias(indexName);
        }

        updateMetadata(maxSerial, metadataName);
    }

    private static void performBulkIndexing(final ElasticsearchClient client, final List<String> failedToIndexed, final BulkRequest bulkRequest) {
        try {
            final BulkResponse response = client.bulk(bulkRequest);
            if(response.errors()) {

                final List<BulkResponseItem> failedItems = response.items().stream().filter(bulkResponseItem -> bulkResponseItem.error() != null).toList();
                failedToIndexed.addAll(
                        failedItems.stream().map(BulkResponseItem::id).collect(Collectors.toSet())
                );

                failedItems.forEach(failedItem -> {
                    LOGGER.error("Failures in bulk request for id {} caused by {}", failedItem.id(),  failedItem.error());
                });
            }
        } catch (final IOException e) {
            LOGGER.error("Error while indexing bulk request, due to", e);
            throw new RuntimeException(e);
        }
    }

    private void createIndex(final String indexName) throws IOException {
      final ElasticsearchClient esClient = elasticIndexService.getClient();

      final CreateIndexRequest request = new CreateIndexRequest.Builder()
              .index(indexName)
              .settings(getSettings(elasticHosts.size()))
              .mappings( getMappings())
              .build();

      esClient.indices().create(request);
    }

    private void setNewIndexAsWhoisAlias(final String indexName) {
        LOGGER.info("Setting index {} as default whois index", indexName);

        final ElasticsearchClient esClient = elasticIndexService.getClient();

        try {
            final UpdateAliasesRequest request = new UpdateAliasesRequest.Builder()
                    .actions(actions -> actions
                            .add(add -> add
                                    .index(indexName)
                                    .alias(elasticIndexService.getWhoisAliasIndex())
                            )
                    )
                    .build();

            final UpdateAliasesResponse response = esClient.indices().updateAliases(request);
            LOGGER.info("Alias updated acknowledged: {}", response.acknowledged());

        } catch (IOException e) {
            LOGGER.warn("Failed to set {} as default index", indexName, e);
        }
    }

    private void deleteOldIndexes(final String currentIndex) {
        LOGGER.info("Deleting existing indexes");

        final ElasticsearchClient esClient = elasticIndexService.getClient();

        try {
            final GetIndexRequest request = new GetIndexRequest.Builder()
                    .index("*")
                    .build();

            final GetIndexResponse response = esClient.indices().get(request);

            for (final String index : response.result().keySet()) {
                if (index.equals(currentIndex) || index.equals("metadata")) {
                    continue;
                }
                LOGGER.info("Deleting existing index: {}", index);
                deleteIndex(index);
            }

        } catch (Exception ex) {
            LOGGER.warn("Failed to list or delete old indexes", ex);
        }
    }

    private void deleteIndex(final String indexName) {
        final ElasticsearchClient esClient = elasticIndexService.getClient();

        try {
            final DeleteIndexRequest delRequest = new DeleteIndexRequest.Builder()
                    .index(indexName)
                    .build();
            esClient.indices().delete(delRequest);
        } catch (Exception ex) {
            LOGGER.warn("{} Index deleting failed, still continuing {}", indexName, ex.getCause());
        }
    }

    private void updateMetadata(final int maxSerial, final String metadataName) {
        LOGGER.info("Setting metadata Index");

        try {
            // Update your own metadata service
            elasticIndexService.updateMetadata(new ElasticIndexMetadata(maxSerial, source), metadataName);

            final ElasticsearchClient esClient = elasticIndexService.getClient();

            // Directly update index settings using builder
            esClient.indices().putSettings(builder -> builder
                    .index(metadataName)
                    .settings( IndexSettings.of(s -> s
                                        .index(i -> i
                                                .numberOfReplicas(String.valueOf(elasticHosts.size() - 1))
                                                .autoExpandReplicas("false")
                                        )
                    ))
            );

            LOGGER.info("Metadata index settings updated successfully");

        } catch (final Exception ex) {
            LOGGER.warn("Failed to update metadata index: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to set default metadata index", ex);
        }
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
}
