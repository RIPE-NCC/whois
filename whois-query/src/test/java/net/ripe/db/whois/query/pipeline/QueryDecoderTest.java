package net.ripe.db.whois.query.pipeline;

import com.google.common.net.InetAddresses;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Attribute;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.query.pipeline.WhoisEncoder.CHARSET_ATTRIBUTE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryDecoderTest {

    @Mock private Channel channelMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private Attribute attributeMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private AccessControlListManager accessControlListManager;
    @InjectMocks private QueryDecoder subject;

    @Test
    public void invalidProxyShouldThrowException() {
        assertThrows(QueryException.class, () -> {
            Query.parse("-Vone,two,three -Tperson DW-RIPE");
        });
    }

    @Test
    public void validDecodedStringShouldReturnQuery() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(accessControlListManager.isTrusted(any(InetAddress.class))).thenReturn(true);
        when(channelMock.attr(CHARSET_ATTRIBUTE)).thenReturn(attributeMock);

        final String queryString = "-Tperson DW-RIPE";
        final Query expectedQuery = Query.parse(queryString);
        final List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        subject.decode(channelHandlerContextMock, queryString, actualQuery);

        assertThat(actualQuery.get(0), equalTo(expectedQuery));
    }

    @Test
    public void invalidOptionQuery() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(accessControlListManager.isTrusted(any(InetAddress.class))).thenReturn(true);

        final String queryString = "-Yperson DW-RIPE";
        final List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(channelHandlerContextMock, queryString, actualQuery);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }

    @Test
    public void invalidProxyQuery() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(accessControlListManager.isTrusted(any(InetAddress.class))).thenReturn(true);

        final String queryString = "-Vone,two,three DW-RIPE";
        final List<Object> actualQuery = new ArrayList<>();
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(InetAddresses.forString("10.0.0.1"), 80));

        try {
            subject.decode(channelHandlerContextMock, queryString, actualQuery);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.PARAMETER_ERROR));
        }
    }
}
