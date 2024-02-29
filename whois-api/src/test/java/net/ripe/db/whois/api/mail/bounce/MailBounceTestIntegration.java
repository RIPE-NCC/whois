package net.ripe.db.whois.api.mail.bounce;

import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class MailBounceTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private MailSenderStub mailSenderStub;
    @Autowired
    private MailUpdatesTestSupport mailUpdatesTestSupport;

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
                "notify:        notify-dummy-role@ripe.net\n" +
                "nic-hdl:       DR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "source:        TEST\n";

        // send message and read acknowledgement reply
        final String from = insertIncomingMessage("NEW", role + "\npassword: test\n");
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // Outgoing notification email to notify-dummy-role@ripe.net
        final MimeMessage notify = mailSenderStub.getMessage("notify-dummy-role@ripe.net");
        final String notifyMessageId = notify.getHeader("Message-ID", "\n");
        assertThat(notifyMessageId, Matchers.is(not(nullValue())));

        // Generate failure response for notification
        insertOutgoingMessageId("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net", "notify-dummy-role@ripe.net");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail");
        insertIncomingMessage(message);

        // Wait for address to be marked as undeliverable
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("notify-dummy-role@ripe.net")));
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

        DatabaseHelper.dumpSchema(internalsTemplate.getDataSource());   // TODO

        // make sure that normalised email address is stored in outgoing messages table
        assertThat(getOutgoingMessageId("nonexistant@ripe.net"), is("X"));

        // TODO: test that notification mail message is sent to nonexistant@ripe.net
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
    public void delayed_delivery_is_not_permanently_undeliverable() {
        // insert delayed response
        insertOutgoingMessageId("XXXXXXXX-734E-496B-AD3F-84D3425A7F27", "enduser@host.org");
        final MimeMessage message = MimeMessageProvider.getUpdateMessage("messageDelayedRfc822Headers.mail");
        insertIncomingMessage(message);

        // wait for incoming message to be processed
        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (! anyIncomingMessages()));

        // delayed message has been processed but address is not set to undeliverable
        assertThat(isUndeliverableAddress("enduser@host.org"), is(false));
    }

    @Test
    public void full_email_address_is_normalised() {
        // TODO: expect address to be normalised in undeliverable table if undeliverable, e.g. "First Last <USER@host.org>" is stored as "user@host.org"

        // TODO: Also test that email is *not* sent to that undeliverable address, even if the format is different (e.g. user@HOST.org)
    }

    // TODO: test permanent failure without original message-id
    // permanentFailureWithoutMessageId.mail

    // TODO: test permanent failure with message/rfc822 part
    // permanentFailureMessageRfc822.mail

    // TODO: test permanent failure with text/rfc822-headers part
    // permanentFailureRfc822Headers.mail


    // helper methods

    private void insertUndeliverableAddress(final String emailAddress) {
        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO undeliverable_email (email) VALUES (?)", emailAddress);
    }

    private boolean isUndeliverableAddress(final String emailAddress) {
        return databaseHelper.getInternalsTemplate().query("SELECT count(email) FROM undeliverable_email WHERE email= ?",
                (rs, rowNum) -> rs.getInt(1), emailAddress).size() == 1;
    }

    // TODO: extend MailSenderStub to also update outgoing_message table ?

    private void insertOutgoingMessageId(final String messageId, final String emailAddress) {
        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO outgoing_message (message_id, email) VALUES (?, ?)", messageId, emailAddress);
    }

    private String getOutgoingMessageId(final String emailAddress) {
        return databaseHelper.getInternalsTemplate().queryForObject(
                "SELECT message_id FROM outgoing_message WHERE email = ?", String.class, emailAddress);
    }

    private void insertIncomingMessage(final MimeMessage message) {
        mailUpdatesTestSupport.insert(message);
    }

    private String insertIncomingMessage(final String subject, final String body) {
        return mailUpdatesTestSupport.insert(subject, body);
    }

    private boolean anyIncomingMessages() {
        return Boolean.TRUE.equals(databaseHelper.getMailupdatesTemplate().query("SELECT message FROM mailupdates", (rs, rowNum) -> rs.next()));
    }

}
