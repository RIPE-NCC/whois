package net.ripe.db.nrtm4.dao;

import com.google.common.collect.Maps;
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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@Repository
public class SnapshotSourceAwareFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotSourceAwareFileRepository.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    public SnapshotSourceAwareFileRepository(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    public Map<String, Object> getByFileName(final String name) {
        final Map<String, Object> payloadWithHash = getPayload(name);

        if(!payloadWithHash.isEmpty()) {
            return payloadWithHash;
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

        return payloadWithHash;
    }

    private Map<String, Object> getPayload(final String name) {
        try {
            return jdbcTemplate.queryForMap(" SELECT payload, hash  FROM snapshot_file WHERE name = ?",
                    name);
        } catch (final EmptyResultDataAccessException ex) {
            return Collections.emptyMap();
        }
    }
}
