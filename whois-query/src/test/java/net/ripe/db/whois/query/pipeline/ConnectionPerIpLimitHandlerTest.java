package net.ripe.db.whois.query.pipeline;

import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.UpstreamChannelStateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private IpResourceConfiguration ipResourceConfiguration;
    @Mock private WhoisLog whoisLog;
    @Mock private ApplicationVersion applicationVersion;

    private ConnectionPerIpLimitHandler subject;

    @Before
    public void setUp() {
        System.setProperty("instance.name", "10.0.0.0");
        this.subject = new ConnectionPerIpLimitHandler(ipResourceConfiguration, whoisLog, MAX_CONNECTIONS_PER_IP, applicationVersion);

        when(ctx.getChannel()).thenReturn(channel);

        when(ipResourceConfiguration.isUnlimitedConnections(any(InetAddress.class))).thenReturn(false);
        when(ipResourceConfiguration.isProxy(any(InetAddress.class))).thenReturn(false);
        when(channel.write(any())).thenReturn(channelFuture);
        when(applicationVersion.getVersion()).thenReturn("1.0");
    }

    @Test
    public void one_connected() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        verify(ctx, times(2)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_same_ip() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent openEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);

        verify(ctx, times(2)).sendUpstream(openEvent);
        verify(channel, times(1)).write(argThat(argument -> QueryMessages.connectionsExceeded(MAX_CONNECTIONS_PER_IP).equals(argument)));
        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(whoisLog).logQueryResult(anyString(), eq(0), eq(0), eq(QueryCompletionInfo.REJECTED), eq(0L), any(), Mockito.anyInt(), eq(""));
        verify(ctx, times(2)).sendUpstream(openEvent);
    }

    @Test
    public void multiple_connected_limit_disabled() throws Exception {
        this.subject = new ConnectionPerIpLimitHandler(ipResourceConfiguration, whoisLog, 0, applicationVersion);

        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent openEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);

        final ChannelEvent closeEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.FALSE);
        subject.handleUpstream(ctx, closeEvent);
        subject.handleUpstream(ctx, closeEvent);
        subject.handleUpstream(ctx, closeEvent);

        verify(ctx, times(3)).sendUpstream(openEvent);
        verify(ctx, times(3)).sendUpstream(closeEvent);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_unlimited_allowed() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(ipResourceConfiguration.isUnlimitedConnections(any(InetAddress.class))).thenReturn(true);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        verify(ctx, times(3)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_proxy_allowed() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(ipResourceConfiguration.isProxy(any(InetAddress.class))).thenReturn(true);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        verify(ctx, times(3)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_different_ip() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        final InetSocketAddress remoteAddress2 = new InetSocketAddress("10.0.0.1", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress).thenReturn(remoteAddress).thenReturn(remoteAddress2);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        final ChannelEvent event2 = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event2);

        verify(ctx, times(2)).sendUpstream(event);
        verify(ctx, times(1)).sendUpstream(event2);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_same_ip_and_closed() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);

        final ChannelEvent openEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);

        final ChannelEvent closeEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.FALSE);
        subject.handleUpstream(ctx, closeEvent);
        subject.handleUpstream(ctx, closeEvent);

        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);

        verify(ctx, times(4)).sendUpstream(openEvent);
        verify(ctx, times(2)).sendUpstream(closeEvent);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
