package net.ripe.db.whois.api.elasticsearch;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ElasticSearchHelper;
import net.ripe.db.whois.api.fulltextsearch.ElasticFullTextRebuild;
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

    @Autowired
    ElasticFullTextRebuild elasticFullTextRebuild;


    @BeforeAll
    public static void setUpElasticCluster() {
        if (StringUtils.isBlank(System.getProperty(ENV_DISABLE_TEST_CONTAINERS))) {
            if (elasticsearchContainer == null) {
                elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.16.3");
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
        rebuildIndex();
    }

    @AfterEach
    public void tearDownIndexes() throws Exception {
        elasticSearchHelper.resetElasticIndexes(getWhoisIndex(), getMetadataIndex());
    }

    public void rebuildIndex() {
        try {
            elasticSearchHelper.resetElasticIndexes(getWhoisIndex(), getMetadataIndex());
            elasticFullTextRebuild.rebuild(getWhoisIndex(), getMetadataIndex(), false);
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.info("Failed to update the ES indexes {}", e.getMessage());
        }
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


    public abstract String getMetadataIndex();

}
