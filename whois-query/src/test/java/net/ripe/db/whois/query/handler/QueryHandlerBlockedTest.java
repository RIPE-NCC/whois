package net.ripe.db.whois.query.handler;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpAccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryHandlerBlockedTest {
    @Mock WhoisLog whoisLog;
    @Mock
    IpAccessControlListManager IPAccessControlListManager;
    @Mock SourceContext sourceContext;
    @Mock QueryExecutor queryExecutor;
    QueryHandler subject;

    int contextId = 1;
    InetAddress remoteAddress = InetAddresses.forString("193.0.0.10");
    @Mock ResponseHandler responseHandler;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new QueryHandler(whoisLog, IPAccessControlListManager, sourceContext, queryExecutor);
        when(queryExecutor.supports(any(Query.class))).thenReturn(true);
        when(queryExecutor.isAclSupported()).thenReturn(true);
    }

    @Test
    public void blocked_permanently() {
        when(IPAccessControlListManager.isDenied(remoteAddress)).thenReturn(true);
        expectedFailure(Query.parse("10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(remoteAddress));
    }

    @Test
    public void blocked_permanently_proxy() {
        when(IPAccessControlListManager.isDenied(remoteAddress)).thenReturn(true);
        expectedFailure(Query.parse("-VclientId,11.0.0.0 10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(remoteAddress));
    }

    @Test
    public void blocked_permanently_proxy_client() {
        InetAddress clientAddress = InetAddresses.forString("11.0.0.0");
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(true);
        when(IPAccessControlListManager.isAllowedToProxy(remoteAddress)).thenReturn(true);
        lenient().when(IPAccessControlListManager.isDenied(clientAddress)).thenReturn(true);

        expectedFailure(Query.parse("-VclientId,11.0.0.0 10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(clientAddress));
    }

    @Test
    public void blocked_temporary() {
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(false);
        expectedFailure(Query.parse("10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
    }

    @Test
    public void blocked_temporary_proxy() {
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(false);
        expectedFailure(Query.parse("-VclientId,11.0.0.0 10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
    }

    @Test
    public void blocked_temporary_proxy_client() {
        InetAddress clientAddress = InetAddresses.forString("11.0.0.0");
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(true);
        when(IPAccessControlListManager.isAllowedToProxy(remoteAddress)).thenReturn(true);
        when(IPAccessControlListManager.canQueryPersonalObjects(clientAddress)).thenReturn(false);

        expectedFailure(Query.parse("-VclientId,11.0.0.0 10.0.0.0"), QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(clientAddress));
    }

    @Test
    public void proxy_not_allowed() {
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(true);
        when(IPAccessControlListManager.isAllowedToProxy(remoteAddress)).thenReturn(false);

        expectedFailure(Query.parse("-VclientId,11.0.0.0 10.0.0.0"), QueryCompletionInfo.PROXY_NOT_ALLOWED, QueryMessages.notAllowedToProxy());
    }

    private void expectedFailure(final Query query, final QueryCompletionInfo queryCompletionInfo, final Message... messages) {
        try {
            subject.streamResults(query, remoteAddress, contextId, responseHandler);
            fail("Expected failure");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(queryCompletionInfo));
            assertThat(e.getMessages(), contains(messages));

            verify(whoisLog).logQueryResult(any(), eq(0), eq(0), eq(queryCompletionInfo), anyLong(), eq(remoteAddress), eq(contextId), eq(query.toString()));
            verify(responseHandler, never()).handle(any(ResponseObject.class));
        }
    }
}
