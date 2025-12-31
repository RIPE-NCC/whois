package net.ripe.db.whois.api.mail.dao;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.domain.Hosts;
import net.ripe.db.whois.common.domain.Timestamp;
import net.ripe.db.whois.update.domain.DequeueStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.CheckForNull;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

@Repository
@RetryFor(value = {TransientDataAccessException.class, RecoverableDataAccessException.class})
public class MailMessageDaoJdbc implements MailMessageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailMessageDaoJdbc.class);

    private static final Session SESSION = Session.getInstance(new Properties());

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public MailMessageDaoJdbc(
            @Qualifier("mailupdatesDataSource") final DataSource dataSource,
            final DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @CheckForNull
    public String claimMessage() {
        final String uuid = UUID.randomUUID().toString();
        LOGGER.debug("[{}] about to claim message with uuid {}", Thread.currentThread().threadId(), uuid);
        final int rows = jdbcTemplate.update("" +
                "update mailupdates " +
                "set status = ?, changed = ?, claim_host = ?, claim_uuid = ? " +
                "where status is null " +
                "limit 1 ",
                DequeueStatus.CLAIMED.name(),
                Timestamp.from(dateTimeProvider.getCurrentDateTime()).getValue(),
                Hosts.getInstanceName(),
                uuid);
        switch (rows) {
            case 0:
                return null;
            case 1:
                LOGGER.debug("[{}] claimed message with uuid {}", Thread.currentThread().threadId(), uuid);
                return uuid;
            default:
                throw new IllegalStateException("Should never claim more than 1 row");
        }
    }

    @Override
    public void addMessage(final MimeMessage message) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            message.writeTo(baos);
            baos.close();
        } catch (MessagingException e) {
            throw new IllegalArgumentException("Invalid mime message", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid mime message", e);
        }

        final Object messageBytes = baos.toByteArray();
        jdbcTemplate.update("insert into mailupdates (message) values (?)", messageBytes);
    }

    @Override
    public MimeMessage getMessage(final String messageUuid) {
        LOGGER.debug("[{}] get message with uuid {}", Thread.currentThread().threadId(), messageUuid);

        final byte[] bytes = jdbcTemplate.queryForObject("select message from mailupdates where claim_uuid = ?", byte[].class, messageUuid);

        try {
            return new MimeMessage(SESSION, new ByteArrayInputStream(bytes));
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to parse message with id: " + messageUuid);
        }
    }

    @Override
    public void deleteMessage(final String messageUuid) {
        LOGGER.debug("[{}] delete message with uuid {}", Thread.currentThread().threadId(), messageUuid);

        int rows = jdbcTemplate.update("delete from mailupdates where claim_uuid = ?", messageUuid);
        if (rows != 1) {
            throw new IllegalArgumentException("Unable to delete message with id: " + messageUuid);
        }
    }

    @Override
    public void setStatus(final String messageUuid, final DequeueStatus status) {
        LOGGER.debug("[{}] set status uuid {} status {}", Thread.currentThread().threadId(), messageUuid, status);

        final int rows = jdbcTemplate.update(
                "update mailupdates set status = ?, changed = ? where claim_uuid = ?",
                status.name(),
                Timestamp.from(dateTimeProvider.getCurrentDateTime()).getValue(),
                messageUuid);

        if (rows != 1) {
            throw new IllegalArgumentException("Unable to set status for message with id: " + messageUuid);
        }
    }
}
