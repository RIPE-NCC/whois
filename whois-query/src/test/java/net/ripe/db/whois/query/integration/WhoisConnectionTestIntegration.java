package net.ripe.db.whois.query.integration;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.pipeline.*;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.support.AbstractWhoisIntegrationTest;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.junit.After;
import org.junit.Before;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:applicationContext-query-test.xml"}, inheritLocations = false)
@Category(IntegrationTest.class)
public class WhoisConnectionTestIntegration extends AbstractWhoisIntegrationTest {
    @Autowired @ReplaceWithMock WhoisServerPipelineFactory whoisServerPipelineFactory;
    @Autowired @ReplaceWithMock QueryHandler queryHandler;
    @Autowired @WrapWithSpy QueryChannelsRegistry queryChannelsRegistry;

    private String queryString = "-rBGxTinetnum 10.0.0.0";
    private String queryResult = "inetnum: 127.0.0.1";

    private SimpleChannelUpstreamHandler upstreamMock;

    @Before
    public void setUp() throws Exception {
        upstreamMock = Mockito.mock(SimpleChannelUpstreamHandler.class, Answers.CALLS_REAL_METHODS.get());

        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("open-channels", queryChannelsRegistry);
        pipeline.addLast("delimiter", new DelimiterBasedFrameDecoder(1024, true, ChannelBuffers.wrappedBuffer(new byte[]{'\n'})));
        pipeline.addLast("string-decoder", new StringDecoder(Charsets.UTF_8));
        pipeline.addLast("whois-encoder", applicationContext.getBean(WhoisEncoder.class));
        pipeline.addLast("exception", new ExceptionHandler());
        pipeline.addLast("query-decoder", applicationContext.getBean(QueryDecoder.class));
        pipeline.addLast("connection-state", new ConnectionStateHandler());
        pipeline.addLast("upstreamMock", upstreamMock);

        when(whoisServerPipelineFactory.getPipeline()).thenReturn(pipeline);

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                ResponseHandler responseHandler = (ResponseHandler) invocationOnMock.getArguments()[3];
                responseHandler.handle(RpslObject.parse(queryResult));
                return null;
            }
        }).when(queryHandler).streamResults(any(Query.class), any(InetAddress.class), anyInt(), any(ResponseHandler.class));

        queryServer.start();
    }

    @After
    public void tearDown() throws Exception {
        queryServer.stop(true);
    }

    @Test
    public void closedChannelException_in_upstreamChannelHandler() throws Exception {
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws ClosedChannelException {
                Object[] args = invocation.getArguments();

                // Need to close the channel before throwing the ClosedChannelException
                ((MessageEvent) args[1]).getChannel().close();

                throw new ClosedChannelException();
            }
        }).when(upstreamMock).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));

        String response = new DummyWhoisClient(QueryServer.port).sendQuery(queryString);

        assertEquals("", stripHeader(response));
    }

    @Test
    public void timeoutException_in_upstreamChannelHandler() throws Exception {
        doThrow(new TimeoutException()).when(upstreamMock).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));

        String response = new DummyWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:305: connection has been closed"));
    }

    @Test
    public void tooLongFrameException_in_upstreamChannelHandler() throws Exception {
        doThrow(new TooLongFrameException("")).when(upstreamMock).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));

        String response = new DummyWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:107: input line too long"));
    }

    @Test
    public void nullPointerException_in_upstreamChannelHandler() throws Exception {
        doThrow(new NullPointerException()).when(upstreamMock).messageReceived(any(ChannelHandlerContext.class), any(MessageEvent.class));

        String response = new DummyWhoisClient(QueryServer.port).sendQuery(queryString);

        assertThat(response, containsString("%ERROR:100: internal software error"));
    }
}
