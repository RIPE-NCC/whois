package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;


@Repository
public class NotificationFileDao {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<NotificationFile> rowMapper = (rs, rowNum) ->
        new NotificationFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getLong(3),
            rs.getString(4)
        );

    public NotificationFileDao(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(final NotificationFile file) {
        final String sql = """
            INSERT INTO notification_file (version_id, created, payload)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(sql, file.versionId(), file.created(), file.payload());
    }

    public Optional<NotificationFile> findLastNotification(final NrtmSourceModel source) {
        try {
            final String sql = """
                SELECT nf.id, nf.version_id, nf.created, nf.payload
                FROM notification_file nf
                JOIN version_info vi ON vi.id = nf.version_id
                WHERE vi.source_id = ?
                ORDER BY nf.created DESC LIMIT 1
                """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, source.getId()));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
