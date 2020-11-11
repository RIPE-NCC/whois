package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.query.Query;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AbuseCInfoDecoratorTest {
    @Mock private AbuseCFinder abuseCFinder;
    @Mock private SourceContext sourceContext;
    @InjectMocks AbuseCInfoDecorator subject;

    @Test
    public void notApplicable() {
        final RpslObject object = RpslObject.parse("person: Someone\nnic-hdl: NIC-TEST");

        final Iterator<? extends ResponseObject> iterator = subject.decorate(Query.parse("--abuse-contact AS3333"), Collections.singletonList(object)).iterator();
        final ResponseObject result = iterator.next();

        assertThat(result, is(object));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void inet6num_with_abuse_contact() {
        final RpslObject object = RpslObject.parse("inet6num: ffc::0/64\norg: ORG-TEST");
        final RpslObject abuseRole = RpslObject.parse("role: Abuse Role\n" +
                        "nic-hdl: AA1-TEST\n" +
                        "abuse-mailbox: abuse@ripe.net"
        );
        when(abuseCFinder.getAbuseContact(object)).thenReturn(Optional.of(new AbuseContact(abuseRole, false, ciString(""))));
        when(sourceContext.isMain()).thenReturn(true);

        final Iterator<? extends ResponseObject> iterator = subject.decorate(Query.parse("AS3333"), Collections.singletonList(object)).iterator();

        final MessageObject result = (MessageObject) iterator.next();
        assertThat(result.toString(), is("% Abuse contact for 'ffc::0/64' is 'abuse@ripe.net'\n"));
        assertThat(iterator.hasNext(), is(true));

        assertThat(iterator.next(), is(instanceOf(ResponseObject.class)));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void autnum_without_abuse_contact() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS333\nas-name: TEST-NAME\norg: ORG-TOL1-TEST");
        when(abuseCFinder.getAbuseContact(autnum)).thenReturn(Optional.empty());
        when(sourceContext.isMain()).thenReturn(true);

        final Iterator<? extends ResponseObject> iterator = subject.decorate(Query.parse("AS3333"), Collections.singletonList(autnum)).iterator();

        final MessageObject result = (MessageObject) iterator.next();

        assertThat(result.toString(), is(QueryMessages.abuseCNotRegistered("AS333").getFormattedText()));
    }
}
