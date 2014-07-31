package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class UpdateObjectVersions {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateObjectVersions.class);

    private final ObjectReferenceDao objectReferenceDao;
    private final JdbcTemplate jdbcTemplate;

    private final AtomicBoolean inProgress = new AtomicBoolean();

    @Autowired
    public UpdateObjectVersions(
            final ObjectReferenceDao objectReferenceDao,
            @Qualifier("whoisReadOnlySlaveDataSource") final DataSource dataSource) {
        this.objectReferenceDao = objectReferenceDao;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // periodically update the object_version and object_reference tables
    @Scheduled(fixedDelay = 60 * 1000)
    @Transactional // TODO - consider two separate transactional methods (1) update versions (2) update references
    public void run() {
        if (!inProgress.compareAndSet(false, true)) {
            LOGGER.info("run already in progress, returning...");
        }

        try {
            final long timestamp = getMaxTimestamp();

            updateObjectVersionsTable(timestamp);
            updateOutgoingReferences(timestamp);
            updateIncomingReferences(timestamp);

        } finally {
            if (!inProgress.compareAndSet(true, false)) {
                LOGGER.warn("inProgress unexpectedly false?");
            }
        }
    }

    private void updateObjectVersionsTable(final long timestamp) {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        final List<ObjectData> updates = Lists.newArrayList();

        jdbcTemplate.query(
                    "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM history WHERE timestamp > ?) " +
                    "UNION ALL " +
                    "(SELECT pkey, object_type, timestamp, object_id, sequence_id FROM last WHERE timestamp > ?)",
                new Object[] {timestamp, timestamp},
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        try {
                            final String pkey = rs.getString(1);
                            final ObjectType type = ObjectTypeIds.getType(rs.getInt(2));
                            final long timestamp = rs.getLong(3);
                            final int objectId = rs.getInt(4);
                            final int sequenceId = rs.getInt(5);        // if 0 then delete event

                            updates.add(new ObjectData(pkey, type, timestamp, objectId, sequenceId));
                        } catch (IllegalArgumentException e) {
                            // ignore - invalid object type
                        }
                    }
                });

        Collections.sort(updates);

        for (final ObjectData objectData : updates) {

            final List<ObjectVersion> versions = objectReferenceDao.getVersions(objectData.pkey, objectData.objectType);

            if (objectData.sequenceId == 0) {
                // delete - add end timestamp to existing version
                if (!versions.isEmpty()) {
                    final ObjectVersion previousVersion = versions.get(versions.size() - 1);
                    objectReferenceDao.updateVersionToTimestamp(previousVersion, objectData.timestamp);
                }
            } else {
                if (!versions.isEmpty()) {
                    // update - add end timestamp to existing version
                    final ObjectVersion previousVersion = versions.get(versions.size() - 1);
                    objectReferenceDao.updateVersionToTimestamp(previousVersion, objectData.timestamp);
                }

                // create new version
                final ObjectVersion newVersion = new ObjectVersion(
                        0,
                        objectData.objectType,
                        objectData.pkey,
                        objectData.timestamp,
                        Long.MAX_VALUE,
                        versions.isEmpty() ? 1 : versions.get(versions.size() - 1).getRevision() + 1);
                objectReferenceDao.createVersion(newVersion);
            }
        }

        LOGGER.info("Updated object_versions table in {}", stopwatch.stop());
    }

    private void updateOutgoingReferences(final long timestamp) {
        // TODO
    }

    private void updateIncomingReferences(final long timestamp) {
        // TODO
    }

    // get timestamp of the most recent object version
    // TODO: time consuming call
    private long getMaxTimestamp() {
        try {
            return jdbcTemplate.queryForObject(
                        "SELECT greatest(max(from_timestamp),max(to_timestamp)) FROM object_version",
                    new RowMapper<Long>() {
                        @Override
                        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getLong(1);
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            return 0L;
        }
    }

    ///

    private static class ObjectData implements Comparable<ObjectData> {
        private final String pkey;
        private final ObjectType objectType;
        private final long timestamp;
        private final int objectId;
        private final int sequenceId;

        public ObjectData(final String pkey, final ObjectType objectType, final long timestamp, final int objectId, final int sequenceId) {
            this.pkey = pkey;
            this.objectType = objectType;
            this.timestamp = timestamp;
            this.objectId = objectId;
            this.sequenceId = sequenceId;
        }

        @Override
        public int compareTo(ObjectData o) {
            final int c1 = Long.compare(timestamp, o.timestamp);
            if (c1 != 0) {
                // order by date
                return c1;
            }

            final int c2 = Integer.compare(objectId, o.objectId);
            if (c2 != 0) {
                // order by objectId
                return c2;
            }

            // order by sequenceId
            return Integer.compare(sequenceId, o.sequenceId);
        }
    }
}
