package net.ripe.db.whois.api.elasticsearch;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Profile({WhoisProfile.DEPLOYED})
@Primary
@Component
public class ElasticSearchInstance implements ElasticRestHighlevelClient {

    private static final int SOCKET_TIMEOUT_IN_MS = 35000;
    private static final int CONNECTION_TIMEOUT_IN_MS = 10000;
    private static final Logger LOGGER = getLogger(ElasticSearchInstance.class);
    private final ElasticsearchClient client;
    private final RestClient restClient;

    @Autowired
    public ElasticSearchInstance(@Value("#{'${elastic.host:}'.split(',')}") final List<String> elasticHosts,
                                 @Value("${elastic.user:}") final String elasticUser,
                                 @Value("${elastic.password:}")  final String elasticPassword ) {
      this.restClient = getRestClient(elasticHosts, elasticUser, elasticPassword);
      this.client = getEsClient(restClient);
    }

    @Nullable
    public static ElasticsearchClient getEsClient(final RestClient restClient) {
        try {

            // Transport layer for the new Java client
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient,
                    new JacksonJsonpMapper()
            );

            // Return the high-level typed client
            return new ElasticsearchClient(transport);

        } catch (Exception e) {
            LOGGER.warn("Failed to start the ES client: {}", e.getMessage());
            return null;
        }
    }

    private static @NonNull RestClient getRestClient( final List<String> elasticHosts,
                                                      final String elasticUser,
                                                      final String elasticPassword) {
        // Credentials for Basic Auth
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(elasticUser, elasticPassword)
        );

        // Build low-level RestClient for multiple hosts
        RestClientBuilder builder = RestClient.builder(asHttpHosts(elasticHosts))
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setSocketTimeout(SOCKET_TIMEOUT_IN_MS)
                                .setConnectTimeout(CONNECTION_TIMEOUT_IN_MS)
                )
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        RestClient restClient = builder.build();
        return restClient;
    }

    private static HttpHost[] asHttpHosts(final List<String> hosts) {
        return hosts.stream()
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
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
