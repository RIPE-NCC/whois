package net.ripe.db.whois.common;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class MessagesTest {
    @Test
    public void getMessages() {
        final Message info1 = new Message(Messages.Type.INFO, "info1");
        Messages subject = new Messages(info1);

        final Message info2 = new Message(Messages.Type.INFO, "info2");
        final Message warn = new Message(Messages.Type.WARNING, "warn");

        subject.add(info2);
        subject.add(warn);

        assertThat(subject.getMessages(Messages.Type.INFO), contains(info1, info2));
        assertThat(subject.getMessages(Messages.Type.WARNING), contains(warn));
        assertThat(subject.getMessages(Messages.Type.ERROR), hasSize(0));
    }

    @Test
    public void getMessages_duplicates() {
        Messages subject = new Messages();

        final Message info = new Message(Messages.Type.INFO, "info");
        subject.add(info);
        subject.add(info);

        assertThat(subject.getMessages(Messages.Type.INFO), contains(info));
    }

    @Test
    public void addAll() {
        final Messages subject = new Messages();
        final Message error1 = new Message(Messages.Type.ERROR, "error1");
        subject.add(error1);

        final Messages additionalMessages = new Messages();
        final Message error2 = new Message(Messages.Type.ERROR, "error2");
        final Message info = new Message(Messages.Type.INFO, "info");
        additionalMessages.add(error2);
        additionalMessages.add(info);

        subject.addAll(additionalMessages);

        assertThat(subject.getAllMessages(), hasSize(3));
        assertThat(subject.getAllMessages(), contains(error1, error2, info));
    }

    @Test
    public void getErrors() {
        final Messages subject = new Messages();
        final Message error1 = new Message(Messages.Type.ERROR, "error1");
        final Message error2 = new Message(Messages.Type.ERROR, "error2");
        final Message info = new Message(Messages.Type.INFO, "info");
        subject.add(error1);
        subject.add(error2);
        subject.add(info);

        assertThat(subject.getErrors(), hasSize(2));
        assertThat(subject.getErrors(), contains(error1, error2));
    }

    @Test
    public void getWarnings() {
        final Messages subject = new Messages();
        final Message error = new Message(Messages.Type.ERROR, "error");
        final Message warning = new Message(Messages.Type.WARNING, "warning");
        final Message info = new Message(Messages.Type.INFO, "info");
        subject.add(error);
        subject.add(warning);
        subject.add(info);

        assertThat(subject.getWarnings(), hasSize(1));
        assertThat(subject.getWarnings(), contains(warning));
    }

    @Test
    public void type() {
        for (final Messages.Type type : Messages.Type.values()) {
            assertNotNull(type.toString());
        }
    }
}
