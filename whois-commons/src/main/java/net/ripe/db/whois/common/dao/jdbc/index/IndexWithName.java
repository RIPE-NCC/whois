package net.ripe.db.whois.common.dao.jdbc.index;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectResultSetExtractor;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;

class IndexWithName extends IndexStrategyWithSingleLookupTable {
    protected static final int MYSQL_MAX_JOINS = 61;
    protected static final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

    public IndexWithName(final AttributeType attributeType, final String lookupTableName) {
        super(attributeType, lookupTableName);
    }

    @Override
    public List<RpslObjectInfo> findInIndex(final JdbcTemplate jdbcTemplate, final String value) {
        final String[] names = Iterables.toArray(SPACE_SPLITTER.split(value), String.class);
        final String query = getObjectQueryByName(lookupTableName, names);
        return jdbcTemplate.query(query, new RpslObjectResultSetExtractor(), (Object[]) names);
    }

    protected static String getObjectQueryByName(String table, String[] names) {
        Validate.notEmpty(names, "no name");
        Validate.isTrue(names.length <= MYSQL_MAX_JOINS, "reached join limit");

        StringBuilder query = new StringBuilder();
        query.append("SELECT l.object_id, l.object_type, l.pkey FROM last l JOIN ");
        query.append(table);
        query.append(" n1 ON l.object_id = n1.object_id ");

        if (names.length == 1) {
            query.append(" WHERE n1.name = ?");
        } else {
            StringBuilder joins = new StringBuilder();

            for (int count = names.length; count > 1; count--) {

                if (count == names.length) {
                    joins.append(
                            String.format("JOIN %s n%d ON n%d.name = ? AND n%d.object_id = n%d.object_id",
                                    table, count, count, count, count - 1));
                } else {
                    joins.insert(0, String.format("JOIN (%s n%d ", table, count));
                    joins.append(
                            String.format(") ON n%d.name = ? AND n%d.object_id = n%d.object_id",
                                    count, count, count - 1));
                }
            }

            query.append(joins);
            query.append(" AND n1.name = ?");
            query.append(" AND l.sequence_id != 0");
        }

        return query.toString();
    }

    @Override
    public int addToIndex(final JdbcTemplate jdbcTemplate, final RpslObjectInfo objectInfo, final RpslObject object, final String value) {
        int ret = 0;

        final Set<CIString> names = Sets.newHashSet();
        for (String name : SPACE_SPLITTER.split(value)) {
            if (names.add(ciString(name))) {
                ret += addToIndex(jdbcTemplate, objectInfo.getObjectId(), name);
            }
        }

        return ret;
    }

    int addToIndex(final JdbcTemplate jdbcTemplate, final int objectId, final String name) {
        final String query = String.format("INSERT INTO %s (object_id, name) VALUES (?, ?)", lookupTableName);
        return jdbcTemplate.update(query, objectId, name);
    }
}
