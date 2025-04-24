package net.ripe.db.whois.query.handler;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.hazelcast.IpBlockManager;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.QueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class QueryHandlerAclTest {
    @Mock WhoisLog whoisLog;
    @Mock
    AccessControlListManager accessControlListManager;
    @Mock SourceContext sourceContext;
    @Mock
    IpBlockManager ipBlockManager;
    @Mock QueryExecutor queryExecutor;
    QueryHandler subject;

    int contextId = 1;
    InetAddress remoteAddress = InetAddresses.forString("193.0.0.10");
    ResponseObject message, maintainer, personTest, roleTest, roleAbuse;
    @Mock ResponseHandler responseHandler;
    private AccountingIdentifier accountingIdentifier;


    @BeforeEach
    public void setUp() throws Exception {
        subject = new QueryHandler(whoisLog, accessControlListManager, ipBlockManager, sourceContext, queryExecutor);
        subject = spy(subject);

        accountingIdentifier = new AccountingIdentifier(remoteAddress, null);
        when(accessControlListManager.getAccountingIdentifier(remoteAddress, null)).thenReturn(accountingIdentifier);

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

        lenient().when(sourceContext.getCurrentSource()).thenReturn(Source.slave("RIPE"));
        lenient().when(accessControlListManager.canQueryPersonalObjects(accountingIdentifier)).thenReturn(true);
        lenient().when(accessControlListManager.requiresAcl(any(RpslObject.class), any(Source.class), any(String.class))).thenAnswer(new Answer<Object>() {
            @Override
            @SuppressWarnings("SuspiciousMethodCalls")
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                return Sets.newHashSet(personTest, roleTest).contains(invocationOnMock.getArguments()[0]);
            }
        });
    }

    @Test
    public void source_without_acl() {
        when(accessControlListManager.requiresAcl(any(RpslObject.class), any(Source.class), any(String.class))).thenReturn(false);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        verify(accessControlListManager, never()).accountPersonalObjects(any(AccountingIdentifier.class), any(Integer.class));
        verifyLog(query, null, 0, 4);
    }

    @Test
    public void acl_with_unlimited() {
        when(accessControlListManager.isUnlimited(remoteAddress)).thenReturn(true);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        verify(accessControlListManager, never()).requiresAcl(any(RpslObject.class), any(Source.class), any(String.class));
        verify(accessControlListManager, never()).accountPersonalObjects(any(AccountingIdentifier.class), any(Integer.class));
        verifyLog(query, null, 0, 4);
    }

    @Test
    public void acl_without_hitting_limit() {
        when(accessControlListManager.getPersonalObjects( MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(remoteAddress))))).thenReturn(10);

        final Query query = Query.parse("DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);

        verify(responseHandler, times(5)).handle(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest, roleTest, roleAbuse));

        verify(accessControlListManager).accountPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(remoteAddress))),eq(2));

        verifyLog(query, null, 2, 2);
    }

    @Test
    public void acl_hitting_limit() {
        when(accessControlListManager.getPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(remoteAddress))))).thenReturn(1);

        final Query query = Query.parse("DEV-MNT");
        try {
            subject.streamResults(query, remoteAddress, contextId, responseHandler);
            fail("Expected failure");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.BLOCKED));
            assertThat(e.getMessages(), containsInAnyOrder(QueryMessages.accessDeniedTemporarily(remoteAddress.getHostAddress())));

            final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);
            verify(responseHandler, times(3)).handle(responseCaptor.capture());
            assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest));

            verify(accessControlListManager).accountPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(remoteAddress))),eq(2));

            verifyLog(query, QueryCompletionInfo.BLOCKED, 2, 1);
        }
    }

    @Test
    public void acl_with_proxy() {
        final InetAddress clientAddress = InetAddresses.forString("10.0.0.0");

        when(accessControlListManager.isAllowedToProxy(remoteAddress)).thenReturn(true);
        lenient().when(accessControlListManager.canQueryPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(remoteAddress))))).thenReturn(true);
        when(accessControlListManager.getAccountingIdentifier(clientAddress, null)).thenReturn(new AccountingIdentifier(clientAddress, null));
        when(accessControlListManager.getPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(clientAddress))))).thenReturn(10);

        final Query query = Query.parse("-VclientId,10.0.0.0 DEV-MNT");
        subject.streamResults(query, remoteAddress, contextId, responseHandler);

        final ArgumentCaptor<ResponseObject> responseCaptor = ArgumentCaptor.forClass(ResponseObject.class);
        verify(responseHandler, times(5)).handle(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues(), contains(message, maintainer, personTest, roleTest, roleAbuse));

        verify(accessControlListManager).accountPersonalObjects(MockitoHamcrest.argThat(hasProperty("remoteAddress", equalTo(clientAddress))), eq(2));

        verifyLog(query, null, 2, 2);
    }

    private void verifyLog(final Query query, final QueryCompletionInfo completionInfo, final int nrAccounted, final int nrNotAccounted) {
        verify(whoisLog).logQueryResult(any(), eq(nrAccounted), eq(nrNotAccounted), eq(completionInfo), anyLong(), eq(remoteAddress), eq(contextId), eq(query.toString()));
    }
}
