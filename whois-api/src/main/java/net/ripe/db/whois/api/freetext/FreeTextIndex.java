package net.ripe.db.whois.api.freetext;

import com.google.common.base.CharMatcher;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.api.search.RebuildableIndex;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static org.slf4j.LoggerFactory.getLogger;

// TODO: instead of relying on scattered 'if (StringUtils.isBlank(indexDir) {...}', we should use profiles/...
@Component
public class FreeTextIndex extends RebuildableIndex {
    private static final Logger LOGGER = getLogger(FreeTextIndex.class);

    private static final int INDEX_UPDATE_INTERVAL_IN_SECONDS = 60;

    static final String OBJECT_TYPE_FIELD_NAME = "object-type";
    static final String PRIMARY_KEY_FIELD_NAME = "primary-key";
    static final String LOOKUP_KEY_FIELD_NAME = "lookup-key";

    static final Analyzer QUERY_ANALYZER = new FreeTextAnalyzer(FreeTextAnalyzer.Operation.QUERY);
    static final Analyzer INDEX_ANALYZER = new FreeTextAnalyzer(FreeTextAnalyzer.Operation.INDEX);

    static final String[] FIELD_NAMES;

    private static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH, AttributeType.CERTIF, AttributeType.CHANGED, AttributeType.SOURCE), AttributeType.class);

    private static final FieldType INDEXED_AND_TOKENIZED;
    private static final FieldType INDEXED_NOT_TOKENIZED;

    static {
        final List<String> names = newArrayListWithExpectedSize(AttributeType.values().length);
        for (final AttributeType attributeType : AttributeType.values()) {
            if (!SKIPPED_ATTRIBUTES.contains(attributeType)) {
                names.add(attributeType.getName());
            }
        }

        FIELD_NAMES = names.toArray(new String[names.size()]);

        INDEXED_AND_TOKENIZED = new FieldType();
        INDEXED_AND_TOKENIZED.setIndexed(true);
        INDEXED_AND_TOKENIZED.setStored(true);
        INDEXED_AND_TOKENIZED.setTokenized(true);
        INDEXED_AND_TOKENIZED.freeze();

        INDEXED_NOT_TOKENIZED = new FieldType();
        INDEXED_NOT_TOKENIZED.setIndexed(true);
        INDEXED_NOT_TOKENIZED.setStored(true);
        INDEXED_NOT_TOKENIZED.setTokenized(false);
        INDEXED_NOT_TOKENIZED.freeze();
    }

    private final JdbcTemplate jdbcTemplate;
    private final String source;
    private final FacetsConfig facetsConfig;

    @Autowired
    FreeTextIndex(
            @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
            @Value("${whois.source}") final String source,
            @Value("${dir.freetext.index:}") final String indexDir) {

        super(LOGGER, indexDir);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
        this.facetsConfig = new FacetsConfig();
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(indexDir)) return;
        super.init(new IndexWriterConfig(Version.LUCENE_4_10_0, INDEX_ANALYZER)
                        .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND),
                new IndexTemplate.WriteCallback() {
                    @Override
                    public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                        if (indexWriter.numDocs() == 0) {
                            rebuild(indexWriter, taxonomyWriter);
                        } else {
                            final Map<String, String> commitData = indexWriter.getCommitData();
                            final String committedSource = commitData.get("source");

                            if (!source.equals(committedSource)) {
                                LOGGER.warn("Index {} has invalid source: {}, rebuild", indexDir, committedSource);
                                rebuild(indexWriter, taxonomyWriter);
                                return;
                            }

                            if (!commitData.containsKey("serial")) {
                                LOGGER.warn("Index {} is missing serial, rebuild", indexDir);
                                rebuild(indexWriter, taxonomyWriter);
                                return;
                            }
                        }
                    }
                }
        );
    }

    @PreDestroy
    public void destroy() {
        cleanup();
    }

    protected void rebuild(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
        if (StringUtils.isBlank(indexDir)) return;

        indexWriter.deleteAll();
        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();

        // sadly Executors don't offer a bounded/blocking submit() implementation
        int numThreads = Runtime.getRuntime().availableProcessors();
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(numThreads * 64);
        final ExecutorService executorService = new ThreadPoolExecutor(numThreads, numThreads,
                0L, TimeUnit.MILLISECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());

        JdbcStreamingHelper.executeStreaming(jdbcTemplate, "" +
                        "SELECT object_id, object " +
                        "FROM last " +
                        "WHERE sequence_id != 0 ",
                new ResultSetExtractor<Void>() {
                    private static final int LOG_EVERY = 500000;

                    @Override
                    public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        int nrIndexed = 0;

                        while (rs.next()) {
                            executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getBytes(2), indexWriter, taxonomyWriter));

                            if (++nrIndexed % LOG_EVERY == 0) {
                                LOGGER.info("Indexed {} objects", nrIndexed);
                            }
                        }

                        LOGGER.info("Indexed {} objects", nrIndexed);
                        return null;
                    }
                }
        );

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOGGER.error("shutdown", e);
        }

        updateMetadata(indexWriter, source, maxSerial);
    }

    @Scheduled(fixedDelay = INDEX_UPDATE_INTERVAL_IN_SECONDS * 1000)
    public void scheduledUpdate() {
        if (StringUtils.isBlank(indexDir)) return;
        update();
    }

    protected void update(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
        final Map<String, String> metadata = indexWriter.getCommitData();
        final int end = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        final int last = Integer.parseInt(metadata.get("serial"));

        if (last > end) {
            LOGGER.warn("Index serial ({}) higher than database serial ({}), rebuilding", last, end);
            rebuild(indexWriter, taxonomyWriter);
        } else if (last < end) {
            LOGGER.debug("Updating index {} from {} to {}", indexDir, last, end);

            final Stopwatch stopwatch = Stopwatch.createStarted();
            for (int serial = last + 1; serial <= end; serial++) {
                final SerialEntry serialEntry = JdbcRpslObjectOperations.getSerialEntry(jdbcTemplate, serial);
                if (serialEntry == null) {
                    // suboptimal;there could be big gaps in serial entries.
                    continue;
                }
                final RpslObject rpslObject = serialEntry.getRpslObject();

                switch (serialEntry.getOperation()) {
                    case UPDATE:
                        deleteEntry(indexWriter, rpslObject);
                        addEntry(indexWriter, taxonomyWriter, rpslObject);
                        break;
                    case DELETE:
                        deleteEntry(indexWriter, rpslObject);
                        break;
                }
            }

            LOGGER.debug("Updated index {} in {}", indexDir, stopwatch.stop());
        }

        updateMetadata(indexWriter, source, end);
    }

    private void updateMetadata(final IndexWriter indexWriter, final String source, final int serial) {
        final Map<String, String> metadata = Maps.newHashMap();
        metadata.put("serial", Integer.toString(serial));
        metadata.put("source", source);
        indexWriter.setCommitData(metadata);
    }

    private void addEntry(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter, final RpslObject rpslObject) throws IOException {
        final Document document = new Document();
        document.add(new Field(PRIMARY_KEY_FIELD_NAME, Integer.toString(rpslObject.getObjectId()), INDEXED_NOT_TOKENIZED));
        document.add(new Field(OBJECT_TYPE_FIELD_NAME, rpslObject.getType().getName(), INDEXED_AND_TOKENIZED));
        document.add(new Field(LOOKUP_KEY_FIELD_NAME, rpslObject.getKey().toString(), INDEXED_AND_TOKENIZED));

        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            if (!SKIPPED_ATTRIBUTES.contains(attribute.getType())) {
                document.add(new Field(attribute.getKey(), sanitise(attribute.getValue().trim()), INDEXED_AND_TOKENIZED));
            }
        }

        document.add(new FacetField(OBJECT_TYPE_FIELD_NAME, rpslObject.getType().getName()));

        indexWriter.addDocument(facetsConfig.build(taxonomyWriter, document));
    }

    private static String sanitise(final String value) {
        return CharMatcher.JAVA_ISO_CONTROL.removeFrom(value);
    }

    private void deleteEntry(final IndexWriter indexWriter, final RpslObject rpslObject) throws IOException {
        indexWriter.deleteDocuments(new Term(PRIMARY_KEY_FIELD_NAME, Integer.toString(rpslObject.getObjectId())));
    }

    final class DatabaseObjectProcessor implements Runnable {
        final int objectId;
        final byte[] object;
        final IndexWriter indexWriter;
        final TaxonomyWriter taxonomyWriter;

        private DatabaseObjectProcessor(int objectId, byte[] object, IndexWriter indexWriter, TaxonomyWriter taxanomyWriter) {
            this.objectId = objectId;
            this.object = object;
            this.indexWriter = indexWriter;
            this.taxonomyWriter = taxanomyWriter;
        }

        @Override
        public void run() {
            final RpslObject rpslObject;
            try {
                rpslObject = RpslObject.parse(objectId, object);
            } catch (RuntimeException e) {
                LOGGER.warn("Unable to parse object with id: {}", objectId, e);
                return;
            }

            try {
                addEntry(indexWriter, taxonomyWriter, rpslObject);
            } catch (IOException e) {
                throw new IllegalStateException("Indexing", e);
            }
        }
    }
}
