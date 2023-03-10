package net.ripe.db.nrtm4.dao;

import net.ripe.db.nrtm4.domain.NotificationFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class NotificationFileDao {

    private final JdbcTemplate jdbcTemplate;

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
}
