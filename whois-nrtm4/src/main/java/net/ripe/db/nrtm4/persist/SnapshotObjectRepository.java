package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.stream.Stream;


@Repository
public class SnapshotObjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotObjectRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public SnapshotObject save(final int serialId, final String payload) {
        final String sql = "" +
            "INSERT INTO snapshot_object (serial_id, payload) " +
            "VALUES (?, ?)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, serialId);
            pst.setString(2, payload);
            return pst;
        }, keyHolder);
        return new SnapshotObject(keyHolder.getKeyAs(Long.class), serialId, payload);
    }

    public void delete(final int serialId) {
        final String sql = "" +
            "DELETE FROM snapshot_object " +
            "WHERE serial_id = ?";
        jdbcTemplate.update(connection -> {
            final PreparedStatement pst = connection.prepareStatement(sql);
            pst.setInt(1, serialId);
            return pst;
        });
    }

    public void streamPayloads(final Stream.Builder<String> outputStream) {
        final String sql = "" +
            "SELECT payload " +
            "FROM snapshot_object " +
            "ORDER BY serial_id";
        jdbcTemplate.query(sql, rs -> {
            outputStream.add(rs.getString(1));
            if (!rs.isLast()) {
                outputStream.add(",");
            }
        });
    }

}
