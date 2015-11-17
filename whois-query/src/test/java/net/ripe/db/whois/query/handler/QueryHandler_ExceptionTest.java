package net.ripe.db.whois.query.handler;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueryHandler_ExceptionTest {
    @Mock WhoisLog whoisLog;
    @Mock AccessControlListManager accessControlListManager;
    @Mock SourceContext sourceContext;
    @Mock QueryExecutor queryExecutor;
    QueryHandler subject;

    int contextId = 1;
    InetAddress remoteAddress = InetAddresses.forString("193.0.0.10");
    @Mock ResponseHandler responseHandler;

    @Before
    public void setUp() throws Exception {
        subject = new QueryHandler(whoisLog, accessControlListManager, sourceContext, queryExecutor);

        when(queryExecutor.supports(any(Query.class))).thenReturn(true);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(true);
    }

    @Test
    public void query_no_source_specified() {
        final Query query = Query.parse("10.0.0.0");
        when(sourceContext.getWhoisSlaveSource()).thenReturn(Source.slave("RIPE"));
        doThrow(IllegalStateException.class).when(queryExecutor).execute(any(Query.class), any(ResponseHandler.class));

        try {
            subject.streamResults(query, remoteAddress, contextId, responseHandler);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            verify(responseHandler, never()).handle(any(ResponseObject.class));
            verifyLog(query);
        }
    }

    private void verifyLog(final Query query) {
        verify(whoisLog).logQueryResult(anyString(), eq(0), eq(0), eq(QueryCompletionInfo.EXCEPTION), anyLong(), eq(remoteAddress), eq(contextId), eq(query.toString()));
    }
}
