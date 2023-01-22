package net.ripe.db.whois.api.rdap;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.autocomplete.LuceneSearchCondition;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.fulltextsearch.IndexTemplate;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.List;

import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.LOOKUP_KEY_FIELD_NAME;

@Component
@Conditional(LuceneSearchCondition.class)
public class RdapLuceneFullTextSearchService implements RdapFullTextSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapLuceneFullTextSearchService.class);

    private final int maxResultSize;
    private final RpslObjectDao objectDao;
    private final AccessControlListManager accessControlListManager;
    private final FullTextIndex fullTextIndex;

    //sort for consistent search results
    private static final Sort SORT_BY_OBJECT_TYPE =
            new Sort(new SortField(FullTextIndex.OBJECT_TYPE_FIELD_NAME, SortField.Type.STRING), new SortField(LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));

    @Autowired
    public RdapLuceneFullTextSearchService(@Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao objectDao,
                                           final FullTextIndex fullTextIndex,
                                           final AccessControlListManager accessControlListManager,
                                           @Value("${rdap.search.max.results:100}") final int maxResultSize) {
        this.objectDao = objectDao;
        this.fullTextIndex = fullTextIndex;
        this.accessControlListManager = accessControlListManager;
        this.maxResultSize = maxResultSize;
    }

    @Override
    public List<RpslObject> performSearch(final String[] fields, final String term, final String remoteAddr, final Source source) throws IOException {
        LOGGER.debug("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw new BadRequestException("empty search term");
        }

        return fullTextIndex.search(
                new IndexTemplate.AccountingSearchCallback<>(accessControlListManager, remoteAddr, source) {

                    @Override
                    protected List<RpslObject> doSearch(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException {
                        final Stopwatch stopWatch = Stopwatch.createStarted();

                        final List<RpslObject> results = Lists.newArrayList();
                        try {
                            final QueryParser queryParser = new MultiFieldQueryParser(fields, new RdapLuceneFullTextSearchService.RdapAnalyzer());
                            queryParser.setAllowLeadingWildcard(true);
                            queryParser.setDefaultOperator(QueryParser.Operator.AND);

                            // TODO SB: Yuck, query is case insensitive by default
                            // but case sensitivity also depends on field type
                            final org.apache.lucene.search.Query query = queryParser.parse(term.toLowerCase());

                            final TopDocs topDocs = indexSearcher.search(query, maxResultSize, SORT_BY_OBJECT_TYPE);
                            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                                final Document document = indexSearcher.doc(scoreDoc.doc);

                                final RpslObject rpslObject;
                                try {
                                    rpslObject = objectDao.getById(getObjectId(document));
                                } catch (EmptyResultDataAccessException e) {
                                    // object was deleted from the database but index was not updated yet
                                    continue;
                                }
                                account(rpslObject);
                                results.add(rpslObject);
                            }

                            LOGGER.debug("Found {} objects in {}", results.size(), stopWatch.stop());
                            return results;

                        } catch (ParseException e) {
                            LOGGER.error("handleSearch", e);
                            throw new BadRequestException("cannot parse query " + term);
                        }
                    }
                }
        );
    }

    private class RdapAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(final String fieldName) {
            final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
            TokenStream tok = new WordDelimiterGraphFilter(
                    tokenizer,
                    WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
                    CharArraySet.EMPTY_SET);
            tok = new LowerCaseFilter(tok);
            return new TokenStreamComponents(tokenizer, tok);
        }
    }
}
