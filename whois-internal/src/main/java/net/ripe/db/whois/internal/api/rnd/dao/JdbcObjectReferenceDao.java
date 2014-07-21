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
                "SELECT version_id, \n" +
                "       object_type, \n" +
                "       pkey, \n" +
                "       from_timestamp, \n" +
                "       to_timestamp \n" +
                "FROM object_version \n" +
                "WHERE object_type = ? \n" +
                "  AND pkey = ? \n" +
                "  AND from_timestamp <= ? \n" +
                "  AND (? <= to_timestamp \n" +
                "       OR to_timestamp IS NULL) \n" +
                "ORDER BY version_id DESC",
                new ObjectVersionRowMapper(),
                ObjectTypeIds.getId(type),
                pkey,
                timestamp,
                timestamp);
    }

    @Override
    public List<ObjectReference> getReferencedBy(final long versionId) {
        return getReferences(versionId, ReferenceType.REFERENCED_BY);
    }

    @Override
    public List<ObjectReference> getReferencing(final long versionId) {
        return getReferences(versionId, ReferenceType.REFERENCING);
    }
    public List<ObjectReference> getReferences(final long versionId, final ReferenceType referenceType) {
        return jdbcTemplate.query(""+
                "SELECT version_id,\n" +
                "       object_type,\n" +
                "       pkey,\n" +
                "       ref_type\n" +
                "FROM object_reference\n" +
                "WHERE version_id = ?\n" +
                "  AND ref_type = ?\n" +
                "ORDER BY object_type,\n" +
                "         pkey ASC\n" +
                "\n",
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
