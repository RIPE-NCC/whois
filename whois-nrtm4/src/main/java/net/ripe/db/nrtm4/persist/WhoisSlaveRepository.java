package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class WhoisSlaveRepository {

    private final JdbcTemplate jdbcTemplate;

//    private final RowMapper<SerialModel> serialModelMapper = (rs, n) ->
//            new SerialModel(
//                    rs.getInt(1),
//                    rs.getLong(2),
//                    rs.getLong(3),
//                    rs.getBoolean(4),
//                    Operation.valueOf(rs.getString(5))
//            );
//
//    private final RowMapper<RpslObjectModel> rpslObjectModelMapper = (rs, n) ->
//            new RpslObjectModel(
//                    rs.getInt(1),
//                    rs.getLong(2),
//                    rs.getLong(3),
//                    ObjectTypeIds.getType(rs.getInt(4)),
//                    RpslObject.parse(rs.getString(5)),
//                    rs.getString(6)
//            );

    private final RowMapper<Pair<SerialModel, RpslObjectModel>> serialAndRpslObjectMapper = (rs, n) ->
            new Pair<>(
                    new SerialModel(
                            rs.getInt(1),
                            rs.getLong(2),
                            rs.getLong(3),
                            rs.getBoolean(4),
                            Operation.valueOf(rs.getString(5))
                    ),
                    new RpslObjectModel(
                            rs.getInt(6),
                            rs.getLong(7),
                            rs.getLong(8),
                            ObjectTypeIds.getType(rs.getInt(9)),
                            RpslObject.parse(rs.getString(10)),
                            rs.getString(11))
            );

    public WhoisSlaveRepository(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Pair<SerialModel, RpslObjectModel>> findSerialsInLastSince(final int serialId) {
        final String sql = "" +
                "select " +
                "s.serial_id, s.object_id, s.sequence_id, s.atlast, s.operation, " +
                "r.object_id, r.sequence_id, r.timestamp, r.object_type, r.object, r.pkey " +
                "from serials s join history r on r.object_id = s.object_id and r.sequence_id = s.sequence_id " +
                "where s.serial_id > ? " +
                "order by s.serial_id asc";
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

    public List<Pair<SerialModel, RpslObjectModel>> findSerialsInHistorySince(final int serialId) {
        final String sql = "" +
                "select " +
                "s.serial_id, s.object_id, s.sequence_id, s.atlast, s.operation, " +
                "r.object_id, r.sequence_id, r.timestamp, r.object_type, r.object, r.pkey " +
                "from serials s join history r on r.object_id = s.object_id and r.sequence_id = s.sequence_id " +
                "where s.serial_id > ? " +
                "order by s.serial_id asc";
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

}
