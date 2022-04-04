package net.ripe.db.whois.api.elasticsearch;

import com.google.common.util.concurrent.Uninterruptibles;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.ElasticSearchHelper;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractElasticSearchIntegrationTest extends AbstractIntegrationTest {

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

        System.setProperty("elasticsearch.enabled", "true");
    }

    @AfterAll
    public static void resetElasticCluster() {
        System.clearProperty("elastic.host");
        System.clearProperty("elasticsearch.enabled");
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
            e.printStackTrace();
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
