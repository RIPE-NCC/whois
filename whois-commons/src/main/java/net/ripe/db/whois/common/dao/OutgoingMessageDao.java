package net.ripe.db.whois.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public String getEmailByMessageId(final String messageId){
        try{
            return jdbcTemplate.queryForObject("SELECT email from outgoing_message where message_id = ?",
                    new RowMapper<>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            // TODO: check rs.next() first
                            return rs.getString(1);
                        }
                    },
                    messageId);
        } catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

}
