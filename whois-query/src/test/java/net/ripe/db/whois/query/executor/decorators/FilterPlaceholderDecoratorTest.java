package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterPlaceholderDecoratorTest {

    @Mock
    SourceContext sourceContext;
    @Mock
    AuthoritativeResourceData authoritativeResourceData;
    @Mock
    AuthoritativeResource authoritativeResource;
    @Mock
    Source source;

    @InjectMocks
    FilterPlaceholdersDecorator subject;

    @Before
    public void setup() {
        when(sourceContext.getCurrentSource()).thenReturn(source);
        when(source.getName()).thenReturn(ciString("TEST-GRS"));
        when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        subject = new FilterPlaceholdersDecorator(sourceContext, authoritativeResourceData);
    }

    @Test
    public void filter_works() {
        when(sourceContext.isVirtual()).thenReturn(true);

        List<? extends ResponseObject> toFilter = Lists.newArrayList(
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, true, true);

        Iterator<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter).iterator();

        assertSame(result.next(), toFilter.get(1));
        assertSame(result.next(), toFilter.get(2));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void messagesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        List<? extends ResponseObject> toFilter = Lists.newArrayList(
                new MessageObject(QueryMessages.duplicateIpFlagsPassed()),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, false, true);

        Iterator<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter).iterator();

        assertSame(result.next(), toFilter.get(0));
        assertSame(result.next(), toFilter.get(3));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void nonResourceQueriesAreFilteredAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        List<? extends ResponseObject> toFilter = Collections.emptyList();

        Iterable<? extends ResponseObject> result = subject.decorate(Query.parse("10.10.10.10"), toFilter);

        assertNotSame(result, toFilter);
    }

    @Test
    public void nonVirtualSourcesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(false);
        List<? extends ResponseObject> toFilter = Collections.emptyList();

        Iterable<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter);

        assertSame(result, toFilter);
    }
}
