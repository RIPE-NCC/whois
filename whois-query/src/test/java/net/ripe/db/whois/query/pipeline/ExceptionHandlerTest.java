package net.ripe.db.whois.query.pipeline;

import com.sun.jdi.event.ExceptionEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.timeout.ReadTimeoutException;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channelMock;
    @Mock private ChannelPipeline channelPipelineMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @InjectMocks private ExceptionHandler subject;

    private static final String QUERY = "query";

    @Before
    public void setup() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.id().asLongText()).thenReturn("anyString()");
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.isOpen()).thenReturn(true);
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
        subject.exceptionCaught(channelHandlerContextMock, new Throwable());

        verify(channelMock, times(1)).write(QueryMessages.internalErroroccurred());
    }

    @Test
    public void handle_timeout_exception() {

        subject.exceptionCaught(channelHandlerContextMock, ReadTimeoutException.INSTANCE);

        verify(channelMock, times(1)).write(QueryMessages.timeout());
    }

    @Test
    public void handle_too_long_frame_exception() {

        subject.exceptionCaught(channelHandlerContextMock, new TooLongFrameException());

        verify(channelMock, times(1)).write(QueryMessages.inputTooLong());
    }

    @Test
    public void handle_io_exception() {

        subject.exceptionCaught(channelHandlerContextMock, new IOException());

        verify(channelPipelineMock, times(1)).write(new QueryCompletedEvent(channelMock, QueryCompletionInfo.EXCEPTION));
    }

    @Test
    public void no_write_if_channel_closed() {
        when(channelMock.isOpen()).thenReturn(false);

        subject.exceptionCaught(channelHandlerContextMock, ReadTimeoutException.INSTANCE);

        verify(channelMock, times(0)).write(QueryMessages.timeout());
    }
}
