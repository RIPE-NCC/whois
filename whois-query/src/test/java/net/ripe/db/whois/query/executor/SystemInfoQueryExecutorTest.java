package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Iterator;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SystemInfoQueryExecutorTest {

    private SystemInfoQueryExecutor subject;

    @Mock private SourceContext sourceContext;

    @Before
    public void setUp() throws Exception {
        subject = new SystemInfoQueryExecutor(sourceContext);
    }

    @Test
    public void supports_version_ignore_case() {
        assertThat(subject.supports(Query.parse("-q Version")), is(true));
    }

    @Test
    public void supports_types_ignore_case() {
        assertThat(subject.supports(Query.parse("-q Types")), is(true));
    }

    @Test
    public void supports_sources_ignore_case() {
        assertThat(subject.supports(Query.parse("-q Sources")), is(true));
    }

    @Test
    public void types_query() {
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(Query.parse("-q types"), responseHandler);
        Iterator<? extends ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        String responseString = iterator.next().toString();

        assertThat(iterator.hasNext(), is(false));

        for (ObjectType objectType : ObjectType.values()) {
            assertThat(responseString, containsString(objectType.getName()));
        }
    }

    @Test
    public void types_query_invalid_argument() {
        try {
            subject.execute(Query.parse("-q invalid"), new CaptureResponseHandler());
            fail("expected QueryException to be thrown");
        } catch (QueryException qe) {
            assertThat(qe.getMessage(), containsString(QueryMessages.malformedQuery().toString()));
        }
    }

    @Test
    public void version_query() {
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(Query.parse("-q version"), responseHandler);
        Iterator<? extends ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        String responseString = iterator.next().toString();

        assertThat(iterator.hasNext(), is(false));
        assertThat(responseString, containsString("% whois-server"));
    }

    @Test
    public void sources_query() {
        when(sourceContext.getAllSourceNames()).thenReturn(ciSet("RIPE"));

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(Query.parse("-q sources"), responseHandler);
        Iterator<? extends ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        String responseString = iterator.next().toString();

        assertThat(iterator.hasNext(), is(false));
        assertThat(responseString, containsString("RIPE:3:N:0-0\n"));
    }
}
