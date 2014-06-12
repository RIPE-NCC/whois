package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class JdbcCommonOperations {
    public static List<Integer> getObjectIds(final JdbcTemplate jdbcTemplate, final ObjectType type, final String searchKey) {
        return jdbcTemplate.queryForList("" +
                        "SELECT object_id " +
                        "FROM last " +
                        "WHERE object_type = ? " +
                        "AND pkey = ? ",
                Integer.class,
                ObjectTypeIds.getId(type),
                searchKey
        );
    }
}
