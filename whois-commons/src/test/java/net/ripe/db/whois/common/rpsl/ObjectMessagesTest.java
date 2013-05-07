package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ObjectMessagesTest {
    private ObjectMessages subject;
    private Message error;
    private Message warning;
    private RpslObject object;

    @Before
    public void setUp() throws Exception {
        subject = new ObjectMessages();
        error = new Message(Messages.Type.ERROR, "error");
        warning = new Message(Messages.Type.WARNING, "warning");
        object = RpslObject.parse("mntner: DEV-ROOT-MNT\n");
    }

    @Test
    public void empty() {
        assertThat(subject.getMessages().getAllMessages(), hasSize(0));
    }

    @Test
    public void global_messages() {
        subject.addMessage(warning);
        subject.addMessage(error);

        assertThat(subject.getMessages().getAllMessages(), hasSize(2));
        assertThat(subject.getMessages().getErrors(), contains(error));
        assertThat(subject.getMessages().getWarnings(), contains(warning));

        assertThat(subject.contains(error), is(true));
        assertThat(subject.hasErrors(), is(true));
        assertThat(subject.contains(warning), is(true));
    }

    @Test
    public void attribute_warnings() {
        final RpslAttribute attribute = object.getAttributes().get(0);

        subject.addMessage(attribute, warning);
        assertThat(subject.getMessages().getAllMessages(), hasSize(0));
        assertThat(subject.getMessages(attribute).getAllMessages(), contains(warning));
        assertThat(subject.hasErrors(), is(false));
    }

    @Test
    public void attribute_errors() {
        final RpslAttribute attribute = object.getAttributes().get(0);

        subject.addMessage(attribute, error);
        assertThat(subject.getMessages().getAllMessages(), hasSize(0));
        assertThat(subject.getMessages(attribute).getAllMessages(), contains(error));
        assertThat(subject.hasErrors(), is(true));
    }

    @Test
    public void addAll() {
        final RpslAttribute attribute = object.getAttributes().get(0);

        final ObjectMessages other = new ObjectMessages();
        other.addMessage(error);
        other.addMessage(attribute, warning);

        subject.addAll(other);

        assertThat(subject.contains(error), is(true));
        assertThat(subject.getMessages(attribute).getWarnings(), contains(warning));
    }
}
