package net.ripe.db.whois.api.mail.dequeue;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.mail.EmailStatusType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@Service
public class MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final AutoSubmittedMessageParser autoSubmittedMessageParser;
    private final UnsubscribeMessageParser unsubscribeMessageParser;
    private final OutgoingMessageDao outgoingMessageDao;
    private final EmailStatusDao emailStatusDao;
    private final BouncedMessageParser bouncedMessageParser;

    @Autowired
    public MessageService(
            final AutoSubmittedMessageParser autoSubmittedMessageParser,
            final UnsubscribeMessageParser unsubscribeMessageParser,
            final BouncedMessageParser bouncedMessageParser,
            final OutgoingMessageDao outgoingMessageDao,
            final EmailStatusDao emailStatusDao) {
        this.autoSubmittedMessageParser = autoSubmittedMessageParser;
        this.unsubscribeMessageParser = unsubscribeMessageParser;
        this.bouncedMessageParser = bouncedMessageParser;
        this.outgoingMessageDao = outgoingMessageDao;
        this.emailStatusDao = emailStatusDao;
    }

    @Nullable
    public EmailMessageInfo getBouncedMessageInfo(final MimeMessage message) throws MessagingException, MailParsingException {
        return bouncedMessageParser.parse(message);
    }

    @Nullable
    public EmailMessageInfo getUnsubscribedMessageInfo(final MimeMessage message) throws MessagingException, MailParsingException {
        return unsubscribeMessageParser.parse(message);
    }

    @Nullable
    public EmailMessageInfo getAutomatedFailureMessageInfo(final MimeMessage message) throws MessagingException, MailParsingException {
        return autoSubmittedMessageParser.parse(message);
    }

    public void verifyAndSetAsUndeliverable(final EmailMessageInfo messageInfo) {
        final List<String> outgoingEmail = outgoingMessageDao.getEmails(messageInfo.messageId());

        if (!isValidMessage(messageInfo, outgoingEmail)) {
            return;
        }

        LOGGER.debug("Undeliverable message-id {} email {}", messageInfo.messageId(), StringUtils.join(messageInfo.emailAddresses(), ", "));
        for (String email : messageInfo.emailAddresses()) {
            try {
                emailStatusDao.createEmailStatus(email, EmailStatusType.UNDELIVERABLE, messageInfo.message());
            } catch (DuplicateKeyException ex) {
                LOGGER.debug("Email already exist in EmailStatus table {}", StringUtils.join(messageInfo.emailAddresses(), ", "), ex);
            } catch (MessagingException | IOException e) {
                LOGGER.error("Unable to transform the bounced message of {} into byte[]", email);
                emailStatusDao.createEmailStatus(email, EmailStatusType.UNDELIVERABLE);
            }
        }
    }

    public void verifyAndSetAsUnsubscribed(final EmailMessageInfo message) {
        if (message.emailAddresses() != null && message.emailAddresses().size() != 1) {
            LOGGER.warn("This can not happen, unsubscribe with multiple recipients. messageId: {}", message.messageId());
            return;
        }

        final String unsubscribeRequestEmail = message.emailAddresses().get(0);
        final List<String> emails = outgoingMessageDao.getEmails(message.messageId());

        if (emails.stream().noneMatch(email -> email.equalsIgnoreCase(unsubscribeRequestEmail))) {
            LOGGER.debug("Couldn't find outgoing message matching {}", message.messageId());
            return;
        }

        LOGGER.debug("Unsubscribe message-id {} email {}", message.messageId(), unsubscribeRequestEmail);
        emailStatusDao.createEmailStatus(unsubscribeRequestEmail, EmailStatusType.UNSUBSCRIBE);

    }

    private boolean isValidMessage(final EmailMessageInfo message, final List<String> outgoingEmail) {
        if (message.messageId() == null || message.emailAddresses() == null || message.emailAddresses().isEmpty()) {
            LOGGER.info("Incorrect message {}", message.messageId());
            LOGGER.warn("Incorrect message <Filtered>");
            return false;
        }

        if (outgoingEmail == null || outgoingEmail.isEmpty()) {
            LOGGER.debug("Couldn't find outgoing message matching {}", message.messageId());
            return false;
        }

        if (!containsAllCaseInsensitive(message.emailAddresses(), outgoingEmail)) {
            LOGGER.info("Email {} in outgoing message doesn't match '{}' in failure response", outgoingEmail, StringUtils.join(message.emailAddresses(), ", "));
            LOGGER.debug("Email[Filtered] in outgoing message doesn't match '<Filtered>' in failure response");
            return false;
        }
        return true;
    }

    private boolean containsAllCaseInsensitive(final List<String> messageRecipients, final List<String> storedEmails) {
        final List<String> emailsInLowerCase = storedEmails
                .stream()
                .map(String::toLowerCase)
                .toList();

        return messageRecipients
                .stream()
                .map(String::toLowerCase)
                .allMatch(emailsInLowerCase::contains);
    }

}
