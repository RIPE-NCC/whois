package net.ripe.db.whois.api;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ElasticSearchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final List<String> STOP_WORDS = Lists.newArrayList(
            "a", "an", "and", "are", "as", "at",
            "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "s",
            "such", "t", "that", "the", "their", "then",
            "there", "these", "they", "this", "to", "was", "will", "with");

    private String hostname;
    private int port;

    public String getHostname() {
        return hostname;
    }

    @Value("${elastic.host:localhost}")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    @Value("${elastic.port:9200}")
    public void setPort(int port) {
        this.port = port;
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

    private static XContentBuilder getSettings() throws IOException {
        final XContentBuilder indexSettings =  XContentFactory.jsonBuilder();
        indexSettings.startObject()
                .startObject("analysis")
                     .startObject("analyzer")
                         .startObject("fulltext_analyzer")
                            .field("tokenizer", "whitespace")
                            .field("filter", new String[]{"my_word_delimiter_graph", "lowercase", "asciifolding"})
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
                            .field("stopwords", STOP_WORDS.toArray())
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

    private static XContentBuilder getMappings() throws IOException {
        final XContentBuilder mappings = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties");

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
             } else {
                 mappings.startObject(type.getName())
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

        mappings.startObject("lookup-key")
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
                .endObject();

        return mappings.endObject().endObject();
    }
}
