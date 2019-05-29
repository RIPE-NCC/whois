package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryException;
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
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.INDEX_ANALYZER;
import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.PRIMARY_KEY_FIELD_NAME;

@Component
@Path("/fulltextsearch")
public class FullTextSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullTextSearch.class);

    private static final Sort SORT_BY_OBJECT_TYPE = new Sort(new SortField(FullTextIndex.OBJECT_TYPE_FIELD_NAME, SortField.Type.STRING));
    private static final int MAX_RESULTS = 100;

    private final FullTextIndex fullTextIndex;
    private final AccessControlListManager accessControlListManager;
    private final Source source;

    @Autowired
    public FullTextSearch(final FullTextIndex fullTextIndex,
                          final AccessControlListManager accessControlListManager,
                          final SourceContext sourceContext) {
        this.fullTextIndex = fullTextIndex;
        this.accessControlListManager = accessControlListManager;
        this.source = sourceContext.getCurrentSource();
    }

    @GET
    @Path("/select")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response search(
        @QueryParam("q") final String query,
        @QueryParam("rows") @DefaultValue("10") final String rows,
        @QueryParam("start") @DefaultValue("0") final String start,
        @QueryParam("hl") @DefaultValue("false") final String highlight,
        @QueryParam("hl.simple.pre") @DefaultValue("<b>") final String highlightPre,
        @QueryParam("hl.simple.post") @DefaultValue("</b>") final String highlightPost,
        @QueryParam("wt") @DefaultValue("xml") final String writerType,
        @QueryParam("facet") @DefaultValue("false") final String facet,
        @Context final HttpServletRequest request) {
        try {
            return ok(search(
                new SearchRequest.SearchRequestBuilder()
                    .setRows(rows)
                    .setStart(start)
                    .setQuery(query)
                    .setHighlight(highlight)
                    .setHighlightPre(highlightPre)
                    .setHighlightPost(highlightPost)
                    .setFormat(writerType)
                    .setFacet(facet)
                    .build(), request));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (QueryException qe) {
            throw RestServiceHelper.createWebApplicationException(qe, request);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return internalServerError("Unexpected error");
        }
    }

    private Response ok(final SearchResponse searchResponse) {
        return Response.ok(searchResponse).build();
    }

    private Response badRequest(final String message) {
        return javax.ws.rs.core.Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response internalServerError(final String message) {
        return javax.ws.rs.core.Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }

    public SearchResponse search(final SearchRequest searchRequest, final HttpServletRequest request) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final QueryParser queryParser = new MultiFieldQueryParser(FullTextIndex.FIELD_NAMES, FullTextIndex.QUERY_ANALYZER);
        queryParser.setDefaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.AND);

        final Query query;
        try {
            query = queryParser.parse(escape(searchRequest.getQuery()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        try {
            return fullTextIndex.search(
                    new IndexTemplate.AccountingSearchCallback<SearchResponse>(accessControlListManager, request.getRemoteAddr(), source) {

                @Override
                protected SearchResponse doSearch(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {

                    final int maxResults = Math.max(MAX_RESULTS, indexReader.numDocs());
                    final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_OBJECT_TYPE, maxResults, false, false, false, false);
                    final FacetsCollector facetsCollector = new FacetsCollector();

                    indexSearcher.search(query, MultiCollector.wrap(topFieldCollector, facetsCollector));

                    final List<Document> documents = Lists.newArrayList();

                    final TopDocs topDocs = topFieldCollector.topDocs();
                    final int start = Math.max(0, searchRequest.getStart());
                    final int end = Math.min(start + searchRequest.getRows(), topDocs.totalHits);
                    for (int index = start; index < end; index++) {
                        final ScoreDoc scoreDoc = topDocs.scoreDocs[index];

                        Document document = indexSearcher.doc(scoreDoc.doc);
                        account(convertToRpslObject(document));

                        documents.add(document);
                    }

                    final List<SearchResponse.Lst> responseLstList = Lists.newArrayList();
                    responseLstList.add(getResponseHeader(searchRequest, stopwatch.elapsed(TimeUnit.MILLISECONDS)));

                    if (searchRequest.isHighlight()) {
                        responseLstList.add(createHighlights(searchRequest, query, documents));
                    }

                    if (searchRequest.isFacet()) {
                        final FacetsConfig facetsConfig = new FacetsConfig();
                        final Facets facets = new FastTaxonomyFacetCounts(taxonomyReader, facetsConfig, facetsCollector);

                        responseLstList.add(getFacet(facets));
                    }

                    final SearchResponse searchResponse = new SearchResponse();
                    searchResponse.setResult(createResult(searchRequest, documents, topDocs.totalHits));
                    searchResponse.setLsts(responseLstList);

                    return searchResponse;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String escape(final String value) {
        return value.replaceAll("[/]", "\\\\/");
    }

    private SearchResponse.Lst getResponseHeader(final SearchRequest searchRequest, final long elapsedTime) {
        SearchResponse.Lst responseHeader = new SearchResponse.Lst("responseHeader");
        final List<SearchResponse.Int> responseHeaderInts = Lists.newArrayList(new SearchResponse.Int("status", "0"), new SearchResponse.Int("QTime", Long.toString(elapsedTime)));
        responseHeader.setInts(responseHeaderInts);

        final List<SearchResponse.Str> paramStrs = Lists.newArrayList();
        paramStrs.add(new SearchResponse.Str("q", searchRequest.getQuery()));
        paramStrs.add(new SearchResponse.Str("rows", Integer.toString(searchRequest.getRows())));
        paramStrs.add(new SearchResponse.Str("start", Integer.toString(searchRequest.getStart())));
        paramStrs.add(new SearchResponse.Str("hl", Boolean.toString(searchRequest.isHighlight())));
        paramStrs.add(new SearchResponse.Str("hl.simple.pre", searchRequest.getHighlightPre()));
        paramStrs.add(new SearchResponse.Str("hl.simple.post", searchRequest.getHighlightPost()));
        paramStrs.add(new SearchResponse.Str("wt", searchRequest.getFormat()));
        paramStrs.add(new SearchResponse.Str("facet", Boolean.toString(searchRequest.isFacet())));

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

        final SimpleHTMLFormatter formatter;
        if (SearchRequest.XML_FORMAT.equals(searchRequest.getFormat())) {
            formatter = new SimpleHTMLFormatter(escape(searchRequest.getHighlightPre()), escape(searchRequest.getHighlightPost()));
        } else {
            // don't escape highlighting in JSON responses
            formatter = new SimpleHTMLFormatter(searchRequest.getHighlightPre(), searchRequest.getHighlightPost());
        }

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
