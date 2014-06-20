package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.domain.VersionInfoRowMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public abstract class JdbcVersionBaseDao implements VersionDao {
    private final JdbcTemplate jdbcTemplate;

    protected JdbcVersionBaseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RpslObject getRpslObject(final VersionInfo info) {
        if (info.isInLast()) {
            return getJdbcTemplate().queryForObject("" +
                            "SELECT object_id, object " +
                            "FROM last " +
                            "WHERE object_id = ? " +
                            "AND sequence_id != 0",
                    new RpslObjectRowMapper(), info.getObjectId());
        }

        return getJdbcTemplate().queryForObject("" +
                        "SELECT object_id, object " +
                        "FROM history " +
                        "WHERE object_id = ? " +
                        "AND sequence_id = ?",
                new RpslObjectRowMapper(), info.getObjectId(), info.getSequenceId());
    }


    public List<Integer> getObjectIds(final ObjectType type, final String searchKey) {
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

    @Override
    public Set<ObjectType> getObjectType(String searchKey) {
        final EnumSet<ObjectType> objectTypes = EnumSet.noneOf(ObjectType.class);
        final List<Integer> serialTypes = jdbcTemplate.queryForList("SELECT object_type FROM last WHERE pkey = ? ORDER BY object_type", Integer.class, searchKey);
        for (Integer serialType : serialTypes) {
            objectTypes.add(ObjectTypeIds.getType(serialType));
        }
        return objectTypes;
    }



    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
