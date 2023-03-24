package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;


@Repository
public class NrtmVersionInfoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionInfoRepository.class);
    static final Function<Integer, RowMapper<NrtmVersionInfo>> rowMapperWithOffset = (offset) -> (rs, rowNum) -> {
        final NrtmSourceModel source = new NrtmSourceModel(rs.getLong(offset + 2), CIString.ciString(rs.getString(offset + 3)));
        return new NrtmVersionInfo(
            rs.getLong(offset + 1),
            source,
            rs.getLong(offset + 4),
            rs.getString(offset + 5),
            NrtmDocumentType.valueOf(rs.getString(offset + 6)),
            rs.getInt(offset + 7),
            rs.getInt(offset + 8)
        );
    };
    private final RowMapper<NrtmVersionInfo> rowMapper = rowMapperWithOffset.apply(0);
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    public NrtmVersionInfoRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource,
        final DateTimeProvider dateTimeProvider
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    public List<NrtmVersionInfo> findLastVersionPerSource() {
        try {
            return jdbcTemplate.query("""
                    SELECT vi.id, src.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id, vi.created
                    FROM version_info vi
                        JOIN source src ON src.id = vi.source_id,
                        (SELECT source_id, MAX(version) version FROM version_info GROUP BY source_id) maxv
                    WHERE vi.source_id = maxv.source_id AND vi.version = maxv.version
                    ORDER BY vi.last_serial_id DESC
                    """,
                rowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersions found no entries");
            return List.of();
        }
    }

    public NrtmVersionInfo findLastSnapshotVersionForSource(final NrtmSourceModel source) {
        return jdbcTemplate.queryForObject("""
                SELECT vi.id, src.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id, vi.created
                FROM version_info vi
                    JOIN source src ON src.id = vi.source_id,
                    (
                    SELECT source_id, MAX(version) version
                    FROM version_info
                    WHERE type = ? AND source_id = ?
                    ) maxv
                WHERE vi.source_id = maxv.source_id
                  AND vi.version = maxv.version
                  AND vi.type = ?
                ORDER BY vi.last_serial_id DESC
                """,
            rowMapper, NrtmDocumentType.SNAPSHOT.toString(), source.getId(), NrtmDocumentType.SNAPSHOT.toString());
    }

    // TODO: Only used by tests, so should be removed from here.
    public Optional<NrtmVersionInfo> findLastVersion() {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                    SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
                    FROM version_info v
                    JOIN source src ON src.id = v.source_id
                    ORDER BY v.version DESC LIMIT 1
                    """,
                rowMapper)
            );
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersion found no entries, and so threw exception: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Creates a row in the version table for an initial snapshot.
     *
     * @param source       The source which is being initialized
     * @param lastSerialId The last serialID from the Whois serials table which is in the snapshot
     * @return An initialized version object filled from the new row in the database
     */
    public NrtmVersionInfo createInitialVersion(final NrtmSourceModel source, final int lastSerialId) {
        final long version = 1L;
        final String sessionID = UUID.randomUUID().toString();
        final NrtmDocumentType type = NrtmDocumentType.SNAPSHOT;
        return save(source, version, sessionID, type, lastSerialId);
    }

    public NrtmVersionInfo findById(final long versionId) {
        final String sql = """
            SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
            FROM version_info v
            JOIN source src ON src.id = v.source_id
            WHERE v.id = ?
            """;
        return jdbcTemplate.queryForObject(sql, rowMapper, versionId);
    }

    public NrtmVersionInfo saveNewDeltaVersion(final NrtmVersionInfo version, final int lastSerialID) {
        return save(version.source(), version.version() + 1, version.sessionID(), NrtmDocumentType.DELTA, lastSerialID);
    }

    public NrtmVersionInfo saveNewSnapshotVersion(final NrtmVersionInfo version) {
        return save(version.source(), version.version(), version.sessionID(), NrtmDocumentType.SNAPSHOT, version.lastSerialId());
    }

    private NrtmVersionInfo save(
        final NrtmSourceModel source,
        final long version,
        final String sessionID,
        final NrtmDocumentType type,
        final int lastSerialId) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        jdbcTemplate.update(connection -> {
                final String sql = """
                    INSERT INTO version_info (source_id, version, session_id, type, last_serial_id, created)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, source.getId());
                pst.setLong(2, version);
                pst.setString(3, sessionID);
                pst.setString(4, type.name());
                pst.setInt(5, lastSerialId);
                pst.setLong(6, now);
                return pst;
            }, keyHolder
        );
        return new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, lastSerialId, now);
    }

}
