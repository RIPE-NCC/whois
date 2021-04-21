package net.ripe.db.whois.query.pipeline;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryDecoderTest {

    @Mock private Channel channelMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private AccessControlListManager accessControlListManager;
    @InjectMocks private QueryDecoder subject;

    private List<Object> writtenBuffer = Lists.newArrayList();

    @Before
    public void setup() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(accessControlListManager.isTrusted(any(InetAddress.class))).thenReturn(true);
    }

    @Test(expected = QueryException.class)
    public void invalidProxyShouldThrowException() {
        Query.parse("-Vone,two,three -Tperson DW-RIPE");
    }

    @Test
    public void validDecodedStringShouldReturnQuery() throws Exception {
        String queryString = "-Tperson DW-RIPE";
        Query expectedQuery = Query.parse(queryString);
        List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        subject.decode(channelHandlerContextMock, queryString, actualQuery);

        assertEquals(expectedQuery, actualQuery.get(0));
    }

    @Test
    public void invalidOptionQuery() {
        String queryString = "-Yperson DW-RIPE";
        List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(channelHandlerContextMock, queryString, actualQuery);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }

    @Test
    public void invalidProxyQuery() throws Exception {
        String queryString = "-Vone,two,three DW-RIPE";
        List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(channelHandlerContextMock, queryString, actualQuery);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }
}
