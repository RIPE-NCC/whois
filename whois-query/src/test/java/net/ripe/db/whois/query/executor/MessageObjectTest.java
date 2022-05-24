package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.QueryMessages;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public class MessageObjectTest {

    private MessageObject subject = new MessageObject("message");

    @Test
    public void equals_and_hashCode() throws Exception {
        MessageObject same = new MessageObject(subject.toString());
        MessageObject other = new MessageObject(subject.toString() + "Other");

        assertThat("self", subject, is(subject));
        assertThat("same", subject, is(same));
        assertFalse(subject.equals(null), "null");
        assertFalse( subject.equals(1), "type");
        assertFalse(subject.equals(other), "other");

        final Message message = QueryMessages.timeout();
        assertThat("fromMessage", new MessageObject(message), is(new MessageObject(message)));

        assertThat("hashCode self", subject.hashCode(), is(subject.hashCode()));
        assertThat("hashCode same", subject.hashCode(), is(same.hashCode()));
    }
}
