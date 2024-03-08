package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.MimeMessageProvider;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Tag("IntegrationTest")
public class UnsubscribeMessageServiceTestIntegration extends AbstractBounceMailMessageIntegrationTest {

    @Test
    public void unsubscribe_message_apple_mail() {
        insertOutgoingMessageId("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net", "enduser@ripe.net");

        insertIncomingMessage(MimeMessageProvider.getUpdateMessage("unsubscribeAppleMail.mail"));

        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("enduser@ripe.net")));
    }

    @Test
    public void unsubscribe_message_google_mail() {
        insertOutgoingMessageId("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net", "enduser@gmail.com");

        insertIncomingMessage(MimeMessageProvider.getUpdateMessage("unsubscribeGmail.mail"));

        Awaitility.waitAtMost(10L, TimeUnit.SECONDS).until(() -> (isUndeliverableAddress("enduser@gmail.com")));
    }


}
