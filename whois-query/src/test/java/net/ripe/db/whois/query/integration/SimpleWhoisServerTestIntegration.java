package net.ripe.db.whois.query.integration;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.InetAddress;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-query-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class SimpleWhoisServerTestIntegration extends AbstractQueryIntegrationTest {
    @Autowired @ReplaceWithMock private QueryHandler queryHandler;
    @Autowired @ReplaceWithMock private AccessControlListManager accessControlListManager;

    @Before
    public void setUp() throws Exception {
        when(accessControlListManager.canQueryPersonalObjects(any(InetAddress.class))).thenReturn(true);

        queryServer.start();
    }

    @After
    public void tearDown() throws Exception {
        queryServer.stop(true);
    }

    @Test
    public void performIncorrectQuery() throws IOException {
        String response = new TelnetWhoisClient(QueryServer.port).sendQuery("-W test");

        assertThat(stripHeader(response), containsString(trim(QueryMessages.malformedQuery())));
    }

    @Test
    public void performWhoisQuery() throws IOException {
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

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(stripHeader(response), containsString(queryResult));
    }

    @Test
    public void whoisQueryGivesException() throws IOException {
        doThrow(IllegalStateException.class).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery("-rBGxTinetnum 10.0.0.0");

        assertThat(stripHeader(response), Matchers.containsString("% This query was served by the RIPE Database Query"));
        assertThat(stripHeader(response), Matchers.containsString(trim(QueryMessages.internalErroroccurred())));
    }

    @Test
    public void end_of_transmission_exception() throws IOException {
        doThrow(IllegalStateException.class).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery("10.0.0.0");

        assertThat(response, Matchers.containsString("% This query was served by the RIPE Database Query"));
        assertThat(response, endsWith("\n\n\n"));
        assertThat(response, not(endsWith("\n\n\n\n")));
    }

    @Test
    public void end_of_transmission_success() {
        final String response = TelnetWhoisClient.queryLocalhost(QueryServer.port, "10.0.0.0");
        assertThat(response, endsWith("\n\n\n"));
        assertThat(response, not(endsWith("\n\n\n\n")));
    }

    @Test
    public void onConnectionShouldAlwaysGetHeaderMessage() throws IOException {
        String response = new TelnetWhoisClient(QueryServer.port).sendQuery("-rBGxTinetnum 10.0.0.0");
        assertTrue(response.startsWith(trim(QueryMessages.termsAndConditions())));
    }

    @Test
    public void sendALotOfDataShouldGiveErrorMessage() throws IOException {
        String bigString = StringUtils.repeat("Hello World!", 5000);

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(bigString);

        assertThat(response, containsString(trim(QueryMessages.inputTooLong())));
    }

    private String trim(Message message) {
        return message.toString().trim();
    }
}
