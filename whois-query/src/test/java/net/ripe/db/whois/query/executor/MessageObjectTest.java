package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class MessageObjectTest {

    private MessageObject subject = new MessageObject("message");

    @Test
    public void equals_and_hashCode() throws Exception {
        MessageObject same = new MessageObject(subject.toString());
        MessageObject other = new MessageObject(subject.toString() + "Other");

        assertThat("self", subject, is(subject));
        assertThat("same", subject, is(same));
        assertThat(subject, not(equalTo(null)));
        assertThat( subject, not(equalTo(1)));  // type
        assertThat(subject, not(equalTo(other)));   // other

        final Message message = QueryMessages.timeout();
        assertThat("fromMessage", new MessageObject(message), is(new MessageObject(message)));

        assertThat("hashCode self", subject.hashCode(), is(subject.hashCode()));
        assertThat("hashCode same", subject.hashCode(), is(same.hashCode()));
    }
}
