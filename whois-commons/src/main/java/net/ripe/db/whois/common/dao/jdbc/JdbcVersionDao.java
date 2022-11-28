package net.ripe.db.whois.common.dao.jdbc;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectModel;
import net.ripe.db.whois.common.dao.Serial;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionDateTime;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.dao.jdbc.domain.RpslObjectRowMapper;
import net.ripe.db.whois.common.dao.jdbc.domain.VersionInfoRowMapper;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Repository
public class JdbcVersionDao implements VersionDao {
    private final JdbcTemplate jdbcTemplate;

    final String sqlSerialAndRpslTemplate = "" +
        "SELECT " +
        "s.serial_id, s.atlast, s.object_id, s.sequence_id, s.operation, " +
        "r.object_id, r.sequence_id, r.object_type, r.pkey, r.object, r.timestamp " +
        "FROM serials s JOIN %s r ON r.object_id = s.object_id and r.sequence_id = s.sequence_id " +
        "WHERE s.serial_id > ? " +
        "ORDER BY s.serial_id ASC";

    private final RowMapper<SerialRpslObjectTuple> serialAndRpslObjectMapper = (rs, n) ->
        new SerialRpslObjectTuple(
            new Serial(
                rs.getInt(1),
                rs.getBoolean(2),
                rs.getInt(3),
                rs.getInt(4),
                Operation.getByCode(rs.getInt(5))
            ),
            new RpslObjectModel(
                rs.getInt(6),
                rs.getInt(7),
                ObjectTypeIds.getType(rs.getInt(8)),
                rs.getString(9),
                RpslObject.parse(rs.getString(10)),
                rs.getLong(11)
            )
        );

    @Autowired
    public JdbcVersionDao(@Qualifier("sourceAwareDataSource") final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
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


    @Override
    public VersionLookupResult findByKey(final ObjectType type, final String searchKey) {
        final List<Integer> objectIds = getObjectIds(type, searchKey);

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

     @Nullable
     @Override
     public List<VersionInfo> getVersionsForTimestamp(final ObjectType type, final String searchKey, final VersionDateTime timestamp) {
        final List<Integer> objectIds = getObjectIds(type, searchKey);

        if (objectIds.isEmpty()) {
            return null;
        }

        final MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("objectIds", objectIds)
                .addValue("cutoffTime", Timestamp.from(timestamp.getTimestamp()).getValue());

        // need named jdbc template to correctly resolve objectids in the IN sql part.
        final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
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
                "WHERE temp.timestamp=:cutoffTime ", parameters, new VersionInfoRowMapper()));
        return versionInfos;
    }

    public List<SerialRpslObjectTuple> findSerialsInLastSince(final int serialId) {
        final String sql = String.format(sqlSerialAndRpslTemplate, "last");
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

    public List<SerialRpslObjectTuple> findSerialsInHistorySince(final int serialId) {
        final String sql = String.format(sqlSerialAndRpslTemplate, "history");
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

}
