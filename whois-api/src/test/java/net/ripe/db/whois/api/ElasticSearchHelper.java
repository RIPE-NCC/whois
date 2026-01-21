package net.ripe.db.whois.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import net.ripe.db.whois.api.elasticsearch.ElasticSearchConfigurations;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class ElasticSearchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private String hostname;

    @Value("${elastic.host:elasticsearch:9200}")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setupElasticIndexes(final String indexName, final String metadataIndex) throws Exception {
        try (final ElasticsearchClient esClient = getEsClient()) {

            if (!isElasticRunning(esClient)) {
                return;
            }

            // Create whois index
            esClient.indices().create(c -> c
                    .index(indexName)
                    .settings(ElasticSearchConfigurations.getSettings(hostname.split(",").length))
                    .mappings(ElasticSearchConfigurations.getMappings())
            );

            // Create metadata index
            esClient.indices().create(c -> c
                            .index(metadataIndex)
                    // optionally add settings/mappings if needed
            );
        }
    }

    public void resetElasticIndexes(final String indexName, final String metadataIndex) throws Exception {
        try (final ElasticsearchClient esClient = getEsClient()) {

            if (!isElasticRunning(esClient)) {
                return;
            }

            // Delete whois index if it exists
            try {
                esClient.indices().delete(d -> d.index(indexName));
            } catch (Exception ignored) {
            }

            // Delete metadata index if it exists
            try {
                esClient.indices().delete(d -> d.index(metadataIndex));
            } catch (Exception ignored) {
            }
        }
    }

    @Nonnull
    private ElasticsearchClient getEsClient() {
        return new ElasticsearchClient( new RestClientTransport(
                RestClient.builder(HttpHost.create(hostname)).build(),
                new JacksonJsonpMapper()
        ));

    }

    private boolean isElasticRunning(final ElasticsearchClient esClient) {
        try {
            return esClient.ping().value();
        } catch (Exception e) {
            LOGGER.warn("ElasticSearch is not running");
            return false;
        }
    }
}
