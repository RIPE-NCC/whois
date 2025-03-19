package net.ripe.db.whois.smtp.request;

import io.netty.handler.codec.smtp.SmtpCommand;
import net.ripe.db.whois.smtp.SmtpException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MailSmtpRequestTest {

    @Test
    public void mail() {
        final SmtpException e  = assertThrows(SmtpException.class, () -> {
            new MailSmtpRequest("MAIL");
        });
        assertThat(e.getResponse().code(), is(500));
        assertThat(e.getResponse().details(), contains("unrecognized command"));
    }

    @Test
    public void mail_from() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
    }

    @Test
    public void mail_from_space() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM: <user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
    }

    @Test
    public void mail_from_name() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM: User Example <user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
    }

    @Test
    public void mail_from_size() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com> SIZE=1023");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", "1023"));
    }

}
