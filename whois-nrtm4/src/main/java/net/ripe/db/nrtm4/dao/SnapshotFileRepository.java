package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;


@Repository
public class SnapshotFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;
    private final RowMapper<SnapshotFile> rowMapper = (rs, rowNum) ->
        new SnapshotFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4),
            rs.getLong(5)
        );

    private final String snapshotFileFields = "sf.id, sf.version_id, sf.name, sf.hash, sf.created ";

    public SnapshotFileRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource,
        final DateTimeProvider dateTimeProvider
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public void save(
        final long versionId,
        final String name,
        final String hash
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO snapshot_file (version_id, name, hash, created) " +
                    "VALUES (?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, name);
                pst.setString(3, hash);
                pst.setLong(4, now);
                return pst;
            }, keyHolder
        );
    }

    public Optional<SnapshotFile> getByName(final String sessionId, final String name) {
        final String sql = "" +
            "SELECT " + snapshotFileFields +
            "FROM snapshot_file sf " +
            "JOIN version_info v ON v.id = sf.version_id " +
            "WHERE v.session_id = ? " +
            "  AND sf.name = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, sessionId, name));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSource source) {
        final String sql = "" +
            "SELECT " + snapshotFileFields +
            "FROM snapshot_file sf " +
            "JOIN version_info v ON v.id = sf.version_id " +
            "JOIN source src ON src.id = v.source_id " +
            "WHERE src.name = ? " +
            "ORDER BY v.version DESC LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, source.name()));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

}
