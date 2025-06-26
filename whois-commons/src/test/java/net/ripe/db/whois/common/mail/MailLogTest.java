package net.ripe.db.whois.common.mail;

import com.google.common.collect.Sets;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MailLogTest {

    private static final Session SESSION = Session.getDefaultInstance(new Properties(), null);

    private Logger logger;
    private MailLog subject;

    @BeforeEach
    public void before() throws Exception {
        this.logger = mock(Logger.class);
        this.subject = new MailLog();
        FieldUtils.writeField(subject, "logger", logger, true);
    }

    @Test
    public void single_recipient() {
        subject.info("This is a test", "Test User <unread@ripe.net>");

        verify(logger).info(eq("{} {}"), eq("This_is_a_test"), eq("unread@ripe.net"));
    }

    @Test
    public void multiple_recipients() {
        subject.info("This is a test", Sets.newHashSet("Test User <unread@ripe.net>", "Another User <another@ripe.net>", "Final Test User <final@ripe.net>"));

        verify(logger).info(eq("{} {}"), eq("This_is_a_test"), eq("unread@ripe.net,another@ripe.net,final@ripe.net"));
    }

    @Test
    public void single_recipient_mimemessage() {
        subject.info(parse("From: Test DBM <test-dbm@ripe.net>\nTo: Test User <unread@ripe.net>\nSubject: This is a test\n\nHello.\n"));

        verify(logger).info(eq("{} {}"), eq("This_is_a_test"), eq("unread@ripe.net"));
    }

    private static MimeMessage parse(final String mimeMessage) {
        try {
            return new MimeMessage(SESSION, new ByteArrayInputStream(mimeMessage.getBytes()));
        } catch (MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

}
