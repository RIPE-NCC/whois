package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmDocumentType;
import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.nrtm4.domain.NrtmSourceHolder;
import net.ripe.db.nrtm4.domain.NrtmVersionInfo;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
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
import java.util.UUID;


@Repository
public class NrtmVersionInfoRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionInfoRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;
    private final RowMapper<NrtmVersionInfo> rowMapper = (rs, rowNum) ->
        new NrtmVersionInfo(
            rs.getLong(1),
            rs.getLong(2),
            NrtmSourceHolder.valueOf(rs.getString(3)),
            rs.getLong(4),
            rs.getString(5),
            NrtmDocumentType.valueOf(rs.getString(6)),
            rs.getInt(7),
            rs.getInt(8)
        );

    public NrtmVersionInfoRepository(
        @Qualifier("nrtmDataSource") final DataSource dataSource,
        final DateTimeProvider dateTimeProvider
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dateTimeProvider = dateTimeProvider;
    }

    /**
     * Finds the last version saved to the version table.
     *
     * @param source Find version only for this source
     * @return Optional version object, if one was found
     */
//    public Optional<NrtmVersionInfo> findLastVersion(final NrtmSource source) {
//        try {
//            return Optional.ofNullable(jdbcTemplate.queryForObject("""
//                    SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
//                    FROM version_info v
//                    JOIN source src ON src.id = v.source_id
//                    WHERE src.name = ?
//                    ORDER BY v.version DESC LIMIT 1
//                    """,
//                rowMapper,
//                source.name())
//            );
//        } catch (final EmptyResultDataAccessException ex) {
//            LOGGER.debug("findLastVersion found no entries, and so threw exception: {}", ex.getMessage());
//            return Optional.empty();
//        }
//    }

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
    public NrtmVersionInfo createInitialVersion(final NrtmSource source, final int lastSerialId) {
        final long version = 1L;
        final String sessionID = UUID.randomUUID().toString();
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
        final long now = JdbcRpslObjectOperations.now(dateTimeProvider);
        final Long sourceID = jdbcTemplate.queryForObject(
            "SELECT id FROM source WHERE name = ?",
            (rs, rowNum) -> rs.getLong(1), source.name());
        jdbcTemplate.update(connection -> {
                final String sql = """
                    INSERT INTO version_info (source_id, version, session_id, type, last_serial_id, created)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, sourceID);
                pst.setLong(2, version);
                pst.setString(3, sessionID);
                pst.setString(4, type.name());
                pst.setInt(5, lastSerialId);
                pst.setLong(6, now);
                return pst;
            }, keyHolder
        );
        return new NrtmVersionInfo(keyHolder.getKeyAs(Long.class), sourceID, source, version, sessionID, type, lastSerialId, now);
    }

    public Optional<NrtmVersionInfo> findById(final long versionId) {
        try {
            final String sql = """
                SELECT v.id, src.id, src.name, v.version, v.session_id, v.type, v.last_serial_id, v.created
                FROM version_info v
                JOIN source src ON src.id = v.source_id
                WHERE v.id = ?
                """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, versionId));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
