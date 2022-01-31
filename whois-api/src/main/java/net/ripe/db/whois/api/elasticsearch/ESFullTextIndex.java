package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.Stopwatch;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.elasticsearch.IndexMetadata;
import net.ripe.db.whois.common.elasticsearch.IndexService;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
public class ESFullTextIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESFullTextIndex.class);
    private IndexService indexService;
    private final JdbcTemplate jdbcTemplate;
    private final String source;
    private final String TASK_NAME = "fulltextIndexUpdate";

    public ESFullTextIndex(IndexService indexService,
                           @Qualifier("whoisSlaveDataSource") final DataSource dataSource,
                           @Value("${whois.source}") final String source) {
        this.indexService = indexService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.source = source;
    }

    @PostConstruct
    public void init() throws IOException {
        if (!indexService.isEnabled()) {
            return;
        }

        if(indexService.getMetadata() == null) {
            LOGGER.warn("Invalid Metadata");
            rebuild();
        }

        if (indexService.getWhoisDocCount() == 0L) {
            rebuild();
        } else {
            final IndexMetadata committedMetadata = indexService.getMetadata();
            if (!source.equals(committedMetadata.getSource())) {
                LOGGER.warn("Index has invalid source: {}, rebuild", committedMetadata.getSource());
                rebuild();
                return;
            }

            if (committedMetadata.getSerial() == null) {
                LOGGER.warn("Index is missing serial, rebuild");
                rebuild();
            }
        }
    }

    @Scheduled(fixedDelayString = "${fulltext.index.update.interval.msecs:60000}")
    @SchedulerLock(name = TASK_NAME)
    public void scheduledUpdate() {
        if (!indexService.isEnabled()) {
            LOGGER.info("ES not enabled");
            return;
        }

        LOGGER.info("started scheduled job for  elastic search  indexes");

        try {
            update();
        } catch (DataAccessException e) {
            LOGGER.warn("Unable to update fulltext index due to {}: {}", e.getClass(), e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void update() throws IOException {
        final int end = JdbcRpslObjectOperations.getSerials(jdbcTemplate).getEnd();
        final int last = indexService.getMetadata().getSerial();
        if (last > end) {
            rebuild();
        } else if (last < end) {
            LOGGER.debug("Updating index from {} to {}", last, end);
            LOGGER.warn("Index serial ({}) higher than database serial ({}), rebuilding", last, end);
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
                        indexService.deleteEntry(rpslObject.getObjectId());
                        indexService.addEntry(rpslObject);
                        break;
                    case DELETE:
                        indexService.deleteEntry(rpslObject.getObjectId());
                        break;
                }
            }

            LOGGER.debug("Updated index in {}", stopwatch.stop());

        }

        indexService.updateMetadata(new IndexMetadata(end, source));
    }

    private void rebuild() throws IOException {
        if (!indexService.isEnabled()) {
            LOGGER.warn("ES not enabled");
            return;
        }
        LOGGER.info("Rebuilding elastic search indexes");

        indexService.deleteAll();
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

        indexService.updateMetadata(new IndexMetadata(maxSerial, source));
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
                indexService.addEntry(rpslObject);
            } catch (IOException e) {
                throw new IllegalStateException("Indexing", e);
            }
        }
    }
}


