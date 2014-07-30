package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.domain.VersionInfoRowMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.List;

@Repository
public class JdbcVersionDao extends JdbcVersionBaseDao {

    @Autowired
    public JdbcVersionDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

    @Override
    public VersionLookupResult findByKey(final ObjectType type, final String searchKey) {
        final List<Integer> objectIds = getObjectIds(type, searchKey);

        if (objectIds.isEmpty()) {
            return null;
        }

        final List<VersionInfo> versionInfos = Lists.newArrayList();
        for (final Integer objectId : objectIds) {
            versionInfos.addAll(getJdbcTemplate().query("" +
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

    @Nullable
    @Override
    public List<VersionInfo> getVersionsForTimestamp(ObjectType type, String searchKey, long timestamp) {
        throw new UnsupportedOperationException();
    }
}
