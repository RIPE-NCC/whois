package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Conditional(ElasticSearchCondition.class)
public class ElasticAutocompleteSearch implements AutocompleteSearch {

    private static final int MAX_SEARCH_RESULTS = 10;
    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");
    public static final List<SortBuilder<?>> SORT_BUILDERS = Arrays.asList(SortBuilders.scoreSort(), SortBuilders.fieldSort("lookup-key.raw").unmappedType("string"));

    private final ElasticIndexService elasticIndexService;

    @Autowired
    public ElasticAutocompleteSearch(final ElasticIndexService elasticIndexService) {
        this.elasticIndexService = elasticIndexService;
    }

    public List<Map<String, Object>> search(
        final String queryString,                       // search value
        final Set<AttributeType> queryAttributes,       // attribute(s) to search in
        final Set<AttributeType> responseAttributes,    // attribute(s) to return
        final Set<ObjectType> objectTypes)              // filter by object type(s)
            throws IOException {                        // TODO: wrap IOException, return something sensible


        final MultiMatchQueryBuilder multiMatchQuery = new MultiMatchQueryBuilder(queryString, queryAttributes.stream().map((atrributeType) -> atrributeType.getName()).toArray(String[]::new));
        multiMatchQuery.type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);

        final BoolQueryBuilder finalQuery = QueryBuilders.boolQuery();

        if(!objectTypes.isEmpty()) {
            final TermsQueryBuilder  matchByObjectTypeQuery = new TermsQueryBuilder("object-type", objectTypes.stream().map((objectType) -> objectType.getName()).toArray(String[]::new));
            finalQuery.must(matchByObjectTypeQuery);
        }

        finalQuery.must(multiMatchQuery);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(finalQuery);
        sourceBuilder.size(MAX_SEARCH_RESULTS);
        sourceBuilder.sort(SORT_BUILDERS);

        final SearchRequest searchRequest = new SearchRequest(elasticIndexService.getWHOIS_INDEX());
        searchRequest.source(sourceBuilder);
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);

        final SearchResponse searchResponse = elasticIndexService.getClient().search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();

        final List<Map<String, Object>> results = Lists.newArrayList();

        for(SearchHit hit: hits) {
            final Map<String, Object>  attributes = hit.getSourceAsMap();

            final Map<String, Object> result = Maps.newLinkedHashMap();
            result.put("key", attributes.get("lookup-key"));
            result.put("type", attributes.get("object-type"));

            for (final AttributeType responseAttribute : responseAttributes) {

                if(attributes.containsKey(responseAttribute.getName())) {
                    final Object attributeValue = attributes.get(responseAttribute.getName());

                    if (attributeValue instanceof List) {
                        result.put(responseAttribute.getName(), filterValues(responseAttribute, (List<String>) attributeValue));
                    } else {
                        result.put(responseAttribute.getName(), filterValue(responseAttribute, (String) attributeValue));
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
