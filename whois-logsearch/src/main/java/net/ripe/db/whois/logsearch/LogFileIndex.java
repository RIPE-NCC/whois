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
import org.apache.lucene.index.*;
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

    private static final Analyzer INDEX_ANALYZER = new LogFileAnalyzer(Version.LUCENE_44);
    private static final Analyzer QUERY_ANALYZER = new LogSearchQueryAnalyzer(Version.LUCENE_44);
    private static final Sort SORT_BY_DATE = new Sort(new SortField("date", SortField.Type.STRING, true));
    private static final double INDEX_WRITER_RAM_BUFFER_SIZE = 16d;

    private static final FieldType UPDATE_ID_FIELD_TYPE;
    private static final FieldType DATE_FIELD_TYPE;
    private static final FieldType CONTENTS_FIELD_TYPE;

    static {
        UPDATE_ID_FIELD_TYPE = new FieldType();
        UPDATE_ID_FIELD_TYPE.setIndexed(true);
        UPDATE_ID_FIELD_TYPE.setTokenized(false);
        UPDATE_ID_FIELD_TYPE.setStored(true);
        UPDATE_ID_FIELD_TYPE.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        UPDATE_ID_FIELD_TYPE.freeze();

        DATE_FIELD_TYPE = new FieldType();
        DATE_FIELD_TYPE.setNumericType(FieldType.NumericType.INT);
        DATE_FIELD_TYPE.setIndexed(true);
        DATE_FIELD_TYPE.setTokenized(false);
        DATE_FIELD_TYPE.setStored(true);
        DATE_FIELD_TYPE.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        DATE_FIELD_TYPE.freeze();

        CONTENTS_FIELD_TYPE = new FieldType();
        CONTENTS_FIELD_TYPE.setIndexed(true);
        CONTENTS_FIELD_TYPE.setTokenized(true);
        CONTENTS_FIELD_TYPE.setStored(false);
        CONTENTS_FIELD_TYPE.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        CONTENTS_FIELD_TYPE.freeze();
    }

    private final int resultLimit;
    private final IndexTemplate index;

    @Autowired
    LogFileIndex(
            @Value("${dir.logsearch.index}") final String indexDir,
            @Value("${logsearch.result.limit:-1}") final int resultLimit) {

        try {
            final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, INDEX_ANALYZER);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            config.setRAMBufferSizeMB(INDEX_WRITER_RAM_BUFFER_SIZE);
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
            indexWriter.deleteDocuments(new Term("updateId", loggedUpdate.getUpdateId()));
            final Document document = new Document();
            document.add(new Field("updateId", loggedUpdate.getUpdateId(), UPDATE_ID_FIELD_TYPE));
            document.add(new IntField("date", Integer.parseInt(loggedUpdate.getDate()), DATE_FIELD_TYPE));
            document.add(new Field("contents", contents, CONTENTS_FIELD_TYPE));
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
                try {
                    LOGGER.info("Removing all documents by date {}", DATE_FORMATTER.print(date));
                    indexWriter.deleteDocuments(createDateQuery(date, null));
                    LOGGER.info("Remove complete.");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw e;
                }
            }
        });
    }

    public void removeAll() {
        update(new IndexTemplate.WriteCallback() {
            @Override
            public void write(IndexWriter indexWriter, TaxonomyWriter taxonomyWriter) throws IOException {
                indexWriter.deleteAll();
            }
        });
    }

    //
    //   searching
    //

    public Set<LoggedUpdate> searchByUpdateId(final String regex) {
        final Set<LoggedUpdate> loggedUpdates = search(new RegexpQuery(new Term("updateId", regex)), 1_000_000);
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
        return search(query, resultLimit);
    }

    private Set<LoggedUpdate> search(final Query query, final int maxResults) {
        try {
            return index.search(new IndexTemplate.SearchCallback<Set<LoggedUpdate>>() {
                @Override
                public Set<LoggedUpdate> search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                    LOGGER.debug("executing lucene query: {}", query);

                    final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_DATE, getResultLimit(maxResults, indexReader.numDocs()), false, false, false, false);

                    indexSearcher.search(query, topFieldCollector);

                    LOGGER.debug("Matched documents: {} from total documents: {}", topFieldCollector.getTotalHits(), indexReader.numDocs());
                    final TopDocs topDocs = topFieldCollector.topDocs();

                    final Set<LoggedUpdate> loggedUpdates = Sets.newLinkedHashSetWithExpectedSize(topDocs.scoreDocs.length);
                    for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        final Document doc = indexSearcher.doc(scoreDoc.doc);
                        loggedUpdates.add(
                                LoggedUpdate.parse(
                                        doc.getField("updateId").stringValue(),
                                        doc.getField("date").stringValue()));
                    }

                    return loggedUpdates;
                }

                private int getResultLimit(final int maxResults, final int numDocs) {
                    if (numDocs == 0) {
                        return 1;
                    }
                    return (maxResults < 1) ? numDocs : Math.min(maxResults, numDocs);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Query createContentQuery(@Nullable final String queryString) {
        try {
            final QueryParser queryParser = new QueryParser(Version.LUCENE_44, "contents", LogFileIndex.QUERY_ANALYZER);
            queryParser.setDefaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.AND);
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
