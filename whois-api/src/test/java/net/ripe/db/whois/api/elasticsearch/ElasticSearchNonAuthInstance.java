package net.ripe.db.whois.api.elasticsearch;


import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
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
    private final RestHighLevelClient client;

    @Autowired
    public ElasticSearchNonAuthInstance(@Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts) {
        this.client = getEsClient(elasticHosts);
    }

    @Nullable
    private RestHighLevelClient getEsClient(final List<String> elasticHosts) {
        try {
            final RestClientBuilder clientBuilder = RestClient.builder(elasticHosts.stream().map((host) -> HttpHost.create(host)).toArray(HttpHost[]::new));
            return new RestHighLevelClient(clientBuilder);
        } catch (Exception e) {
            LOGGER.warn("Failed to start the ES client {}", e.getMessage());
            return null;
        }
    }

    @Override
    public RestHighLevelClient getClient() {
        return client;
    }
}
