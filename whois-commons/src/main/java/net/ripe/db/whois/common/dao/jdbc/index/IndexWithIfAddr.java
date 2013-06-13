package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class IndexWithIfAddr extends IndexStrategyWithSingleLookupTable {

    private final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

    public IndexWithIfAddr(final AttributeType attributeType) {
        super(attributeType, "ifaddr");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv4Resource ifaddr = parseIfAddr(value);
        if (ifaddr == null) {
            throw new IllegalArgumentException("invalid ifaddr");
        }

        return jdbcTemplate.update(
                "INSERT INTO ifaddr (object_id, ifaddr) VALUES (?, ?)",
                objectInfo.getObjectId(),
                ifaddr.begin());
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final Ipv4Resource resource = parseIfAddr(value);
        if (resource == null) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM ifaddr " +
                "  LEFT JOIN last l ON l.object_id = ifaddr.object_id " +
                "  WHERE ifaddr = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectResultSetExtractor(),
                resource.begin());
    }

    private Ipv4Resource parseIfAddr(final String value) {
        Iterator<String> iterator = SPACE_SPLITTER.split(value).iterator();
        if (iterator.hasNext()) {
            try {
                Ipv4Resource result = Ipv4Resource.parse(iterator.next());
                if (result.begin() == result.end()) {
                    return result;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return null;
    }
}
