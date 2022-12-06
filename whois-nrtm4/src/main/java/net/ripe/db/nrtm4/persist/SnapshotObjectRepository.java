package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;


@Repository
public class SnapshotObjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnapshotObjectRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public SnapshotObject insert(final int serialId, final String payload) {
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

    public void streamSnapshot(final OutputStream outputStream) throws IOException {
        final String sql = "" +
            "SELECT payload " +
            "FROM snapshot_object " +
            "ORDER BY serial_id";

        outputStream.write('[');
        jdbcTemplate.query(sql, rs -> {
            try {
                outputStream.write(rs.getString(1).getBytes(StandardCharsets.UTF_8));
                if (!rs.isLast()) {
                    outputStream.write(',');
                }
            } catch (final IOException e) {
                throw new RuntimeException("streamPayloads threw exception", e);
            }
        });
        outputStream.write(']');
    }

}
