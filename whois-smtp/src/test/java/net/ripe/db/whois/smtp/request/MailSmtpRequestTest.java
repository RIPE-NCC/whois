package net.ripe.db.whois.smtp.request;

import io.netty.handler.codec.smtp.SmtpCommand;
import net.ripe.db.whois.smtp.SmtpException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MailSmtpRequestTest {

    @Test
    public void mail() {
        final SmtpException e  = assertThrows(SmtpException.class, () -> {
            new MailSmtpRequest("MAIL");
        });
        assertThat(e.getResponse().code(), is(500));
        assertThat(e.getResponse().details(), contains("unrecognised command"));
    }

    @Test
    public void mail_from() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(nullValue()));
    }

    @Test
    public void mail_empty_from() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("", null));
        assertThat(mailSmtpRequest.getFrom(), is(""));
        assertThat(mailSmtpRequest.getSize(), is(nullValue()));
    }

    @Test
    public void mail_from_with_space() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM: <user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(nullValue()));
    }

    @Test
    public void mail_from_with_name() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM: User Example <user@example.com>");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(nullValue()));
    }

    @Test
    public void mail_from_with_size() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com> SIZE=1023");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", "1023"));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(1023));
    }

    @Test
    public void mail_empty_from_with_size() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<> SIZE=1023");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("", "1023"));
        assertThat(mailSmtpRequest.getFrom(), is(""));
        assertThat(mailSmtpRequest.getSize(), is(1023));
    }

    @Test
    public void mail_from_with_size_extra_spaces() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com>    size=1023   ");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", "1023"));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(1023));
    }

    @Test
    public void mail_from_with_invalid_size() {
        final MailSmtpRequest mailSmtpRequest = new MailSmtpRequest("MAIL FROM:<user@example.com> SIZE=nnn");

        assertThat(mailSmtpRequest.command(), is(SmtpCommand.MAIL));
        assertThat(mailSmtpRequest.parameters(), contains("user@example.com", null));
        assertThat(mailSmtpRequest.getFrom(), is("user@example.com"));
        assertThat(mailSmtpRequest.getSize(), is(nullValue()));
    }
}
