package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SystemInfoQueryExecutorTest {

    @Mock private ApplicationVersion applicationVersion;
    @Mock private SourceContext sourceContext;
    @InjectMocks private SystemInfoQueryExecutor subject;

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
