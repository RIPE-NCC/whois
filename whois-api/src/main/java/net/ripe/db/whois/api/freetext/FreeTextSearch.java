package net.ripe.db.whois.api.freetext;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.search.IndexTemplate;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.CountFacetRequest;
import org.apache.lucene.facet.params.FacetSearchParams;
import org.apache.lucene.facet.search.FacetResult;
import org.apache.lucene.facet.search.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.api.freetext.FreeTextIndex.INDEX_ANALYZER;
import static net.ripe.db.whois.api.freetext.FreeTextIndex.PRIMARY_KEY_FIELD_NAME;

@Component
class FreeTextSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeTextSearch.class);

    static final Sort SORT_BY_OBJECT_TYPE = new Sort(new SortField(FreeTextIndex.OBJECT_TYPE_FIELD_NAME, SortField.Type.STRING));
    static final FacetSearchParams FACET_SEARCH_PARAMS = new FacetSearchParams(new CountFacetRequest(new CategoryPath(FreeTextIndex.OBJECT_TYPE_FIELD_NAME), Integer.MAX_VALUE));

    private final FreeTextIndex freeTextIndex;
    private final Marshaller marshaller;

    @Autowired
    public FreeTextSearch(final FreeTextIndex freeTextIndex, final Marshaller marshaller) {
        this.freeTextIndex = freeTextIndex;
        this.marshaller = marshaller;
    }

    public void freeTextSearch(final String query, final Writer writer) throws IOException {
        try {
            final SearchRequest searchRequest = SearchRequest.parse(query);
            performFreeTextSearch(searchRequest, writer);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid query: %s", query), e);
        }
    }

    private void performFreeTextSearch(final SearchRequest searchRequest, final Writer writer) throws IOException, ParseException {
        if (!"XML".equalsIgnoreCase(searchRequest.getFormat())) {
            throw new IllegalArgumentException(String.format("Unsupported format: %s", searchRequest.getFormat()));
        }

        if (searchRequest.getQuery() == null) {
            throw new IllegalArgumentException("Missing query parameter");
        }

        search(searchRequest, writer);
    }

    private void search(final SearchRequest searchRequest, final Writer writer) throws IOException, ParseException {
        final Stopwatch stopwatch = new Stopwatch().start();

        final QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_44, FreeTextIndex.FIELD_NAMES, FreeTextIndex.QUERY_ANALYZER);
        queryParser.setDefaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.AND);
        final Query query = queryParser.parse(searchRequest.getQuery());

        freeTextIndex.search(new IndexTemplate.SearchCallback<Void>() {
            @Override
            public Void search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                final int maxResults = Math.max(100, indexReader.numDocs());
                final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_OBJECT_TYPE, maxResults, false, false, false, false);
                final FacetsCollector facetsCollector = FacetsCollector.create(FACET_SEARCH_PARAMS, indexReader, taxonomyReader);

                indexSearcher.search(query, MultiCollector.wrap(topFieldCollector, facetsCollector));

                final List<Document> documents = Lists.newArrayList();

                final TopDocs topDocs = topFieldCollector.topDocs();
                final int start = Math.max(0, searchRequest.getStart());
                final int end = Math.min(start + searchRequest.getRows(), topDocs.totalHits);
                for (int index = start; index < end; index++) {
                    final ScoreDoc scoreDoc = topDocs.scoreDocs[index];
                    documents.add(indexSearcher.doc(scoreDoc.doc));
                }

                final List<FacetResult> facetResults = facetsCollector.getFacetResults();
                final List<SearchResponse.Lst> responseLstList = Lists.newArrayList();
                responseLstList.add(getResponseHeader(searchRequest, stopwatch.elapsedTime(TimeUnit.MILLISECONDS)));

                if (searchRequest.isHighlight()) {
                    responseLstList.add(createHighlights(searchRequest, query, documents));
                }

                if (searchRequest.isFacet()) {
                    responseLstList.add(getFacet(facetResults));
                }

                final SearchResponse searchResponse = new SearchResponse();
                searchResponse.setResult(createResult(searchRequest, documents, topDocs.totalHits));
                searchResponse.setLsts(responseLstList);

                marshaller.marshal(searchResponse, new StreamResult(writer));
                return null;
            }
        });
    }

    private SearchResponse.Lst getResponseHeader(SearchRequest searchRequest, final long elapsedTime) {
        SearchResponse.Lst responseHeader = new SearchResponse.Lst("responseHeader");
        final List<SearchResponse.Int> responseHeaderInts = Lists.newArrayList(new SearchResponse.Int("status", "0"), new SearchResponse.Int("QTime", Long.toString(elapsedTime)));
        responseHeader.setInts(responseHeaderInts);

        final List<SearchResponse.Str> paramStrs = Lists.newArrayList();
        for (Map.Entry<String, String> param : searchRequest.getParams().entrySet()) {
            paramStrs.add(new SearchResponse.Str(param.getKey(), param.getValue()));
        }

        final SearchResponse.Lst params = new SearchResponse.Lst("params");
        params.setStrs(paramStrs);
        responseHeader.setLsts(Lists.newArrayList(params));
        return responseHeader;
    }

    private SearchResponse.Result createResult(final SearchRequest searchRequest, final List<Document> documents, final int totalHits) {
        final SearchResponse.Result result = new SearchResponse.Result("response", totalHits, searchRequest.getStart());

        final List<SearchResponse.Result.Doc> resultDocumentList = Lists.newArrayList();
        for (Document document : documents) {
            final SearchResponse.Result.Doc resultDocument = new SearchResponse.Result.Doc();
            final List<SearchResponse.Str> attributes = Lists.newArrayList();

            for (final IndexableField field : document.getFields()) {
                attributes.add(new SearchResponse.Str(field.name(), field.stringValue()));
            }

            resultDocument.setStrs(attributes);
            resultDocumentList.add(resultDocument);
        }

        result.setDocs(resultDocumentList);
        return result;
    }

    private SearchResponse.Lst createHighlights(final SearchRequest searchRequest, final Query query, final List<Document> documents) {
        final SearchResponse.Lst highlight = new SearchResponse.Lst("highlighting");
        final List<SearchResponse.Lst> highlightDocs = Lists.newArrayList();

        final SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(searchRequest.getHighlightPre(), searchRequest.getHighlightPost());

        final Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));

        for (final Document document : documents) {
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

            documentLst.setArrs(documentArrs);
            highlightDocs.add(documentLst);
        }

        highlight.setLsts(highlightDocs);
        return highlight;
    }

    private SearchResponse.Lst getFacet(final List<FacetResult> facetResults) {
        final SearchResponse.Lst facetCounts = new SearchResponse.Lst("facet_counts");
        final List<SearchResponse.Lst> facetCountsList = Lists.newArrayList();

        final SearchResponse.Lst facetFields = new SearchResponse.Lst("facet_fields");
        final List<SearchResponse.Lst> facetFieldsList = Lists.newArrayList();

        for (final FacetResult facetResult : facetResults) {
            final String label = facetResult.getFacetResultNode().label.toString();

            final SearchResponse.Lst facetLst = new SearchResponse.Lst(label);
            final List<SearchResponse.Int> facetInts = Lists.newArrayList();

            for (final FacetResultNode facetResultNode : facetResult.getFacetResultNode().subResults) {
                final String name = facetResultNode.label.toString();
                final String value = Integer.toString(Double.valueOf(facetResultNode.value).intValue());
                facetInts.add(new SearchResponse.Int(name.substring(name.indexOf('/') + 1), value));
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
