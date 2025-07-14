package net.ripe.db.whois.query.executor.decorators;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilterPlaceholderDecoratorTest {

    @Mock
    SourceContext sourceContext;
    @Mock
    AuthoritativeResourceData authoritativeResourceData;
    @Mock
    AuthoritativeResource authoritativeResource;

    Source source;

    @InjectMocks
    FilterPlaceholdersDecorator subject;

    @BeforeEach
    public void setup() {
        source = Source.slave("TEST-GRS");
        lenient().when(sourceContext.getCurrentSource()).thenReturn(source);
        lenient().when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        subject = new FilterPlaceholdersDecorator(sourceContext, authoritativeResourceData);
    }

    @Test
    public void filter_works() {
        when(sourceContext.isVirtual()).thenReturn(true);

        final List<? extends ResponseObject> toFilter = Lists.newArrayList(
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, true, true);

        final Iterator<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter).iterator();

        assertThat(result.next(), is(toFilter.get(1)));
        assertThat(result.next(), is(toFilter.get(2)));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void messagesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        final List<? extends ResponseObject> toFilter = Lists.newArrayList(
                new MessageObject(QueryMessages.duplicateIpFlagsPassed()),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, false, true);

        final Iterator<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter).iterator();

        assertThat(result.next(), is(toFilter.get(0)));
        assertThat(result.next(), is(toFilter.get(3)));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void nonResourceQueriesAreFilteredAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        final List<? extends ResponseObject> toFilter = Collections.emptyList();

        final Iterable<? extends ResponseObject> result = subject.decorate(Query.parse("10.10.10.10"), toFilter);

        assertThat(result, is(not(toFilter)));
    }

    @Test
    public void nonVirtualSourcesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(false);
        final List<? extends ResponseObject> toFilter = Collections.emptyList();

        final Iterable<? extends ResponseObject> result = subject.decorate(Query.parse("--resource 10.10.10.10"), toFilter);

        assertThat(result, is(toFilter));
    }
}
