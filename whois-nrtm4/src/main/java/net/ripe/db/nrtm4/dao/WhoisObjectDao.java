package net.ripe.db.nrtm4.dao;

import com.google.common.collect.Maps;
import net.ripe.db.nrtm4.domain.WhoisObjectData;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.serials.SerialEntry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
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

    @CheckForNull
    public List<SerialEntry> getSerialEntriesBetween(final int serialId, final int serialIdTo) {
        return JdbcRpslObjectOperations.getSerialEntriesBetween(jdbcTemplate, serialId, serialIdTo);
    }

    public Integer getLastSerialId() {
        return jdbcTemplate.queryForObject(
            "SELECT MAX(serial_id) FROM serials",
            (rs, rowNum) -> rs.getInt(1));
    }

    public Map<Integer, Integer> getAllObjectsFromLast() {
        final Map<Integer, Integer> objectAndSequenceId = Maps.newHashMap();

        jdbcTemplate.query(
            "SELECT object_id, sequence_id FROM last WHERE sequence_id > 0",
            (rs, rowNum) -> objectAndSequenceId.put(
                rs.getInt(1),                           // objectId
                rs.getInt(2))                           // sequenceId
        );

        return objectAndSequenceId;
    }

    public Map<Integer, Integer> geMinimumSequenceIdBetweenSerials(final int serialIDFrom, final int serialIDTo) {
        final Map<Integer, Integer> objectAndSequenceId = Maps.newHashMap();
        jdbcTemplate.query("""
                SELECT object_id, MIN(sequence_id) 
                FROM serials 
                WHERE serials.serial_id > ? AND  serials.serial_id <= ?
                GROUP BY object_id;
                """,
                (rs, rowNum) -> objectAndSequenceId.put(
                        rs.getInt(1),
                        rs.getInt(2)),
                serialIDFrom, serialIDTo
        );

        return objectAndSequenceId;
    }


    public Map<Integer, String> findRpslMapForLastObjects(final List<WhoisObjectData> objects) {
        final String sql = """
            SELECT object_id, object
            FROM last
            WHERE object_id = ?
              AND sequence_id = ?
            """;
        final Map<Integer, String> resultMap = new HashMap<>();
        jdbcTemplate.execute(sql, (PreparedStatementCallback<Object>) ps -> {
            for (final WhoisObjectData object : objects) {
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
