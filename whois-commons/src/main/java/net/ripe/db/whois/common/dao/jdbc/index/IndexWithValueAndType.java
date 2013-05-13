package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

class IndexWithValueAndType extends IndexWithValue {

    public IndexWithValueAndType(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final String query = String.format("INSERT INTO %s (object_id, %s, object_type) VALUES (?, ?, ?)", lookupTableName, lookupColumnName);
        return jdbcTemplate.update(query, objectInfo.getObjectId(), value, ObjectTypeIds.getId(objectInfo.getObjectType()));
    }
}
