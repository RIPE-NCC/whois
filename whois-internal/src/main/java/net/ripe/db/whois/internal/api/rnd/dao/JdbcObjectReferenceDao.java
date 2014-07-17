package net.ripe.db.whois.internal.api.rnd.dao;


import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public JdbcObjectReferenceDao(@Qualifier("sourceAwareDataSource") final SourceAwareDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<ObjectReference> getReferencing(final ObjectType fromObjectType, final String fromPkey, final long timestamp) {

        final List<ObjectReference> referencing = Lists.newArrayList();
        referencing.addAll(jdbcTemplate.query("" +
                        "SELECT \n" +
                        "  from_object_type, \n" +
                        "  from_pkey, \n" +
                        "  from_object_id, \n" +
                        "  from_sequence_id, \n" +
                        "  to_object_type, \n" +
                        "  to_pkey, \n" +
                        "  to_object_id, \n" +
                        "  to_sequence_id, \n" +
                        "  from_timestamp, \n" +
                        "  to_timestamp \n" +
                        "FROM object_reference \n"+
                        "WHERE \n" +
                        "  from_object_type = ? \n" +
                        "  AND from_pkey = ? \n" +
                        "  AND from_timestamp <= ? \n" +
                        "  AND (to_timestamp IS NULL OR ? <= to_timestamp) \n",
                new ObjectReferenceRowMapper(),
                ObjectTypeIds.getId(fromObjectType),
                fromPkey,
                timestamp,
                timestamp));

        return referencing;
    }

    @Override
    public List<ObjectReference> getReferencedBy(final ObjectType toObjectType, final String toPkey, final long timestamp) {
        final List<ObjectReference> referenced = Lists.newArrayList();
        referenced.addAll(jdbcTemplate.query("" +
                    "SELECT \n" +
                        "  from_object_type, \n" +
                        "  from_pkey, \n" +
                        "  from_object_id, \n" +
                        "  from_sequence_id, \n" +
                        "  to_object_type, \n" +
                        "  to_pkey, \n" +
                        "  to_object_id, \n" +
                        "  to_sequence_id, \n" +
                        "  from_timestamp, \n" +
                        "  to_timestamp \n" +
                    "FROM object_reference \n" +
                        "WHERE\n" +
                        "  to_object_type = ? \n" +
                        "  AND to_pkey = ? \n" +
                        "  AND from_timestamp <= ? \n" +
                        "  AND (to_timestamp IS NULL OR ? <= to_timestamp) \n",
                new ObjectReferenceRowMapper(),
                ObjectTypeIds.getId(toObjectType),
                toPkey,
                timestamp,
                timestamp));

        return referenced;
    }

    class ObjectReferenceRowMapper implements RowMapper<ObjectReference> {
        @Override
        public ObjectReference mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ObjectReference (
                    ObjectTypeIds.getType(rs.getInt(1)), //fromType
                    rs.getString(2), //pkey
                    rs.getInt(3), //from_objId
                    rs.getInt(4), //from_seqId

                    ObjectTypeIds.getType(rs.getInt(5)), //toType
                    rs.getString(6), //to_pkey
                    rs.getInt(7), //to_objId
                    rs.getInt(8), //to_seqId

                    rs.getLong(9), //from_timestamp
                    rs.getLong(10) //to_timestamp
            );
        }
    }
}
