package net.ripe.db.nrtm4.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ripe.db.nrtm4.domain.DeltaChange;
import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.nrtm4.domain.DeltaFileVersionInfo;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.nrtm4.domain.PublishableDeltaFile;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;


@Repository
public class DeltaFileDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
        new DeltaFile(
            rs.getLong(1),   // id
            rs.getLong(2),   // version_id
            rs.getString(3), // name
            rs.getString(4), // hash
            rs.getString(5)  // payload
        );

    private final RowMapper<DeltaFileVersionInfo> rowMapperWithVersion = (rs, rowNum) ->
        new DeltaFileVersionInfo(
            rowMapper.mapRow(rs, rowNum),
            NrtmVersionInfoRepository.rowMapperWithOffset.apply(5).mapRow(rs, rowNum)
        );

    public DeltaFileDao(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void storeDeltasAsPublishableFile(final NrtmVersionInfo newVersion, final List<DeltaChange> deltas) throws JsonProcessingException {
        final PublishableDeltaFile publishableDeltaFile = new PublishableDeltaFile(newVersion, deltas);
        final String json = new ObjectMapper().writeValueAsString(publishableDeltaFile);
        final String hash = NrtmFileUtil.calculateSha256(json.getBytes(StandardCharsets.UTF_8));
        final DeltaFile deltaFile = DeltaFile.of(newVersion.id(), NrtmFileUtil.newFileName(newVersion), hash, json);
        save(deltaFile.versionId(), deltaFile.name(), deltaFile.hash(), deltaFile.payload());
    }

    public Optional<DeltaFile> getByName(final String name) {
        final String sql = """
            SELECT df.id, df.version_id, df.name, df.hash, df.payload
            FROM delta_file df
            JOIN version_info vi ON vi.id = df.version_id
            WHERE df.name = ?
            """;
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, name));
    }

    public List<DeltaFileVersionInfo> getDeltasForNotification(final NrtmVersionInfo sinceVersion, final LocalDateTime since) {
        final long sinceTimestamp = since.toEpochSecond(ZoneOffset.UTC);
        final String sql = """
            SELECT
                df.id, df.version_id, df.name, df.hash, df.payload,
                vi.id, src.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id, vi.created
            FROM delta_file df
            JOIN version_info vi ON vi.id = df.version_id
            JOIN source src ON src.id = vi.source_id
            WHERE vi.source_id = ?
              AND (vi.version > ? OR vi.created > ?)
            ORDER BY vi.version ASC
            """;
        try {
            return jdbcTemplate.query(sql, rowMapperWithVersion, sinceVersion.source().getId(), sinceVersion.version(), sinceTimestamp);
        } catch (final DataAccessException e) {
            LOGGER.warn("Exception in getDeltasForNotification", e);
            return List.of();
        }
    }

    public List<DeltaFileVersionInfo> getAllDeltasForSourceSince(final NrtmSource source, final LocalDateTime since) {
        final long sinceTimestamp = since.toEpochSecond(ZoneOffset.UTC);
        final String sql = """
            SELECT
                df.id, df.version_id, df.name, df.hash, df.payload,
                vi.id, src.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id, vi.created
            FROM delta_file df
            JOIN version_info vi ON vi.id = df.version_id
            JOIN source src ON src.id = vi.source_id
            WHERE vi.source_id = ?
              AND vi.created >= ?
            ORDER BY vi.version ASC
            """;
        try {
            return jdbcTemplate.query(sql, rowMapperWithVersion, source.getId(), sinceTimestamp);
        } catch (final DataAccessException e) {
            LOGGER.warn("Exception in getDeltasForNotification", e);
            return List.of();
        }
    }


    public List<DeltaFileVersionInfo> getAllDeltas(final NrtmVersionInfo sinceVersion, final LocalDateTime since) {
        final long sinceTimestamp = since.toEpochSecond(ZoneOffset.UTC);
        final String sql = """
            SELECT
                df.id, df.version_id, df.name, df.hash, df.payload,
                vi.id, src.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id, vi.created
            FROM delta_file df
            JOIN version_info vi ON vi.id = df.version_id
            JOIN source src ON src.id = vi.source_id
            WHERE vi.source_id = ?
              AND (vi.version > ? OR vi.created > ?)
            ORDER BY vi.version ASC
            """;
        try {
            return jdbcTemplate.query(sql, rowMapperWithVersion, sinceVersion.source().getId(), sinceVersion.version(), sinceTimestamp);
        } catch (final DataAccessException e) {
            LOGGER.warn("Exception in getDeltasForNotification", e);
            return List.of();
        }
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
