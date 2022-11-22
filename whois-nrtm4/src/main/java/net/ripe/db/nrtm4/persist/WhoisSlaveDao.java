package net.ripe.db.nrtm4.persist;

import net.ripe.db.whois.common.dao.jdbc.domain.ObjectTypeIds;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class WhoisSlaveDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SerialModel> serialModelMapper = (rs, n) ->
            new SerialModel(
                    rs.getInt(1),
                    rs.getLong(2),
                    rs.getLong(3),
                    rs.getBoolean(4),
                    Operation.valueOf(rs.getString(5))
            );

    private final RowMapper<RpslObjectModel> rpslObjectModelMapper = (rs, n) ->
            new RpslObjectModel(
                    rs.getInt(1),
                    rs.getLong(2),
                    rs.getLong(3),
                    ObjectTypeIds.getType(rs.getInt(4)),
                    RpslObject.parse(rs.getString(5)),
                    rs.getString(6)
            );

    public WhoisSlaveDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<SerialModel> findSerialsSince(final int serialId) {
        final String sql = "" +
                "select serial_id, object_id, sequence_id, atlast, operation " +
                "from serials " +
                "where serial_id > ? " +
                "order by serial_id asc";
        return jdbcTemplate.query(sql, serialModelMapper, serialId);
    }

    public List<RpslObjectModel> getLastRpslObjects(final List<Integer> objectIds) {
        final String sql = "" +
                "select object_id, sequence_id, timestamp, object_type, object, pkey " +
                "from last " +
                "where object_id in ?";
        return jdbcTemplate.query(sql, rpslObjectModelMapper, objectIds);

    }

}
