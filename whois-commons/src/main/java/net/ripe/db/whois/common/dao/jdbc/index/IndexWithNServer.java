package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.MessageFormat;
import java.util.List;

class IndexWithNServer extends IndexWithValue {
    IndexWithNServer(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        String host = value;
        if (host.endsWith(".") && host.length() > 1) {
            host = host.substring(0, host.length() - 1);
        }

        final String query = MessageFormat.format(
                "SELECT l.object_id, l.object_type, l.pkey " +
                        "  FROM {0} " +
                        "  LEFT JOIN last l ON l.object_id = {0}.object_id " +
                        "  WHERE {0}.{1} LIKE ? " +
                        "  AND l.sequence_id != 0 ",
                lookupTableName,
                lookupColumnName
        );

        return jdbcTemplate.query(query, new RpslObjectInfoResultSetExtractor(), host + "%");
    }
}
