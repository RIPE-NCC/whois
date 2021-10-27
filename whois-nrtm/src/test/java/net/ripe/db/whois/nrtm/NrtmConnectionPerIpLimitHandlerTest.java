package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NrtmConnectionPerIpLimitHandlerTest {

    private static final int MAX_CONNECTIONS_PER_IP = 1;

    @Mock private ChannelHandlerContext ctx;
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private NrtmLog nrtmLog;

    private NrtmConnectionPerIpLimitHandler subject;

    @BeforeEach
    public void setUp() {
        this.subject = new NrtmConnectionPerIpLimitHandler(MAX_CONNECTIONS_PER_IP, nrtmLog);

        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    public void one_connected() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);

        subject.channelActive(ctx);
        subject.channelActive(ctx);

        verify(ctx, times(2)).fireChannelActive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_same_ip() throws Exception {
        when(channel.write(any())).thenReturn(channelFuture);
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);

        subject.channelActive(ctx);
        subject.channelActive(ctx);
        subject.channelActive(ctx);

        verify(ctx, times(2)).fireChannelActive();
        verify(channel, times(1)).write(argThat(argument -> NrtmMessages.connectionsExceeded(MAX_CONNECTIONS_PER_IP).equals(argument)));
        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
        verify(ctx, times(2)).fireChannelActive();
    }

    @Test
    public void multiple_connected_unlimited_allowed() throws Exception {
        this.subject = new NrtmConnectionPerIpLimitHandler(0, nrtmLog);

        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.remoteAddress()).thenReturn(remoteAddress);

        subject.channelActive(ctx);
        subject.channelActive(ctx);
        subject.channelActive(ctx);

        verify(ctx, times(3)).fireChannelActive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_different_ip() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        final InetSocketAddress remoteAddress2 = new InetSocketAddress("10.0.0.1", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress).thenReturn(remoteAddress).thenReturn(remoteAddress2);

        subject.channelActive(ctx);
        subject.channelActive(ctx);

        subject.channelActive(ctx);

        verify(ctx, times(3)).fireChannelActive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

}
