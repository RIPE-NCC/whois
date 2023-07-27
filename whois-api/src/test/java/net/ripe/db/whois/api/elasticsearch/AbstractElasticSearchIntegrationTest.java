package net.ripe.db.whois.api.elasticsearch;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ElasticSearchHelper;
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
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
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
            elasticFullTextIndex.update();
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } catch (IOException e) {
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
}
