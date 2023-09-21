package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotFileVersionInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;


@Repository
public class SnapshotFileDao {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<SnapshotFile> rowMapper = (rs, rowNum) ->
        new SnapshotFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4)
        );

    private static final RowMapper<SnapshotFileVersionInfo> rowMapperWithVersion = (rs, rowNum) ->
            new SnapshotFileVersionInfo(
                    rowMapper.mapRow(rs, rowNum),
                    NrtmVersionInfoDao.rowMapperWithOffset.apply(4).mapRow(rs, rowNum)
            );

    public SnapshotFileDao(@Qualifier("nrtmMasterDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Optional<SnapshotFile> getByName(final String name) {
        final String sql = """
            SELECT sf.id, sf.version_id, sf.name, sf.hash
            FROM snapshot_file sf
            JOIN version_info v ON v.id = sf.version_id
            WHERE sf.name = ?
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, name));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<SnapshotFile> getLastSnapshot(final NrtmSource source) {
        final String sql = """
            SELECT sf.id, sf.version_id, sf.name, sf.hash
            FROM snapshot_file sf
            JOIN version_info v ON v.id = sf.version_id
            WHERE v.source_id = ?
            ORDER BY v.version DESC LIMIT 1
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, source.getId()));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<SnapshotFileVersionInfo> getLastSnapshotWithVersion(final NrtmSource source) {
        final String sql = """
            SELECT
             sf.id, sf.version_id, sf.name, sf.hash,
             v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
            FROM snapshot_file sf
            JOIN version_info v ON v.id = sf.version_id
            JOIN source src ON src.id = v.source_id
            WHERE v.source_id = ?
            ORDER BY v.version DESC LIMIT 1
            """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapperWithVersion, source.getId()));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
