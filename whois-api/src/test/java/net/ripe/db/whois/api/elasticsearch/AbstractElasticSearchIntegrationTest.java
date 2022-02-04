package net.ripe.db.whois.api.elasticsearch;

import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.jetbrains.annotations.NotNull;

public class AbstractElasticSearchIntegrationTest {

    public static final String CONTAINER_HOST = "elasticsearch";
    public static final Integer CONTAINER_PORT = 9200;

    @NotNull
    protected ElasticIndexService getElasticIndexService(final String whoisIndex, final String metadataIndex) {
        return new ElasticIndexService(CONTAINER_HOST, CONTAINER_PORT, whoisIndex, metadataIndex);
    }

    protected static RestHighLevelClient testClient() {
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(CONTAINER_HOST, CONTAINER_PORT));
        return new RestHighLevelClient(clientBuilder);
    }
}
