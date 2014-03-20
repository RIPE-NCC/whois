package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

class IndexWithAuth extends IndexWithValueAndType {

    public IndexWithAuth(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, String value) {
        final String auth = value.toUpperCase();
        if (auth.startsWith("SSO ") || auth.startsWith("MD5-PW ")) {
            return 1;
        } else {
            return super.addToIndex(jdbcTemplate, objectInfo, object, value);
        }
    }
}
