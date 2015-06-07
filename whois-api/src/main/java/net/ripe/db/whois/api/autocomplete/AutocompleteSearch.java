package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.search.IndexTemplate;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.WildcardQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class AutocompleteSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteSearch.class);

    private static final int MAX_SEARCH_RESULTS = 10;

    private final FreeTextIndex freeTextIndex;

    @Autowired
    public AutocompleteSearch(final FreeTextIndex freeTextIndex) {
        this.freeTextIndex = freeTextIndex;
    }

    public List<String> search(final String queryString, final String fieldName) throws IOException {
        return freeTextIndex.search(new IndexTemplate.SearchCallback<List<String>>() {
            @Override
            public List<String> search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {

                final List<String> results = Lists.newArrayList();

                final Query query = new WildcardQuery(new Term(fieldName, String.format("%s*", queryString.toLowerCase())));
                final Sort sortedBy = new Sort(new SortField(fieldName, SortField.Type.STRING));

                final int maxResults = Math.max(MAX_SEARCH_RESULTS, indexReader.numDocs());

                final TopFieldCollector topFieldCollector = TopFieldCollector.create(sortedBy, maxResults, false, false, false, false);
                final FacetsCollector facetsCollector = new FacetsCollector();

                indexSearcher.search(query, MultiCollector.wrap(topFieldCollector, facetsCollector));

                final TopDocs topDocs = topFieldCollector.topDocs();
                final int start = 0;
                final int end = Math.min(start + 10, topDocs.totalHits);
                for (int index = start; index < end; index++) {
                    final ScoreDoc scoreDoc = topDocs.scoreDocs[index];
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    results.add(doc.get(fieldName));
                }

                return results;
            }
        });
    }
}
