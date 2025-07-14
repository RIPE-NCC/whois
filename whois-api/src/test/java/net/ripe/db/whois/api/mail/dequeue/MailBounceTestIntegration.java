package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class MailBounceTestIntegration extends AbstractMailMessageIntegrationTest {

    @Autowired
    private MailSenderStub mailSenderStub;

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
        databaseHelper.updateObject(
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST");
    }

    @Test
    public void bouncing_mail_causes_address_to_be_marked_as_undeliverable() throws Exception {
        final String role =
                "role:        dummy role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dummyrole@ripe.net\n" +
                "phone:         +31 6 12345678\n" +
                "notify:        nonexistant@host.org\n" +
                "nic-hdl:       DR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n";

        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // Outgoing notification email to notify-dummy-role@ripe.net
        final MimeMessage notify = mailSenderStub.getMessage("nonexistant@host.org");
        final String notifyMessageId = notify.getHeader("Message-ID", "\n");
        assertThat(notifyMessageId, is(not(nullValue())));

        // Generate failure response for notification
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "nonexistant@host.org");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");
        insertIncomingMessage(message);

        // Wait for address to be marked as undeliverable
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("nonexistant@host.org")));

        // Make sure that failure response message was deleted
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void do_send_notification_mail_to_deliverable_address() throws Exception {
        final String role =
                "role:        dummy role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dummyrole@ripe.net\n" +
                "phone:         +31 6 12345678\n" +
                "notify:        Non Existant <nonexistant@ripe.net>\n" +
                "nic-hdl:       DR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n";

        // send message to mailupdates
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");

        // read reply from mailupdates
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // make sure that normalised email address is stored in outgoing messages table
        assertThat(countOutgoingForAddress("nonexistant@ripe.net"), is(1));

        // test that there is a notification mail sent to nonexistant@ripe.net
        assertThat(mailSenderStub.anyMoreMessages(), is(true));
    }

    @Test
    public void dont_send_notification_mail_to_undeliverable_address() throws Exception {
        final String role =
                "role:        dummy role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dummyrole@ripe.net\n" +
                "phone:         +31 6 12345678\n" +
                "notify:        nonexistant@ripe.net\n" +
                "nic-hdl:       DR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n";

        // mark address as undeliverable
        insertUndeliverableAddress("nonexistant@ripe.net");

        // send message to mailupdates
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");

        // read reply from mailupdates
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // test that no notification mail is sent to nonexistant@ripe.net
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void dont_send_notification_mail_to_undeliverable_address_is_case_insensitive() throws Exception {
        final String role =
                "role:        dummy role\n" +
                        "address:       Singel 258\n" +
                        "e-mail:        dummyrole@ripe.net\n" +
                        "phone:         +31 6 12345678\n" +
                        "notify:        nonexistant@ripe.net\n" +
                        "nic-hdl:       DR1-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST\n";

        // mark address as undeliverable
        insertUndeliverableAddress("NonEXISTanT@ripe.net");

        // send message to mailupdates
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");

        // read reply from mailupdates
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // test that no notification mail is sent to nonexistant@ripe.net
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void permanent_delivery_failure_not_enough_parts() {
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageNotEnoughParts.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));
    }

    @Test
    public void permanent_delivery_failure_to_one_recipient_multiple_final_recipients() {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "noc@host.org");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822Noc.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));

        // delayed message has been processed but address is not set to undeliverable
        assertThat(isUndeliverableAddress("noc@host.org"), is(false));
        assertThat(isUndeliverableAddress("First.Person@host.org"), is(false));
        assertThat(isUndeliverableAddress("Second.Person@host.org"), is(false));

    }

    @Test
    public void permanent_delivery_failure_to_one_recipient_too_long_address_then_delete_message_without_marking_undeliverable_address() {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "noc@host.org");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822JustOneTooLongAddress.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));

        // delayed message has been processed but address is not set to undeliverable
        assertThat(isUndeliverableAddress("noc@host.org"), is(false));
        assertThat(isUndeliverableAddress(
            "G=noreply/S=noreply/O=noreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                "noreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                "noreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                "noreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreplynoreplynoreplnoreply" +
                "noreplynorepl/P=AA/A=ripe.net/C=SP/@noreply.ripe.net"), is(false));
    }


    @Test
    public void delayed_delivery_is_not_permanently_undeliverable() {
        // insert delayed response
        insertOutgoingMessageId("XXXXXXXX-734E-496B-AD3F-84D3425A7F27", "enduser@host.org");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("messageDelayedRfc822Headers.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));

        // delayed message has been processed but address is not set to undeliverable
        assertThat(isUndeliverableAddress("enduser@host.org"), is(false));

        // delayed message deleted from mailupdates table
        assertThat(anyIncomingMessages(), is(false));
    }

    @Test
    public void full_email_address_is_normalised() throws MessagingException, IOException {
        final String role =
                "role:        dummy role\n" +
                        "address:       Singel 258\n" +
                        "e-mail:        dummyrole@ripe.net\n" +
                        "phone:         +31 6 12345678\n" +
                        "notify:        Non Existant <nonexistant@host.org>\n" +
                        "nic-hdl:       DR1-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST\n";

        final String dr2role =
                "role:        dummy role1\n" +
                        "address:       Singel test 258\n" +
                        "e-mail:        dummyrole@ripe.net\n" +
                        "phone:         +31 6 12345678\n" +
                        "notify:        Non Existant <nonEXISTANT@host.org>\n" +
                        "nic-hdl:       DR2-TEST\n" +
                        "mnt-by:        OWNER-MNT\n" +
                        "source:        TEST\n";

        // send message to mailupdates
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");

        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // make sure that normalised email address is stored in outgoing messages table
        assertThat(countOutgoingForAddress("nonexistant@host.org"), is(1));

        //Match the message Id with the message Id of the bounced email
        updateOutgoingMessageIdForEmail("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "nonexistant@host.org");

        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");
        insertIncomingMessage(message);

        // make sure that normalised email address is stored in undelivered messages table
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("nonexistant@host.org")));

        //Clean previous messages from mailsender
        mailSenderStub.reset();

        // send message to mailupdates for undeliverabled address
        final String secondCreation = insertIncomingMessage("NEW", dr2role + "\npassword: test\n");
        final MimeMessage secondAcknowledgement = mailSenderStub.getMessage(secondCreation);
        assertThat(secondAcknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR2-TEST   dummy role"));

        // test that no notification mail is sent to nonEXISTANT@ripe.net
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void invalid_email_do_not_causes_address_to_be_marked_as_undeliverable() {
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureWithoutMessageId.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));

        assertThat(countUndeliverableAddresses(), is(0));

        // Make sure that failure response message was deleted
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void bouncing_headers_causes_address_to_be_marked_as_undeliverable() {
        insertOutgoingMessageId("XXXXXXXX-2BCC-4B29-9D86-3B8C68DD835D@ripe.net", "nonexistant@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureRfc822Headers.mail");
        insertIncomingMessage(message);

        // Wait for address to be marked as undeliverable
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("nonexistant@ripe.net")));

        // Make sure that failure response message was deleted
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void real_failure_mail_causes_address_to_be_marked_as_undeliverable() {
        insertOutgoingMessageId("796892877.6.1709643245290@gaolao.ripe.net", "testing4@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822Real.mail");
        insertIncomingMessage(message);

        // Wait for address to be marked as undeliverable
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("testing4@ripe.net")));

        // Make sure that failure response message was deleted
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void second_failure_mail_is_deleted() {
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "nonexistant@host.org");
        final MimeMessage firstNotification = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");
        insertIncomingMessage(firstNotification);
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA722A@ripe.net", "nonexistant@host.org");
        final MimeMessage secondNotification = MimeMessageProvider.getUpdateMessage("permanentFailureMessageAnotherRfc822.mail");
        insertIncomingMessage(secondNotification);

        // Wait for address to be marked as undeliverable
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("nonexistant@host.org")));
        // Make sure that failure response messages were deleted
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));
    }

    // TODO: test that acknowledgement email (i.e. the reply to an incoming message) *is* sent to unsubscribed address
}
