package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

class IndexWithEmailAddressValueAndType extends IndexWithValueAndType {

    public IndexWithEmailAddressValueAndType(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, String value) {
        return super.addToIndex(jdbcTemplate, objectInfo, object, IndexWithEmailAddressValue.normaliseEmailAddress(value));
    }
}
