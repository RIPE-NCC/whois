package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.JdbcVersionBaseDao;
import net.ripe.db.whois.common.dao.jdbc.domain.VersionInfoRowMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceAwareDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
        final List<Integer> objectIds = getObjectIds(type, searchKey);

        if (objectIds.isEmpty()) {
            return null;
        }

        final List<VersionInfo> versionInfos = Lists.newArrayList();

        for (Integer objectId : objectIds) {
            versionInfos.addAll(
                    getJdbcTemplate().query("" +
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
                            new VersionInfoRowMapper(), objectId
                    )
            );
        }

        return new VersionLookupResult(versionInfos, type, searchKey);
    }

    @Nullable
    @Override
    public List<VersionInfo> getVersionsBeforeTimestamp(ObjectType type, String searchKey, long timestamp) {
        final List<Integer> objectIds = getObjectIds(type, searchKey);

        if (objectIds.isEmpty()) {
            return null;
        }

        final List<VersionInfo> versionInfos = Lists.newArrayList();
        versionInfos.addAll(
                getJdbcTemplate().query("" +
                                "select " +
                                "  temp.atlast, " +
                                "  temp.object_id, " +
                                "  temp.sequence_id, " +
                                "  temp.operation," +
                                "  temp.timestamp" +
                                "from " +
                                "  (" +
                                "    SELECT serials.atlast, " +
                                "       serials.object_id, " +
                                "       serials.sequence_id, " +
                                "       serials.operation, " +
                                "       COALESCE(history.timestamp, last.timestamp) AS timestamp " +
                                "    FROM serials " +
                                "       LEFT JOIN last ON serials.object_id = last.object_id " +
                                "       LEFT JOIN history ON serials.object_id=history.object_id AND serials.sequence_id=history.sequence_id " +
                                "    WHERE serials.object_id in (?) " +
                                "    ORDER BY timestamp DESC, serials.object_id desc, serials.sequence_id desc " +
                                "  ) as temp\n" +
                                "where temp.timestamp<=? "
                        , new VersionInfoRowMapper(),
                        Joiner.on(',').join(objectIds), timestamp
                )
        );

        return versionInfos;
    }
}
