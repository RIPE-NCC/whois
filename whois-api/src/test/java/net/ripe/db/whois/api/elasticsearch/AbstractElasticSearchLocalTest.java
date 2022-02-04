package net.ripe.db.whois.api.elasticsearch;

import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class AbstractElasticSearchLocalTest {

    @Container
    private static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.15.0");

    @NotNull
    protected ElasticIndexService getElasticIndexService(final String whoisIndex, final String metadataIndex) {
        return new ElasticIndexService(elasticsearchContainer.getHttpHostAddress().split(":")[0], Integer.parseInt(elasticsearchContainer.getHttpHostAddress().split(":")[1]), whoisIndex, metadataIndex);
    }

    public static RestHighLevelClient testClient() {
        RestClientBuilder clientBuilder = RestClient.builder(HttpHost.create(elasticsearchContainer.getHttpHostAddress()));
        return new RestHighLevelClient(clientBuilder);
    }

}
