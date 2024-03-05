package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.base.Strings;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.mail.BouncedMessage;
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

    public BouncedMessageService(final UndeliverableMailDao undeliverableMailDao,
                                final OutgoingMessageDao outgoingMessageDao, final BouncedMessageParser bouncedMessageParser){
        this.undeliverableMailDao = undeliverableMailDao;
        this.outgoingMessageDao = outgoingMessageDao;
        this.bouncedMessageParser = bouncedMessageParser;

    }

    public boolean isBouncedMessage(final MimeMessage message) throws MessagingException, IOException {
        final BouncedMessage bouncedMessage = bouncedMessageParser.parse(message);
        LOGGER.info("bounce message is null?{}", bouncedMessage != null);
        if (bouncedMessage != null) {
            markUndeliverable(bouncedMessage);
            return true;
        }
        return false;
    }

    private void markUndeliverable(final BouncedMessage bouncedMessage) {
        final String email = outgoingMessageDao.getEmail(bouncedMessage.getMessageId());
        if (Strings.isNullOrEmpty(email)) {
            LOGGER.warn("Couldn't find outgoing message matching {}", bouncedMessage.getMessageId());
            return;
        }
        //TODO: Test case
        if (!email.equalsIgnoreCase(bouncedMessage.getEmailAddress())) {
            LOGGER.warn("Email {} in outgoing message doesn't match '{}' in failure response", email, bouncedMessage.getEmailAddress());
            return;
        }
        undeliverableMailDao.createUndeliverableEmail(email);
    }

}
