package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

class IndexWithLocalAs extends IndexStrategyWithSingleLookupTable {

    public IndexWithLocalAs(final AttributeType attributeType) {
        super(attributeType, "inet_rtr");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        return jdbcTemplate.update(
                "UPDATE inet_rtr SET local_as = ? WHERE object_id = ?",
                value,
                objectInfo.getObjectId());
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM inet_rtr " +
                "  LEFT JOIN last l ON l.object_id = inet_rtr.object_id " +
                "  WHERE local_as = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectResultSetExtractor(),
                value);
    }

    @Override
    public void removeFromIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo) {
        //do nothing, removed for inet_rtr using the indexWithValue strategy
    }
}
