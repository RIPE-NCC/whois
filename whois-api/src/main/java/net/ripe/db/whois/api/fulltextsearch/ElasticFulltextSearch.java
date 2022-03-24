package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.autocomplete.ElasticSearchCondition;
import net.ripe.db.whois.api.elasticsearch.AccountingElasticSearchCallback;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.elasticsearch.ElasticIndexService;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Conditional(ElasticSearchCondition.class)
public class ElasticFulltextSearch extends FulltextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFulltextSearch.class);

    public static final TermsAggregationBuilder AGGREGATION_BUILDER = AggregationBuilders.terms("types-count").field("object-type.keyword");
    public static final List<SortBuilder<?>> SORT_BUILDERS = Arrays.asList(SortBuilders.scoreSort(), SortBuilders.fieldSort("lookup-key.keyword").unmappedType("string"));

    private final FullTextIndex fullTextIndex;
    private final AccessControlListManager accessControlListManager;
    private final ElasticIndexService elasticIndexService;

    private final Source source;
    private final RpslObjectDao objectDao;

    private final int maxResultSize;

    @Autowired
    public ElasticFulltextSearch(final FullTextIndex fullTextIndex,
                                 final ElasticIndexService elasticIndexService,
                                 @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
                                 final AccessControlListManager accessControlListManager,
                                 final SourceContext sourceContext,
                                 final ApplicationVersion applicationVersion,
                                 @Value("${fulltext.search.max.results:100}") final int maxResultSize) {
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

        if (searchRequest.getStart() > maxResultSize) {
            throw new IllegalArgumentException("Too many rows");
        }

        return new AccountingElasticSearchCallback<SearchResponse>(accessControlListManager, remoteAddr, source) {

            @Override
            protected SearchResponse doSearch() throws IOException {

                final int start = Math.max(0, searchRequest.getStart());
                final int end = Math.min(start + searchRequest.getRows(), maxResultSize);
                final HighlightBuilder highlightBuilder = new HighlightBuilder()
                        .postTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPost()))
                        .preTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPre()))
                        .field("*");

                final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                        .query(getQueryBuilder(searchRequest.getQuery()))
                        .size(end).from(start)
                        .aggregation(AGGREGATION_BUILDER)
                        .sort(SORT_BUILDERS)
                        .highlighter(highlightBuilder);

                final org.elasticsearch.action.search.SearchRequest fulltextRequest = new org.elasticsearch.action.search.SearchRequest(elasticIndexService.getWHOIS_INDEX());
                fulltextRequest.source(sourceBuilder);

                final org.elasticsearch.action.search.SearchResponse fulltextResponse = elasticIndexService.getClient().search(fulltextRequest, RequestOptions.DEFAULT);
                SearchHit[] hits = fulltextResponse.getHits().getHits();

                final List<RpslObject> results = new ArrayList<>();
                int resultSize = hits.length;

                final SearchResponse.Lst highlight = new SearchResponse.Lst("highlighting");
                final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();

                for (final SearchHit hit : hits) {
                    final RpslObject rpslObject;
                    try {
                        rpslObject = objectDao.getById(Integer.parseInt(hit.getId()));
                        results.add(rpslObject);
                        account(rpslObject);

                        highlightDocs.add(createHighlights(hit));

                    } catch (EmptyResultDataAccessException e) {
                        // object was deleted from the database but index was not updated yet
                        resultSize--;
                        continue;
                    }
                }

                highlight.setLsts(highlightDocs);

                final List<SearchResponse.Lst> responseLstList = Lists.newArrayList();
                responseLstList.add(getResponseHeader(searchRequest, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

                if (searchRequest.isHighlight()) {
                    responseLstList.add(highlight);
                }

                if (searchRequest.isFacet()) {
                    Terms countByType = fulltextResponse.getAggregations().get("types-count");
                    responseLstList.add(getCountByType(countByType));
                }

                responseLstList.add(createVersion());

                final SearchResponse searchResponse = new SearchResponse();
                searchResponse.setResult(createResult(searchRequest, results, resultSize));
                searchResponse.setLsts(responseLstList);

                return searchResponse;
            }
        }.search();
    }

    private String getHighlightTag(final String format, final String highlightPost) {
        return SearchRequest.XML_FORMAT.equals(format) ? escape(highlightPost) : highlightPost;
    }

    private QueryStringQueryBuilder getQueryBuilder(final String query) {
        return QueryBuilders.queryStringQuery(escape(query)).type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX).analyzer("fulltext_analyzer");
    }

    private SearchResponse.Lst createHighlights(final SearchHit hit) {

        final SearchResponse.Lst documentLst = new SearchResponse.Lst(hit.getId());
        final List<SearchResponse.Arr> documentArrs = Lists.newArrayList();

        for (final HighlightField highlightField : hit.getHighlightFields().values()) {
            try {
                final SearchResponse.Arr arr = new SearchResponse.Arr(highlightField.name());
                arr.setStr(new SearchResponse.Str(null, StringUtils.join(highlightField.getFragments(), ",")));
                documentArrs.add(arr);
            } catch (Exception e) {
                LOGGER.warn("Field name: " + highlightField.name() + " value:" + highlightField.getFragments(), e);
            }
        }

        documentLst.setArrs(documentArrs);
        return documentLst;
    }

    private SearchResponse.Lst getCountByType(final Terms facets) throws IOException {
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

    private SearchResponse.Result createResult(final SearchRequest searchRequest, final List<RpslObject> objects, final int totalHits) {
        final SearchResponse.Result result = new SearchResponse.Result("response", totalHits, searchRequest.getStart());

        final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();

        for (RpslObject rpslObject : objects) {

            final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
            final List<SearchResponse.Str> attributes = Lists.newArrayList();

            attributes.add(new SearchResponse.Str("primary-key", String.valueOf(rpslObject.getObjectId())));
            attributes.add(new SearchResponse.Str("object-type", rpslObject.getType().getName()));
            attributes.add(new SearchResponse.Str("lookup-key", rpslObject.getKey().toString()));

            for (final RpslAttribute rpslAttribute : fullTextIndex.filterRpslObject(rpslObject).getAttributes()) {
                attributes.add(new SearchResponse.Str(rpslAttribute.getKey(), rpslAttribute.getValue()));
            }

            resultDocument.setStrs(attributes);
            resultDocumentList.add(resultDocument);
        }

        result.setDocs(resultDocumentList);
        return result;
    }
}
