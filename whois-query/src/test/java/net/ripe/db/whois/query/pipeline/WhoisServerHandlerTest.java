package net.ripe.db.whois.query.pipeline;

import com.google.common.net.InetAddresses;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WhoisServerHandlerTest {
    @Mock
    ChannelHandlerContext ctx;
    @Mock
    Channel channel;
    @Mock
    ChannelPipeline pipeline;
    @Mock
    ChannelId channelId;
    @Mock QueryHandler queryHandler;
    @InjectMocks WhoisServerHandler subject;


    InetAddress inetAddress = InetAddresses.forString("10.0.0.1");
    ResponseObject responseObject = RpslObject.parse("inetnum: 10.0.0.0");

    @Before
    public void setUp() throws Exception {
        when(ctx.channel()).thenReturn(channel);
        when(ctx.pipeline()).thenReturn(pipeline);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress(inetAddress, 80));
        when(channel.id()).thenReturn(channelId);

        doNothing().when(queryHandler).streamResults(
            any(Query.class),
            eq(inetAddress),
            any(Integer.class),
            argThat(o -> {
                o.handle(responseObject);
                return true;
            }));
    }

    @Test
    public void messageReceived_no_proxy_no_personal_object() throws Exception {
        Query msg = Query.parse("10.0.0.0");

        subject.channelRead(ctx, msg);

        verify(channel).write(responseObject);

        final ArgumentCaptor<QueryCompletedEvent> channelEventCapture = ArgumentCaptor.forClass(QueryCompletedEvent.class);
        verify(pipeline).write(channelEventCapture.capture());
        assertNull(channelEventCapture.getValue().getCompletionInfo());
    }

    @Test
    public void messageReceived_closed() throws Exception {
        Query msg = Query.parse("-V test,10.0.0.0 10.0.0.0");

        subject.channelInactive(ctx);

        try {
            subject.channelRead(ctx, msg);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.DISCONNECTED));
            assertThat(e.getMessages(), hasSize(0));
        }
    }
}
