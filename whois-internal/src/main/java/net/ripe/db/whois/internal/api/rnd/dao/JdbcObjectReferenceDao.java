package net.ripe.db.whois.internal.api.rnd.dao;


import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.domain.ReferenceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcObjectReferenceDao implements ObjectReferenceDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcObjectReferenceDao(final SourceAwareDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<ObjectVersion> getObjectVersion(final ObjectType type, final String pkey, long timestamp) {
        return jdbcTemplate.query("" +
                "SELECT version_id, " +
                "       object_type, " +
                "       pkey, " +
                "       from_timestamp, " +
                "       to_timestamp " +
                "FROM object_version " +
                "WHERE object_type = ? " +
                "  AND pkey = ? " +
                "  AND from_timestamp <= ? " +
                "  AND (? <= to_timestamp " +
                "       OR to_timestamp IS NULL) " +
                "ORDER BY version_id DESC",
                new ObjectVersionRowMapper(),
                ObjectTypeIds.getId(type),
                pkey,
                timestamp,
                timestamp);
    }

    @Override
    public List<ObjectReference> getIncoming(final long versionId) {
        return getReferences(versionId, ReferenceType.INCOMING);
    }

    @Override
    public List<ObjectReference> getOutgoing(final long versionId) {
        return getReferences(versionId, ReferenceType.OUTGOING);
    }
    public List<ObjectReference> getReferences(final long versionId, final ReferenceType referenceType) {
        return jdbcTemplate.query(""+
                "SELECT version_id," +
                "       object_type," +
                "       pkey," +
                "       ref_type " +
                "FROM object_reference " +
                "WHERE version_id = ?" +
                "  AND ref_type = ? " +
                "ORDER BY object_type," +
                "         pkey ASC",
                new ObjectReferenceRowMapper(),
                versionId,
                referenceType.getTypeId());
    }


    class ObjectVersionRowMapper implements RowMapper<ObjectVersion> {
        @Override
        public ObjectVersion mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ObjectVersion (
                    rs.getLong(1), //versionId
                    ObjectTypeIds.getType(rs.getInt(2)),
                    rs.getString(3), //pkey
                    rs.getLong(4),//fromTimestamp
                    rs.getLong(5) == 0 ? Long.MAX_VALUE : rs.getLong(5)//toTimestamp
            );
        }
    }

    class ObjectReferenceRowMapper implements RowMapper<ObjectReference> {
        @Override
        public ObjectReference mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ObjectReference (
                    rs.getLong(1), //versionId
                    ObjectTypeIds.getType(rs.getInt(2)), //reftype
                    CIString.ciString(rs.getString(3)), //refpkey
                    ReferenceType.get(rs.getInt(4))
            );
        }
    }
}
