package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.ReadTimeoutException;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExceptionHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channelMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelId channelId;
    @InjectMocks private ExceptionHandler subject;

    private static final String QUERY = "query";

    @BeforeEach
    public void setup() {
        when(channelMock.id()).thenReturn(channelId);
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.write(any())).thenReturn(channelFutureMock);
        when(channelMock.pipeline()).thenReturn(channelPipelineMock);
    }

    @Test
    public void record_incoming_queries() {
        subject.channelRead(channelHandlerContextMock, QUERY);

        verify(channelHandlerContextMock, times(1)).fireChannelRead(QUERY);
    }

    @Test
    public void handle_unknown_exceptions() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.isOpen()).thenReturn(true);

        subject.exceptionCaught(channelHandlerContextMock, new Throwable());

        verify(channelMock, times(1)).write(QueryMessages.internalErroroccurred());
    }

    @Test
    public void handle_timeout_exception() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.isOpen()).thenReturn(true);

        subject.exceptionCaught(channelHandlerContextMock, ReadTimeoutException.INSTANCE);

        verify(channelMock, times(1)).write(QueryMessages.timeout());
    }

    @Test
    public void handle_too_long_frame_exception() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.isOpen()).thenReturn(true);
        subject.exceptionCaught(channelHandlerContextMock, new TooLongFrameException());

        verify(channelMock, times(1)).write(QueryMessages.inputTooLong());
    }

    @Test
    public void handle_io_exception() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.isOpen()).thenReturn(true);

        subject.exceptionCaught(channelHandlerContextMock, new IOException());

        verify(channelPipelineMock, times(1)).write(new QueryCompletedEvent(channelMock, QueryCompletionInfo.EXCEPTION));
    }

    @Test
    public void no_write_if_channel_closed() {
        when(channelMock.isOpen()).thenReturn(false);
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        subject.exceptionCaught(channelHandlerContextMock, ReadTimeoutException.INSTANCE);

        verify(channelMock, times(0)).write(QueryMessages.timeout());
    }
}
