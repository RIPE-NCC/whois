package net.ripe.db.whois.common;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class MessageTest {
    @Test
    public void without_args() {
        final Message subject = new Message(Messages.Type.INFO, "info");
        assertThat(subject.toString(), is("***Info:    info\n"));
    }

    @Test
    public void without_args_long_message() {
        final Message subject = new Message(Messages.Type.INFO, "" +
                "An as-block object is needed to delegate a range of AS numbers " +
                "to a given repository.  This object may be used for authorisation " +
                "of the creation of aut-num objects within the range specified " +
                "by the \"as-block:\" attribute.\n");

        assertThat(subject.toString(), is("" +
                "***Info:    An as-block object is needed to delegate a range of AS numbers to a\n" +
                "            given repository.  This object may be used for authorisation of the\n" +
                "            creation of aut-num objects within the range specified by the\n" +
                "            \"as-block:\" attribute.\n"));
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
        final Message subject = new Message(Messages.Type.ERROR, "% http://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied\n");
        assertThat(subject.toString(), is("% http://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied\n"));
    }

    @Test
    public void long_message() {
        final Message subject = new Message(Messages.Type.INFO, "" +
                "Some\n" +
                "really-really-really-really-really-really-really-really-really-really-really-really-really-really-really long string \n" +
                "should work");
        assertThat(subject.toString(), is("" +
                "***Info:    Some\n" +
                "            really-really-really-really-really-really-really-really-really-really-really-really-really-really-really\n" +
                "            long string\n" +
                "            should work\n"));
    }

    @Test
    public void rdns_message() {
        final Message subject = new Message(Messages.Type.ERROR, "" +
                "***RDNS: (related to 0.2.193.in-addr.arpa) CRITICAL: Fatal error in delegation for zone 0.2.193.in-addr.arpa.\n" +
                "\n" +
                "No name servers found at child or at parent. No further testing can be performed.");

        assertThat(subject.toString(), is("" +
                "***Error:   ***RDNS: (related to 0.2.193.in-addr.arpa) CRITICAL: Fatal error in\n" +
                "            delegation for zone 0.2.193.in-addr.arpa.\n" +
                "            \n" +
                "            No name servers found at child or at parent. No further testing can\n" +
                "            be performed.\n"));
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
        assertFalse(subject.equals(null));
        assertFalse(subject.equals(""));
        assertTrue(subject.equals(subject));
        assertTrue(subject.equals(new Message(Messages.Type.INFO, "info")));
        assertTrue(subject.equals(new Message(Messages.Type.INFO, "info", "")));
        assertFalse(subject.equals(new Message(Messages.Type.INFO, "info2")));
        assertFalse(subject.equals(new Message(Messages.Type.ERROR, "info")));
        assertFalse(subject.equals(new Message(Messages.Type.ERROR, "info2")));
    }
}
