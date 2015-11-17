package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ToKeysFunctionTest {
    ToKeysFunction subject;

    @Before
    public void setUp() throws Exception {
        subject = new ToKeysFunction();
    }

    @Test
    public void apply_resonseObject() {
        final ResponseObject object = new MessageObject("text");
        final ResponseObject response = subject.apply(object);
        assertThat(response, is(object));
    }

    @Test
    public void apply_inetnum() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inetnum: 10.0.0.0\n" +
                "mnt-by:   BAR\n" +
                "source: QUX\n" +
                "abuse-mailbox: abuse@me.now");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inetnum:        10.0.0.0\n"));
    }

    @Test
    public void keys_route() {
        RpslObject rpslObject = RpslObject.parse("" +
                "route:          193.0.0.0/21\n" +
                "descr:          RIPE-NCC\n" +
                "origin:         AS3333\n" +
                "mnt-by:         RIPE-NCC-MNT\n" +
                "source:         RIPE # Filtered");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "route:          193.0.0.0/21\n" +
                "origin:         AS3333\n"));
    }

    @Test
    public void keys_as_set() {
        RpslObject rpslObject = RpslObject.parse("" +
                "as-set:         AS-TEST\n" +
                "descr:          Description\n" +
                "members:        AS2602, AS42909, AS51966\n" +
                "tech-c:         PN-RIPE\n" +
                "admin-c:        PN-RIPE\n" +
                "mnt-by:         TEST-MNT\n" +
                "source:         RIPE # Filtered");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "as-set:         AS-TEST\n" +
                "members:        AS2602, AS42909, AS51966\n"));
    }
}
