package net.ripe.db.whois.query.handler;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.hazelcast.IpBlockManager;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryHandlerExceptionTest {
    @Mock WhoisLog whoisLog;
    @Mock
    AccessControlListManager accessControlListManager;
    @Mock SourceContext sourceContext;
    @Mock QueryExecutor queryExecutor;
    @Mock
    IpBlockManager ipBlockManager;
    QueryHandler subject;

    int contextId = 1;
    InetAddress remoteAddress = InetAddresses.forString("193.0.0.10");
    @Mock ResponseHandler responseHandler;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new QueryHandler(whoisLog, accessControlListManager, ipBlockManager, sourceContext, queryExecutor);
    }

    @Test
    public void query_no_source_specified() {
        doThrow(IllegalStateException.class).when(queryExecutor).execute(any(Query.class), any(ResponseHandler.class));
        when(queryExecutor.supports(any(Query.class))).thenReturn(true);

        final Query query = Query.parse("10.0.0.0");
        try {
            subject.streamResults(query, remoteAddress, contextId, responseHandler);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            verify(responseHandler, never()).handle(any(ResponseObject.class));
            verifyLog(query);
        }
    }

    private void verifyLog(final Query query) {
        verify(whoisLog).logQueryResult(isNull(), eq(0), eq(0), eq(QueryCompletionInfo.EXCEPTION), anyLong(), eq(remoteAddress), eq(contextId), eq(query.toString()));
    }
}
