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
public class NrtmVersionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionDao.class);
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<VersionInformation> rowMapper = (rs, rowNum) ->
            new VersionInformation(
                    rs.getLong(1),
                    NrtmSource.valueOf(rs.getString(2)),
                    rs.getLong(3),
                    UUID.fromString(rs.getString(4)),
                    NrtmDocumentType.valueOf(rs.getString(5))
            );

    @Autowired
    public NrtmVersionDao(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Optional<VersionInformation> findLastVersion(final NrtmSource source) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "select vi.id, src.name, vi.version, vi.session_id, vi.type " +
                            "from version_information vi join source src on src.id = vi.source_id " +
                            "where src.name = ? " +
                            "order by vi.version desc limit 1",
                    rowMapper,
                    source.name())
            );
        } catch (final EmptyResultDataAccessException ex) {
            LOGGER.debug("findLastVersion threw exception: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public VersionInformation createNew(final NrtmSource source) {

        jdbcTemplate.update("insert into source (name) values (?)", source.name());
        final Long sourceID = jdbcTemplate.queryForObject("select id from source where name = ?",
                (rs, rowNum) -> rs.getLong(1), source.name());
        final long version = 1L;
        final UUID sessionID = UUID.randomUUID();
        final NrtmDocumentType type = NrtmDocumentType.snapshot;
        jdbcTemplate.update(
                "insert into version_information (source_id, version, session_id, type) " +
                        "values (?, ?, ?, ?)",
                sourceID,
                version,
                sessionID.toString(),
                type.name()
        );
        // TODO: get the ID and return it here
        return new VersionInformation(0L, source, version, sessionID, type);
    }

}