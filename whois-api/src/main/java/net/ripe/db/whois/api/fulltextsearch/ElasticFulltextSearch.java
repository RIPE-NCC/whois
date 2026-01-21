package net.ripe.db.whois.api.fulltextsearch;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    public static final TermsAggregation AGGREGATION_BUILDER = TermsAggregation.of(t -> t
            .field("object-type.raw")
            .size(ObjectType.values().length));

    public static final SortOptions SORT_BUILDERS = SortOptions.of( s-> s
            .field(fs -> fs
                            .field("lookup-key.raw")
                            .order(SortOrder.Asc)
                            .unmappedType(FieldType.Keyword)
            )
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

                final co.elastic.clients.elasticsearch.core.SearchResponse fulltextResponse = performFulltextSearch(searchRequest);

                final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();
                final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();

                final List<Hit<Map<String , Object>>> hits = fulltextResponse.hits().hits();
                for (final Hit<Map<String, Object>> hit : hits) {
                    final Map<String, Object> hitAttributes = hit.source();
                    final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
                    final List<SearchResponse.Str> responseStrs = Lists.newArrayList();
                    final List<RpslAttribute> attributes = Lists.newArrayList();
                    highlightDocs.add(createHighlights(hit));

                    final ObjectType objectType = ObjectType.getByName(hitAttributes.get(OBJECT_TYPE_FIELD_NAME).toString());
                    final String pKey = hitAttributes.get(LOOKUP_KEY_FIELD_NAME).toString();

                    responseStrs.add(new SearchResponse.Str(PRIMARY_KEY_FIELD_NAME, hit.id()));
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

    private co.elastic.clients.elasticsearch.core.SearchResponse performFulltextSearch(final SearchRequest searchRequest) throws IOException {
        try {
            return elasticIndexService.getClient().search(getFulltextRequest(searchRequest), Map.class);
        } catch (ElasticsearchException ex){
            // Detect BAD REQUEST (invalid query) from server error type
            if (ex.status() == 400) { // common for bad query DSL
                LOGGER.info("ElasticFullTextSearch failed due to invalid query syntax: {}", ex.getMessage());
                throw new IllegalArgumentException("Invalid query syntax");
            }

            LOGGER.error("ElasticFullTextSearch error: {}", ex.getMessage());
            throw ex;
        }
    }

    private co.elastic.clients.elasticsearch.core.SearchRequest getFulltextRequest(final SearchRequest searchRequest ) {
        final int start = Math.max(0, searchRequest.getStart());
        final Highlight highlight = getHighlight(searchRequest);

        return co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
                .index(elasticIndexService.getWhoisAliasIndex())
                .query(q -> q.queryString(w -> w.query(escape(searchRequest.getQuery())).type(TextQueryType.Phrase)))
                .from(start)
                .size(searchRequest.getRows())
                .timeout(Time.of(t -> t.time(GRACEFUL_TIMEOUT_IN_MS + "ms")).time())
                .aggregations("types-count", Aggregation.of( a -> a.terms(AGGREGATION_BUILDER)))
                .sort(SORT_BUILDERS)
                .highlight(highlight)
                .trackTotalHits(t -> t.enabled(true))
        );
    }

    private Highlight getHighlight(SearchRequest searchRequest) {
        return Highlight.of(h -> h
                .preTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPre()))
                .postTags(getHighlightTag(searchRequest.getFormat(), searchRequest.getHighlightPost()))
                .maxAnalyzedOffset(HIGHLIGHT_OFFSET_SIZE)
                .fields("*", HighlightField.of(f -> f)) // highlight all fields
        );
    }

    private SearchResponse prepareResponse(co.elastic.clients.elasticsearch.core.SearchResponse fulltextResponse, List<SearchResponse.Lst> highlightDocs, List<SearchResponse.Result.Doc> resultDocumentList, SearchRequest searchRequest, Stopwatch stopwatch) {
        final SearchResponse.Result result = new SearchResponse.Result("response", Long.valueOf(fulltextResponse.hits().total().value()).intValue(), searchRequest.getStart());
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
            final Aggregate countByType = (Aggregate) fulltextResponse.aggregations().get("types-count");

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

   private SearchResponse.Lst createHighlights(final Hit<Map<String, Object>> hit) {
       final SearchResponse.Lst documentLst = new SearchResponse.Lst(hit.id());
       final List<SearchResponse.Arr> documentArrs = new ArrayList<>();

       if (hit.highlight() != null) {

           final Map<String, List<String>> normalized = hit.highlight().entrySet().stream()
                   .collect(Collectors.toMap(
                           this::getCleanHighlightField,
                           Map.Entry::getValue,
                           (existing, replacement) -> existing,
                           LinkedHashMap::new
                   ));

           normalized.forEach((attribute, fragments) -> {
               if ("lookup-key".equals(attribute) || attribute.contains("lowercase")) {
                   return;
               }

               String joined = String.join(",", fragments);

               final SearchResponse.Arr arr = new SearchResponse.Arr(attribute);
               arr.setStr(new SearchResponse.Str(null, joined));
               documentArrs.add(arr);
           });
       }

       documentLst.setArrs(documentArrs);
       return documentLst;
   }

    private String getCleanHighlightField(final Map.Entry<String, List<String>> e) {
        return e.getKey().contains(".custom") ?
                StringUtils.substringBefore( e.getKey(), ".custom") :
                StringUtils.substringBefore( e.getKey(), ".raw");
    }

    private SearchResponse.Lst getCountByType(final Aggregate agg) {
        if (agg == null || !(agg._get() instanceof StringTermsAggregate terms)) {
            return null;
        }

        final SearchResponse.Lst facetCounts = new SearchResponse.Lst("facet_counts");
        final List<SearchResponse.Lst> facetCountsList = new ArrayList<>();

        final SearchResponse.Lst facetFields = new SearchResponse.Lst("facet_fields");
        final List<SearchResponse.Lst> facetFieldsList = new ArrayList<>();

        final SearchResponse.Lst facetLst = new SearchResponse.Lst("object-type");
        final List<SearchResponse.Int> facetInts = new ArrayList<>();

        // Iterate new buckets API
        for (var bucket : terms.buckets().array()) {
            String key = bucket.key().stringValue(); // 👈 aggregation bucket key
            long count = bucket.docCount();          // 👈 doc count

            if ("lookup-key".equals(key) || key.contains("lowercase")) {
                continue;
            }

            facetInts.add(new SearchResponse.Int(key, String.valueOf(count)));
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
