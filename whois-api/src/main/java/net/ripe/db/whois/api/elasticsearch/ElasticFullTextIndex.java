package net.ripe.db.whois.api.elasticsearch;

import com.google.common.base.Stopwatch;
import jakarta.annotation.PostConstruct;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.TransactionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

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
            LOGGER.error("ES indexes needs to be rebuild");
        }
    }

    @Scheduled(fixedDelayString = "${fulltext.index.update.interval.msecs:60000}")
    @SchedulerLock(name = TASK_NAME)
    public void scheduledUpdate() {
        if (!elasticIndexService.isEnabled()) {
            LOGGER.error("Elasticsearch is not enabled");
            return;
        }

        LOGGER.info("started scheduled job for  elastic search  indexes");
        try {
            update();
        } catch (DataAccessException | IOException | IllegalStateException e) {
            LOGGER.error("Unable to update fulltext index due to {}: {}", e.getClass(), e.getMessage());
        }

        LOGGER.info("Completed updating Elasticsearch indexes");
    }

    @Transactional(transactionManager = TransactionConfiguration.WHOIS_READONLY_TRANSACTION , isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public void update() throws IOException {
        if (shouldRebuild()) {
            LOGGER.error("ES indexes needs to be rebuild");
            return;
        }

        final ElasticIndexMetadata committedMetadata = elasticIndexService.getMetadata();
        final Map<Integer, Integer> maxSerialIdWithObjectCount = getMaxSerialIdWithObjectCount();
        final int dbMaxSerialId = (Integer) maxSerialIdWithObjectCount.keySet().toArray()[0];

        final int esSerialId = committedMetadata.getSerial();
        if (esSerialId > dbMaxSerialId) {
            LOGGER.error("Seems like ES is ahead of database, this should never have happened. ES max serial id is {} and database max serial id is {}", esSerialId, dbMaxSerialId);
            return;
        }

        if(esSerialId == dbMaxSerialId) {
            LOGGER.info("No database update since last run.ES serial id is {} and database max serial id is {}", esSerialId, dbMaxSerialId);
            return;
        }

        LOGGER.info("Index serial ({}) lower than database serial ({}), updating", esSerialId, dbMaxSerialId);
        final Stopwatch stopwatch = Stopwatch.createStarted();

        for (int serial = esSerialId + 1; serial <= dbMaxSerialId; serial++) {
            final SerialEntry serialEntry = getSerialEntry(serial);
            if (serialEntry == null) {
                // suboptimal;there could be big gaps in serial entries.
                continue;
            }

            final RpslObject rpslObject = serialEntry.getRpslObject();

            switch (serialEntry.getOperation()) {
                case UPDATE -> elasticIndexService.createOrUpdateEntry(rpslObject);
                case DELETE -> elasticIndexService.deleteEntry(rpslObject.getObjectId());
            }
        }

        LOGGER.debug("Updated index in {}", stopwatch.stop());

        elasticIndexService.refreshIndex();

        elasticIndexService.updateMetadata(new ElasticIndexMetadata(dbMaxSerialId, source));

        final int countInDb = (int) maxSerialIdWithObjectCount.values().toArray()[0];
        final long countInES = elasticIndexService.getWhoisDocCount();
        if(countInES != countInDb) {
            LOGGER.error(String.format("Number of objects in DB (%s) does not match to number of objects indexed in ES (%s) for serialId (%s)", countInDb, countInES, dbMaxSerialId));
        }
    }

    private SerialEntry getSerialEntry(final int serial) {
        try {
            return JdbcRpslObjectOperations.getSerialEntry(jdbcTemplate, serial);
        } catch (Exception e) {
            LOGGER.debug("Caught exception reading serial {} from the database, Ignoring", serial, e);
            return null;
        }
    }

    private Map<Integer, Integer> getMaxSerialIdWithObjectCount() {
        try {
            return JdbcRpslObjectOperations.getMaxSerialIdWithObjectCount(jdbcTemplate);
        } catch (Exception e) {
            LOGGER.error("Caught exception reading max serial Id with object count", e);
            throw e;
        }
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
}


