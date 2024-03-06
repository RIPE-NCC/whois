package net.ripe.db.whois.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@Repository
public class OutgoingMessageDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutgoingMessageDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void saveOutGoingMessageId(final String uuid, final String email){
        jdbcTemplate.update("INSERT INTO outgoing_message (message_id, email, last_update) VALUES (?, ?, ?)", uuid, email, LocalDateTime.now());
    }

    @Nullable
    public String getEmail(final String messageId) {
        return jdbcTemplate.query(
            "SELECT email from outgoing_message where message_id = ?",
            new Object[] { messageId },
            (ResultSet resultSet) -> {
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
                return null;
            });
    }

}
