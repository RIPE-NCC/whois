package net.ripe.db.whois.api.autocomplete;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.freetext.FreeTextAnalyzer;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AutocompleteSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteSearch.class);

    // TODO: [ES] search results are NOT sorted
    private static final Sort SORT_BY_LOOKUP_KEY = new Sort(new SortField(FreeTextIndex.LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));

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

                final Query query = constructQuery(getObjectTypeLookupKeys(fieldName), queryString);
                final TopFieldDocs topDocs = indexSearcher.search(query, MAX_SEARCH_RESULTS, SORT_BY_LOOKUP_KEY);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    results.add(doc.get(FreeTextIndex.LOOKUP_KEY_FIELD_NAME));
                }

                return results;
            }
        });
    }

    public List<Map<String, Object>> searchExtended(final String queryString, final String fieldName, final List<String> attributes) throws IOException {
        return freeTextIndex.search(new IndexTemplate.SearchCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {

                final List<Map<String, Object>> results = Lists.newArrayList();

                final Query query = constructQuery(getObjectTypeLookupKeys(fieldName), queryString);
                final TopFieldDocs topDocs = indexSearcher.search(query, MAX_SEARCH_RESULTS, SORT_BY_LOOKUP_KEY);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    final Map<String, Object> result = Maps.newLinkedHashMap();
                    result.put("key", doc.get(FreeTextIndex.LOOKUP_KEY_FIELD_NAME));
                    result.put("type", doc.get(FreeTextIndex.OBJECT_TYPE_FIELD_NAME));

                    for (final String attribute : attributes) {
                        final ObjectTemplate template = ObjectTemplate.getTemplate(
                                ObjectType.getByName(doc.get(FreeTextIndex.OBJECT_TYPE_FIELD_NAME)));

                        if (template.getMultipleAttributes().contains(AttributeType.getByName(attribute))) {
                            result.put(attribute, Lists.newArrayList(doc.getValues(attribute)));
                        } else {
                            result.put(attribute, doc.get(attribute));
                        }
                    }

                    results.add(result);

                }
                return results;
            }
        });
    }

    private Query constructQuery(final Set<String> fields, final String queryString) {
        try {
            final MultiFieldQueryParser parser = new MultiFieldQueryParser(fields.toArray(new String[fields.size()]),
                    new FreeTextAnalyzer(FreeTextAnalyzer.Operation.QUERY));
            parser.setAnalyzer(FreeTextIndex.QUERY_ANALYZER);
            parser.setDefaultOperator(QueryParser.Operator.AND);
            return parser.parse(String.format("%s*", queryString.toLowerCase()));
        } catch (ParseException e) {
            LOGGER.debug("Unable to parse query", e);
            throw new IllegalStateException(e.getMessage());
        }

    }

    private Set<String> getObjectTypeLookupKeys(final String fieldType){
        final AttributeType attributeType = AttributeType.getByNameOrNull(fieldType);
        if ( attributeType == null ) {
            throw new IllegalArgumentException("not valid field");
        }

        if (ObjectType.getByNameOrNull(fieldType) != null) {
            return Collections.singleton(fieldType);
        }

        return FluentIterable.from(attributeType.getReferences()).transform(new Function<ObjectType, String>() {
            @Nullable
            @Override
            public String apply(final ObjectType input) {
                return ObjectTemplate.getTemplate(input).getKeyLookupAttribute().getName();
            }
        }).toSet();
    }
}
