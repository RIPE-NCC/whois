package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.MessageInfo;
import net.ripe.db.whois.common.dao.OutgoingMessageDao;
import net.ripe.db.whois.common.dao.UndeliverableMailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BouncedMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BouncedMessageService.class);

    private final UndeliverableMailDao undeliverableMailDao;
    private final OutgoingMessageDao outgoingMessageDao;
    private final BouncedMessageParser bouncedMessageParser;

    public BouncedMessageService(
            final UndeliverableMailDao undeliverableMailDao,
            final OutgoingMessageDao outgoingMessageDao,
            final BouncedMessageParser bouncedMessageParser){
        this.undeliverableMailDao = undeliverableMailDao;
        this.outgoingMessageDao = outgoingMessageDao;
        this.bouncedMessageParser = bouncedMessageParser;
    }

    public MessageInfo getBouncedMessageInfo(final MimeMessage message) throws MessagingException, IOException {
        return bouncedMessageParser.parse(message);
    }

    public void verifyAndSetAsUndeliverable(final MessageInfo bouncedMessage) {
        final String email = outgoingMessageDao.getEmail(bouncedMessage.messageId());
        if (Strings.isNullOrEmpty(email)) {
            LOGGER.warn("Couldn't find outgoing message matching {}", bouncedMessage.messageId());
            return;
        }

        if (!email.equalsIgnoreCase(bouncedMessage.emailAddress())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", email, bouncedMessage.emailAddress());
            return;
        }
        undeliverableMailDao.createUndeliverableEmail(email);
    }

}
