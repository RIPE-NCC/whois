package net.ripe.db.whois.nrtm;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.IpAccessControlListManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class NrtmAclLimitHandlerTest {

    @Mock private ChannelHandlerContext ctx;
    @Mock private Channel channel;
    @Mock private ChannelFuture channelFuture;
    @Mock private NrtmLog nrtmLog;
    @Mock private IpAccessControlListManager IPAccessControlListManager;

    private NrtmAclLimitHandler subject;

    @BeforeEach
    public void setUp() {
        this.subject = new NrtmAclLimitHandler(IPAccessControlListManager, nrtmLog);

        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    public void acl_permanently_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(IPAccessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(true);

        try {
            subject.channelActive(ctx);
            fail();
        } catch (NrtmException e) {
            assertThat(e.getMessage(), is(QueryMessages.accessDeniedPermanently(remoteAddress.getAddress()).toString()));
            verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
        }
    }

    @Test
    public void acl_temporarily_blocked() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);

        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(IPAccessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(false);

        try {
            subject.channelActive(ctx);
            fail();
        } catch (NrtmException e) {
            assertThat(e.getMessage(), is(QueryMessages.accessDeniedTemporarily(remoteAddress.getAddress()).toString()));
            verify(nrtmLog).log(Inet4Address.getByName("10.0.0.0"), "REJECTED");
        }
    }

    @Test
    public void acl_limit_not_breached() throws Exception {
        final InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.0", 43);
        when(channel.remoteAddress()).thenReturn(remoteAddress);
        when(IPAccessControlListManager.isDenied(remoteAddress.getAddress())).thenReturn(false);
        when(IPAccessControlListManager.canQueryPersonalObjects(remoteAddress.getAddress())).thenReturn(true);

        subject.channelActive(ctx);


        verify(ctx, times(1)).fireChannelActive();
        verify(channel, never()).close();
        verify(channel, never()).write(any());
        verify(channelFuture, never()).addListener(ChannelFutureListener.CLOSE);
    }
}
