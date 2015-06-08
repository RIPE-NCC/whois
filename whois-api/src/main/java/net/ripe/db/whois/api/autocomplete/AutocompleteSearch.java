package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.search.IndexTemplate;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
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

                // TODO: field to sort must be indexed but not tokenized

                final TopFieldDocs topDocs = indexSearcher.search(query, MAX_SEARCH_RESULTS, new Sort(new SortField(fieldName, SortField.Type.STRING)));

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    results.add(doc.get(fieldName));
                }

                return results;
            }
        });
    }
}
