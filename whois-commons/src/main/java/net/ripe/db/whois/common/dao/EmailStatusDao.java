package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.mail.EmailStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.LocalDateTime;

@Repository
public class EmailStatusDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmailStatusDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createUndeliverableEmail(final String email, final EmailStatus emailStatus) {
        jdbcTemplate.update("INSERT INTO email_status (email, status, last_update) VALUES (?, ?, ?)", email,
                emailStatus,
                LocalDateTime.now());
    }

    public boolean isUndeliverable(final String emailAddress) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT email from email_status where email = ?",
                new Object[]{emailAddress},
                ResultSet::next));
    }
}
