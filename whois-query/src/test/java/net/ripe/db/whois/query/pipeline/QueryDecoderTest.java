package net.ripe.db.whois.query.pipeline;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryDecoderTest {

    @Mock private Channel channelMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @InjectMocks private QueryDecoder subject;

    private List<Object> writtenBuffer = Lists.newArrayList();

    @Before
    public void setup() {
        when(channelMock.write(any(ChannelBuffer.class))).thenAnswer(new Answer<ChannelFuture>() {
            public ChannelFuture answer(InvocationOnMock invocation) throws Throwable {
                writtenBuffer.add(invocation.getArguments()[0]);
                return channelFutureMock;
            }
        });

        when(channelMock.getPipeline()).thenReturn(channelPipelineMock);
        when(channelHandlerContextMock.getPipeline()).thenReturn(channelPipelineMock);
        when(channelPipelineMock.getContext(QueryDecoder.class)).thenReturn(channelHandlerContextMock);
    }

    @Test(expected = QueryException.class)
    public void invalidProxyShouldThrowException() {
        Query.parse("-Vone,two,three -Tperson DW-RIPE");
    }

    @Test
    public void validDecodedStringShouldReturnQuery() throws Exception {
        String queryString = "-Tperson DW-RIPE";
        Query expectedQuery = Query.parse(queryString);

        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        Query actualQuery = (Query) subject.decode(channelHandlerContextMock, channelMock, queryString);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void invalidOptionQuery() {
        String queryString = "-Yperson DW-RIPE";
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(null, channelMock, queryString);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }

    @Test
    public void invalidProxyQuery() throws Exception {
        String queryString = "-Vone,two,three DW-RIPE";
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(null, channelMock, queryString);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }
}
