package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class MessageTest {
    @Test
    public void without_args() {
        final Message subject = new Message(Messages.Type.INFO, "info");
        assertThat(subject.toString(), is("info"));
        assertThat(subject.getType(), is(Messages.Type.INFO));
        assertThat(subject.getFormattedText(), is("info"));
    }

    @Test
    public void without_args_long_message() {
        final Message subject = new Message(Messages.Type.INFO, "" +
                "An as-block object is needed to delegate a range of AS numbers to a given repository.  This object may be used for authorisation of the creation of aut-num objects within the range specified by the \"as-block:\" attribute.\n");

        assertThat(subject.toString(), is("" +
                "An as-block object is needed to delegate a range of AS numbers to a given repository.  This object may be used for authorisation of the creation of aut-num objects within the range specified by the \"as-block:\" attribute.\n"));
    }

    @Test
    public void with_marker() {
        final Message subject = new Message(Messages.Type.ERROR, ""
                + "%ERROR:100: internal software error\n"
                + "%\n"
                + "% Please contact ripe-dbm@ripe.net if the problem persists.\n");

        assertThat(subject.toString(), is(""
                + "%ERROR:100: internal software error\n"
                + "%\n"
                + "% Please contact ripe-dbm@ripe.net if the problem persists.\n"));
    }

    @Test
    public void with_marker_long() {
        final Message subject = new Message(Messages.Type.ERROR, "% https://docs.db.ripe.net/FAQ/#why-did-i-receive-an-error-201-access-denied\n");

        assertThat(subject.toString(), is("% https://docs.db.ripe.net/FAQ/#why-did-i-receive-an-error-201-access-denied\n"));
    }

    @Test
    public void rdns_message() {
        final Message subject = new Message(Messages.Type.ERROR, "" +
                "***RDNS: (related to 0.2.193.in-addr.arpa) CRITICAL: Fatal error in delegation for zone 0.2.193.in-addr.arpa.\n" +
                "\n" +
                "No name servers found at child or at parent. No further testing can be performed.");

        assertThat(subject.toString(), is("" +
                "***RDNS: (related to 0.2.193.in-addr.arpa) CRITICAL: Fatal error in delegation for zone 0.2.193.in-addr.arpa.\n\nNo name servers found at child or at parent. No further testing can be performed."));
    }

    @Test
    public void with_args() {
        final Message subject = new Message(Messages.Type.INFO, "%%%s %s", "1", "2");

        assertThat(subject.toString(), is("%1 2"));
    }

    @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
    @Test
    public void Message_equals() {
        final Message subject = new Message(Messages.Type.INFO, "info");

        assertThat(subject, not(equalTo(null)));
        assertThat(subject, not(equalTo("")));
        assertThat(subject, is(subject));
        assertThat(subject, is(new Message(Messages.Type.INFO, "info")));
        assertThat(subject, is(new Message(Messages.Type.INFO, "info", "")));
        assertThat(subject, not(equalTo(new Message(Messages.Type.INFO, "info2"))));
        assertThat(subject, not(equalTo(new Message(Messages.Type.ERROR, "info"))));
        assertThat(subject, not(equalTo(new Message(Messages.Type.ERROR, "info2"))));
    }
}
