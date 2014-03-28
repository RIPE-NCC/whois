package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

class IndexWithAsBlock extends IndexStrategyWithSingleLookupTable {
    public IndexWithAsBlock(final AttributeType attributeType) {
        super(attributeType, "as_block");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final AsBlockRange asBlockRange = parseAsBlockRange(value);
        if (asBlockRange == null) {
            throw new IllegalArgumentException("invalid asBlockRange");
        }

        return jdbcTemplate.update(
                "INSERT INTO as_block (object_id, begin_as, end_as) VALUES (?, ?, ?)",
                objectInfo.getObjectId(),
                asBlockRange.getBegin(),
                asBlockRange.getEnd());
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final AsBlockRange asBlockRange = parseAsBlockRange(value);
        if (asBlockRange == null) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM as_block " +
                "  LEFT JOIN last l ON l.object_id = as_block.object_id " +
                "  WHERE ? = as_block.begin_as " +
                "  AND ? = as_block.end_as " +
                "  AND l.sequence_id != 0",
                new RpslObjectInfoResultSetExtractor(),
                asBlockRange.getBegin(),
                asBlockRange.getEnd());
    }

    private AsBlockRange parseAsBlockRange(final String s) {
        try {
            return AsBlockRange.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
