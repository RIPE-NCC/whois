package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

class IndexWithNameAndType extends IndexWithName {
    private final int objectTypeId;

    public IndexWithNameAndType(final AttributeType attributeType, final ObjectType objectType, final String lookupTableName) {
        super(attributeType, lookupTableName);

        Validate.notNull(objectType);
        objectTypeId = ObjectTypeIds.getId(objectType);
    }

    @Override
    int addToIndex(final JdbcTemplate jdbcTemplate, final int objectId, final String name) {
        final String query = String.format("INSERT INTO %s (object_id, name, object_type) VALUES (?, ?, ?)", lookupTableName);
        return jdbcTemplate.update(query, objectId, name, objectTypeId);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final String[] names = Iterables.toArray(SPACE_SPLITTER.split(value), String.class);
        final String query = new StringBuilder(getObjectQueryByName(lookupTableName, names)).append(" AND l.object_type = ").append(objectTypeId).toString();
        return jdbcTemplate.query(query, new RpslObjectResultSetExtractor(), (Object[]) names);
    }
}
