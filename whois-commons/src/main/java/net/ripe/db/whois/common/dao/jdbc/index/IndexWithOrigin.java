package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

class IndexWithOrigin extends IndexStrategyAdapter {
    IndexWithOrigin(final AttributeType attributeType) {
        super(attributeType);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        if (!AttributeType.ORIGIN.isValidValue(ObjectType.ROUTE, value) && !AttributeType.ORIGIN.isValidValue(ObjectType.ROUTE6, value)) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query("" +
                "SELECT l.object_id, l.object_type, l.pkey FROM route " +
                " LEFT JOIN last l ON l.object_id = route.object_id" +
                " WHERE l.sequence_id != 0  AND route.origin = ?" +
                " UNION" +
                " SELECT l.object_id, l.object_type, l.pkey FROM route6" +
                " LEFT JOIN last l ON l.object_id = route6.object_id" +
                " WHERE route6.origin = ?" +
                " AND l.sequence_id != 0 ",
                new RpslObjectInfoResultSetExtractor(), value, value);
    }
}
