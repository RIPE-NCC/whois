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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Repository
public class JdbcVersionDao implements VersionDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcVersionDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RpslObject getRpslObject(final VersionInfo info) {
        if (info.isInLast()) {
            return jdbcTemplate.queryForObject("" +
                    "SELECT object_id, object " +
                    "FROM last " +
                    "WHERE object_id = ? " +
                    "AND sequence_id != 0",
                    new RpslObjectRowMapper(), info.getObjectId());
        }

        return jdbcTemplate.queryForObject("" +
                "SELECT object_id, object " +
                "FROM history " +
                "WHERE object_id = ? " +
                "AND sequence_id = ?",
                new RpslObjectRowMapper(), info.getObjectId(), info.getSequenceId());
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

    @Override
    public VersionLookupResult findByKey(final ObjectType type, final String searchKey) {
        final List<Integer> objectIds = jdbcTemplate.queryForList("" +
                "SELECT object_id " +
                "FROM last " +
                "WHERE object_type = ? " +
                "AND pkey = ? ",
                Integer.class,
                ObjectTypeIds.getId(type),
                searchKey);

        if (objectIds.isEmpty()) {
            return null;
        }

        final List<VersionInfo> versionInfos = Lists.newArrayList();
        for (final Integer objectId : objectIds) {
            versionInfos.addAll(jdbcTemplate.query("" +
                    "SELECT serials.atlast, " +
                    "       serials.object_id, " +
                    "       serials.sequence_id, " +
                    "       serials.operation, " +
                    "       COALESCE(last.timestamp, history.timestamp) AS timestamp " +
                    "FROM   serials " +
                    "       LEFT JOIN last ON last.object_id = serials.object_id AND (serials.atlast = 1 OR serials.operation = 2) " +
                    "       LEFT JOIN history ON history.object_id = serials.object_id AND history.sequence_id = serials.sequence_id " +
                    "WHERE serials.object_id = ? " +
                    "ORDER BY timestamp, serials.sequence_id",
                    new VersionInfoRowMapper(),
                    objectId)
            );
        }
        return new VersionLookupResult(versionInfos, type, searchKey);
    }
}
