package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MailMessageBuilderTest {
    private MailMessageBuilder subject;

    @Before
    public void setUp() throws Exception {
        subject = new MailMessageBuilder();
    }

    @Test
    public void messageInfo() {
        final MailMessage message = subject.id("id")
                .from("me@ripe.net")
                .subject("help")
                .date("today")
                .replyTo("you@ripe.net")
                .build();

        assertThat(message.getId(), is("id"));
        assertThat(message.getFrom(), is("me@ripe.net"));
        assertThat(message.getSubject(), is("help"));
        assertThat(message.getDate(), is("today"));
        assertThat(message.getReplyTo(), is("you@ripe.net"));
    }

    @Test
    public void help() {
        subject.keyword(Keyword.HELP);
        subject.addContentWithCredentials(new ContentWithCredentials("mntner: DEV-MNT\n"));
        final MailMessage message = subject.build();

        assertThat(message.getKeyword(), is(Keyword.HELP));
        assertThat(message.getContentWithCredentials().get(0).getContent(), containsString("mntner: DEV-MNT\n"));
    }

    @Test
    public void single_content_with_password() {
        subject.addContentWithCredentials(
                new ContentWithCredentials(
                        "mntner: DEV-MNT\n" +
                                "descr: DEV maintainer\n" +
                                "password: pass\n"));

        final MailMessage message = subject.build();
        assertThat(message.getContentWithCredentials(), hasSize(1));
        assertThat(message.getContentWithCredentials().get(0).getContent(), containsString("mntner"));
    }

    @Test
    public void multiple_content_separate_parts() {
        subject.addContentWithCredentials(new ContentWithCredentials("mntner: DEV-MNT1\npassword: password1"));
        subject.addContentWithCredentials(new ContentWithCredentials("mntner: DEV-MNT2\npassword: password2"));
        subject.addContentWithCredentials(new ContentWithCredentials("mntner: DEV-MNT3\npassword: password3"));

        final MailMessage message = subject.build();
        assertThat(message.getContentWithCredentials(), hasSize(3));
    }
}
