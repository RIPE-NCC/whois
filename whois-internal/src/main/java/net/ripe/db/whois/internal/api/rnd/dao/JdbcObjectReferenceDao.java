package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.internal.api.rnd.StreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class JdbcObjectReferenceDao implements ObjectReferenceDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcObjectReferenceDao(final SourceAwareDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

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
                    "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
                    "WHERE pkey = ? AND object_type = ? " +
                    "ORDER BY from_timestamp,to_timestamp ASC",
                    new Object[]{pkey, ObjectTypeIds.getId(objectType)},
                    new ObjectVersionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void streamVersions(final String pkey, final ObjectType objectType, final StreamHandler streamHandler) {
        JdbcStreamingHelper.executeStreaming(
                jdbcTemplate,
                "SELECT id,object_type,pkey,from_timestamp,to_timestamp,revision FROM object_version " +
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
                        final ObjectVersion objectVersion = new ObjectVersion(
                                rs.getLong(1),                          // id
                                ObjectTypeIds.getType(rs.getInt(2)),    // object_type
                                rs.getString(3),                        // pkey
                                rs.getLong(4),                          // from_timestamp
                                rs.getLong(5) == 0 ? Long.MAX_VALUE : rs.getLong(5),     //to_timestamp
                                rs.getInt(6)                           // revision
                        );
                        streamHandler.handleStreamEvent(objectVersion);
                    }
                });
    }

    @Override
    public void createVersion(final ObjectVersion objectVersion) {
        jdbcTemplate.update(
                "INSERT INTO object_version (pkey, object_type, from_timestamp, to_timestamp, revision) VALUES (?, ?, ?, ?, ?)",
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                getFromTimestamp(objectVersion.getInterval()),
                getToTimestamp(objectVersion.getInterval()),
                objectVersion.getRevision());
    }

    @Override
    public void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp) {
        final int rows = jdbcTemplate.update(
                "UPDATE object_version SET to_timestamp = ? WHERE pkey = ? AND object_type = ? AND from_timestamp = ? AND revision = ?",
                endTimestamp,
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                getFromTimestamp(objectVersion.getInterval()),
                objectVersion.getRevision());
        if (rows != 1) {
            throw new IllegalStateException("Updated " + rows + " rows for " + objectVersion.toString());
        }
    }

    @Override
    public void deleteVersion(final ObjectVersion objectVersion) {
        final int rows = jdbcTemplate.update(
                "DELETE FROM object_version WHERE " +
                "pkey = ? AND object_type = ? AND from_timestamp = ? AND to_timestamp = ? AND revision = ?",
                objectVersion.getPkey(),
                ObjectTypeIds.getId(objectVersion.getType()),
                getFromTimestamp(objectVersion.getInterval()),
                getToTimestamp(objectVersion.getInterval()),
                objectVersion.getRevision());
        if (rows != 1) {
            throw new IllegalStateException("Updated " + rows + " rows for " + objectVersion.toString());
        }
    }

    @Override
    public List<ObjectVersion> getIncoming(final ObjectVersion focusObjectVersion) {
        return jdbcTemplate.query(""+
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
                "ORDER BY v.id ASC ",
                new ObjectVersionRowMapper(),
                focusObjectVersion.getVersionId());
    }

    @Override
    public List<ObjectVersion> getOutgoing(final ObjectVersion focusObjectVersion) {
        return jdbcTemplate.query(""+
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
                "ORDER BY v.id ASC ",
                new ObjectVersionRowMapper(),
                focusObjectVersion.getVersionId());
    }

    private static long getFromTimestamp(final Interval interval) {
        return interval.getStart().getMillis() / 1000L;
    }

    private static long getToTimestamp(final Interval interval) {
        if (interval.getStart().equals(interval.getEnd())) {
            // by convention, an interval with Long.MAX_VALUE is used to represent a revision with no end timestamp
            return 0;
        } else {
            return interval.getEnd().getMillis() / 1000;
        }
    }


    class ObjectVersionRowMapper implements RowMapper<ObjectVersion> {
        @Override
        public ObjectVersion mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ObjectVersion (
                    rs.getLong(1),                          // id
                    ObjectTypeIds.getType(rs.getInt(2)),    // object_type
                    rs.getString(3),                        // pkey
                    rs.getLong(4),                          // from_timestamp
                    rs.getLong(5) == 0 ? Long.MAX_VALUE : rs.getLong(5), //to_timestamp
                    rs.getInt(6)                           // revision
            );
        }
    }
}
