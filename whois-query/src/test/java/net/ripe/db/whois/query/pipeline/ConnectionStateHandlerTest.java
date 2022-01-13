package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ConnectionStateHandlerTest {

    @Mock private Channel channelMock;
    @Mock private ChannelPipeline pipelineMock;
    @Mock private ChannelFuture futureMock;
    @Mock private ChannelPromise promiseMock;
    @Mock private ChannelHandlerContext contextMock;
    @InjectMocks private ConnectionStateHandler subject;

    @BeforeEach
    public void setup() {
        lenient().when(contextMock.write(any())).thenReturn(futureMock);
        lenient().when(contextMock.channel()).thenReturn(channelMock);
        lenient().when(channelMock.pipeline()).thenReturn(pipelineMock);
    }

    @Test
    public void sendingNoKFlagShouldNotEnableKeepAlive() {
        Query msg = Query.parse("help");

        subject.channelRead(contextMock, msg);
        verify(contextMock, times(1)).fireChannelRead(msg);

        subject.write(contextMock, new QueryCompletedEvent(channelMock), promiseMock);
        verify(contextMock, times(1)).write(ConnectionStateHandler.NEWLINE);
        verify(futureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void sendingNoKFlagButConnectionStateIsKeepAliveKeepItThatWay() throws Exception {
        Query msg = Query.parse("-k");
        subject.channelRead(contextMock, msg);

        subject.channelRead(contextMock, msg);
        verify(pipelineMock, atLeastOnce()).write(new QueryCompletedEvent(channelMock));


        subject.write(contextMock, new QueryCompletedEvent(channelMock), promiseMock);
        verify(contextMock, atLeastOnce()).channel();
        verify(contextMock, atLeastOnce()).write(any(QueryCompletedEvent.class));
        verify(channelMock, atLeastOnce()).write(any(byte[].class));
        verify(channelMock, atLeastOnce()).write(QueryMessages.termsAndConditions());
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void firstSingleKShouldKeepConnectionOpen() throws Exception {
        Query msg = Query.parse("-k");

        subject.channelRead(contextMock, msg);
        verify(contextMock, times(0)).write(msg);

        subject.write(contextMock, new QueryCompletedEvent(channelMock), promiseMock);
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void firstKWithArgumentsShouldKeepConnectionOpen() throws Exception {
        Query msg = Query.parse("-k -r -T inetnum 10.0.0.0");

        subject.channelRead(contextMock, msg);
        verify(contextMock, times(1)).fireChannelRead(msg);

        subject.write(contextMock, new QueryCompletedEvent(channelMock), promiseMock);
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
        verify(channelMock, times(2)).write(any());
    }

    @Test
    public void secondSingleKShouldCloseConnection() throws Exception {
        Query msg = Query.parse("-k");

        subject.channelRead(contextMock, msg);
        verify(channelMock, times(0)).close();

        subject.channelRead(contextMock, msg);
        verify(channelMock, times(1)).close();
    }

    @Test
    public void secondKWithArgumentsShouldKeepConnectionOpen() throws Exception {
        Query msg = Query.parse("-k -r -T inetnum 10.0.0.0");

        subject.channelRead(contextMock, msg);
        verify(contextMock, times(1)).fireChannelRead(msg);

        subject.write(contextMock, new QueryCompletedEvent(channelMock), promiseMock);
        verify(futureMock, times(0)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void forceCloseShouldCloseConnection() throws Exception {
        Query msg = Query.parse("-k -r -T inetnum 10.0.0.0");

        subject.channelRead(contextMock, msg);
        verify(contextMock, times(1)).fireChannelRead(msg);

        subject.write(contextMock, new QueryCompletedEvent(channelMock, QueryCompletionInfo.DISCONNECTED), promiseMock);
        verify(channelMock, times(0)).write(QueryMessages.termsAndConditions());
        verify(contextMock, times(1)).write(ConnectionStateHandler.NEWLINE);
        verify(futureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void dontActOnDownstreamNonQueryCompletedEvents() {
        subject.write(contextMock, null, promiseMock);

        verify(channelMock, times(0)).write(any());
    }
}
