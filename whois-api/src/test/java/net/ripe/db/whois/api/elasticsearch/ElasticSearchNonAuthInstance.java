package net.ripe.db.whois.api.elasticsearch;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Profile({WhoisProfile.TEST})
@Component
public class ElasticSearchNonAuthInstance implements ElasticRestHighlevelClient {

    private static final Logger LOGGER = getLogger(ElasticSearchNonAuthInstance.class);
    private final ElasticsearchClient client;
    private final RestClient restClient;


    @Autowired
    public ElasticSearchNonAuthInstance(@Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts) {
        this.restClient = getRestClient(elasticHosts);
        this.client = getEsClient(restClient);
    }

    @Nullable
    private static RestClient getRestClient(List<String> elasticHosts) {
        try {
            return RestClient.builder(elasticHosts.stream().map(HttpHost::create).toArray(HttpHost[]::new)).build();
        } catch (Exception e) {
            LOGGER.warn("Failed to start the ES client {}", e.getMessage());
            return null;
        }
    }

    @Nullable
    private ElasticsearchClient getEsClient(final RestClient restClient) {
        try {

            return new ElasticsearchClient( new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper()
            ));
        } catch (Exception e) {
            LOGGER.warn("Failed to start the ES client {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ElasticsearchClient getClient() {
        return client;
    }

    @Override
    public void close() {
        try {
            this.restClient.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close the ES client: {}", e.getMessage());
        }
    }
}
