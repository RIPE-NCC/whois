package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.planner.RpslResponseDecorator;
import net.ripe.db.whois.query.query.Query;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Collections;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchQueryExecutorTest {
    @Mock SourceContext sourceContext;
    @Mock RpslObjectSearcher rpslObjectSearcher;
    @Mock RpslResponseDecorator rpslResponseDecorator;
    @InjectMocks SearchQueryExecutor subject;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(rpslObjectSearcher.search(any(Query.class), any(SourceContext.class))).thenReturn((Iterable)Collections.emptyList());
        lenient().when(rpslResponseDecorator.getResponse(any(Query.class), any(Iterable.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArguments()[1];
            }
        });
    }

    @Test
    public void all_attributes_handled() {
        for (final AttributeType attributeType : AttributeType.values()) {
            assertThat(subject.supports(Query.parse("-i " + attributeType.getName() + " query")), is(true));
        }
    }

    @Test
    public void test_supports_no_attributes() {
        assertThrows(QueryException.class, () -> {
            assertThat(subject.supports(Query.parse("-i")), is(false));
        });

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
        verify(rpslObjectSearcher, never()).search(query, sourceContext);

        assertThat(responseHandler.getResponseObjects(), hasSize(1));   // make sure that e.g. 'no results found' is not printed
        assertThat(responseHandler.getResponseObjects().get(0), Matchers.<ResponseObject>is(new MessageObject(QueryMessages.unknownSource("UNKNOWN"))));
    }

    @Test
    public void query_all_sources() {
        when(sourceContext.getAllSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));

        final Query query = Query.parse("--all-sources 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query, sourceContext);
    }

    @Test
    public void query_sources() {
        final Query query = Query.parse("--sources APNIC-GRS,ARIN-GRS 10.0.0.0");

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query, sourceContext);
    }

    @Test
    public void query_sources_and_additional() {
        when(sourceContext.getAllSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));

        final Query query = Query.parse("--all-sources --sources RIPE 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext, times(3)).removeCurrentSource();
        verify(rpslObjectSearcher, times(3)).search(query, sourceContext);
    }

    @Test
    public void query_resources() {
        when(sourceContext.getGrsSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));

        final Query query = Query.parse("--resource 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query, sourceContext);
    }

    @Test
    public void query_all_sources_filters_virtual_sources() {
        when(sourceContext.getAllSourceNames()).thenReturn(ciSet("RIPE", "RIPE-GRS", "APNIC-GRS", "ARIN-GRS"));
        when(sourceContext.isVirtual(any(CIString.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();
                return (ciString("RIPE-GRS").equals(arguments[0]));
            }
        });

        final Query query = Query.parse("--all-sources 10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext, never()).setCurrent(Source.slave("RIPE-GRS"));
        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext, times(3)).removeCurrentSource();
        verify(rpslObjectSearcher, times(3)).search(query, sourceContext);
    }

    @Test
    public void query_no_source_specified() {
        when(sourceContext.getSlaveSource()).thenReturn(Source.slave("RIPE"));

        final Query query = Query.parse("10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext).removeCurrentSource();
        verify(rpslObjectSearcher).search(query, sourceContext);
    }

    @Test
    public void no_results_found_gives_message() {
        final Query query = Query.parse("-s RIPE 10.0.0.0");

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);
        verify(rpslObjectSearcher).search(query, sourceContext);
        verify(rpslResponseDecorator).getResponse(eq(query), any(Iterable.class));

        assertThat(responseHandler.getResponseObjects(), contains((ResponseObject) new MessageObject(QueryMessages.noResults("RIPE").toString())));
    }

    @Test
    public void query_additional_sources() {
        when(sourceContext.getAdditionalSourceNames()).thenReturn(ciSet("APNIC-GRS", "ARIN-GRS"));
        when(sourceContext.getSlaveSource()).thenReturn(Source.slave("RIPE"));

        final Query query = Query.parse("10.0.0.0");
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("RIPE"));
        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(3)).removeCurrentSource();
        verify(rpslObjectSearcher, times(3)).search(query, sourceContext);
    }

    @Test
    public void query_sources_not_additional() {

        final Query query = Query.parse("--sources APNIC-GRS,ARIN-GRS 10.0.0.0");

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(query, responseHandler);

        verify(sourceContext).setCurrent(Source.slave("APNIC-GRS"));
        verify(sourceContext).setCurrent(Source.slave("ARIN-GRS"));
        verify(sourceContext, times(2)).removeCurrentSource();
        verify(rpslObjectSearcher, times(2)).search(query, sourceContext);
    }

}
