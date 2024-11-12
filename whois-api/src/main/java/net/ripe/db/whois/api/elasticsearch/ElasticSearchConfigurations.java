package net.ripe.db.whois.api.elasticsearch;

import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;

import java.io.IOException;

public class ElasticSearchConfigurations {

    public static XContentBuilder getSettings(final int nodes) throws IOException {

        final XContentBuilder indexSettings =  XContentFactory.jsonBuilder();
        indexSettings.startObject()
                .startObject("index")
                    .field("number_of_replicas", nodes-1)
                    .field("auto_expand_replicas", false)
                    .field("max_result_window", 100000)
                .endObject()
                .startObject("analysis")
                    .startObject("analyzer")
                        .startObject("fulltext_analyzer")
                            .field("tokenizer", "whitespace")
                            .field("filter", new String[]{"my_word_delimiter_graph", "lowercase", "asciifolding", "english_stop" })
                        .endObject()
                        .startObject("my_email_analyzer")
                            .field("type", "custom")
                            .field("tokenizer", "uax_url_email")
                            .field("filter", new String[]{"my_word_email_delimiter_graph", "lowercase"})
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
                    .startObject("my_word_email_delimiter_graph")
                        .field("type", "word_delimiter_graph")
                        .field("preserve_original", true)
                        .field("split_on_case_change", false)
                    .endObject()
                .endObject()
        .endObject().endObject();

        return indexSettings;
    }

    public static XContentBuilder getMappings() throws IOException {
        final XContentBuilder mappings = XContentFactory.jsonBuilder().startObject();

        mappings.startArray("dynamic_templates")
                    .startObject()
                        .startObject("default_mapping")
                            .field("match_mapping_type", "string")
                            .startObject("mapping")
                                .field("type", "text")
                            .startObject("fields")
                                    .startObject("custom")
                                        .field("type", "text")
                                        .field("analyzer", "fulltext_analyzer")
                                        .field("search_analyzer", "fulltext_analyzer")
                                    .endObject()
                                    .startObject("raw")
                                        .field("type", "keyword")
                                        .field("ignore_above", 10922)
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                .endArray();

        mappings.startObject("properties");
        for(AttributeType type : AttributeType.values()) {
            if(type.getSyntax() == AttributeSyntax.EMAIL_SYNTAX) {
                mappings.startObject(type.getName())
                            .field("type", "text")
                            .field("analyzer", "fulltext_analyzer")
                            .field("search_analyzer", "fulltext_analyzer")
                            .startObject("fields")
                                 .startObject("custom")
                                    .field("type", "text")
                                    .field("analyzer", "my_email_analyzer")
                                 .endObject()
                                 .startObject("raw")
                                    .field("type", "keyword")
                                    .field("ignore_above", 10922)
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
