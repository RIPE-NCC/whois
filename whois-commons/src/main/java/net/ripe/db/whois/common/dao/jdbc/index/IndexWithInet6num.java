package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

class IndexWithInet6num extends IndexStrategyWithSingleLookupTable {
    public IndexWithInet6num(final AttributeType attributeType) {
        super(attributeType, "inet6num");
    }

    // MySQL 5.1 bug workaround: if 64-bit integer has its msb bit set, the comparison fails
    // (proved to be working in mysql 5.5; we can drop the Long.toString() then
    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv6Resource resource = Ipv6Resource.parse(objectInfo.getKey());
        final String netname = object.getValueForAttribute(AttributeType.NETNAME).toString();

        return jdbcTemplate.update(
                "INSERT INTO inet6num (object_id, i6_msb, i6_lsb, prefix_length, netname) VALUES (?, ?, ?, ?, ?)",
                objectInfo.getObjectId(),
                Long.toString(Ipv6Resource.msb(resource.begin())),
                Long.toString(Ipv6Resource.lsb(resource.begin())),
                resource.getPrefixLength(),
                netname);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final Ipv6Resource resource = parseIpv6Resource(value);
        if (resource == null) {
            return Collections.emptyList();
        }

        // MySQL 5.1 bug workaround: if 64-bit integer has its msb bit set, the comparison fails
        // (proved to be working in mysql 5.5; we can drop the Long.toString() then
        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM inet6num " +
                "  LEFT JOIN last l ON l.object_id = inet6num.object_id " +
                "  WHERE i6_msb = ? AND i6_lsb = ? AND prefix_length = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectResultSetExtractor(),
                Long.toString(Ipv6Resource.msb(resource.begin())),
                Long.toString(Ipv6Resource.lsb(resource.begin())),
                resource.getPrefixLength());
    }

    private Ipv6Resource parseIpv6Resource(final String s) {
        try {
            return Ipv6Resource.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
