package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class UpdateNotificationFileDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotificationFileDao.class);

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<NotificationFile> rowMapper = (rs, rowNum) ->
        new NotificationFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getLong(3),
            rs.getString(4)
        );

    public UpdateNotificationFileDao(@Qualifier("nrtmMasterDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(final NotificationFile file) {
        final String sql = """
            INSERT INTO notification_file (version_id, created, payload)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(sql, file.versionId(), file.created(), file.payload());
    }

    public void update(final NotificationFile file) {
        final String sql = """
            UPDATE notification_file set version_id =? , created =?,  payload=? 
            WHERE id =?
            """;
        jdbcTemplate.update(sql, file.versionId(), file.created(), file.payload(), file.id());
    }

    public Optional<NotificationFile> findLastNotification(final NrtmSource source) {
        try {
            final String sql = """
                SELECT nf.id, nf.version_id, nf.created, nf.payload
                FROM notification_file nf
                JOIN version_info vi ON vi.id = nf.version_id
                WHERE vi.source_id = ?
                """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, source.getId()));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (final IncorrectResultSizeDataAccessException exception) {
            LOGGER.error("More than one entry found for a source {} in update notification file", source.getName());
            throw new IllegalStateException("More than one entry found for a source in update notification file");
        }
    }
}
