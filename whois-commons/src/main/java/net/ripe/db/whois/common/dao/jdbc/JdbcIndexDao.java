package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategies;
import net.ripe.db.whois.common.dao.jdbc.index.IndexStrategy;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ConcurrentState;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JdbcIndexDao implements IndexDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcIndexDao.class);

    private final static int BATCH_SIZE = 100;
    private final static int LOG_EVERY = 100_000;

    private final JdbcTemplate jdbcTemplate;
    private final UpdateLockDao updateLockDao;
    private final AttributeSanitizer attributeSanitizer;
    private final ConcurrentState state;

    private enum Phase {KEYS, OTHER}

    @Autowired
    JdbcIndexDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource, final UpdateLockDao updateLockDao, final AttributeSanitizer attributeSanitizer) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.updateLockDao = updateLockDao;
        this.attributeSanitizer = attributeSanitizer;
        this.state = new ConcurrentState();
    }

    @Override
    public void rebuild() {
        deleteIndexesForMissingObjects();

        final List<Integer> objectIds = jdbcTemplate.queryForList("SELECT object_id FROM last WHERE sequence_id != 0", Integer.class);
        rebuildForObjects(objectIds, Phase.KEYS);
        rebuildForObjects(objectIds, Phase.OTHER);
    }

    @Override
    public void pause() {
        state.set(false);
    }

    @Override
    public void resume() {
        state.set(true);
    }

    private void rebuildForObjects(final List<Integer> objectIds, final Phase phase) {
        final List<List<Integer>> objectIdBatches = Lists.partition(objectIds, BATCH_SIZE);

        final Stopwatch stopwatch = new Stopwatch().start();
        state.set(true);

        int count = 0;
        for (final List<Integer> objectIdBatch : objectIdBatches) {
            state.waitUntil(true);

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
                final Map<String, Object> map = jdbcTemplate.queryForMap(
                        "SELECT object_id, object, pkey FROM last " +
                        "WHERE object_id = ? " +
                        "AND sequence_id != 0 ",
                        objectId);
                RpslObject rpslObject = RpslObject.parse(((Long)map.get("object_id")).intValue(), (byte[])map.get("object"));
                final String pkey = (String)map.get("pkey");

                if (phase == Phase.KEYS) {
                    rpslObject = sanitizeObject(rpslObject, pkey);
                }

                final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());
                final Set<AttributeType> keyAttributes = objectTemplate.getKeyAttributes();
                final Set<AttributeType> otherAttributes = Sets.newHashSet();
                otherAttributes.addAll(objectTemplate.getInverseLookupAttributes());
                otherAttributes.addAll(objectTemplate.getLookupAttributes());
                otherAttributes.removeAll(keyAttributes);

                final Set<AttributeType> updateAttributes = Phase.KEYS.equals(phase) ? keyAttributes : otherAttributes;

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
        for (final RpslAttribute attribute : rpslObject.findAttributes(attributeType)) {
            for (final CIString value : attribute.getReferenceValues()) {
                if (uniqueValues.add(value)) {
                    if (!attribute.getType().isValidValue(rpslObject.getType(), value)) {
                        LOGGER.info("Invalid value {} type {} (object id {})", value, rpslObject.getType(), rpslObject.getObjectId());
                        continue;
                    }

                    try {
                        indexStrategy.addToIndex(jdbcTemplate, rpslObjectInfo, rpslObject, value.toString());
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("Missing reference for {}: {}", rpslObjectInfo, value);
                    }
                }
            }
        }
    }

    private RpslObject sanitizeObject(final RpslObject rpslObject, final String pkey) {
        final RpslObject sanitizedObject = attributeSanitizer.sanitize(rpslObject, new ObjectMessages());

        final CIString sanitizedPKey = sanitizedObject.getKey();
        if (sanitizedPKey.equals(rpslObject.getKey()) &&
                sanitizedPKey.toString().equals(pkey)) {
            return rpslObject;
        }

        LOGGER.info("Updating {} object from {} to {}", rpslObject.getType(), rpslObject.getKey(), sanitizedPKey);

        final int rows = jdbcTemplate.update("UPDATE last SET pkey = ?, object = ? WHERE object_id = ?",
                sanitizedObject.getKey(),
                sanitizedObject.toByteArray(),
                rpslObject.getObjectId());
        if (rows != 1) {
            throw new DataIntegrityViolationException("Unexpected rows:" + rows + " when updating object:" + rpslObject.getObjectId());
        }

        return sanitizedObject;
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
