package net.ripe.db.whois.nrtm;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NrtmExceptionHandlerTest {

    @Mock private MessageEvent messageEventMock;
    @Mock private ExceptionEvent exceptionEventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @InjectMocks private NrtmExceptionHandler subject;

    private static final String QUERY = "query";

    @Before
    public void setup() {
        when(messageEventMock.getMessage()).thenReturn(QUERY);
        when(exceptionEventMock.getChannel()).thenReturn(channelMock);
        when(exceptionEventMock.getCause()).thenReturn(new Throwable());
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.isOpen()).thenReturn(true);
        when(channelMock.write(any())).thenReturn(channelFutureMock);
    }

    @Test
    public void handle_illegal_argument_exception() throws Exception {
        when(exceptionEventMock.getCause()).thenReturn(new IllegalArgumentException(QUERY));

        subject.exceptionCaught(channelHandlerContextMock, exceptionEventMock);

        verify(channelMock, times(1)).write(QUERY + "\n\n");
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void handle_exception() throws Exception {
        when(exceptionEventMock.getCause()).thenReturn(new Exception());

        subject.exceptionCaught(channelHandlerContextMock, exceptionEventMock);

        verify(channelMock, times(1)).write(NrtmExceptionHandler.MESSAGE);
        verify(channelFutureMock, times(1)).addListener(ChannelFutureListener.CLOSE);
    }
}

