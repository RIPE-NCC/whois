package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.DeltaFile;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class DeltaFileSourceAwareDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaFileSourceAwareDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;
    private final static RowMapper<DeltaFile> rowMapper = (rs, rowNum) ->
            new DeltaFile(
                    rs.getLong(1),   // id
                    rs.getLong(2),   // version_id
                    rs.getString(3), // name
                    rs.getString(4), // hash
                    rs.getString(5)  // payload
            );

    public DeltaFileSourceAwareDao(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, @Qualifier("nrtmSourceContext") final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    public Optional<DeltaFile> getByFileName(final String name) {
        final Optional<DeltaFile> payload = getPayload(name);

        if(payload.isPresent()) {
            return payload;
        }

        final Source originalSource = sourceContext.getCurrentSource();
        if (originalSource.getType().equals(Source.Type.SLAVE)) {
            final Source masterSource = Source.master(originalSource.getName());
            try {
                sourceContext.setCurrent(masterSource);
                return getPayload(name);
            } catch (IllegalSourceException e) {
                LOGGER.debug("Source not configured: {}", masterSource, e);
            } finally {
                sourceContext.setCurrent(originalSource);
            }
        }

        return payload;
    }

    private Optional<DeltaFile> getPayload(final String name) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(" SELECT id, version_id, name, hash, payload  FROM delta_file WHERE name = ?",
                    rowMapper,
                    name)
            );
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
