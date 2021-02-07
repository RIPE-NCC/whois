package net.ripe.db.whois.query.pipeline;

import io.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class QueryChannelsRegistryTest {

    @Mock private ChannelHandlerContext contextMock;
    @Mock private Channel channelMock;
    @Mock private ChannelFuture futureMock;
    @InjectMocks private QueryChannelsRegistry subject;

    @Before
    public void setup() {
        when(contextMock.channel()).thenReturn(channelMock);
        when(channelMock.closeFuture()).thenReturn(futureMock);
        when(channelMock.close()).thenReturn(futureMock);
        when(channelMock.id()).thenReturn(DefaultChannelId.newInstance());
    }

    @Test
    public void channel_open_records_sends_upstream() {
        subject.channelActive(contextMock);

        assertThat(subject.size(), is(1));
        verify(contextMock, times(1)).fireChannelActive();
    }

    @Test
    public void service_stop_closes_channels() {
        subject.channelActive(contextMock);

        subject.closeChannels();

        verify(channelMock, times(1)).close();
    }



}
