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
    @InjectMocks private MessageFilter subject;

    @Mock MailMessage message;

    @Test
    public void shouldProcess() throws Exception {
        when(message.getReplyTo()).thenReturn("me@ripte.net");
        assertThat(subject.shouldProcess(message), is(true));
    }

    @Test
    public void shouldProcess_localhost() throws Exception {
        when(message.getReplyTo()).thenReturn("something@localhost");
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void shouldProcess_127_0_0_1() throws Exception {
        when(message.getReplyTo()).thenReturn("something@127.0.0.1");
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void noReplytoFails() throws Exception {
        when(message.getReplyTo()).thenReturn(null);
        assertThat(subject.shouldProcess(message), is(false));
    }

    @Test
    public void invalidReplytoFails() throws Exception {
        when(message.getReplyTo()).thenReturn("email_with_no_domain_or_at_sign");
        assertThat(subject.shouldProcess(message), is(false));
    }
}
