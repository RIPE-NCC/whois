package net.ripe.db.whois.api.elasticsearch;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Profile({WhoisProfile.TEST})
@Component
public class ElasticSearchNonAuthInstance implements ElasticRestHighlevelClient {

    private static final Logger LOGGER = getLogger(ElasticSearchNonAuthInstance.class);
    private final ElasticsearchClient client;

    @Autowired
    public ElasticSearchNonAuthInstance(@Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts) {
        this.client = getEsClient(elasticHosts);
    }

    @Nullable
    private ElasticsearchClient getEsClient(final List<String> elasticHosts) {
        try {
            final RestClientBuilder clientBuilder = RestClient.builder(elasticHosts.stream().map((host) -> HttpHost.create(host)).toArray(HttpHost[]::new));

            return new ElasticsearchClient( new RestClientTransport(
                    clientBuilder.build(),
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
}
