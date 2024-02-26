package net.ripe.db.whois.api.mail.bounce;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("IntegrationTest")
public class MailBounceTestIntegration extends AbstractIntegrationTest {

    private static final Session SESSION = Session.getInstance(new Properties());

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


    // TODO: generate bounce reply to mailupdate. That address should be marked as undeliverable

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

        // send message and read reply
        final String from = mailUpdatesTestSupport.insert("NEW", role + "\npassword: test\n");
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // Outgoing mail to notify-dummy-role@ripe.net
        final MimeMessage notify = mailSenderStub.getMessage("notify-dummy-role@ripe.net");
        final String notifyMessageId = notify.getHeader("Message-ID", "\n");
        assertThat(notifyMessageId, Matchers.is(not(nullValue())));

        // TODO: generate bounce reply to Whois for notify-dummy-role@ripe.net

        final MimeMessage bounceMessage = new MimeMessage(SESSION, new ByteArrayInputStream(String.format(
                "Return-path: <>\n" +
                "Envelope-to: auto-dbm@ripe.net\n" +
                "Delivery-date: Tue, 16 Feb 2016 10:18:49 +0100\n" +
                "Received: by bogus.localdomain (sSMTP sendmail emulation); Fri, 24 Aug 2012 11:52:53 -0400\n" +
                "Message-Id: %s\n" +      // TODO: does the failure reply have the same message-id as the outgoing message?
                "Date: 16 Feb 2016 10:17:29 +0100\n" +
                "To: auto-dbm@ripe.net\n" +
                "From: \"Mail Delivery System\" <MAILER-DAEMON@bogus.localdomain>\n" +
                "Subject: Delivery Status Notification (Failure)\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: multipart/report; report-type=delivery-status; boundary=\"nOsDR.5Az+QGeGV.2E/OdDVjIfq.2DVyKAZ\"\n" +
                "Delivered-To: auto-dbm@ripe.net\n" +
                "\n" +
                "--nOsDR.5Az+QGeGV.2E/OdDVjIfq.2DVyKAZ\n" +
                "content-type: text/plain;\n" +
                "    charset=\"us-ascii\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "The following message to <notify-dummy-role@ripe.net> was undeliverable.\n" +
                "The reason for the problem:\n" +
                "5.3.0 - Other mail system problem 554-'Too many hops'\n" +
                "\n" +
                "--nOsDR.5Az+QGeGV.2E/OdDVjIfq.2DVyKAZ\n" +
                "content-type: message/delivery-status\n" +
                "\n" +
                "Reporting-MTA: dns; s4de8nsazdfe010.bogus.localdomain\n" +
                "\n" +
                "Final-Recipient: rfc822;notify-dummy-role@ripe.net\n" +
                "Action: failed\n" +
                "Status: 5.0.0 (permanent failure)\n" +
                "Remote-MTA: dns; [1.2.3.44]\n" +
                "Diagnostic-Code: smtp; 5.3.0 - Other mail system problem 554-'Too many hops' (delivery attempts: 0)\n" +
                "\n" +
                "--nOsDR.5Az+QGeGV.2E/OdDVjIfq.2DVyKAZ\n" +
                "content-type: message/rfc822\n" +
                "\n" +
                "--nOsDR.5Az+QGeGV.2E/OdDVjIfq.2DVyKAZ--\n" +
                "\n" +
                "\n", notifyMessageId).getBytes()));


        mailUpdatesTestSupport.insert(bounceMessage);

        // TODO: poll undeliverable table as update is asynchronous

        // check that address gets marked as undeliverable
        assertThat(isUndeliverableAddress("notify-dummy-role@ripe.net"), is(false));
    }

    @Test
    public void dont_send_mail_to_undeliverable_address() throws Exception {
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
        setUndeliverableAddress("nonexistant@ripe.net");

        // send message and read reply
        final String from = mailUpdatesTestSupport.insert("NEW", role + "\npassword: test\n");
        final MimeMessage acknowledgement = mailSenderStub.getMessage(from);
        assertThat(acknowledgement.getContent().toString(), containsString("Create SUCCEEDED: [role] DR1-TEST   dummy role"));

        // test that no mail is sent to nonexistant@ripe.net
        assertThat(mailSenderStub.anyMoreMessages(), is(false));
    }

    @Test
    public void delayed_delivery_is_not_permanent_undeliverable() throws Exception {

        // TODO: make sure that a delayed delivery message doesn't cause the address to be marked as undeliverable

    }

    // helper methods

    private void setUndeliverableAddress(final String emailAddress) {
        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO undeliverable_email (email, last_update) VALUES (?, now())", emailAddress);
    }

    private boolean isUndeliverableAddress(final String emailAddress) {
        return Boolean.TRUE.equals(databaseHelper.getInternalsTemplate().queryForObject("SELECT email FROM undeliverable_email WHERE email = ?", (rs, rowNum) -> rs.next(), emailAddress));
    }

}
