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

// TODO: [ES] merge with BouncedMessageService? They are almost the same
@Service
public class UnsubscribeMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnsubscribeMessageService.class);

    private final UnsubscribeMessageParser unsubscribeMessageParser;
    private final OutgoingMessageDao outgoingMessageDao;
    private final UndeliverableMailDao undeliverableMailDao;


    @Autowired
    public UnsubscribeMessageService(
            final UnsubscribeMessageParser unsubscribeMessageParser,
            final OutgoingMessageDao outgoingMessageDao,
            final UndeliverableMailDao undeliverableMailDao) {
        this.unsubscribeMessageParser = unsubscribeMessageParser;
        this.outgoingMessageDao = outgoingMessageDao;
        this.undeliverableMailDao = undeliverableMailDao;
    }

    public MessageInfo getUnsubscribedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return unsubscribeMessageParser.parse(message);
    }

    public void verifyAndSetAsUnsubscribed(final MessageInfo unsubscribedMessage) {
        final String email = outgoingMessageDao.getEmail(unsubscribedMessage.messageId());

        if (Strings.isNullOrEmpty(email)) {
            LOGGER.warn("Couldn't find outgoing message matching {}", unsubscribedMessage.messageId());
            return;
        }

        if (!email.equalsIgnoreCase(unsubscribedMessage.emailAddress())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", email, unsubscribedMessage.emailAddress());
            return;
        }

        LOGGER.info("Unsubscribe message-id {} email {}", unsubscribedMessage.messageId(), unsubscribedMessage.emailAddress());
        undeliverableMailDao.createUndeliverableEmail(email);
    }

}
