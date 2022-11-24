package net.ripe.db.nrtm4.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RowMapper<VersionInformation> rowMapper = (rs, rowNum) ->
        new VersionInformation(
            rs.getLong(1),
            NrtmSourceHolder.valueOf(rs.getString(2)),
            rs.getLong(3),
            UUID.fromString(rs.getString(4)),
            NrtmDocumentType.valueOf(rs.getString(5)),
            rs.getInt(6)
        );

    @Autowired
    public NrtmVersionInfoRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Finds the last version saved to the version table.
     *
     * @param source Find version only for this source
     * @return Optional version object, if one was found
     */
    public Optional<VersionInformation> findLastVersion(final NrtmSource source) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("" +
                    "SELECT vi.id, src.name, vi.version, vi.session_id, vi.type, vi.last_serial_id " +
                    "FROM version_information vi JOIN source src ON src.id = vi.source_id " +
                    "WHERE src.name = ? " +
                    "ORDER BY vi.version DESC LIMIT 1",
                rowMapper,
                source.name())
            );
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersion found no entries, and so threw exception: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Creates a row in the version_information table for an initial snapshot.
     *
     * @param source The source which is being initialized
     * @param lastSerialId The last serialID from the Whois serials table which is in the snapshot
     * @return An initialized version object filled from the new row in the database
     */
    public VersionInformation createInitialSnapshot(final NrtmSource source, final int lastSerialId) {
        jdbcTemplate.update("INSERT INTO source (name) VALUES (?)", source.name());
        final long version = 1L;
        final UUID sessionID = UUID.randomUUID();
        final NrtmDocumentType type = NrtmDocumentType.snapshot;
        return save(source, version, sessionID, type, lastSerialId);
    }

    /**
     * Using a given version, create a new incremented version number for a serialID.
     *
     * @param version Existing version
     * @param serialId The last serialID from the Whois serials table which is in this version
     * @return An incremented version object
     */
    public VersionInformation incrementAndSave(final VersionInformation version, final int serialId) {
        return save(version.getSource(), version.getVersion() + 1, version.getSessionID(), version.getType(), serialId);
    }

    /**
     * Makes a copy of an existing delta version object so it can be used for a snapshot. If the input version
     * was used for a snapshot, throws an IllegalStateException
     *
     * @param version A version already in use for a delta
     * @return A version object which will be used for a snapshot
     */
    public VersionInformation copyAsSnapshotVersion(final VersionInformation version) {
        if (version.getType() == NrtmDocumentType.snapshot) {
            throw new IllegalStateException("Cannot copy a snapshot version number - must be a delta version");
        }
        return save(version.getSource(), version.getVersion(), version.getSessionID(), NrtmDocumentType.snapshot, version.getLastSerialId());
    }

    private VersionInformation save(
        final NrtmSource source,
        final long version,
        final UUID sessionID,
        final NrtmDocumentType type,
        final int lastSerialId) {
        final Long sourceID = jdbcTemplate.queryForObject("SELECT id FROM source WHERE name = ?",
            (rs, rowNum) -> rs.getLong(1), source.name());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO version_information (source_id, version, session_id, type, last_serial_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, sourceID);
                pst.setLong(2, version);
                pst.setString(3, sessionID.toString());
                pst.setString(4, type.name());
                pst.setInt(5, lastSerialId);
                return pst;
            }, keyHolder
        );
        return new VersionInformation(keyHolder.getKeyAs(Long.class), source, version, sessionID, type, lastSerialId);
    }

}