package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.List;

class IndexWithValue extends IndexStrategySimpleLookup {

    protected IndexWithValue(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final String query = String.format("INSERT INTO %s (object_id, %s) VALUES (?, ?)", lookupTableName, lookupColumnName);
        return jdbcTemplate.update(query, objectInfo.getObjectId(), value);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        // TODO: [ES] Case-insensitive latin1-bin collation on auth.auth column should be fixed in database
        final boolean forceIgnoreCase = AttributeType.AUTH.equals(attributeType);

        final String query = MessageFormat.format(
                "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0} " +
                        "  LEFT JOIN last l ON l.object_id = {0}.object_id " +
                        "  WHERE " + (forceIgnoreCase ? "upper({0}.{1}) = upper(?) " : "{0}.{1} = ? ") +
                        "  AND l.sequence_id != 0 ",
                lookupTableName,
                lookupColumnName
        );

        return jdbcTemplate.query(query, new RpslObjectResultSetExtractor(), value);
    }
}
