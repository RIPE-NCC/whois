package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
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
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NrtmConnectionPerIpLimitHandlerTest {

    private static final int MAX_CONNECTIONS_PER_IP = 1;

    @Mock private ChannelHandlerContext ctx;
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private NrtmLog nrtmLog;
    @Mock private AccessControlListManager accessControlListManager;

    private NrtmConnectionPerIpLimitHandler subject;

    @Before
    public void setUp() {
        this.subject = new NrtmConnectionPerIpLimitHandler(MAX_CONNECTIONS_PER_IP, accessControlListManager, nrtmLog);

        when(ctx.getChannel()).thenReturn(channel);

        when(channel.write(anyObject())).thenReturn(channelFuture);
    }

    @Test
    public void one_connected() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        verify(ctx, times(2)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(anyObject());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_same_ip() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent openEvent = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);
        subject.handleUpstream(ctx, openEvent);

        verify(ctx, times(2)).sendUpstream(openEvent);
        verify(channel, times(1)).write(argThat(new ArgumentMatcher<Object>() {
            @Override
            public boolean matches(Object argument) {
                return NrtmMessages.connectionsExceeded(MAX_CONNECTIONS_PER_IP).equals(argument);
            }
        }));
        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
        verify(ctx, times(2)).sendUpstream(openEvent);
    }

    @Test
    public void acl_permanently_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);

        verify(channel, times(1)).write(Matchers.eq(QueryMessages.accessDeniedPermanently(remoteAddress.getAddress())));

        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
    }

    @Test
    public void acl_temporarily_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(false);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);

        verify(channel, times(1)).write(Matchers.eq(QueryMessages.accessDeniedTemporarily(remoteAddress.getAddress())));

        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
    }

    @Test
    public void multiple_connected_unlimited_allowed() throws Exception {
        this.subject = new NrtmConnectionPerIpLimitHandler(0, accessControlListManager, nrtmLog);

        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        verify(ctx, times(3)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(anyObject());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void multiple_connected_different_ip() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        final InetSocketAddress remoteAddress2 = new InetSocketAddress("10.0.0.1", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress).thenReturn(remoteAddress).thenReturn(remoteAddress2);

        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.isDenied(remoteAddress2.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress2.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);
        subject.handleUpstream(ctx, event);

        final ChannelEvent event2 = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event2);

        verify(ctx, times(2)).sendUpstream(event);
        verify(ctx, times(1)).sendUpstream(event2);
        verify(channel, never()).close();
        verify(channel, never()).write(anyObject());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
