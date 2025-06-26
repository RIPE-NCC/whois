package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

        when(abuseCFinder.getAbuseContact(rpslObject)).thenReturn(Optional.empty());

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

        when(abuseCFinder.getAbuseContact(rpslObject)).thenReturn(Optional.empty());

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
        assertThat(response, is(nullValue()));
    }

    @Test
    public void apply_inet6num_abusec() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inet6num: ::0\n" +
                "mnt-by:   BAR\n" +
                "source: RIPE\n" +
                "abuse-mailbox: abuse@me.now");
        final RpslObject abuseRole = RpslObject.parse("role: Abuse Role\n" +
                "nic-hdl: AA1-TEST\n" +
                "abuse-mailbox: abusec@ripe.net"
        );

        when(abuseCFinder.getAbuseContact(rpslObject)).thenReturn(Optional.of(new AbuseContact(abuseRole, false, ciString(""))));

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
        final RpslObject abuseRole = RpslObject.parse("role: Abuse Role\n" +
                "nic-hdl: AA1-TEST\n" +
                "abuse-mailbox: abuse@ripe.net"
        );

        when(abuseCFinder.getAbuseContact(rpslObject)).thenReturn(Optional.of(new AbuseContact(abuseRole, false, ciString(""))));

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inetnum:        10.0.0.0\n" +
                "abuse-mailbox:  abuse@ripe.net\n"));
    }

    @Test
    public void apply_rootobject_abusec() {
        RpslObject rpslObject = RpslObject.parse("" +
                "inetnum: 0.0.0.0\n" +
                "mnt-by:   BAR\n" +
                "source: QUX\n" +
                "abuse-mailbox: abuse@me.now");

        when(abuseCFinder.getAbuseContact(rpslObject)).thenReturn(Optional.empty());

        final ResponseObject response = subject.apply(rpslObject);
        assertThat(response.toString(), is("" +
                "inetnum:        0.0.0.0\n" +
                "abuse-mailbox:  abuse@me.now\n"));
    }
}
