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

    final String sqlSerialAndRpslTemplate = "" +
            "select " +
            "s.serial_id, s.object_id, s.sequence_id, s.atlast, s.operation, " +
            "r.object_id, r.sequence_id, r.timestamp, r.object_type, r.object, r.pkey " +
            "from serials s join %s r on r.object_id = s.object_id and r.sequence_id = s.sequence_id " +
            "where s.serial_id > ? " +
            "order by s.serial_id asc";

    private final RowMapper<Pair<SerialModel, RpslObjectModel>> serialAndRpslObjectMapper = (rs, n) ->
            new Pair<>(
                    new SerialModel(
                            rs.getInt(1),
                            rs.getLong(2),
                            rs.getLong(3),
                            rs.getBoolean(4),
                            Operation.getByCode(rs.getInt(5))
                    ),
                    new RpslObjectModel(
                            rs.getInt(6),
                            rs.getLong(7),
                            rs.getLong(8),
                            ObjectTypeIds.getType(rs.getInt(9)),
                            RpslObject.parse(rs.getString(10)),
                            rs.getString(11))
            );

    WhoisSlaveRepository(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    List<Pair<SerialModel, RpslObjectModel>> findSerialsInLastSince(final int serialId) {
        final String sql = String.format(sqlSerialAndRpslTemplate, "last");
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

    List<Pair<SerialModel, RpslObjectModel>> findSerialsInHistorySince(final int serialId) {
        final String sql = String.format(sqlSerialAndRpslTemplate, "history");
        return jdbcTemplate.query(sql, serialAndRpslObjectMapper, serialId);
    }

}
