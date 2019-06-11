package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.List;

class IndexWithValueAndType extends IndexWithValue {

    public IndexWithValueAndType(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(JdbcTemplate jdbcTemplate, String value, ObjectType type) {
        final String query = MessageFormat.format("" +
                        "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0} " +
                        "  LEFT JOIN last l ON l.object_id = {0}.object_id " +
                        "  WHERE {0}.{1} = ? AND {0}.object_type = ? " +
                        "  AND l.sequence_id != 0 ",
                lookupTableName,
                lookupColumnName
        );

        return jdbcTemplate.query(query, new RpslObjectInfoResultSetExtractor(), value, ObjectTypeIds.getId(type));
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final String query = String.format("INSERT INTO %s (object_id, %s, object_type) VALUES (?, ?, ?)", lookupTableName, lookupColumnName);
        return jdbcTemplate.update(query, objectInfo.getObjectId(), value, ObjectTypeIds.getId(objectInfo.getObjectType()));
    }
}
