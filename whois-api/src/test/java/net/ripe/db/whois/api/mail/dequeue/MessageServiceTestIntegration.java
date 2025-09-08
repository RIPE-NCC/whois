package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
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


    private static final String FORMATTED_PASSWORD_WARN = """
            ***Warning: Password authentication will be removed from Mailupdates in a future
                        Whois release as the mail message may have been sent insecurely.
                        Please switch to PGP signing for authentication or use a different
                        update method such as the REST API or Syncupdates.
            """;

    private static final String PASSWORD_RELATED_WARN = FORMATTED_PASSWORD_WARN + """
          ***Warning: MD5 hashed password authentication is deprecated and support will be
                      removed at the end of 2025. Please switch to an alternative
                      authentication method before then.
          """;

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
    public void test_upd_single_object_with_password_then_warn() throws MessagingException, IOException {
        final String incomingMessage = """
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER-MNT
                source:        TEST
                password: test
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();
        assertThat(acknowledgement, containsString(FORMATTED_PASSWORD_WARN));
    }

    @Test
    public void test_upd_single_object_without_password_then_no_warn() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, not(containsString(FORMATTED_PASSWORD_WARN)));
    }

    @Test
    public void test_upd_multiple_objects_with_without_password_then_warn() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;


        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString(FORMATTED_PASSWORD_WARN));
    }

    @Test
    public void test_upd_multiple_objects_with_password_then_warn() throws MessagingException, IOException {
        final String incomingMessage = """
                mntner:        OWNER1-MNT
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        upd-to@ripe.net
                notify:        notify@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER1-MNT
                source:        TEST
                
                role:        dummy role
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR1-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                
                role:        dummy role 1
                address:       Singel 258
                e-mail:        dummyrole@ripe.net
                phone:         +31 6 12345678
                notify:        notify-dummy-role@ripe.net
                nic-hdl:       DR2-TEST
                mnt-by:        OWNER1-MNT
                source:        TEST
                password: test
                """;

        final String expectedGlobalWarn = String.format("""
                DETAILED EXPLANATION:

                %s
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                """, PASSWORD_RELATED_WARN);

        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", incomingMessage);
        final String acknowledgement = mailSenderStub.getMessage(from).getContent().toString();

        assertThat(acknowledgement, containsString(expectedGlobalWarn));
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
