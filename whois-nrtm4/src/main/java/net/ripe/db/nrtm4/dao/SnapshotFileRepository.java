package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.PublishableSnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;


@Repository
public class SnapshotFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<SnapshotFile> rowMapper = (rs, rowNum) ->
        new SnapshotFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4)
        );

    public SnapshotFileRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void insert(final PublishableSnapshotFile snapshotFile) {
        final String sql = """
            INSERT INTO snapshot_file (version_id, name, hash)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(sql,
            snapshotFile.getVersionId(),
            snapshotFile.getFileName(),
            snapshotFile.getHash());
    }

    public Optional<SnapshotFile> getByName(final String sessionId, final String name) {
        final String sql = """
            SELECT sf.id, sf.version_id, sf.name, sf.hash
            FROM snapshot_file sf
            JOIN version_info v ON v.id = sf.version_id
            WHERE v.session_id = ?
              AND sf.name = ?
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, sessionId, name));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSource source) {
        final String sql = """
            SELECT sf.id, sf.version_id, sf.name, sf.hash
            FROM snapshot_file sf
            JOIN version_info v ON v.id = sf.version_id
            JOIN source src ON src.id = v.source_id
            WHERE src.name = ?
            ORDER BY v.version DESC LIMIT 1
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, source.name()));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

}
