package net.ripe.db.whois.query.planner;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BriefAbuseCFunctionTest {
    @Mock private AbuseCFinder abuseCFinder;
    @InjectMocks BriefAbuseCFunction subject;

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
                "inetnum:        10.0.0.0\n" +
                "abuse-mailbox:  abuse@me.now\n"));
    }

    @Test
    public void apply_inet6num() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inet6num: ::0\n" +
                "mnt-by:   BAR\n" +
                "source: QUX\n" +
                "abuse-mailbox: abuse@me.now");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inet6num:       ::0\n" +
                "abuse-mailbox:  abuse@me.now\n"));
    }

    @Test
    public void apply_person() {
        RpslObject rpslObject = RpslObject.parse("" +
                "person: FOO\n" +
                "mnt-by:   BAR\n" +
                "nic-hdl: FOO-QUX\n" +
                "source: QUX\n" +
                "abuse-mailbox: abuse@me.now");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "abuse-mailbox:  abuse@me.now\n"));
    }

    @Test
    public void apply_person_nothing_remains() {
        RpslObject rpslObject = RpslObject.parse("" +
                "person: FOO\n" +
                "nic-hdl: FOO-QUX\n" +
                "source: QUX");

        final ResponseObject response = subject.apply(rpslObject);
        assertNull(response);
    }

    @Test
    public void apply_inet6num_abusec() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inet6num: ::0\n" +
                "mnt-by:   BAR\n" +
                "source: RIPE\n" +
                "abuse-mailbox: abuse@me.now");
        final HashMap<CIString, CIString> map = Maps.newHashMap();
        map.put(CIString.ciString("::0"), CIString.ciString("abusec@ripe.net"));
        when(abuseCFinder.getAbuseContacts(rpslObject)).thenReturn(map);

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inet6num:       ::0\n" +
                "abuse-mailbox:  abusec@ripe.net\n"));
    }

    @Test
    public void apply_inetnum_abusec() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inetnum: 10.0.0.0\n" +
                "mnt-by:   BAR\n" +
                "source: RIPE\n" +
                "abuse-mailbox: abuse@me.now");
        final HashMap<CIString, CIString> map = Maps.newHashMap();
        map.put(CIString.ciString("10.0.0.0"), CIString.ciString("abusec@ripe.net"));

        when(abuseCFinder.getAbuseContacts(rpslObject)).thenReturn(map);

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inetnum:        10.0.0.0\n" +
                "abuse-mailbox:  abusec@ripe.net\n"));
    }

    @Test
    public void apply_rootobject_abusec() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inetnum: 0.0.0.0\n" +
                "mnt-by:   BAR\n" +
                "source: QUX\n" +
                "abuse-mailbox: abuse@me.now");

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inetnum:        0.0.0.0\n" +
                "abuse-mailbox:  abuse@me.now\n"));
    }
}
