package net.ripe.db.whois.api.mail.dequeue;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.MessageInfo;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.mail.EmailStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public MessageInfo getBouncedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return bouncedMessageParser.parse(message);
    }
    public MessageInfo getUnsubscribedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return unsubscribeMessageParser.parse(message);
    }

    public void verifyAndSetAsUndeliverable(final MessageInfo message){
        final List<String> emails = outgoingMessageDao.getEmails(message.messageId());

        if (isIncorrectMessage(message, emails)){
            return;
        }

        LOGGER.debug("Undeliverable message-id {} email {}", message.messageId(), message.emailAddresses());
        emails.forEach(email -> emailStatusDao.createEmailStatus(email, EmailStatus.UNDELIVERABLE));
    }

    public void verifyAndSetAsUnsubscribed(final MessageInfo message){
        final List<String> emails = outgoingMessageDao.getEmails(message.messageId());

        if (isIncorrectMessage(message, emails)){
            return;
        }

        LOGGER.debug("Unsubscribe message-id {} email {}", message.messageId(), message.emailAddresses());
        emails.forEach(email -> emailStatusDao.createEmailStatus(email, EmailStatus.UNSUBSCRIBE));
    }

    private boolean isIncorrectMessage(final MessageInfo message, final List<String> emails){
        if (emails == null || emails.isEmpty()) {
            LOGGER.warn("Couldn't find outgoing message matching {}", message.messageId());
            return true;
        }

        if (!emails.containsAll(message.emailAddresses())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", emails, message.emailAddresses());
            return true;
        }
        return false;
    }

}
