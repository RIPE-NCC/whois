package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.VersionedDeltaFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;


@Repository
public class DeltaFileDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
        new DeltaFile(
            rs.getLong(1),   // id
            rs.getLong(2),   // version_id
            rs.getString(3), // name
            rs.getString(4), // hash
            rs.getString(5)  // payload
        );

    private final RowMapper<VersionedDeltaFile> rowMapperWithVersion = (rs, rowNum) ->
        new VersionedDeltaFile(
            rs.getLong(1),   // id
            rs.getLong(2),   // version
            rs.getString(3), // session ID
            rs.getString(4), // name
            rs.getString(5)  // hash
        );

    public DeltaFileDao(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(final DeltaFile deltaFile) {
        save(deltaFile.versionId(), deltaFile.name(), deltaFile.hash(), deltaFile.payload());
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

    public List<VersionedDeltaFile> getDeltasForNotification(final NrtmVersionInfo sinceVersion, final long sinceTimestamp) {
        final String sql = """
            SELECT df.id, v.version, v.session_id, df.name, df.hash
            FROM delta_file df
            JOIN version_info v ON v.id = df.version_id
            JOIN source s ON s.id = v.source_id
            WHERE v.source_id = ?
              AND (v.version > ? OR v.created > ?)
            ORDER BY v.version ASC
            """;
        return jdbcTemplate.query(sql, rowMapperWithVersion, sinceVersion.source().getId(), sinceVersion.version(), sinceTimestamp);
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

}
