package net.ripe.db.whois.update.mail;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Profile({WhoisProfile.TEST})
@Component
public class MailSenderStub extends MailSenderBase implements Stub {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailSenderStub.class);

    private final Set<MimeMessage> messages = Collections.synchronizedSet(Sets.<MimeMessage>newHashSet());

    @Override
    public void reset() {
        messages.clear();
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) {
        try {
            final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(System.getProperties()));
            mimeMessagePreparator.prepare(mimeMessage);
//            LOGGER.info("Send message: {}\n\n{}\n\n", EnumerationUtils.toList(mimeMessage.getAllHeaderLines()), mimeMessage.getContent());
            messages.add(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Send message", e);
        }
    }

    public MimeMessage getMessage(final String to) throws MessagingException {
        final GetResponse getResponse = new GetResponse(to);

        try {
            Awaitility.await().atMost(30L, TimeUnit.SECONDS).until(getResponse);
            final MimeMessage message = getResponse.getMessage();
            messages.remove(message);
            return message;
        } catch (Exception e) {
            for (final MimeMessage message : messages) {
                LOGGER.warn("Got message for: {}", message.getRecipients(Message.RecipientType.TO)[0].toString());
            }
            throw new AssertionError("Unable to get message for: " + to, e);
        }
    }

    private class GetResponse implements Callable<Boolean> {
        private final String to;
        private MimeMessage message;

        public GetResponse(final String to) {
            this.to = to;
        }

        @Override
        public Boolean call() throws Exception {
            synchronized (messages) {
                for (MimeMessage message : messages) {
                    if (message.getRecipients(Message.RecipientType.TO)[0].toString().equalsIgnoreCase(to)) {
                        this.message = message;
                        return true;
                    }
                }
            }

            return false;
        }

        public MimeMessage getMessage() {
            return message;
        }

    }

    public boolean anyMoreMessages() {
        if (!messages.isEmpty()) {
            synchronized (messages) {
                for (Message message : messages) {
                    try {
                        Address[] to = message.getRecipients(Message.RecipientType.TO);
                        LOGGER.warn("Found message to: {}, subject: {}", Arrays.deepToString(to), message.getSubject());
                    } catch (MessagingException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }

        return !messages.isEmpty();
    }

    public List<Address> getAllRecipients() {
        final List<Address> addresses = Lists.newArrayList();
        synchronized (messages) {
            for (Message message : messages) {
                try {
                    addresses.addAll(Arrays.asList(message.getRecipients(Message.RecipientType.TO)));
                } catch (MessagingException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return addresses;
    }
}
