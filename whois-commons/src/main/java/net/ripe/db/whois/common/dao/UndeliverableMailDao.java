package net.ripe.db.whois.common.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@Repository
public class UndeliverableMailDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UndeliverableMailDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createUndeliverableEmail(final String email) {
        jdbcTemplate.update("INSERT INTO undeliverable_email (email, last_update) VALUES (?, ?)", email, LocalDateTime.now());
    }

    // TODO: make sure email address is normalised (i.e. user@host and nothing else)
    public boolean isUndeliverable(final String emailAddress) {
        return jdbcTemplate.query(
            "SELECT email from undeliverable_email where email = ?",
            new Object[] { emailAddress },
            (ResultSet resultSet) -> resultSet.next());
    }


}
