package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.freetext.FreeTextAnalyzer;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AutocompleteSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutocompleteSearch.class);

    // results will always be sorted by lookup key (which is case sensitive, and by string value)
    private static final Sort SORT_BY_LOOKUP_KEY = new Sort(new SortField(FreeTextIndex.LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));

    private static final int MAX_SEARCH_RESULTS = 10;

    private final FreeTextIndex freeTextIndex;

    @Autowired
    public AutocompleteSearch(final FreeTextIndex freeTextIndex) {
        this.freeTextIndex = freeTextIndex;
    }

    public List<Map<String, Object>> search(
        final String queryString,                       // search value
        final Set<AttributeType> queryAttributes,       // attribute(s) to search in
        final Set<AttributeType> responseAttributes,    // attribute(s) to return
        final Set<ObjectType> objectTypes)              // filter by object type(s)
            throws IOException {                        // TODO: wrap IOException, return something sensible
        return freeTextIndex.search(
            (final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) -> {
                final List<Map<String, Object>> results = Lists.newArrayList();

                final Query query;
                try {
                    if (objectTypes != null && !objectTypes.isEmpty()) {
                        query = combine(constructQuery(queryAttributes, queryString), constructQuery(objectTypes));
                    } else {
                        query = constructQuery(queryAttributes, queryString);
                    }
                } catch (ParseException e) {
                    // TODO: [ES] fix parsing of asterisk (wildcard) as first character
                    LOGGER.info("Caught {} on {}", e.getMessage(), queryString);
                    return Collections.<Map<String, Object>>emptyList();
                }

                final TopFieldDocs topDocs = indexSearcher.search(query, MAX_SEARCH_RESULTS, SORT_BY_LOOKUP_KEY);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    final Map<String, Object> result = Maps.newLinkedHashMap();
                    result.put("key", doc.get(FreeTextIndex.LOOKUP_KEY_FIELD_NAME));
                    result.put("type", doc.get(FreeTextIndex.OBJECT_TYPE_FIELD_NAME));

                    for (final AttributeType attribute : responseAttributes) {
                        final ObjectTemplate template = ObjectTemplate.getTemplate(
                                ObjectType.getByName(doc.get(FreeTextIndex.OBJECT_TYPE_FIELD_NAME)));

                        if (template.getMultipleAttributes().contains(attribute)) {
                            result.put(attribute.getName(), Lists.newArrayList(doc.getValues(attribute.getName())));
                        } else {
                            result.put(attribute.getName(), doc.get(attribute.getName()));
                        }
                    }

                    results.add(result);
                }

                return results;
        });
    }

    // query by attribute(s)
    private Query constructQuery(final Set<AttributeType> queryAttributes, final String queryString) throws ParseException {
        final Set<String> queryAttributeNames = queryAttributes.stream().map(attributeType -> attributeType.getName()).collect(Collectors.toSet());

        final MultiFieldQueryParser parser = new MultiFieldQueryParser(
                                                        queryAttributeNames.toArray(new String[queryAttributeNames.size()]),
                                                        new FreeTextAnalyzer(FreeTextAnalyzer.Operation.QUERY));
        parser.setAnalyzer(FreeTextIndex.QUERY_ANALYZER);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        return parser.parse(String.format("%s*", normalise(queryString)));
    }

    private String normalise(final String queryString) {
        return QueryParser.escape(queryString).toLowerCase();
    }

    // query by object type
    private Query constructQuery(final Set<ObjectType> objectTypes) {
        final BooleanQuery result = new BooleanQuery();

        for (ObjectType objectType : objectTypes) {
            result.add(
                new TermQuery(
                    new Term(FreeTextIndex.OBJECT_TYPE_FIELD_NAME, objectType.getName())),
                    BooleanClause.Occur.SHOULD);
        }

        return result;
    }

    private Query combine(final Query ... queries) {
        final BooleanQuery result = new BooleanQuery();

        for (Query query : queries) {
            result.add(query, BooleanClause.Occur.MUST);
        }

        return result;
    }
}
