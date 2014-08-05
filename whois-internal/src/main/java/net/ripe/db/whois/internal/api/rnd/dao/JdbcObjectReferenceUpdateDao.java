package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class JdbcObjectReferenceUpdateDao implements ObjectReferenceUpdateDao {

    public static final Logger LOGGER = LoggerFactory.getLogger(JdbcObjectReferenceUpdateDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcObjectReferenceUpdateDao(@Qualifier("whoisUpdateMasterDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void createVersion(final ObjectVersion objectVersion) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO object_version (pkey, object_type, from_timestamp, to_timestamp, revision) VALUES (?, ?, ?, ?, ?)",
                    objectVersion.getPkey(),
                    ObjectTypeIds.getId(objectVersion.getType()),
                    objectVersion.getFromDate().getMillis() / 1000,
                    objectVersion.getToDate() == null ? null : (objectVersion.getToDate().getMillis() / 1000),
                    objectVersion.getRevision());
        } catch (DuplicateKeyException | UncategorizedSQLException e) {
            LOGGER.debug("Duplicate version pkey={} type={} revision={}", objectVersion.getPkey(), objectVersion.getType(), objectVersion.getRevision());
        }
    }

    @Override
    public void createReference(final ObjectVersion from, final ObjectVersion to) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO object_reference (from_version, to_version) VALUES (?, ?)",
                    from.getVersionId(),
                    to.getVersionId());
        } catch (DuplicateKeyException | UncategorizedSQLException e) {
            LOGGER.debug("Duplicate reference from={} to={}", from.getVersionId(), to.getVersionId());
        }
    }

    @Override
    public void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp) {
        jdbcTemplate.update(
                "UPDATE object_version SET to_timestamp = ? WHERE pkey = ? AND object_type = ? AND from_timestamp = ? AND revision = ?",
                endTimestamp,
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                objectVersion.getFromDate().getMillis() / 1000,
                objectVersion.getRevision());
    }

    @Override
    public List<ObjectVersion> findIncomingReferences(final ObjectVersion objectVersion) {
        try {
            return jdbcTemplate.query(
                    "SELECT v.id, v.object_type, v.pkey, v.from_timestamp, v.to_timestamp, v.revision FROM object_reference r " +
                    "JOIN object_version v ON v.id = r.from_version " +
                    "WHERE r.to_version = ?",
                    new Object[]{objectVersion.getVersionId()},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<ObjectVersion> getVersions(final long fromTimestamp) {
        try {
            return jdbcTemplate.query(
                    "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
                    "WHERE from_timestamp >= ? " +
                    "ORDER BY from_timestamp ASC",
                    new Object[]{fromTimestamp},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType) {
        try {
            return jdbcTemplate.query(
                    "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
                    "WHERE pkey = ? AND object_type = ? " +
                    "ORDER BY from_timestamp,to_timestamp ASC",
                    new Object[]{
                        pkey,
                        ObjectTypeIds.getId(objectType)},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType, final DateTime fromDate, final DateTime toDate) {
        try {
            if (toDate == null) {
                // open-ended range
                return jdbcTemplate.query(
                        "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
                                "WHERE pkey = ? AND object_type = ? " +
                                "AND from_timestamp <= ? " +
                                "ORDER BY from_timestamp,to_timestamp ASC",
                        new Object[]{
                                pkey,
                                ObjectTypeIds.getId(objectType),
                                fromDate.getMillis() / 1000
                        },
                        new ObjectVersionRowMapper());
            } else {
                // closed range
                return jdbcTemplate.query(
                        "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
                                "WHERE pkey = ? AND object_type = ? " +
                                "AND from_timestamp <= ? AND (to_timestamp >= ? OR to_timestamp IS NULL) " +
                                "ORDER BY from_timestamp,to_timestamp ASC",
                        new Object[]{
                                pkey,
                                ObjectTypeIds.getId(objectType),
                                fromDate.getMillis() / 1000,
                                toDate.getMillis() / 1000
                        },
                        new ObjectVersionRowMapper());
            }
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    // TODO: [ES] duplicates ObjectReferenceDao
    @Override
    public ObjectVersion getVersion(final ObjectType type, final String pkey, final int revision) {
        return jdbcTemplate.queryForObject(
                "SELECT " +
                "  id, " +
                "  object_type, " +
                "  pkey, " +
                "  from_timestamp, " +
                "  to_timestamp," +
                "  revision " +
                "FROM object_version " +
                "WHERE object_type = ? " +
                "  AND pkey = ? " +
                "  AND revision = ? " +
                "ORDER BY id DESC",
                new ObjectVersionRowMapper(),
                ObjectTypeIds.getId(type),
                pkey,
                revision);
    }

    @Override
    public long getLatestVersionTimestamp() {
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
            LOGGER.warn("Unable to determine latest timestamp, updating ALL object versions...");
            return 0;
        }
    }

    class ObjectVersionRowMapper implements RowMapper<ObjectVersion> {
        @Override
        public ObjectVersion mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ObjectVersion(
                    rs.getLong(1),                          // id
                    ObjectTypeIds.getType(rs.getInt(2)),    // object_type
                    rs.getString(3),                        // pkey
                    rs.getLong(4),                          // from_timestamp
                    rs.getLong(5),                          // to_timestamp
                    rs.getInt(6)                            // revision
            );
        }
    }
}
