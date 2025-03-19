package net.ripe.db.whois.smtp.request;

import io.netty.handler.codec.smtp.SmtpCommand;
import net.ripe.db.whois.smtp.SmtpException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RcptSmtpRequestTest {

    @Test
    public void rcpt() {
        final SmtpException e  = assertThrows(SmtpException.class, () -> {
            new RecipientSmtpRequest("RCPT");
        });
        assertThat(e.getResponse().code(), is(500));
        assertThat(e.getResponse().details(), contains("unrecognised command"));
    }

    @Test
    public void rcpt_to() {
        final RecipientSmtpRequest rcptSmtpRequest = new RecipientSmtpRequest("RCPT TO:<user@example.com>");

        assertThat(rcptSmtpRequest.command(), is(SmtpCommand.RCPT));
        assertThat(rcptSmtpRequest.parameters(), contains("user@example.com"));
    }

    @Test
    public void rcpt_to_name() {
        final RecipientSmtpRequest rcptSmtpRequest = new RecipientSmtpRequest("RCPT TO: User Example <user@example.com>");

        assertThat(rcptSmtpRequest.command(), is(SmtpCommand.RCPT));
        assertThat(rcptSmtpRequest.parameters(), contains("user@example.com"));
    }
}
