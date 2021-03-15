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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NrtmAclLimitHandlerTest {

    private static final int MAX_CONNECTIONS_PER_IP = 1;

    @Mock private ChannelHandlerContext ctx;
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private NrtmLog nrtmLog;
    @Mock private AccessControlListManager accessControlListManager;

    private NrtmAclLimitHandler subject;

    @Before
    public void setUp() {
        this.subject = new NrtmAclLimitHandler(accessControlListManager, nrtmLog);

        when(ctx.getChannel()).thenReturn(channel);

        when(channel.write(any())).thenReturn(channelFuture);
    }

    @Test
    public void acl_permanently_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);

        verify(channel, times(1)).write(eq(QueryMessages.accessDeniedPermanently(remoteAddress.getAddress())));

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

        verify(channel, times(1)).write(eq(QueryMessages.accessDeniedTemporarily(remoteAddress.getAddress())));

        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
    }

    @Test
    public void acl_limit_not_breached() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.getRemoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        final ChannelEvent event = new UpstreamChannelStateEvent(channel, ChannelState.OPEN, Boolean.TRUE);
        subject.handleUpstream(ctx, event);


        verify(ctx, times(1)).sendUpstream(event);
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
