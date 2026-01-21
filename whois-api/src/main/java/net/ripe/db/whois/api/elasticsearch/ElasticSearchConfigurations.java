package net.ripe.db.whois.api.elasticsearch;

import co.elastic.clients.elasticsearch._types.analysis.Analyzer;
import co.elastic.clients.elasticsearch._types.analysis.Normalizer;
import co.elastic.clients.elasticsearch._types.analysis.TokenFilter;
import co.elastic.clients.elasticsearch._types.mapping.DynamicTemplate;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.NamedValue;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchConfigurations {

    public static IndexSettings getSettings(final int nodes) {

        final IndexSettings settings = IndexSettings.of(s -> s
                .numberOfReplicas(String.valueOf(nodes - 1))
                .autoExpandReplicas("false")
                .maxResultWindow(100000)
                .analysis(a -> a
                        // Token Filters
                        .filter("english_stop", f -> f
                                .definition(d -> d
                                        .stop(st -> st.stopwords("_english_"))
                                )
                        )
                        .filter("my_word_delimiter_graph", f -> f
                                .definition(d -> d
                                        .wordDelimiterGraph(wdg -> wdg
                                                .generateWordParts(true)
                                                .catenateWords(true)
                                                .catenateNumbers(true)
                                                .preserveOriginal(true)
                                                .splitOnCaseChange(true)
                                        )
                                )
                        )
                        .filter("my_word_email_delimiter_graph", f -> f
                                .definition(d -> d
                                        .wordDelimiterGraph(wdg -> wdg
                                                .preserveOriginal(true)
                                                .splitOnCaseChange(false)
                                        )
                                )
                        )
                        // Analyzers
                        .analyzer("fulltext_analyzer", az -> az
                                .custom(c -> c
                                        .tokenizer("whitespace")
                                        .filter("my_word_delimiter_graph", "lowercase", "asciifolding", "english_stop")
                                )
                        )
                        .analyzer("my_email_analyzer", az -> az
                                .custom(c -> c
                                        .tokenizer("uax_url_email")
                                        .filter("my_word_email_delimiter_graph", "lowercase")
                                )
                        )
                        // Normalizers
                        .normalizer("my_lowercase_normalizer", n -> n
                                .custom(c -> c
                                        .filter("lowercase")
                                )
                        )
                )
        );

        return settings;
    }

    public static TypeMapping getMappings()  {

       final Map<String, Property> propertiesMap = new HashMap<>();

       final Property emailFieldProperty = Property.of(p -> p
                .text(t -> t
                        .analyzer("fulltext_analyzer")
                        .searchAnalyzer("fulltext_analyzer")
                        .fields("custom", f -> f.text(ft -> ft.analyzer("my_email_analyzer")))
                        .fields("raw", f -> f.keyword(k -> k.ignoreAbove(10922)))
                        .fields("lowercase", f -> f.keyword(k -> k.normalizer("my_lowercase_normalizer").ignoreAbove(10922)))
                )
        );

        for(final AttributeType type : AttributeType.values()) {
            if (type.getSyntax() == AttributeSyntax.EMAIL_SYNTAX) {
                propertiesMap.put(type.getName(), emailFieldProperty);
            }
        }

      final TypeMapping mapping = TypeMapping.of(m -> m
                .dynamicTemplates(Collections.singletonList(
                        Map.of("default_mapping", DynamicTemplate.of(dt -> dt
                                .matchMappingType("string")
                                .mapping(p -> p
                                        .text(t -> t
                                                .fields("custom", f -> f.text(ft -> ft.analyzer("fulltext_analyzer").searchAnalyzer("fulltext_analyzer")))
                                                .fields("raw", f -> f.keyword(k -> k.ignoreAbove(10922)))
                                                .fields("lowercase", f -> f.keyword(k -> k.normalizer("my_lowercase_normalizer").ignoreAbove(10922)))
                                        )
                                )
                        ))
                ))

                .properties(propertiesMap)
                .properties("object-type", p -> p
                        .text(t -> t
                                .fields("raw", f -> f.keyword(k -> k))
                        )
                )
        );

        return mapping;
    }
}
