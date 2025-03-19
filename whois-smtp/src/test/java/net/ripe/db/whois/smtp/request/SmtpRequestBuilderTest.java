package net.ripe.db.whois.smtp.request;

import io.netty.handler.codec.smtp.SmtpCommand;
import io.netty.handler.codec.smtp.SmtpRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

class SmtpRequestBuilderTest {


    @Test
    public void data() {
        final SmtpRequest subject = new SmtpRequestBuilder("DATA").build();

        assertThat(subject.command(), is(SmtpCommand.DATA));
        assertThat(subject.parameters(), contains(""));
    }

    @Test
    public void data_and_spaces() {
        final SmtpRequest subject = new SmtpRequestBuilder("DATA \t  ").build();

        assertThat(subject.command(), is(SmtpCommand.DATA));
        assertThat(subject.parameters(), contains(""));
    }

    @Test
    public void mail_from() {
        final SmtpRequest subject = new SmtpRequestBuilder("MAIL FROM:<usr@example.com> SIZE=1023").build();

        assertThat(subject.command(), is(SmtpCommand.MAIL));
        assertThat(subject.parameters(), contains("usr@example.com", "1023"));
    }

    @Test
    public void unknown() {
        final SmtpRequest subject = new SmtpRequestBuilder("unknown").build();

        assertThat(subject.command(), is(SmtpCommand.valueOf("UNKNOWN")));
        assertThat(subject.parameters(), contains(""));
    }
}
