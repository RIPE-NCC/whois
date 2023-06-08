package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.autocomplete.ElasticSearchCondition;
import net.ripe.db.whois.api.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.api.elasticsearch.ElasticSearchAccountingCallback;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Conditional(ElasticSearchCondition.class)
public class ElasticFulltextSearch extends FulltextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFulltextSearch.class);

    public static final TermsAggregationBuilder AGGREGATION_BUILDER = AggregationBuilders.terms("types-count").field("object-type.raw");
    public static final List<SortBuilder<?>> SORT_BUILDERS = Arrays.asList(SortBuilders.scoreSort(), SortBuilders.fieldSort("lookup-key.raw").unmappedType("keyword"));

    private final FullTextIndex fullTextIndex;
    private final AccessControlListManager accessControlListManager;
    private final ElasticIndexService elasticIndexService;

    private final Source source;
    private final RpslObjectDao objectDao;

    //Truncate after 100k of characters
    private static final int HIGHLIGHT_OFFSET_SIZE = 100000;

    private final int maxResultSize;

    @Autowired
    public ElasticFulltextSearch(final FullTextIndex fullTextIndex,
                                 final ElasticIndexService elasticIndexService,
                                 @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
                                 final AccessControlListManager accessControlListManager,
                                 final SourceContext sourceContext,
                                 final ApplicationVersion applicationVersion,
                                 @Value("${fulltext.search.max.results:10000}") final int maxResultSize) {
        super(applicationVersion);

        this.fullTextIndex = fullTextIndex;
        this.accessControlListManager = accessControlListManager;
        this.source = sourceContext.getCurrentSource();
        this.objectDao = rpslObjectDao;
        this.maxResultSize = maxResultSize;
        this.elasticIndexService = elasticIndexService;
    }

    @Override
    public SearchResponse performSearch(final SearchRequest searchRequest, final String remoteAddr) throws IOException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (searchRequest.getRows() > maxResultSize) {
            throw new IllegalArgumentException("Too many results requested, the maximum allowed is " + maxResultSize);
        }

        return new ElasticSearchAccountingCallback<SearchResponse>(accessControlListManager, remoteAddr, source) {

            @Override
            protected SearchResponse doSearch() throws IOException {

                final int start = Math.max(0, searchRequest.getStart());
                final HighlightBuilder highlightBuilder = new HighlightBuilder()
                        .postTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPost()))
                        .preTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPre()))
                        .maxAnalyzedOffset(HIGHLIGHT_OFFSET_SIZE)
                        .field("*");

                final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                        .query(getQueryBuilder(searchRequest.getQuery()))
                        .size(searchRequest.getRows()).from(start)
                        .aggregation(AGGREGATION_BUILDER)
                        .sort(SORT_BUILDERS)
                        .highlighter(highlightBuilder);

                final org.elasticsearch.action.search.SearchRequest fulltextRequest = new org.elasticsearch.action.search.SearchRequest(elasticIndexService.getWhoisAliasIndex());
                fulltextRequest.source(sourceBuilder);

                final org.elasticsearch.action.search.SearchResponse fulltextResponse = elasticIndexService.getClient().search(fulltextRequest, RequestOptions.DEFAULT);
                final SearchHit[] hits = fulltextResponse.getHits().getHits();

                LOGGER.debug("ElasticSearch {} hits for the query: {}", hits.length, searchRequest.getQuery());
                return prepareResponse(fulltextResponse, hits, searchRequest, stopwatch);
            }
        }.search();
    }

    private SearchResponse prepareResponse(final org.elasticsearch.action.search.SearchResponse fulltextResponse,
                                           final SearchHit[] hits,
                                           final SearchRequest searchRequest,
                                           final Stopwatch stopwatch) {
        final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();
        final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();

        for (final SearchHit hit : hits) {
            final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
            highlightDocs.add(createHighlights(hit));

            resultDocument.setStrs(getAttributes(hit.getSourceAsMap()));
            resultDocumentList.add(resultDocument);
        }

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

    private List<SearchResponse.Str> getAttributes(Map<String, Object> hitAttributes) {
        final List<SearchResponse.Str> attributes = Lists.newArrayList();

        final ObjectType objectType = ObjectType.getByName(hitAttributes.get(FullTextIndex.OBJECT_TYPE_FIELD_NAME).toString());
        attributes.add(new SearchResponse.Str(FullTextIndex.OBJECT_TYPE_FIELD_NAME, objectType.getName()));
        attributes.add(new SearchResponse.Str(FullTextIndex.LOOKUP_KEY_FIELD_NAME, hitAttributes.get(FullTextIndex.LOOKUP_KEY_FIELD_NAME).toString()));

        final Set<AttributeType> templateAttributes = ObjectTemplate.getTemplate(objectType).getAllAttributes();

        for (final RpslAttribute rpslAttribute : fullTextIndex.filterRpslAttributes(templateAttributes, hitAttributes)) {
            attributes.add(new SearchResponse.Str(rpslAttribute.getKey(), rpslAttribute.getValue()));
        }
        return attributes;
    }

    private String getHighlightTag(final String format, final String highlightPost) {
        return SearchRequest.XML_FORMAT.equals(format) ? escape(highlightPost) : highlightPost;
    }

    private QueryStringQueryBuilder getQueryBuilder(final String query) {
        return QueryBuilders.queryStringQuery(escape(query)).type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX);
    }

    private SearchResponse.Lst createHighlights(final SearchHit hit) {
        final SearchResponse.Lst documentLst = new SearchResponse.Lst(hit.getId());
        final List<SearchResponse.Arr> documentArrs = Lists.newArrayList();

        hit.getHighlightFields().forEach((attribute, highlightField) -> {
            if("lookup-key".equals(attribute) || "lookup-key.custom".equals(attribute)){
                return;
            }
            if(attribute.contains(".custom")) {
                final SearchResponse.Arr arr = new SearchResponse.Arr(StringUtils.substringBefore(highlightField.name(), ".custom"));
                arr.setStr(new SearchResponse.Str(null, StringUtils.join(highlightField.getFragments(), ",")));
                documentArrs.add(arr);

                //Somehow if searched term contains "." highlight field custom has no vlue for it.
            } else if(!hit.getHighlightFields().containsKey(attribute + ".custom"))  {
                final SearchResponse.Arr arr = new SearchResponse.Arr(highlightField.name());
                arr.setStr(new SearchResponse.Str(null, StringUtils.join(highlightField.getFragments(), ",")));
                documentArrs.add(arr);
            }
        });
        documentLst.setArrs(documentArrs);
        return documentLst;
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
}
