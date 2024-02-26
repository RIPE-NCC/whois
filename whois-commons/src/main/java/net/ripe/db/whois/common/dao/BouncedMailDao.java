package net.ripe.db.whois.common.dao;

import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Repository
public class BouncedMailDao {

    private final JdbcTemplate internalsMasterTemplate;

    private final JdbcTemplate internalsTemplate;

    @Autowired
    public BouncedMailDao(@Qualifier("internalsDataSource") final DataSource internalsMasterDatasource, @Qualifier("internalsSlaveDataSource") final DataSource internalsDatasource) {
        this.internalsMasterTemplate = new JdbcTemplate(internalsMasterDatasource);
        this.internalsTemplate = new JdbcTemplate(internalsDatasource);
    }

    public void saveOnGoingMessageId(final String uuid, final String email){
        internalsMasterTemplate.update("INSERT INTO in_progress_message (messageId, email, last_update) VALUES (?, ?, ?)", uuid, email, LocalDateTime.now());
    }

    public void createBouncedEmail(final String email){
        internalsMasterTemplate.update("INSERT INTO undeliverable_email (email, last_update) VALUES (?, ?)", email, LocalDateTime.now());
    }

    public Boolean isBouncedEmail(final String email){
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
    public Boolean onGoingMessageExist(final String uuid){
        try {
            return internalsTemplate.queryForObject("SELECT messageId from in_progress_message where messageId = ?",
                    new RowMapper<>() {
                        @Override
                        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            // TODO: check rs.next() first
                            return !StringUtil.isNullOrEmpty(rs.getString(1));
                        }
                    },
                    uuid);
        } catch (EmptyResultDataAccessException ex){
            return false;
        }
    }

    public void deleteOnGoingMessage(final String uuid){
        internalsMasterTemplate.update("DELETE FROM in_progress_message WHERE messageId = ?", uuid);
    }
}
