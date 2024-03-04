package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@Tag("IntegrationTest")
public class BouncedMessageServiceTestIntegration extends AbstractIntegrationTest {
    @Autowired
    private MailSenderStub mailSenderStub;
    @Autowired
    private MailUpdatesTestSupport mailUpdatesTestSupport;

    @Autowired
    private BouncedMessageService bouncedMessageService;

    private static final String BOUNCED_MAIL_RECIPIENT = "nonexistant@host.org";

    @BeforeAll
    public static void setSmtpFrom() {
        // Email address to use for SMTP MAIL command. This sets the envelope return address.
        // Ref. https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html
        System.setProperty("mail.smtp.from", "bounce-handler@ripe.net");
    }

    @AfterAll
    public static void clearSmtpFrom() {
        System.clearProperty("mail.smtp.from");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject(
                "person:        Test Person\n" +
                        "nic-hdl:       TP1-TEST\n" +
                        "source:        TEST");
        databaseHelper.addObject(
                "mntner:        OWNER-MNT\n" +
                        "descr:         Owner Maintainer\n" +
                        "admin-c:       TP1-TEST\n" +
                        "upd-to:        upd-to@ripe.net\n" +
                        "notify:        notify@ripe.net\n" +
                        "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST");
    }

    @Test
    public void testNoBouncedEmailFromCorrectEmail() throws MessagingException, IOException {
        final String role =
                "role:        dummy role\n" +
                        "address:       Singel 258\n" +
                        "e-mail:        dummyrole@ripe.net\n" +
                        "phone:         +31 6 12345678\n" +
                        "notify:        notify-dummy-role@ripe.net\n" +
                        "nic-hdl:       DR1-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST\n";

        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);

        insertOutgoingMessageId(acknowledgement.getMessageID(), "notify-dummy-role@ripe.net");
        assertThat(bouncedMessageService.isBouncedMessage(acknowledgement), is(false));
        assertThat(isUndeliverableAddress("notify-dummy-role@ripe.net"), is(false));
    }

    @Test
    public void testBouncedEmailFromIncorrectEmail() throws MessagingException, IOException {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "enduser@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");

        assertThat(bouncedMessageService.isBouncedMessage(message), is(true));
        assertThat(isUndeliverableAddress("enduser@ripe.net"), is(false));
    }

    @Test
    public void testBouncedEmailFromCorrectEmail() throws MessagingException, IOException {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", BOUNCED_MAIL_RECIPIENT);
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");

        assertThat(bouncedMessageService.isBouncedMessage(message), is(true));
        assertThat(isUndeliverableAddress(BOUNCED_MAIL_RECIPIENT), is(true));
    }

    private String insertIncomingMessage(final String subject, final String body) {
        return mailUpdatesTestSupport.insert(subject, body);
    }

    private void insertOutgoingMessageId(final String messageId, final String emailAddress) {
        internalsTemplate.update(
                "INSERT INTO outgoing_message (message_id, email) VALUES (?, ?)", messageId, emailAddress);
    }

    private boolean isUndeliverableAddress(final String emailAddress) {
        return internalsTemplate.queryForObject("SELECT count(email) FROM undeliverable_email WHERE email= ?",
                (rs, rowNum) -> rs.getInt(1), emailAddress) == 1;
    }
}
