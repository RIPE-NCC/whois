package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NrtmExceptionHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channelMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelId channelId;
    @InjectMocks private NrtmExceptionHandler subject;

    private static final String QUERY = "query";

    @BeforeEach
    public void setup() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.id()).thenReturn(channelId);
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(any())).thenReturn(channelFutureMock);
        when(channelMock.writeAndFlush(any())).thenReturn(channelFutureMock);
    }

    @Test
    public void handle_nrtm_exception() throws Exception {
        subject.exceptionCaught(channelHandlerContextMock, new NrtmException(QUERY));

        verify(channelMock, times(1)).writeAndFlush(QUERY + "\n\n");
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void handle_exception() throws Exception {
        subject.exceptionCaught(channelHandlerContextMock, new Exception());

        verify(channelMock, times(1)).writeAndFlush(NrtmMessages.internalError().toString());
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }
}

