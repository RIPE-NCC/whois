package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class MessageObjectTest {

    private MessageObject subject = new MessageObject("message");

    @Test
    public void equals_and_hashCode() throws Exception {
        MessageObject same = new MessageObject(subject.toString());
        MessageObject other = new MessageObject(subject.toString() + "Other");

        assertThat("self", subject, is(subject));
        assertThat("same", subject, is(same));
        assertFalse("null", subject.equals(null));
        assertFalse("type", subject.equals(1));
        assertFalse("other", subject.equals(other));

        final Message message = QueryMessages.timeout();
        assertThat("fromMessage", new MessageObject(message), is(new MessageObject(message)));

        assertThat("hashCode self", subject.hashCode(), is(subject.hashCode()));
        assertThat("hashCode same", subject.hashCode(), is(same.hashCode()));
    }
}
