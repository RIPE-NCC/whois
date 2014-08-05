package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.dao.jdbc.JdbcStreamingHelper;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.ReferenceStreamHandler;
import net.ripe.db.whois.internal.api.rnd.ReferenceType;
import net.ripe.db.whois.internal.api.rnd.VersionsStreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static net.ripe.db.whois.internal.api.rnd.ReferenceType.INCOMING;
import static net.ripe.db.whois.internal.api.rnd.ReferenceType.OUTGOING;

@Repository
public class JdbcObjectReferenceDao implements ObjectReferenceDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcObjectReferenceDao(@Qualifier("whoisReadOnlySlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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


    // helper methods

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
