package net.ripe.db.whois.query.integration;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.pipeline.QueryChannelsRegistry;
import net.ripe.db.whois.query.pipeline.WhoisServerChannelInitializer;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.kubek2k.springockito.annotations.WrapWithSpy;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Ignore("[DA] I suggest we remove this test. For one, the changes in the API makes mocking cumbersome. But the main reason is that the utility of the test can be argued." +
        "The test basically constructs a different pipeline, starts the server with that and run assertion against that. It is testing the Netty framework rather than" +
        "actually testing the pipeline and handlers as we have them in a running whois instance")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-query-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class WhoisConnectionTestIntegration extends AbstractQueryIntegrationTest {
    @Autowired
    WhoisServerChannelInitializer whoisServerChannelInitializer;
    @Autowired @ReplaceWithMock QueryHandler queryHandler;
    @Autowired @WrapWithSpy QueryChannelsRegistry queryChannelsRegistry;

    private String queryString = "-rBGxTinetnum 10.0.0.0";
    private String queryResult = "inetnum: 127.0.0.1";

    private ChannelInboundHandler upstreamMock;

    @Before
    public void setUp() throws Exception {
        upstreamMock = Mockito.mock(ChannelInboundHandler.class, Answers.CALLS_REAL_METHODS);


//        ChannelPipeline pipeline = Channels.pipeline();
//        pipeline.addLast("open-channels", queryChannelsRegistry);
//        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(1024, true, ChannelBuffers.wrappedBuffer(new byte[]{'\n'})));
//        pipeline.addLast("string-decoder", new StringDecoder(StandardCharsets.UTF_8));
//        pipeline.addLast("whois-encoder", applicationContext.getBean(WhoisEncoder.class));
//        pipeline.addLast("exception", new ExceptionHandler());
//        pipeline.addLast("query-decoder", applicationContext.getBean(QueryDecoder.class));
//        pipeline.addLast("connection-state", new ConnectionStateHandler());
//        pipeline.addLast("upstreamMock", upstreamMock);
//
//        when(whoisServerPipelineFactory.getPipeline()).thenReturn(pipeline);



        queryServer.start();
    }

    @After
    public void tearDown() throws Exception {
        queryServer.stop(true);
    }

    @Test
    @Ignore
    public void closedChannelException_in_upstreamChannelHandler() throws Exception {
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws ClosedChannelException {
                Object[] args = invocation.getArguments();

                // Need to close the channel before throwing the ClosedChannelException
                ((ChannelHandlerContext) args[0]).channel().close();

                throw new ClosedChannelException();
            }
        }).when(upstreamMock).channelRead(any(ChannelHandlerContext.class), anyString());

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(queryString);

        assertEquals("", stripHeader(response));
    }

    @Test
    @Ignore
    public void timeoutException_in_upstreamChannelHandler() throws Exception {
        doThrow(new Exception()).when(upstreamMock).channelRead(any(ChannelHandlerContext.class), any(Object.class));

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:305: connection has been closed"));
    }

    @Test
    @Ignore
    public void tooLongFrameException_in_upstreamChannelHandler() throws Exception {
        doThrow(new Exception("")).when(upstreamMock).channelRead(any(ChannelHandlerContext.class), any());

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:107: input line too long"));
    }

    @Test
    @Ignore
    public void nullPointerException_in_upstreamChannelHandler() throws Exception {
        doThrow(new NullPointerException()).when(upstreamMock).channelRead(any(ChannelHandlerContext.class), any());

        String response = new TelnetWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:100: internal software error"));
    }
}
