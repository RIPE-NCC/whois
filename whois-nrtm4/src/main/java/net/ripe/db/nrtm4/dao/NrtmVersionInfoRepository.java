package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class NrtmVersionInfoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionInfoRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<NrtmVersionInfo> rowMapper = (rs, rowNum) ->
        new NrtmVersionInfo(
            rs.getLong(1),
            NrtmSourceHolder.valueOf(rs.getString(2)),
            rs.getLong(3),
            rs.getString(4),
            NrtmDocumentType.valueOf(rs.getString(5)),
            rs.getInt(6)
        );

    public NrtmVersionInfoRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Finds the last version saved to the version table.
     *
     * @param source Find version only for this source
     * @return Optional version object, if one was found
     */
    public Optional<NrtmVersionInfo> findLastVersion(final NrtmSource source) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    SELECT v.id, v.source, v.version, v.session_id, v.type, v.last_serial_id
                    FROM version_info v
                    WHERE v.source = ?
                    ORDER BY v.version DESC LIMIT 1
                    """,
                rowMapper,
                source.name())
            );
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersion found no entries, and so threw exception: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Creates a row in the version table for an initial snapshot.
     *
     * @param source The source which is being initialized
     * @param lastSerialId The last serialID from the Whois serials table which is in the snapshot
     * @return An initialized version object filled from the new row in the database
     */
    public NrtmVersionInfo createInitialVersion(final NrtmSource source, final int lastSerialId) {
        final long version = 1L;
        final String sessionID = NrtmFileUtil.sessionId();
        final NrtmDocumentType type = NrtmDocumentType.SNAPSHOT;
        return save(source, version, sessionID, type, lastSerialId);
    }

    private NrtmVersionInfo save(
        final NrtmSource source,
        final long version,
        final String sessionID,
        final NrtmDocumentType type,
        final int lastSerialId) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO version_info (source, version, session_id, type, last_serial_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setString(1, source.name());
                pst.setLong(2, version);
                pst.setString(3, sessionID);
                pst.setString(4, type.name());
                pst.setInt(5, lastSerialId);
                return pst;
            }, keyHolder
        );
        return new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, lastSerialId);
    }

    public NrtmVersionInfo findLastSnapshotVersion(final NrtmSource source) {
        final String sql = """
            SELECT v.id, v.source, v.version, v.session_id, v.type, v.last_serial_id
            FROM version_info v
            JOIN snapshot_file sf ON sf.version_id = v.id
            WHERE src.name = ?
            ORDER BY v.version DESC LIMIT 1
            """;
        return jdbcTemplate.queryForObject(sql, rowMapper, source);
    }

    public Optional<NrtmVersionInfo> findById(final long versionId) {
        final String sql = """
            SELECT v.id, v.source, v.version, v.session_id, v.type, v.last_serial_id
            FROM version_info v
            WHERE v.id = ?
            """;
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, versionId));
    }

}
