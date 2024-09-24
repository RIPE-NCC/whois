package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.springframework.jdbc.core.JdbcTemplate;

class IndexWithEmailAddressValueAndType extends IndexWithValueAndType {

    private static final AttributeParser.EmailParser EMAIL_PARSER = new AttributeParser.EmailParser();

    public IndexWithEmailAddressValueAndType(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, String value) {
        try {
            return super.addToIndex(jdbcTemplate, objectInfo, object, normaliseEmailAddress(value));
        } catch (AttributeParseException e) {
            throw new IllegalArgumentException("Invalid email address: " + value);
        }
    }

    private String normaliseEmailAddress(final String emailAddress) {
        return EMAIL_PARSER.parse(emailAddress).getAddress();
    }
}
