package net.ripe.db.whois.api.mail.dequeue;


import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.MessageInfo;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UnsubscribeMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeMessageService.class);

    private final UnsubscribeMessageParser unsubscribeMessageParser;
    private final OutgoingMessageDao outgoingMessageDao;
    private final UndeliverableMailDao undeliverableMailDao;
    private final BouncedMessageParser bouncedMessageParser;

    @Autowired
    public UnsubscribeMessageService(
            final UnsubscribeMessageParser unsubscribeMessageParser,
            final BouncedMessageParser bouncedMessageParser,
            final OutgoingMessageDao outgoingMessageDao,
            final UndeliverableMailDao undeliverableMailDao) {
        this.unsubscribeMessageParser = unsubscribeMessageParser;
        this.bouncedMessageParser = bouncedMessageParser;
        this.outgoingMessageDao = outgoingMessageDao;
        this.undeliverableMailDao = undeliverableMailDao;
    }

    public MessageInfo getBouncedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return bouncedMessageParser.parse(message);
    }
    public MessageInfo getUnsubscribedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return unsubscribeMessageParser.parse(message);
    }

    public void verifyAndSetAsUndeliverable(final MessageInfo message) {
        final String email = outgoingMessageDao.getEmail(message.messageId());

        if (Strings.isNullOrEmpty(email)) {
            LOGGER.warn("Couldn't find outgoing message matching {}", message.messageId());
            return;
        }

        if (!email.equalsIgnoreCase(message.emailAddress())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", email, message.emailAddress());
            return;
        }

        LOGGER.debug("Unsubscribe message-id {} email {}", message.messageId(), message.emailAddress());
        undeliverableMailDao.createUndeliverableEmail(email);
    }

}
