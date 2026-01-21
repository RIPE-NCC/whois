package net.ripe.db.whois.api.autocomplete;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SearchType;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.LOOKUP_KEY_FIELD_NAME;
import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.OBJECT_TYPE_FIELD_NAME;
import static net.ripe.db.whois.api.fulltextsearch.ElasticFulltextSearch.SORT_BUILDERS;

@Component
public class ElasticAutocompleteSearch implements AutocompleteSearch {

    private static final int MAX_SEARCH_RESULTS = 10;
    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");
    private final ElasticIndexService elasticIndexService;

    @Autowired
    public ElasticAutocompleteSearch(final ElasticIndexService elasticIndexService) {
        this.elasticIndexService = elasticIndexService;
    }

    public List<Map<String, Object>> search(
            final String queryString,
            final Set<AttributeType> queryAttributes,
            final Set<AttributeType> responseAttributes,
            final Set<ObjectType> objectTypes
    ) throws IOException {

        final MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                .query(queryString)
                .fields(queryAttributes.stream()
                        .map(AttributeType::getName)
                        .toList()
                )
                .type(TextQueryType.PhrasePrefix)  // PHRASE_PREFIX
        );

        final BoolQuery boolQuery = BoolQuery.of(b -> {
            BoolQuery.Builder builder = new BoolQuery.Builder();

            if (!objectTypes.isEmpty()) {
                builder.must(TermsQuery.of(t -> t
                        .field("object-type")
                        .terms(terms -> terms
                                .value(objectTypes.stream().map(o -> FieldValue.of( f -> f.stringValue(o.getName()))).toList())
                        )
                )._toQuery());
            }

            builder.must(multiMatchQuery._toQuery());

            return builder;
        });

        List<Hit<Map>> searchResponse = elasticIndexService.getClient().search(s -> s
                        .index(elasticIndexService.getWhoisAliasIndex())
                        .query(boolQuery._toQuery())
                        .size(MAX_SEARCH_RESULTS)
                        .searchType(SearchType.DfsQueryThenFetch)
                        .sort(SORT_BUILDERS)
                , Map.class  // return type
        ).hits().hits();


       final List<Map<String, Object>> results = new ArrayList<>();

        for (final Hit<Map> hit : searchResponse) {
            Map<String, Object> attributes = hit.source();

            if (attributes == null) continue;

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("key", attributes.get(LOOKUP_KEY_FIELD_NAME));
            result.put("type", attributes.get(OBJECT_TYPE_FIELD_NAME));

            for (AttributeType responseAttribute : responseAttributes) {
                if (attributes.containsKey(responseAttribute.getName())) {
                    Object attributeValue = attributes.get(responseAttribute.getName());

                    if (attributeValue instanceof List) {
                        result.put(responseAttribute.getName(),
                                filterValues(responseAttribute, (List<String>) attributeValue));
                    } else if (attributeValue instanceof String) {
                        result.put(responseAttribute.getName(),
                                filterValue(responseAttribute, (String) attributeValue));
                    }
                }
            }

            results.add(result);
        }

        return results;
    }

    @Nullable
    private String filterValue(final AttributeType type, final String attributeValue) {
        return attributeValue == null ? null : COMMENT_PATTERN.matcher( elasticIndexService.filterRpslAttribute(type, attributeValue) ).replaceFirst("").trim();
    }

    private List<String> filterValues(final AttributeType attributeType, final List<String> attributeValues) {
        return attributeValues.stream().map( (attributeValue) -> filterValue(attributeType, attributeValue)).collect(Collectors.toList());
    }
}
