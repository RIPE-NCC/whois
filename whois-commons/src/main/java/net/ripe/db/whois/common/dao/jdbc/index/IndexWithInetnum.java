package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectInfoResultSetExtractor;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;

class IndexWithInetnum extends IndexStrategyWithSingleLookupTable {
    public IndexWithInetnum(final AttributeType attributeType) {
        super(attributeType, "inetnum");
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        final Ipv4Resource resource = Ipv4Resource.parse(objectInfo.getKey());

        // GRS sources might not have netname
        final List<RpslAttribute> netnameAttribute = object.findAttributes(AttributeType.NETNAME);
        final String netname = netnameAttribute.isEmpty() ? "" : netnameAttribute.get(0).getCleanValue().toString();

        return jdbcTemplate.update(
                "INSERT INTO inetnum (object_id, begin_in, end_in, netname) VALUES (?, ?, ?, ?)",
                objectInfo.getObjectId(),
                resource.begin(),
                resource.end(),
                netname);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final Ipv4Resource resource = parseIpv4Resource(value);
        if (resource == null) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query(" " +
                "SELECT l.object_id, l.object_type, l.pkey " +
                "  FROM inetnum " +
                "  LEFT JOIN last l ON l.object_id = inetnum.object_id " +
                "  WHERE begin_in = ? AND end_in = ? " +
                "  AND l.sequence_id != 0 ",
                new RpslObjectInfoResultSetExtractor(),
                resource.begin(),
                resource.end());
    }

    private Ipv4Resource parseIpv4Resource(final String s) {
        try {
            return Ipv4Resource.parse(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
