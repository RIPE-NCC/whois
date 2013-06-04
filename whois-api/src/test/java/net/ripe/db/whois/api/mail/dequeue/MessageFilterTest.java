package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.update.log.LoggerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageFilterTest {
    @Mock LoggerContext loggerContext;
    @InjectMocks MessageFilter subject;

    @Mock MailMessage message;

    @Test
    public void shouldProcess() throws Exception {
        when(message.getReplyTo()).thenReturn("me@ripte.net");
        when(message.getReplyToEmail()).thenReturn("me@ripte.net");
        assertThat(subject.shouldProcess(message), is(true));
    }

    @Test
    public void shouldProcessFull() throws Exception {
        when(message.getReplyTo()).thenReturn("Maximus Maxus <me@ripte.net>");
        when(message.getReplyToEmail()).thenReturn("me@ripte.net");
        assertThat(subject.shouldProcess(message), is(true));
    }

    @Test
    public void shouldProcess_localhost() throws Exception {
        when(message.getReplyTo()).thenReturn("something@localhost");
        when(message.getReplyToEmail()).thenReturn("something@localhost");
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void shouldProcess_127_0_0_1() throws Exception {
        when(message.getReplyTo()).thenReturn("something@127.0.0.1");
        when(message.getReplyToEmail()).thenReturn("something@127.0.0.1");
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void noReplytoFails() throws Exception {
        when(message.getReplyTo()).thenReturn(null);
        when(message.getReplyToEmail()).thenReturn(null);
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void noReplytoFailsFull() throws Exception {
        when(message.getReplyTo()).thenReturn("Maximus Maxus");
        when(message.getReplyToEmail()).thenReturn(null);
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void invalidReplytoFails() throws Exception {
        when(message.getReplyTo()).thenReturn("email_with_no_domain_or_at_sign");
        when(message.getReplyToEmail()).thenReturn("email_with_no_domain_or_at_sign");
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void invalidReplytoFailsFull() throws Exception {
        when(message.getReplyTo()).thenReturn("Minima Mina <email_with_no_domain_or_at_sign>");
        when(message.getReplyToEmail()).thenReturn("email_with_no_domain_or_at_sign");
        assertThat(subject.shouldProcess(message), is(false));
    }
}
