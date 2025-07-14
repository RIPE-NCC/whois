package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NrtmSource;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class UpdateNotificationFileSourceAwareDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileSourceAwareDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SourceContext sourceContext;

    public UpdateNotificationFileSourceAwareDao(@Qualifier("nrtmSourceAwareDataSource") final DataSource dataSource, @Qualifier("nrtmSourceContext") final SourceContext sourceContext) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sourceContext = sourceContext;
    }

    public Optional<String> findLastNotification(final NrtmSource source) {
        final Optional<String> payload = getPayload(source);

        if(payload.isPresent()) {
            return payload;
        }

        final Source originalSource = sourceContext.getCurrentSource();
        if (originalSource.getType().equals(Source.Type.SLAVE)) {
            LOGGER.info("Cannot find the entry in slave source , fetching it from master");

            final Source masterSource = Source.master(originalSource.getName());
            try {
                sourceContext.setCurrent(masterSource);
                return getPayload(source);
            } catch (IllegalSourceException e) {
                LOGGER.debug("Source not configured: {}", masterSource, e);
            } finally {
                sourceContext.setCurrent(originalSource);
            }
        }

        return payload;
    }

    private Optional<String> getPayload(final NrtmSource source) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("""
                                                SELECT nf.payload
                                                FROM notification_file nf
                                                JOIN version_info vi ON vi.id = nf.version_id
                                                WHERE vi.source_id = ?
                                                ORDER BY vi.version DESC LIMIT 1
                                                """,
                    (rs, rowNum) -> rs.getString(1),
                    source.getId())
            );
        } catch (final EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
