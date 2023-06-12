package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.Stopwatch;
import jakarta.annotation.PostConstruct;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO [DA] Lucene implementation has some mechanism around thread safety. check if that is also necessary
@Component
public class ElasticFullTextIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFullTextIndex.class);

    private static final String TASK_NAME = "elasticFulltextIndexUpdate";

    private final ElasticIndexService elasticIndexService;
    private final JdbcTemplate jdbcTemplate;
    private final String source;

    @Autowired
    public ElasticFullTextIndex(final ElasticIndexService elasticIndexService,
                                @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
                                @Value("${whois.source}") final String source) {
        this.elasticIndexService = elasticIndexService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
    }

    @PostConstruct
    public void init() {
        if (elasticIndexService.isEnabled() && shouldRebuild()) {
            try {
                rebuild();
            } catch (IOException | IllegalStateException ex) {
                LOGGER.info("Failed to update the ES indexes {}", ex.getMessage());
            }
        }
    }

    @Scheduled(fixedDelayString = "${fulltext.index.update.interval.msecs:60000}")
    @SchedulerLock(name = TASK_NAME)
    public void scheduledUpdate() {
        if (!elasticIndexService.isEnabled()) {
            LOGGER.warn("Elasticsearch is not enabled");
            return;
        }

        LOGGER.info("started scheduled job for  elastic search  indexes");

        try {
            update();
        } catch (DataAccessException | IOException | IllegalStateException e) {
            LOGGER.warn("Unable to update fulltext index due to {}: {}", e.getClass(), e.getMessage());
        }

        LOGGER.info("Completed updating Elasticsearch indexes");
    }

    protected void update() throws IOException {
        if (shouldRebuild()) {
            rebuild();
            return;
        }

        final ElasticIndexMetadata committedMetadata = elasticIndexService.getMetadata();
        final int end = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        final int last = committedMetadata.getSerial();
        if (last > end) {
            rebuild();
        } else if (last < end) {
            LOGGER.debug("Updating index from {} to {}", last, end);
            LOGGER.info("Index serial ({}) lower than database serial ({}), updating", last, end);
            final Stopwatch stopwatch = Stopwatch.createStarted();

            for (int serial = last + 1; serial <= end; serial++) {
                final SerialEntry serialEntry = getSerialEntry(serial);
                if (serialEntry == null) {
                    // suboptimal;there could be big gaps in serial entries.
                    continue;
                }

                final RpslObject rpslObject = serialEntry.getRpslObject();

                switch (serialEntry.getOperation()) {
                    case UPDATE:
                        //indexService.deleteEntry(rpslObject.getObjectId());
                        elasticIndexService.addEntry(rpslObject);
                        break;
                    case DELETE:
                        elasticIndexService.deleteEntry(rpslObject.getObjectId());
                        break;
                }
            }
            LOGGER.debug("Updated index in {}", stopwatch.stop());
        }

        elasticIndexService.updateMetadata(new ElasticIndexMetadata(end, source));
    }

    private SerialEntry getSerialEntry(int serial) {
        try {
            return JdbcRpslObjectOperations.getSerialEntry(jdbcTemplate, serial);
        } catch (Exception e) {
            LOGGER.debug("Caught exception reading serial {} from the database, Ignoring", serial, e);
            return null;
        }
    }

    private void rebuild() throws IOException {
        if (!elasticIndexService.isEnabled()) {
            LOGGER.info("Elasticsearch not enabled");
            return;
        }
        LOGGER.info("Rebuilding Elasticsearch indexes");

        elasticIndexService.deleteAll();
        final int maxSerial = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();

        // sadly Executors don't offer a bounded/blocking submit() implementation
        final int numThreads = Runtime.getRuntime().availableProcessors();
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
                            executorService.submit(new DatabaseObjectProcessor(rs.getInt(1), rs.getBytes(2)));
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

        elasticIndexService.updateMetadata(new ElasticIndexMetadata(maxSerial, source));
        LOGGER.info("Completed Rebuilding Elasticsearch indexes");
    }

    private boolean shouldRebuild() {
        try {
            if (elasticIndexService.getWhoisDocCount() == 0L) {
                LOGGER.warn("Whois index count is zero, rebuilding");
                return true;
            }

            final ElasticIndexMetadata committedMetadata = elasticIndexService.getMetadata();
            if (committedMetadata == null || committedMetadata.getSerial() == null) {
                LOGGER.warn("Index has invalid or null source, rebuild");
                return true;
            }

            if (committedMetadata.getSerial() == 0) {
                LOGGER.warn("Index is missing serial, rebuild");
                return true;
            }
        } catch (IOException | IllegalStateException ex) {
            LOGGER.info("Failed to check if ES index needs rebuilding {}", ex.getMessage());
        }
        return false;
    }

    final class DatabaseObjectProcessor implements Runnable {
        final int objectId;
        final byte[] object;

        private DatabaseObjectProcessor(final int objectId, final byte[] object) {
            this.objectId = objectId;
            this.object = object;
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
                elasticIndexService.addEntry(rpslObject);
            } catch (IOException e) {
                throw new IllegalStateException("Indexing", e);
            }
        }
    }
}
