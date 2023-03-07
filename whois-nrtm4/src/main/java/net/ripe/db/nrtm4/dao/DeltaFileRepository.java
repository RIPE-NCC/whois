package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.NrtmFileService;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@Repository
public class DeltaFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NrtmFileService nrtmFileService;
    private final RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
        new DeltaFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4),
            rs.getString(5),
            rs.getLong(6)
        );

    public DeltaFileRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource,
        final NrtmFileService nrtmFileService
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.nrtmFileService = nrtmFileService;
    }

    public void save(final PublishableDeltaFile deltaFile, final String payload) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {

            final ByteArrayOutputStream bos = new ByteArrayOutputStream(json.length());
            bos.write(json.getBytes(StandardCharsets.UTF_8));
            bos.close();
            deltaFile.setHash(NrtmFileUtil.calculateSha256(bos));
            save(deltaFile.getVersionId(), deltaFile.getFileName(), deltaFile.getHash(), payload);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save(
        final long versionId,
        final String name,
        final String hash,
        final String payload
    ) {
        final long now = System.currentTimeMillis();
        final String sql = """
            INSERT INTO delta_file (version_id, name, hash, payload, created)
            VALUES (?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql, versionId, name, hash, payload, now);
    }

    public Optional<DeltaFile> getByName(final String sessionId, final String name) {
        final String sql = "" +
            "SELECT df.id, df.version_id, df.type, df.name, df.hash, df.payload, df.created " +
            "FROM delta_file df " +
            "JOIN version_info v ON v.id = df.version_id " +
            "WHERE v.session_id = ? " +
            "  AND df.name = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, sessionId, name));
    }

}
