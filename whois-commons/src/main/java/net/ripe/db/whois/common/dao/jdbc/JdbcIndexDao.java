package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class JdbcIndexDao implements IndexDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcIndexDao.class);

    private static final Set<AttributeType> ALL_ATTRIBUTES = Sets.newEnumSet(Lists.newArrayList(AttributeType.values()), AttributeType.class);

    private final static int BATCH_SIZE = 100;
    private final static int LOG_EVERY = 100_000;

    private final JdbcTemplate jdbcTemplate;
    private final UpdateLockDao updateLockDao;

    private enum Phase {KEYS, OTHER}

    @Autowired
    JdbcIndexDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final UpdateLockDao updateLockDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.updateLockDao = updateLockDao;
    }

    @Override
    public void rebuild() {
        deleteIndexesForMissingObjects();

        final List<Integer> objectIds = jdbcTemplate.queryForList("SELECT object_id FROM last WHERE sequence_id != 0", Integer.class);
        rebuildForObjects(objectIds, Phase.KEYS);
        rebuildForObjects(objectIds, Phase.OTHER);
    }

    private void rebuildForObjects(final List<Integer> objectIds, final Phase phase) {
        final List<List<Integer>> objectIdBatches = Lists.partition(objectIds, BATCH_SIZE);

        final Stopwatch stopwatch = new Stopwatch().start();

        int count = 0;
        for (final List<Integer> objectIdBatch : objectIdBatches) {
            rebuildIndexes(objectIdBatch, phase);

            count += objectIdBatch.size();
            if (count % LOG_EVERY == 0) {
                LOGGER.info("Rebuilt {} indexes for {} objects in {}", phase, count, stopwatch);
            }
        }

        if (count % LOG_EVERY != 0) {
            LOGGER.info("Rebuilt {} indexes for {} objects in {}", phase, count, stopwatch);
        }
    }

    @Override
    public void rebuildForObject(final int objectId) {
        final Set<Integer> objectIds = Collections.singleton(objectId);
        rebuildIndexes(objectIds, Phase.KEYS);
        rebuildIndexes(objectIds, Phase.OTHER);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    private void rebuildIndexes(final Iterable<Integer> objectIds, final Phase phase) {
        updateLockDao.setUpdateLock();

        for (final Integer objectId : objectIds) {
            try {
                final RpslObject rpslObject = jdbcTemplate.queryForObject(
                        "SELECT object_id, object FROM last WHERE object_id = ? AND sequence_id != 0",
                        new RpslObjectRowMapper(),
                        objectId);

                final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());
                final Set<AttributeType> keyAttributes = objectTemplate.getKeyAttributes();
                final Set<AttributeType> updateAttributes = Phase.KEYS.equals(phase) ? keyAttributes : Sets.difference(ALL_ATTRIBUTES, keyAttributes);

                for (final AttributeType attributeType : updateAttributes) {
                    updateAttributeIndex(rpslObject, attributeType);
                }
            } catch (EmptyResultDataAccessException e) {
                LOGGER.debug("Missing: {}", objectId);
            } catch (RuntimeException e) {
                LOGGER.error("Rebuilding indexes: {}", objectId, e);
            }
        }
    }

    private void updateAttributeIndex(final RpslObject rpslObject, final AttributeType attributeType) {
        final RpslObjectInfo rpslObjectInfo = new RpslObjectInfo(rpslObject.getObjectId(), rpslObject.getType(), rpslObject.getKey());
        final IndexStrategy indexStrategy = IndexStrategies.get(attributeType);

        indexStrategy.removeFromIndex(jdbcTemplate, rpslObjectInfo);

        final Set<CIString> uniqueValues = Sets.newHashSet();
        final List<RpslAttribute> attributes = rpslObject.findAttributes(attributeType);
        for (final RpslAttribute attribute : attributes) {
            for (final CIString value : attribute.getReferenceValues()) {
                if (uniqueValues.add(value)) {
                    try {
                        indexStrategy.addToIndex(jdbcTemplate, rpslObjectInfo, rpslObject, value.toString());
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("Missing reference for {}: {}", rpslObjectInfo, value);
                    }
                }
            }
        }
    }

    private void deleteIndexesForMissingObjects() {
        for (final AttributeType attributeType : AttributeType.values()) {
            deleteIndexForMissingObjects(attributeType);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    private void deleteIndexForMissingObjects(final AttributeType attributeType) {
        updateLockDao.setUpdateLock();

        try {
            final Stopwatch stopwatch = new Stopwatch().start();
            IndexStrategies.get(attributeType).cleanupMissingObjects(jdbcTemplate);
            LOGGER.info("Removed {} indexes for missing objects in {}", attributeType, stopwatch);
        } catch (RuntimeException e) {
            LOGGER.error("Remove {} indexes for missing objects", attributeType, e);
        }
    }
}
