package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IndexWithRoute6 extends IndexStrategyWithSingleLookupTable {
    public IndexWithRoute6(final AttributeType attributeType) {
        super(attributeType, "route6");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv6Resource resource = Ipv6Resource.parse(value);
        final String origin = object.getValueForAttribute(AttributeType.ORIGIN).toString();

        return jdbcTemplate.update(
                "INSERT INTO route6 (object_id, r6_msb, r6_lsb, prefix_length, origin) VALUES (?, ?, ?, ?, ?)",
                objectInfo.getObjectId(),
                Ipv6Resource.msb(resource.begin()),
                Ipv6Resource.lsb(resource.begin()),
                resource.getPrefixLength(),
                origin);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final Route6Key route6Key = Route6Key.parse(value);
        if (route6Key == null) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query("" +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM route6 " +
                "  LEFT JOIN last l ON l.object_id = route6.object_id " +
                "  WHERE route6.r6_msb = ? AND route6.r6_lsb = ? AND route6.prefix_length = ? AND route6.origin = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectResultSetExtractor(),
                Long.toString(Ipv6Resource.msb(route6Key.resource.begin())),
                Long.toString(Ipv6Resource.lsb(route6Key.resource.begin())),
                route6Key.resource.getPrefixLength(),
                route6Key.origin);
    }

    private static Ipv6Resource parseIpv6Resource(final String s) {
        try {
            return Ipv6Resource.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static final class Route6Key {
        private static final Pattern ROUTE_PATTERN = Pattern.compile("(?i)(.*)(AS(?:\\d+))");

        private final Ipv6Resource resource;
        private final String origin;

        private Route6Key(final Ipv6Resource resource, final String origin) {
            this.resource = resource;
            this.origin = origin;
        }

        private static Route6Key parse(final String value) {
            final Matcher matcher = ROUTE_PATTERN.matcher(value);
            if (!matcher.matches()) {
                return null;
            }

            final Ipv6Resource resource = parseIpv6Resource(matcher.group(1));
            if (resource == null) {
                return null;
            }

            return new Route6Key(resource, matcher.group(2));
        }
    }
}
