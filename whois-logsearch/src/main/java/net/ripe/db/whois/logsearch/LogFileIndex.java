package net.ripe.db.whois.logsearch;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.logsearch.logformat.LoggedUpdate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@Component
public class LogFileIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileIndex.class);

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");

    private static final Analyzer INDEX_ANALYZER = new LogFileAnalyzer(Version.LUCENE_41);
    private static final Analyzer QUERY_ANALYZER = new LogSearchQueryAnalyzer(Version.LUCENE_41);
    private static final Sort SORT_BY_DATE = new Sort(new SortField("date", SortField.Type.STRING, true));
    private final int resultLimit;

    private static final FieldType STORED;
    private static final FieldType INDEXED_TOKENIZED;
    private static final FieldType INDEXED_STORED;
    private static final FieldType INDEXED_TOKENIZED_STORED;
    private static final FieldType INDEXED_TOKENIZED_STORED_INTEGER;

    static {
        STORED = new FieldType();
        STORED.setIndexed(false);
        STORED.setTokenized(false);
        STORED.setStored(true);
        STORED.freeze();

        INDEXED_TOKENIZED = new FieldType();
        INDEXED_TOKENIZED.setIndexed(true);
        INDEXED_TOKENIZED.setTokenized(true);
        INDEXED_TOKENIZED.setStored(false);
        INDEXED_TOKENIZED.freeze();

        INDEXED_STORED = new FieldType();
        INDEXED_STORED.setIndexed(true);
        INDEXED_STORED.setTokenized(false);
        INDEXED_STORED.setStored(true);
        INDEXED_STORED.freeze();

        INDEXED_TOKENIZED_STORED = new FieldType();
        INDEXED_TOKENIZED_STORED.setIndexed(true);
        INDEXED_TOKENIZED_STORED.setTokenized(true);
        INDEXED_TOKENIZED_STORED.setStored(true);
        INDEXED_TOKENIZED_STORED.freeze();

        INDEXED_TOKENIZED_STORED_INTEGER = new FieldType(INDEXED_TOKENIZED_STORED);
        INDEXED_TOKENIZED_STORED_INTEGER.setNumericType(FieldType.NumericType.INT);
        INDEXED_TOKENIZED_STORED_INTEGER.freeze();
    }

    protected IndexTemplate index;

    @Autowired
    LogFileIndex(
            @Value("${dir.logsearch.index}") final String indexDir,
            @Value("${logsearch.result.limit}") final int resultLimit) {

        try {
            final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41, INDEX_ANALYZER).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            index = new IndexTemplate(indexDir, config);
            this.resultLimit = resultLimit;
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Initializing index in %s", indexDir), e);
        }
    }

    @PreDestroy
    public void destroy() {
        IOUtils.closeQuietly(index);
    }

    public void update(IndexTemplate.WriteCallback writeCallback) {
        try {
            index.write(writeCallback);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void addToIndex(final LoggedUpdate loggedUpdate, final String contents, final IndexWriter indexWriter) {
        try {
            LOGGER.debug("Indexing {}", loggedUpdate);
            indexWriter.deleteDocuments(new Term("updateId", loggedUpdate.getUpdateId()));

            final Document document = new Document();
            // TODO: [AH] do we need to store updateId? Index should be enough
            document.add(new Field("updateId", loggedUpdate.getUpdateId(), INDEXED_STORED));
            // TODO: [AH] Why tokenize date? Why store date?
            document.add(new IntField("date", Integer.parseInt(loggedUpdate.getDate()), INDEXED_TOKENIZED_STORED_INTEGER));
            // TODO: [AH] type could be derived from updateId
            document.add(new Field("type", loggedUpdate.getType().name(), STORED));
            document.add(new Field("contents", contents, INDEXED_TOKENIZED));
            indexWriter.addDocument(document);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeAllByIdPrefix(final String updateIdPrefix) {
        update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                indexWriter.deleteDocuments(new PrefixQuery(new Term("date", updateIdPrefix)));
            }
        });
    }

    public void removeAllByDate(final LocalDate date) {
        update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                indexWriter.deleteDocuments(createDateQuery(date, null));
            }
        });
    }

    public void removeAll() {
        update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(IndexWriter indexWriter, TaxonomyWriter taxonomyWriter) throws IOException {
                indexWriter.deleteAll();
                indexWriter.getCommitData().clear();
            }
        });
    }

    //
    //   searching
    //

    public Set<LoggedUpdate> searchByUpdateId(final String regex) {
        final Set<LoggedUpdate> loggedUpdates = search(new RegexpQuery(new Term("updateId", regex)));
        LOGGER.debug("Found {} updates matching regex {}", loggedUpdates.size(), regex);
        return loggedUpdates;
    }

    public Set<LoggedUpdate> searchByDate(final LocalDate date) {
        return search(createDateQuery(date, null));
    }

    public Set<LoggedUpdate> searchByDate(final String date) {
        return searchByDates(Collections.singleton(date));
    }

    public Set<LoggedUpdate> searchByDates(final Iterable<String> dates) {
        return search(createDateQuery(dates));
    }

    public Set<LoggedUpdate> searchByDateRangeAndContent(final String queryString, @Nullable final LocalDate fromDate, @Nullable final LocalDate toDate) {
        final BooleanQuery query = new BooleanQuery();

        if ((fromDate != null) || (toDate != null)) {
            query.add(createDateQuery(fromDate, toDate), BooleanClause.Occur.MUST);
        }

        if (!StringUtils.isEmpty(queryString)) {
            query.add(createContentQuery(queryString), BooleanClause.Occur.MUST);
        }

        return search(query);
    }

    private Set<LoggedUpdate> search(final Query query) {
        try {
            return index.search(new IndexTemplate.SearchCallback<Set<LoggedUpdate>>() {
                @Override
                public Set<LoggedUpdate> search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                    LOGGER.debug("executing lucene query: {}", query);
                    final int maxResults = Math.max(Math.min(LogFileIndex.this.resultLimit, indexReader.numDocs()), 1);
                    final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_DATE, maxResults, false, false, false, false);

                    indexSearcher.search(query, topFieldCollector);

                    LOGGER.debug("total hits: {} from total documents: {}", topFieldCollector.getTotalHits(), indexReader.numDocs());
                    final TopDocs topDocs = topFieldCollector.topDocs();
                    final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                    final Set<LoggedUpdate> loggedUpdates = Sets.newLinkedHashSetWithExpectedSize(scoreDocs.length);
                    for (final ScoreDoc scoreDoc : scoreDocs) {
                        final Document doc = indexReader.document(scoreDoc.doc);
                        loggedUpdates.add(
                                LoggedUpdate.parse(
                                        doc.getField("updateId").stringValue(),
                                        doc.getField("date").stringValue(),
                                        LoggedUpdate.Type.valueOf(doc.getField("type").stringValue())));
                    }

                    return loggedUpdates;
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Query createContentQuery(@Nullable final String queryString) {
        try {
            final QueryParser queryParser = new QueryParser(Version.LUCENE_41, "contents", LogFileIndex.QUERY_ANALYZER);
            return queryParser.parse(QueryParser.escape(queryString));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Query createDateQuery(@Nullable final LocalDate fromDate, @Nullable final LocalDate toDate) {
        final Integer from = (fromDate != null) ? Integer.parseInt(DATE_FORMATTER.print(fromDate)) : null;
        final Integer to = (toDate != null) ? Integer.parseInt(DATE_FORMATTER.print(toDate)) : null;

        if (from != null && to != null) {
            return NumericRangeQuery.newIntRange("date", Math.min(from, to), Math.max(from, to), true, true);
        }

        if (from != null) {
            return NumericRangeQuery.newIntRange("date", from, from, true, true);
        }

        if (to != null) {
            return NumericRangeQuery.newIntRange("date", to, to, true, true);
        }

        throw new IllegalStateException("both dates null");
    }

    private Query createDateQuery(final Iterable<String> dates) {
        final BooleanQuery booleanQuery = new BooleanQuery();

        for (String date : dates) {
            final int term = Integer.parseInt(date);
            booleanQuery.add(NumericRangeQuery.newIntRange("date", term, term, true, true), BooleanClause.Occur.SHOULD);
        }

        return booleanQuery;
    }
}
