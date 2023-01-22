package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

class IndexWithInet6num extends IndexStrategyWithSingleLookupTable {

    public IndexWithInet6num(final AttributeType attributeType) {
        super(attributeType, "inet6num");
    }

    // MariaDB bug workaround: if 64-bit integer has its msb bit set, the comparison fails
    // Always use Long.toString() instead of passing long for VARCHAR column
    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv6Resource resource = Ipv6Resource.parse(objectInfo.getKey());

        // GRS sources might not have netname
        final CIString netnameAttribute = object.getValueOrNullForAttribute(AttributeType.NETNAME);
        final String netname = netnameAttribute == null ? "" : netnameAttribute.toString();

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

        // MariaDB bug workaround: if 64-bit integer has its msb bit set, the comparison fails
        // Always use Long.toString() instead of passing long for VARCHAR column
        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM inet6num " +
                "  LEFT JOIN last l ON l.object_id = inet6num.object_id " +
                "  WHERE i6_msb = ? AND i6_lsb = ? AND prefix_length = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectInfoResultSetExtractor(),
                Long.toString(Ipv6Resource.msb(resource.begin())),
                Long.toString(Ipv6Resource.lsb(resource.begin())),
                resource.getPrefixLength());
    }

    @Nullable
    private Ipv6Resource parseIpv6Resource(final String s) {
        try {
            return Ipv6Resource.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
