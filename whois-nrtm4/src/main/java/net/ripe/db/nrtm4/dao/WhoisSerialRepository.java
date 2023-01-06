package net.ripe.db.nrtm4.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;


@Repository
public class WhoisSerialRepository {

    private final JdbcTemplate jdbcTemplate;

    WhoisSerialRepository(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Integer findLastSerialId() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(serial_id) FROM serials",
            (rs, rowNum) -> rs.getInt(1));
    }

    public List<ObjectData> findLastObjects() {
        return jdbcTemplate.query(
            "SELECT object_id, sequence_id, object FROM last WHERE sequence_id > 0",
            (rs, rowNum) -> new ObjectData(
                        rs.getInt(1),                           // objectId
                        rs.getInt(2))                           // sequenceId
            );
    }

    public String findRpslInLast(final int objectId, final int sequenceId) {
        final String sql = "" +
            "SELECT object " +
            "FROM last " +
            "WHERE object_id = ? " +
            "  AND sequence_id = ?";
        return jdbcTemplate.queryForObject(
            sql,
            (rs, rn) -> rs.getString(1),
            objectId,
            sequenceId);
    }

}
