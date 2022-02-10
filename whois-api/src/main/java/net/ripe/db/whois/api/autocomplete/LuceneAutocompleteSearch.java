package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.fulltextsearch.FullTextAnalyzer;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Conditional(LuceneSearchCondition.class)
public class LuceneAutocompleteSearch implements  AutoCompleteSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneAutocompleteSearch.class);

    // results will always be sorted by lookup key (which is case sensitive, and by string value)
    private static final Sort SORT_BY_LOOKUP_KEY = new Sort(SortField.FIELD_SCORE, new SortField(FullTextIndex.LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));

    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*");

    private static final int MAX_SEARCH_RESULTS = 10;

    private final FullTextIndex fullTextIndex;

    private final RpslObjectDao objectDao;

    @Autowired
    public LuceneAutocompleteSearch(final FullTextIndex fullTextIndex, @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao) {
        this.fullTextIndex = fullTextIndex;
        this.objectDao = rpslObjectDao;
    }

    public List<Map<String, Object>> search(
        final String queryString,                       // search value
        final Set<AttributeType> queryAttributes,       // attribute(s) to search in
        final Set<AttributeType> responseAttributes,    // attribute(s) to return
        final Set<ObjectType> objectTypes)              // filter by object type(s)
            throws IOException {                        // TODO: wrap IOException, return something sensible
        return fullTextIndex.search(
            (final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) -> {
                final List<Map<String, Object>> results = Lists.newArrayList();

                final Query query;
                try {
                    if (objectTypes != null && !objectTypes.isEmpty()) {
                        query = new BooleanQuery.Builder()
                                .add(constructQuery(queryAttributes, queryString), BooleanClause.Occur.MUST)
                                .add(constructQuery(objectTypes), BooleanClause.Occur.MUST)
                                .build();
                    } else {
                        query = constructQuery(queryAttributes, queryString);
                    }
                } catch (ParseException e) {
                    // TODO: [ES] fix parsing of asterisk (wildcard) as first character
                    LOGGER.info("Caught {} on {}", e.getMessage(), queryString);
                    return Collections.emptyList();
                }

                final TopFieldDocs topDocs = indexSearcher.search(query, MAX_SEARCH_RESULTS, SORT_BY_LOOKUP_KEY, true);

                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    final Map<String, Object> result = Maps.newLinkedHashMap();
                    final RpslObject rpslObject;

                    try {
                        rpslObject =   objectDao.getByKey(
                                            ObjectType.getByName(doc.get(FullTextIndex.OBJECT_TYPE_FIELD_NAME)),
                                            doc.get(FullTextIndex.LOOKUP_KEY_FIELD_NAME)
                                        );

                    } catch (EmptyResultDataAccessException ex) {
                        LOGGER.info("seems like object has been deleted from database");
                        continue;
                    }

                    result.put("key", rpslObject.getKey().toString());
                    result.put("type", rpslObject.getType().getName());

                    for (final AttributeType attribute : responseAttributes) {
                        final ObjectTemplate template = ObjectTemplate.getTemplate(rpslObject.getType());

                        if (template.getMultipleAttributes().contains(attribute)) {
                            result.put(attribute.getName(), filterValues(attribute, rpslObject));
                        } else {
                            result.put(attribute.getName(), filterValue(attribute, rpslObject.containsAttribute(attribute) ? rpslObject.findAttribute(attribute).getValue() : null));
                        }
                    }

                    results.add(result);
                }

                return results;
        });
    }

    @Nullable
    private String filterValue(final AttributeType type, final String attributeValue) {
        return attributeValue == null ? null : COMMENT_PATTERN.matcher( fullTextIndex.filterRpslAttribute(type, attributeValue)).replaceFirst("").trim();
    }

    private List<String> filterValues(final AttributeType attributeType, final RpslObject rpslObject) {
        return rpslObject.findAttributes(attributeType).stream().map( (attribute) -> filterValue(attributeType, attribute.getValue())).collect(Collectors.toList());
    }

    // query by attribute(s)
    private Query constructQuery(final Set<AttributeType> queryAttributes, final String queryString) throws ParseException {
        final Set<String> queryAttributeNames = queryAttributes.stream().map(attributeType -> attributeType.getName()).collect(Collectors.toSet());

        final MultiFieldQueryParser parser = new MultiFieldQueryParser(
                                                        queryAttributeNames.toArray(new String[queryAttributeNames.size()]),
                                                        new FullTextAnalyzer(FullTextAnalyzer.Operation.QUERY));
        parser.setAnalyzer(FullTextIndex.QUERY_ANALYZER);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query likeQuery = parser.parse(String.format("%s*", normalise(queryString)));

        final Query exactMatchQuery = new BoostQuery(new TermQuery(new Term(FullTextIndex.LOOKUP_KEY_FIELD_NAME, normalise(queryString))), 2);

        return new BooleanQuery.Builder()
                .add(exactMatchQuery, BooleanClause.Occur.SHOULD)
                .add(likeQuery, BooleanClause.Occur.SHOULD)
                .build();
    }

    private String normalise(final String queryString) {
        return QueryParser.escape(queryString).toLowerCase();
    }

    // query by object type
    private Query constructQuery(final Set<ObjectType> objectTypes) {
        final BooleanQuery.Builder builder = new BooleanQuery.Builder();

        for (ObjectType objectType : objectTypes) {
            builder.add(
                new TermQuery(
                    new Term(FullTextIndex.OBJECT_TYPE_FIELD_NAME, objectType.getName())),
                    BooleanClause.Occur.SHOULD);
        }

        return builder.build();
    }

}
