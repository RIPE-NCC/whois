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
public class UndeliverableMailDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UndeliverableMailDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createUndeliverableEmail(final String email){
        jdbcTemplate.update("INSERT INTO undeliverable_email (email, last_update) VALUES (?, ?)", email, LocalDateTime.now());
    }

    // TODO: make sure email address is normalised (i.e. user@host and nothing else)
    public Boolean isUndeliverableEmail(final String email){
        try {
            return jdbcTemplate.queryForObject("SELECT email from undeliverable_email where email = ?",
                    new RowMapper<>() {
                        @Override
                        public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            // TODO: if not email then there aren't any rows, instead check for resultset.next() == false
                            return !StringUtil.isNullOrEmpty(rs.getString(1));
                        }
                    },
                    email);
        } catch (EmptyResultDataAccessException ex) {
            // TODO: don't rely on exception to detect missing row, as creating them is slow
            return false;
        }
    }


}
