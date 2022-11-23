package net.ripe.db.nrtm4.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;


@Repository
public class NrtmVersionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionRepository.class);
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<VersionInformation> rowMapper = (rs, rowNum) ->
        new VersionInformation(
            rs.getLong(1),
            NrtmSourceHolder.valueOf(rs.getString(2)),
            rs.getLong(3),
            UUID.fromString(rs.getString(4)),
            NrtmDocumentType.valueOf(rs.getString(5))
        );

    @Autowired
    public NrtmVersionRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Optional<VersionInformation> findLastVersion(final NrtmSource source) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("" +
                    "SELECT vi.id, src.name, vi.version, vi.session_id, vi.type " +
                    "FROM version_information vi JOIN source src ON src.id = vi.source_id " +
                    "WHERE src.name = ? " +
                    "ORDER BY vi.version DESC LIMIT 1",
                rowMapper,
                source.name())
            );
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersion threw exception: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public void createNew(final NrtmSource source) {

        jdbcTemplate.update("INSERT INTO source (name) VALUES (?)", source.name());
        final Long sourceID = jdbcTemplate.queryForObject(
            "SELECT id FROM source WHERE name = ?",
            (rs, rowNum) -> rs.getLong(1), source.name());
        final long version = 1L;
        final UUID sessionID = UUID.randomUUID();
        jdbcTemplate.update("" +
                "INSERT INTO version_information (source_id, version, session_id, type) " +
                "VALUES (?, ?, ?, ?)",
            sourceID,
            version,
            sessionID.toString(),
            NrtmDocumentType.snapshot.name()
        );
    }

}