package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NotificationFile;
import net.ripe.db.nrtm4.domain.NrtmSourceModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class NotificationFileDao {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<NotificationFile> rowMapper = (rs, rowNum) ->
        new NotificationFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3)
        );

    public NotificationFileDao(
        @Qualifier("nrtmDataSource") final DataSource dataSource
    ) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(final NotificationFile file) {
        final String sql = """
            INSERT INTO notification_file (version_id, payload)
            VALUES (?, ?)
            """;
        jdbcTemplate.update(sql, file.versionId(), file.payload());
    }

    public NotificationFile findLastNotification(final NrtmSourceModel source) {
        final String sql = """
            SELECT nf.id, nf.version_id, nf.payload
            FROM notification_file nf
            JOIN version_info vi ON vi.id = nf.version_id
            WHERE vi.source_id = ?
            ORDER BY vi.version DESC LIMIT 1
            """;
        return jdbcTemplate.queryForObject(sql, rowMapper, source.getId());
    }

}
