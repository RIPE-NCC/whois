package net.ripe.db.whois.api.elasticsearch;

import co.elastic.clients.elasticsearch._types.analysis.PatternTokenizer;
import co.elastic.clients.elasticsearch._types.mapping.DynamicTemplate;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchConfigurations {

    private final static List<AttributeSyntax> SET_TYPES = List.of(
            AttributeSyntax.AS_SET_SYNTAX,
            AttributeSyntax.FILTER_SET_SYNTAX,
            AttributeSyntax.PEERING_SET_SYNTAX,
            AttributeSyntax.ROUTE_SET_SYNTAX,
            AttributeSyntax.RTR_SET_SYNTAX
            );

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
                        ).filter("colon_sets_combinations", f -> f
                                .definition(d -> d
                                        .patternCapture(pc -> pc
                                                .preserveOriginal(true)
                                                .patterns("^(.*)$")
                                        )
                                )
                        )
                        .tokenizer("colon_tokeniser", t -> t
                                .definition(p -> p
                                        .pattern(PatternTokenizer.of(pt -> pt.pattern(":"))))
                        )
                        // Analyzers
                        .analyzer("fulltext_analyzer", az -> az
                                .custom(c -> c
                                        .tokenizer("whitespace")
                                        .filter("my_word_delimiter_graph", "lowercase", "english_stop")
                                )
                        )
                        .analyzer("my_email_analyzer", az -> az
                                .custom(c -> c
                                        .tokenizer("uax_url_email")
                                        .filter("my_word_email_delimiter_graph", "lowercase")
                                )
                        )
                        .analyzer("set_analyzer", az -> az
                                .custom(c -> c
                                    .tokenizer("colon_tokeniser")
                                    .filter("colon_sets_combinations")
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

        final Property setsFieldProperty = Property.of(p -> p
                .text(t -> t
                        .analyzer("fulltext_analyzer")
                        .searchAnalyzer("fulltext_analyzer")
                        .fields("custom", f -> f.text(ft -> ft.analyzer("set_analyzer")))
                        .fields("raw", f -> f.keyword(k -> k.ignoreAbove(10922)))
                        .fields("lowercase", f -> f.keyword(k -> k.normalizer("my_lowercase_normalizer").ignoreAbove(10922)))
                )
        );

        for(final AttributeType type : AttributeType.values()) {
            if (type.getSyntax() == AttributeSyntax.EMAIL_SYNTAX) {
                propertiesMap.put(type.getName(), emailFieldProperty);
            }
            if (SET_TYPES.contains(type.getSyntax())) {
                propertiesMap.put(type.getName(), setsFieldProperty);
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
