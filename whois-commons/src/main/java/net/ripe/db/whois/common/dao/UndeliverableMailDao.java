package net.ripe.db.whois.common.dao;

import io.netty.util.internal.StringUtil;
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
public class UndeliverableMailDao {

    private final JdbcTemplate internalsMasterTemplate;

    private final JdbcTemplate internalsTemplate;

    @Autowired
    public UndeliverableMailDao(@Qualifier("internalsDataSource") final DataSource internalsMasterDatasource, @Qualifier("internalsSlaveDataSource") final DataSource internalsDatasource) {
        this.internalsMasterTemplate = new JdbcTemplate(internalsMasterDatasource);
        this.internalsTemplate = new JdbcTemplate(internalsDatasource);
    }

    public void saveOutGoingMessageId(final String uuid, final String email){
        internalsMasterTemplate.update("INSERT INTO outgoing_message (message_id, email, last_update) VALUES (?, ?, ?)", uuid, email, LocalDateTime.now());
    }

    @Nullable
    public String saveOutGoingMessageId(final String email) {
        return internalsTemplate.execute(
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

    public void createUndeliverableEmail(final String email){
        internalsMasterTemplate.update("INSERT INTO undeliverable_email (email, last_update) VALUES (?, ?)", email, LocalDateTime.now());
    }

    // TODO: make sure email address is normalised (i.e. user@host and nothing else)
    public Boolean isUndeliverableEmail(final String email){
        try {
            return internalsTemplate.queryForObject("SELECT email from undeliverable_email where email = ?",
                    new RowMapper<>() {
                        @Override
                        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            // TODO: if not email then there aren't any rows, instead check for resultset.next() == false
                            return !StringUtil.isNullOrEmpty(rs.getString(1));
                        }
                    },
                    email);
        } catch (EmptyResultDataAccessException ex){
            return false;
        }
    }

    @Nullable
    public String getEmailByMessageId(final String messageId){
        try{
            return internalsTemplate.queryForObject("SELECT email from outgoing_message where message_id = ?",
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
