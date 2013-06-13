package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.rpsl.AttributeType;

abstract class IndexStrategySimpleLookup extends IndexStrategyWithSingleLookupTable {
    protected final String lookupColumnName;

    protected IndexStrategySimpleLookup(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName);
        this.lookupColumnName = lookupColumnName;
    }

    @Override
    public final String getLookupColumnName() {
        return lookupColumnName;
    }
}
