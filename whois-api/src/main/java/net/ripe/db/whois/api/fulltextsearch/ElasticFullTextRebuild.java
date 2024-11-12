package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexMetadata;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.settings.Settings;
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
                                  @Value("${elastic.deprecation.log.level:DEBUG}") final String logLevel){
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

        final RestHighLevelClient client = elasticIndexService.getClient();

        LOGGER.info("Start building index {}", indexName);

        createIndex(indexName);

        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        LOGGER.info("Indexing upto serial number {}", maxSerial);

        final List<List<Integer>> objectIds = Lists.partition(jdbcTemplate.queryForList("select object_id from last where sequence_id > 0", Integer.class), BATCH_SIZE);

        final Timer timer = new Timer(true);
        final AtomicInteger totalProcessed = new AtomicInteger(0);
        printProgress(totalProcessed, objectIds.stream().mapToInt(i -> i.size()).sum(), timer);

        final List<Integer> failedToIndexed = Lists.newArrayList();
        final List<CIString> failedToParse = Lists.newArrayList();

        final ForkJoinPool customThreadPool = new ForkJoinPool(4);

        Future future = customThreadPool.submit(
                () -> objectIds.parallelStream().forEach(batchObjectIds -> {
                    final BulkRequest bulkRequest = new BulkRequest();

                    final List<RpslObject> objects = getObjects(batchObjectIds);
                    objects.forEach(rpslObject -> {
                        try {
                            bulkRequest.add(new IndexRequest(indexName)
                                    .id(String.valueOf(rpslObject.getObjectId()))
                                    .source(elasticIndexService.json(rpslObject))
                            );
                        } catch (final Exception ioe) {
                            failedToParse.add(rpslObject.getKey());
                            LOGGER.warn("Failed to parse rpslObject , skipping indexing {}: {}", rpslObject.getKey(), ioe);
                        }
                    });
                    totalProcessed.addAndGet(objects.size());

                    performBulkIndexing(client, failedToIndexed, bulkRequest);
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

    private void createIndex(final String indexName) throws IOException {
        final CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(getSettings(elasticHosts.size()));
        request.mapping(getMappings());

        elasticIndexService.getClient().indices().create(request, RequestOptions.DEFAULT);

        final ClusterUpdateSettingsRequest clusterUpdateSettingsRequest = new ClusterUpdateSettingsRequest();
        Settings persistentSettings = Settings.builder()
                .put("logger.deprecation.level", logLevel)
                .build();
        clusterUpdateSettingsRequest.persistentSettings(persistentSettings);
        elasticIndexService.getClient().cluster().putSettings(clusterUpdateSettingsRequest, RequestOptions.DEFAULT);
    }

    private void setNewIndexAsWhoisAlias(final String indexName) {
        LOGGER.info("Setting index {} as default whois index", indexName);

        // swap whois alias to newly built index so it becomes the default
        final IndicesAliasesRequest aliasRequest = new IndicesAliasesRequest();
        final IndicesAliasesRequest.AliasActions addIndexAction =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                        .index(indexName)
                        .alias(elasticIndexService.getWhoisAliasIndex());
        aliasRequest.addAliasAction(addIndexAction);

        try {
            elasticIndexService.getClient().indices().updateAliases(aliasRequest, RequestOptions.DEFAULT);
        } catch (final IOException ioe) {
            LOGGER.warn("Failed to set {} as default index", indexName);
        }
    }

    private void deleteOldIndexes(final String currentIndex) {
        LOGGER.info("Deleting existing indexes");

        try {
            final GetIndexRequest request = new GetIndexRequest("*");
            final GetIndexResponse response = elasticIndexService.getClient().indices().get(request, RequestOptions.DEFAULT);


            for (final String index : response.getIndices()) {
                if(index.equals(currentIndex) || index.equals("metadata")) {
                    continue;
                }

                LOGGER.info("Deleting existing index:" + index);
                deleteIndex(index);
            }
        } catch (final Exception ex) {
        }
    }

    private void deleteIndex(final String indexName) {
        try {
            elasticIndexService.getClient().indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (final Exception ex) {
            LOGGER.warn("{} Index deleting failed , still continuing {}", indexName, ex.getCause());
        }
    }

    private void updateMetadata(final int maxSerial, final String metadataName) {

        LOGGER.info("Setting metadata Index");

        try {
            elasticIndexService.updateMetadata(new ElasticIndexMetadata(maxSerial, source), metadataName);

            final UpdateSettingsRequest settingRequest = new UpdateSettingsRequest(metadataName);

            final Map<String, Object> map = new HashMap<>();
            map.put("index.number_of_replicas", elasticHosts.size() - 1);
            map.put("index.auto_expand_replicas", false);
            settingRequest.settings(map);

            elasticIndexService.getClient().indices().putSettings(settingRequest, RequestOptions.DEFAULT);

            LOGGER.info("Setting metadata Index Done");

        } catch (final Exception ioe) {
            LOGGER.info("Caught {} on {}: {}", ioe.getClass(), ioe.getMessage());
            throw new RuntimeException("Failed to set default metadata index", ioe);
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
