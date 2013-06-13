package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.deleteFromTables;
import static net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations.insertIntoTablesIgnoreMissing;

@Component
public class JdbcIndexDao implements IndexDao {
    private final static Logger LOGGER = LoggerFactory.getLogger(JdbcIndexDao.class);

    private final static int BATCH_SIZE = 100;
    private final static int LOG_EVERY = 100_000;

    private final JdbcTemplate jdbcTemplate;
    private final UpdateLockDao updateLockDao;

    @Autowired
    JdbcIndexDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final UpdateLockDao updateLockDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.updateLockDao = updateLockDao;
    }

    @Override
    public void rebuild() {
        deleteIndexesForMissingObjects();

        final List<Integer> objectIds = jdbcTemplate.queryForList("SELECT object_id FROM last WHERE sequence_id != 0", Integer.class);
        final List<List<Integer>> objectIdBatches = Lists.partition(objectIds, BATCH_SIZE);

        final Stopwatch stopwatch = new Stopwatch().start();

        int count = 0;
        for (final List<Integer> objectIdBatch : objectIdBatches) {
            rebuildIndexes(objectIdBatch);

            count += objectIdBatch.size();
            if (count % LOG_EVERY == 0) {
                LOGGER.info("Rebuilt indexes for {} objects in {}", count, stopwatch);
            }
        }

        if (count % LOG_EVERY != 0) {
            LOGGER.info("Rebuilt indexes for {} objects in {}", count, stopwatch);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    private void rebuildIndexes(final Iterable<Integer> objectIds) {
        updateLockDao.setUpdateLock();

        for (final Integer objectId : objectIds) {
            try {
                final RpslObject rpslObject = jdbcTemplate.queryForObject(
                        "SELECT object_id, object FROM last WHERE object_id = ? AND sequence_id != 0",
                        new RpslObjectRowMapper(),
                        objectId);

                final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(rpslObject.getObjectId(), rpslObject.getType(), rpslObject.getKey());

                deleteFromTables(jdbcTemplate, rpslObjectInfo);
                final Set<CIString> missingReferences = insertIntoTablesIgnoreMissing(jdbcTemplate, rpslObjectInfo, rpslObject);
                if (!missingReferences.isEmpty()) {
                    LOGGER.warn("Missing references for object {}: {}", rpslObjectInfo, missingReferences);
                }
            } catch (EmptyResultDataAccessException e) {
                LOGGER.debug("Missing: {}", objectId);
            } catch (RuntimeException e) {
                LOGGER.error("Rebuilding indexes: {}", objectId, e);
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    private void deleteIndexesForMissingObjects() {
        updateLockDao.setUpdateLock();

        for (final AttributeType attributeType : AttributeType.values()) {
            try {
                final Stopwatch stopwatch = new Stopwatch().start();
                IndexStrategies.get(attributeType).cleanupMissingObjects(jdbcTemplate);
                LOGGER.info("Removed {} indexes for missing objects in {}", attributeType, stopwatch);
            } catch (RuntimeException e) {
                LOGGER.error("Remove {} indexes for missing objects", attributeType, e);
            }
        }
    }
}
