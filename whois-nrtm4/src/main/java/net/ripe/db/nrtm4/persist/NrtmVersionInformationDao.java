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
public class NrtmVersionInformationDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmVersionInformationDao.class);
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
    public NrtmVersionInformationDao(@Qualifier("nrtmDataSource") final DataSource dataSource) {
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
        final long version = 1L;
        final UUID sessionID = UUID.randomUUID();
        final NrtmDocumentType type = NrtmDocumentType.snapshot;
        return save(source, version, sessionID, type);
    }

    public VersionInformation incrementAndSave(final VersionInformation version) {
        return save(version.getSource(), version.getVersion() + 1, version.getSessionID(), version.getType());
    }

    private VersionInformation save(
            final NrtmSource source,
            final long version,
            final UUID sessionID,
            final NrtmDocumentType type) {
        final Long sourceID = jdbcTemplate.queryForObject("select id from source where name = ?",
                (rs, rowNum) -> rs.getLong(1), source.name());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                    final String sql = "insert into version_information (source_id, version, session_id, type) " +
                            "values (?, ?, ?, ?)";
                    final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setLong(1, sourceID);
                    pst.setLong(2, version);
                    pst.setString(3, sessionID.toString());
                    pst.setString(4, type.name());
                    return pst;
                }, keyHolder
        );
        // TODO: get the ID and return it here
        return new VersionInformation(keyHolder.getKeyAs(Long.class), source, version, sessionID, type);
    }

}