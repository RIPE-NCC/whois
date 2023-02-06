package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.RpslObjectData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Repository
public class WhoisObjectDao {

    private final JdbcTemplate jdbcTemplate;

    WhoisObjectDao(@Qualifier("whoisSlaveDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Integer getLastSerialId() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(serial_id) FROM serials",
            (rs, rowNum) -> rs.getInt(1));
    }

    public List<RpslObjectData> getAllObjectsFromLast() {
        return jdbcTemplate.query(
            "SELECT object_id, sequence_id FROM last WHERE sequence_id > 0",
            (rs, rowNum) -> new RpslObjectData(
                rs.getInt(1),                           // objectId
                rs.getInt(2))                           // sequenceId
        );
    }

    public Map<Integer, String> findRpslMapForLastObjects(final List<RpslObjectData> objects) {
        final String sql = """
            SELECT object_id, object
            FROM last
            WHERE object_id = ?
              AND sequence_id = ?
            """;
        final Map<Integer, String> resultMap = new HashMap<>();
        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            for (final RpslObjectData object : objects) {
                ps.setInt(1, object.objectId());
                ps.setInt(2, object.sequenceId());
                final ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    resultMap.put(rs.getInt(1), rs.getString(2));
                }
                rs.close();
            }
            return null;
        });
        return resultMap;
    }

    public String findRpslForHistoryObject(final int objectId, final int sequenceId) {
        final String sql = """
            SELECT object
            FROM history
            WHERE object_id = ?
            AND sequence_id = ?
            """;
        final AtomicReference<String> result = new AtomicReference<>();
        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            ps.setInt(1, objectId);
            ps.setInt(2, sequenceId);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.set(rs.getString(1));
                }
            }
            return null;
        });
        return result.get();
    }

}
