package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.api.elasticsearch.ElasticSearchAccountingCallback;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.LOOKUP_KEY_FIELD_NAME;
import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.OBJECT_TYPE_FIELD_NAME;
import static net.ripe.db.whois.api.elasticsearch.ElasticIndexService.PRIMARY_KEY_FIELD_NAME;

@Component
public class ElasticFulltextSearch extends FulltextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFulltextSearch.class);

    private static final Integer GRACEFUL_TIMEOUT_IN_MS = 30000; // 30seconds

    public static final TermsAggregationBuilder AGGREGATION_BUILDER = AggregationBuilders
            .terms("types-count")
            .field("object-type.raw")
            .size(ObjectType.values().length);

    public static final List<SortBuilder<?>> SORT_BUILDERS = List.of(
            SortBuilders.fieldSort("lookup-key.raw")
                    .unmappedType("keyword")
                    .order(SortOrder.ASC)
    );

    private final AccessControlListManager accessControlListManager;
    private final SsoTokenTranslator ssoTokenTranslator;

    private final ElasticIndexService elasticIndexService;

    private final Source source;

    //Truncate after 100k of characters
    private static final int HIGHLIGHT_OFFSET_SIZE = 100000;

    private static final int MAX_ROW_LIMIT_SIZE = 100000;
    private final int maxResultSize;

    @Autowired
    public ElasticFulltextSearch(final ElasticIndexService elasticIndexService,
                                 final AccessControlListManager accessControlListManager,
                                 final SsoTokenTranslator ssoTokenTranslator,
                                 final SourceContext sourceContext,
                                 final ApplicationVersion applicationVersion,
                                 @Value("${fulltext.search.max.results:10000}") final int maxResultSize) {
        super(applicationVersion);

        this.accessControlListManager = accessControlListManager;
        this.source = sourceContext.getCurrentSource();
        this.maxResultSize = maxResultSize;
        this.elasticIndexService = elasticIndexService;
        this.ssoTokenTranslator = ssoTokenTranslator;
    }

    @Override
    public SearchResponse performSearch(final SearchRequest searchRequest, final String ssoToken, final String remoteAddr) throws IOException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (searchRequest.getRows() > maxResultSize) {
            throw new IllegalArgumentException("Too many results requested, the maximum allowed is " + maxResultSize);
        }

        if (searchRequest.getStart() + searchRequest.getRows() > MAX_ROW_LIMIT_SIZE) {
            throw new IllegalArgumentException("Exceeded maximum " + MAX_ROW_LIMIT_SIZE + " documents");
        }

        final UserSession userSession = ssoTokenTranslator.translateSsoTokenOrNull(ssoToken);

        return new ElasticSearchAccountingCallback<SearchResponse>(accessControlListManager, remoteAddr, userSession, source) {

            @Override
            protected SearchResponse doSearch() throws IOException {

                final org.elasticsearch.action.search.SearchResponse fulltextResponse = performFulltextSearch(searchRequest);

                final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();
                final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();

                for (final SearchHit hit : fulltextResponse.getHits().getHits()) {
                    final Map<String, Object> hitAttributes = hit.getSourceAsMap();
                    final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
                    final List<SearchResponse.Str> responseStrs = Lists.newArrayList();
                    final List<RpslAttribute> attributes = Lists.newArrayList();
                    highlightDocs.add(createHighlights(hit));

                    final ObjectType objectType = ObjectType.getByName(hitAttributes.get(OBJECT_TYPE_FIELD_NAME).toString());
                    final String pKey = hitAttributes.get(LOOKUP_KEY_FIELD_NAME).toString();

                    responseStrs.add(new SearchResponse.Str(PRIMARY_KEY_FIELD_NAME, hit.getId()));
                    responseStrs.add(new SearchResponse.Str(OBJECT_TYPE_FIELD_NAME, objectType.getName()));
                    responseStrs.add(new SearchResponse.Str(LOOKUP_KEY_FIELD_NAME, pKey));

                    final Set<AttributeType> templateAttributes = ObjectTemplate.getTemplate(objectType).getAllAttributes();

                    for (final AttributeType attributeType : templateAttributes) {
                        if (hitAttributes.containsKey(attributeType.getName())){
                            filterRpslAttributes(attributeType.getName(), hitAttributes.get(attributeType.getName())).forEach((rpslAttribute) -> {
                                attributes.add(rpslAttribute);
                                responseStrs.add(new SearchResponse.Str(rpslAttribute.getKey(), rpslAttribute.getValue()));
                            });
                        }
                    }
                    account(new RpslObject(attributes));

                    resultDocument.setStrs(responseStrs);
                    resultDocumentList.add(resultDocument);
                }

                return prepareResponse(fulltextResponse, highlightDocs, resultDocumentList, searchRequest, stopwatch);
            }

        }.search();
    }

    private org.elasticsearch.action.search.SearchResponse performFulltextSearch(final SearchRequest searchRequest) throws IOException {
        try {
            return elasticIndexService.getClient().search(getFulltextRequest(searchRequest), RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException ex){
            if (ex.status().equals(RestStatus.BAD_REQUEST)){
                LOGGER.info("ElasticFullTextSearch fails due to the query: {}", ex.getMessage());
                throw new IllegalArgumentException("Invalid query syntax");
            }
            LOGGER.error("ElasticFullTextSearch error: {}", ex.getMessage());
            throw ex;
        }
    }

    private org.elasticsearch.action.search.SearchRequest getFulltextRequest(final SearchRequest searchRequest ) {
        final int start = Math.max(0, searchRequest.getStart());
        final HighlightBuilder highlightBuilder = getHighlightBuilder(searchRequest);
        final SearchSourceBuilder sourceBuilder = getSourceBuilder(start, highlightBuilder, searchRequest);


        final org.elasticsearch.action.search.SearchRequest fulltextRequest = new org.elasticsearch.action.search.SearchRequest(elasticIndexService.getWhoisAliasIndex());
        fulltextRequest.source(sourceBuilder);
        return fulltextRequest;
    }

    private SearchSourceBuilder getSourceBuilder(int start, HighlightBuilder highlightBuilder, SearchRequest searchRequest) {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(getQueryBuilder(searchRequest.getQuery()))
                .size(searchRequest.getRows()).from(start)
                .timeout(TimeValue.timeValueMillis(GRACEFUL_TIMEOUT_IN_MS))
                .aggregation(AGGREGATION_BUILDER)
                .sort(SORT_BUILDERS)
                .highlighter(highlightBuilder).trackTotalHits(true);
        return sourceBuilder;
    }

    private HighlightBuilder getHighlightBuilder(SearchRequest searchRequest) {
        final HighlightBuilder highlightBuilder = new HighlightBuilder()
                .postTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPost()))
                .preTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPre()))
                .maxAnalyzedOffset(HIGHLIGHT_OFFSET_SIZE)
                .field("*");
        return highlightBuilder;
    }

    private SearchResponse prepareResponse(org.elasticsearch.action.search.SearchResponse fulltextResponse, List<SearchResponse.Lst> highlightDocs, List<SearchResponse.Result.Doc> resultDocumentList, SearchRequest searchRequest, Stopwatch stopwatch) {
        final SearchResponse.Result result = new SearchResponse.Result("response", Long.valueOf(fulltextResponse.getHits().getTotalHits().value).intValue(), searchRequest.getStart());
        result.setDocs(resultDocumentList);

        final SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(result);

        final List<SearchResponse.Lst> responseLstList = Lists.newArrayList();
        responseLstList.add(getResponseHeader(searchRequest, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

        if (searchRequest.isHighlight()) {
            final SearchResponse.Lst highlight = new SearchResponse.Lst("highlighting");
            highlight.setLsts(highlightDocs);
            responseLstList.add(highlight);
        }

        if (searchRequest.isFacet()) {
            final Terms countByType = fulltextResponse.getAggregations().get("types-count");
            responseLstList.add(getCountByType(countByType));
        }

        responseLstList.add(createVersion());
        searchResponse.setLsts(responseLstList);
        return searchResponse;
    }

    private List<RpslAttribute> filterRpslAttributes(final String attributeKey, final Object attributeValue) {
        if (attributeValue == null){
            return Collections.emptyList();
        }
        final AttributeType type = AttributeType.getByName(attributeKey);
        if (ElasticIndexService.SKIPPED_ATTRIBUTES.contains(type)) {
          return Collections.emptyList();
        }

        if (attributeValue instanceof List){
            return filterValues(type, (List<String>) attributeValue);
        } else {
            return filterValue(type, (String) attributeValue) == null ? Collections.emptyList() :
                    List.of(new RpslAttribute(type, filterValue(type, (String) attributeValue)));
        }

    }

    private String getHighlightTag(final String format, final String highlightPost) {
        return SearchRequest.XML_FORMAT.equals(format) ? escape(highlightPost) : highlightPost;
    }

    private QueryStringQueryBuilder getQueryBuilder(final String query) {
        return QueryBuilders.queryStringQuery(escape(query)).type(MultiMatchQueryBuilder.Type.PHRASE);
    }

    private SearchResponse.Lst createHighlights(final SearchHit hit) {
        final SearchResponse.Lst documentLst = new SearchResponse.Lst(hit.getId());
        final List<SearchResponse.Arr> documentArrs = Lists.newArrayList();

        hit.getHighlightFields().values().stream().collect(getHighlightsCollector()).forEach((attribute, highlightField) -> {
            if("lookup-key".equals(attribute) || attribute.contains("lowercase")){
                return;
            }

            final SearchResponse.Arr arr = new SearchResponse.Arr(attribute);
            arr.setStr(new SearchResponse.Str(null, StringUtils.join(highlightField.getFragments(), ",")));
            documentArrs.add(arr);
        });

        documentLst.setArrs(documentArrs);
        return documentLst;
    }

    private static Collector<HighlightField, ?, Map<String, HighlightField>> getHighlightsCollector() {
        return Collectors.toMap(highlightField -> highlightField.name().contains(".custom") ?
                        StringUtils.substringBefore(highlightField.name(), ".custom") :
                        StringUtils.substringBefore(highlightField.name(), ".raw"), Function.identity(),
                (existing, replacement) -> existing);
    }

    private SearchResponse.Lst getCountByType(final Terms facets) {
        final SearchResponse.Lst facetCounts = new SearchResponse.Lst("facet_counts");
        final List<SearchResponse.Lst> facetCountsList = Lists.newArrayList();

        final SearchResponse.Lst facetFields = new SearchResponse.Lst("facet_fields");
        final List<SearchResponse.Lst> facetFieldsList = Lists.newArrayList();

        final SearchResponse.Lst facetLst = new SearchResponse.Lst("object-type");
        final List<SearchResponse.Int> facetInts = Lists.newArrayList();

        for (Terms.Bucket entry : facets.getBuckets()) {
            facetInts.add(new SearchResponse.Int((String) entry.getKey(), String.valueOf(entry.getDocCount())));
        }

        facetLst.setInts(facetInts);
        facetFieldsList.add(facetLst);

        facetFields.setLsts(facetFieldsList);
        facetCountsList.add(facetFields);

        facetCounts.setLsts(facetCountsList);
        return facetCounts;
    }

    @Nullable
    private String filterValue(final AttributeType type, final String attributeValue) {
        return attributeValue == null ? null : elasticIndexService.filterRpslAttribute(type, attributeValue);
    }

    private List<RpslAttribute> filterValues(final AttributeType attributeType, final List<String> attributeValues) {
        return attributeValues.stream().map( (attributeValue) -> new RpslAttribute( attributeType, filterValue(attributeType, attributeValue))).collect(Collectors.toList());
    }
}
