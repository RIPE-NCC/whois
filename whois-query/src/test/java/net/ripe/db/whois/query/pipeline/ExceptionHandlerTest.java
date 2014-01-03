package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryMessages;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {

    @Mock private MessageEvent messageEventMock;
    @Mock private ExceptionEvent exceptionEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @InjectMocks private ExceptionHandler subject;

    private static final String QUERY = "query";

    @Before
    public void setup() {
        when(messageEventMock.getMessage()).thenReturn(QUERY);
        when(exceptionEventMock.getCause()).thenReturn(new Throwable());
        when(exceptionEventMock.getChannel()).thenReturn(channelMock);
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(any())).thenReturn(channelFutureMock);
        when(channelMock.getPipeline()).thenReturn(channelPipelineMock);
    }

    @Test
    public void record_incoming_queries() throws Exception {
        subject.messageReceived(channelHandlerContextMock, messageEventMock);

        verify(channelHandlerContextMock, times(1)).sendUpstream(messageEventMock);
    }

    @Test
    public void handle_unknown_exceptions() throws Exception {
        subject.exceptionCaught(channelHandlerContextMock, exceptionEventMock);

        verify(channelMock, times(1)).write(QueryMessages.internalErroroccurred());
    }

    @Test
    public void handle_timeout_exception() throws Exception {
        when(exceptionEventMock.getCause()).thenReturn(new TimeoutException());

        subject.exceptionCaught(null, exceptionEventMock);

        verify(channelMock, times(1)).write(QueryMessages.timeout());
    }

    @Test
    public void handle_too_long_frame_exception() throws Exception {
        when(exceptionEventMock.getCause()).thenReturn(new TooLongFrameException());

        subject.exceptionCaught(null, exceptionEventMock);

        verify(channelMock, times(1)).write(QueryMessages.inputTooLong());
    }

    @Test
    public void handle_io_exception() throws Exception {
        when(exceptionEventMock.getCause()).thenReturn(new IOException());

        subject.exceptionCaught(null, exceptionEventMock);

        verify(channelPipelineMock, times(1)).sendDownstream(new QueryCompletedEvent(channelMock, QueryCompletionInfo.EXCEPTION));
    }

    @Test
    public void no_write_if_channel_closed() {
        when(channelMock.isOpen()).thenReturn(false);
        when(exceptionEventMock.getCause()).thenReturn(new TimeoutException());

        subject.exceptionCaught(null, exceptionEventMock);

        verify(channelMock, times(0)).write(QueryMessages.timeout());
    }
}
