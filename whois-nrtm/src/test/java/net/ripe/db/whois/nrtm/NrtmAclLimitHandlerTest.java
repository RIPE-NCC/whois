package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
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

    @Mock private ChannelHandlerContext ctx;
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private NrtmLog nrtmLog;
    @Mock private AccessControlListManager accessControlListManager;

    private NrtmAclLimitHandler subject;

    @Before
    public void setUp() {
        this.subject = new NrtmAclLimitHandler(accessControlListManager, nrtmLog);

        when(ctx.channel()).thenReturn(channel);

        when(channel.write(any())).thenReturn(channelFuture);
    }

    @Test
    public void acl_permanently_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(true);

        subject.channelActive(ctx);

        verify(channel, times(1)).write(eq(QueryMessages.accessDeniedPermanently(remoteAddress.getAddress())));

        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
    }

    @Test
    public void acl_temporarily_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(false);

        subject.channelActive(ctx);

        verify(channel, times(1)).write(eq(QueryMessages.accessDeniedTemporarily(remoteAddress.getAddress())));

        verify(channelFuture, times(1)).addListener(ChannelFutureListener.CLOSE);
        verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
    }

    @Test
    public void acl_limit_not_breached() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(accessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(accessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        subject.channelActive(ctx);


        verify(ctx, times(1)).fireChannelActive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
