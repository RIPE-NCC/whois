package net.ripe.db.whois.api;

import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticSearchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private String hostname;

    @Value("${elastic.host:elasticsearch:9200}")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setupElasticIndexes(final String indexName, final String metaDetaIndex) throws Exception {

        try (final RestHighLevelClient esClient = getEsClient()) {
            if (!isElasticRunning(esClient)) {
                return;
            }

            CreateIndexRequest whoisRequest = new CreateIndexRequest(indexName);
            whoisRequest.settings(getSettings());
            whoisRequest.mapping(getMappings());

            esClient.indices().create(whoisRequest, RequestOptions.DEFAULT);

            CreateIndexRequest whoisMetaDataRequest = new CreateIndexRequest(metaDetaIndex);
            esClient.indices().create(whoisMetaDataRequest, RequestOptions.DEFAULT);
        }
    }

    public void resetElasticIndexes(final String indexName, final String metaDetaIndex) throws Exception {
        try (final RestHighLevelClient esClient = getEsClient()) {

            if (!isElasticRunning(esClient)) {
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
    private RestHighLevelClient getEsClient() {
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(hostname)));
    }

    private boolean isElasticRunning(final RestHighLevelClient esClient) {
        try {
            return esClient.ping(RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.warn("ElasticSearch is not running");
            return false;
        }
    }

    private XContentBuilder getSettings() throws IOException {

        final XContentBuilder indexSettings =  XContentFactory.jsonBuilder();
        indexSettings.startObject()
                .startObject("analysis")
                     .startObject("analyzer")
                         .startObject("fulltext_analyzer")
                            .field("tokenizer", "whitespace")
                            .field("filter", new String[]{"my_word_delimiter_graph", "lowercase", "asciifolding", "english_stop" })
                         .endObject()
                        .startObject("my_email_analyzer")
                            .field("type", "custom")
                            .field("tokenizer", "uax_url_email")
                            .field("filter", new String[]{"stop", "lowercase"})
                        .endObject()
                     .endObject()
                    .startObject("filter")
                        .startObject("english_stop")
                            .field("type", "stop")
                            .field("stopwords", "_english_")
                        .endObject()
                        .startObject("my_word_delimiter_graph")
                            .field("type", "word_delimiter_graph")
                            .field("generate_word_parts", true)
                            .field("catenate_words", true)
                            .field("catenate_numbers", true)
                            .field("preserve_original", true)
                            .field("split_on_case_change", true)
                        .endObject()
                    .endObject()
                .endObject().endObject();

        return indexSettings;
    }

    private XContentBuilder getMappings() throws IOException {
        final XContentBuilder mappings = XContentFactory.jsonBuilder().startObject();

         mappings.startArray("dynamic_templates").startObject()
                    .startObject("default_mapping")
                       .field("match_mapping_type", "string")
                       .startObject("mapping")
                         .field("type", "text")
                         .startObject("fields")
                             .startObject("custom")
                                 .field("type", "text")
                                 .field("analyzer", "fulltext_analyzer")
                                 .field("search_analyzer", "standard")
                             .endObject()
                             .startObject("raw")
                                .field("type", "keyword")
                             .endObject()
                         .endObject()
                      .endObject()
                    .endObject().endObject()
                 .endArray();

         mappings.startObject("properties");
         for(AttributeType type : AttributeType.values()) {
             if(type.getSyntax() == AttributeSyntax.EMAIL_SYNTAX) {
                 mappings.startObject(type.getName())
                             .field("type", "text")
                             .startObject("fields")
                                 .startObject("custom")
                                    .field("type", "text")
                                    .field("analyzer", "my_email_analyzer")
                                 .endObject()
                             .endObject()
                         .endObject();
             }
         }

        mappings.startObject("object-type")
                    .field("type", "text")
                    .startObject("fields")
                        .startObject("raw")
                             .field("type", "keyword")
                        .endObject()
                    .endObject()
                .endObject();

        return mappings.endObject().endObject();
    }
}
