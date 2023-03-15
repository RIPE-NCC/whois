package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.SnapshotFile;
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
public class SnapshotReadOnlyFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotReadOnlyFileRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    private static final RowMapper<SnapshotFile> rowMapper = (rs, rowNum) ->
        new SnapshotFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getString(4)
        );

    public SnapshotReadOnlyFileRepository(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }


    public Optional<byte[]> getByFileName(final String name) {
        final Optional<byte[]> payload = getPayload(name);

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

    private Optional<byte[]> getPayload(final String name) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(" SELECT payload  FROM snapshot_file WHERE name = ?",
                    (rs, rowNum) -> rs.getBytes(1),
                    name)
            );
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
