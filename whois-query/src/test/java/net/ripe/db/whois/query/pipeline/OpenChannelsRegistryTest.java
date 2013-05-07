package net.ripe.db.whois.query.pipeline;

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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenChannelsRegistryTest {

    @Mock private ChannelHandlerContext contextMock;
    @Mock private ChannelStateEvent eventMock;
    @Mock private Channel channelMock;
    @Mock private ChannelFuture futureMock;
    @InjectMocks private OpenChannelsRegistry subject;

    @Before
    public void setup() {
        when(eventMock.getChannel()).thenReturn(channelMock);
        when(channelMock.getCloseFuture()).thenReturn(futureMock);
        when(channelMock.close()).thenReturn(futureMock);
    }

    @Test
    public void channel_open_records_sends_upstream() {
        subject.channelOpen(contextMock, eventMock);

        assertThat(subject.size(), is(1));
        verify(contextMock, times(1)).sendUpstream(eventMock);
    }

    @Test
    public void service_stop_closes_channels() {
        subject.channelOpen(contextMock, eventMock);

        subject.stopService();

        verify(channelMock, times(1)).close();
    }



}
