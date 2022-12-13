package net.ripe.db.nrtm4.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;


@Repository
public class NotificationFileRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<NotificationFile> rowMapper = (rs, rowNum) ->
        new NotificationFile(
            rs.getLong(1),
            rs.getLong(2),
            rs.getString(3),
            rs.getLong(4)
        );

    private final String notificationFileFields = "id, version_id, payload, created ";

    @Autowired
    public NotificationFileRepository(@Qualifier("nrtmDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public NotificationFile save(
        final long versionId,
        final String payload
    ) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final long now = System.currentTimeMillis();
        jdbcTemplate.update(connection -> {
                final String sql = "" +
                    "INSERT INTO notification_file (version_id, payload, created) " +
                    "VALUES (?, ?, ?)";
                final PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pst.setLong(1, versionId);
                pst.setString(2, payload);
                pst.setLong(3, now);
                return pst;
            }, keyHolder
        );
        return new NotificationFile(keyHolder.getKeyAs(Long.class), versionId, payload, now);
    }

    // Assumes the latest notification has the highest ID, which should be ok coz it's autoincrement
    public NotificationFile getNotificationFile() {
        final String sql = "" +
            "SELECT " + notificationFileFields +
            "FROM notification_file " +
            "WHERE id = (SELECT max(id) FROM notification_file)";
        return jdbcTemplate.queryForObject(sql, rowMapper);
    }
}
