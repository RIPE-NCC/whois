package net.ripe.db.whois.common.dao;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.common.mail.EmailStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Repository
public class EmailStatusDao {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public EmailStatusDao(@Qualifier("internalsDataSource") final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public void createEmailStatus(final String email, final EmailStatusType emailStatus, final MimeMessage message) throws MessagingException, IOException {
        jdbcTemplate.update("INSERT INTO email_status (email, status, message, last_update) VALUES (?, ?, ?, ?)", email,
                emailStatus.name(),
                getMimeMessageBytes(message),
                LocalDateTime.now());
    }

    public void createEmailStatus(final String email, final EmailStatusType emailStatus) {
        jdbcTemplate.update("INSERT INTO email_status (email, status, last_update) VALUES (?, ?, ?)", email,
                emailStatus.name(),
                LocalDateTime.now());
    }

    public Map<String, EmailStatusType> getEmailStatusMap(final Set<String> emailAddresses) {
        if( emailAddresses.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        final Map<String, EmailStatusType> results = Maps.newHashMap();

        namedParameterJdbcTemplate.query(
                "SELECT email, status FROM email_status WHERE email IN (:emails)",
                Map.of("emails", emailAddresses),
                resultSet -> {
                    final String email = resultSet.getString(1);
                    final EmailStatusType status = EmailStatusType.valueOf(resultSet.getString(2));
                    results.put(email,status);
                });

        return results;
    }

    public Set<EmailStatus> getEmailStatus(final Set<String> emailAddresses) {
        if (emailAddresses.isEmpty()) {
            return Sets.newHashSet();
        }

        final Set<EmailStatus> results = Sets.newHashSet();

        namedParameterJdbcTemplate.query(
                "SELECT email, status, last_update FROM email_status WHERE email IN (:emails)",
                Map.of("emails", emailAddresses),
                resultSet -> {
                    final String email = resultSet.getString(1);
                    final EmailStatusType status = EmailStatusType.valueOf(resultSet.getString(2));
                    final LocalDateTime date = LocalDateTime.parse(resultSet.getString(3), formatter);
                    results.add(new EmailStatus(email, status, date));
                });

        return results;
    }


    private static byte[] getMimeMessageBytes(final MimeMessage message) throws MessagingException, IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        message.writeTo(byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
