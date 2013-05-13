package net.ripe.db.whois.nrtm;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessControlHandlerTest {
    @Mock private Channel channelMock;
    @Mock private ChannelHandlerContext channelHandlerContextMock;
    @Mock private ChannelFuture channelFutureMock;
    @Mock private ChannelStateEvent channelStateEventMock;
    @Mock private AccessControlList accessControlListMock;
    @InjectMocks private AccessControlHandler subject;

    @Before
    public void setup() {
        when(channelHandlerContextMock.getChannel()).thenReturn(channelMock);
        when(channelMock.getRemoteAddress()).thenReturn(new InetSocketAddress(0));
        when(channelMock.write(any())).thenReturn(channelFutureMock);
    }

    @Test
    public void acl_denied() throws Exception {
        when(accessControlListMock.isMirror(any(InetAddress.class))).thenReturn(false);

        subject.channelBound(channelHandlerContextMock, channelStateEventMock);

        verify(channelMock, times(1)).write(argThat(containsString("ERROR:402")));
        verify(channelHandlerContextMock, times(0)).sendUpstream(channelStateEventMock);
    }

    @Test
    public void acl_allowed() throws Exception {
        when(accessControlListMock.isMirror(any(InetAddress.class))).thenReturn(true);

        subject.channelBound(channelHandlerContextMock, channelStateEventMock);

        verify(channelMock, times(0)).write(anyString());
        verify(channelHandlerContextMock, times(1)).sendUpstream(channelStateEventMock);
    }
}
