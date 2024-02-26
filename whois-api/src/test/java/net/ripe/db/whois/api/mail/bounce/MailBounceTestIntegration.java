package net.ripe.db.whois.api.mail.bounce;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.Uninterruptibles;
import jakarta.mail.Address;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag("IntegrationTest")
public class MailBounceTestIntegration extends AbstractUpdateDaoIntegrationTest {


    @Autowired
    private MailSenderStub mailSender;


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
                "mnt-nfy:       mnt-nfy@ripe.net\n" +
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
    public void bouncing_mail_causes_address_to_be_marked_as_undeliverable() {
        // send mail update to Whois

        // generate bounce reply to Whois for one of the mail addresses

        // check that address gets marked as undeliverable
    }


    // TODO: mark address as undeliverable. Send mailupdate doesn't generate outgoing mail to that address

    @Test
    public void dont_send_mail_to_undeliverable_address() {
        // mark address as undeliverable
        insertUndeliverableAddress("nonexistant@ripe.net");

        insertMailUpdate(
            createPlainTextMailMessage(
                "role:        dummy role\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       DR1-TEST\n" +
                "mnt-by:        DR1-MNT\n" +
                "source:        TEST"));

        final Joiner commaJoiner = Joiner.on(",");

        for (int i=0; i<10; i++) {
            if (mailSender.anyMoreMessages()) {
                final List<Address> allRecipients = mailSender.getAllRecipients();
                System.out.println("*** FOUND " + commaJoiner.join(allRecipients) + " ***");
            }
            Uninterruptibles.sleepUninterruptibly(1L, TimeUnit.SECONDS);
        }

        //

        // make sure no mail is sent to undeliverabe address

        DatabaseHelper.dumpSchema(whoisTemplate.getDataSource());
    }

    // helper methods

    private String createPlainTextMailMessage(final String rpslString) {
        return String.format(
                "From root@test.de Mon May 28 00:04:45 2012\n" +
                "Return-path: <root@test.de>\n" +
                "Envelope-to: dbase@whois-update.db.ripe.net\n" +
                "Delivery-date: Mon, 28 May 2012 00:04:45 +0200\n" +
                "Date: Mon, 28 May 2012 00:04:44 +0200\n" +
                "From: \"foo@test.de\" <bitbucket@ripe.net>\n" +
                "To: auto-dbm@XXXripe.net\n" +
                "Message-ID: <20120527220444.GA6565@XXXsource.test.de>\n" +
                "MIME-Version: 1.0\n" +
                "Content-Type: text/plain; charset=us-ascii\n" +
                "Content-Disposition: inline\n" +
                "User-Agent: Mutt/1.5.17+20080114 (2008-01-14)\n" +
                "\n" +
                "%s" +
                "\n", rpslString);
    }


    private void insertMailUpdate(final String mailMessage) {
        databaseHelper.getMailupdatesTemplate().update("INSERT INTO mailupdates (message) VALUES (?)", mailMessage);
    }

    private void insertMessageId(final String messageId, final String emailAddress) {
        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO in_progress_message (message_id, email, last_update) VALUES (?, ?, now())", messageId, emailAddress);
    }

    private void insertUndeliverableAddress(final String emailAddress) {
        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO undeliverable_email (email, last_update) VALUES (?, now())", emailAddress);
    }




}
