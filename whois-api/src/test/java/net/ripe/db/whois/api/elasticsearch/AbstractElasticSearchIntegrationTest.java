package net.ripe.db.whois.api.elasticsearch;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ElasticSearchHelper;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractElasticSearchIntegrationTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElasticSearchIntegrationTest.class);
    public static final String ENV_DISABLE_TEST_CONTAINERS = "test.containers.disabled";
    private static ElasticsearchContainer elasticsearchContainer;

    @Autowired
    ElasticIndexService elasticIndexService;

    @Autowired
    ElasticSearchHelper elasticSearchHelper;

    @Autowired
    ElasticFullTextIndex elasticFullTextIndex;

    @BeforeAll
    public static void setUpElasticCluster() {
        if (StringUtils.isBlank(System.getProperty(ENV_DISABLE_TEST_CONTAINERS))) {
            if (elasticsearchContainer == null) {
                elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.0");
                elasticsearchContainer.start();
            }

            System.setProperty("elastic.host", elasticsearchContainer.getHttpHostAddress());
        } else {
            System.setProperty("elastic.host", "elasticsearch:9200");
        }
    }

    @AfterAll
    public static void resetElasticCluster() {
        System.clearProperty("elastic.host");
    }

    @BeforeEach
    public void setUpIndexes() throws Exception {
        elasticSearchHelper.setupElasticIndexes(getWhoisIndex(), getMetadataIndex());
        rebuildIndex();
    }

    @AfterEach
    public void tearDownIndexes() throws Exception {
        elasticSearchHelper.resetElasticIndexes(getWhoisIndex(), getMetadataIndex());
    }

    public void rebuildIndex() {
        try {
            this.doRebuild();
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } catch (IOException e) {
            LOGGER.info("Failed to update the ES indexes {}", e.getMessage());
        }
    }

    private void doRebuild() throws IOException {
        if (!elasticIndexService.isEnabled()) {
            LOGGER.info("Elasticsearch not enabled");
            return;
        }
        LOGGER.info("Rebuilding Elasticsearch indexes");

        elasticIndexService.deleteAll();
        final int maxSerial = JdbcRpslObjectOperations.getSerials(databaseHelper.getWhoisTemplate()).getEnd();

        // sadly Executors don't offer a bounded/blocking submit() implementation
        final int numThreads = Runtime.getRuntime().availableProcessors();
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads * 64);
        final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        JdbcStreamingHelper.executeStreaming(databaseHelper.getWhoisTemplate(), "" +
                        "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 ",
                new ResultSetExtractor<Void>() {
                    private static final int LOG_EVERY = 500000;

                    @Override
                    public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        int nrIndexed = 0;
                        while (rs.next()) {
                            executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getBytes(2)));
                            if (++nrIndexed % LOG_EVERY == 0) {
                                LOGGER.info("Indexed {} objects", nrIndexed);
                            }
                        }
                        LOGGER.info("Indexed {} objects", nrIndexed);
                        return null;
                    }
                }
        );

        executorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("shutdown", e);
        }

        elasticIndexService.updateMetadata(new ElasticIndexMetadata(maxSerial,
                sourceContext.getMasterSource().getName().toString()));
        LOGGER.info("Completed Rebuilding Elasticsearch indexes");
    }

    public void deleteAll() throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(getWhoisIndex());
        request.setQuery(QueryBuilders.matchAllQuery());

        elasticIndexService.getClient().deleteByQuery(request, RequestOptions.DEFAULT);

        DeleteByQueryRequest metadata = new DeleteByQueryRequest(getMetadataIndex());
        metadata.setQuery(QueryBuilders.matchAllQuery());

        elasticIndexService.getClient().deleteByQuery(metadata, RequestOptions.DEFAULT);
    }

    public abstract String getWhoisIndex();

    public static ElasticsearchContainer getElasticsearchContainer() {
        return elasticsearchContainer;
    }

    public ElasticIndexService getElasticIndexService() {
        return elasticIndexService;
    }

    public ElasticSearchHelper getElasticSearchHelper() {
        return elasticSearchHelper;
    }

    public ElasticFullTextIndex getElasticFullTextIndex() {
        return elasticFullTextIndex;
    }

    public abstract String getMetadataIndex();

    final class DatabaseObjectProcessor implements Runnable {
        final int objectId;
        final byte[] object;

        private DatabaseObjectProcessor(final int objectId, final byte[] object) {
            this.objectId = objectId;
            this.object = object;
        }

        @Override
        public void run() {
            final RpslObject rpslObject;
            try {
                rpslObject = RpslObject.parse(objectId, object);

            } catch (RuntimeException e) {
                LOGGER.warn("Unable to parse object with id: {}", objectId, e);
                return;
            }

            try {
                elasticIndexService.addEntry(rpslObject);
            } catch (IOException e) {
                throw new IllegalStateException("Indexing", e);
            }
        }
    }
}
