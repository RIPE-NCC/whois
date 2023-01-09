package net.ripe.db.nrtm4.dao;

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
            "SELECT object_id, sequence_id FROM last WHERE sequence_id > 0",
            (rs, rowNum) -> new ObjectData(
                rs.getInt(1),                           // objectId
                rs.getInt(2))                           // sequenceId
        );
    }

    public Map<Integer, String> findRpslMapForLastObjects(final List<ObjectData> objects) {
        final String sql = "" +
            "SELECT object_id, object " +
            "FROM last " +
            "WHERE object_id = ? " +
            "  AND sequence_id = ?";
        final Map<Integer, String> resultMap = new HashMap<>();
        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            for (final ObjectData object : objects) {
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

    public String findRpslMapForHistoryObject(final int objectId, final int sequenceId) {
        final String sql = "" +
            "SELECT object " +
            "FROM history " +
            "WHERE object_id = ? " +
            "  AND sequence_id = ?";
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
