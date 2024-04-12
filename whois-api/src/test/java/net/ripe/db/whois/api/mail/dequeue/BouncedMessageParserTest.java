package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BouncedMessageParserTest {

    private BouncedMessageParser subject;

    @BeforeEach
    public void setup() {
        this.subject = new BouncedMessageParser("bounce-handler@ripe.net");
    }

    @Test
    public void parse_permanent_delivery_failure_message_rfc822() throws Exception {
        final EmailMessageInfo bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail"));

        assertThat(bouncedMessage.messageId(), is("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net"));
        assertThat(bouncedMessage.emailAddresses(), containsInAnyOrder("nonexistant@host.org"));
    }


    @Test
    public void parse_permanent_delivery_failure_multiple_recipients_rfc8221() throws Exception {
        final EmailMessageInfo bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureMessagePartialReportWithMultipleRecipientsRfc822.mail"));

        assertThat(bouncedMessage.messageId(), is("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net"));
        assertThat(bouncedMessage.emailAddresses(), containsInAnyOrder("nonexistant@host.org"));
    }

    @Test
    public void parse_permanent_delivery_failure_multiple_recipients_rfc822() throws Exception {
        final EmailMessageInfo bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailurePerRecipientMessageRfc822.mail"));

        assertThat(bouncedMessage.messageId(), is("XXXXXXXX-8553-47AB-A79B-A9896A2DFBAC@ripe.net"));
        assertThat(bouncedMessage.emailAddresses(), containsInAnyOrder("nonexistant@host.org", "nonexistant1@host.org"));
    }

    @Test
    public void parse_permanent_delivery_failure_rfc822_headers() throws Exception {
        final EmailMessageInfo bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureRfc822Headers.mail"));

        assertThat(bouncedMessage.messageId(), is("XXXXXXXX-2BCC-4B29-9D86-3B8C68DD835D@ripe.net"));
        assertThat(bouncedMessage.emailAddresses(), containsInAnyOrder("nonexistant@ripe.net"));
    }


    @Test
    public void parse_permanent_delivery_failure_message_rfc822_headers_real() throws Exception {
        final EmailMessageInfo bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822Real.mail"));

        assertThat(bouncedMessage.messageId(), is("796892877.6.1709643245290@gaolao.ripe.net"));
        assertThat(bouncedMessage.emailAddresses(), containsInAnyOrder("testing4@ripe.net"));
    }

    @Test
    public void parse_permanent_delivery_failure_without_message_id() {
        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> {
            subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureWithoutMessageId.mail"));
        });

        assertThat(e.getMessage(), is("No Message-Id header"));
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
