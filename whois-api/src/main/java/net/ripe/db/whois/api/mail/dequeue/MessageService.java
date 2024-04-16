package net.ripe.db.whois.api.mail.dequeue;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.mail.EmailStatus;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final UnsubscribeMessageParser unsubscribeMessageParser;
    private final OutgoingMessageDao outgoingMessageDao;
    private final EmailStatusDao emailStatusDao;
    private final BouncedMessageParser bouncedMessageParser;

    @Autowired
    public MessageService(
            final UnsubscribeMessageParser unsubscribeMessageParser,
            final BouncedMessageParser bouncedMessageParser,
            final OutgoingMessageDao outgoingMessageDao,
            final EmailStatusDao emailStatusDao) {
        this.unsubscribeMessageParser = unsubscribeMessageParser;
        this.bouncedMessageParser = bouncedMessageParser;
        this.outgoingMessageDao = outgoingMessageDao;
        this.emailStatusDao = emailStatusDao;
    }

    public EmailMessageInfo getBouncedMessageInfo(final MimeMessage message) throws MessagingException, MailParsingException {
        return bouncedMessageParser.parse(message);
    }
    public EmailMessageInfo getUnsubscribedMessageInfo(final MimeMessage message) throws MessagingException, MailParsingException {
        return unsubscribeMessageParser.parse(message);
    }

    public void verifyAndSetAsUndeliverable(final EmailMessageInfo message){
        final List<String> outgoingEmail = outgoingMessageDao.getEmails(message.messageId());

        if (!isValidMessage(message, outgoingEmail)){
            return;
        }

        LOGGER.debug("Undeliverable message-id {} email {}", message.messageId(), StringUtils.join(message.emailAddresses(), ", "));
        message.emailAddresses().forEach(email -> {
            try {
                emailStatusDao.createEmailStatus(email, EmailStatus.UNDELIVERABLE);
            } catch (DuplicateKeyException ex) {
                LOGGER.debug("Email already exist in EmailStatus table {}", StringUtils.join(message.emailAddresses(), ", "), ex);
            }
        });
    }

    public void verifyAndSetAsUnsubscribed(final EmailMessageInfo message){
        if (message.emailAddresses() != null && message.emailAddresses().size() != 1){
            LOGGER.warn("This can not happen, unsubscribe with multiple recipients. messageId: {}", message.messageId());
            return;
        }

        final String unsubscribeRequestEmail = message.emailAddresses().get(0);
        final List<String> emails = outgoingMessageDao.getEmails(message.messageId());

        if (emails.stream().noneMatch(email -> email.equalsIgnoreCase(unsubscribeRequestEmail))){
            LOGGER.warn("Couldn't find outgoing message matching {}", message.messageId());
            return;
        }

        LOGGER.debug("Unsubscribe message-id {} email {}", message.messageId(), unsubscribeRequestEmail);
        emailStatusDao.createEmailStatus(unsubscribeRequestEmail, EmailStatus.UNSUBSCRIBE);
    }

    private boolean isValidMessage(final EmailMessageInfo message, final List<String> outgoingEmail){
        if (message.messageId() == null || message.emailAddresses() == null || message.emailAddresses().isEmpty()){
            LOGGER.warn("Incorrect message {}", message.messageId());
            return false;
        }

        if (outgoingEmail == null || outgoingEmail.isEmpty()) {
            LOGGER.warn("Couldn't find outgoing message matching {}", message.messageId());
            return false;
        }

        if (!containsAllCaseInsensitive(message.emailAddresses(), outgoingEmail)) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", outgoingEmail, StringUtils.join(message.emailAddresses(), ", "));
            return false;
        }
        return true;
    }

    private boolean containsAllCaseInsensitive(final List<String> messageRecipients, final List<String> storedEmails){
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
