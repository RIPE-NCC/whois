package net.ripe.db.whois.query.integration;


import net.ripe.db.mock.QueryMockTestConfiguration;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.WhoisQueryTestConfiguration;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.acl.AccountingIdentifier;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {WhoisQueryTestConfiguration.class, QueryMockTestConfiguration.class}, inheritLocations = false)
@Tag("IntegrationTest")
public class SimpleWhoisServerTestIntegration extends AbstractQueryIntegrationTest {
    @Autowired private QueryHandler queryHandler;
    @Autowired private AccessControlListManager accessControlListManager;

    @BeforeEach
    public void setUp() throws Exception {
        when(accessControlListManager.canQueryPersonalObjects(any(AccountingIdentifier.class))).thenReturn(true);

        queryServer.start();
    }

    @AfterEach
    public void tearDown() {
        queryServer.stop(true);
    }

    @Test
    public void performIncorrectQuery() {
        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery("-W test");

        assertThat(stripHeader(response), containsString(trim(QueryMessages.malformedQuery())));
    }

    @Test
    public void performWhoisQuery() {
        final String queryString = "-rBGxTinetnum 10.0.0.0";
        final String queryResult = "inetnum:        10.0.0.0 - 10.255.255.255";

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                ResponseHandler responseHandler = (ResponseHandler) invocationOnMock.getArguments()[3];
                responseHandler.handle(RpslObject.parse(queryResult));
                return null;
            }
        }).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery(queryString);

        assertThat(stripHeader(response), containsString(queryResult));
    }

    @Test
    public void whoisQueryGivesException() {
        doThrow(IllegalStateException.class).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery("-rBGxTinetnum 10.0.0.0");

        assertThat(stripHeader(response), Matchers.containsString("% This query was served by the RIPE Database Query"));
        assertThat(stripHeader(response), Matchers.containsString(trim(QueryMessages.internalErroroccurred())));
    }

    @Test
    public void end_of_transmission_exception() {
        doThrow(IllegalStateException.class).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery("10.0.0.0");

        assertThat(response, Matchers.containsString("% This query was served by the RIPE Database Query"));
        assertThat(response, endsWith("\n\n\n"));
        assertThat(response, not(endsWith("\n\n\n\n")));
    }

    @Test
    public void end_of_transmission_success() {
        final String response = TelnetWhoisClient.queryLocalhost(queryServer.getPort(), "10.0.0.0");

        assertThat(response, endsWith("\n\n\n"));
        assertThat(response, not(endsWith("\n\n\n\n")));
    }

    @Test
    public void onConnectionShouldAlwaysGetHeaderMessage() throws IOException {
        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery("-rBGxTinetnum 10.0.0.0");

        assertThat(response, startsWith(trim(QueryMessages.termsAndConditions())));
    }

    @Test
    public void sendALotOfDataShouldGiveErrorMessage() throws IOException {
        final String bigString = StringUtils.repeat("Hello World!", 5000);

        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery(bigString);

        assertThat(response, containsString(trim(QueryMessages.inputTooLong())));
    }

    @Test
    public void exceptionShouldGiveErrorMessage() {
        doThrow(new NullPointerException()).when(queryHandler)
                .streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        final String response = new TelnetWhoisClient(queryServer.getPort()).sendQuery("-rBGxTinetnum 10.0.0.0");

        assertThat(response, containsString("%ERROR:100: internal software error"));
    }

    private String trim(final Message message) {
        return message.toString().trim();
    }
}
