package net.ripe.db.whois.api;

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final String WHOIS_INDEX = "whois";
    private static final String METADATA_INDEX = "metadata";

    public static synchronized void setupElasticIndexes() throws Exception {

        try(final RestHighLevelClient esClient = getEsClient()) {
            if(!isElasticRunning(esClient)) {
                return;
            }

            CreateIndexRequest whoisRequest = new CreateIndexRequest(WHOIS_INDEX);
            esClient.indices().create(whoisRequest, RequestOptions.DEFAULT);

            CreateIndexRequest whoisMetaDataRequest = new CreateIndexRequest(METADATA_INDEX);
            esClient.indices().create(whoisMetaDataRequest, RequestOptions.DEFAULT);
        }
    }

    public static synchronized void resetElasticIndexes() throws Exception {
        try(final RestHighLevelClient esClient = getEsClient()) {

            if(!isElasticRunning(esClient)) {
                return;
            }

            try {
                DeleteIndexRequest whoisRequest = new DeleteIndexRequest(WHOIS_INDEX);
                esClient.indices().delete(whoisRequest, RequestOptions.DEFAULT);
            } catch (Exception ignored) {
            }

            try {
                DeleteIndexRequest metadataRequest = new DeleteIndexRequest(METADATA_INDEX);
                esClient.indices().delete(metadataRequest, RequestOptions.DEFAULT);
            } catch (Exception ignored) {
            }
        }
    }

    @NotNull
    private static RestHighLevelClient getEsClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(System.getProperty("elastic.host"), Integer.parseInt(System.getProperty("elastic.port")))));
    }

    private static boolean isElasticRunning(final RestHighLevelClient esClient) {
        try {
            return esClient.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("ElasticSearch is not running");
            return false;
        }
    }

    public static void deleteAll(final RestHighLevelClient esClient) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest(WHOIS_INDEX);
        request.setQuery(QueryBuilders.matchAllQuery());

        esClient.deleteByQuery(request, RequestOptions.DEFAULT);

        DeleteByQueryRequest metaDataRequest = new DeleteByQueryRequest(METADATA_INDEX);
        metaDataRequest.setQuery(QueryBuilders.matchAllQuery());

        esClient.deleteByQuery(metaDataRequest, RequestOptions.DEFAULT);
    }

}
