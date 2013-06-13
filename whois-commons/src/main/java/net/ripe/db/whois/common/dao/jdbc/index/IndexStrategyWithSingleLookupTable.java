package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

abstract class IndexStrategyWithSingleLookupTable extends IndexStrategyAdapter {
    protected final String lookupTableName;

    public IndexStrategyWithSingleLookupTable(final AttributeType attributeType, final String lookupTableName) {
        super(attributeType);

        Validate.notNull(lookupTableName);
        this.lookupTableName = lookupTableName;
    }

    @Override
    public String getLookupTableName() {
        return lookupTableName;
    }

    @Override
    public void removeFromIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo) {
        jdbcTemplate.update(String.format("DELETE FROM %s WHERE object_id = ?", lookupTableName), objectInfo.getObjectId());
    }

    @Override
    public void cleanupMissingObjects(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(String.format("DELETE %s FROM %s LEFT JOIN LAST ON %s.object_id=last.object_id WHERE last.id IS NULL", lookupTableName, lookupTableName, lookupTableName));
    }
}
