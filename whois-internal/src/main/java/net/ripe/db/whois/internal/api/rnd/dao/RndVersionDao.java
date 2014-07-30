package net.ripe.db.whois.internal.api.rnd.dao;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.JdbcVersionBaseDao;
import net.ripe.db.whois.common.dao.jdbc.domain.VersionInfoRowMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;

@Repository
public class RndVersionDao extends JdbcVersionBaseDao {

    @Autowired
    public RndVersionDao(final SourceAwareDataSource dataSource) {
        super(new JdbcTemplate(dataSource));
    }

    @Override
    @Nullable
    public VersionLookupResult findByKey(final ObjectType type, final String searchKey) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public List<VersionInfo> getVersionsForTimestamp(final ObjectType type, final String searchKey, final long timestampInMilliseconds) {
        final List<Integer> objectIds = getObjectIds(type, searchKey);
        final long timestamp = timestampInMilliseconds / 1000L;

        if (objectIds.isEmpty()) {
            return null;
        }

        final MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("objectIds", objectIds)
                .addValue("cutofTime", timestamp);

        // need named jdbc template to correctly resolve objectids in the IN sql part.
        final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
        final List<VersionInfo> versionInfos = Lists.newArrayList();

        versionInfos.addAll(namedParameterJdbcTemplate.query("" +
                "SELECT " +
                "  temp.atlast, " +
                "  temp.object_id, " +
                "  temp.sequence_id, " +
                "  temp.operation, " +
                "  temp.timestamp " +
                "FROM " +
                "  (" +
                "    SELECT serials.atlast, " +
                "       serials.object_id, " +
                "       serials.sequence_id, " +
                "       serials.operation, " +
                "       COALESCE(history.timestamp, last.timestamp) AS timestamp " +
                "    FROM serials " +
                "       LEFT JOIN last ON serials.object_id = last.object_id " +
                "       LEFT JOIN history ON serials.object_id=history.object_id AND serials.sequence_id=history.sequence_id " +
                "    WHERE serials.object_id IN (:objectIds) " +
                "    ORDER BY timestamp DESC, serials.object_id DESC, serials.sequence_id DESC " +
                "  ) AS temp " +
                "WHERE temp.timestamp=:cutofTime ", parameters, new VersionInfoRowMapper()));
        return versionInfos;
    }
}
