package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class IndexWithRoute extends IndexStrategyWithSingleLookupTable {

    public IndexWithRoute(final AttributeType attributeType) {
        super(attributeType, "route");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv4Resource ipRange = Ipv4Resource.parse(value);
        final String origin = object.getValueForAttribute(AttributeType.ORIGIN).toString();

        return jdbcTemplate.update(
                "INSERT INTO route (object_id, prefix, prefix_length, origin) VALUES (?, ?, ?, ?)",
                objectInfo.getObjectId(),
                ipRange.begin(),
                ipRange.getPrefixLength(),
                origin);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final RouteKey routeKey = RouteKey.parse(value);
        if (routeKey == null) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query("" +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM route " +
                "  LEFT JOIN last l ON l.object_id = route.object_id " +
                "  WHERE route.prefix = ? AND route.prefix_length = ? AND route.origin = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectInfoResultSetExtractor(),
                routeKey.ipRange.begin(),
                routeKey.ipRange.getPrefixLength(),
                routeKey.origin);
    }

    private static final class RouteKey {
        private static final Pattern ROUTE_PATTERN = Pattern.compile("(?i)(.*)(AS(?:\\d+))");

        private final Ipv4Resource ipRange;
        private final String origin;

        private RouteKey(final Ipv4Resource ipRange, final String origin) {
            this.ipRange = ipRange;
            this.origin = origin;
        }

        @Nullable
        private static RouteKey parse(final String value) {
            final Matcher matcher = ROUTE_PATTERN.matcher(value);
            if (!matcher.matches()) {
                return null;
            }

            final Ipv4Resource ipRange = Ipv4Resource.parseIPv4Resource(matcher.group(1));
            if (ipRange == null) {
                return null;
            }

            return new RouteKey(ipRange, matcher.group(2));
        }
    }
}
