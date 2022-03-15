package net.ripe.db.whois.api;

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    public static final String ELASTIC_HOSTNAME = System.getProperty("elastic.host");
    public static final int ELASTIC_PORT = Integer.parseInt(System.getProperty("elastic.port"));

    public static void setupElasticIndexes(final String indexName, final String metaDetaIndex) throws Exception {

        try(final RestHighLevelClient esClient = getEsClient()) {
            if(!isElasticRunning(esClient)) {
                return;
            }

            CreateIndexRequest whoisRequest = new CreateIndexRequest(indexName);
            esClient.indices().create(whoisRequest, RequestOptions.DEFAULT);

            CreateIndexRequest whoisMetaDataRequest = new CreateIndexRequest(metaDetaIndex);
            esClient.indices().create(whoisMetaDataRequest, RequestOptions.DEFAULT);
        }
    }

    public static void resetElasticIndexes(final String indexName, final String metaDetaIndex) throws Exception {
        try(final RestHighLevelClient esClient = getEsClient()) {

            if(!isElasticRunning(esClient)) {
                return;
            }

            try {
                DeleteIndexRequest whoisRequest = new DeleteIndexRequest(indexName);
                esClient.indices().delete(whoisRequest, RequestOptions.DEFAULT);
            } catch (Exception ignored) {
            }

            try {
                DeleteIndexRequest metadataRequest = new DeleteIndexRequest(metaDetaIndex);
                esClient.indices().delete(metadataRequest, RequestOptions.DEFAULT);
            } catch (Exception ignored) {
            }
        }
    }

    @NotNull
    private static RestHighLevelClient getEsClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(ELASTIC_HOSTNAME, ELASTIC_PORT)));
    }

    private static boolean isElasticRunning(final RestHighLevelClient esClient) {
        try {
            return esClient.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("ElasticSearch is not running");
            return false;
        }
    }

}
