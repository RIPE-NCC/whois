package net.ripe.db.whois.query.planner;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Iterator;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AbuseCInfoFunctionTest {
    @Mock private AbuseCFinder abuseCFinder;
    @InjectMocks AbuseCInfoFunction subject;

    @Test
    public void notApplicable() {
        final RpslObject object = RpslObject.parse("person: Someone\nnic-hdl: NIC-TEST");

        final Iterator<? extends ResponseObject> iterator = subject.apply(object).iterator();
        final ResponseObject result = iterator.next();

        assertThat(result, is((ResponseObject) object));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void inet6num_with_abuse_contact() {
        final RpslObject object = RpslObject.parse("inet6num: ffc::0/64\norg: ORG-TEST");
        final HashMap<CIString, CIString> map = Maps.newHashMap();
        map.put(CIString.ciString("ffc::0/64"), CIString.ciString("abuse@ripe.net"));

        when(abuseCFinder.getAbuseContacts(object)).thenReturn(map);

        AbuseCInfoFunction subject = new AbuseCInfoFunction(abuseCFinder);
        final Iterator<? extends ResponseObject> iterator = subject.apply(object).iterator();

        final MessageObject result = (MessageObject) iterator.next();
        assertThat(result.toString(), is("% Abuse contact for 'ffc::0/64' is 'abuse@ripe.net'\n"));
        assertThat(iterator.hasNext(), is(true));

        assertThat(iterator.next(), is(instanceOf(ResponseObject.class)));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void autnum_without_abuse_contact() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS333\nas-name: TEST-NAME\norg: ORG-TOL1-TEST");
        when(abuseCFinder.getAbuseContacts(autnum)).thenReturn(new HashMap<CIString, CIString>());

        final AbuseCInfoFunction subject = new AbuseCInfoFunction(abuseCFinder);
        final Iterator<? extends ResponseObject> iterator = subject.apply(autnum).iterator();

        final MessageObject result = (MessageObject) iterator.next();

        assertThat(result.toString(), is(QueryMessages.abuseCNotRegistered("AS333").getValue()));
    }
}
