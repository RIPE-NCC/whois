package net.ripe.db.whois.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
    public String saveOutGoingMessageId(final String email) {
        return jdbcTemplate.execute(
            connection -> {
                final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO outgoing_message (message_id, email) VALUES (uuid(), ?) RETURNING message_id", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, email);
                return preparedStatement;
            },
            (PreparedStatementCallback<String>) preparedStatement -> {
                if (preparedStatement.execute()) {
                    final ResultSet resultSet = preparedStatement.getResultSet();
                    if (resultSet != null && resultSet.next()) {
                          return resultSet.getString("message_id");
                    }
                }
                return null;
        });
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
