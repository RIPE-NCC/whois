package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class UnsubscribeMessageParserTest {

    private UnsubscribeMessageParser subject;

    @BeforeEach
    public void setup() {
        this.subject = new UnsubscribeMessageParser("bounce-handler@ripe.net");
    }

    @Test
    public void unsubscribe_from_apple_mail() throws Exception {
        final EmailMessageInfo unsubscribedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("unsubscribeAppleMail.mail"));

        assertThat(unsubscribedMessage.messageId(), is("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net"));
        assertThat(unsubscribedMessage.emailAddresses(), containsInAnyOrder("enduser@ripe.net"));
    }

    @Test
    public void unsubscribe_from_google_mail() throws Exception {
        final EmailMessageInfo unsubscribedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("unsubscribeGmail.mail"));

        assertThat(unsubscribedMessage.messageId(), is("8b8ed6c0-f9cc-4a5f-afbb-fde079b94f44@ripe.net"));
        assertThat(unsubscribedMessage.emailAddresses(), containsInAnyOrder("enduser@gmail.com"));
    }

    @Test
    public void parse_permanent_delivery_failure_message_rfc822() throws Exception {
        assertThat(subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail")), is(nullValue()));
    }

    @Test
    public void parse_delayed_delivery() throws Exception {
        assertThat(subject.parse(MimeMessageProvider.getUpdateMessage("messageDelayedRfc822Headers.mail")), is(nullValue()));
    }

    @Test
    public void parse_inline_pgp_signed_mailupdate() throws Exception {
        assertThat(subject.parse(MimeMessageProvider.getUpdateMessage("inlinePgpSigned.mail")), is(nullValue()));
    }

    @Test
    public void parse_raw_object() throws Exception {
        assertThat(subject.parse(MimeMessageProvider.getUpdateMessage("giantRawUnsignedObject")), is(nullValue()));
    }

    @Test
    public void parse_simple_plain_text() throws Exception {
        assertThat(subject.parse(MimeMessageProvider.getUpdateMessage("simplePlainTextUnsigned.mail")), is(nullValue()));
    }
}
