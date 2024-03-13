package net.ripe.db.whois.common.dao;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.mail.EmailStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Repository
public class EmailStatusDao {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public EmailStatusDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public void createEmailStatus(final String email, final EmailStatus emailStatus) {
        jdbcTemplate.update("INSERT INTO email_status (email, status, last_update) VALUES (?, ?, ?)", email,
                emailStatus.name(),
                LocalDateTime.now());
    }

    public Map<String, String> getEmailStatus(final Set<String> emailAddresses) {
        Map<String, String> results = Maps.newHashMap();

        namedParameterJdbcTemplate.query(
                "SELECT email, status from email_status where email in (:emails)",
                Map.of("emails", emailAddresses),
                resultSet -> {
                    results.put(resultSet.getString(1), resultSet.getString(2));
                });

        return results;
    }

    public boolean canNotSendEmail(final String emailAddress) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT email from email_status where email = ?",
                new Object[]{emailAddress},
                ResultSet::next));
    }
}
