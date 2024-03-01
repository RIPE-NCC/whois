package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.MimeMessageProvider;
import net.ripe.db.whois.api.mail.BouncedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BouncedMessageParserTest {

    @InjectMocks BouncedMessageParser subject;

    @Test
    public void parse_permanent_delivery_failure_message_rfc822() throws Exception {
        final BouncedMessage bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureMessageRfc822.mail"));

        assertThat(bouncedMessage.getMessageId(), is("XXXXXXXX-5AE3-4C58-8E3F-860327BA955D@ripe.net"));
        assertThat(bouncedMessage.getEmailAddress(), is("nonexistant@host.org"));
    }

    @Test
    public void parse_permanent_delivery_failure_rfc822_headers() throws Exception {
        final BouncedMessage bouncedMessage = subject.parse(MimeMessageProvider.getUpdateMessage("permanentFailureRfc822Headers.mail"));

        assertThat(bouncedMessage.getMessageId(), is("XXXXXXXX-2BCC-4B29-9D86-3B8C68DD835D@ripe.net"));
        assertThat(bouncedMessage.getEmailAddress(), is("nonexistant@ripe.net"));
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


}
