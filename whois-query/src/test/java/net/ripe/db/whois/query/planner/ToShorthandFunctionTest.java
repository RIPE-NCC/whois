package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ToShorthandFunctionTest {
    ToShorthandFunction subject;

    @Before
    public void setUp() throws Exception {
        subject = new ToShorthandFunction();
    }

    @Test
    public void apply_resonseObject() {
        final ResponseObject object = new MessageObject("text");
        final ResponseObject response = subject.apply(object);
        assertThat(response, is(object));
    }

    @Test
    public void apply_mntner() {
        RpslObject rpslObject = RpslObject.parse("" +
                "mntner: FOO\n" +
                "mnt-by:   BAR\n" +
                "source: QUX");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "*mt: FOO\n" +
                "*mb: BAR\n" +
                "*so: QUX\n"));
    }

    @Test
    public void apply_unknown() {
        RpslObject rpslObject = RpslObject.parse("" +
                "mntner:FOO\n" +
                "IREALLYDUNNO:BAR\n" +
                "source:QUX");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "*mt: FOO\n" +
                "ireallydunno:   BAR\n" +
                "*so: QUX\n"));
    }

    @Test
    public void apply_can_be_parsed_back() {
        RpslObject rpslObject = RpslObject.parse("" +
                "mntner:FOO\n" +
                "IREALLYDUNNO:BAR\n" +
                "source:QUX");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(RpslObject.parse(response.toString()), is(rpslObject));
    }

    @Test
    public void shorthand_empty_attribute() {
        RpslObject rpslObject = RpslObject.parse("" +
                "mntner: FOO\n" +
                "address:\n" +
                "source: QUX");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "*mt: FOO\n" +
                "*ad:\n" +
                "*so: QUX\n"));
    }
}
