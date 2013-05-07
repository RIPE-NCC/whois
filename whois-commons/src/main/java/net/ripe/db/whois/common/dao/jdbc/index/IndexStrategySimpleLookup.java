package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.springframework.jdbc.core.JdbcTemplate;

abstract class IndexStrategySimpleLookup extends IndexStrategyAdapter {
    protected final String lookupTableName;
    protected final String lookupColumnName;

    protected IndexStrategySimpleLookup(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType);

        this.lookupTableName = lookupTableName;
        this.lookupColumnName = lookupColumnName;
    }

    @Override
    public final String getLookupTableName() {
        return lookupTableName;
    }

    @Override
    public final String getLookupColumnName() {
        return lookupColumnName;
    }

    @Override
    public void removeFromIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo) {
        jdbcTemplate.update(String.format("DELETE FROM %s WHERE object_id = ?", lookupTableName), objectInfo.getObjectId());
    }
}
