package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.SnapshotFile;
import net.ripe.db.nrtm4.domain.SnapshotWithPayload;
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
public class SnapshotFileSourceAwareDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotFileSourceAwareDao.class);

    private static final RowMapper<SnapshotWithPayload> rowMapper = (rs, rowNum) ->
            new SnapshotWithPayload(
                    new SnapshotFile(rs.getLong(1),
                    rs.getLong(2),
                    rs.getString(3),
                    rs.getString(4)),
                    rs.getBytes(5)
            );
    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    public SnapshotFileSourceAwareDao(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, @Qualifier("nrtmSourceContext") final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    public Optional<SnapshotWithPayload> getByFileName(final String name) {
        final Optional<SnapshotWithPayload> snapshotFile = getSnapshotPayload(name);

        if(!snapshotFile.isEmpty()) {
            return snapshotFile;
        }

        final Source originalSource = sourceContext.getCurrentSource();
        if (originalSource.getType().equals(Source.Type.SLAVE)) {
            final Source masterSource = Source.master(originalSource.getName());
            try {
                sourceContext.setCurrent(masterSource);
                return getSnapshotPayload(name);
            } catch (IllegalSourceException e) {
                LOGGER.debug("Source not configured: {}", masterSource, e);
            } finally {
                sourceContext.setCurrent(originalSource);
            }
        }

        return snapshotFile;
    }

    private Optional<SnapshotWithPayload> getSnapshotPayload(final String name) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(" SELECT id, version_id, name, hash, payload  FROM snapshot_file WHERE name = ?",
                    rowMapper,
                    name));
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
