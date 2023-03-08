package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;


@Repository
public class DeltaFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
        new DeltaFile(
            rs.getLong(1),   // id
            rs.getLong(2),   // version_id
            rs.getString(3), // name
            rs.getString(4), // hash
            rs.getString(5)  // payload
        );

    public DeltaFileRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(final PublishableDeltaFile deltaFile, final String payload) {
        save(deltaFile.getVersionId(), deltaFile.getFileName(), deltaFile.getHash(), payload);
    }

    private void save(
        final long versionId,
        final String name,
        final String hash,
        final String payload
    ) {
        final String sql = """
            INSERT INTO delta_file (version_id, name, hash, payload)
            VALUES (?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql, versionId, name, hash, payload);
    }

    public Optional<DeltaFile> getByName(final String sessionId, final String name) {
        final String sql = """
            SELECT df.id, df.version_id, df.name, df.hash, df.payload
            FROM delta_file df
            JOIN version_info v ON v.id = df.version_id
            WHERE v.session_id = ?
              AND df.name = ?
            """;
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, sessionId, name));
    }

    public List<DeltaFile> getDeltasForNotification(final NrtmVersionInfo sinceVersion, final int sinceTimestamp) {
        final String sql = """
            SELECT df.id, df.version_id, df.name, df.hash, df.payload
            FROM delta_file df
            JOIN version_info v ON v.id = df.version_id
            WHERE v.source_id = ?
              AND (v.version > ? OR v.created > ?)
            """;
        return jdbcTemplate.query(sql, rowMapper, sinceVersion.source().getId(), sinceVersion.version(), sinceTimestamp);
    }

}
