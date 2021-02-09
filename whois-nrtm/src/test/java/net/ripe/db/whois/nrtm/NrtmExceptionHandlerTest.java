package net.ripe.db.whois.nrtm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NrtmExceptionHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channelMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @InjectMocks private NrtmExceptionHandler subject;

    private static final String QUERY = "query";

    @Before
    public void setup() {
        when(channelHandlerContextMock.channel()).thenReturn(channelMock);
        when(channelMock.remoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.id().asLongText()).thenReturn("anyString()");
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(any())).thenReturn(channelFutureMock);
        when(channelMock.writeAndFlush(any())).thenReturn(channelFutureMock);
    }

    @Test
    public void handle_illegal_argument_exception() throws Exception {
        subject.exceptionCaught(channelHandlerContextMock, new IllegalArgumentException(QUERY));

        verify(channelMock, times(1)).writeAndFlush(QUERY + "\n\n");
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void handle_exception() throws Exception {
        subject.exceptionCaught(channelHandlerContextMock, new Exception());

        verify(channelMock, times(1)).write(NrtmExceptionHandler.MESSAGE);
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }
}

