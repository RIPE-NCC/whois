package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.rpsl.attrs.MntRoutes;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.List;

class IndexWithMntRoutes extends IndexWithReference {
    IndexWithMntRoutes(final AttributeType attributeType) {
        super(attributeType, "mnt_routes", "mnt_id");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final RpslObjectInfo reference = getReference(jdbcTemplate, MntRoutes.parse(value).getMaintainer().toString());
        if (reference == null) {
            throw new IllegalArgumentException("Referenced object does not exist: " + value);
        }

        int existing = jdbcTemplate.queryForInt("" +
                "SELECT COUNT(*) " +
                "FROM mnt_routes " +
                "WHERE object_id = ? " +
                "AND " + lookupColumnName + " = ? " +
                "AND object_type = ?",
                objectInfo.getObjectId(),
                reference.getObjectId(),
                ObjectTypeIds.getId(objectInfo.getObjectType()));

        if (existing != 0) {
            return existing;
        }

        return jdbcTemplate.update("" +
                "INSERT INTO mnt_routes " +
                "(object_id, " + lookupColumnName + ", object_type) " +
                "VALUES (?, ?, ?)",
                objectInfo.getObjectId(),
                reference.getObjectId(),
                ObjectTypeIds.getId(objectInfo.getObjectType()));
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        return Lists.newArrayList(findInIndex(jdbcTemplate, value, "mnt_routes"));
    }

    private List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value, final String lookupTableName) {

        final String query = MessageFormat.format(
                "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0} " +
                        "  LEFT JOIN {2} ON {2}.{3} = {0}.object_id " +
                        "  LEFT JOIN last l ON l.object_id = {2}.object_id " +
                        "  WHERE {0}.{1} = ? " +
                        "  AND l.sequence_id != 0 ",
                IndexStrategies.get(AttributeType.MNTNER).getLookupTableName(),
                IndexStrategies.get(AttributeType.MNTNER).getLookupColumnName(),
                lookupTableName,
                lookupColumnName);

        return jdbcTemplate.query(query, new RpslObjectInfoResultSetExtractor(), value);
    }

    @Override
    public void removeFromIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo) {
        jdbcTemplate.update("DELETE FROM mnt_routes WHERE object_id = ?", objectInfo.getObjectId());
    }
}
