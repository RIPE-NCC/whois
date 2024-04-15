package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@Tag("IntegrationTest")
public class MessageServiceTestIntegration extends AbstractMailMessageIntegrationTest {
    @Autowired
    private MailSenderStub mailSenderStub;

    @Autowired
    private MessageService messageService;

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
    public void testNoBouncedEmailFromCorrectEmail() throws MessagingException, MailParsingException {
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
        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(acknowledgement);
        assertThat(bouncedMessageInfo, is(nullValue()));
        assertThat(isUndeliverableAddress("enduser@ripe.net"), is(false));

    }

    @Test
    public void testBouncedEmailFromIncorrectEmail() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "enduser@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");

        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUndeliverable(bouncedMessageInfo);
        assertThat(isUndeliverableAddress("enduser@ripe.net"), is(false));
    }

    @Test
    public void testBouncedEmailFromCorrectEmail() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", BOUNCED_MAIL_RECIPIENT);
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");

        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUndeliverable(bouncedMessageInfo);
        assertThat(isUndeliverableAddress(BOUNCED_MAIL_RECIPIENT), is(true));
    }

    @Test
    public void testBouncedFailureRecipientFromCorrectEmail() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", BOUNCED_MAIL_RECIPIENT);
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", ANOTHER_BOUNCED_MAIL_RECIPIENT);

        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessagePartialReportWithMultipleRecipientsRfc822.mail");

        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUndeliverable(bouncedMessageInfo);
        assertThat(isUndeliverableAddress(BOUNCED_MAIL_RECIPIENT), is(true));
        assertThat(isUndeliverableAddress(ANOTHER_BOUNCED_MAIL_RECIPIENT), is(false));
    }

    @Test
    public void testBouncedMultipleFailurePerRecipientFromCorrectEmail() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("XXXXXXXX-8553-47AB-A79B-A9896A2DFBAC@ripe.net", BOUNCED_MAIL_RECIPIENT);
        insertOutgoingMessageId("XXXXXXXX-8553-47AB-A79B-A9896A2DFBAC@ripe.net", ANOTHER_BOUNCED_MAIL_RECIPIENT);

        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailurePerRecipientMessageRfc822.mail");

        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUndeliverable(bouncedMessageInfo);
        assertThat(isUndeliverableAddress(BOUNCED_MAIL_RECIPIENT), is(true));
        assertThat(isUndeliverableAddress(ANOTHER_BOUNCED_MAIL_RECIPIENT), is(true));
    }

    @Test
    public void testUnsubscribedEmailFromCorrectEmail() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net", UNSUBSCRIBED_MAIL_RECIPIENT);
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("unsubscribeAppleMail.mail");

        final EmailMessageInfo unsubscribedMessageInfo = messageService.getUnsubscribedMessageInfo(message);
        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(nullValue()));
        assertThat(unsubscribedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUnsubscribed(unsubscribedMessageInfo);
        assertThat(isUndeliverableAddress(UNSUBSCRIBED_MAIL_RECIPIENT), is(false));
        assertThat(isUnsubscribeAddress(UNSUBSCRIBED_MAIL_RECIPIENT), is(true));
    }

    @Test
    public void testUnsubscribedEmailFromCorrectEmailIsCaseInsensitive() throws MessagingException, MailParsingException {
        insertOutgoingMessageId("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net", "EnDuseR@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("unsubscribeAppleMail.mail");

        final EmailMessageInfo unsubscribedMessageInfo = messageService.getUnsubscribedMessageInfo(message);
        final EmailMessageInfo bouncedMessageInfo = messageService.getBouncedMessageInfo(message);
        assertThat(bouncedMessageInfo, is(nullValue()));
        assertThat(unsubscribedMessageInfo, is(not(nullValue())));

        messageService.verifyAndSetAsUnsubscribed(unsubscribedMessageInfo);
        assertThat(isUndeliverableAddress(UNSUBSCRIBED_MAIL_RECIPIENT), is(false));
        assertThat(isUnsubscribeAddress(UNSUBSCRIBED_MAIL_RECIPIENT), is(true));
    }
}
