package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.dao.AbstractUpdateDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class MailBounceTestIntegration extends AbstractUpdateDaoIntegrationTest {


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

        // send mail update to Whois

        // make sure no mail is sent to undeliverabe address
    }





}
