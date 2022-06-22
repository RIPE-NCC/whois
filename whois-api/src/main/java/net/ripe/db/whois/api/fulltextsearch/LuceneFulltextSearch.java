package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.autocomplete.LuceneSearchCondition;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.INDEX_ANALYZER;
import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.LOOKUP_KEY_FIELD_NAME;
import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.PRIMARY_KEY_FIELD_NAME;

@Component
@Conditional(LuceneSearchCondition.class)
public class LuceneFulltextSearch extends FulltextSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneFulltextSearch.class);

    private static final Sort SORT_BY_OBJECT_TYPE =
            new Sort(new SortField(FullTextIndex.OBJECT_TYPE_FIELD_NAME, SortField.Type.STRING), new SortField(LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));


    private final FullTextIndex fullTextIndex;
    private final AccessControlListManager accessControlListManager;
    private final Source source;
    private final RpslObjectDao objectDao;

    private final int maxResultSize;

    @Autowired
    public LuceneFulltextSearch(final FullTextIndex fullTextIndex,
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
    }

    @Override
    public SearchResponse performSearch(final SearchRequest searchRequest, final String remoteAddr) throws IOException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        if (searchRequest.getStart() > maxResultSize) {
            throw new IllegalArgumentException("Too many rows");
        }

        final QueryParser queryParser = new MultiFieldQueryParser(FullTextIndex.FIELD_NAMES, FullTextIndex.QUERY_ANALYZER);
        queryParser.setDefaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.AND);

        final Query query;
        try {
            query = queryParser.parse(escape(searchRequest.getQuery()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        return fullTextIndex.search(
                new IndexTemplate.AccountingSearchCallback<>(accessControlListManager, remoteAddr, source) {

                    @Override
                    protected SearchResponse doSearch(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {

                        final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_OBJECT_TYPE, maxResultSize, Integer.MAX_VALUE);
                        final FacetsCollector facetsCollector = new FacetsCollector();

                        indexSearcher.search(query, MultiCollector.wrap(topFieldCollector, facetsCollector));

                        final Map<RpslObject, Document> rpslObjectToDocument = Maps.newHashMap();

                        final TopDocs topDocs = topFieldCollector.topDocs();
                        final int start = Math.max(0, searchRequest.getStart());
                        int resultSize = Math.min(maxResultSize, Long.valueOf(topDocs.totalHits.value).intValue());

                        final int end = Math.min(start + searchRequest.getRows(), resultSize);
                        for (int index = start; index < end; index++) {
                            final ScoreDoc scoreDoc = topDocs.scoreDocs[index];
                            final Document document = indexSearcher.doc(scoreDoc.doc);
                            final RpslObject object;
                            try {
                                object = objectDao.getById(getObjectId(document));
                            } catch (EmptyResultDataAccessException e) {
                                // object was deleted from the database but index was not updated yet
                                resultSize--;
                                continue;
                            }
                            account(object);
                            rpslObjectToDocument.put(object, document);
                        }

                        final List<SearchResponse.Lst> responseLstList = Lists.newArrayList();
                        responseLstList.add(getResponseHeader(searchRequest, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

                        if (searchRequest.isHighlight()) {
                            responseLstList.add(createHighlights(searchRequest, query, rpslObjectToDocument));
                        }

                        if (searchRequest.isFacet()) {
                            final FacetsConfig facetsConfig = new FacetsConfig();
                            final Facets facets = new FastTaxonomyFacetCounts(taxonomyReader, facetsConfig, facetsCollector);

                            responseLstList.add(getFacet(facets));
                        }

                        responseLstList.add(createVersion());

                        final SearchResponse searchResponse = new SearchResponse();
                        searchResponse.setResult(createResult(searchRequest, rpslObjectToDocument, resultSize));
                        searchResponse.setLsts(responseLstList);

                        return searchResponse;
                    }
                });
    }

    private SearchResponse.Result createResult(final SearchRequest searchRequest, final Map<RpslObject, Document> rpslObjectToDocument, final int totalHits) {
        final SearchResponse.Result result = new SearchResponse.Result("response", totalHits, searchRequest.getStart());

        final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();

        rpslObjectToDocument.forEach((rpslObject, document) -> {

            final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
            final List<SearchResponse.Str> attributes = Lists.newArrayList();

            for (final IndexableField field : document.getFields()) {
                attributes.add(new SearchResponse.Str(field.name(), field.stringValue()));
            }

            for (final RpslAttribute rpslAttribute : fullTextIndex.filterRpslObject(rpslObject).getAttributes()) {
                attributes.add(new SearchResponse.Str(rpslAttribute.getKey(), rpslAttribute.getValue()));
            }

            resultDocument.setStrs(attributes);
            resultDocumentList.add(resultDocument);
        });


        result.setDocs(resultDocumentList);
        return result;
    }

    private SearchResponse.Lst createHighlights(final SearchRequest searchRequest, final Query query, final Map<RpslObject, Document> rpslObjectToDocument) {
        final SearchResponse.Lst highlight = new SearchResponse.Lst("highlighting");
        final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();

        final SimpleHTMLFormatter formatter;
        if (SearchRequest.XML_FORMAT.equals(searchRequest.getFormat())) {
            formatter = new SimpleHTMLFormatter(escape(searchRequest.getHighlightPre()), escape(searchRequest.getHighlightPost()));
        } else {
            // don't escape highlighting in JSON responses
            formatter = new SimpleHTMLFormatter(searchRequest.getHighlightPre(), searchRequest.getHighlightPost());
        }

        final Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));

        rpslObjectToDocument.forEach((rpslObject, document) -> {
            final SearchResponse.Lst documentLst = new SearchResponse.Lst(document.get(PRIMARY_KEY_FIELD_NAME));
            final List<SearchResponse.Arr> documentArrs = Lists.newArrayList();

            for (final IndexableField field : document.getFields()) {
                try {
                    final String highlightedValue = highlighter.getBestFragment(INDEX_ANALYZER, field.stringValue(), field.stringValue());
                    if (highlightedValue != null) {
                        final SearchResponse.Arr arr = new SearchResponse.Arr(field.name());
                        arr.setStr(new SearchResponse.Str(null, highlightedValue));
                        documentArrs.add(arr);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Field name: " + field.name() + " value:" + field.stringValue(), e);
                }
            }

            for (final RpslAttribute rpslAttribute : fullTextIndex.filterRpslObject(rpslObject).getAttributes()) {
                try {
                    final String highlightedValue = highlighter.getBestFragment(INDEX_ANALYZER, rpslAttribute.getValue(), rpslAttribute.getValue());
                    if (highlightedValue != null) {
                        final SearchResponse.Arr arr = new SearchResponse.Arr(rpslAttribute.getKey());
                        arr.setStr(new SearchResponse.Str(null, highlightedValue));
                        documentArrs.add(arr);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Field name: " + rpslAttribute.getKey() + " value:" + rpslAttribute.getValue(), e);
                }
            }

            documentLst.setArrs(documentArrs);
            highlightDocs.add(documentLst);
        });

        highlight.setLsts(highlightDocs);
        return highlight;
    }

    private SearchResponse.Lst getFacet(final Facets facets) throws IOException {
        final SearchResponse.Lst facetCounts = new SearchResponse.Lst("facet_counts");
        final List<SearchResponse.Lst> facetCountsList = Lists.newArrayList();

        final SearchResponse.Lst facetFields = new SearchResponse.Lst("facet_fields");
        final List<SearchResponse.Lst> facetFieldsList = Lists.newArrayList();

        for (FacetResult facetResult : facets.getAllDims(Integer.MAX_VALUE)) {

            final String label = facetResult.dim;

            final SearchResponse.Lst facetLst = new SearchResponse.Lst(label);
            final List<SearchResponse.Int> facetInts = Lists.newArrayList();

            for (LabelAndValue labelValue : facetResult.labelValues) {
                facetInts.add(new SearchResponse.Int(labelValue.label, labelValue.value.toString()));
            }

            facetLst.setInts(facetInts);
            facetFieldsList.add(facetLst);
        }

        facetFields.setLsts(facetFieldsList);
        facetCountsList.add(facetFields);

        facetCounts.setLsts(facetCountsList);
        return facetCounts;
    }
}
