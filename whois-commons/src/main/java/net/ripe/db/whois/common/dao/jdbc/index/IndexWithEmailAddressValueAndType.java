package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

class IndexWithEmailAddressValueAndType extends IndexWithValueAndType {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexWithEmailAddressValueAndType.class);

    private static final AttributeParser.EmailParser EMAIL_PARSER = new AttributeParser.EmailParser();

    public IndexWithEmailAddressValueAndType(final AttributeType attributeType, final String lookupTableName, final String lookupColumnName) {
        super(attributeType, lookupTableName, lookupColumnName);
    }

    @Override
    public int addToIndex(JdbcTemplate jdbcTemplate, RpslObjectInfo objectInfo, RpslObject object, String value) {
        return super.addToIndex(jdbcTemplate, objectInfo, object, normaliseEmailAddress(value));
    }

    private String normaliseEmailAddress(final String emailAddress) {
        try {
            return EMAIL_PARSER.parse(emailAddress).getAddress();
        } catch (AttributeParseException e) {
            LOGGER.warn("Invalid email address: {}", emailAddress);
            return emailAddress;
        }
    }
}
