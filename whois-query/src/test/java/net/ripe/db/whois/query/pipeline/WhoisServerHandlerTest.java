package net.ripe.db.whois.query.pipeline;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WhoisServerHandlerTest {
    @Mock ChannelHandlerContext ctx;
    @Mock Channel channel;
    @Mock ChannelPipeline pipeline;
    @Mock ChannelStateEvent channelStateEvent;
    @Mock MessageEvent messageEvent;

    @Mock QueryHandler queryHandler;
    @InjectMocks WhoisServerHandler subject;

    InetAddress inetAddress = InetAddresses.forString("10.0.0.1");
    ResponseObject responseObject = RpslObject.parse("inetnum: 10.0.0.0");

    @Before
    public void setUp() throws Exception {

        when(ctx.getChannel()).thenReturn(channel);
        when(channelStateEvent.getChannel()).thenReturn(channel);
        when(messageEvent.getChannel()).thenReturn(channel);
        when(channel.getRemoteAddress()).thenReturn(new InetSocketAddress(inetAddress, 80));
        when(channel.getPipeline()).thenReturn(pipeline);

        doNothing().when(queryHandler).streamResults(any(Query.class), eq(inetAddress), eq(0), argThat(new BaseMatcher<ResponseHandler>() {
            @Override
            public boolean matches(final Object o) {
                ((ResponseHandler) o).handle(responseObject);
                return true;
            }

            @Override
            public void describeTo(final Description description) {
            }
        }));
    }

    @Test
    public void messageReceived_no_proxy_no_personal_object() throws Exception {
        final Query query = Query.parse("10.0.0.0");
        when(messageEvent.getMessage()).thenReturn(query);

        subject.messageReceived(ctx, messageEvent);

        verify(channel).write(responseObject);

        final ArgumentCaptor<QueryCompletedEvent> channelEventCapture = ArgumentCaptor.forClass(QueryCompletedEvent.class);
        verify(pipeline).sendDownstream(channelEventCapture.capture());
        assertNull(channelEventCapture.getValue().getCompletionInfo());
    }

    @Test
    public void messageReceived_closed() throws Exception {
        final Query query = Query.parse("-V test,10.0.0.0 10.0.0.0");

        final RpslObject responseObject = mock(RpslObject.class);
        when(responseObject.getType()).thenReturn(ObjectType.INETNUM);
        when(messageEvent.getMessage()).thenReturn(query);

        subject.channelClosed(ctx, channelStateEvent);

        try {
            subject.messageReceived(ctx, messageEvent);
            fail("Expected query exception");
        } catch (QueryException e) {
            assertThat(e.getCompletionInfo(), is(QueryCompletionInfo.DISCONNECTED));
            assertThat(e.getMessages(), hasSize(0));
        }
    }
}
