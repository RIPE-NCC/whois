package net.ripe.db.whois.api.mail.dequeue;


import com.google.common.base.Strings;
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
        final String email = outgoingMessageDao.getEmail(message.messageId());

        if (isIncorrectMessage(message, email)){
            return;
        }

        LOGGER.debug("Undeliverable message-id {} email {}", message.messageId(), message.emailAddress());
        emailStatusDao.createEmailStatus(email, EmailStatus.UNDELIVERABLE);
    }

    public void verifyAndSetAsUnsubscribed(final MessageInfo message){
        final String email = outgoingMessageDao.getEmail(message.messageId());

        if (isIncorrectMessage(message, email)){
            return;
        }

        LOGGER.debug("Unsubscribe message-id {} email {}", message.messageId(), message.emailAddress());
        emailStatusDao.createEmailStatus(email, EmailStatus.UNSUBSCRIBE);
    }

    private boolean isIncorrectMessage(final MessageInfo message, final String email){
        if (Strings.isNullOrEmpty(email)) {
            LOGGER.warn("Couldn't find outgoing message matching {}", message.messageId());
            return true;
        }

        if (!email.equalsIgnoreCase(message.emailAddress())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", email, message.emailAddress());
            return true;
        }
        return false;
    }

}
