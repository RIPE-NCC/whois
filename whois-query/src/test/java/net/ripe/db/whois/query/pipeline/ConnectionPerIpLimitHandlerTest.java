package net.ripe.db.whois.query.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionPerIpLimitHandlerTest {
    private static final int MAX_CONNECTIONS_PER_IP = 2;

    @Mock private ChannelHandlerContext ctx;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private IpResourceConfiguration ipResourceConfiguration;
    @Mock private WhoisLog whoisLog;
    @Mock private ApplicationVersion applicationVersion;

    private ConnectionPerIpLimitHandler subject;

    @Before
    public void setUp() {
        this.subject = new ConnectionPerIpLimitHandler(ipResourceConfiguration, whoisLog, MAX_CONNECTIONS_PER_IP, applicationVersion);

        when(ctx.channel()).thenReturn(channel);

        when(ipResourceConfiguration.isUnlimitedConnections(any(InetAddress.class))).thenReturn(false);
        when(ipResourceConfiguration.isProxy(any(InetAddress.class))).thenReturn(false);
        when(channel.write(any())).thenReturn(channelFuture);
        when(applicationVersion.getVersion()).thenReturn("1.0");
    }

    @Test
    public void one_connected() {
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
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(channel.id().asLongText()).thenReturn("anyString()");

        subject.channelActive(ctx);
        subject.channelActive(ctx);
        subject.channelActive(ctx);

        verify(ctx, times(2)).fireChannelActive();
        verify(channel, times(1)).write(argThat(argument -> QueryMessages.connectionsExceeded(MAX_CONNECTIONS_PER_IP).equals(argument)));
        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(whoisLog).logQueryResult(anyString(), eq(0), eq(0), eq(QueryCompletionInfo.REJECTED), eq(0L), any(), Mockito.anyString(), eq(""));
        verify(ctx, times(2)).fireChannelActive();
    }

    @Test
    public void multiple_connected_limit_disabled() throws Exception {
        this.subject = new ConnectionPerIpLimitHandler(ipResourceConfiguration, whoisLog, 0, applicationVersion);

        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);

        subject.channelActive(ctx);
        subject.channelActive(ctx);
        subject.channelActive(ctx);

        subject.channelInactive(ctx);
        subject.channelInactive(ctx);
        subject.channelInactive(ctx);

        verify(ctx, times(3)).fireChannelActive();
        verify(ctx, times(3)).fireChannelInactive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_unlimited_allowed() {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(ipResourceConfiguration.isUnlimitedConnections(any(InetAddress.class))).thenReturn(true);
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
    public void multiple_connected_proxy_allowed() {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(ipResourceConfiguration.isProxy(any(InetAddress.class))).thenReturn(true);
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

    @Test
    public void multiple_connected_same_ip_and_closed() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);

        subject.channelActive(ctx);
        subject.channelActive(ctx);

        subject.channelInactive(ctx);
        subject.channelInactive(ctx);

        subject.channelActive(ctx);
        subject.channelActive(ctx);

        verify(ctx, times(4)).fireChannelActive();
        verify(ctx, times(2)).fireChannelInactive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
