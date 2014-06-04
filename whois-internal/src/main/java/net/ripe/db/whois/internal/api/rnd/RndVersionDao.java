package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Repository
public class RndVersionDao implements VersionDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public RndVersionDao(final SourceAwareDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public RpslObject getRpslObject(VersionInfo info) {
        return null;
    }

    @Override
    public VersionLookupResult findByKey(ObjectType type, String searchKey) {
        final List<Integer> objectIds = jdbcTemplate.queryForList("" +
                        "SELECT object_id " +
                        "FROM last " +
                        "WHERE object_type = ? " +
                        "AND pkey = ? ",
                Integer.class,
                ObjectTypeIds.getId(type),
                searchKey
        );

        if (objectIds.isEmpty()) {
            return null;
        }

        final List<VersionInfo> versionInfos = Lists.newArrayList();

        for (Integer objectId : objectIds) {
            versionInfos.addAll(
                    jdbcTemplate.query("" +
                                    "SELECT serials.atlast, " +
                                    "       serials.object_id, " +
                                    "       serials.sequence_id, " +
                                    "       serials.operation, " +
                                    "       COALESCE(history.timestamp, last.timestamp) AS timestamp " +
                                    "FROM serials " +
                                    "       LEFT JOIN last ON serials.object_id = last.object_id " +
                                    "       LEFT JOIN history ON serials.object_id=history.object_id AND serials.sequence_id=history.sequence_id " +
                                    "WHERE serials.object_id =? " +
                                    "ORDER BY timestamp, serials.sequence_id",
                            new RowMapper<VersionInfo>() {
                                @Override
                                public VersionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    return new VersionInfo(
                                            rs.getBoolean(1),
                                            rs.getInt(2),
                                            rs.getInt(3),
                                            rs.getLong(5),
                                            Operation.getByCode(rs.getInt(4)));
                                }
                            }, objectId
                    )
            );
        }

        return new VersionLookupResult(versionInfos, type, searchKey);
    }

    @Override
    public Set<ObjectType> getObjectType(String searchKey) {
        return null;
    }
}
