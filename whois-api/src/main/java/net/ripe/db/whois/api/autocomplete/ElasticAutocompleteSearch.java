package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
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
public class ElasticAutocompleteSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticAutocompleteSearch.class);
    private static final int MAX_SEARCH_RESULTS = 10;
    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");

    private final ElasticIndexService elasticIndexService;
    private final RpslObjectDao objectDao;


    @Autowired
    public ElasticAutocompleteSearch(final ElasticIndexService elasticIndexService, @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao) {
        this.elasticIndexService = elasticIndexService;
        this.objectDao = rpslObjectDao;
    }

    public List<Map<String, Object>> search(
        final String queryString,                       // search value
        final Set<AttributeType> queryAttributes,       // attribute(s) to search in
        final Set<AttributeType> responseAttributes,    // attribute(s) to return
        final Set<ObjectType> objectTypes)              // filter by object type(s)
            throws IOException {                        // TODO: wrap IOException, return something sensible


        final MultiMatchQueryBuilder multiMatchQuery = new MultiMatchQueryBuilder(queryString,
                                                        queryAttributes.stream().map((atrributeType) -> atrributeType.getName()).toArray(String[]::new));
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
        sourceBuilder.sort(Arrays.asList(SortBuilders.scoreSort(), SortBuilders.fieldSort("primary-key.keyword")));

        final SearchRequest searchRequest = new SearchRequest(elasticIndexService.getWHOIS_INDEX());
        searchRequest.source(sourceBuilder);

        final SearchResponse searchResponse = elasticIndexService.getClient().search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();

        final List<Map<String, Object>> results = Lists.newArrayList();

        for(SearchHit hit: hits) {
            final Map<String, Object>  attributes = hit.getSourceAsMap();
            final RpslObject rpslObject;

            try {
                rpslObject = objectDao.getByKey(
                        ObjectType.getByName(attributes.get("object-type").toString()),
                        attributes.get("primary-key").toString()
                );

            } catch (EmptyResultDataAccessException ex) {
                LOGGER.info("seems like object has been deleted from database");
                continue;
            }

            final Map<String, Object> result = Maps.newLinkedHashMap();
            result.put("key", rpslObject.getKey());
            result.put("type", rpslObject.getType().getName());

            for (final AttributeType responseAttribute : responseAttributes) {
                final ObjectTemplate template = ObjectTemplate.getTemplate(rpslObject.getType());

                if (template.getMultipleAttributes().contains(responseAttribute)) {
                    result.put(responseAttribute.getName(), filterValues(responseAttribute, rpslObject));
                } else {
                    result.put(responseAttribute.getName(), filterValue(responseAttribute, rpslObject.containsAttribute(responseAttribute) ? rpslObject.findAttribute(responseAttribute).getValue() : null));
                }
            }
            results.add(result);
        }

        return results;
    }

    //TODO: check for Auth filter
    @Nullable
    private String filterValue(final AttributeType type, final String attributeValue) {
        return attributeValue == null ? null : COMMENT_PATTERN.matcher( elasticIndexService.filterRpslAttribute(type, attributeValue) ).replaceFirst("").trim();
    }

    private List<String> filterValues(final AttributeType attributeType, final RpslObject rpslObject) {
        return rpslObject.findAttributes(attributeType).stream().map( (attribute) -> filterValue(attributeType, attribute.getValue())).collect(Collectors.toList());
    }
}
