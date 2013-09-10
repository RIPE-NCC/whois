package net.ripe.db.whois.api.freetext;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
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
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class FreeTextIndex extends RebuildableIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeTextIndex.class);

    private static final int INDEX_UPDATE_INTERVAL_IN_SECONDS = 60;

    static final String OBJECT_TYPE_FIELD_NAME = "object-type";
    static final String PRIMARY_KEY_FIELD_NAME = "primary-key";
    static final String LOOKUP_KEY_FIELD_NAME = "lookup-key";

    static final Analyzer QUERY_ANALYZER = new FreeTextAnalyzer(Version.LUCENE_41, FreeTextAnalyzer.Operation.QUERY);
    static final Analyzer INDEX_ANALYZER = new FreeTextAnalyzer(Version.LUCENE_41, FreeTextAnalyzer.Operation.INDEX);

    static final String[] FIELD_NAMES;

    private static final Set<AttributeType> SKIPPED_ATTRIBUTES = Sets.newEnumSet(Sets.newHashSet(AttributeType.AUTH, AttributeType.CERTIF, AttributeType.CHANGED, AttributeType.SOURCE), AttributeType.class);

    private static final FieldType INDEXED_AND_TOKENIZED;
    private static final FieldType INDEXED_NOT_TOKENIZED;

    static {
        final List<String> names = Lists.newArrayListWithExpectedSize(AttributeType.values().length);
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

    public static final int MAX_UPDATE_BACKLOG = 25000;

    private final JdbcTemplate jdbcTemplate;
    private final String source;

    @Autowired
    FreeTextIndex(
            @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
            @Value("${whois.source}") final String source,
            @Value("${dir.freetext.index:}") final String indexDir) {

        super(LOGGER, indexDir);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(indexDir)) return;
        super.init(new IndexWriterConfig(Version.LUCENE_41, INDEX_ANALYZER)
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
                            }

                            if (!commitData.containsKey("serial")) {
                                LOGGER.warn("Index {} is missing serial, rebuild", indexDir);
                                rebuild(indexWriter, taxonomyWriter);
                            }

                            final int indexSerial = Integer.parseInt(commitData.get("serial"));
                            final int serial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
                            if (serial < indexSerial) {
                                LOGGER.warn("Index serial ({}) higher than database serial ({}), rebuild", indexSerial, serial);
                                rebuild(indexWriter, taxonomyWriter);
                            }

                            if (serial - MAX_UPDATE_BACKLOG > indexSerial) {
                                LOGGER.warn("Index serial ({}) too far behind database serial ({}), rebuild", indexSerial, serial);
                                rebuild(indexWriter, taxonomyWriter);
                            }
                        }
                    }
                });
    }

    @PreDestroy
    public void destroy() {
        cleanup();
    }

    protected void rebuild(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
        if (StringUtils.isBlank(indexDir)) return;

        indexWriter.deleteAll();
        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();

        JdbcStreamingHelper.executeStreaming(jdbcTemplate, "" +
                "SELECT object_id, object " +
                "FROM last " +
                "WHERE sequence_id != 0 " +
                "AND object_type != 100 ",
                new ResultSetExtractor<Void>() {
                    private static final int LOG_EVERY = 500000;

                    @Override
                    public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
                        int nrIndexed = 0;

                        while (rs.next()) {
                            final int objectId = rs.getInt(1);

                            final RpslObject object;
                            try {
                                object = RpslObject.parse(objectId, rs.getBytes(2));
                            } catch (RuntimeException e) {
                                LOGGER.warn("Unable to parse object with id: {}", objectId, e);
                                continue;
                            }

                            try {
                                addEntry(indexWriter, taxonomyWriter, object);

                                if (++nrIndexed % LOG_EVERY == 0) {
                                    LOGGER.info("Indexed {} objects", nrIndexed);
                                }
                            } catch (IOException e) {
                                throw new IllegalStateException("Indexing", e);
                            }
                        }

                        LOGGER.info("Indexed {} objects", nrIndexed);
                        return null;
                    }
                });


        updateMetadata(indexWriter, source, maxSerial);
    }

    @Scheduled(fixedDelay = INDEX_UPDATE_INTERVAL_IN_SECONDS * 1000)
    public void scheduledUpdate() {
        update();
    }

    protected void update(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
        if (StringUtils.isBlank(indexDir)) return;

        final Map<String, String> metadata = indexWriter.getCommitData();
        final int end = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        final int last = Integer.parseInt(metadata.get("serial"));

        if (last > end) {
            LOGGER.warn("Index serial ({}) higher than database serial ({}), rebuilding", last, end);
            rebuild(indexWriter, taxonomyWriter);
        } else if (last < end) {
            LOGGER.debug("Updating index {} to {}", indexDir, end);

            final Stopwatch stopwatch = new Stopwatch().start();
            for (int serial = last + 1; serial <= end; serial++) {
                final SerialEntry serialEntry = JdbcRpslObjectOperations.getById(jdbcTemplate, serial);
                if (serialEntry == null) {
                    // TODO: [AH] suboptimal; there could be big gaps in serial entries. we should have a getNextId() call instead, SELECT()ing on serial_id > serial
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
                document.add(new Field(attribute.getKey(), attribute.getValue().trim(), INDEXED_AND_TOKENIZED));
            }
        }

        final CategoryPath categoryPath = new CategoryPath(OBJECT_TYPE_FIELD_NAME, rpslObject.getType().getName());
        final FacetFields facetFields = new FacetFields(taxonomyWriter);
        facetFields.addFields(document, Lists.newArrayList(categoryPath));

        indexWriter.addDocument(document);
    }

    private void deleteEntry(final IndexWriter indexWriter, final RpslObject rpslObject) throws IOException {
        indexWriter.deleteDocuments(new Term(PRIMARY_KEY_FIELD_NAME, Integer.toString(rpslObject.getObjectId())));
    }
}
