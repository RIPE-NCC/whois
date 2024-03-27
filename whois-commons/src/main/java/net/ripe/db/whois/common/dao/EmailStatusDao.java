package net.ripe.db.whois.common.dao;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.mail.EmailStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
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

    public Map<String, EmailStatus> getEmailStatus(final Set<String> emailAddresses) {
        if( emailAddresses.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        final Map<String, EmailStatus> results = Maps.newHashMap();

        namedParameterJdbcTemplate.query(
                "SELECT email, status FROM email_status WHERE email IN (:emails)",
                Map.of("emails", emailAddresses),
                resultSet -> {
                    final String email = resultSet.getString(1);
                    final EmailStatus status = EmailStatus.valueOf(resultSet.getString(2));
                    results.put(email,status);
                });

        return results;
    }
}
