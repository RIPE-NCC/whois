package net.ripe.db.whois.api.wsearch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.api.search.RebuildableIndex;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
public class LogFileIndex extends RebuildableIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileIndex.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    private static final int INDEX_UPDATE_INTERVAL_IN_SECONDS = 300;

    private static final LogFileAnalyzer ANALYZER = new LogFileAnalyzer(Version.LUCENE_41);
    private static final Sort SORT_BY_UPDATE_ID = new Sort(new SortField("updateId", SortField.Type.STRING, true));
    private static final int MAX_RESULTS = 1000;

    private static final FieldType STORED;
    private static final FieldType INDEXED;

    static {
        STORED = new FieldType();
        STORED.setIndexed(false);
        STORED.setStored(true);
        STORED.setTokenized(false);
        STORED.freeze();

        INDEXED = new FieldType();
        INDEXED.setIndexed(true);
        INDEXED.setStored(false);
        INDEXED.setTokenized(true);
        INDEXED.freeze();
    }

    private final File logDir;

    @Autowired
    LogFileIndex(
            @Value("${dir.update.audit.log}") final String logDir,
            @Value("${dir.wsearch.index}") final String indexDir) {
        super(LOGGER, indexDir);

        final File file = new File(logDir);
        if (file.exists()) {
            LOGGER.info("Using log dir: {}", file.getAbsolutePath());
        } else if (file.mkdirs()) {
            LOGGER.warn("Created log dir: {}", file.getAbsolutePath());
        } else {
            throw new IllegalArgumentException(String.format("Unable to create log dir: %s", file.getAbsolutePath()));
        }

        this.logDir = file;
    }

    @PostConstruct
    public void init() {
        super.init(
                new IndexWriterConfig(Version.LUCENE_41, ANALYZER).setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND),

                new IndexTemplate.WriteCallback() {
                    @Override
                    public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    }
                });
    }

    @PreDestroy
    public void destroy() {
        cleanup();
    }

    @Override
    protected void lockedRebuild() throws IOException {
        LOGGER.info("Rebuilding index {}", indexDir);
        final Stopwatch stopwatch = new Stopwatch().start();

        index.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                indexWriter.deleteAll();
                resetMetadata(indexWriter);
            }
        });

        lockedUpdate();

        LOGGER.info("Rebuilt index {} in {}", indexDir, stopwatch.stop());
    }

    @Scheduled(fixedDelay = INDEX_UPDATE_INTERVAL_IN_SECONDS * 1000)
    public void scheduledUpdate() {
        final Thread thread = new Thread("Logfile index updater") {
            @Override
            public void run() {
                update();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void lockedUpdate() throws IOException {
        final Map<String, String> commitData = index.getCommitData();
        final String startDailyLogFolder = commitData.containsKey("dailyLogFolder") ? commitData.get("dailyLogFolder") : "00000000";
        final String startUpdateLogFolder = commitData.containsKey("updateFolder") ? commitData.get("updateFolder") : "000000.0";

        for (final DailyLogFolder dailyLogFolder : DailyLogFolder.getDailyLogFolders(logDir, startDailyLogFolder)) {
            index.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {

                    dailyLogFolder.processLoggedFiles(new DailyLogFolder.LoggedFilesProcessor() {
                        @Override
                        public boolean accept(final LoggedUpdateInfo loggedUpdateInfo) {
                            if (loggedUpdateInfo.getType().equals(LoggedUpdateInfo.Type.AUDIT)) {
                                return false;
                            }

                            final LoggedUpdateId updateId = loggedUpdateInfo.getLoggedUpdateId();
                            return !updateId.getDailyLogFolder().equals(startDailyLogFolder) || startUpdateLogFolder.compareTo(updateId.getUpdateFolder()) < 0;
                        }

                        @Override
                        public void process(final LoggedUpdateInfo loggedUpdateInfo, final String contents) {
                            try {
                                addLoggedUpdate(loggedUpdateInfo, contents, indexWriter, taxonomyWriter);
                                final LoggedUpdateId updateId = loggedUpdateInfo.getLoggedUpdateId();
                                updateMetadata(indexWriter, updateId.getDailyLogFolder(), updateId.getUpdateFolder());
                            } catch (IOException e) {
                                LOGGER.error("Indexing {}", loggedUpdateInfo, e);
                            }
                        }
                    });
                }
            });
        }
    }

    private void addLoggedUpdate(final LoggedUpdateInfo loggedUpdateInfo, final String contents, final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
        LOGGER.debug("Indexing {}", loggedUpdateInfo);

        final Document document = new Document();
        final LoggedUpdateId loggedUpdateId = loggedUpdateInfo.getLoggedUpdateId();
        final String date = loggedUpdateId.getDailyLogFolder();

        document.add(new Field("updateId", loggedUpdateId.toString(), STORED));
        document.add(new Field("date", date, INDEXED));
        document.add(new Field("filename", loggedUpdateInfo.getFilename(), STORED));
        document.add(new Field("type", loggedUpdateInfo.getType().name(), STORED));
        document.add(new Field("contents", contents, INDEXED));

        final CategoryPath categoryPath = new CategoryPath("date", date);
        final FacetFields facetFields = new FacetFields(taxonomyWriter);
        facetFields.addFields(document, Lists.newArrayList(categoryPath));

        indexWriter.addDocument(document);
    }

    private void resetMetadata(final IndexWriter indexWriter) {
        updateMetadata(indexWriter, "00000000", "000000.0");
    }

    private void updateMetadata(final IndexWriter indexWriter, final String dailyLogFolder, final String updateFolder) {
        final Map<String, String> metadata = Maps.newHashMap();
        metadata.put("dailyLogFolder", dailyLogFolder);
        metadata.put("updateFolder", updateFolder);
        indexWriter.setCommitData(metadata);
    }

    Set<LoggedUpdateId> searchLoggedUpdateIds(final String queryString, @Nullable final LocalDate date) throws IOException, ParseException {
        final QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_41, new String[]{"date", "contents"}, LogFileIndex.ANALYZER);
        queryParser.setDefaultOperator(QueryParser.Operator.AND);

        final StringBuilder queryBuilder = new StringBuilder();
        if (date != null) {
            queryBuilder.append("date:").append(DATE_FORMATTER.print(date)).append(' ');
        }

        queryBuilder.append("contents:").append(QueryParser.escape(queryString));

        final Query query = queryParser.parse(queryBuilder.toString());

        return search(new IndexTemplate.SearchCallback<Set<LoggedUpdateId>>() {
            @Override
            public Set<LoggedUpdateId> search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                final int maxResults = Math.max(Math.min(MAX_RESULTS, indexReader.numDocs()), 1);
                final TopFieldCollector topFieldCollector = TopFieldCollector.create(SORT_BY_UPDATE_ID, maxResults, false, false, false, false);

                indexSearcher.search(query, topFieldCollector);

                final TopDocs topDocs = topFieldCollector.topDocs();
                final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

                final Set<LoggedUpdateId> updateIds = Sets.newLinkedHashSetWithExpectedSize(scoreDocs.length);
                for (final ScoreDoc scoreDoc : scoreDocs) {
                    final Document doc = indexSearcher.doc(scoreDoc.doc);
                    updateIds.add(LoggedUpdateId.parse(doc.getField("updateId").stringValue()));
                }

                return updateIds;
            }
        });
    }
}