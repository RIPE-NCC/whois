package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.planner.RpslResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchQueryExecutorTest {
    @Mock SourceContext sourceContext;
    @Mock RpslObjectSearcher rpslObjectSearcher;
    @Mock RpslResponseDecorator rpslResponseDecorator;
    @InjectMocks SearchQueryExecutor subject;

    @Before
    public void setUp() throws Exception {
        when(rpslObjectSearcher.search(any(Query.class))).thenReturn((Iterable)Collections.emptyList());
        when(rpslResponseDecorator.getResponse(any(Query.class), any(Iterable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArguments()[1];
            }
        });
    }

    @Test
    public void all_attributes_handled() {
        for (final AttributeType attributeType : AttributeType.values()) {
            assertTrue(subject.supports(Query.parse("-i " + attributeType.getName() + " query")));
        }
    }

    @Test(expected = QueryException.class)
    public void test_supports_no_attributes() {
        assertThat(subject.supports(Query.parse("-i")), is(false));
    }

    @Test
    public void test_supports_inverse_with_filter() {
        assertThat(subject.supports(Query.parse("-T inetnum -i mnt-by aardvark-mnt")), is(true));
    }

    @Test
    public void test_supports_inverse_recursive() {
        assertThat(subject.supports(Query.parse("-i mnt-by aardvark-mnt")), is(true));
    }

    @Test
    public void test_supports_inverse() {
        assertThat(subject.supports(Query.parse("-r -i mnt-by aardvark-mnt")), is(true));
    }

    @Test
    public void test_supports_inverse_multiple() {
        assertThat(subject.supports(Query.parse("-r -i mnt-by,mnt-ref aardvark-mnt")), is(true));
    }

    @Test
    public void test_supports_inverse_multiple_unknown() {
        assertThat(subject.supports(Query.parse("-r -i mnt-by,mnt-ref,mnt-lower aardvark-mnt")), is(true));
    }

    @Test
    public void unknown_source() {
        final Query query = Query.parse("-s UNKNOWN 10.0.0.0");
        doThrow(IllegalSourceException.class).when(sourceContext).setCurrent(Source.slave("UNKNOWN"));

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);
        verify(rpslObjectSearcher, never()).search(query);

        assertThat(responseHandler.getResponseObjects(), contains((ResponseObject) new MessageObject(QueryMessages.unknownSource("UNKNOWN").toString() + "\n")));
    }

    @Test
    public void query_all_sources() {
        when(sourceContext.getGrsSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));

        final Query query = Query.parse("--all-sources 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query);
    }

    @Test
    public void query_sources() {
        final Query query = Query.parse("--sources APNIC-GRS,ARIN-GRS 10.0.0.0");

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query);
    }

    @Test
    public void query_all_sources_and_additional() {
        when(sourceContext.getGrsSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));

        final Query query = Query.parse("--all-sources --sources RIPE 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext, times(3)).removeCurrentSource();
        verify(rpslObjectSearcher, times(3)).search(query);
    }

    @Test
    public void query_no_source_specified() {
        when(sourceContext.getWhoisSlaveSource()).thenReturn(Source.slave("RIPE"));

        final Query query = Query.parse("10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext).removeCurrentSource();
        verify(rpslObjectSearcher).search(query);
    }

    @Test
    public void no_results_found_gives_message() {
        final Query query = Query.parse("-s RIPE 10.0.0.0");

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);
        verify(rpslObjectSearcher).search(query);
        verify(rpslResponseDecorator).getResponse(eq(query), any(Iterable.class));

        assertThat(responseHandler.getResponseObjects(), contains((ResponseObject) new MessageObject(QueryMessages.noResults("RIPE").toString())));
    }
}
