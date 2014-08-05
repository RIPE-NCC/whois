package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.internal.api.rnd.ReferenceStreamHandler;
import net.ripe.db.whois.internal.api.rnd.ReferenceType;
import net.ripe.db.whois.internal.api.rnd.VersionsStreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.internal.api.rnd.ReferenceType.INCOMING;
import static net.ripe.db.whois.internal.api.rnd.ReferenceType.OUTGOING;

// TODO: [ES] add update master data source
@Repository
public class JdbcObjectReferenceDao implements ObjectReferenceDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcObjectReferenceDao(final SourceAwareDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // versions

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
    public List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType) {
        try {
            return jdbcTemplate.query(
                    "SELECT id, object_type, pkey, from_timestamp, to_timestamp, revision FROM object_version " +
                    "WHERE pkey = ? AND object_type = ? " +
                    "ORDER BY from_timestamp,to_timestamp ASC",
                    new Object[]{pkey, ObjectTypeIds.getId(objectType)},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    // get versions of a specific object within a time range
    @Override
    public List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType, final long fromTimestamp, final long toTimestamp) {
        try {
            if (toTimestamp == 0) {
                // open-ended range
                return jdbcTemplate.query(
                        "SELECT id, object_type, pkey, from_timestamp, to_timestamp, revision FROM object_version " +
                        "WHERE pkey = ? AND object_type = ? " +
                        "AND from_timestamp >= ? " +
                        "ORDER BY from_timestamp,to_timestamp ASC",
                        new Object[]{
                            pkey, ObjectTypeIds.getId(objectType), fromTimestamp
                        },
                        new ObjectVersionRowMapper());
            } else {
                // closed range
                return jdbcTemplate.query(
                        "SELECT id, object_type, pkey, from_timestamp, to_timestamp, revision FROM object_version " +
                        "WHERE pkey = ? AND object_type = ? " +
                        "AND from_timestamp <= ? AND (to_timestamp >= ? OR to_timestamp = NULL) " +
                        "ORDER BY from_timestamp,to_timestamp ASC",
                        new Object[]{
                            pkey, ObjectTypeIds.getId(objectType), fromTimestamp, toTimestamp
                        },
                        new ObjectVersionRowMapper());
            }
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    // get all object versions from a given timestamp onwards
    @Override
    public List<ObjectVersion> getVersions(final long fromTimestamp) {
        try {
            return jdbcTemplate.query(
                    "SELECT id, object_type, pkey, from_timestamp, to_timestamp, revision FROM object_version " +
                    "WHERE from_timestamp >= ? " +
                    "ORDER BY from_timestamp ASC",
                    new Object[]{fromTimestamp},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void streamVersions(final String pkey, final ObjectType objectType, final VersionsStreamHandler versionsStreamHandler) {
        JdbcStreamingHelper.executeStreaming(
                jdbcTemplate,
                "SELECT id, object_type, pkey, from_timestamp, to_timestamp, revision FROM object_version " +
                        "WHERE pkey = ? AND object_type = ? " +
                        "ORDER BY from_timestamp,to_timestamp ASC",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(final PreparedStatement ps) throws SQLException {
                        ps.setString(1, pkey);
                        ps.setInt(2, ObjectTypeIds.getId(objectType));
                    }
                }, new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        versionsStreamHandler.streamObjectVersion(createObjectVersion(rs));
                    }
                });
    }

    @Override
    public void createVersion(final ObjectVersion objectVersion) {
        jdbcTemplate.update(
                "INSERT INTO object_version (pkey, object_type, from_timestamp, to_timestamp, revision) VALUES (?, ?, ?, ?, ?)",
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                convertToTimestamp(objectVersion.getFromDate()),
                convertToTimestamp(objectVersion.getToDate()),
                objectVersion.getRevision());
    }

    @Override
    public void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp) {
        final int rows = jdbcTemplate.update(
                "UPDATE object_version SET to_timestamp = ? WHERE pkey = ? AND object_type = ? AND from_timestamp = ? AND revision = ?",
                endTimestamp,
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                convertToTimestamp(objectVersion.getFromDate()),
                objectVersion.getRevision());
        if (rows != 1) {
            throw new IllegalStateException("Updated " + rows + " rows for UPDATE operation on " + objectVersion.toString());
        }
    }

    @Override
    public void deleteVersion(final ObjectVersion objectVersion) {
        final int rows = jdbcTemplate.update(
                "DELETE FROM object_version WHERE " +
                "pkey = ? AND object_type = ? AND from_timestamp = ? AND to_timestamp = ? AND revision = ?",
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                convertToTimestamp(objectVersion.getFromDate()),
                convertToTimestamp(objectVersion.getToDate()),
                objectVersion.getRevision());
        if (rows != 1) {
            throw new IllegalStateException("Updated " + rows + " rows for DELETE operation on " + objectVersion.toString());
        }
    }

    @Override
    public void streamIncoming(final ObjectVersion focusObjectVersion, final ReferenceStreamHandler streamHandler) {
        final String query = "" +
                "SELECT " +
                "  v.id, " +
                "  v.object_type, " +
                "  v.pkey, " +
                "  v.from_timestamp, " +
                "  v.to_timestamp, " +
                "  v.revision " +
                "FROM object_version v " +
                "INNER JOIN object_reference ref " +
                "  ON v.id = ref.from_version " +
                "WHERE ref.to_version = ? " +
                "ORDER BY v.id ASC ";
        streamReference(INCOMING, query, focusObjectVersion, streamHandler);
    }

    @Override
    public void streamOutgoing(final ObjectVersion focusObjectVersion, final ReferenceStreamHandler streamHandler) {
        final String query = "" +
                "SELECT " +
                "  v.id, " +
                "  v.object_type, " +
                "  v.pkey, " +
                "  v.from_timestamp, " +
                "  v.to_timestamp, " +
                "  v.revision " +
                "FROM object_version v " +
                "INNER JOIN object_reference ref " +
                "  ON v.id = ref.to_version " +
                "WHERE ref.from_version = ? " +
                "ORDER BY v.id ASC ";
        streamReference(OUTGOING, query, focusObjectVersion, streamHandler);
    }

    private void streamReference(final ReferenceType referenceType, final String query, final ObjectVersion objectVersion, final ReferenceStreamHandler handler) {
        JdbcStreamingHelper.executeStreaming(
                jdbcTemplate,
                query,
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(final PreparedStatement ps) throws SQLException {
                        ps.setLong(1, objectVersion.getVersionId());
                    }
                },
                new RowCallbackHandler() {
                    @Override
                    public void processRow(final ResultSet rs) throws SQLException {
                        handler.streamReference(referenceType, createObjectVersion(rs));
                    }
                }
        );
    }

    // references

    @Override
    public void createReference(final ObjectVersion from, final ObjectVersion to) {
        jdbcTemplate.update(
                "INSERT INTO object_reference (from_version, to_version) VALUES (?, ?)",
                from.getVersionId(),
                to.getVersionId());
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
    public List<ObjectVersion> findOutgoingReferences(final ObjectVersion objectVersion) {
        try {
            return jdbcTemplate.query(
                    "SELECT v.id, v.object_type, v.pkey, v.from_timestamp, v.to_timestamp, v.revision FROM object_reference r " +
                    "JOIN object_version v ON v.id = r.to_version " +
                    "WHERE r.from_version = ?",
                    new Object[]{objectVersion.getVersionId()},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    // helper methods

    @Nullable
    private static Long convertToTimestamp(final DateTime dateTime) {
        if (dateTime == null) {
            return null;        // by convention, an unspecified date is represented by NULL
        } else {
            return dateTime.getMillis() / 1000;     // database timestamp has seconds resolution
        }
    }

    class ObjectVersionRowMapper implements RowMapper<ObjectVersion> {
        @Override
        public ObjectVersion mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return createObjectVersion(rs);
        }
    }

    private static ObjectVersion createObjectVersion(final ResultSet rs) throws SQLException {
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
