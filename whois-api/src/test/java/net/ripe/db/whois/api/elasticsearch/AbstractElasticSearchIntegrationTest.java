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
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractElasticSearchIntegrationTest extends AbstractIntegrationTest {

    protected static final String WHOIS_INDEX = "whois";
    protected static final String METADATA_INDEX = "metadata";

    public static final String ENV_DISABLE_TEST_CONTAIENRS = "test.containers.disabled";
    private static ElasticsearchContainer elasticsearchContainer;

    @Autowired
    ElasticIndexService elasticIndexService;

    @Autowired
    ElasticFullTextIndex elasticFullTextIndex;

    @BeforeAll
    public static synchronized void setUpElasticCluster() throws Exception {

       if(StringUtils.isBlank(System.getProperty(ENV_DISABLE_TEST_CONTAIENRS))) {

           if(elasticsearchContainer == null) {
               elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.0");
               elasticsearchContainer.start();
           }

           System.setProperty("elastic.host", elasticsearchContainer.getHttpHostAddress().split(":")[0]);
           System.setProperty("elastic.port", elasticsearchContainer.getHttpHostAddress().split(":")[1]);

       } else {
            System.setProperty("elastic.host", "elasticsearch");
            System.setProperty("elastic.port", "9200");
        }

        System.setProperty("elasticsearch.enabled", "true");
        ElasticSearchHelper.setupElasticIndexes();
    }

    @AfterAll
    public synchronized static void resetElasticCluster() throws Exception {
        ElasticSearchHelper.resetElasticIndexes();
        System.clearProperty("elastic.host");
        System.clearProperty("elastic.port");
        System.clearProperty("elasticsearch.enabled");
    }

    @AfterEach
    public void tearDownIndexes() throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(WHOIS_INDEX);
        request.setQuery(QueryBuilders.matchAllQuery());

        elasticIndexService.getClient().deleteByQuery(request, RequestOptions.DEFAULT);

        DeleteByQueryRequest metadata = new DeleteByQueryRequest(METADATA_INDEX);
        metadata.setQuery(QueryBuilders.matchAllQuery());

        elasticIndexService.getClient().deleteByQuery(metadata, RequestOptions.DEFAULT);
    }

    public void rebuildIndex(){
        try {
            elasticFullTextIndex.update();
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
