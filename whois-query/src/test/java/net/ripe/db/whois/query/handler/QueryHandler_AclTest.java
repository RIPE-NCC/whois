package net.ripe.db.whois.query.handler;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.*;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.InetAddress;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueryHandler_AclTest {
    @Mock WhoisLog whoisLog;
    @Mock AccessControlListManager accessControlListManager;
    @Mock SourceContext sourceContext;
    @Mock QueryExecutor queryExecutor;
    QueryHandler subject;

    int contextId = 1;
    InetAddress remoteAddress = InetAddresses.forString("193.0.0.10");
    ResponseObject message, maintainer, personTest, roleTest, roleAbuse;
    @Mock ResponseHandler responseHandler;

    @Before
    public void setUp() throws Exception {
        subject = new QueryHandler(whoisLog, accessControlListManager, sourceContext, queryExecutor);

        message = new MessageObject("test");
        maintainer = RpslObject.parse("mntner: DEV-MNT");
        personTest = RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST");
        roleTest = RpslObject.parse("role: Test Role\nnic-hdl: TR1-TEST");
        roleAbuse = RpslObject.parse("role: Abuse Role\nnic-hdl: AR1-TEST\nabuse-mailbox: abuse@ripe.net");

        when(queryExecutor.supports(any(Query.class))).thenReturn(true);
        when(queryExecutor.isAclSupported()).thenReturn(true);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final ResponseHandler responseHandler = (ResponseHandler) invocationOnMock.getArguments()[1];
                responseHandler.handle(message);
                responseHandler.handle(maintainer);
                responseHandler.handle(personTest);
                responseHandler.handle(roleTest);
                responseHandler.handle(roleAbuse);
                return null;
            }
        }).when(queryExecutor).execute(any(Query.class), any(ResponseHandler.class));

        when(sourceContext.getCurrentSource()).thenReturn(Source.slave("RIPE"));
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress)).thenReturn(true);
        when(accessControlListManager.requiresAcl(any(RpslObject.class), any(Source.class))).thenAnswer(new Answer<Object>() {
            @Override
            @SuppressWarnings("SuspiciousMethodCalls")
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return Sets.newHashSet(personTest, roleTest).contains(invocationOnMock.getArguments()[0]);
            }
        });

        when(sourceContext.getWhoisSlaveSource()).thenReturn(Source.slave("RIPE"));
    }

    @Test
    public void source_without_acl() {
        when(accessControlListManager.requiresAcl(any(RpslObject.class), any(Source.class))).thenReturn(false);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);
        verify(accessControlListManager, never()).accountPersonalObjects(any(InetAddress.class), anyInt());
        verifyLog(query, null, 0, 4);
    }

    @Test
    public void acl_with_unlimited() {
        when(accessControlListManager.isUnlimited(remoteAddress)).thenReturn(true);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);
        verify(accessControlListManager, never()).requiresAcl(any(RpslObject.class), any(Source.class));
        verify(accessControlListManager, never()).accountPersonalObjects(any(InetAddress.class), anyInt());
        verifyLog(query, null, 0, 4);
    }

    @Test
    public void acl_without_hitting_limit() {
        when(accessControlListManager.getPersonalObjects(remoteAddress)).thenReturn(10);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);
        verify(responseHandler, times(5)).handle(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest, roleTest, roleAbuse));

        verify(accessControlListManager).accountPersonalObjects(remoteAddress, 2);

        verifyLog(query, null, 2, 2);
    }

    @Test
    public void acl_hitting_limit() {
        when(accessControlListManager.getPersonalObjects(remoteAddress)).thenReturn(1);

        final Query query = Query.parse("DEV-MNT");
        try {
            subject.streamResults(query, remoteAddress, contextId, responseHandler);
            fail("Expected failure");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.BLOCKED));
            assertThat(e.getMessages(), containsInAnyOrder(QueryMessages.accessDeniedTemporarily(remoteAddress)));

            final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);
            verify(responseHandler, times(3)).handle(responseCaptor.capture());
            assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest));

            verify(accessControlListManager).accountPersonalObjects(remoteAddress, 2);

            verifyLog(query, QueryCompletionInfo.BLOCKED, 2, 1);
        }
    }

    @Test
    public void acl_with_proxy() {
        final InetAddress clientAddress = InetAddresses.forString("10.0.0.0");

        when(accessControlListManager.isAllowedToProxy(remoteAddress)).thenReturn(true);
        when(accessControlListManager.canQueryPersonalObjects(clientAddress)).thenReturn(true);
        when(accessControlListManager.getPersonalObjects(clientAddress)).thenReturn(10);

        final Query query = Query.parse("-VclientId,10.0.0.0 DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);
        verify(responseHandler, times(5)).handle(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest, roleTest, roleAbuse));

        verify(accessControlListManager).accountPersonalObjects(clientAddress, 2);

        verifyLog(query, null, 2, 2);
    }

    private void verifyLog(final Query query, final QueryCompletionInfo completionInfo, final int nrAccounted, final int nrNotAccounted) {
        verify(whoisLog).logQueryResult(anyString(), eq(nrAccounted), eq(nrNotAccounted), eq(completionInfo), anyLong(), eq(remoteAddress), eq(contextId), eq(query.toString()));
    }
}
